package io.github.paul1365972.rhythmofnature.networking;

import io.github.paul1365972.rhythmofnature.client.Context;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

public class ProtocolManager {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Int2ObjectMap<Handler> protocolHandlers = new Int2ObjectOpenHashMap<>();
	
	public void register(int id, HandlerFunction func) {
		if (protocolHandlers.containsKey(id))
			throw new IllegalStateException("Handler for Packet-ID " + id + " already registered (" + protocolHandlers.get(id) + ")");
		protocolHandlers.put(id, new Handler(func));
	}
	
	public void processPacket(Context context, ByteBuffer packet) {
		int packetId = packet.getInt();
		Handler handler = protocolHandlers.get(packetId);
		if (handler != null) {
			try {
				handler.getFunc().handle(packetId, context, packet);
			} catch (Exception e) {
				LOGGER.warn("Error handling packet", e);
				context.getNetworkingClient().send(ClientProtocols.prepLogOut("Client Crashed: " + e.toString()));
				context.getNetworkingClient().disconnectSoft();
			}
		} else {
			LOGGER.warn("Received unregistered PacketId: " + packetId);
		}
	}
	
	@FunctionalInterface
	public interface HandlerFunction {
		void handle(int packetId, Context context, ByteBuffer packet);
	}
	
	private class Handler {
		private final HandlerFunction func;
		
		public Handler(HandlerFunction func) {
			this.func = func;
		}
		
		public HandlerFunction getFunc() {
			return func;
		}
	}
}
