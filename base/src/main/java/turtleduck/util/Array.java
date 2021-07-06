package turtleduck.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface Array extends Iterable<Object> {
	public static Array create() {
		return new ArrayImpl<>(new ArrayList<>(), null);
	}

	public static <T> Array of(Class<T> type) {
		ArrayImpl<T> a = new ArrayImpl<T>(new ArrayList<>(), type);
		return a;
	}

	public static <T> Array of(Class<T> type, T first) {
		ArrayImpl<T> a = new ArrayImpl<T>(new ArrayList<>(), type);
		a.add(first);
		return a;
	}

	public static <T> Array of(T first) {
		@SuppressWarnings("unchecked")
		ArrayImpl<T> a = new ArrayImpl<T>(new ArrayList<>(), (Class<T>) first.getClass());
		a.add(first);
		return a;
	}

	public static <T> Array of(Class<T> type, T... elements) {
		ArrayImpl<T> a = new ArrayImpl<T>(new ArrayList<>(), type);
		for (T e : elements)
			a.add(e);

		return a;
	}

	public static <T> Array of(T first, T... rest) {
		@SuppressWarnings("unchecked")
		ArrayImpl<T> a = new ArrayImpl<T>(new ArrayList<>(), (Class<T>) first.getClass());
		a.add(first);
		for (T e : rest)
			a.add(e);
		return a;
	}
	
	Class<?> elementType();

	Object get(int index);

	<T> T get(int index, Class<T> type);

	<T> Array set(int index, T value);

	<T> Array add(T value);

	<T> Array insert(int index, T value);

	<T> T remove(int index, Class<T> type);

	Object remove(int index);

	int size();

	boolean isEmpty();

	<T> List<T> toListOf(Class<T> type);

	<T> Stream<T> stream(Class<T> type);

	Stream<?> stream();

	static class ArrayImpl<U> implements Array {
		private List<U> list;
		private Class<U> type;

		protected ArrayImpl(List<U> list, Class<U> type) {
			this.list = list;
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<Object> iterator() {
			return (Iterator<Object>) list.iterator();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<?> elementType() {
			if (type == null) {
				if (!list.isEmpty()) {
					type = (Class<U>) list.get(0).getClass();
				} else {
					return Object.class;
				}
			}
			return type;
		}

		@Override
		public Object get(int index) {
			return list.get(index);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(int index, Class<T> type) {
			U u = list.get(index);
			if (!type.isInstance(u))
				throw new ClassCastException("expected " + type.getName() + ", got " + u.getClass().getName());
			return (T) u;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Array set(int index, T value) {
			if (type != null && !type.isInstance(value))
				throw new ClassCastException("expected " + type.getName() + ", got " + value.getClass().getName());
			list.set(index, (U) value);
			return this;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public boolean isEmpty() {
			return list.isEmpty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> toListOf(Class<T> type) {
			if (this.type != null) {
				if (!type.isAssignableFrom(this.type))
					throw new ClassCastException("expected " + type.getName() + ", got " + this.type.getName());
			} else {
				for (U u : list) {
					if (!type.isInstance(u))
						throw new ClassCastException("expected " + type.getName() + ", got " + u.getClass().getName());
				}
			}
			return (List<T>) list;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Array add(T value) {
			if (type != null && !type.isInstance(value)) {
				throw new ClassCastException("expected " + type.getName() + ", got " + value.getClass().getName());
			}
			list.add((U) value);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Array insert(int index, T value) {
			if (type != null && !type.isInstance(value)) {
				throw new ClassCastException("expected " + type.getName() + ", got " + value.getClass().getName());
			}
			list.add(index, (U) value);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T remove(int index, Class<T> type) {
			U u = list.remove(index);
			if (!type.isInstance(u))
				throw new ClassCastException("expected " + type.getName() + ", got " + u.getClass().getName());
			return (T) u;
		}

		@Override
		public U remove(int index) {
			U u = list.get(index);

			return u;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((list == null) ? 0 : list.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ArrayImpl)) {
				return false;
			}
			ArrayImpl other = (ArrayImpl) obj;
			if (list == null) {
				if (other.list != null) {
					return false;
				}
			} else if (!list.equals(other.list)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return JsonUtil.encode(this);
		}

		@Override
		public <T> Stream<T> stream(Class<T> type) {
			return toListOf(type).stream();
		}

		@Override
		public Stream<?> stream() {
			return list.stream();
		}
	}

	public static <T> Array from(List<T> list, Class<T> type) {
		return new ArrayImpl<>(list, type);
	}
	public static <T> Array from(Collection<T> list, Class<T> type) {
		return new ArrayImpl<>(new ArrayList<>(list), type);
	}
}
