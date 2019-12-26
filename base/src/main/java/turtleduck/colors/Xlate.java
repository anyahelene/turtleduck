package turtleduck.colors;

import java.util.ServiceLoader;

public interface Xlate {
	public static Xlate xlator = ServiceLoader.load(Xlate.class).findFirst().orElseThrow();
	Object translate(IColor col);
	
}
