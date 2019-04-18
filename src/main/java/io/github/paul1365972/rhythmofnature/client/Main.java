package io.github.paul1365972.rhythmofnature.client;

import io.github.paul1365972.rhythmofnature.RhythmOfNature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

public class Main {
	
	public static void main(String[] args) {
		LogManager.getRootLogger();
		Logger LOGGER = LogManager.getLogger();
		String processId = ManagementFactory.getRuntimeMXBean().getName();
		LOGGER.info("Process ID: " + processId.substring(0, processId.indexOf('@')));
		LOGGER.info("Parsing arguments: " + Arrays.toString(args));
		
		Thread.currentThread().setName("Main Thread");
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("Exit");
		}, "Client Shutdown Thread"));
		
		new RhythmOfNature().run();
	}
	
}
