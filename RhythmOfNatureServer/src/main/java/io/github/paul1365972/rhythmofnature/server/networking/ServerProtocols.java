package io.github.paul1365972.rhythmofnature.server.networking;

import io.github.paul1365972.rhythmofnature.server.Constants;
import io.github.paul1365972.rhythmofnature.server.lobby.LobbyPlayer;
import io.github.paul1365972.rhythmofnature.server.world.WorldState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class ServerProtocols {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int S_BYTE = 1;
	private static final int S_SHORT = 2;
	private static final int S_INT = 4;
	private static final int S_LONG = 8;
	
	private static final IntFunction<ByteBuffer> NEW_TMP_BUFFER = ByteBuffer::allocate;
	private static final IntFunction<ByteBuffer> NEW_LT_BUFFER = ByteBuffer::allocate;
	
	private ServerProtocols() {
	}
	
	public static void registerDefault(ProtocolManager pm) {
		pm.register(1, (packetId, instance, con, packet) -> {
			long time = packet.getLong();
			ByteBuffer packetOut = prepPing(time);
			
			con.send(packetOut);
		});
		pm.register(2, (packetId, instance, con, packet) -> {
			String pw = decodeString(packet, StandardCharsets.UTF_8);
			if (pw.contentEquals("1234")) {
				con.setState(Connection.State.LOBBY);
				long playerId = packet.getLong();
				String name = decodeString(packet, StandardCharsets.UTF_8);
				if (name.length() <= Constants.NAME_LEN_MAX) {
					// Send Server settings etc.
					// Add to server
					instance.getLobby().addPlayer(con, playerId, name);
				} else {
					con.send(prepLoginFail());
					con.setSoftClose(true);
				}
			} else {
				con.send(prepLoginFail());
				con.setSoftClose(true);
			}
		}, Connection.State.LOGGING_IN);
		pm.register(4, (packetId, instance, con, packet) -> {
			int charId = packet.getInt();
			LobbyPlayer lobbyPlayer = instance.getLobby().getLobbyPlayer(con);
			if (lobbyPlayer != null && !instance.getState().hasPlayedBefore(lobbyPlayer.getPlayerId())) {
				instance.getLobby().changeCharId(con, charId);
			}
		}, Connection.State.LOBBY);
		pm.register(10, (packetId, instance, con, packet) -> {
			LobbyPlayer lobbyPlayer = instance.getLobby().removeConnection(con);
			WorldState state = instance.getState();
			if (lobbyPlayer != null && state.isLoaded()) {
				state.add(con, lobbyPlayer.getPlayerId(), lobbyPlayer.getName(), lobbyPlayer.getCharacterId());
			}
		}, Connection.State.LOBBY);
	}
	
	public static ByteBuffer prepPing(long time) {
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT + S_LONG);
		packet.putInt(S_INT + S_LONG).putInt(1);
		packet.putLong(time);
		return packet.flip();
	}
	
	public static ByteBuffer prepLoginFail() {
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT);
		packet.putInt(S_INT).putInt(2);
		return packet.flip();
	}
	
	public static ByteBuffer prepLoginSuccess(boolean hasPlayedBefore) {
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT + S_BYTE);
		packet.putInt(S_INT + S_BYTE).putInt(3);
		packet.put((byte) (hasPlayedBefore ? 1 : 0));
		return packet.flip();
	}
	
	public static ByteBuffer prepLobbyList(Collection<LobbyPlayer> players) {
		Map<LobbyPlayer, ByteBuffer> names = new HashMap<>();
		players.forEach(player -> names.put(player, rawEncodeString(player.getName(), StandardCharsets.UTF_8)));
		int namesBytes = names.values().stream().mapToInt(Buffer::remaining).sum() + names.size() * S_SHORT;
		int totalSize = S_INT + namesBytes + names.size() * (S_LONG + S_INT);
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + totalSize);
		packet.putInt(totalSize).putInt(4);
		packet.putInt(names.size());
		names.forEach((player, name) -> packet.putLong(player.getPlayerId()).putShort((short) name.remaining()).put(name)
											  .putInt(player.getCharacterId()));
		return packet.flip();
	}
	
	public static ByteBuffer prepException(String message) {
		ByteBuffer messageBuffer = encodeString(message, StandardCharsets.UTF_8);
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT + messageBuffer.remaining());
		packet.putInt(S_INT + messageBuffer.remaining()).putInt(8);
		packet.put(messageBuffer);
		return packet.flip();
	}
	
	public static ByteBuffer prepLoggedOut() {
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT);
		packet.putInt(S_INT).putInt(9);
		return packet.flip();
	}
	
	private static String decodeString(ByteBuffer bb, Charset cs) {
		int length = Short.toUnsignedInt(bb.getShort());
		int oldLimit = bb.limit();
		CharBuffer cb = cs.decode(bb.limit(bb.position() + length));
		bb.limit(oldLimit);
		return cb.toString();
	}
	
	private static ByteBuffer encodeString(String str, Charset cs) {
		ByteBuffer chars = rawEncodeString(str, cs);
		int len = chars.remaining();
		ByteBuffer ret = NEW_TMP_BUFFER.apply(S_SHORT + len);
		return ret.putShort((short) len).put(chars).flip();
	}
	
	private static ByteBuffer encodeString(String str, ByteBuffer buffer, Charset cs) {
		ByteBuffer chars = rawEncodeString(str, cs);
		int len = chars.remaining();
		return buffer.putShort((short) len).put(chars);
	}
	
	private static ByteBuffer rawEncodeString(String str, Charset cs) {
		ByteBuffer chars = cs.encode(str);
		if (chars.remaining() > Short.toUnsignedInt((short) -1))
			throw new IllegalArgumentException("String too long to encode: " + chars.remaining());
		return chars;
	}
	
	private static String dumpBuffer(ByteBuffer buffer) {
		int inRow = 0;
		StringBuilder sb = new StringBuilder(buffer.remaining() * 4);
		while (buffer.hasRemaining()) {
			if (++inRow > 16) {
				inRow = 1;
				sb.append(System.lineSeparator());
			}
			sb.append(Integer.toHexString(buffer.get() & 0xFF)).append(' ');
		}
		return sb.substring(0, sb.length() - 2);
	}
	
	
}
