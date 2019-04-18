package io.github.paul1365972.rhythmofnature.server.networking;

import io.github.paul1365972.rhythmofnature.server.RhythmOfNatureServer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ProtocolManager {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Int2ObjectMap<Handler> protocolHandlers = new Int2ObjectOpenHashMap<>();
	
	public void register(int id, HandlerFunction func, Connection.State... required) {
		if (protocolHandlers.containsKey(id))
			throw new IllegalStateException("Handler for Packet-ID " + id + " already registered (" + protocolHandlers.get(id) + ")");
		protocolHandlers.put(id, new Handler(func, required));
	}
	
	public void processPacket(RhythmOfNatureServer instance, Connection con, ByteBuffer packet) {
		int packetId = packet.getInt();
		Handler handler = protocolHandlers.get(packetId);
		if (handler != null) {
			if (handler.getRequired().isEmpty() || handler.getRequired().contains(con.getState())) {
				try {
					handler.getFunc().handle(packetId, instance, con, packet);
				} catch (Exception e) {
					LOGGER.warn("Error handling packet", e);
					con.send(ServerProtocols.prepException("Internal Serer Error: " + e.toString()));
					con.setSoftClose(true);
				}
			} else {
				LOGGER.warn("Illegal connection state " + con.getState() + " for PacketId " + packetId);
			}
		} else {
			LOGGER.warn("Received unregistered PacketId: " + packetId);
		}
	}
	
	@FunctionalInterface
	public interface HandlerFunction {
		void handle(int packetId, RhythmOfNatureServer instance, Connection con, ByteBuffer packet);
	}
	
	private class Handler {
		private final HandlerFunction func;
		private final List<Connection.State> required;
		
		public Handler(HandlerFunction func, Connection.State... required) {
			this.func = func;
			this.required = Arrays.asList(required);
		}
		
		public HandlerFunction getFunc() {
			return func;
		}
		
		public List<Connection.State> getRequired() {
			return required;
		}
	}
}
