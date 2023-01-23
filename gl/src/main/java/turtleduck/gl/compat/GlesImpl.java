package turtleduck.gl.compat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengles.GLES31;
import org.lwjgl.opengles.GLES32;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.opengles.NVPolygonMode;
import org.lwjgl.system.Callback;

import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES30;

class GlesImpl extends GlBase {
    private static GlesImpl instance;

    public GlesImpl() {
        super("OpenGL ES");
    }

    public static GLA create() {
        if (instance == null) {

            instance = new GlesImpl();
            instance.initialize();
        }
        return instance;
    }

    private GLESCapabilities caps;

    protected void initializeImpl() {
        caps = GLES.createCapabilities();

        if (caps.glGetProgramInterfaceiv != 0) {
            piqImpl = new PIQImpl();
        }
        if (caps.GLES32)
            maxVersion = 320;
        else if (caps.GLES31)
            maxVersion = 310;
        else if (caps.GLES30)
            maxVersion = 300;
        else if (caps.GLES20)
            maxVersion = 200;
    }

    @Override
    public Callback enableDebug() {
        if (caps.glDebugMessageCallback != 0) {
            System.out.println("Filtering debug messages");
            GLES32.glDebugMessageControl(GL_DONT_CARE, GLES32.GL_DEBUG_TYPE_OTHER,
                    GLES32.GL_DEBUG_SEVERITY_NOTIFICATION,
                    new int[0], false);
            glEnable(GLES32.GL_DEBUG_OUTPUT);
        } else {
            System.out.println("Debug output not supported");
        }
        return null;
    }

    /**
     * @return GL_FRONT_LEFT (OpenGL) or GL_FRONT (OpenGL ES)
     */
    public int GL_FRONT_FB() {
        return GLES30.GL_FRONT;
    }

    /**
     * @return GL_BACK_LEFT (OpenGL) or GL_BACK (OpenGL ES)
     */
    public int GL_BACK_FB() {
        return GLES30.GL_BACK;
    }

    public boolean wireframe(boolean enable) {
        if (caps.GL_NV_polygon_mode) {
            NVPolygonMode.glPolygonModeNV(GL_FRONT_AND_BACK,
                    enable ? NVPolygonMode.GL_LINE_NV : NVPolygonMode.GL_FILL_NV);
            return true;
        } else {
            return !enable;
        }
    }

    static class PIQImpl implements ProgramInterfaceQuery {

        public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
            GLES31.glGetProgramInterfaceiv(program, programInterface, pname, params);
        }

        public int glGetProgramInterfacei(int program, int programInterface, int pname) {
            return GLES31.glGetProgramInterfacei(program, programInterface, pname);
        }

        public int glGetProgramResourceIndex(int program, int programInterface, ByteBuffer name) {
            return GLES31.glGetProgramResourceIndex(program, programInterface, name);
        }

        public int glGetProgramResourceIndex(int program, int programInterface, CharSequence name) {
            return GLES31.glGetProgramResourceIndex(program, programInterface, name);
        }

        public void glGetProgramResourceName(int program, int programInterface, int index, IntBuffer length,
                ByteBuffer name) {
            GLES31.glGetProgramResourceName(program, programInterface, index, length, name);
        }

        public String glGetProgramResourceName(int program, int programInterface, int index, int bufSize) {
            return GLES31.glGetProgramResourceName(program, programInterface, index, bufSize);
        }

        public String glGetProgramResourceName(int program, int programInterface, int index) {
            return GLES31.glGetProgramResourceName(program, programInterface, index);
        }

        public void glGetProgramResourceiv(int program, int programInterface, int index, IntBuffer props,
                IntBuffer length, IntBuffer params) {
            GLES31.glGetProgramResourceiv(program, programInterface, index, props, length, params);
        }

        public int glGetProgramResourceLocation(int program, int programInterface, ByteBuffer name) {
            return GLES31.glGetProgramResourceLocation(program, programInterface, name);
        }

        public int glGetProgramResourceLocation(int program, int programInterface, CharSequence name) {
            return GLES31.glGetProgramResourceLocation(program, programInterface, name);
        }
//
//        public int glGetProgramResourceLocationIndex(int program, int programInterface, ByteBuffer name) {
//            return GLES31.glGetProgramResourceLocationIndex(program, programInterface, name);
//        }
//
//        public int glGetProgramResourceLocationIndex(int program, int programInterface, CharSequence name) {
//            return GLES31.glGetProgramResourceLocationIndex(program, programInterface, name);
//        }

