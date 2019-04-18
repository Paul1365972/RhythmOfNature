package io.github.paul1365972.rhythmofnature.server.lobby;


public class LobbyPlayer {
	
	private long playerId;
	private String name;
	
	private int characterId;
	
	public LobbyPlayer(long playerId, String name) {
		this.playerId = playerId;
		this.name = name;
	}
	
	public int getCharacterId() {
		return characterId;
	}
	
	public void setCharacterId(int characterId) {
		this.characterId = characterId;
	}
	
	public long getPlayerId() {
		return playerId;
	}
	
	public String getName() {
		return name;
	}
}
