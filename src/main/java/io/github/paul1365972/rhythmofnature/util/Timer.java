package io.github.paul1365972.rhythmofnature.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timer {
	
	public static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;
	private static final Logger LOGGER = LogManager.getLogger();
	
	static {
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (Throwable e) {
				LOGGER.catching(e);
			}
		}, "Timer Hack Thread");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	private long lastUpdate;
	private long nextFrame;
	private int elapsedTicks;
	private float renderPartialTicks;
	private long debugUpdateTime;
	
	private Average sleepAverage;
	private Average yieldAverage;
	
	public Timer() {
		sleepAverage = new Average(1000L * 1000L);
		yieldAverage = new Average((long) (-(getTime() - getTime()) * 1.333));
		nextFrame = getTime();
		lastUpdate = getTime();
		debugUpdateTime = getTime();
		long timerFreq = org.lwjgl.glfw.GLFW.glfwGetTimerFrequency();
		LOGGER.debug("Timer Frequency: {} Hz, Timer Inaccuracy: {} ns", timerFreq, NANOS_IN_SECOND / (double) timerFreq);
	}
	
	public static long getTime() {
		return (long) (org.lwjgl.glfw.GLFW.glfwGetTime() * NANOS_IN_SECOND);
		//return System.nanoTime();
		//return System.currentTimeMillis() * 1_000_000;
	}
	
	public void update(float tps) {
		float waitTime = NANOS_IN_SECOND / tps;
		long t = getTime();
		float partialTicks = ((t - lastUpdate) / waitTime);
		lastUpdate = t;
		this.renderPartialTicks += partialTicks;
		this.elapsedTicks = (int) renderPartialTicks;
		this.renderPartialTicks -= elapsedTicks;
	}
	
	public int getElapsedTicks() {
		return elapsedTicks;
	}
	
	public float getPartialTicks() {
		return renderPartialTicks;
	}
	
	public void sync(int fps) {
		if (fps < 0)
			return;
		try {
			for (long t0 = getTime(), t1; (nextFrame - t0) > sleepAverage.avg(); t0 = t1) {
				Thread.sleep(1);
				sleepAverage.add((t1 = getTime()) - t0);
			}
			
			sleepAverage.dampenForLowResTicker();
			
			for (long t0 = getTime(), t1; (nextFrame - t0) > yieldAverage.avg(); t0 = t1) {
				Thread.yield();
				yieldAverage.add((t1 = getTime()) - t0);
			}
		} catch (InterruptedException e) {
			LOGGER.catching(e);
		}
		
		nextFrame = Math.max(nextFrame + (fps != 0 ? NANOS_IN_SECOND / fps : Long.MAX_VALUE), getTime());
	}
	
	public boolean updateDebugTime() {
		if (getTime() >= debugUpdateTime + NANOS_IN_SECOND) {
			debugUpdateTime += NANOS_IN_SECOND;
			return true;
		}
		return false;
	}
	
	private static class Average {
		private long[] slots;
		private int index;
		
		public Average(long value) {
			slots = new long[10];
			index = 0;
			for (int i = 0; i < slots.length; i++) {
				slots[i] = value;
			}
		}
		
		public void dampenForLowResTicker() {
			if (avg() > 10 * 1_000_000L) {
				for (int i = 0; i < slots.length; i++) {
					slots[i] *= 0.9f;
				}
			}
		}
		
		public void add(long value) {
			index++;
			index %= slots.length;
			slots[index] = value;
		}
		
		public long avg() {
			long result = 0;
			for (long slot : slots) {
				result += slot;
			}
			return result / slots.length;
		}
	}
}
