package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL33C.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.opengl.ARBProgramInterfaceQuery;
import org.lwjgl.opengl.GL43C;

import turtleduck.gl.objects.Variables.AbstractUniform;
import turtleduck.gl.objects.Variables.TypeDesc;

public class ShaderProgram extends DataHandle<ShaderProgram, ShaderProgram.ProgramData> {
	private static ProgramData lastBound = null;

	public ShaderProgram(ProgramData data) {
		super(data);
	}

	public static ShaderProgram createProgram(String name, ShaderObject... shaders) throws IOException {
		ProgramData data = ProgramData.getCached(name);
		if (data != null) {
			// TODO: check that we use the same shaders
			for (int i = 0; i < shaders.length; i++) {
				if (!shaders[i].equals(data.shaders.get(i))) {
					throw new IllegalArgumentException("Program already created with different shaders");
				}
			}
			return new ShaderProgram(data.open());
		}
		int program = glCreateProgram();
		data = new ProgramData(program, name);
		for (ShaderObject sh : shaders) {
			glAttachShader(program, sh.id());
			data.shaders.add(sh);
		}
		glLinkProgram(program);
		int linked = glGetProgrami(program, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(program);
		if (programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		data.log = programLog;
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		} else {
			data.linked = true;
		}

		data.cacheIt();
		ShaderProgram prog = new ShaderProgram(data);
		prog.processVars();
		for (int i = 0; i < 10; i++) {
			int loc = prog.getUniformLocation("texture" + i);
			if (loc >= 0) {
				glUniform1i(loc, i);
			}
		}
		return prog;
	}

	private void processVars() {
		ProgramData data = data();
		if (data.vars == null) {
			data.vars = new HashMap<>();
		}

		int program = data().id();
		bind();
		int nUnis = ARBProgramInterfaceQuery.glGetProgramInterfacei(program, ARBProgramInterfaceQuery.GL_UNIFORM, ARBProgramInterfaceQuery.GL_ACTIVE_RESOURCES);
//		int nUnis = glGetProgramInterfacei(program, GL43C.GL_UNIFORM, GL43C.GL_ACTIVE_RESOURCES);
		for (int i = 0; i < nUnis; i++) {
			String s = ARBProgramInterfaceQuery.glGetProgramResourceName(program, ARBProgramInterfaceQuery.GL_UNIFORM, i);
			int[] props = { ARBProgramInterfaceQuery.GL_BLOCK_INDEX, ARBProgramInterfaceQuery.GL_TYPE, ARBProgramInterfaceQuery.GL_LOCATION };
			int[] values = new int[props.length];
			ARBProgramInterfaceQuery.glGetProgramResourceiv(program, ARBProgramInterfaceQuery.GL_UNIFORM, i, props, null, values);
			System.out.println("uniform " + i + ": " + s + " " + Arrays.toString(values));
			if (s.startsWith("texture")) {
				try {
					int n = Integer.parseInt(s.substring(7));
					glUniform1i(values[2], n);
				} catch (NumberFormatException e) {
				}
			}
			try {
				AbstractUniform<?> var = Variables.createVariable(values[1]);
				if (var != null) {
					var.loc = values[2];
					var.name = s;
					var.program = this;
					data.vars.put(s, var);
					System.out.println(var);
				}
			} catch (IllegalArgumentException e) {
				System.out.println(s + ": type=" + values[1] + ", loc=" + values[2]);
			}
		}

		int nInputs = ARBProgramInterfaceQuery.glGetProgramInterfacei(program, ARBProgramInterfaceQuery.GL_PROGRAM_INPUT, ARBProgramInterfaceQuery.GL_ACTIVE_RESOURCES);
		for (int i = 0; i < nInputs; i++) {
			String s = ARBProgramInterfaceQuery.glGetProgramResourceName(program, ARBProgramInterfaceQuery.GL_PROGRAM_INPUT, i);
			int[] props = { ARBProgramInterfaceQuery.GL_TYPE, ARBProgramInterfaceQuery.GL_LOCATION };
			int[] values = new int[props.length];
			ARBProgramInterfaceQuery.glGetProgramResourceiv(program, ARBProgramInterfaceQuery.GL_PROGRAM_INPUT, i, props, null, values);
			TypeDesc typeDesc = Variables.GL_TYPES.get(values[0]);
			if(typeDesc != null) {
				data.inputFormat.addField(s, values[1], typeDesc);
			}
		}
		System.out.println("Input format: " + data.inputFormat);
	}

	public int getUniformLocation(String name) {
		return glGetUniformLocation(data().id(), name);
	}

	public void setUniform(String name, int value) {
		bind();
		glUniform1i(getUniformLocation(name), value);
	}

	public void setUniform(String name, float value) {
		bind();
		glUniform1f(getUniformLocation(name), value);
	}

	public void setUniform(String name, Vector2fc value) {
		bind();
		glUniform2f(getUniformLocation(name), value.x(), value.y());
	}

	public void setUniform(String name, Vector3fc value) {
		bind();
		glUniform3f(getUniformLocation(name), value.x(), value.y(), value.z());
	}

	public void setUniform(String name, Vector4fc value) {
		bind();
		glUniform4f(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
	}

	public int getUniform(String name) {
		return glGetUniformi(data().id(), getUniformLocation(name));
	}

	public VertexArrayFormat format() {
		ProgramData data = data();
		return data.format != null ? data.format : data.inputFormat;
	}
	public void format(VertexArrayFormat format) {
		data().format = format;
	}
	static class ProgramData extends DataObject {
		List<ShaderObject> shaders = new ArrayList<>();
		String log = "";
		boolean linked = false;
		Map<String, Uniform<?>> vars;
		VertexArrayFormat inputFormat = new VertexArrayFormat();
		VertexArrayFormat format = null;

		public static ProgramData getCached(String name) {
			return DataObject.getCached(name, GL43C.GL_PROGRAM, ProgramData.class);
		}

		public ProgramData(int id, String name) {
			super(id, GL43C.GL_PROGRAM, name);
		}

	}

	@Override
	protected void dispose(int id, ProgramData data) {
		if (lastBound == data) {
			glUseProgram(0);
		}
		glDeleteProgram(id);
	}

	@Override
	public void bind() {
		ProgramData data = data();
		if (lastBound != data) {
			glUseProgram(data().id());
			lastBound = data;
		}
	}

	@Override
	public String toString() {
		return "program(" + id() + ")";
	}

	@SuppressWarnings("unchecked")
	public <T> Uniform<T> uniform(String name, Class<T> type) {
		Map<String, Uniform<?>> vars = data().vars;
		if (vars == null) {
			processVars();
		}
		return (Uniform<T>) vars.get(name);
	}

	@Override
	protected ShaderProgram create(ProgramData data) {
		return new ShaderProgram(data);
	}
}
