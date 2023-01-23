package turtleduck.gl.compat;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.system.MemoryUtil;

public interface GLA extends Gles30, GLConstants {
    static GLAState GLA_STATE = new GLAState();
    static String PLATFORM = GLAState.decodePlatform(glfwGetPlatform());

    public static void initialize() {

    }

    public static GLASetup setup() {
        return new GLASetup();
    }

    default boolean isOpenGL() {
        return GLA_STATE.api == GLFW_OPENGL_API;
    }

    default boolean isOpenGLES() {
        return GLA_STATE.api == GLFW_OPENGL_ES_API;
    }

    public static GLA get() {
        if (GLA_STATE.api == GLFW_OPENGL_ES_API)
            return GlesImpl.create();
        else
            return GlImpl.create();
    }

    static class GLASetup {
        private int api = GLFW_OPENGL_API;
        private int apiMajor = 3;
        private int apiMinor = 0;
        private int forwardCompat = GLFW_TRUE;
        private int profile = GLFW_OPENGL_ANY_PROFILE;
        private int doubleBuffer = GLFW_TRUE;
        private int srgbCapable = GLFW_TRUE;

        public GLASetup apiGL() {
            api = GLFW_OPENGL_API;
            return this;
        }

        public GLASetup apiGLES() {
            api = GLFW_OPENGL_ES_API;
            if(apiMajor < 2)
                apiMajor = 3;
            return this;
        }

        /**
         * Default is true.
         * 
         * Always true on Mac.
         * 
         * @param enable
         * @return this
         */
        public GLASetup forwardCompat(boolean enable) {
            forwardCompat = enable ? GLFW_TRUE : GLFW_FALSE;
            return this;
        }

        /**
         * Default is any profile.
         * 
         * No effect on Mac, which always uses core profile
         * 
         * @return this
         */
        public GLASetup core() {
            profile = GLFW_OPENGL_CORE_PROFILE;
            return this;
        }

        /**
         * Default is any profile.
         * 
         * No effect on Mac, which always uses core profile
         * 
         * @return this
         */
        public GLASetup compat() {
            profile = GLFW_OPENGL_COMPAT_PROFILE;
            return this;
        }

        /**
         * @param majorVersion Minimum major API version, at least 3 (Mac supports
         *                     3.2–4.2 only)
         * @return this
         */
        public GLASetup major(int majorVersion) {
            if (majorVersion < 3) {
                throw new IllegalArgumentException("Only OpenGL (ES) version 3.0 or later is supported");
            }
            apiMajor = majorVersion;
            return this;
        }

        /**
         * @param majorVersion Minimum minor API version(Mac supports 3.2–4.2 only)
         * @return this
         */
        public GLASetup minor(int minorVersion) {
            apiMinor = minorVersion;
            return this;
        }

        /**
         * Turns off double buffering for the framebuffer
         * 
         * You probably don't want this
         * 
         * @return this
         */
        public GLASetup singleBuffer() {
            doubleBuffer = GLFW_FALSE;
            return this;
        }

        /**
         * Whether the framebuffer should be sRGB capable.
         * 
         * True by default. If enabled, it will always be on in OpenGL ES, and it can be
         * turned on in OpenGL using GL_FRAMEBUFFER_SRGB
         * 
         * @param enable
         * @return this
         */
        public GLASetup srgbCapable(boolean enable) {
            srgbCapable = enable ? GLFW_TRUE : GLFW_FALSE;
            return this;
        }

        public void initialize() {
            glfwSetErrorCallback(GLA_STATE::callbackGlfwError);
            if (!glfwInit()) {
                if (GLA_STATE.exception != null) {
                    RuntimeException ex = GLA_STATE.exception;
                    ex.printStackTrace();
                    GLA_STATE.exception = null;
                    throw ex;
                } else {
                    throw new RuntimeException("Failed to initialize OpenGL");
                }
            }
            glfwDefaultWindowHints();

            if (glfwGetPlatform() == GLFW_PLATFORM_COCOA) {
                if (apiMajor == 3 && apiMinor < 2) {
                    apiMinor = 2;
                    // TODO: log
                }
                forwardCompat = GLFW_TRUE;
                profile = GLFW_OPENGL_CORE_PROFILE;
                // full resolution on Mac
                glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE);
            }
            GLA_STATE.api = api;
            GLA_STATE.apiMajor = apiMajor;
            GLA_STATE.apiMinor = apiMinor;
            glfwWindowHint(GLFW_CLIENT_API, api);
            
            if (apiMajor > 0) {
                glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, apiMajor);
                glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, apiMinor);
            }
            if (api == GLFW_OPENGL_API) {
                glfwWindowHint(GLFW_OPENGL_PROFILE, profile);
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, forwardCompat);
            }
            glfwWindowHint(GLFW_DOUBLEBUFFER, doubleBuffer);
            glfwWindowHint(GLFW_SRGB_CAPABLE, srgbCapable);
        }
    }

    default void checkError() {
        if (GLA_STATE.exception != null) {
            RuntimeException ex = GLA_STATE.exception;
            GLA_STATE.exception = null;
            throw ex;
        }
    }

    static class GLAState {
        RuntimeException exception;
        int api, apiMajor, apiMinor;

        public void callbackGlfwError(int error, long description) {
            String desc = MemoryUtil.memUTF8(description);
            exception = new RuntimeException("GLFW error " + error + ": " + desc);
            exception.printStackTrace();
        }

        public String platform() {
            return decodePlatform(glfwGetPlatform());
        }

        public String glName() {
            if (api == GLFW_OPENGL_API)
                return "OpenGL";
            else if (api == GLFW_OPENGL_ES_API)
                return "OpenGL ES";
            else
                return "unknown";
        }

        private static String decodePlatform(int platform) {
            switch (platform) {
                case GLFW_PLATFORM_X11:
                    return "X11";
                case GLFW_PLATFORM_COCOA:
                    return "Cocoa";
                case GLFW_PLATFORM_WAYLAND:
                    return "Wayland";
                case GLFW_PLATFORM_WIN32:
                    return "Windows";
                case GLFW_PLATFORM_ERROR:
                    return "ERROR";
                default:
                    return "unknown";
            }
        }

    }
}
