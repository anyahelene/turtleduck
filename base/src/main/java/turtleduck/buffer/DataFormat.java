package turtleduck.buffer;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import turtleduck.colors.Color;

public class DataFormat {
	private static final int GL_BYTE = 0x1400, GL_UNSIGNED_BYTE = 0x1401, GL_SHORT = 0x1402, GL_UNSIGNED_SHORT = 0x1403,
			GL_INT = 0x1404, GL_UNSIGNED_INT = 0x1405, GL_FLOAT = 0x1406, GL_DOUBLE = 0x140A;

	public enum Type {
		FLOAT(4, GL_FLOAT, false), NORM_BYTE(1, GL_UNSIGNED_BYTE, true), NORM_SHORT(2, GL_UNSIGNED_SHORT, true),
		UNSIGNED_BYTE(1, GL_UNSIGNED_BYTE, false), UNSIGNED_SHORT(2, GL_UNSIGNED_SHORT, false),
		INTEGER(4, GL_INT, false);

		protected int bytes;
		protected int glType;
		protected boolean norm;

		Type(int bytes, int glType, boolean norm) {
			this.bytes = bytes;
			this.glType = glType;
			this.norm = norm;
		}
	};

	protected List<DataField<?>> fields = new ArrayList<>();
	protected int vertexSize = 0;
	protected int location = 0;
	protected boolean DEBUG;

	public DataFormat() {
	}

	protected void addField(DataField<?> field) {
		fields.add(field);
		location += field.numLocations();
		vertexSize += field.numBytes();

	}

	protected <T> DataField<T> makeField(String name, Class<T> type, int loc, int off) {
		if (type == Integer.class) {
			return (DataField<T>) new DataField.DataField1i(name, loc, off);
		} else if (type == Float.class) {
			return (DataField<T>) new DataField.DataField1f(name, loc, off);
		} else if (Vector2fc.class.isAssignableFrom(type)) {
			return (DataField<T>) new DataField.DataField2f(name, loc, off);
		} else if (Vector3fc.class.isAssignableFrom(type)) {
			return (DataField<T>) new DataField.DataField3f(name, loc, off);
		} else if (Vector4fc.class.isAssignableFrom(type)) {
			return (DataField<T>) new DataField.DataField4f(name, loc, off);
		} else if (Color.class.isAssignableFrom(type)) {
			return (DataField<T>) new DataField.DataField4c(name, loc, off);
		} else {
			throw new IllegalArgumentException(type.getName());
		}
	}

	public <T> DataField<T> addField(String name, Class<T> type) {
		DataField<T> field = makeField(name, type, location, vertexSize);
		addField(field);
		return field;
	}

	public <T> DataField<T> setField(String name, Class<T> type) {
		for (int i = 0; i < fields.size(); i++) {
			DataField<?> oldField = fields.get(i);
			if (oldField.name.equals(name)) {
				DataField<T> field = makeField(name, type, oldField.location, oldField.offset);
				if (oldField.numLocations() != field.numLocations())
					throw new IllegalArgumentException(
							"Incompatible replacement field " + field + " for old field " + oldField);
				fields.set(i, field);
				int sizeDiff = field.numBytes() - oldField.numBytes();
				if(sizeDiff != 0) {
					for(int j = i+1; j < fields.size(); j++) {
						System.out.print(fields.get(j));
						fields.get(j).offset += sizeDiff;
						System.out.println(" → " + fields.get(j));
				}
					vertexSize += sizeDiff;
				}
				return field;
			}
		}
		throw new IllegalArgumentException("Illegal field " + name + " (for " + toString() + ")");
	}


	public String toString() {
		return fields.toString();
	}

	public DataField<?> field(int index) {
		return fields.get(index);
	}

	public int numBytes() {
		return vertexSize;
	}

	public int numFields() {
		return fields.size();
	}
}