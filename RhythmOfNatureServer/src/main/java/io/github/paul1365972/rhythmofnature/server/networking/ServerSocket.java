package io.github.paul1365972.rhythmofnature.server.networking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSocket {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final int port, backlog;
	private final int readBufferSize;
	
	private volatile boolean markedClosing;
	private volatile boolean running;
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	private ByteBuffer swapBuffer;
	private int nextId;
	
	private ConcurrentHashMap<Integer, Connection> connections = new ConcurrentHashMap<>();
	
	private Thread thread = new Thread(() -> {
		while (running) {
			try {
				runLoop();
			} catch (IOException e) {
				LOGGER.catching(e);
				running = false;
			}
			if (markedClosing && selector.keys().stream().filter(selKey -> selKey.attachment() instanceof Connection)
										 .noneMatch(selectionKey -> ((Connection) selectionKey.attachment()).hasSendable()))
				running = false;
		}
		try {
			serverSocketChannel.close();
			selector.keys().forEach(selectionKey -> {
				try {
					selectionKey.channel().close();
				} catch (IOException e) {
					LOGGER.catching(e);
				}
			});
			selector.close();
		} catch (IOException e) {
			LOGGER.catching(e);
		}
	}, "Networking Server Thread");
	
	public ServerSocket(int port, int backlog, int readBufferSize) {
		this.port = port;
		this.backlog = backlog;
		this.readBufferSize = readBufferSize;
		swapBuffer = ByteBuffer.allocateDirect(readBufferSize);
		
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(port), backlog);
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			LOGGER.info("Created Server on port " + getPort());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getPort() {
		try {
			return ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Collection<Connection> getConnections() {
		return connections.values();
	}
	
	public void start() {
		running = true;
		thread.start();
	}
	
	public void join(long millis) throws InterruptedException {
		thread.join(millis);
	}
	
	public void markClosing() {
		markedClosing = true;
		try {
			serverSocketChannel.close();
		} catch (IOException e) {
			LOGGER.catching(e);
		}
		selector.keys().stream().map(SelectionKey::attachment).filter(att -> att instanceof Connection)
				.forEach(con -> ((Connection) con).setSoftClose(true));
		selector.wakeup();
	}
	
	public void stop() {
		running = false;
		selector.wakeup();
		thread.interrupt();
	}
	
	public boolean isStopped() {
		return !thread.isAlive();
	}
	
	public boolean isMarkedClosing() {
		return markedClosing;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	private void runLoop() throws IOException {
		for (SelectionKey selKey : selector.keys()) {
			if (selKey.attachment() instanceof Connection) {
				Connection att = ((Connection) selKey.attachment());
				if (!selKey.isValid()) {
					selKey.interestOps((att.hasSendable() ? SelectionKey.OP_WRITE : 0) | SelectionKey.OP_READ);
				}
			}
		}
		selector.select();
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		for (Iterator<SelectionKey> it = selectedKeys.iterator(); it.hasNext(); it.remove()) {
			SelectionKey selKey = it.next();
			if (selKey.isValid() && selKey.isAcceptable() && !markedClosing) {
				accept();
			}
			if (selKey.isValid() && selKey.isWritable()) {
				SocketChannel socketChannel = (SocketChannel) selKey.channel();
				Connection att = (Connection) selKey.attachment();
				boolean success = write(socketChannel, att);
				if (!success)
					selKey.cancel();
			}
			if (selKey.isValid() && selKey.isReadable() && !markedClosing) {
				SocketChannel socketChannel = (SocketChannel) selKey.channel();
				Connection att = (Connection) selKey.attachment();
				boolean success = read(socketChannel, att);
				if (!success)
					selKey.cancel();
			}
		}
		
		for (SelectionKey selKey : selector.keys()) {
			if (selKey.attachment() instanceof Connection) {
				Connection att = ((Connection) selKey.attachment());
				if (att.isHardClose() || (att.isSoftClose() && !att.hasSendable()))
					selKey.cancel();
				if (!selKey.isValid()) {
					selKey.channel().close();
					att.setState(Connection.State.DISCONNECTED);
				}
			}
		}
	}
	
	private void accept() throws IOException {
		for (SocketChannel socketChannel; (socketChannel = serverSocketChannel.accept()) != null; ) {
			try {
				socketChannel.configureBlocking(false);
				Connection att = new Connection(nextId++, readBufferSize);
				socketChannel.register(selector, SelectionKey.OP_READ, att);
				connections.put(att.getId(), att);
			} catch (IOException e) {
				LOGGER.warn("Error configuring accepted channel", e);
			}
		}
	}
	
	private boolean write(SocketChannel socketChannel, Connection att) {
		try {
			for (ByteBuffer cur; (cur = att.packetOutQueue.peek()) != null; att.packetOutQueue.remove()) {
				socketChannel.write(cur);
				if (cur.hasRemaining())
					break;
			}
			return true;
		} catch (Exception e) {
			LOGGER.warn("Error writing channel", e);
		}
		return false;
	}
	
	private boolean read(SocketChannel socketChannel, Connection att) {
		try {
			int readNum = socketChannel.read(att.readBuffer);
			if (readNum == -1)
				return false;
			
			ByteBuffer readBuffer = att.readBuffer;
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
				att.packetInQueue.add(packetBuffer.put(readBuffer).flip());
				readBuffer.limit(dataEnd);
			}
			ByteBuffer tmp = swapBuffer.clear();
			tmp.put(readBuffer);
			readBuffer.clear().put(tmp);
			// Double copy is bad, optimise with swapping
			return true;
		} catch (Exception e) {
			LOGGER.warn("Error reading channel", e);
		}
		return false;
	}
	
}
