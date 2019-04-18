package io.github.paul1365972.rhythmofnature;

import io.github.paul1365972.rhythmofnature.client.Context;
import io.github.paul1365972.rhythmofnature.util.Waiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RhythmOfNature {
	public static final String NAME = "RhythmOfNature";
	public static final String VERSION = "0.0.1 Snapshot";
	private static final Logger LOGGER = LogManager.getLogger();
	
	public void run() {
		LOGGER.info("Starting Rhythm Of Nature Client");
		Context context = new Context(NAME, VERSION);
		
		context.initMain();
		Waiter waiter = new Waiter();
		new Thread(() -> {
			try {
				context.initGame();
				context.processScheduledGameTasks();
				waiter.unblock();
				while (context.isRunning()) {
					context.runLoop();
				}
			} catch (Throwable e) {
				LOGGER.catching(e);
				context.stop();
			} finally {
				waiter.unblock();
			}
			context.shutdownGameThread();
		}, "Game Thread").start();
		
		try {
			waiter.await();
			LOGGER.info("Enable Event Listener");
			while (context.isRunning()) {
				context.processScheduledMainTasks();
				context.getDisplay().pollEvents();
				Thread.sleep(1);
			}
		} catch (Throwable e) {
			LOGGER.catching(e);
			context.stop();
		}
		context.shutdownMainMain();
	}
}
