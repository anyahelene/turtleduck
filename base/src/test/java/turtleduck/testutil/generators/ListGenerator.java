package turtleduck.testutil.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import turtleduck.testutil.Generator;

public class ListGenerator<T> extends AbstractGenerator<List<T>>
	implements Generator<List<T>> {
	/**
	 * Generator for the length of the list
	 */
	private final Generator<Integer> lengthGenerator;

	/**
	 * Generator for one element of a random grid
	 */
	private final Generator<T> elementGenerator;

	public ListGenerator(Generator<T> elementGenerator) {
		this.elementGenerator = elementGenerator;
		this.lengthGenerator = new IntGenerator(0, 100);
	}

	public ListGenerator(Generator<T> elementGenerator, int maxLength) {
		if (maxLength < 0) {
			throw new IllegalArgumentException("Length must be 1 or greater");
		}

		this.elementGenerator = elementGenerator;
		this.lengthGenerator = new IntGenerator(0, maxLength);
	}

	public ListGenerator(Generator<T> elementGenerator, int minLength, int maxLength) {
		if (maxLength < 0 || minLength < 0) {
			throw new IllegalArgumentException("Length must be 1 or greater");
		}
		if (minLength < maxLength) {
			throw new IllegalArgumentException("Minlength must be less than maxlength");
		}

		this.elementGenerator = elementGenerator;
		this.lengthGenerator = new IntGenerator(minLength, maxLength);
	}
	@Override
	public List<T> generate(Random r) {
		int l = lengthGenerator.generate(r);
		List<T> result = new ArrayList<>(l);

		
		for (int i = 0; i < l; i++) {
			result.add(elementGenerator.generate(r));
		}
		
		assert !result.isEmpty();
		
		return result;
	}
}
