package io.github.paul1365972.rhythmofnature.server.lobby;

import io.github.paul1365972.rhythmofnature.server.RhythmOfNatureServer;
import io.github.paul1365972.rhythmofnature.server.networking.Connection;
import io.github.paul1365972.rhythmofnature.server.networking.ServerProtocols;
import io.github.paul1365972.rhythmofnature.server.util.BiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;


public class Lobby {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private boolean modified;
	private BiMap<Connection, LobbyPlayer> players = new BiMap<>();
	
	public void update(RhythmOfNatureServer instance) {
		if (wasModified()) {
			ByteBuffer packet = ServerProtocols.prepLobbyList(players.keySetB());
			players.keySetA().forEach(con -> con.send(packet));
		}
	}
	
	public void changeCharId(Connection con, int charId) {
		LobbyPlayer player = players.getA(con);
		if (player != null && player.getCharacterId() != charId) {
			player.setCharacterId(charId);
			modified = true;
		}
	}
	
	public void addPlayer(Connection con, long playerId, String name) {
		modified = true;
		players.put(con, new LobbyPlayer(playerId, name));
	}
	
	public LobbyPlayer getLobbyPlayer(Connection con) {
		return players.getA(con);
	}
	
	public LobbyPlayer removeConnection(Connection player) {
		LobbyPlayer removed = players.removeA(player);
		if (removed != null)
			modified = true;
		return removed;
	}
	
	public Collection<LobbyPlayer> getPlayers() {
		return Collections.unmodifiableSet(players.keySetB());
	}
	
	public Collection<Connection> getConnections() {
		return Collections.unmodifiableSet(players.keySetA());
	}
	
	private boolean wasModified() {
		boolean tmp = modified;
		modified = false;
		return tmp;
	}
	
}
