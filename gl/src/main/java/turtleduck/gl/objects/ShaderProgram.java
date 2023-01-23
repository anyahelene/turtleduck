package turtleduck.gl.objects;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import turtleduck.gl.GLScreen;
import turtleduck.gl.compat.GLConstants;
import turtleduck.gl.compat.ProgramInterfaceQuery;
import turtleduck.gl.objects.Variables.AbstractUniform;
import turtleduck.gl.objects.Variables.TypeDesc;

public class ShaderProgram implements ShaderObject.ShaderChangeListener {
    private static int lastBound = 0;
    private final boolean debug = true;
    List<ShaderObject> shaders = new ArrayList<>();
    String log = "";
    boolean linked = false;
    Map<String, Uniform<?>> vars;
    VertexArrayFormat inputFormat = new VertexArrayFormat();
    VertexArrayFormat format = null;
    private String name;
    private int id = 0;

    private ShaderProgram(String name, VertexArrayFormat format, ShaderObject... shaders) {
        this.name = name;
        if (format != null)
            this.format = format;
        this.shaders = List.of(shaders);
    }

    public static void unbind() {
        if (lastBound != 0) {
            gl.glUseProgram(0);
            lastBound = 0;
        }
    }

    public static void bind(int program) {
        gl.glUseProgram(program);
        lastBound = program;
    }

    public int id() {
        return id;
    }

    public static ShaderProgram createProgram(String name, VertexArrayFormat format, ShaderObject... shaders)
            throws IOException {
        ShaderProgram prog = new ShaderProgram(name, format, shaders);
        for (ShaderObject sh : shaders)
            sh.addChangeListener(prog);
        if (!prog.compile())
            throw new AssertionError("Could not link program:" + name);

        return prog;
    }

    boolean compile() {
        int program = gl.glCreateProgram();
        try {
            for (ShaderObject sh : shaders) {
                gl.glAttachShader(program, sh.id());
            }
            gl.glLinkProgram(program);
            int linked = gl.glGetProgrami(program, GL_LINK_STATUS);
            log = gl.glGetProgramInfoLog(program);
            if (log.trim().length() > 0) {
                System.err.println(log);
            }
            if (linked == 0) {
                return false;
            } else {
                this.linked = true;
            }
            System.err.printf("Linked shader program 0x%x: %s%n", program, name);

            if (id > 0) {
                gl.glDeleteProgram(id);
            }
            bind(program);
            id = program;
            program = 0;
            if (gl.hasProgramInterfaceQuery())
                processVars();
            for (int i = 0; i < 10; i++) {
                int loc = getUniformLocation("texture" + i);
                if (loc >= 0) {
                    gl.glUniform1i(loc, i);
                }
            }
        } finally {
            if (program > 0)
                gl.glDeleteProgram(program);
        }
        return true;
    }

    private void processVars() {
        if (vars == null) {
            vars = new HashMap<>();
        }

        int program = id;

        bind();
        ProgramInterfaceQuery piq = gl.getProgramInterfaceQuery();
        if (piq == null) {
            throw new UnsupportedOperationException("ProgramInterfaceQuery");
        }
        int nUnis = piq.glGetProgramInterfacei(program, ProgramInterfaceQuery.GL_UNIFORM,
                ProgramInterfaceQuery.GL_ACTIVE_RESOURCES);
        // int nUnis = glGetProgramInterfacei(program, GL43C.GL_UNIFORM,
        // GL43C.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < nUnis; i++) {
            String s = piq.glGetProgramResourceName(program, ProgramInterfaceQuery.GL_UNIFORM,
                    i);
            int[] props = { ProgramInterfaceQuery.GL_BLOCK_INDEX, ProgramInterfaceQuery.GL_TYPE,
                    ProgramInterfaceQuery.GL_LOCATION };
            int[] values = new int[props.length];
            piq.glGetProgramResourceiv(program, ProgramInterfaceQuery.GL_UNIFORM, i, props,
                    null, values);
            if (debug)
                System.out.println("uniform " + i + ": " + s + " " + Arrays.toString(values));
            if (s.startsWith("texture")) {
                try {
                    int n = Integer.parseInt(s.substring(7));
                    gl.glUniform1i(values[2], n);
                } catch (NumberFormatException e) {
                }
            }
            try {
                AbstractUniform<?> var = Variables.createVariable(values[1]);
                if (var != null) {
                    var.loc = values[2];
                    var.name = s;
                    var.program = this;
                    vars.put(s, var);
                    if (debug)
                        System.out.println(var);
                }
            } catch (IllegalArgumentException e) {
                System.out.println(s + ": type=" + values[1] + ", loc=" + values[2]);
            }
        }

