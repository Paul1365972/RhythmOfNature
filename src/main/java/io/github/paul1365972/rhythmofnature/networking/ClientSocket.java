package io.github.paul1365972.rhythmofnature.networking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ClientSocket {
	private static final Logger LOGGER = LogManager.getLogger();
	private final int readBufferSize;
	private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<ByteBuffer> packetInQueue = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<ByteBuffer> packetOutQueue = new ConcurrentLinkedQueue<>();
	private volatile boolean running = true;
	private volatile boolean markedClosing = false;
	private Selector selector;
	private ByteBuffer readBuffer;
	private ByteBuffer swapBuffer;
	private AtomicReference<ConnectionState> state = new AtomicReference<>(ConnectionState.DISCONNECTED);
	private volatile boolean softClosed = false;
	private volatile boolean hardClosed = false;
	
	private IOException exception;
	
	private long lastPing = 0;
	
	private Thread networkingThread = new Thread(() -> {
		try {
			while (running) {
				runLoop();
				if (markedClosing && state.get() == ConnectionState.DISCONNECTED)
					running = false;
			}
		} catch (Exception e) {
			LOGGER.catching(e);
		}
		selector.keys().forEach(selectionKey -> {
			try {
				selectionKey.channel().close();
			} catch (IOException e) {
				LOGGER.catching(e);
			}
		});
		try {
			selector.close();
		} catch (IOException e) {
			LOGGER.catching(e);
		}
	}, "Networking Client Thread");
	
	public ClientSocket(int readBufferSize) {
		this.readBufferSize = readBufferSize;
		readBuffer = ByteBuffer.allocate(readBufferSize);
		swapBuffer = ByteBuffer.allocate(readBufferSize);
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		networkingThread.start();
	}
	
	public boolean nextPing(long nanoTime, long interval) {
		if (!isActive())
			return false;
		if (nanoTime > lastPing + interval) {
			lastPing = nanoTime;
			return true;
		}
		return false;
	}
	
	public void send(ByteBuffer buffer) {
		if (isActive())
			packetOutQueue.add(buffer);
	}
	
	public ByteBuffer poll() {
		return isActive() ? packetInQueue.poll() : null;
	}
	
	public boolean isActive() {
		return running && !markedClosing && !softClosed && !hardClosed && state.get() == ConnectionState.CONNECTED;
	}
	
	public IOException getException() {
		return exception;
	}
	
	public void clearException() {
		exception = null;
	}
	
	private void error(IOException exception) {
		if (this.exception == null)
			this.exception = exception;
	}
	
	public void connect(String host, int port) {
		if (!state.compareAndSet(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING))
			throw new IllegalStateException("Can only connect when disconnected");
		
		packetInQueue.clear();
		packetOutQueue.clear();
		readBuffer.clear();
		hardClosed = softClosed = false;
		
		taskQueue.add(() -> {
			try {
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				boolean connected = socketChannel.connect(new InetSocketAddress(host, port));
				socketChannel.register(selector, connected ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT, null);
				if (connected)
					state.set(ConnectionState.CONNECTED);
			} catch (IOException e) {
				error(e);
				LOGGER.warn("Error preparing connection", e);
			}
		});
		selector.wakeup();
	}
	
	public void disconnectSoft() {
		state.set(ConnectionState.DISCONNECTING);
		softClosed = true;
		selector.wakeup();
	}
	
	public void disconnectHard() {
		state.set(ConnectionState.DISCONNECTING);
		hardClosed = true;
		selector.wakeup();
	}
	
	public void markClosed() {
		markedClosing = true;
		selector.wakeup();
	}
	
	public void stop() {
		running = false;
		selector.wakeup();
		networkingThread.interrupt();
	}
	
	public ConnectionState getState() {
		return state.get();
	}
	
	private void runLoop() throws IOException {
		for (Runnable task; (task = taskQueue.poll()) != null; task.run()) ;
		
		selector.keys().stream().filter(key -> key.isValid() && key.interestOps() != SelectionKey.OP_CONNECT).forEach(key -> {
			boolean hasWritable = !packetOutQueue.isEmpty();
			key.interestOps(hasWritable ? (SelectionKey.OP_WRITE | SelectionKey.OP_READ) : SelectionKey.OP_READ);
		});
		
		selector.select();
		selector.keys().forEach(selectionKey -> {
			try {
				if (selectionKey.isValid() && selectionKey.isConnectable()) {
					connect(selectionKey);
				}
				if (selectionKey.isValid() && selectionKey.isWritable()) {
					write(selectionKey);
				}
				if (selectionKey.isValid() && selectionKey.isReadable()) {
					read(selectionKey);
				}
			} catch (IOException e) {
				error(e);
				LOGGER.warn("Network error", e);
				selectionKey.cancel();
			}
		});
		for (SelectionKey selKey : selector.keys()) {
			if (hardClosed || (softClosed && packetOutQueue.isEmpty()))
				selKey.cancel();
			if (!selKey.isValid()) {
				selKey.channel().close();
				state.set(ConnectionState.DISCONNECTED);
			}
		}
	}
	
	private void connect(SelectionKey selectionKey) throws IOException {
		if (state.compareAndSet(ConnectionState.CONNECTING, ConnectionState.CONNECTED)) {
			boolean nowConnected = ((SocketChannel) selectionKey.channel()).finishConnect();
			if (nowConnected)
				selectionKey.interestOps(SelectionKey.OP_READ);
		}
	}
	
	private void write(SelectionKey selectionKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		for (ByteBuffer cur; (cur = packetOutQueue.peek()) != null; packetOutQueue.remove()) {
			socketChannel.write(cur);
			if (cur.hasRemaining())
				break;
		}
	}
	
	private void read(SelectionKey selectionKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		int readNum = socketChannel.read(readBuffer);
		if (readNum == -1)
			throw new IOException("End of Stream reached, this is no error TODO REMOVE ME, graceful Disconnect yay");
		
		readBuffer.flip();
		int dataEnd = readBuffer.limit();
		while (readBuffer.remaining() >= 4) {
			int packetSize = readBuffer.getInt(readBuffer.position());
			if (readBuffer.capacity() < packetSize + 4)
				throw new IOException("Illegal Packet Size: " + packetSize);
			if (readBuffer.remaining() < packetSize + 4)
				break;
			readBuffer.position(readBuffer.position() + 4);
			readBuffer.limit(readBuffer.position() + packetSize);
			ByteBuffer packetBuffer = ByteBuffer.allocate(packetSize);
			packetInQueue.add(packetBuffer.put(readBuffer).flip());
			readBuffer.limit(dataEnd);
		}
		ByteBuffer tmp = swapBuffer.clear();
		tmp.put(readBuffer);
		readBuffer.clear().put(tmp);
		// Double copy is bad, optimise with swapping
	}
	
	public enum ConnectionState {
		CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
	}
	
}
