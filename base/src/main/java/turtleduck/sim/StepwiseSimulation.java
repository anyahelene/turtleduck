package turtleduck.sim;

public interface StepwiseSimulation<T> {
	void initialize(T state1, T state2);

	void step(int stepNum, T current, T last);

	T createState(int stepNum);
}
