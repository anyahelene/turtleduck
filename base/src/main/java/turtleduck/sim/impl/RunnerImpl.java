package turtleduck.sim.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import turtleduck.sim.StepwiseRunner;
import turtleduck.sim.StepwiseSimulation;
import turtleduck.util.ShutdownHooks;

public class RunnerImpl<T> implements StepwiseRunner<T>, Runnable {
	private static final long NS = 1_000_000_000;
	private StepwiseSimulation<T> sim;
	private T state1, state2;
	private int stepNum = 0;
	private boolean running = false;
	private final ScheduledExecutorService executor;
	private ScheduledFuture<?> initFuture;
	private ScheduledFuture<?> stepFuture;
	private long delay = 1000;
	private long nanos = 0;
	private long timeBudget = 0;
	private Runnable exitHook;

	public RunnerImpl() {
		this(Executors.newSingleThreadScheduledExecutor());
	}

	public RunnerImpl(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	void exitHook() {
		synchronized (this) {
			System.err.println("Shutdown hook called for " + this);
			if (initFuture != null)
				initFuture.cancel(true);
			if (stepFuture != null)
				stepFuture.cancel(true);
			if (executor != null && !executor.isShutdown())
				executor.shutdownNow();
		}
	}

	@Override
	public void timePerStep(double secs) {
		long t = (long) (secs * NS);
		if (t <= 0) {
			throw new IllegalArgumentException("Time must be at least 1 ms");
		}
		synchronized (this) {
			delay = t;
		}
	}

	@Override
	public void stepsPerSec(int steps) {
		long t = (long) (NS / steps);
		if (t <= 0) {
			throw new IllegalArgumentException("Time must be at least 1 ms");
		}
		synchronized (this) {
			delay = t;
		}
	}

	@Override
	public double timePerStep() {
		return (double) delay / NS;
	}

	@Override
	public double stepsPerSec() {
		return (double) NS / delay;
	}

	@Override
	public void initialize(StepwiseSimulation<T> sim) {
		synchronized (this) {
			this.sim = sim;

			if (exitHook == null) {
				exitHook = () -> exitHook();
				ShutdownHooks.INSTANCE.register(exitHook);
			}
			initFuture = executor.schedule(this, 0, TimeUnit.NANOSECONDS);
		}
	}

	@Override
	public void stop() {
		synchronized (this) {
			if (running) {
				stepFuture.cancel(true);
				running = false;
			}
		}
	}

	@Override
	public void start() {
		synchronized (this) {
			if (state1 == null) {
				if (initFuture == null) {
					throw new IllegalStateException("not initialized");
				}
			} else {
				try {
					initFuture.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
				initFuture = null;
			}
			if (!running) {
				nanos = System.nanoTime();
				stepFuture = executor.scheduleAtFixedRate(this, 0, delay, TimeUnit.NANOSECONDS);
				running = true;
			}
		}
	}

	@Override
	public T currentState() {
		synchronized (this) {
			if (stepNum % 2 == 0) {
				return state2;
			} else {
				return state1;
			}
		}
	}

	@Override
	public int currentStepNum() {
		synchronized (this) {
			return stepNum;
		}
	}

	@Override
	public void run() {
		try {
			if (state1 == null) {
				this.state1 = sim.createState(0);
				this.state2 = sim.createState(1);
				sim.initialize(state1, state2);
			} else if (running) {
				T next, last;
				synchronized (this) {
					long t = System.nanoTime();
					long dt = t - nanos;
					timeBudget = Math.min(timeBudget + dt, delay * 10);
					nanos = t;
				}
				while (timeBudget >= delay) {
					if (stepNum % 2 == 0) {
						next = state1;
						last = state2;
					} else {
						next = state2;
						last = state1;
					}
					sim.step(stepNum, next, last);
					synchronized (this) {
						timeBudget -= delay;
						stepNum++;
					}
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		synchronized (this) {
			running = false;
			executor.shutdown();
			if (exitHook != null) {
				ShutdownHooks.INSTANCE.unregister(exitHook);
				exitHook = null;
			}
		}
	}

}
