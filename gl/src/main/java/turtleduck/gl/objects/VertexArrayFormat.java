package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL33C.*;

import org.joml.Vector3f;

import turtleduck.buffer.DataField;
import turtleduck.buffer.DataFormat;
import turtleduck.gl.objects.Variables.TypeDesc;

public class VertexArrayFormat extends DataFormat {

	private boolean DEBUG = false;

	public VertexArrayFormat() {
	}

	public DataField<?> addField(String name, int location, TypeDesc type) {
		Type t;
		switch (type.baseType) {
		case "float":
			t = Type.FLOAT;
			break;
		default:
			System.err.println("Don't know how to deal with type " + type.name);
			t = Type.FLOAT;
		}
		DataField<?> field;
		switch (type.rows) {
		case 1:
			field = new DataField.DataField1f(name, location, vertexSize);
			break;
		case 2:
			field = new DataField.DataField2f(name, location, vertexSize);
			break;
		case 3:
			field = new DataField.DataField3f(name, location, vertexSize);
			break;
		case 4:
			field = new DataField.DataField4f(name, location, vertexSize);
			break;
		default:
			throw new IllegalArgumentException(
					String.format("Don't know how to deal with type %s[%d][%d]", type.name, type.rows, type.cols));
		}

		addField(field);
		return field;
	}
	
		

	public void setVertexAttributes(long offset) {
		for (DataField<?> field : fields) {
			if (DEBUG)
				System.out.println(field.toLine());
			glVertexAttribPointer(field.location(), field.numElements(), field.glType(), field.isNormalized(),
					vertexSize, offset + field.offset());
			glEnableVertexAttribArray(field.location());
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