package turtleduck.events;

import java.util.function.Function;
import java.util.function.Supplier;

public class Controls {
	public static <T> InputControl<T> create(String name, Supplier<T> source) {
		return create(name.replace(" ", "-"), name, name, source);
	}
	public static <T> InputControl<T> create(String id, String name, String shortName, Supplier<T> source) {
		return new InputControl<T>() {

			@Override
			public String id() {
				return id;
			}

			@Override
			public String name() {
				return name;
			}

			@Override
			public String shortName() {
				return shortName;
			}

			@Override
			public T get() {
				return source.get();
			}
			
		};
	}
	public static <T> InputControl<T> transform(InputControl<T> inputControl, Function<T,T> fun) {
		return new DelegateControl<T,T>(inputControl) {
			@Override
			public T get() {
				return fun.apply(inputControl.get());
			}	
		};
	}
	public static <T,U> InputControl<U> translate(InputControl<T> inputControl, Function<T,U> fun) {
		return new DelegateControl<U,T>(inputControl) {
			@Override
			public U get() {
				return fun.apply(inputControl.get());
			}	
		};
	}
	protected static  abstract class DelegateControl<T,U> implements InputControl<T> {
		protected final InputControl<U> parent;
		public DelegateControl(InputControl<U> parent) {
			this.parent = parent;
		}
		@Override
		public String id() {
			return parent.id();
		}

		@Override
		public String name() {
			return parent.name();
		}

		@Override
		public String shortName() {
			return parent.shortName();
		}

	}
}
