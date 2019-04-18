package io.github.paul1365972.rhythmofnatureserverapi;

import io.github.paul1365972.rhythmofnature.server.RhythmOfNatureServer;
import io.github.paul1365972.rhythmofnature.server.ServerConfigurationI;

public class RhythmOfNatureServerWrapper implements Runnable {
	
	private RhythmOfNatureServer instance;
	
	private RhythmOfNatureServerWrapper(ServerConfigurationI config) {
		this.instance = new RhythmOfNatureServer(config);
	}
	
	public static RhythmOfNatureServerWrapper create(ServerConfiguration config) {
		return new RhythmOfNatureServerWrapper(config);
	}
	
	@Override
	public void run() {
		instance.run();
	}
	
	public RhythmOfNatureServer.Status getStatus() {
		return instance.getStatus();
	}
	
	public void stop() {
		instance.stop();
	}
}
