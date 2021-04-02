package turtleduck.util;

import java.util.function.Supplier;

public interface Key<T> {
	public static <T> Key<T> key(String name, Class<T> type) {
		return new KeyImpl<>(name, type, null);
	}

	public static Key<Integer> intKey(String name) {
		return new KeyImpl<>(name, Integer.class, null);
	}

	public static Key<Boolean> boolKey(String name) {
		return new KeyImpl<>(name, Boolean.class, null);
	}

	public static Key<String> strKey(String name) {
		return new KeyImpl<>(name, String.class, null);
	}

	public static Key<Dict> dictKey(String name) {
		return new KeyImpl<>(name, Dict.class, null);
	}

	public static Key<Array> arrayKey(String name) {
		return new KeyImpl<>(name, Array.class, null);
	}

	public static Key<Integer> intKey(String name, int defaultValue) {
		return new KeyImpl<>(name, Integer.class, () -> defaultValue);
	}

	public static Key<Boolean> boolKey(String name, boolean defaultValue) {
		return new KeyImpl<>(name, Boolean.class, () -> defaultValue);
	}

	public static Key<String> strKey(String name, String defaultValue) {
		return new KeyImpl<>(name, String.class, () -> defaultValue);
	}

	public static Key<Dict> dictKey(String name, Supplier<Dict> defaultValue) {
		return new KeyImpl<>(name, Dict.class, defaultValue);
	}

	public static Key<Array> arrayKey(String name, Supplier<Array> defaultValue) {
		return new KeyImpl<>(name, Array.class, defaultValue);
	}

	public static <T> Key<T> key(String name, Class<T> type, Supplier<T> defaultValue) {
		return new KeyImpl<>(name, type, defaultValue);
	}

	public static <T> Key<T> key(String name, Class<T> type, Object defaultValue) {
		return new KeyImpl<>(name, type, () -> (T) defaultValue);
	}

	String key();

	Class<T> type();

	T defaultValue();

	String toJava();

	static class KeyImpl<T> implements Key<T> {
		private final String keyCode;
		private final String key;
		private final String code;
		private final Class<T> type;
		private final Supplier<T> defaultValue;

		protected KeyImpl(String key, Class<T> type, Supplier<T> defaultValue) {
			keyCode = key;
			int indexOf = key.indexOf(':');
			if (indexOf >= 0) {
				this.key = key.substring(0, indexOf);
				this.code = String.format("%-4s", key.substring(indexOf + 1));
				if (code.length() > 4)
					throw new IllegalArgumentException("Code must be <= 4 chars: " + key);
			} else {
				this.key = key;
				this.code = null;
			}
			this.type = type;
			this.defaultValue = defaultValue;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public Class<T> type() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof KeyImpl)) {
				return false;
			}
			KeyImpl other = (KeyImpl) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Key<").append(type != null ? type.getName() : "Object").append(">(").append(keyCode)
					.append(")");
			return builder.toString();
		}

		@Override
		public T defaultValue() {
			return defaultValue != null ? defaultValue.get() : null;
		}

		@Override
		public String toJava() {
			return String.format("Key.key(\"%s\", %s.class)", keyCode, type != null ? type.getName() : "Object");
		}

	}
}
