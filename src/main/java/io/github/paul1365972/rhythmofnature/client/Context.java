package io.github.paul1365972.rhythmofnature.client;

import io.github.paul1365972.rhythmofnature.client.io.Display;
import io.github.paul1365972.rhythmofnature.client.managers.ResourceManager;
import io.github.paul1365972.rhythmofnature.client.settings.GameSettings;
import io.github.paul1365972.rhythmofnature.networking.ClientProtocols;
import io.github.paul1365972.rhythmofnature.networking.ClientSocket;
import io.github.paul1365972.rhythmofnature.networking.ProtocolManager;
import io.github.paul1365972.rhythmofnature.renderer.MasterRenderer;
import io.github.paul1365972.rhythmofnature.serverapi.RhythmOfNatureServerWrapper;
import io.github.paul1365972.rhythmofnature.util.LWJGLUtils;
import io.github.paul1365972.rhythmofnature.util.Timer;
import io.github.paul1365972.rhythmofnature.world.WorldState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Context {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private String name;
	private String version;
	private boolean debug;
	
	private boolean running;
	private ConcurrentLinkedQueue<Runnable> gameTasks = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Runnable> mainTasks = new ConcurrentLinkedQueue<>();
	
	private GameSettings settings;
	private Display display;
	private Timer timer;
	
	private ResourceManager resourceManager;
	private MasterRenderer renderer;
	
	private WorldState worldState;
	private RhythmOfNatureServerWrapper server;
	private ClientSocket networkingClient;
	private ProtocolManager protocolManager;
	
	private int totalTicks, lastTotalTicks, totalRenders, lastTotalRenders;
	private int pingRTT = -1;
	
	public Context(String name, String version) {
		this.name = name;
		this.version = version;
		
		this.debug = Boolean.getBoolean("customdebug");
		if (debug) {
			LOGGER.info("Debug mode activated");
			LWJGLUtils.initDebug();
		}
	}
	
	public void initMain() {
		ImageIO.setUseCache(false);
		Locale.setDefault(Locale.ROOT);
		settings = new GameSettings();
		Display.init();
		display = new Display(this, name);
	}
	
	public void initGame() {
		running = true;
		timer = new Timer();
		display.makeContextCurrent(this);
		LWJGLUtils.initGLDebugFilter(this);
		resourceManager = new ResourceManager();
		resourceManager.reload();
		renderer = new MasterRenderer();
		renderer.init(this);
		
		protocolManager = new ProtocolManager();
		ClientProtocols.registerDefault(protocolManager);
		networkingClient = new ClientSocket(8 * 1024 * 1024);
	}
	
	public void tick() {
		totalTicks++;
		for (ByteBuffer packet; networkingClient.isActive() && (packet = networkingClient.poll()) != null; ) {
			protocolManager.processPacket(this, packet);
		}
		
		if (worldState != null)
			worldState.tick(this);
		
	}
	
	public void render() {
		totalRenders++;
		display.updateDimensions();
		if (display.pollResized())
			renderer.resize(this);
		renderer.render(this);
		
		display.swapBuffers();
	}
	
	public void runLoop() {
		processScheduledGameTasks();
		display.updateDimensions();
		timer.update(20);
		
		for (int i = 0; i < timer.getElapsedTicks(); i++) {
			tick();
		}
		
		long now = Timer.getTime();
		if (networkingClient.isActive() && networkingClient.nextPing(now, 5 * 1000))
			networkingClient.send(ClientProtocols.prepPing(now));
		
		while (timer.updateDebugTime()) {
			LOGGER.debug("TPS: {}, FPS: {}, Tick: {}", totalRenders - lastTotalRenders, totalTicks - lastTotalTicks, totalTicks);
			lastTotalTicks = totalTicks;
			lastTotalRenders = totalRenders;
		}
		
		render();
		
		timer.sync(60);
		if (display.shouldClose())
			stop();
	}
	
	public void shutdownGameThread() {
		networkingClient.stop();
		if (server != null)
			server.stop();
		renderer.cleanUp();
		display.destroyContext();
		LOGGER.info("GameThread Exit");
	}
	
	public void shutdownMainMain() {
		display.close();
		Display.terminate();
		LOGGER.info("MainThread Exit");
	}
	
	public void processScheduledMainTasks() {
		for (Runnable task; (task = mainTasks.poll()) != null; task.run()) ;
	}
	
	public void processScheduledGameTasks() {
		for (Runnable task; (task = gameTasks.poll()) != null; task.run()) ;
	}
	
	public void scheduleMainTask(Runnable task) {
		mainTasks.add(task);
	}
	
	public void scheduleGameTask(Runnable task) {
		gameTasks.add(task);
	}
	
	public void scheduleMainTask(Collection<Runnable> tasks) {
		mainTasks.addAll(tasks);
	}
	
	public void scheduleGameTask(Collection<Runnable> tasks) {
		gameTasks.addAll(tasks);
	}
	
	public void scheduleMainTask(Runnable... tasks) {
		scheduleMainTask(Arrays.asList(tasks));
	}
	
	public void scheduleGameTask(Runnable... tasks) {
		scheduleGameTask(Arrays.asList(tasks));
	}
	
	public void stop() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public Display getDisplay() {
		return display;
	}
	
	public GameSettings getSettings() {
		return settings;
	}
	
	public Timer getTimer() {
		return timer;
	}
	
	public ResourceManager getResourceManager() {
		return resourceManager;
	}
	
	public MasterRenderer getRenderer() {
		return renderer;
	}
	
	public WorldState getWorldState() {
		return worldState;
	}
	
	public RhythmOfNatureServerWrapper getServer() {
		return server;
	}
	
	public ClientSocket getNetworkingClient() {
		return networkingClient;
	}
	
	public int getTotalTicks() {
		return totalTicks;
	}
	
	public int getTotalRenders() {
		return totalRenders;
	}
	
	public int getPingRTT() {
		return pingRTT;
	}
	
	public void setPingRTT(int pingRTT) {
		this.pingRTT = pingRTT;
	}
}
