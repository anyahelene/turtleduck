package turtleduck.tea;

import java.util.HashMap;
import java.util.Map;

public class Env {
		Map<Var<?>, Object> data = new HashMap<>();
		public static class Var<T> {
			
			private Class<T> clazz;

			public Var(Class<T> clazz) {
				this.clazz = clazz;
			}
		}
		
		public <T> T get(Var<T> var) {
			Object obj = data.get(var);
			if(var.clazz.isInstance(obj))
				return (T) obj;
			else 
				return null;
		}
}
