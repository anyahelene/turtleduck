package turtleduck.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import turtleduck.annotations.Icon;

public class Meta {
	public static final Key<String> TYPE = Key.strKey("type");
	public static final Key<String> NAME = Key.strKey("name");
	public static final Key<String> ICON = Key.strKey("icon");
	public static final Key<String> DECL = Key.strKey("decl");
	public static final Key<Array> IFACES = Key.arrayKey("ifaces");
	public static final Key<Array> FLAGS = Key.arrayKey("flags");
	private Map<Class<?>, Dict> inspectMemo = new HashMap<>();

	public static Meta create() {
		return new Meta();
	}

	private Meta() {

	}

	/**
	 * Find the class of a class or object.
	 * 
	 * Similar to <code>obj.getClass()</code>, but will return <code>obj</code>
	 * itself if <code>obj instanceof Class&lt;?&gt;</code>, and <code>null</code>
	 * if <code>obj == null</code>
	 * 
	 * @param classOrObject An object, a class, or <code>null</code>
	 * @return The class, or <code>null</code> if <code>obj == null</code>
	 */
	public Class<?> classOf(Object classOrObject) {
		Integer x;
		return classOrObject != null
				? (classOrObject instanceof Class<?> ? (Class<?>) classOrObject : classOrObject.getClass())
				: null;
	}

	/**
	 * Return the {@link turtleduck.annotations.Icon <code>@Icon</code>} annotation
	 * of a class or object.
	 * 
	 * Checks all implememented interfaces (according to
	 * {@link #interfacesOf(Class)}, and picks the first icon found.
	 * 
	 * @param classOrObject A class or object
	 * @return The icon, or <code>null</code> if not found
	 */
	public String iconOf(Object classOrObject) {
		Class<?> clazz = classOf(classOrObject);
		return withAnnotation(Icon.class, clazz, a -> a.value(), () -> {
			switch (clazz.getName()) {
			case "java.lang.Number":
			case "java.lang.Double":
			case "java.lang.Long":
			case "java.lang.Integer":
			case "java.lang.Short":
			case "java.lang.Byte":
			case "double":
			case "long":
			case "int":
			case "short":
			case "byte":
				return "🔢";
			case "char":
			case "java.lang.Character":
				return "🔣";
			case "java.lang.Boolean":
			case "boolean":
				return "⁉️";
			case "java.lang.String":
				return "🔤";
			case "void":
				return "🕳️";
			default:
				return null;
			}
		});
	}

	/**
	 * Collect useful information about a class.
	 * 
	 * @param classOrObject A class or object
	 * @return
	 */
	public Dict inspect(Object classOrObject) {
		Class<?> clazz = classOf(classOrObject);
		if (clazz != null)
			return inspect(clazz.getSimpleName(), clazz);
		else
			return null;
	}

	public Dict inspect(String name, Class<?> clazz) {
		Dict d = inspectMemo.get(clazz);
		if (d != null) {
			return d;
		}
		d = Dict.create();
		inspectMemo.put(clazz, d);
		d.put(NAME, name);
		d.put(TYPE, interfaceOrClassOf(clazz).getName());
		d.put(IFACES, Array.from(interfacesOf(clazz).stream().map(c -> c.getName()).collect(Collectors.toList()),
				String.class));
		d.put(ICON, iconOf(clazz));
		d.put("class", clazz.toGenericString());

		Array flags = Array.of(String.class);
		if (overridesMethod(clazz, "toString"))
			flags.add("HAS_TOSTRING");
		if (overridesMethod(clazz, "equals", Object.class))
			flags.add("HAS_EQUALS");
		if (overridesMethod(clazz, "hashCode"))
			flags.add("HAS_HASHCODE");

		try {
			clazz.getConstructor();
			flags.add("HAS_DEFAULT_CONSTRUCTOR");
		} catch (NoSuchMethodException | SecurityException e) {
			// ignore
		}

		d.put(FLAGS, flags);
		return d;
	}

