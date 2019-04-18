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
	private AtomicReference<ConnectionState> state = new AtomicReference<>(ConnectionState.DISCONNECTED);
	private Selector selector;
	private SelectionKey selectionKey;
	private ByteBuffer readBuffer;
	private ByteBuffer swapBuffer;
	private IOException exception;
	
	private Thread networkingThread;
	
	public ClientSocket(int readBufferSize) {
		this.readBufferSize = readBufferSize;
		readBuffer = ByteBuffer.allocate(readBufferSize);
		swapBuffer = ByteBuffer.allocate(readBufferSize);
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		networkingThread = new Thread(() -> {
			try {
				while (running) {
					runLoop();
				}
				selector.close();
			} catch (Exception e) {
				LOGGER.warn("Networking Error", e);
			}
		}, "Networking Client Thread");
		networkingThread.setDaemon(true);
		networkingThread.start();
	}
	
	public void send(ByteBuffer buffer) {
		packetOutQueue.add(buffer);
	}
	
	public ByteBuffer poll() {
		return packetInQueue.poll();
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
			throw new IllegalStateException("Illegal WorldState " + state.get());
		
		taskQueue.add(() -> {
			try {
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				boolean connected = socketChannel.connect(new InetSocketAddress(host, port));
				selectionKey = socketChannel.register(selector, connected ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT, null);
				if (connected)
					state.set(ConnectionState.CONNECTED);
			} catch (IOException e) {
				error(e);
				LOGGER.warn("Error preparing connection", e);
			}
		});
		selector.wakeup();
	}
	
	public void disconnect() {
		state.set(ConnectionState.DISCONNECTING);
		taskQueue.add(() -> {
			if (selectionKey != null)
				selectionKey.cancel();
			selector.wakeup();
			packetInQueue.clear();
			packetOutQueue.clear();
			readBuffer.clear();
		});
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
		
		if (selectionKey != null && selectionKey.isValid() && selectionKey.interestOps() != SelectionKey.OP_CONNECT) {
			selectionKey.interestOps((packetOutQueue.isEmpty() ? 0 : SelectionKey.OP_WRITE) | SelectionKey.OP_READ);
		}
		
		selector.select();
		if (!selector.selectedKeys().isEmpty()) {
			try {
				if (selectionKey != null && selectionKey.isValid() && selectionKey.isConnectable()) {
					if (state.compareAndSet(ConnectionState.CONNECTING, ConnectionState.CONNECTED)) {
						boolean nowConnected = ((SocketChannel) selectionKey.channel()).finishConnect();
						if (nowConnected)
							selectionKey.interestOps(SelectionKey.OP_READ);
					}
				}
				if (selectionKey != null && selectionKey.isValid() && selectionKey.isWritable()) {
					SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
					for (ByteBuffer cur; (cur = packetOutQueue.peek()) != null; packetOutQueue.remove()) {
						socketChannel.write(cur);
						if (cur.hasRemaining())
							break;
					}
				}
				if (selectionKey != null && selectionKey.isValid() && selectionKey.isReadable()) {
					SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
					int readNum = socketChannel.read(readBuffer);
					if (readNum == -1)
						throw new IOException("End of Stream reached, this is no error REMOVE ME, graceful Disconnect yay");
					
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
			} catch (IOException e) {
				error(e);
				LOGGER.warn("Network error", e);
				selectionKey.cancel();
			}
		}
		if (selectionKey != null && !selectionKey.isValid()) {
			try {
				state.set(ConnectionState.DISCONNECTED);
				selectionKey.channel().close();
			} catch (IOException e) {
				error(e);
				LOGGER.error("Error closing invalidated channel", e);
			}
		}
	}
	
	public enum ConnectionState {
		CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
	}
	
}
