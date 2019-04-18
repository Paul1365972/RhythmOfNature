package io.github.paul1365972.rhythmofnature.server.world;

import io.github.paul1365972.rhythmofnature.server.networking.Connection;
import io.github.paul1365972.rhythmofnature.server.util.BiMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class WorldState {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Long2ObjectMap<OfflinePlayer> offlinePlayers = new Long2ObjectOpenHashMap<>();
	
	private boolean loaded;
	private Int2ObjectMap<Connection> connections = new Int2ObjectOpenHashMap<>();
	private BiMap<Integer, Long> idUuidMap = new BiMap<>();
	private Long2ObjectMap<Player> players = new Long2ObjectOpenHashMap<>();
	
	public void tick() {
	}
	
	public void add(Connection con, long playerId, String name, int characterId) {
		connections.put(con.getId(), con);
		idUuidMap.put(con.getId(), playerId);
		if (offlinePlayers.containsKey(playerId)) {
			// Use old player
		} else {
			// Create new player
		}
		
		LOGGER.info("Player " + name + " [" + playerId + "] connected as Number " + con.getId());
	}
	
	public void remove(Connection con) {
	
	}
	
	public boolean hasPlayedBefore(long playerId) {
		if (!loaded)
			throw new IllegalStateException("Testing if someone has played before while not loaded");
		return offlinePlayers.containsKey(playerId);
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public void load(File path) {
		
		loaded = true;
	}
	
	public void save(File file) {
	
	}
	
}
