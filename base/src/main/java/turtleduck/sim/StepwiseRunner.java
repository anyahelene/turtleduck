	package turtleduck.sim;

import turtleduck.sim.impl.RunnerImpl;

public interface StepwiseRunner<T> {
	static <T> StepwiseRunner<T> create() {
		return new RunnerImpl<>();
	}
	void initialize(StepwiseSimulation<T> sim);

	void stop();

	void start();

	T currentState();

	int currentStepNum();

	void shutdown();

	double stepsPerSec();

	double timePerStep();

	void timePerStep(double secs);

	void stepsPerSec(int steps);
}
