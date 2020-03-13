package turtleduck;

import org.junit.jupiter.api.Test;

import turtleduck.testutil.EqualsProperties;
import turtleduck.testutil.Generator;

public abstract class AbstractEqualsTest<T> {
	public enum ToStringEqualsProperty {
		STRONG, WEAK
	};

	protected Generator<T> generator;
	private ToStringEqualsProperty toStringEqualsProperty;
	protected static final int N = 1000;

	public AbstractEqualsTest(Generator<T> gen, ToStringEqualsProperty prop) {
		generator = gen;
		toStringEqualsProperty = prop;
	}

	@Test
	public void allEqualsTest() {
		EqualsProperties.allEqualsTests(generator, N);
		if (toStringEqualsProperty == ToStringEqualsProperty.STRONG)
			EqualsProperties.toStringEqualsStrongTest(generator, N);
		else if (toStringEqualsProperty == ToStringEqualsProperty.WEAK)
			EqualsProperties.toStringEqualsWeakTest(generator, N);
	}
}
