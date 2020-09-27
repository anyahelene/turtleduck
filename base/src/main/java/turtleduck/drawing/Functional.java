package turtleduck.drawing;

import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.function.BiConsumer;

public interface Functional<T> {
	/**
	 * Create a sub unit of <code>this</code>, and run <code>f</code> on it.
	 * 
	 * The sub unit has its own state, so the current state of <code>this</code> is
	 * untouched when the method returns. The sub unit is only valid for the
	 * duration of the call to <code>f</code>, so <code>f</code> should not store a
	 * reference to it.
	 * 
	 * @param f a consumer method; it will be called with <code>this.spawn()</code>
	 *          as its first argument
	 * @return <code>this</code>
	 */
	T spawn(Consumer<T> f);

	/**
	 * Create <code>n</code> sub units of <code>this</code>, and run <code>f</code>
	 * on them.
	 * 
	 * The sub units have their own state, so the current state of <code>this</code>
	 * is untouched when the method returns. The sub units are only valid for the
	 * duration of the calls to <code>f</code>, so <code>f</code> should not store
	 * the reference it is given.
	 * 
	 * The sub units can be started in any order, they all start with the same state
	 * as <code>this</code>.
	 * 
	 * @param n the number of sub units to spawn
	 * @param f a consumer method; it will be called with <code>this.spawn()</code>
	 *          as its first argument, and a number 0 <= i < n as the second
	 *          argument
	 * @return <code>this</code>
	 */
	T spawn(int n, BiConsumer<T, Integer> f);

	/**
	 * Create sub units of <code>this</code> for each item, and run <code>f</code>
	 * on them.
	 * 
	 * The sub units can be started in any order, they all start with the same state
	 * as <code>this</code>.
	 *
	 * The sub units have their own state, so the current state of <code>this</code>
	 * is untouched when the method returns. The sub units are only valid for the
	 * duration of the calls to <code>f</code>, so <code>f</code> should not store
	 * the reference it is given.
	 * 
	 * @param elts a collection of items to spawn sub unit for
	 * @param f    a consumer method; it will be called with
	 *             <code>this.spawn()</code> as its first argument, and an item of
	 *             <code>elts</code> as the second argument
	 * @return <code>this</code>
	 */
	<U> T spawn(Iterable<U> elts, BiConsumer<T, U> f);

	/**
	 * Create sub units of <code>this</code> for each item, and run <code>f</code>
	 * on them.
	 * 
	 * The sub units can be started in any order, they all start with the same state
	 * as <code>this</code>.
	 * 
	 * The sub units have their own state, so the current state of <code>this</code>
	 * is untouched when the method returns. The sub units are only valid for the
	 * duration of the calls to <code>f</code>, so <code>f</code> should not store
	 * the reference it is given.
	 * 
	 * @param elts a stream of items to spawn sub unit for
	 * @param f    a consumer method; it will be called with
	 *             <code>this.spawn()</code> as its first argument, and an item of
	 *             <code>elts</code> as the second argument
	 * @return <code>this</code>
	 */
	<U> T spawn(Stream<U> elts, BiConsumer<T, U> f);

	/**
	 * Run <code>f</code> on <code>this</code> <code>n</code> times.
	 * 
	 * The iterations are done in order; no sub units are spawned, so each iteration
	 * will update the state of <code>this</code>.
	 * 
	 * Equivalent to <code>for(int i = 0; i < n; i++) obj.apply(i, f);</code>
	 * 
	 * @param n the number of iterations
	 * @param f a consumer method; it will be called with <code>this</code> as its
	 *          first argument, and a number 0 <= i < n as the second argument
	 * @return <code>this</code>
	 */
	T repeat(int n, BiConsumer<T, Integer> f);

	/**
	 * Run <code>f</code> on <code>this</code> for each item.
	 * 
	 * The iterations are done in order; no sub units are spawned, so each iteration
	 * will update the state of <code>this</code>.
	 * 
	 * Equivalent to <code>for(U elt : elts) obj.apply(elt, f);</code>
	 * 
	 * @param elts a collection of items to process
	 * @param f    a consumer method; it will be called with <code>this</code> as
	 *             its first argument, and an item of <code>elts</code> as the
	 *             second argument
	 * @return <code>this</code>
	 */
	<U> T repeat(Iterable<U> elts, BiConsumer<T, U> f);

	/**
	 * Run <code>f</code> on <code>this</code> for each item.
	 * 
	 * The iterations are done in order; no sub units are spawned, so each iteration
	 * will update the state of <code>this</code>.
	 * 
	 * Equivalent to <code>elts.foreach((elt) -> apply(elt, f));</code>.
	 * 
	 * @param elts a collection of items to process
	 * @param f    a consumer method; it will be called with <code>this</code> as
	 *             its first argument, and an item of <code>elts</code> as the
	 *             second argument
	 * @return <code>this</code>
	 */
	<U> T repeat(Stream<U> elts, BiConsumer<T, U> f);

	/**
	 * Run <code>f</code> on <code>this</code>.
	 * 
	 * Equivalent to <code>f.accept(obj)</code>.
	 * 
	 * @param f a consumer method; it will be called with <code>this</code> as its
	 *          first argument
	 * @return <code>this</code>
	 */
	T apply(Consumer<T> f);

	/**
	 * Run <code>f</code> on <code>this</code> with the extra argument.
	 * 
	 * Equivalent to <code>f.accept(obj, arg)</code>.
	 * 
	 * @param f a consumer method; it will be called with <code>this</code> as its
	 *          first argument, and <code>arg</code> as the second argument
	 * @return <code>this</code>
	 */
	<U> T apply(U arg, BiConsumer<T, U> f);

}
