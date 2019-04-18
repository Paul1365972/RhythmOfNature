package io.github.paul1365972.rhythmofnature.networking;

import io.github.paul1365972.rhythmofnature.client.Context;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public class ClientProtocols {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private IntFunction<ByteBuffer> NEW_BUFFER = ByteBuffer::allocateDirect;
	
	private Int2ObjectMap<Handler> protocolHandlers = new Int2ObjectOpenHashMap<>();
	
	public ByteBuffer sendPing(long nanoTime) {
		ByteBuffer packet = NEW_BUFFER.apply(16);
		packet.putInt(packet.remaining());
		packet.putInt(1);
		packet.putLong(nanoTime);
		return packet.flip();
	}
	
	public void registerDefault() {
	}
	
	public void processPacket(Context context, ByteBuffer buffer) {
		int packetSize = buffer.getInt();
		if (packetSize == buffer.remaining()) {
			int packetId = buffer.getInt();
			Handler handler = protocolHandlers.get(packetId);
			if (handler != null) {
				handler.handle(packetId, context, buffer);
			} else {
				LOGGER.warn("Received unregistered PacketId: " + packetId);
			}
		} else {
			LOGGER.warn("Packet-Buffer with bad size: " + buffer.remaining() + ", expected: " + packetSize);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Dumping Packet-Buffer: ");
				LOGGER.debug(dumpBuffer(buffer));
			}
		}
	}
	
	public void register(int id, Handler handler) {
		if (protocolHandlers.containsKey(id))
			throw new IllegalStateException("Handler for Packet-ID " + id + " already registered (" + protocolHandlers.get(id) + ")");
		protocolHandlers.put(id, handler);
	}
	
	private String dumpBuffer(ByteBuffer buffer) {
		StringBuilder sb = new StringBuilder(buffer.remaining() * 3);
		while (buffer.hasRemaining()) {
			sb.append(Integer.toHexString(buffer.get() & 0xFF)).append(' ');
		}
		return sb.toString();
	}
	
	@FunctionalInterface
	public interface Handler {
		void handle(int packetId, Context context, ByteBuffer packet);
	}
}
