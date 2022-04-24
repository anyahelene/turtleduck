package turtleduck.util;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import turtleduck.util.Key.KeyImpl;

public interface Dict extends Iterable<Key<?>> {
	public static Dict create() {
		return new DictImpl();
	}

	public static Dict wrap(Map<String, ?> toWrap) {
		return new DictImpl(toWrap);
	}

	<T> T get(Key<T> key);

	<T> T get(Key<T> key, T defaultValue);

	<T> T get(String key, T defaultValue);

	<T> T get(String key, Class<T> type);

	default boolean getBoolean(String key) {
		return get(key, Boolean.class);
	}

	default int getInt(String key) {
		return get(key, Integer.class);
	}

	default String getString(String key) {
		return get(key, String.class);
	}

	default Dict getDict(String key) {
		return get(key, Dict.class);
	}

	default Array getArray(String key) {
		return get(key, Array.class);
	}

	Object getObject(String key);

	<T> Dict put(Key<T> key, T value);

	<T> Dict put(String key, T value);

	<T> boolean has(Key<T> key);

	<T> Dict require(Key<T> key);

	<T> boolean has(String key);

	<T> boolean has(String key, Class<T> type);

	Iterable<Entry<String, Object>> entries();

	String toJson();

	static class DictImpl implements Dict {
		private final Map<String, Object> dict;

		public DictImpl() {
			dict = new LinkedHashMap<>();
		}

		@SuppressWarnings("unchecked")
		public DictImpl(Map<String, ?> toWrap) {
			dict = (Map<String, Object>) toWrap;
		}

		@Override
		public Iterator<Key<?>> iterator() {
			Iterator<Entry<String, Object>> it = dict.entrySet().iterator();
			return new Iterator<Key<?>>() {

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public Key<?> next() {
					Entry<String, Object> next = it.next();
					if (next.getValue() != null)
						return new KeyImpl<>(next.getKey(), next.getValue().getClass(), null); // TODO: is perhaps the
																								// source of lots of
																								// extra KeyImpl objects
					else
						return new KeyImpl<>(next.getKey(), Void.class, null);

				}
			};
		}

		public Iterable<Entry<String, Object>> entries() {
			return dict.entrySet();
		}

		@Override
		public <T> T get(Key<T> key) {
			Object obj = dict.get(key.key());
			if (obj == null)
				obj = key.defaultValue();
			return check(obj, key.type());
		}

		@SuppressWarnings("unchecked")
		private <T> T check(Object obj, Class<T> type) {
			if (type != null && obj != null && type != Object.class && type != Void.class) {
				if (type.isInstance(obj))
					return (T) obj;
				else if (Number.class.isAssignableFrom(type) && Number.class.isInstance(obj)) {
					Number n = (Number) obj;
					if (type == Byte.TYPE)
						return (T) (Byte) n.byteValue();
					else if (type == Short.TYPE)
						return (T) (Short) n.shortValue();
					else if (type == Integer.TYPE)
						return (T) (Integer) n.intValue();
					else if (type == Long.TYPE)
						return (T) (Long) n.longValue();
					else if (type == Float.TYPE)
						return (T) (Float) n.floatValue();
					else if (type == Double.TYPE)
						return (T) (Double) n.doubleValue();
					else
						return (T) obj;
				} else
					throw new ClassCastException("expected " + type.getName() + ", got " + obj.getClass().getName());
			} else {
				return (T) obj;
			}
		}

		@SuppressWarnings("unchecked")
		private <T> T check(Object obj, Class<T> type, T defaultValue) {
			if (type != null) {
				if (type.isInstance(obj))
					return (T) obj;
				else
					return defaultValue;

			} else {
				return (T) obj;
			}
		}

		@Override
		public <T> T get(Key<T> key, T defaultValue) {
			Object obj = dict.getOrDefault(key.key(), defaultValue);
			return check(obj, key.type(), defaultValue);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(String key, T defaultValue) {
			Object obj = dict.getOrDefault(key, defaultValue);
			if (defaultValue != null && !defaultValue.getClass().isInstance(obj)) {
				throw new ClassCastException(
						"expected " + defaultValue.getClass().getName() + ", got " + obj.getClass().getName());
			}
			return (T) obj;
		}

		@Override
		public <T> T get(String key, Class<T> type) {
			Object obj = dict.get(key);

			return check(obj, type);
		}

		@Override
		public <T> Dict put(Key<T> key, T value) {
			if (value == null)
				dict.remove(key.key());
			else
				dict.put(key.key(), value);
			return this;
		}

		@Override
		public <T> Dict put(String key, T value) {
			if (value == null)
				dict.remove(key);
			else
				dict.put(key, value);
			return this;
		}

		@Override
		public <T> boolean has(Key<T> key) {
			Object obj = dict.get(key.key());
			if (obj == null)
				return false;
			else if (key.type() != null)
				return key.type().isInstance(obj);
			else
				return true;
		}

		@Override
		public <T> boolean has(String key) {
			return dict.containsKey(key);
		}

		@Override
		public <T> boolean has(String key, Class<T> type) {
			Object obj = dict.get(key);
			if (obj == null)
				return false;
			else
				return type.isInstance(obj);
		}

		@Override
		public <T> Dict require(Key<T> key) {
			if (!has(key)) {
				System.err.println(key);
				throw new IndexOutOfBoundsException(key.toString());
			}
			return this;
		}

		@Override
		public String toJson() {
			return JsonUtil.encode(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dict == null) ? 0 : dict.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DictImpl)) {
				return false;
			}
			DictImpl other = (DictImpl) obj;
			if (dict == null) {
				if (other.dict != null) {
					return false;
				}
			} else if (!dict.equals(other.dict)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return JsonUtil.encode(this);
		}

		@Override
		public Object getObject(String key) {
			return dict.get(key);
		}
	}

}
