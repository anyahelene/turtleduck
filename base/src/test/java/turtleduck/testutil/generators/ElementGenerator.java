package turtleduck.testutil.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


public class ElementGenerator<T> extends AbstractGenerator<T> {
	private List<T> elts;

	/**
	 * New ElementGenerator, will pick a random element from a collection.
	 * 
	 * @requires collection must not be empty
	 */
	public ElementGenerator(Collection<T> elts) {
		if (elts.size() == 0)
			throw new IllegalArgumentException();
		this.elts = new ArrayList<>(elts);
	}

	/**
	 * New ElementGenerator, will pick a random element from a list.
	 * 
	 * @requires list must not be empty
	 */
	public ElementGenerator(List<T> elts) {
		if (elts.size() == 0)
			throw new IllegalArgumentException();
		this.elts = elts;
	}

	public ElementGenerator(T[] values) {
		if (elts.size() == 0)
			throw new IllegalArgumentException();
		this.elts = List.of(values);
	}

	@Override
	public T generate(Random r) {
		return elts.get(r.nextInt(elts.size()));
	}

}
