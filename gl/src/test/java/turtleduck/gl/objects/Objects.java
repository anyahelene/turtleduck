package turtleduck.gl.objects;

public class Objects {

	public static void main(String[] args) {
		System.out.println("\nDouble objects:");
		same(0.0,0.0); // Double objects are unique, even though primitive doubles are ==
		same(1.0,1.0);
		same(1.0/0.0,1.0/0.0); // infinity and
		same(0.0/0.0,0.0/0.0); // not-a-number too

		System.out.println("\nInteger objects:");
		same(0,0);  // Integer objects are only sometimes unique:
		same(127,127); // small integers are == (-128 to +127)
		same(127,126+1); // ... even in an expression
		int i = 1;
		same(127,126+i); // ... even if the expression is not constant
		same(128,128); // larger integers are placed in new Integer objects

		same(new Integer(1), 1); // "new" gives you a new, distinct object
		same(new Integer(1)+1, 1+1); // interesting! (there's a table of "built-in" Integers)

		// => the interaction between primitives (int) and objects (Integer) is a bit strange,
		// mostly due to historical accidents

		System.out.println("\nString objects:");
		same("a","a"); // String constants are ==

		same(new String("a"),"a"); // 'new' always creates distinct object
		same(new String("a"),new String("a")); //
		same(new String("a").intern(),"a"); // intern() turns equals strings into the same object
		same("a",Foo.s); // String constants are always ==, even when defined in different class (except if you start messing with class loaders)

		same(new Object(), new Object()); // new Object() != a, for all a

		System.out.println("\nString concatenation (+):");
		same("ab","a" + "b"); // + on constants, compiler precomputes "a"+"b" to "ab"
		String s = "b";
		same("ab","a" + s); // + constructs new String, so !=
		final String t = "b";
		same("ab","a" + t); // 'final' makes 't' a constant, so we get same("ab","ab")

		// in general, '+' on strings is the same as:
		StringBuilder builder = new StringBuilder("a");
		builder.append(s);
		same("ab", builder.toString());

		// moar fun:
		System.out.println("\nalso – double and float are not exact:");
		double e = 1/10.0; // 0.1
		double f = e+e+e+e+e+e+e+e+e+e; // 0.1 * 10
		System.out.printf("%.2f == %.2f ? %b%n", f, 1.0, f == 1.0); // should be 1.0, right?
	}

	public static void same(Object a, Object b) {
		System.out.printf("%4s %s %-4s ", a, (a == b ? " == " : " != "), b);
		//System.out.printf("%1s%4s.equals(%4s) ", (a.equals(b) ? "" : "!"), a, b);

		System.out.println("(" + a.getClass().getSimpleName() //
				+ " a@" + System.identityHashCode(a) //
				+ " b@" + System.identityHashCode(b) + ")");
	}
}

class Foo {
	public static String s = "a";
}