	/**
	 * Checks if an object or class overrides a default method from
	 * {@link java.lang.Object}.
	 * 
	 * @param classOrObject A class or an object
	 * @param methodName    The method name
	 * @param paramTypes    Parameter types, if any
	 * @return True if the given method exists, and is not the default version
	 *         provided by the <code>Object</code> class
	 */
	public boolean overridesMethod(Object classOrObject, String methodName, Class<?>... paramTypes) {
		Class<?> clazz = classOf(classOrObject);
		try {
			Method method = clazz.getMethod(methodName, paramTypes);
			Class<?> declaringClass = method.getDeclaringClass();
			Class<?> objClass = Object.class;
			return declaringClass != objClass;
		} catch (NoSuchMethodException | SecurityException e) {
//			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Guess the “main” interface implemented by a class or object
	 * 
	 * This will be the first interface implemented by a class, or the first
	 * interface implemented by its superclass (transitively).
	 * 
	 * If <code>classOrObject</code> is already an interface, it is returned
	 * directly; if no interfaces are implemented, <code>null</code> is returned.
	 * 
	 * @param classOrObject A class or an object
	 * @return An interface implemented by <code>obj</code>
	 */
	public Class<?> interfaceOf(Object classOrObject) {
		Class<?> clazz = classOf(classOrObject);
		Class<?> best = null;
		int bestNum = 0;
		while (clazz != null && clazz != Object.class) {
			if (clazz.isInterface())
				return clazz;
			int numMethods = clazz.getMethods().length;
			double min = numMethods;
			if (numMethods < 5)
				min = Math.min(3, numMethods);
			else if (numMethods < 10)
				min = 5;
			else
				min = numMethods / 4;
			System.out.printf("interfaceOf(): Class %s has %d methods\n", clazz.getName(), numMethods);
			for (Class<?> iface : clazz.getInterfaces()) {
				int n = iface.getMethods().length;
				System.out.printf("interfaceOf(): Interface %s has %d methods, we'd like at least %g\n",
						iface.getName(), n, min);
				if (n > min) {
					if (n <= bestNum) {
						System.out.printf(
								"interfaceOf(): We previously found %s, which had %d, let's pick that instead\n",
								best.getName(), bestNum);
						return best;
					} else {
						return iface;
					}
				} else if (n > bestNum) {
					bestNum = n;
					best = iface;
				}
			}

			clazz = clazz.getSuperclass();
		}
		return null;
	}

	/**
	 * Guess the “main” interface implemented by a class or object
	 * 
	 * This will be the first interface implemented by a class, or the first
	 * interface implemented by its superclass (transitively).
	 * 
	 * If <code>classOrObject</code> is already an interface, it is returned
	 * directly; if no interfaces are implemented, the (object's) class is returned.
	 * 
	 * @param classOrObject A class or an object
	 * @return An interface implemented by <code>classOrObject</code>, or its class
	 */
	public Class<?> interfaceOrClassOf(Object classOrObject) {
		Class<?> clazz = interfaceOf(classOrObject);
		if (clazz != null)
			return clazz;
		else
			return classOf(classOrObject);
	}

	/**
	 * Find all interfaces directly or indirectly implemented by <code>clazz</code>.
	 * 
	 * Will include <code>clazz</code> itself if it is an interface. The interfaces
	 * will be listed in the order they appear in the <code>implements</code> or
	 * <code>extends</code> clause, followed by any super-interfaces (transitively).
	 * 
	 * Interfaces that appear multiple times will only be included once.
	 * 
	 * @param clazz
	 * @return A list of interfaces.
	 */
	public List<Class<?>> interfacesOf(Class<?> clazz) {
		List<Class<?>> ifaces = new ArrayList<>();
		if (clazz.isInterface())
			ifaces.add(clazz);

		Deque<Class<?>> todo = new ArrayDeque<>();
		while (clazz != null && clazz != Object.class) {
			todo.addAll(Arrays.asList(clazz.getInterfaces()));

			while (!todo.isEmpty()) {
				Class<?> iface = todo.poll();
				if (!ifaces.contains(iface))
					ifaces.add(iface);

				todo.addAll(Arrays.asList(iface.getInterfaces()));
			}

			clazz = clazz.getSuperclass();
		}
		return ifaces;
	}

	/**
	 * Look up an annotation in the class hierarchy and call a function if found.
	 * 
	 * Will look for the annotation on <code>clazz</code> directly, and then fall
	 * back to searching its super-interfaces transitively.
	 * 
	 * @param <A>   The annotation type
	 * @param <T>   The function result type
	 * @param anno  The annotation class object
	 * @param clazz The class that should be searched
	 * @param fun   A function which is called with the annotation value, if found
	 * @return The result of applying <code>fun</code> or <code>null</code> if the
	 *         annotation was not found
	 */
	public <A extends Annotation, T> T withAnnotation(Class<A> anno, Class<?> clazz, Function<A, T> fun) {
		A annotation = findAnnotation(anno, clazz);
		if (annotation != null)
			return fun.apply(annotation);
		else
			return null;
	}

	/**
	 * Look up an annotation in the class hierarchy and call a function if found.
	 * 
	 * Will look for the annotation on <code>clazz</code> directly, and then fall
	 * back to searching its super-interfaces transitively.
	 * 
	 * @param <A>   The annotation type
	 * @param <T>   The function result type
	 * @param anno  The annotation class object
	 * @param clazz The class that should be searched
	 * @param fun   A function which is called with the annotation value, if found
	 * @return The result of applying <code>fun</code> or <code>null</code> if the
	 *         annotation was not found
	 */
	public <A extends Annotation, T, U> T withAnnotation(Class<A> anno, Class<U> clazz, Function<A, T> fun,
			Supplier<T> otherwise) {
		A annotation = findAnnotation(anno, clazz);
		if (annotation != null)
			return fun.apply(annotation);
		else
			return otherwise.get();
	}

	/**
	 * Look up an annotation in the class hierarchy.
	 * 
	 * Will look for the annotation on <code>clazz</code> directly, and then fall
	 * back to searching its super-interfaces transitively, and then searching the
	 * interfaces of the superclass (if any) and so on.
	 * 
	 * @param <A>   The annotation type
	 * @param anno  The annotation class object
	 * @param clazz The class that should be searched
	 * @return The annotation object, or <code>null</code> if the annotation was not
	 *         found
	 */
	public <A extends Annotation> A findAnnotation(Class<A> anno, Class<?> clazz) {
		A annotation = clazz.getAnnotation(anno);
		if (annotation != null)
			return annotation;

		while (clazz != null && clazz != Object.class) {
			Deque<Class<?>> todo = new ArrayDeque<>();
			todo.addAll(Arrays.asList(clazz.getInterfaces()));

			while (!todo.isEmpty()) {
				Class<?> iface = todo.poll();
				annotation = iface.getAnnotation(anno);
				if (annotation != null)
					return annotation;
				todo.addAll(Arrays.asList(iface.getInterfaces()));
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	/**
	 * Same as {@link #findAnnotation(Class, Class) findAnnotation(anno,
	 * classOf(classOrObject))}
	 */
	public <A extends Annotation> A findAnnotation(Class<A> anno, Object classOrObject) {
		return findAnnotation(anno, classOf(classOrObject));
	}

}
