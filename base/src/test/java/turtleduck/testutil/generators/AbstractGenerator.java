package turtleduck.testutil.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import turtleduck.testutil.Generator;

public abstract class AbstractGenerator<T> implements Generator<T> {
	protected static final Random commonRandom = new Random();

	@Override
	public T generate() {
		return generate(commonRandom);
	}

	@Override
	public List<T> generateEquals(int n) {
		return generateEquals(commonRandom, n);
	}

	@Override
	public List<T> generateEquals(Random r, int n) {
		long seed = r.nextLong();

		List<T> list = new ArrayList<T>();

		for (int i = 0; i < n; i++) {
			list.add(generate(new Random(seed)));
		}

		return list;
	}

}
