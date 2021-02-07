package turtleduck.buffer;

import java.nio.ByteBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.DataFormat.Type;
import turtleduck.colors.Color;

public abstract class DataField<T> {
	final String name;
	final int location;
	final int size;
	int offset;
	Type type;

	public DataField(String name, int location, int offset, int size, Type type) {
		super();
		this.name = name;
		this.location = location;
		this.offset = offset;
		this.size = size;
		this.type = type;
	}


	public String toString() {
		String s = String.format("%d: %s %s", location, type, name);
		if (size > 1)
			s = s + "[" + size + "]";
		return s;
	}

	public String toLine() {
		return String.format("  location %d: %d %s at offset %d", location, size, type.name(), offset);
	}

	public int numBytes() {
		return size * type.bytes;
	}

	public int numElements() {
		return size;
	}

	public String name() {
		return name;
	}

	public String baseType() {
		return type.toString();
	}

	public int glType() {
		return type.glType;
	}

	public int location() {
		return location;
	}

	public int offset() {
		return offset;
	}

	public boolean isNormalized() {
		return type.norm;
	}

	public int numLocations() {
		return 1;
	}

	public Type type() {
		return type;
	}

	public T read(ByteBuffer source) {
		throw new UnsupportedOperationException();
	}

	public void write(ByteBuffer dest, T value) {
		throw new UnsupportedOperationException();
	}

	public void write(ByteBuffer dest, float x) {
		throw new UnsupportedOperationException();
	}

	public void write(ByteBuffer dest, float x, float y) {
		throw new UnsupportedOperationException();
	}

	public void write(ByteBuffer dest, float x, float y, float z) {
		throw new UnsupportedOperationException();
	}

	public void write(ByteBuffer dest, float x, float y, float z, float w) {
		throw new UnsupportedOperationException();
	}

	public static class DataField1i extends DataField<Integer> {
		public DataField1i(String name, int location, int offset) {
			super(name, location, offset, 1, Type.FLOAT);
		}

		public void write(ByteBuffer dest, int value) {
			dest.putInt(value);
		}

		public void write(ByteBuffer dest, Integer value) {
			dest.putInt(value);
		}

		public void write(ByteBuffer dest, float x) {
		}

		public void write(ByteBuffer dest, float x, float y) {
		}

		public void write(ByteBuffer dest, float x, float y, float z) {
		}

		public void write(ByteBuffer dest, float x, float y, float z, float w) {
		}
	}

	public static class DataField1f extends DataField<Float> {
		public DataField1f(String name, int location, int offset) {
			super(name, location, offset, 1, Type.FLOAT);
		}

		public Float read(ByteBuffer source) {
			return source.getFloat();
		}

		public void write(ByteBuffer dest, float value) {
			dest.putFloat(value);
		}

		public void write(ByteBuffer dest, Float value) {
			dest.putFloat(value);
		}

		public void write(ByteBuffer dest, float x, float y) {
		}

		public void write(ByteBuffer dest, float x, float y, float z) {
		}

		public void write(ByteBuffer dest, float x, float y, float z, float w) {
		}
	}

	public static class DataField2f extends DataField<Vector2f> {
		public DataField2f(String name, int location, int offset) {
			super(name, location, offset, 2, Type.FLOAT);
		}

		public Vector2f read(ByteBuffer source) {
			Vector2f v = new Vector2f().set(source);
			source.position(source.position() + 8);
			return v;
		}

		public void write(ByteBuffer dest, Vector2f value) {
			value.get(dest);
			dest.position(dest.position() + 8);
		}

		public void write(ByteBuffer dest, float x, float y) {
			dest.putFloat(x);
			dest.putFloat(y);
		}

		public void write(ByteBuffer dest, float x, float y, float z) {
		}

		public void write(ByteBuffer dest, float x, float y, float z, float w) {
		}
	}

	public static class DataField3f extends DataField<Vector3f> {
		public DataField3f(String name, int location, int offset) {
			super(name, location, offset, 3, Type.FLOAT);
		}

		public Vector3f read(ByteBuffer source) {
			Vector3f v = new Vector3f().set(source);
			source.position(source.position() + 12);
			return v;
		}

		public void write(ByteBuffer dest, Vector3f value) {
			value.get(dest);
			dest.position(dest.position() + 12);
		}

		public void write(ByteBuffer dest, float x, float y, float z) {
			dest.putFloat(x);
			dest.putFloat(y);
			dest.putFloat(z);
		}
	}

	public static class DataField4f extends DataField<Vector4f> {
		public DataField4f(String name, int location, int offset) {
			super(name, location, offset, 4, Type.FLOAT);
		}

		public Vector4f read(ByteBuffer source) {
			Vector4f v = new Vector4f().set(source);
			source.position(source.position() + 16);
			return v;
		}

		public void write(ByteBuffer dest, Vector4f value) {
			value.get(dest);
			dest.position(dest.position() + 16);
		}

		public void write(ByteBuffer dest, float x, float y, float z, float w) {
			dest.putFloat(x);
			dest.putFloat(y);
			dest.putFloat(z);
			dest.putFloat(w);
		}

	}

	public static class DataField4c extends DataField<Color> {
		public DataField4c(String name, int location, int offset) {
			super(name, location, offset, 4, Type.NORM_SHORT);
		}

		public Color read(ByteBuffer source) {
			int r = source.getShort() & 0xffff;
			int g = source.getShort() & 0xffff;
			int b = source.getShort() & 0xffff;
			int a = source.getShort() & 0xffff;
			return Color.color(r / 65535.0, g / 65535.0, b / 65535.0, a / 65535.0);
		}

		public void write(ByteBuffer dest, Color color) {
			dest.putShort((short) Math.round(color.red() * 65535));
			dest.putShort((short) Math.round(color.green() * 65535));
			dest.putShort((short) Math.round(color.blue() * 65535));
			dest.putShort((short) Math.round(color.opacity() * 65535));
		}

		public void write(ByteBuffer dest, float x, float y, float z, float w) {
			dest.putShort((short) Math.round(x * 65535));
			dest.putShort((short) Math.round(y * 65535));
			dest.putShort((short) Math.round(z * 65535));
			dest.putShort((short) Math.round(w * 65535));
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + location;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + offset;
		result = prime * result + size;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DataField)) {
			return false;
		}
		DataField<?> other = (DataField<?>) obj;
		if (location != other.location) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (offset != other.offset) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}
}