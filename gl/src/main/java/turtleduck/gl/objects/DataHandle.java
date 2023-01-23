package turtleduck.gl.objects;

public abstract class DataHandle<C extends DataHandle<?,?>, D extends DataObject> implements AutoCloseable {
	private D data;

	public DataHandle(D data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : System.identityHashCode(data));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DataHandle)) {
			return false;
		}
		DataHandle<?,?> other = (DataHandle<?,?>) obj;
		return data == other.data;
	}

	protected abstract void dispose(int id, D data);
	protected abstract void bind();
	protected abstract C create(D data);

	protected D data() {
		checkOpen();
		return data;
	}

	protected void checkOpen() {
		if(data == null) {
			throw new IllegalStateException("Attempt to use data after close()");
		} else {
			data.check();
		}
	}

	public C use() {
		checkOpen();
		bind();
		return create(data.open());
	}

	public int id() {
		return data().id();
	}
	@Override
	public void close() {
		if(data == null) {
			return;
		}
		D d = data;
		int id = d.id();
		data = null;
		if(d.close() <= 0) {
			dispose(id, d);
		}
	}
}
