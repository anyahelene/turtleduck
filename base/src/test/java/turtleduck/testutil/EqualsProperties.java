package turtleduck.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Supplier;

/**
 * A bunch of standard properties for Java objects, mostly related to equality.
 * <p>
 * Call one of the test methods with a data generator in order to run test for
 * <code>n</code> objects of that type.
 * <p>
 * Relevant test methods are:
 * <ul>
 * <li>{@link #allEqualsTests(Generator, int)} – checks that
 * {@link #equals(Object)} is an equivalence relation and that it corresponds to
 * {@link #hashCode()}. This will run:
 * <ul>
 * <li>{@link #equalsIsReflexiveTest(Generator, int)} – check that a.equals(a)
 * <li>{@link #equalsIsSymmetricTest(Generator, int)} – check that a.equals(b)
 * iff b.equals(a)
 * <li>{@link #equalsIsTransitiveTest(Generator, int)} – check that a.equals(b)
 * && b.equals(c) implies a.equals(c)
 * <li>{@link #hashCodeEqualsTest(Generator, int)} – check that a.equals(b)
 * implies a.hashCode() == b.hashCode()
 * </ul>
 * <li>{@link #toStringEqualsStrongTest(Generator, int)} – check that
 * a.equals(b) iff a.toString().equals(b.toString())
 * <li>{@link #toStringEqualsWeakTest(Generator, int)} – check that a.equals(b)
 * implies a.toString().equals(b.toString())
 * <ul>
 * <p>
 * The {@link #equals(Object)} properties should hold for all classes you
 * implement.
 * <p>
 * The {@link #toString()} properties are desirable, but not always true. The
 * strong property might hold for things like numbers (e.g., rational numbers if
 * there is only <em>one</em> printed representation for each number). The weak
 * one might hold for something like a Person object, where {@link #toString()}
 * prints the name – to equal persons will always have the same name, but having
 * the same name doesn't imply being the same person.
 * 
 * @author Anya Helene Bagge
 * @see #equals(Object)
 * @see #hashCode()
 * @see #toString()
 */
public class EqualsProperties {
	public static final int NEQUALS = 10;
	/**
	 * Test that all the standard properties of equals() hold for objects supplied
	 * by the generator.
	 * <p>
	 * Includes checks for {@link #reflexiveProperty(Object)},
	 * {@link #symmetricProperty(Object, Object)},
	 * {@link #equalsIsTransitiveTest(Generator, int)},
	 * {@link #hashCodeProperty(Object, Object)}.
	 * <p>
	 * Does not include test for the relationship between {@link #equals(Object)}
	 * and {@link #toString()}. Use either
	 * {@link #toStringEqualsStrongTest(Generator, int)} or
	 * {@link #toStringEqualsWeakTest(Generator, int)} for this.
	 * 
	 * @param gen A data generator
	 * @param n   The number of times to run the tests
	 */
	public static <T> void allEqualsTests(Generator<T> gen, int n) {
		equalsGeneratorMakesEqualValues(gen, n);
		equalsIsReflexiveTest(gen, n);
		equalsIsSymmetricTest(gen, n);
		equalsIsTransitiveTest(gen, n);
		hashCodeEqualsTest(gen, n);
	}

