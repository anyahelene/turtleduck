package turtleduck.gl.objects;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.lwjgl.opengl.GL40C;

import turtleduck.gl.FileWatcher;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;

public class ShaderObject {
    public static final Map<String, Integer> SHADER_TYPES;
    static List<Path> shaderPath;
    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("vertex", GL_VERTEX_SHADER);
        map.put("fragment", GL_FRAGMENT_SHADER);
        map.put("geometry", GL40C.GL_GEOMETRY_SHADER); // TODO
        map.put("tess control", GL40C.GL_TESS_CONTROL_SHADER); // TODO
        map.put("tess evaluation", GL40C.GL_TESS_EVALUATION_SHADER); // TODO
        SHADER_TYPES = Collections.unmodifiableMap(map);
        String path = System.getenv("TD_SHADER_PATH");
        if (path == null)
            path = File.pathSeparator + "/turtleduck/gl/shaders/";
        shaderPath = List.of(path.split(File.pathSeparator)).stream().map(s -> Path.of(s)).toList();
    }

    private final int type;
    private int id;
    private Path path;
    private URL url;
    private String code;
    private boolean ready;
    private List<ShaderChangeListener> changeListeners = new ArrayList<>();

    private ShaderObject(String pathName, String code, int type) {
        this.type = type;
        resolve(pathName);
        this.code = code;
    }

    private void resolve(String pathName) {
        if (pathName == null)
            return;
        // if we find an actual file in the file system, use that (enables file watching)
        for (Path dir : shaderPath) {
            Path p = dir.resolve(pathName);
            if (Files.isReadable(p)) {
                path = p;
                return;
            }
        }
        // otherwise we'll read the shader as a resource (probably from a jar file)
        for (Path dir : shaderPath) {
            URL url = ShaderObject.class.getResource(dir.resolve(pathName).toString());
            if (url != null) {
                System.out.println(pathName + ", " + url + ", " + url.getProtocol());
                if (url.getProtocol().equals("file")) {
                    try {
                        path = Path.of(url.toURI());
                        return;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                this.url = url;
                return;
            }
        }
    }

    private boolean load() throws IOException {
        if (path != null) {
            String newCode = Files.readString(path);
            if (!newCode.equals(code)) {
                System.err.printf("Loaded shader source: %s%n", path);
                code = newCode;
                return true;
            }
        } else if (url != null) {
            ByteBuffer buffer = Util.urlToByteBuffer(url, 8192);
            String newCode = Charset.defaultCharset().decode(buffer).toString();
            if (!newCode.equals(code)) {
                System.err.printf("Loaded shader source: %s%n", url);
                code = newCode;
                return true;
            }
        }
        return false;
    }

    private void reload() {
        try {
            if (load()) {
                if (compile()) {
                    for (ShaderChangeListener l : changeListeners)
                        l.changed(this);
                }
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ShaderObject createFromString(String code, int type) throws IOException {
        if (!SHADER_TYPES.values().contains(type)) {
            throw new IllegalArgumentException("Unknown shader type: " + type);
        }
        ShaderObject so = new ShaderObject(null, code, type);
        so.compile();
        return so;
    }

    public static ShaderObject create(String pathName, int type) throws IOException {
        if (!SHADER_TYPES.values().contains(type)) {
            throw new IllegalArgumentException("Unknown shader type: " + type);
        }
        ShaderObject so = new ShaderObject(pathName, null, type);
        so.load();
        if (!so.compile())
            throw new AssertionError("Could not compile shader");
        if (so.path != null)
            FileWatcher.watchFile(so.path, so::reload);
        return so;
    }

    protected boolean compile() {
        int shader = gl.glCreateShader(type);
        try {
            gl.glShaderSource(shader, code);
            gl.glCompileShader(shader);
            int compiled = gl.glGetShaderi(shader, GL_COMPILE_STATUS);
            String shaderLog = gl.glGetShaderInfoLog(shader);
            if (shaderLog.trim().length() > 0) {
                System.err.println(code);
                System.err.println(shaderLog);
            }
            if (compiled == 0) {
                ready = false;
                return false;
            }
            System.err.printf("Loaded shader 0x%x: %s \"%s…\"%n", shader, path, code.substring(0, code.indexOf('\n')));
            if (id > 0)
                gl.glDeleteShader(id);
            id = shader;
            shader = 0;
            ready = true;
        } finally {
            if (shader > 0)
                gl.glDeleteShader(shader);
        }
        return true;
    }

    public void addChangeListener(ShaderChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ShaderChangeListener listener) {
        changeListeners.remove(listener);
    }

    public int id() {
        return id;
    }

    public int type() {
        return type;
    }

    public void dispose() {
        gl.glDeleteShader(id);
        id = 0;
    }

    public interface ShaderChangeListener {
        void changed(ShaderObject obj);
    }
}
