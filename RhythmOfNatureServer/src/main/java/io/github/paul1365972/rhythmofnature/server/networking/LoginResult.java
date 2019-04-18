package io.github.paul1365972.rhythmofnature.server.networking;

public class LoginResult {
	
	public static final LoginResult DENY = new LoginResult(false, 0, null);
	
	private final boolean success;
	
	private final long playerId;
	private final String name;
	
	public LoginResult(boolean success, long playerId, String name) {
		this.success = success;
		this.playerId = playerId;
		this.name = name;
	}
	
	public static LoginResult accept(long playerId, String name) {
		return new LoginResult(true, playerId, name);
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public long getPlayerId() {
		if (!isSuccess())
			throw new UnsupportedOperationException("Login Attempt was unsuccessful");
		return playerId;
	}
	
	public String getName() {
		if (!isSuccess())
			throw new UnsupportedOperationException("Login Attempt was unsuccessful");
		return name;
	}
	
	
}