        Map<Integer, String> fieldNames = new HashMap<>();
        Map<Integer, TypeDesc> fieldTypes = new HashMap<>();
        int nInputs = piq.glGetProgramInterfacei(program,
                ProgramInterfaceQuery.GL_PROGRAM_INPUT, ProgramInterfaceQuery.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < nInputs; i++) {
            String s = piq.glGetProgramResourceName(program,
                    ProgramInterfaceQuery.GL_PROGRAM_INPUT, i);
            int[] props = { ProgramInterfaceQuery.GL_TYPE, ProgramInterfaceQuery.GL_LOCATION };
            int[] values = new int[props.length];
            piq.glGetProgramResourceiv(program, ProgramInterfaceQuery.GL_PROGRAM_INPUT, i,
                    props, null, values);
            TypeDesc typeDesc = Variables.GL_TYPES.get(values[0]);
            if (typeDesc != null) {
                fieldNames.put(values[1], s);
                fieldTypes.put(values[1], typeDesc);
            }
        }
        List<Integer> locations = new ArrayList<>(fieldNames.keySet());
        Collections.sort(locations);
        inputFormat = new VertexArrayFormat();
        for (int i : locations) {
            inputFormat.addField(fieldNames.get(i), i, fieldTypes.get(i));
        }
        if (debug)
            System.out.println("Input format: " + inputFormat);
    }

    public int getUniformLocation(String name) {
        return gl.glGetUniformLocation(id, name);
    }

    public void setUniform(String name, int value) {
        bind();
        gl.glUniform1i(getUniformLocation(name), value);
    }

    public void setUniform(String name, float value) {
        bind();
        gl.glUniform1f(getUniformLocation(name), value);
    }

    public void setUniform(String name, Vector2fc value) {
        bind();
        gl.glUniform2f(getUniformLocation(name), value.x(), value.y());
    }

    public void setUniform(String name, Vector3fc value) {
        bind();
        gl.glUniform3f(getUniformLocation(name), value.x(), value.y(), value.z());
    }

    public void setUniform(String name, Vector4fc value) {
        bind();
        gl.glUniform4f(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
    }

    public int getUniform(String name) {
        return gl.glGetUniformi(id, getUniformLocation(name));
    }

    public VertexArrayFormat format() {
        return format != null ? format : inputFormat;
    }

    public void format(VertexArrayFormat format) {
        this.format = format;
    }

    public void dispose() {
        if (lastBound == id) {
            gl.glUseProgram(0);
        }
        gl.glDeleteProgram(id);
    }

    public void bind() {
        if (lastBound != id) {
            gl.glUseProgram(id);
            lastBound = id;
        }
    }

    @Override
    public String toString() {
        return "program(" + id + ")";
    }

    @SuppressWarnings("unchecked")
    public <T> Uniform<T> uniform(String name, Class<T> type) {
        if (vars == null) {
            if (gl.hasProgramInterfaceQuery()) {
                processVars();
            } else {
                bind();
                vars = new HashMap<>();
            }
        }
        Uniform<?> uniform = vars.get(name);
        if (uniform == null) {
            AbstractUniform<?> var = Variables.createVariable(type);
            var.loc = getUniformLocation(name);
            var.name = name;
            System.out.println("Placeholder var: " + var);
            var.program = this;
            vars.put(name, var);
            uniform = var;
        }
        return (Uniform<T>) vars.get(name);
    }

    @Override
    public void changed(ShaderObject obj) {
        try {
            System.err.println("program changed, recompiling: " + name);
            compile();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
