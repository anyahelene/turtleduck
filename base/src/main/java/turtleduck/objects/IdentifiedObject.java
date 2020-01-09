package turtleduck.objects;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IdentifiedObject {

	String id();

	public static class Registry {
		private static final Map<String, Integer> ID_MAP = new HashMap<>();
		private static final Map<String, WeakReference<IdentifiedObject>> OBJ_MAP = new IdentityHashMap<>();
		private static int lastClean = 0;
		private static final int CLEAN_THRESHOLD = 1024;
		
		/**
		 * Make a unique id for an object.
		 * 
		 * The id is based on the name of the given class, and a unique number for that
		 * class. If an object is provided, it is registered (with a weak reference),
		 * for later retrieval with {@link #findObject(Class, String)} or
		 * {@link #allObjects(Class)}.
		 * 
		 * @param <T>   object type
		 * @param clazz object's class
		 * @param obj   reference to object, or null
		 * @return The ID
		 */
		public synchronized static <T extends IdentifiedObject> String makeId(Class<?> clazz, T obj) {
			String name = clazz.getSimpleName();
			int i = ID_MAP.getOrDefault(name, 0);
			ID_MAP.put(name, i + 1);
			String id = (name + "_" + i).intern();
			if (obj != null) {
				OBJ_MAP.put(id, new WeakReference<>(obj));
				if(OBJ_MAP.size() > lastClean + CLEAN_THRESHOLD)
					clean();
			}
			return id;
		}

		/**
		 * Find a previously registered object.
		 * 
		 * The <code>id</code> should be a name previously returned from
		 * {@link #makeId(Class, IdentifiedObject)}. Will return <code>null</code> if
		 * the object no longer exists.
		 * 
		 * @param <T> object type
		 * @param clazz object type
		 * @param id the identifier
		 * @return the identified object, or null
		 */
		@SuppressWarnings("unchecked")
		public synchronized static <T extends IdentifiedObject> T findObject(Class<T> clazz, String id) {
			WeakReference<IdentifiedObject> ref = OBJ_MAP.get(id.intern());
			IdentifiedObject object = ref.get();
			if (clazz.isInstance(object))
				return (T) object;
			else
				return null;
		}

		public synchronized static void clean() {
			System.err.print("Cleaning object registry... from " + OBJ_MAP.size());
			OBJ_MAP.values().removeIf((ref) -> ref.get() == null);
			lastClean = OBJ_MAP.size();
			System.err.println(" to " + lastClean + " objects");
		}

		/**
		 * Return a snapshot of all live objects of the given type.
		 * 
		 * The list may include otherwise inaccessible objects that haven't been garbage collected yet.
		 * 
		 * @param <T> object type
		 * @param clazz type to look for
		 * @return list of objects
		 */
		@SuppressWarnings("unchecked")
		public synchronized static <T extends IdentifiedObject> List<T> allObjects(Class<T> clazz) {
			clean();
			return OBJ_MAP.values().stream()//
					.map((ref) -> ref.get())//
					.filter((obj) -> clazz.isInstance(obj))//
					.map((obj) -> (T) obj)//
					.collect(Collectors.toList());
		}
	}
}
