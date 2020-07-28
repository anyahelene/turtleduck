package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL40.*;

import java.util.ArrayList;
import java.util.List;

import turtleduck.gl.objects.Variables.TypeDesc;

public class VertexArrayFormat {
	enum Type {
		FLOAT(4, GL_FLOAT, false), NORM_BYTE(1, GL_UNSIGNED_BYTE, true), NORM_SHORT(2, GL_UNSIGNED_SHORT, true),
		UNSIGNED_BYTE(1, GL_UNSIGNED_BYTE, false), UNSIGNED_SHORT(2, GL_UNSIGNED_SHORT, false);

		protected int bytes;
		protected int glType;
		protected boolean norm;

		Type(int bytes, int glType, boolean norm) {
			this.bytes = bytes;
			this.glType = glType;
			this.norm = norm;
		}
	};

	List<Field> fields = new ArrayList<>();
	int vertexSize = 0;
	private boolean DEBUG;

	static class Field {
		String name;
		int location;
		int size;

		public Field(String name, int location, int size, Type type) {
			super();
			this.name = name;
			this.location = location;
			this.size = size;
			this.type = type;
		}

		Type type;

		public String toString() {
			String s = String.format("%d: %s %s", location, type, name);
			if (size > 1)
				s = s + "[" + size + "]";
			return s;
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

		public int location() {
			return location;
		}
	}

	public VertexArrayFormat() {
	}

	public void addField(String name, int location, TypeDesc type) {
		Type t;
		switch (type.baseType) {
		case "float":
			t = Type.FLOAT;
			break;
		default:
			System.err.println("Don't know how to deal with type " + type.name);
			t = Type.FLOAT;
		}
		Field field = new Field(name, location, type.rows * type.cols, t);
		fields.add(field);
		vertexSize += field.numBytes();
	}

	public void layoutFloat(String name, int location, int numFloats) {
		fields.add(new Field(name, location, numFloats, Type.FLOAT));
		vertexSize += numFloats * 4;
	}

	public void layoutNormByte(String name, int location, int numComponents) {
		fields.add(new Field(name, location, numComponents, Type.NORM_BYTE));
		vertexSize += numComponents;
	}

	public void layoutNormShort(String name, int location, int numComponents) {
		fields.add(new Field(name, location, numComponents, Type.NORM_SHORT));
		vertexSize += numComponents * 2;
	}

	public void setVertexAttributes() {
		int offset = 0;

		for (Field field : fields) {
			if (DEBUG)
				System.out.printf("  location %d: %d %s at offset %d%n", field.location, field.size, field.type,
						offset);
			glVertexAttribPointer(field.location, field.size, field.type.glType, field.type.norm, vertexSize, offset);
			glEnableVertexAttribArray(field.location);
			offset += field.numBytes();
		}
	}

	public String toString() {
		return fields.toString();
	}
	
	public VertexArrayBuilder build(int usage) {
		return new VertexArrayBuilder(this, usage);
	}
	public VertexArrayBuilder build(int usage, int vertexCapacity) {
		return new VertexArrayBuilder(this, usage, vertexCapacity);
	}
}