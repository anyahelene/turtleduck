package turtleduck.util;

import java.util.ArrayList;
import java.util.List;

public class ShutdownHooks implements Runnable {
	public static final ShutdownHooks INSTANCE = new ShutdownHooks();
	private final List<Runnable> hooks = new ArrayList<>();
	private Thread thread;

	public synchronized void setup() {
		thread = new Thread(this, "TurtleDuck Shutdown Handler");
		System.err.println("Shutdown hook registered ");
		Runtime.getRuntime().addShutdownHook(thread);
	}

	public synchronized boolean register(Runnable hook) {
		if (thread == null)
			setup();
		System.err.println("Shutdown hook registered for " + hook);
		return hooks.add(hook);
	}

	public synchronized boolean unregister(Runnable hook) {
		System.err.println("Shutdown hook registered for " + hook);
		return hooks.remove(hook);
	}

	@Override
	public void run() {
		List<Runnable> todo;
		List<Throwable> exceptions = new ArrayList<>();
		System.err.println("Shutdown started ");
		synchronized (this) {
			todo = hooks;
		}
		for (Runnable hook : todo) {
			try {
				hook.run();
			} catch (Throwable t) {
				t.printStackTrace();
				exceptions.add(t);
			}
		}
		for (Throwable t : exceptions)
			throw new RuntimeException(t);
	}

}
