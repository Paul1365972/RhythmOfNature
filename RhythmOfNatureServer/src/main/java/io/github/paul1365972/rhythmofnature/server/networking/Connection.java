package io.github.paul1365972.rhythmofnature.server.networking;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection {
	
	final ConcurrentLinkedQueue<ByteBuffer> packetInQueue = new ConcurrentLinkedQueue<>();
	final ConcurrentLinkedQueue<ByteBuffer> packetOutQueue = new ConcurrentLinkedQueue<>();
	private final int id;
	ByteBuffer readBuffer;
	private volatile State state;
	private volatile boolean softClose;
	private volatile boolean hardClose;
	
	public Connection(int id, int readBufferSize) {
		this.id = id;
		this.readBuffer = ByteBuffer.allocateDirect(readBufferSize);
	}
	
	public ByteBuffer receive() {
		if (state == State.DISCONNECTED)
			throw new IllegalStateException("Reading from unconnected connection");
		if (hardClose)
			throw new IllegalStateException("Reading from hardclosed connection");
		if (softClose)
			throw new IllegalStateException("Reading from softclosed connection");
		return packetInQueue.poll();
	}
	
	public void send(ByteBuffer buffer) {
		if (state == State.DISCONNECTED)
			throw new IllegalStateException("Sending to unconnected connection");
		if (hardClose)
			throw new IllegalStateException("Sending to hardclosed connection");
		if (softClose)
			throw new IllegalStateException("Sending to softclosed connection");
		packetOutQueue.add(buffer);
	}
	
	public void send(Collection<ByteBuffer> buffers) {
		if (state == State.DISCONNECTED)
			throw new IllegalStateException("Sending to unconnected connection");
		if (hardClose)
			throw new IllegalStateException("Sending to hardclosed connection");
		if (softClose)
			throw new IllegalStateException("Sending to softclosed connection");
		packetOutQueue.addAll(buffers);
	}
	
	public void send(ByteBuffer... buffers) {
		send(Arrays.asList(buffers));
	}
	
	public boolean hasSendable() {
		return !packetOutQueue.isEmpty();
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public boolean isActive() {
		return !hardClose && !softClose && state != State.DISCONNECTED;
	}
	
	public boolean isSoftClose() {
		return softClose;
	}
	
	public void setSoftClose(boolean softClose) {
		this.softClose = softClose;
	}
	
	public boolean isHardClose() {
		return hardClose;
	}
	
	public void setHardClose(boolean hardClose) {
		this.hardClose = hardClose;
	}
	
	public int getId() {
		return id;
	}
	
	public enum State {
		LOGGING_IN, LOBBY, PLAYING, DISCONNECTED
	}
	
}
