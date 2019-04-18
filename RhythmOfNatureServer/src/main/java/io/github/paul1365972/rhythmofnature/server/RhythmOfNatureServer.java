package io.github.paul1365972.rhythmofnature.server;

import io.github.paul1365972.rhythmofnature.server.lobby.Lobby;
import io.github.paul1365972.rhythmofnature.server.networking.Connection;
import io.github.paul1365972.rhythmofnature.server.networking.ProtocolManager;
import io.github.paul1365972.rhythmofnature.server.networking.ServerProtocols;
import io.github.paul1365972.rhythmofnature.server.networking.ServerSocket;
import io.github.paul1365972.rhythmofnature.server.world.WorldState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.ByteBuffer;

public class RhythmOfNatureServer implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ServerConfigurationI config;
	
	private volatile boolean running;
	private Status status = Status.CREATED;
	
	private ServerSocket serverSocket;
	private ProtocolManager protocolManager;
	
	private Lobby lobby;
	private WorldState state;
	
	public RhythmOfNatureServer(ServerConfigurationI config) {
		this.config = config;
	}
	
	private void init() {
		running = true;
		status = Status.STARTING;
		serverSocket = new ServerSocket(8889, 10, 1 * 1024 * 1024);
		protocolManager = new ProtocolManager();
		ServerProtocols.registerDefault(protocolManager);
		
		lobby = new Lobby();
		state = new WorldState();
		state.load(new File(config.getSavesPath()));
		status = Status.RUNNING;
	}
	
	private void runLoop() {
		serverSocket.getConnections().forEach((con) -> {
			for (ByteBuffer packet; con.isActive() && (packet = con.receive()) != null; ) {
				protocolManager.processPacket(this, con, packet);
			}
		});
		
		state.tick();
		
		serverSocket.getConnections().stream().filter(con -> !con.isActive()).forEach(con -> {
			lobby.removeConnection(con);
			state.remove(con);
		});
		serverSocket.getConnections().removeIf(con -> !con.isActive());
		
		lobby.update(this);
	}
	
	public void shutdown() {
		status = Status.CLOSING;
		ByteBuffer packet = ServerProtocols.prepException("Server Shutdown");
		serverSocket.getConnections().stream().filter(Connection::isActive).forEach(connection -> connection.send(packet));
		serverSocket.markClosing();
		
		state.save(new File(config.getSavesPath()));
		try {
			serverSocket.join(5000);
		} catch (InterruptedException ignored) {
		}
		serverSocket.stop();
		status = Status.CLOSED;
	}
	
	public void run() {
		LOGGER.info("Starting Rhythm Of Nature Server");
		init();
		while (running) {
			runLoop();
			if (Thread.interrupted())
				running = false;
		}
		shutdown();
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void stop() {
		running = false;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public ProtocolManager getProtocolManager() {
		return protocolManager;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	public WorldState getState() {
		return state;
	}
	
	
	public enum Status {
		CREATED, STARTING, GENERATING, RUNNING, CLOSING, CLOSED
	}
}