	/**
	 * A generic test for the reflexivity property.
	 *
	 * Will generate n sets of objects with the generator, and check the property
	 * with them.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test
	 * @see #reflexiveProperty(Object)
	 */
	public static <T> void equalsIsReflexiveTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			EqualsProperties.reflexiveProperty(gen.generate());
		}
	}

	/**
	 * A generic test for the symmetry property.
	 *
	 * Will generate n sets of objects with the generator, and check the property
	 * with them.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test
	 * @see #symmetricProperty(Object, Object)
	 */
	public static <T> void equalsIsSymmetricTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			symmetricProperty(gen.generate(), gen.generate());

			// we want to test this both we totally random values, and with
			// values known to be equal
			List<T> ss = gen.generateEquals(NEQUALS);

			symmetricProperty(ss.get(0), ss.get(1));
		}
	}

	/**
	 * A generic test for the transitivity property.
	 *
	 * Will generate n sets of objects with the generator, and check the property
	 * with them.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test
	 * @see #transitiveProperty(Object, Object, Object)
	 */
	public static <T> void equalsIsTransitiveTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			transitiveProperty(gen.generate(), gen.generate(), gen.generate());

			// we want to test this both we totally random values, and with
			// values known to be equal
			List<T> ss = gen.generateEquals(NEQUALS);

			transitiveProperty(ss.get(0), ss.get(1), ss.get(2));
		}
	}

	/**
	 * A generic test for the hashcode property.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test.
	 * @see #hashCodeProperty(Object, Object)
	 */
	public static <T> void hashCodeEqualsTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			hashCodeProperty(gen.generate(), gen.generate());

			// we want to test this both we totally random values, and with
			// values known to be equal
			List<T> ss = gen.generateEquals(NEQUALS);

			hashCodeProperty(ss.get(0), ss.get(1));
		}
	}

	/**
	 * A generic test for the hashcode property.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test.
	 * @see #hashCodeProperty(Object, Object)
	 */
	public static <T> void equalsGeneratorMakesEqualValues(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			List<T> ss = gen.generateEquals(NEQUALS+1);
			for(int j = 0; j < NEQUALS; j++)
				assertEquals(ss.get(j), ss.get(j+1), "Generator should provide equal values: " + ss);
		}
	}
	/**
	 * Checks the equals/hashCode property, i.e. that s1.equals(s2) implies
	 * s1.hashCode() == s2.hashCode().
	 *
	 * @param s1
	 * @param s2
	 */
	public static <T> void hashCodeProperty(T s1, T s2) {
		if (s1.equals(s2)) {
			assertEquals(s1.hashCode(), s2.hashCode(), failed("a.equals(b) => a.hashCode() == b.hashCode", s1, s2));
		}
	}

	/**
	 * Checks the reflexivity property for equals, i.e. that s.equals(s).
	 *
	 * @param s
	 */
	public static <T> void reflexiveProperty(T s) {
		assertEquals(s, s, failed("a.equals(a)", s));
	}

	/**
	 * Checks the equals/toString property (strong version), i.e. that s1.equals(s2)
	 * if and only if s1.toString().equals(s2.toString()).
	 *
	 * @param s1
	 * @param s2
	 */
	public static <T> void strongToStringProperty(T s1, T s2) {
		if (s2 != null) {
			assertEquals(s1.equals(s2), s1.toString().equals(s2.toString()),
					failed("a.equals(b) <=> a.toString().equals(b.toString())", s1, s2));
		}
	}

	/**
	 * Checks the symmetry property for equals, i.e. that s1.equals(s2) ==
	 * s2.equals(s1).
	 *
	 * @param s1
	 * @param s2
	 */
	public static <T> void symmetricProperty(T s1, T s2) {
		assertEquals(s1.equals(s2), s2.equals(s1), failed("a.equals(b) <=> b.equals(a)", s1, s2));
	}

	/**
	 * A generic test for the toString property (strong version) – that toStrings
	 * are equal if and only if objects are equal.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test.
	 * @see #weakToStringProperty(Object, Object)
	 */
	public static <T> void toStringEqualsStrongTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			strongToStringProperty(gen.generate(), gen.generate());

			List<T> ss = gen.generateEquals(NEQUALS);

			strongToStringProperty(ss.get(0), ss.get(1));
		}
	}

	/**
	 * A generic test for the toString property (weak version) – that equal objects
	 * have equal toStrings.
	 *
	 * @param gen A data generator
	 * @param n   The number of times to run the test.
	 * @see #weakToStringProperty(Object, Object)
	 */
	public static <T> void toStringEqualsWeakTest(Generator<T> gen, int n) {
		for (int i = 0; i < n; i++) {
			weakToStringProperty(gen.generate(), gen.generate());

			List<T> ss = gen.generateEquals(NEQUALS);

			weakToStringProperty(ss.get(0), ss.get(1));
		}
	}

	/**
	 * Checks the transitivity property for equals, i.e. that s1.equals(s2) and
	 * s2.equals(s3) implies s1.equals(s3).
	 *
	 * @param s1
	 * @param s2
	 * @param s3
	 */
	public static <T> void transitiveProperty(T s1, T s2, T s3) {
		if (s1.equals(s2) && s2.equals(s3)) {
			assertEquals(s1, s3, failed("a.equals(b) && b.equals(c) => a.equals(c)", s1, s2, s3));

		}
	}

	/**
	 * Checks the equals/toString property (weak version), i.e. that s1.equals(s2)
	 * implies s1.toString().equals(s2.toString()).
	 *
	 * @param s1
	 * @param s2
	 */
	public static <T> void weakToStringProperty(T s1, T s2) {
		if (s2 != null && s1.equals(s2)) {
			assertEquals(s1.toString(), s2.toString(), failed("a.equals(b) => a.toString().equals(b.toString())", s1, s2));
		}
	}

	private static <T> Supplier<String> failed(String s, T a) {
		return () -> String.format("failed: %s (a=%s)", s, a);
	}

	private static <T> Supplier<String> failed(String s, T a, T b) {
		return () -> String.format("failed: %s (a=%s, b=%s)", s, a, b);
	}

	private static <T> Supplier<String> failed(String s, T a, T b, T c) {
		return () -> String.format("failed: %s (a=%s, b=%s, c=%s)", s, a, b, c);
	}

}
