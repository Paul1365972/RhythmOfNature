package io.github.paul1365972.rhythmofnature.networking;

import io.github.paul1365972.rhythmofnature.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;

public class ClientProtocols {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int S_BYTE = 1;
	private static final int S_SHORT = 2;
	private static final int S_INT = 4;
	private static final int S_LONG = 8;
	
	private static final IntFunction<ByteBuffer> NEW_TMP_BUFFER = ByteBuffer::allocate;
	private static final IntFunction<ByteBuffer> NEW_LT_BUFFER = ByteBuffer::allocate;
	
	private ClientProtocols() {
	}
	
	public static void registerDefault(ProtocolManager pm) {
		pm.register(1, (packetId, context, packet) -> {
			long sendTime = packet.getLong();
			int rtt = (int) (Timer.getTime() - sendTime);
			LOGGER.debug("Ping Packet RTT: " + (rtt * 1_000_000f) + " ms");
			context.setPingRTT(rtt);
		});
	}
	
	public static ByteBuffer prepPing(long nanoTime) {
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT + S_LONG);
		packet.putInt(S_INT + S_LONG).putInt(1);
		packet.putLong(nanoTime);
		return packet.flip();
	}
	
	public static ByteBuffer prepLogOut(String message) {
		ByteBuffer messageBuffer = encodeString(message, StandardCharsets.UTF_8);
		ByteBuffer packet = NEW_LT_BUFFER.apply(S_INT + S_INT + messageBuffer.remaining());
		packet.putInt(S_INT + messageBuffer.remaining()).putInt(9);
		packet.put(messageBuffer);
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
