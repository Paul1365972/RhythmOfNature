package io.github.paul1365972.rhythmofnature.serverapi;

import io.github.paul1365972.rhythmofnature.server.ServerConfigurationI;

public class ServerConfiguration implements ServerConfigurationI {
	
	private String savesPath;
	
	public ServerConfiguration(String savesPath) {
		this.savesPath = savesPath;
	}
	
	@Override
	public String getSavesPath() {
		return savesPath;
	}
}
