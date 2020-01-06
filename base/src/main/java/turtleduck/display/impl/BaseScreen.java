package turtleduck.display.impl;

import turtleduck.display.Screen;
import turtleduck.objects.IdentifiedObject;

public abstract class BaseScreen implements Screen {
	protected final String id;
	private int nLayers = 0;
	public BaseScreen() {
		id = IdentifiedObject.Registry.makeId(Screen.class, this);
	}
	
	@Override
	public String id() {
		return id;
	}
	
	protected String newLayerId() {
		return id + "." + nLayers++;
	}
}
