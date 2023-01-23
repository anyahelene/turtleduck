package turtleduck.gl.compat;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GlBase implements GLA {
    protected boolean initialized = false;
    protected ProgramInterfaceQuery piqImpl;
    protected TexImageMultisample timImpl;
    protected String version, shaderVersion, vendor, renderer;
    protected final String variantName;
    protected int maxVersion = 0;

    public GlBase(String variantName) {
        this.variantName = variantName;
    }

    public ProgramInterfaceQuery getProgramInterfaceQuery() {
        return piqImpl;
    }

    public TexImageMultisample optTexImageMultisample() {
        return timImpl;
    }

    protected abstract void initializeImpl();

    public void initialize() {
        if (!initialized) {
            System.out.println("Context: " + glfwGetCurrentContext());
            initializeImpl();
            version = glGetString(GL_VERSION);
            shaderVersion = glGetString(GL_SHADING_LANGUAGE_VERSION);
            vendor = glGetString(GL_VENDOR);
            renderer = glGetString(GL_RENDERER);

            System.out.printf("%s %d%n%s, shader language version %s, vendor %s, renderer %s%n", variantName,
                    maxVersion,
                    version, shaderVersion, vendor, renderer);
            
            System.out.println("Supports " + Stream.of(piqImpl, timImpl).filter(n -> n != null)
                    .map(n -> n.name()).collect(Collectors.joining(", ")));
            // printExtensions();
            initialized = true;
        }
    }

    public void printExtensions() {
        List<String> l = new ArrayList<>();
        int n = glGetInteger(GL_NUM_EXTENSIONS);
        for (int i = 0; i < n; i++) {
            l.add(glGetStringi(GL_EXTENSIONS, i));
        }
        System.out.println(l);
    }
}