        public void glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params) {
            GLES31.glGetProgramInterfaceiv(program, programInterface, pname, params);
        }

        public void glGetProgramResourceName(int program, int programInterface, int index, int[] length,
                ByteBuffer name) {
            GLES31.glGetProgramResourceName(program, programInterface, index, length, name);
        }

        public void glGetProgramResourceiv(int program, int programInterface, int index, int[] props, int[] length,
                int[] params) {
            GLES31.glGetProgramResourceiv(program, programInterface, index, props, length, params);
        }
    }

    public void glActiveTexture(int texture) {
        GLES30.glActiveTexture(texture);
    }

    public void glAttachShader(int program, int shader) {
        GLES30.glAttachShader(program, shader);
    }

    public void glBindAttribLocation(int program, int index, ByteBuffer name) {
        GLES30.glBindAttribLocation(program, index, name);
    }

    public void glBindAttribLocation(int program, int index, CharSequence name) {
        GLES30.glBindAttribLocation(program, index, name);
    }

    public void glBindBuffer(int target, int buffer) {
        GLES30.glBindBuffer(target, buffer);
    }

    public void glBindFramebuffer(int target, int framebuffer) {
        GLES30.glBindFramebuffer(target, framebuffer);
    }

    public void glBindRenderbuffer(int target, int renderbuffer) {
        GLES30.glBindRenderbuffer(target, renderbuffer);
    }

    public void glBindTexture(int target, int texture) {
        GLES30.glBindTexture(target, texture);
    }

    public void glBlendColor(float red, float green, float blue, float alpha) {
        GLES30.glBlendColor(red, green, blue, alpha);
    }

    public void glBlendEquation(int mode) {
        GLES30.glBlendEquation(mode);
    }

    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        GLES30.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        GLES30.glBlendFunc(sfactor, dfactor);
    }

    public void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GLES30.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    public void glBufferData(int target, long size, int usage) {
        GLES30.glBufferData(target, size, usage);
    }

    public void glBufferData(int target, ByteBuffer data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, ShortBuffer data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, IntBuffer data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, FloatBuffer data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferSubData(int target, long offset, ByteBuffer data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, ShortBuffer data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, IntBuffer data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, FloatBuffer data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public int glCheckFramebufferStatus(int target) {
        return GLES30.glCheckFramebufferStatus(target);
    }

    public void glClear(int mask) {
        GLES30.glClear(mask);
    }

    public void glClearColor(float red, float green, float blue, float alpha) {
        GLES30.glClearColor(red, green, blue, alpha);
    }

    public void glClearDepthf(float d) {
        GLES30.glClearDepthf(d);
    }

    public void glClearStencil(int s) {
        GLES30.glClearStencil(s);
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GLES30.glColorMask(red, green, blue, alpha);
    }

    public void glCompileShader(int shader) {
        GLES30.glCompileShader(shader);
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int imageSize, long data) {
        GLES30.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            ByteBuffer data) {
        GLES30.glCompressedTexImage2D(target, level, internalformat, width, height, border, data);
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int imageSize, long data) {
        GLES30.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, ByteBuffer data) {
        GLES30.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, data);
    }

    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
            int border) {
        GLES30.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
            int height) {
        GLES30.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    public int glCreateProgram() {
        return GLES30.glCreateProgram();
    }

    public int glCreateShader(int type) {
        return GLES30.glCreateShader(type);
    }

    public void glCullFace(int mode) {
        GLES30.glCullFace(mode);
    }

    public void glDeleteBuffers(IntBuffer buffers) {
        GLES30.glDeleteBuffers(buffers);
    }

    public void glDeleteBuffers(int buffer) {
        GLES30.glDeleteBuffers(buffer);
    }

    public void glDeleteFramebuffers(IntBuffer framebuffers) {
        GLES30.glDeleteFramebuffers(framebuffers);
    }

    public void glDeleteFramebuffers(int framebuffer) {
        GLES30.glDeleteFramebuffers(framebuffer);
    }

    public void glDeleteProgram(int program) {
        GLES30.glDeleteProgram(program);
    }

    public void glDeleteRenderbuffers(IntBuffer renderbuffers) {
        GLES30.glDeleteRenderbuffers(renderbuffers);
    }

    public void glDeleteRenderbuffers(int renderbuffer) {
        GLES30.glDeleteRenderbuffers(renderbuffer);
    }

    public void glDeleteShader(int shader) {
        GLES30.glDeleteShader(shader);
    }

    public void glDeleteTextures(IntBuffer textures) {
        GLES30.glDeleteTextures(textures);
    }

    public void glDeleteTextures(int texture) {
        GLES30.glDeleteTextures(texture);
    }

    public void glDepthFunc(int func) {
        GLES30.glDepthFunc(func);
    }

    public void glDepthMask(boolean flag) {
        GLES30.glDepthMask(flag);
    }

    public void glDepthRangef(float n, float f) {
        GLES30.glDepthRangef(n, f);
    }

    public void glDetachShader(int program, int shader) {
        GLES30.glDetachShader(program, shader);
    }

    public void glDisable(int cap) {
        GLES30.glDisable(cap);
    }

    public void glDisableVertexAttribArray(int index) {
        GLES30.glDisableVertexAttribArray(index);
    }

    public void glDrawArrays(int mode, int first, int count) {
        GLES30.glDrawArrays(mode, first, count);
    }

    public void glDrawElements(int mode, int count, int type, long indices) {
        GLES30.glDrawElements(mode, count, type, indices);
    }

    public void glDrawElements(int mode, int type, ByteBuffer indices) {
        GLES30.glDrawElements(mode, type, indices);
    }

    public void glDrawElements(int mode, ByteBuffer indices) {
        GLES30.glDrawElements(mode, indices);
    }

    public void glDrawElements(int mode, ShortBuffer indices) {
        GLES30.glDrawElements(mode, indices);
    }

    public void glDrawElements(int mode, IntBuffer indices) {
        GLES30.glDrawElements(mode, indices);
    }

    public void glEnable(int cap) {
        GLES30.glEnable(cap);
    }

    public void glEnableVertexAttribArray(int index) {
        GLES30.glEnableVertexAttribArray(index);
    }

    public void glFinish() {
        GLES30.glFinish();
    }

    public void glFlush() {
        GLES30.glFlush();
    }

    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GLES30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public void glFrontFace(int mode) {
        GLES30.glFrontFace(mode);
    }

    public void glGenBuffers(IntBuffer buffers) {
        GLES30.glGenBuffers(buffers);
    }

    public int glGenBuffers() {
        return GLES30.glGenBuffers();
    }

    public void glGenerateMipmap(int target) {
        GLES30.glGenerateMipmap(target);
    }

    public void glGenFramebuffers(IntBuffer framebuffers) {
        GLES30.glGenFramebuffers(framebuffers);
    }

    public int glGenFramebuffers() {
        return GLES30.glGenFramebuffers();
    }

    public void glGenRenderbuffers(IntBuffer renderbuffers) {
        GLES30.glGenRenderbuffers(renderbuffers);
    }

    public int glGenRenderbuffers() {
        return GLES30.glGenRenderbuffers();
    }

    public void glGenTextures(IntBuffer textures) {
        GLES30.glGenTextures(textures);
    }

    public int glGenTextures() {
        return GLES30.glGenTextures();
    }

    public void glGetActiveAttrib(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GLES30.glGetActiveAttrib(program, index, length, size, type, name);
    }

    public String glGetActiveAttrib(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GLES30.glGetActiveAttrib(program, index, bufSize, size, type);
    }

    public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        return GLES30.glGetActiveAttrib(program, index, size, type);
    }

    public void glGetActiveUniform(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GLES30.glGetActiveUniform(program, index, length, size, type, name);
    }

    public String glGetActiveUniform(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GLES30.glGetActiveUniform(program, index, bufSize, size, type);
    }

    public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        return GLES30.glGetActiveUniform(program, index, size, type);
    }

    public void glGetAttachedShaders(int program, IntBuffer count, IntBuffer shaders) {
        GLES30.glGetAttachedShaders(program, count, shaders);
    }

    public int glGetAttribLocation(int program, ByteBuffer name) {
        return GLES30.glGetAttribLocation(program, name);
    }

    public int glGetAttribLocation(int program, CharSequence name) {
        return GLES30.glGetAttribLocation(program, name);
    }

    public void glGetBooleanv(int pname, ByteBuffer data) {
        GLES30.glGetBooleanv(pname, data);
    }

    public boolean glGetBoolean(int pname) {
        return GLES30.glGetBoolean(pname);
    }

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        GLES30.glGetBufferParameteriv(target, pname, params);
    }

    public int glGetBufferParameteri(int target, int pname) {
        return GLES30.glGetBufferParameteri(target, pname);
    }

    public int glGetError() {
        return GLES30.glGetError();
    }

    public void glGetFloatv(int pname, FloatBuffer data) {
        GLES30.glGetFloatv(pname, data);
    }

    public float glGetFloat(int pname) {
        return GLES30.glGetFloat(pname);
    }

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        GLES30.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    public int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname) {
        return GLES30.glGetFramebufferAttachmentParameteri(target, attachment, pname);
    }

    public void glGetIntegerv(int pname, IntBuffer data) {
        GLES30.glGetIntegerv(pname, data);
    }

    public int glGetInteger(int pname) {
        return GLES30.glGetInteger(pname);
    }

    public void glGetProgramiv(int program, int pname, IntBuffer params) {
        GLES30.glGetProgramiv(program, pname, params);
    }

    public int glGetProgrami(int program, int pname) {
        return GLES30.glGetProgrami(program, pname);
    }

    public void glGetProgramInfoLog(int program, IntBuffer length, ByteBuffer infoLog) {
        GLES30.glGetProgramInfoLog(program, length, infoLog);
    }

    public String glGetProgramInfoLog(int program, int bufSize) {
        return GLES30.glGetProgramInfoLog(program, bufSize);
    }

    public String glGetProgramInfoLog(int program) {
        return GLES30.glGetProgramInfoLog(program);
    }

    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        GLES30.glGetRenderbufferParameteriv(target, pname, params);
    }

    public int glGetRenderbufferParameteri(int target, int pname) {
        return GLES30.glGetRenderbufferParameteri(target, pname);
    }

    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        GLES30.glGetShaderiv(shader, pname, params);
    }

    public int glGetShaderi(int shader, int pname) {
        return GLES30.glGetShaderi(shader, pname);
    }

    public void glGetShaderInfoLog(int shader, IntBuffer length, ByteBuffer infoLog) {
        GLES30.glGetShaderInfoLog(shader, length, infoLog);
    }

    public String glGetShaderInfoLog(int shader, int bufSize) {
        return GLES30.glGetShaderInfoLog(shader, bufSize);
    }

    public String glGetShaderInfoLog(int shader) {
        return GLES30.glGetShaderInfoLog(shader);
    }

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
        GLES30.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public void glGetShaderSource(int shader, IntBuffer length, ByteBuffer source) {
        GLES30.glGetShaderSource(shader, length, source);
    }

    public String glGetShaderSource(int shader, int bufSize) {
        return GLES30.glGetShaderSource(shader, bufSize);
    }

    public String glGetShaderSource(int shader) {
        return GLES30.glGetShaderSource(shader);
    }

    public String glGetString(int name) {
        return GLES30.glGetString(name);
    }

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        GLES30.glGetTexParameterfv(target, pname, params);
    }

    public float glGetTexParameterf(int target, int pname) {
        return GLES30.glGetTexParameterf(target, pname);
    }

    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        GLES30.glGetTexParameteriv(target, pname, params);
    }

    public int glGetTexParameteri(int target, int pname) {
        return GLES30.glGetTexParameteri(target, pname);
    }

    public void glGetUniformfv(int program, int location, FloatBuffer params) {
        GLES30.glGetUniformfv(program, location, params);
    }

    public float glGetUniformf(int program, int location) {
        return GLES30.glGetUniformf(program, location);
    }

    public void glGetUniformiv(int program, int location, IntBuffer params) {
        GLES30.glGetUniformiv(program, location, params);
    }

    public int glGetUniformi(int program, int location) {
        return GLES30.glGetUniformi(program, location);
    }

    public int glGetUniformLocation(int program, ByteBuffer name) {
        return GLES30.glGetUniformLocation(program, name);
    }

    public int glGetUniformLocation(int program, CharSequence name) {
        return GLES30.glGetUniformLocation(program, name);
    }

    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
        GLES30.glGetVertexAttribfv(index, pname, params);
    }

    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
        GLES30.glGetVertexAttribiv(index, pname, params);
    }

    public void glGetVertexAttribPointerv(int index, int pname, PointerBuffer pointer) {
        GLES30.glGetVertexAttribPointerv(index, pname, pointer);
    }

    public long glGetVertexAttribPointer(int index, int pname) {
        return GLES30.glGetVertexAttribPointer(index, pname);
    }

    public void glHint(int target, int mode) {
        GLES30.glHint(target, mode);
    }

    public boolean glIsBuffer(int buffer) {
        return GLES30.glIsBuffer(buffer);
    }

    public boolean glIsEnabled(int cap) {
        return GLES30.glIsEnabled(cap);
    }

    public boolean glIsFramebuffer(int framebuffer) {
        return GLES30.glIsFramebuffer(framebuffer);
    }

    public boolean glIsProgram(int program) {
        return GLES30.glIsProgram(program);
    }

    public boolean glIsRenderbuffer(int renderbuffer) {
        return GLES30.glIsRenderbuffer(renderbuffer);
    }

    public boolean glIsShader(int shader) {
        return GLES30.glIsShader(shader);
    }

    public boolean glIsTexture(int texture) {
        return GLES30.glIsTexture(texture);
    }

    public void glLineWidth(float width) {
        GLES30.glLineWidth(width);
    }

    public void glLinkProgram(int program) {
        GLES30.glLinkProgram(program);
    }

    public void glPixelStorei(int pname, int param) {
        GLES30.glPixelStorei(pname, param);
    }

    public void glPolygonOffset(float factor, float units) {
        GLES30.glPolygonOffset(factor, units);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReleaseShaderCompiler() {
        GLES30.glReleaseShaderCompiler();
    }

    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        GLES30.glRenderbufferStorage(target, internalformat, width, height);
    }

    public void glSampleCoverage(float value, boolean invert) {
        GLES30.glSampleCoverage(value, invert);
    }

    public void glScissor(int x, int y, int width, int height) {
        GLES30.glScissor(x, y, width, height);
    }

    public void glShaderBinary(IntBuffer shaders, int binaryformat, ByteBuffer binary) {
        GLES30.glShaderBinary(shaders, binaryformat, binary);
    }

    public void glShaderSource(int shader, PointerBuffer string, IntBuffer length) {
        GLES30.glShaderSource(shader, string, length);
    }

    public void glShaderSource(int shader, CharSequence... string) {
        GLES30.glShaderSource(shader, string);
    }

    public void glShaderSource(int shader, CharSequence string) {
        GLES30.glShaderSource(shader, string);
    }

    public void glStencilFunc(int func, int ref, int mask) {
        GLES30.glStencilFunc(func, ref, mask);
    }

    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        GLES30.glStencilFuncSeparate(face, func, ref, mask);
    }

    public void glStencilMask(int mask) {
        GLES30.glStencilMask(mask);
    }

    public void glStencilMaskSeparate(int face, int mask) {
        GLES30.glStencilMaskSeparate(face, mask);
    }

    public void glStencilOp(int fail, int zfail, int zpass) {
        GLES30.glStencilOp(fail, zfail, zpass);
    }

    public void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        GLES30.glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ByteBuffer pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, long pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ShortBuffer pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, IntBuffer pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, FloatBuffer pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexParameterf(int target, int pname, float param) {
        GLES30.glTexParameterf(target, pname, param);
    }

    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        GLES30.glTexParameterfv(target, pname, params);
    }

    public void glTexParameteri(int target, int pname, int param) {
        GLES30.glTexParameteri(target, pname, param);
    }

    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        GLES30.glTexParameteriv(target, pname, params);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, ByteBuffer pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, long pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, ShortBuffer pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, IntBuffer pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, FloatBuffer pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glUniform1f(int location, float v0) {
        GLES30.glUniform1f(location, v0);
    }

    public void glUniform1fv(int location, FloatBuffer value) {
        GLES30.glUniform1fv(location, value);
    }

    public void glUniform1i(int location, int v0) {
        GLES30.glUniform1i(location, v0);
    }

    public void glUniform1iv(int location, IntBuffer value) {
        GLES30.glUniform1iv(location, value);
    }

    public void glUniform2f(int location, float v0, float v1) {
        GLES30.glUniform2f(location, v0, v1);
    }

    public void glUniform2fv(int location, FloatBuffer value) {
        GLES30.glUniform2fv(location, value);
    }

    public void glUniform2i(int location, int v0, int v1) {
        GLES30.glUniform2i(location, v0, v1);
    }

    public void glUniform2iv(int location, IntBuffer value) {
        GLES30.glUniform2iv(location, value);
    }

    public void glUniform3f(int location, float v0, float v1, float v2) {
        GLES30.glUniform3f(location, v0, v1, v2);
    }

    public void glUniform3fv(int location, FloatBuffer value) {
        GLES30.glUniform3fv(location, value);
    }

    public void glUniform3i(int location, int v0, int v1, int v2) {
        GLES30.glUniform3i(location, v0, v1, v2);
    }

    public void glUniform3iv(int location, IntBuffer value) {
        GLES30.glUniform3iv(location, value);
    }

    public void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        GLES30.glUniform4f(location, v0, v1, v2, v3);
    }

    public void glUniform4fv(int location, FloatBuffer value) {
        GLES30.glUniform4fv(location, value);
    }

    public void glUniform4i(int location, int v0, int v1, int v2, int v3) {
        GLES30.glUniform4i(location, v0, v1, v2, v3);
    }

    public void glUniform4iv(int location, IntBuffer value) {
        GLES30.glUniform4iv(location, value);
    }

    public void glUniformMatrix2fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix2fv(location, transpose, value);
    }

    public void glUniformMatrix3fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix3fv(location, transpose, value);
    }

    public void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix4fv(location, transpose, value);
    }

    public void glUseProgram(int program) {
        GLES30.glUseProgram(program);
    }

    public void glValidateProgram(int program) {
        GLES30.glValidateProgram(program);
    }

    public void glVertexAttrib1f(int index, float x) {
        GLES30.glVertexAttrib1f(index, x);
    }

    public void glVertexAttrib1fv(int index, FloatBuffer v) {
        GLES30.glVertexAttrib1fv(index, v);
    }

    public void glVertexAttrib2f(int index, float x, float y) {
        GLES30.glVertexAttrib2f(index, x, y);
    }

    public void glVertexAttrib2fv(int index, FloatBuffer v) {
        GLES30.glVertexAttrib2fv(index, v);
    }

    public void glVertexAttrib3f(int index, float x, float y, float z) {
        GLES30.glVertexAttrib3f(index, x, y, z);
    }

    public void glVertexAttrib3fv(int index, FloatBuffer v) {
        GLES30.glVertexAttrib3fv(index, v);
    }

    public void glVertexAttrib4f(int index, float x, float y, float z, float w) {
        GLES30.glVertexAttrib4f(index, x, y, z, w);
    }

    public void glVertexAttrib4fv(int index, FloatBuffer v) {
        GLES30.glVertexAttrib4fv(index, v);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            ByteBuffer pointer) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            ShortBuffer pointer) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            IntBuffer pointer) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            FloatBuffer pointer) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glViewport(int x, int y, int width, int height) {
        GLES30.glViewport(x, y, width, height);
    }

    public void glBufferData(int target, short[] data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, int[] data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, float[] data, int usage) {
        GLES30.glBufferData(target, data, usage);
    }

    public void glBufferSubData(int target, long offset, short[] data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, int[] data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, float[] data) {
        GLES30.glBufferSubData(target, offset, data);
    }

    public void glDeleteBuffers(int[] buffers) {
        GLES30.glDeleteBuffers(buffers);
    }

    public void glDeleteFramebuffers(int[] framebuffers) {
        GLES30.glDeleteFramebuffers(framebuffers);
    }

    public void glDeleteRenderbuffers(int[] renderbuffers) {
        GLES30.glDeleteRenderbuffers(renderbuffers);
    }

    public void glDeleteTextures(int[] textures) {
        GLES30.glDeleteTextures(textures);
    }

    public void glGenBuffers(int[] buffers) {
        GLES30.glGenBuffers(buffers);
    }

    public void glGenFramebuffers(int[] framebuffers) {
        GLES30.glGenFramebuffers(framebuffers);
    }

    public void glGenRenderbuffers(int[] renderbuffers) {
        GLES30.glGenRenderbuffers(renderbuffers);
    }

    public void glGenTextures(int[] textures) {
        GLES30.glGenTextures(textures);
    }

    public void glGetActiveAttrib(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name) {
        GLES30.glGetActiveAttrib(program, index, length, size, type, name);
    }

    public void glGetActiveUniform(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name) {
        GLES30.glGetActiveUniform(program, index, length, size, type, name);
    }

    public void glGetAttachedShaders(int program, int[] count, int[] shaders) {
        GLES30.glGetAttachedShaders(program, count, shaders);
    }

    public void glGetBufferParameteriv(int target, int pname, int[] params) {
        GLES30.glGetBufferParameteriv(target, pname, params);
    }

    public void glGetFloatv(int pname, float[] data) {
        GLES30.glGetFloatv(pname, data);
    }

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params) {
        GLES30.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    public void glGetIntegerv(int pname, int[] data) {
        GLES30.glGetIntegerv(pname, data);
    }

    public void glGetProgramiv(int program, int pname, int[] params) {
        GLES30.glGetProgramiv(program, pname, params);
    }

    public void glGetProgramInfoLog(int program, int[] length, ByteBuffer infoLog) {
        GLES30.glGetProgramInfoLog(program, length, infoLog);
    }

    public void glGetRenderbufferParameteriv(int target, int pname, int[] params) {
        GLES30.glGetRenderbufferParameteriv(target, pname, params);
    }

    public void glGetShaderiv(int shader, int pname, int[] params) {
        GLES30.glGetShaderiv(shader, pname, params);
    }

    public void glGetShaderInfoLog(int shader, int[] length, ByteBuffer infoLog) {
        GLES30.glGetShaderInfoLog(shader, length, infoLog);
    }

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int[] precision) {
        GLES30.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public void glGetShaderSource(int shader, int[] length, ByteBuffer source) {
        GLES30.glGetShaderSource(shader, length, source);
    }

    public void glGetTexParameterfv(int target, int pname, float[] params) {
        GLES30.glGetTexParameterfv(target, pname, params);
    }

    public void glGetTexParameteriv(int target, int pname, int[] params) {
        GLES30.glGetTexParameteriv(target, pname, params);
    }

    public void glGetUniformfv(int program, int location, float[] params) {
        GLES30.glGetUniformfv(program, location, params);
    }

    public void glGetUniformiv(int program, int location, int[] params) {
        GLES30.glGetUniformiv(program, location, params);
    }

    public void glGetVertexAttribfv(int index, int pname, float[] params) {
        GLES30.glGetVertexAttribfv(index, pname, params);
    }

    public void glGetVertexAttribiv(int index, int pname, int[] params) {
        GLES30.glGetVertexAttribiv(index, pname, params);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, short[] pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, int[] pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glShaderBinary(int[] shaders, int binaryformat, ByteBuffer binary) {
        GLES30.glShaderBinary(shaders, binaryformat, binary);
    }

    public void glShaderSource(int shader, PointerBuffer string, int[] length) {
        GLES30.glShaderSource(shader, string, length);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, short[] pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, int[] pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, float[] pixels) {
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexParameterfv(int target, int pname, float[] params) {
        GLES30.glTexParameterfv(target, pname, params);
    }

    public void glTexParameteriv(int target, int pname, int[] params) {
        GLES30.glTexParameteriv(target, pname, params);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, short[] pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, int[] pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, float[] pixels) {
        GLES30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glUniform1fv(int location, float[] value) {
        GLES30.glUniform1fv(location, value);
    }

    public void glUniform1iv(int location, int[] value) {
        GLES30.glUniform1iv(location, value);
    }

    public void glUniform2fv(int location, float[] value) {
        GLES30.glUniform2fv(location, value);
    }

    public void glUniform2iv(int location, int[] value) {
        GLES30.glUniform2iv(location, value);
    }

    public void glUniform3fv(int location, float[] value) {
        GLES30.glUniform3fv(location, value);
    }

    public void glUniform3iv(int location, int[] value) {
        GLES30.glUniform3iv(location, value);
    }

    public void glUniform4fv(int location, float[] value) {
        GLES30.glUniform4fv(location, value);
    }

    public void glUniform4iv(int location, int[] value) {
        GLES30.glUniform4iv(location, value);
    }

    public void glUniformMatrix2fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix2fv(location, transpose, value);
    }

    public void glUniformMatrix3fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix3fv(location, transpose, value);
    }

    public void glUniformMatrix4fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix4fv(location, transpose, value);
    }

    public void glVertexAttrib1fv(int index, float[] v) {
        GLES30.glVertexAttrib1fv(index, v);
    }

    public void glVertexAttrib2fv(int index, float[] v) {
        GLES30.glVertexAttrib2fv(index, v);
    }

    public void glVertexAttrib3fv(int index, float[] v) {
        GLES30.glVertexAttrib3fv(index, v);
    }

    public void glVertexAttrib4fv(int index, float[] v) {
        GLES30.glVertexAttrib4fv(index, v);
    }

    public void glReadBuffer(int src) {
        GLES30.glReadBuffer(src);
    }

    public void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices) {
        GLES30.glDrawRangeElements(mode, start, end, count, type, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, int type, ByteBuffer indices) {
        GLES30.glDrawRangeElements(mode, start, end, type, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, ByteBuffer indices) {
        GLES30.glDrawRangeElements(mode, start, end, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, ShortBuffer indices) {
        GLES30.glDrawRangeElements(mode, start, end, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, IntBuffer indices) {
        GLES30.glDrawRangeElements(mode, start, end, indices);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ByteBuffer pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, long pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ShortBuffer pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, IntBuffer pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, FloatBuffer pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, ByteBuffer pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, long pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, ShortBuffer pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, IntBuffer pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, FloatBuffer pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
            int width, int height) {
        GLES30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
            int border, int imageSize, long data) {
        GLES30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, data);
    }

    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
            int border, ByteBuffer data) {
        GLES30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, data);
    }

    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int imageSize, long data) {
        GLES30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                imageSize, data);
    }

    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, ByteBuffer data) {
        GLES30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data);
    }

    public void glGenQueries(IntBuffer ids) {
        GLES30.glGenQueries(ids);
    }

    public int glGenQueries() {
        return GLES30.glGenQueries();
    }

    public void glDeleteQueries(IntBuffer ids) {
        GLES30.glDeleteQueries(ids);
    }

    public void glDeleteQueries(int id) {
        GLES30.glDeleteQueries(id);
    }

    public boolean glIsQuery(int id) {
        return GLES30.glIsQuery(id);
    }

    public void glBeginQuery(int target, int id) {
        GLES30.glBeginQuery(target, id);
    }

    public void glEndQuery(int target) {
        GLES30.glEndQuery(target);
    }

    public void glGetQueryiv(int target, int pname, IntBuffer params) {
        GLES30.glGetQueryiv(target, pname, params);
    }

    public int glGetQueryi(int target, int pname) {
        return GLES30.glGetQueryi(target, pname);
    }

    public void glGetQueryObjectuiv(int id, int pname, IntBuffer params) {
        GLES30.glGetQueryObjectuiv(id, pname, params);
    }

    public int glGetQueryObjectui(int id, int pname) {
        return GLES30.glGetQueryObjectui(id, pname);
    }

    public boolean glUnmapBuffer(int target) {
        return GLES30.glUnmapBuffer(target);
    }

    public void glGetBufferPointerv(int target, int pname, PointerBuffer params) {
        GLES30.glGetBufferPointerv(target, pname, params);
    }

    public long glGetBufferPointer(int target, int pname) {
        return GLES30.glGetBufferPointer(target, pname);
    }

    public void glDrawBuffers(IntBuffer bufs) {
        GLES30.glDrawBuffers(bufs);
    }

    public void glDrawBuffers(int buf) {
        GLES30.glDrawBuffers(buf);
    }

    public void glUniformMatrix2x3fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix2x3fv(location, transpose, value);
    }

    public void glUniformMatrix3x2fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix3x2fv(location, transpose, value);
    }

    public void glUniformMatrix2x4fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix2x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x2fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix4x2fv(location, transpose, value);
    }

    public void glUniformMatrix3x4fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix3x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x3fv(int location, boolean transpose, FloatBuffer value) {
        GLES30.glUniformMatrix4x3fv(location, transpose, value);
    }

    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
            int dstY1, int mask, int filter) {
        GLES30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {
        GLES30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }

    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
        GLES30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access) {
        return GLES30.glMapBufferRange(target, offset, length, access);
    }

    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access, ByteBuffer old_buffer) {
        return GLES30.glMapBufferRange(target, offset, length, access, old_buffer);
    }

    public void glFlushMappedBufferRange(int target, long offset, long length) {
        GLES30.glFlushMappedBufferRange(target, offset, length);
    }

    public void glBindVertexArray(int array) {
        GLES30.glBindVertexArray(array);
    }

    public void glDeleteVertexArrays(IntBuffer arrays) {
        GLES30.glDeleteVertexArrays(arrays);
    }

    public void glDeleteVertexArrays(int array) {
        GLES30.glDeleteVertexArrays(array);
    }

    public void glGenVertexArrays(IntBuffer arrays) {
        GLES30.glGenVertexArrays(arrays);
    }

    public int glGenVertexArrays() {
        return GLES30.glGenVertexArrays();
    }

    public boolean glIsVertexArray(int array) {
        return GLES30.glIsVertexArray(array);
    }

    public void glGetIntegeri_v(int target, int index, IntBuffer data) {
        GLES30.glGetIntegeri_v(target, index, data);
    }

    public int glGetIntegeri(int target, int index) {
        return GLES30.glGetIntegeri(target, index);
    }

    public void glBeginTransformFeedback(int primitiveMode) {
        GLES30.glBeginTransformFeedback(primitiveMode);
    }

    public void glEndTransformFeedback() {
        GLES30.glEndTransformFeedback();
    }

    public void glBindBufferRange(int target, int index, int buffer, long offset, long size) {
        GLES30.glBindBufferRange(target, index, buffer, offset, size);
    }

    public void glBindBufferBase(int target, int index, int buffer) {
        GLES30.glBindBufferBase(target, index, buffer);
    }

    public void glTransformFeedbackVaryings(int program, PointerBuffer varyings, int bufferMode) {
        GLES30.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    public void glTransformFeedbackVaryings(int program, CharSequence[] varyings, int bufferMode) {
        GLES30.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    public void glTransformFeedbackVaryings(int program, CharSequence varying, int bufferMode) {
        GLES30.glTransformFeedbackVaryings(program, varying, bufferMode);
    }

    public void glGetTransformFeedbackVarying(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GLES30.glGetTransformFeedbackVarying(program, index, length, size, type, name);
    }

    public String glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GLES30.glGetTransformFeedbackVarying(program, index, bufSize, size, type);
    }

    public String glGetTransformFeedbackVarying(int program, int index, IntBuffer size, IntBuffer type) {
        return GLES30.glGetTransformFeedbackVarying(program, index, size, type);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, ByteBuffer pointer) {
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, long pointer) {
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, ShortBuffer pointer) {
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, IntBuffer pointer) {
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glGetVertexAttribIiv(int index, int pname, IntBuffer params) {
        GLES30.glGetVertexAttribIiv(index, pname, params);
    }

    public int glGetVertexAttribIi(int index, int pname) {
        return GLES30.glGetVertexAttribIi(index, pname);
    }

    public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params) {
        GLES30.glGetVertexAttribIuiv(index, pname, params);
    }

    public int glGetVertexAttribIui(int index, int pname) {
        return GLES30.glGetVertexAttribIui(index, pname);
    }

    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
        GLES30.glVertexAttribI4i(index, x, y, z, w);
    }

    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
        GLES30.glVertexAttribI4ui(index, x, y, z, w);
    }

    public void glVertexAttribI4iv(int index, IntBuffer v) {
        GLES30.glVertexAttribI4iv(index, v);
    }

    public void glVertexAttribI4uiv(int index, IntBuffer v) {
        GLES30.glVertexAttribI4uiv(index, v);
    }

    public void glGetUniformuiv(int program, int location, IntBuffer params) {
        GLES30.glGetUniformuiv(program, location, params);
    }

    public int glGetUniformui(int program, int location) {
        return GLES30.glGetUniformui(program, location);
    }

    public int glGetFragDataLocation(int program, ByteBuffer name) {
        return GLES30.glGetFragDataLocation(program, name);
    }

    public int glGetFragDataLocation(int program, CharSequence name) {
        return GLES30.glGetFragDataLocation(program, name);
    }

    public void glUniform1ui(int location, int v0) {
        GLES30.glUniform1ui(location, v0);
    }

    public void glUniform2ui(int location, int v0, int v1) {
        GLES30.glUniform2ui(location, v0, v1);
    }

    public void glUniform3ui(int location, int v0, int v1, int v2) {
        GLES30.glUniform3ui(location, v0, v1, v2);
    }

    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
        GLES30.glUniform4ui(location, v0, v1, v2, v3);
    }

    public void glUniform1uiv(int location, IntBuffer value) {
        GLES30.glUniform1uiv(location, value);
    }

    public void glUniform2uiv(int location, IntBuffer value) {
        GLES30.glUniform2uiv(location, value);
    }

    public void glUniform3uiv(int location, IntBuffer value) {
        GLES30.glUniform3uiv(location, value);
    }

    public void glUniform4uiv(int location, IntBuffer value) {
        GLES30.glUniform4uiv(location, value);
    }

    public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
        GLES30.glClearBufferiv(buffer, drawbuffer, value);
    }

    public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value) {
        GLES30.glClearBufferuiv(buffer, drawbuffer, value);
    }

    public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
        GLES30.glClearBufferfv(buffer, drawbuffer, value);
    }

    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {
        GLES30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
    }

    public String glGetStringi(int name, int index) {
        return GLES30.glGetStringi(name, index);
    }

    public void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size) {
        GLES30.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }

    public void glGetUniformIndices(int program, PointerBuffer uniformNames, IntBuffer uniformIndices) {
        GLES30.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public void glGetActiveUniformsiv(int program, IntBuffer uniformIndices, int pname, IntBuffer params) {
        GLES30.glGetActiveUniformsiv(program, uniformIndices, pname, params);
    }

    public int glGetUniformBlockIndex(int program, ByteBuffer uniformBlockName) {
        return GLES30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public int glGetUniformBlockIndex(int program, CharSequence uniformBlockName) {
        return GLES30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
        GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    public int glGetActiveUniformBlocki(int program, int uniformBlockIndex, int pname) {
        return GLES30.glGetActiveUniformBlocki(program, uniformBlockIndex, pname);
    }

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, IntBuffer length,
            ByteBuffer uniformBlockName) {
        GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize) {
        return GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize);
    }

    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        GLES30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    public void glDrawArraysInstanced(int mode, int first, int count, int instancecount) {
        GLES30.glDrawArraysInstanced(mode, first, count, instancecount);
    }

    public void glDrawElementsInstanced(int mode, int count, int type, long indices, int instancecount) {
        GLES30.glDrawElementsInstanced(mode, count, type, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, int type, ByteBuffer indices, int instancecount) {
        GLES30.glDrawElementsInstanced(mode, type, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, ByteBuffer indices, int instancecount) {
        GLES30.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, ShortBuffer indices, int instancecount) {
        GLES30.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, IntBuffer indices, int instancecount) {
        GLES30.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public long glFenceSync(int condition, int flags) {
        return GLES30.glFenceSync(condition, flags);
    }

    public boolean glIsSync(long sync) {
        return GLES30.glIsSync(sync);
    }

    public void glDeleteSync(long sync) {
        GLES30.glDeleteSync(sync);
    }

    public int glClientWaitSync(long sync, int flags, long timeout) {
        return GLES30.glClientWaitSync(sync, flags, timeout);
    }

    public void glWaitSync(long sync, int flags, long timeout) {
        GLES30.glWaitSync(sync, flags, timeout);
    }

    public void glGetInteger64v(int pname, LongBuffer data) {
        GLES30.glGetInteger64v(pname, data);
    }

    public long glGetInteger64(int pname) {
        return GLES30.glGetInteger64(pname);
    }

    public void glGetSynciv(long sync, int pname, IntBuffer length, IntBuffer values) {
        GLES30.glGetSynciv(sync, pname, length, values);
    }

    public int glGetSynci(long sync, int pname, IntBuffer length) {
        return GLES30.glGetSynci(sync, pname, length);
    }

    public void glGetInteger64i_v(int target, int index, LongBuffer data) {
        GLES30.glGetInteger64i_v(target, index, data);
    }

    public long glGetInteger64i(int target, int index) {
        return GLES30.glGetInteger64i(target, index);
    }

    public void glGetBufferParameteri64v(int target, int pname, LongBuffer params) {
        GLES30.glGetBufferParameteri64v(target, pname, params);
    }

    public long glGetBufferParameteri64(int target, int pname) {
        return GLES30.glGetBufferParameteri64(target, pname);
    }

    public void glGenSamplers(IntBuffer samplers) {
        GLES30.glGenSamplers(samplers);
    }

    public int glGenSamplers() {
        return GLES30.glGenSamplers();
    }

    public void glDeleteSamplers(IntBuffer samplers) {
        GLES30.glDeleteSamplers(samplers);
    }

    public void glDeleteSamplers(int sampler) {
        GLES30.glDeleteSamplers(sampler);
    }

    public boolean glIsSampler(int sampler) {
        return GLES30.glIsSampler(sampler);
    }

    public void glBindSampler(int unit, int sampler) {
        GLES30.glBindSampler(unit, sampler);
    }

    public void glSamplerParameteri(int sampler, int pname, int param) {
        GLES30.glSamplerParameteri(sampler, pname, param);
    }

    public void glSamplerParameteriv(int sampler, int pname, IntBuffer param) {
        GLES30.glSamplerParameteriv(sampler, pname, param);
    }

    public void glSamplerParameterf(int sampler, int pname, float param) {
        GLES30.glSamplerParameterf(sampler, pname, param);
    }

    public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param) {
        GLES30.glSamplerParameterfv(sampler, pname, param);
    }

    public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params) {
        GLES30.glGetSamplerParameteriv(sampler, pname, params);
    }

    public int glGetSamplerParameteri(int sampler, int pname) {
        return GLES30.glGetSamplerParameteri(sampler, pname);
    }

    public void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params) {
        GLES30.glGetSamplerParameterfv(sampler, pname, params);
    }

    public float glGetSamplerParameterf(int sampler, int pname) {
        return GLES30.glGetSamplerParameterf(sampler, pname);
    }

    public void glVertexAttribDivisor(int index, int divisor) {
        GLES30.glVertexAttribDivisor(index, divisor);
    }

    public void glBindTransformFeedback(int target, int id) {
        GLES30.glBindTransformFeedback(target, id);
    }

    public void glDeleteTransformFeedbacks(IntBuffer ids) {
        GLES30.glDeleteTransformFeedbacks(ids);
    }

    public void glDeleteTransformFeedbacks(int id) {
        GLES30.glDeleteTransformFeedbacks(id);
    }

    public void glGenTransformFeedbacks(IntBuffer ids) {
        GLES30.glGenTransformFeedbacks(ids);
    }

    public int glGenTransformFeedbacks() {
        return GLES30.glGenTransformFeedbacks();
    }

    public boolean glIsTransformFeedback(int id) {
        return GLES30.glIsTransformFeedback(id);
    }

    public void glPauseTransformFeedback() {
        GLES30.glPauseTransformFeedback();
    }

    public void glResumeTransformFeedback() {
        GLES30.glResumeTransformFeedback();
    }

    public void glGetProgramBinary(int program, IntBuffer length, IntBuffer binaryFormat, ByteBuffer binary) {
        GLES30.glGetProgramBinary(program, length, binaryFormat, binary);
    }

    public void glProgramBinary(int program, int binaryFormat, ByteBuffer binary) {
        GLES30.glProgramBinary(program, binaryFormat, binary);
    }

    public void glProgramParameteri(int program, int pname, int value) {
        GLES30.glProgramParameteri(program, pname, value);
    }

    public void glInvalidateFramebuffer(int target, IntBuffer attachments) {
        GLES30.glInvalidateFramebuffer(target, attachments);
    }

    public void glInvalidateFramebuffer(int target, int attachment) {
        GLES30.glInvalidateFramebuffer(target, attachment);
    }

    public void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height) {
        GLES30.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }

    public void glInvalidateSubFramebuffer(int target, int attachment, int x, int y, int width, int height) {
        GLES30.glInvalidateSubFramebuffer(target, attachment, x, y, width, height);
    }

    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        GLES30.glTexStorage2D(target, levels, internalformat, width, height);
    }

    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
        GLES30.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    public void glGetInternalformativ(int target, int internalformat, int pname, IntBuffer params) {
        GLES30.glGetInternalformativ(target, internalformat, pname, params);
    }

    public int glGetInternalformati(int target, int internalformat, int pname) {
        return GLES30.glGetInternalformati(target, internalformat, pname);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, short[] pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, int[] pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, float[] pixels) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, short[] pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, int[] pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, float[] pixels) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glGenQueries(int[] ids) {
        GLES30.glGenQueries(ids);
    }

    public void glDeleteQueries(int[] ids) {
        GLES30.glDeleteQueries(ids);
    }

    public void glGetQueryiv(int target, int pname, int[] params) {
        GLES30.glGetQueryiv(target, pname, params);
    }

    public void glGetQueryObjectuiv(int id, int pname, int[] params) {
        GLES30.glGetQueryObjectuiv(id, pname, params);
    }

    public void glDrawBuffers(int[] bufs) {
        GLES30.glDrawBuffers(bufs);
    }

    public void glUniformMatrix2x3fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix2x3fv(location, transpose, value);
    }

    public void glUniformMatrix3x2fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix3x2fv(location, transpose, value);
    }

    public void glUniformMatrix2x4fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix2x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x2fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix4x2fv(location, transpose, value);
    }

    public void glUniformMatrix3x4fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix3x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x3fv(int location, boolean transpose, float[] value) {
        GLES30.glUniformMatrix4x3fv(location, transpose, value);
    }

    public void glDeleteVertexArrays(int[] arrays) {
        GLES30.glDeleteVertexArrays(arrays);
    }

    public void glGenVertexArrays(int[] arrays) {
        GLES30.glGenVertexArrays(arrays);
    }

    public void glGetIntegeri_v(int target, int index, int[] data) {
        GLES30.glGetIntegeri_v(target, index, data);
    }

    public void glGetTransformFeedbackVarying(int program, int index, int[] length, int[] size, int[] type,
            ByteBuffer name) {
        GLES30.glGetTransformFeedbackVarying(program, index, length, size, type, name);
    }

    public void glGetVertexAttribIiv(int index, int pname, int[] params) {
        GLES30.glGetVertexAttribIiv(index, pname, params);
    }

    public void glGetVertexAttribIuiv(int index, int pname, int[] params) {
        GLES30.glGetVertexAttribIuiv(index, pname, params);
    }

    public void glVertexAttribI4iv(int index, int[] v) {
        GLES30.glVertexAttribI4iv(index, v);
    }

    public void glVertexAttribI4uiv(int index, int[] v) {
        GLES30.glVertexAttribI4uiv(index, v);
    }

    public void glGetUniformuiv(int program, int location, int[] params) {
        GLES30.glGetUniformuiv(program, location, params);
    }

    public void glUniform1uiv(int location, int[] value) {
        GLES30.glUniform1uiv(location, value);
    }

    public void glUniform2uiv(int location, int[] value) {
        GLES30.glUniform2uiv(location, value);
    }

    public void glUniform3uiv(int location, int[] value) {
        GLES30.glUniform3uiv(location, value);
    }

    public void glUniform4uiv(int location, int[] value) {
        GLES30.glUniform4uiv(location, value);
    }

    public void glClearBufferiv(int buffer, int drawbuffer, int[] value) {
        GLES30.glClearBufferiv(buffer, drawbuffer, value);
    }

    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value) {
        GLES30.glClearBufferuiv(buffer, drawbuffer, value);
    }

    public void glClearBufferfv(int buffer, int drawbuffer, float[] value) {
        GLES30.glClearBufferfv(buffer, drawbuffer, value);
    }

    public void glGetUniformIndices(int program, PointerBuffer uniformNames, int[] uniformIndices) {
        GLES30.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public void glGetActiveUniformsiv(int program, int[] uniformIndices, int pname, int[] params) {
        GLES30.glGetActiveUniformsiv(program, uniformIndices, pname, params);
    }

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params) {
        GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int[] length,
            ByteBuffer uniformBlockName) {
        GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public void glGetInteger64v(int pname, long[] data) {
        GLES30.glGetInteger64v(pname, data);
    }

    public void glGetSynciv(long sync, int pname, int[] length, int[] values) {
        GLES30.glGetSynciv(sync, pname, length, values);
    }

    public void glGetInteger64i_v(int target, int index, long[] data) {
        GLES30.glGetInteger64i_v(target, index, data);
    }

    public void glGetBufferParameteri64v(int target, int pname, long[] params) {
        GLES30.glGetBufferParameteri64v(target, pname, params);
    }

    public void glGenSamplers(int[] samplers) {
        GLES30.glGenSamplers(samplers);
    }

    public void glDeleteSamplers(int[] samplers) {
        GLES30.glDeleteSamplers(samplers);
    }

    public void glSamplerParameteriv(int sampler, int pname, int[] param) {
        GLES30.glSamplerParameteriv(sampler, pname, param);
    }

    public void glSamplerParameterfv(int sampler, int pname, float[] param) {
        GLES30.glSamplerParameterfv(sampler, pname, param);
    }

    public void glGetSamplerParameteriv(int sampler, int pname, int[] params) {
        GLES30.glGetSamplerParameteriv(sampler, pname, params);
    }

    public void glGetSamplerParameterfv(int sampler, int pname, float[] params) {
        GLES30.glGetSamplerParameterfv(sampler, pname, params);
    }

    public void glDeleteTransformFeedbacks(int[] ids) {
        GLES30.glDeleteTransformFeedbacks(ids);
    }

    public void glGenTransformFeedbacks(int[] ids) {
        GLES30.glGenTransformFeedbacks(ids);
    }

    public void glGetProgramBinary(int program, int[] length, int[] binaryFormat, ByteBuffer binary) {
        GLES30.glGetProgramBinary(program, length, binaryFormat, binary);
    }

    public void glInvalidateFramebuffer(int target, int[] attachments) {
        GLES30.glInvalidateFramebuffer(target, attachments);
    }

    public void glInvalidateSubFramebuffer(int target, int[] attachments, int x, int y, int width, int height) {
        GLES30.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }

    public void glGetInternalformativ(int target, int internalformat, int pname, int[] params) {
        GLES30.glGetInternalformativ(target, internalformat, pname, params);
    }

}
