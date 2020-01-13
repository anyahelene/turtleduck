package turtleduck.gl.objects;

import java.util.HashMap;
import java.util.Map;

public class DataObject {
	private static final Map<String,DataObject> objectCache = new HashMap<>();

	private int id;
	private int uses = 1;
	protected final int type;
	protected final String name;
	protected final String uuid;


	@SuppressWarnings("unchecked")
	public static <T extends DataObject> T getCached(String name, int type, Class<T> objectClass) {
		if(name != null) {
			return (T)objectCache.get(uuid(name, type, objectClass.getName()));
		} else {
			return null;
		}

	}

	private static String uuid(String name, int type, String className) {
		return name != null ? className + "." + name + "." + type : null;
	}

	public DataObject cacheIt() {
		if(uuid != null) {
			objectCache.put(uuid, this);
		}
		return this;
	}


	protected <T extends DataObject> DataObject(int id, int type, String name) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.uuid = uuid(name, type, getClass().getName());
	}

	public int id() {
		return id;
	}

	public void check() {
		if(uses <= 0) {
			throw new IllegalStateException("Use after close");
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject> T open() {
		if(uses <= 0) {
			throw new IllegalStateException("Open after close");
		}
		uses++;
		return (T) this;
	}

	public int close() {
		if(uses <= 0) {
			throw new IllegalStateException("Close after close");
		}
		if(--uses == 0) {
			id = -1;
			if(uuid != null) {
				objectCache.remove(uuid);
			}
		}
		return uses;
	}
}
