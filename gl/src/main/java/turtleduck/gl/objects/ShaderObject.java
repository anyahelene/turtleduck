package turtleduck.gl.objects;

import static turtleduck.gl.objects.Util.ioResourceToByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL40C;

import static org.lwjgl.opengl.GL32C.*;

public class ShaderObject extends DataHandle<ShaderObject, DataObject> {
	public static final Map<String, Integer> SHADER_TYPES;

	static {
		Map<String, Integer> map = new HashMap<>();
		map.put("vertex", GL_VERTEX_SHADER);
		map.put("fragment", GL_FRAGMENT_SHADER);
		map.put("geometry", GL_GEOMETRY_SHADER);
		map.put("tess control", GL40C.GL_TESS_CONTROL_SHADER);
		map.put("tess evaluation", GL40C.GL_TESS_EVALUATION_SHADER);
		SHADER_TYPES = Collections.unmodifiableMap(map);
	}

	private ShaderObject(DataObject data) {
		super(data);
	}

	public static ShaderObject createFromString(String code, int type) throws IOException {
		DataObject data = DataObject.getCached(code, type, DataObject.class);
		if (data != null) {
			return new ShaderObject(data.open());
		} else {

		}
		if (!SHADER_TYPES.values().contains(type)) {
			throw new IllegalArgumentException("Unknown shader type: " + type);
		}
		int shader = glCreateShader(type);
		try {
			glShaderSource(shader, code);
			glCompileShader(shader);
			int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
			String shaderLog = glGetShaderInfoLog(shader);
			if (shaderLog.trim().length() > 0) {
				System.err.println(code);
				System.err.println(shaderLog);
			}
			if (compiled == 0) {
				throw new AssertionError("Could not compile shader");
			}

			data = new DataObject(shader, type, code);
			data.cacheIt();
			System.err.printf("Loaded shader 0x%x: %s%n", shader, code.substring(0, code.indexOf('\n')));
			shader = -1;
			return new ShaderObject(data);
		} finally {
			if (shader >= 0) { // set to -1 on success
				glDeleteShader(shader);
			}
		}
	}

	public static ShaderObject create(String pathName, int type) throws IOException {
		DataObject data = DataObject.getCached(pathName, type, DataObject.class);
		if (data != null) {
			return new ShaderObject(data.open());
		} else {

		}
		if (!SHADER_TYPES.values().contains(type)) {
			throw new IllegalArgumentException("Unknown shader type: " + type);
		}
		int shader = glCreateShader(type);
		try {
			ByteBuffer source = ioResourceToByteBuffer(pathName, 1024);
			PointerBuffer strings = BufferUtils.createPointerBuffer(1);
			IntBuffer lengths = BufferUtils.createIntBuffer(1);
			strings.put(0, source);
			lengths.put(0, source.remaining());
			glShaderSource(shader, strings, lengths);
			glCompileShader(shader);
			int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
			String shaderLog = glGetShaderInfoLog(shader);
			if (shaderLog.trim().length() > 0) {
				System.err.println(shaderLog);
			}
			if (compiled == 0) {
				throw new AssertionError("Could not compile shader: " + pathName);
			}

			data = new DataObject(shader, type, pathName);
			data.cacheIt();
			System.err.printf("Loaded shader 0x%x: %s%n", shader, pathName);
			shader = -1;
			return new ShaderObject(data);
		} finally {
			if (shader >= 0) { // set to -1 on success
				glDeleteShader(shader);
			}
		}
	}

	public int type() {
		return data().type;
	}

	@Override
	protected void dispose(int id, DataObject data) {
		glDeleteShader(id);
	}

	@Override
	protected void bind() {
	}

	@Override
	protected ShaderObject create(DataObject data) {
		return new ShaderObject(data);
	}

}
