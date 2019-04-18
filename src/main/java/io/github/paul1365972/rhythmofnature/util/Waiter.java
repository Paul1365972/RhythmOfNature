package io.github.paul1365972.rhythmofnature.util;

public class Waiter {
	
	private final Object lock = new Object();
	private volatile boolean blocked = true;
	
	public void await() throws InterruptedException {
		if (blocked) {
			synchronized (lock) {
				while (blocked) {
					lock.wait();
				}
			}
		}
	}
	
	public boolean awaitChecked() {
		try {
			await();
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public void unblock() {
		if (blocked) {
			blocked = false;
			synchronized (lock) {
				lock.notify();
			}
		}
	}
}
