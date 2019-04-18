package io.github.paul1365972.rhythmofnature.networking;

public class NetworkError {
	
	private Exception exception;
	private boolean crash;
	
	public NetworkError(Exception exception, boolean crash) {
		this.exception = exception;
		this.crash = crash;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public boolean isCrash() {
		return crash;
	}
}
