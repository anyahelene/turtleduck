package turtleduck.gl.compat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL41C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.NativeType;
import org.lwjgl.opengl.ARBProgramInterfaceQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.PointerBuffer;

class GlImpl extends GlBase {
    public GlImpl() {
        super("OpenGL");
        // TODO Auto-generated constructor stub
    }

    private GLCapabilities caps;
    private Callback debugProc;
    private static GlImpl instance;

    public static GLA create() {
        if (instance == null) {
            instance = new GlImpl();
            instance.initialize();
        }
        return instance;
    }

    public void initializeImpl() {
        caps = GL.createCapabilities();
        if (caps.GL_ARB_program_interface_query) {
            piqImpl = new PIQImpl();
            System.out.println("PIQ supported");
        }
        
        if (caps.glTexImage2DMultisample != 0) {
            timImpl = new TIMImpl();
            System.out.println("PIQ supported");
        }
        if(caps.OpenGL45)
            maxVersion = 450;
        else if(caps.OpenGL44)
            maxVersion = 440;
        else if(caps.OpenGL43)
            maxVersion = 430;
        else if(caps.OpenGL32)
            maxVersion = 320;
        else if(caps.OpenGL30)
            maxVersion = 300;
    }

    @Override
    public Callback enableDebug() {
        debugProc = GLUtil.setupDebugMessageCallback();
        
        if (caps.glDebugMessageControl != 0) {
            GL43C.glDebugMessageControl(GL_DONT_CARE, GL43C.GL_DEBUG_TYPE_OTHER, GL43C.GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer) null, false);
            glEnable(GL43C.GL_DEBUG_OUTPUT);
        }
        return debugProc;
    }

    public boolean wireframe(boolean enable) {
        GL30C.glPolygonMode(GL_FRONT_AND_BACK, enable ? GL30C.GL_LINE : GL30C.GL_FILL);
        return true;
    }

    public boolean glPolygonModeLine(int face) {
        GL30C.glPolygonMode(face, GL30C.GL_LINE);
        return true;
    }

    public void glPolygonModeFill(int face) {
        GL30C.glPolygonMode(face, GL30C.GL_FILL);
    }

    public boolean glPolygonModePoint(int face) {
        GL30C.glPolygonMode(face, GL30C.GL_POINT);
        return true;
    }

    /**
     * @return GL_FRONT_LEFT (OpenGL) or GL_FRONT (OpenGL ES)
     */
    public int GL_FRONT_FB() {
        return GL30C.GL_FRONT_LEFT;
    }

    /**
     * @return GL_BACK_LEFT (OpenGL) or GL_BACK (OpenGL ES)
     */
    public int GL_BACK_FB() {
        return GL30C.GL_BACK_LEFT;
    }

    public void glActiveTexture(int texture) {
        GL30C.glActiveTexture(texture);
    }

    public void glAttachShader(int program, int shader) {
        GL30C.glAttachShader(program, shader);
    }

    public void glBindAttribLocation(int program, int index, ByteBuffer name) {
        GL30C.glBindAttribLocation(program, index, name);
    }

    public void glBindAttribLocation(int program, int index, CharSequence name) {
        GL30C.glBindAttribLocation(program, index, name);
    }

    public void glBindBuffer(int target, int buffer) {
        GL30C.glBindBuffer(target, buffer);
    }

    public void glBindFramebuffer(int target, int framebuffer) {
        GL30C.glBindFramebuffer(target, framebuffer);
    }

    public void glBindRenderbuffer(int target, int renderbuffer) {
        GL30C.glBindRenderbuffer(target, renderbuffer);
    }

    public void glBindTexture(int target, int texture) {
        GL30C.glBindTexture(target, texture);
    }

    public void glBlendColor(float red, float green, float blue, float alpha) {
        GL30C.glBlendColor(red, green, blue, alpha);
    }

    public void glBlendEquation(int mode) {
        GL30C.glBlendEquation(mode);
    }

    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        GL30C.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        GL30C.glBlendFunc(sfactor, dfactor);
    }

    public void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GL30C.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    public void glBufferData(int target, long size, int usage) {
        GL30C.glBufferData(target, size, usage);
    }

    public void glBufferData(int target, ByteBuffer data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, ShortBuffer data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, IntBuffer data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, FloatBuffer data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferSubData(int target, long offset, ByteBuffer data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, ShortBuffer data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, IntBuffer data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, FloatBuffer data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public int glCheckFramebufferStatus(int target) {
        return GL30C.glCheckFramebufferStatus(target);
    }

    public void glClear(int mask) {
        GL30C.glClear(mask);
    }

    public void glClearColor(float red, float green, float blue, float alpha) {
        GL30C.glClearColor(red, green, blue, alpha);
    }

    public void glClearDepthf(float d) {
        GL41C.glClearDepthf(d);
    }

    public void glClearStencil(int s) {
        GL30C.glClearStencil(s);
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL30C.glColorMask(red, green, blue, alpha);
    }

    public void glCompileShader(int shader) {
        GL30C.glCompileShader(shader);
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int imageSize, long data) {
        GL30C.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            ByteBuffer data) {
        GL30C.glCompressedTexImage2D(target, level, internalformat, width, height, border, data);
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int imageSize, long data) {
        GL30C.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, ByteBuffer data) {
        GL30C.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, data);
    }

    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
            int border) {
        GL30C.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
            int height) {
        GL30C.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    public int glCreateProgram() {
        return GL30C.glCreateProgram();
    }

    public int glCreateShader(int type) {
        return GL30C.glCreateShader(type);
    }

    public void glCullFace(int mode) {
        GL30C.glCullFace(mode);
    }

    public void glDeleteBuffers(IntBuffer buffers) {
        GL30C.glDeleteBuffers(buffers);
    }

    public void glDeleteBuffers(int buffer) {
        GL30C.glDeleteBuffers(buffer);
    }

    public void glDeleteFramebuffers(IntBuffer framebuffers) {
        GL30C.glDeleteFramebuffers(framebuffers);
    }

    public void glDeleteFramebuffers(int framebuffer) {
        GL30C.glDeleteFramebuffers(framebuffer);
    }

    public void glDeleteProgram(int program) {
        GL30C.glDeleteProgram(program);
    }

    public void glDeleteRenderbuffers(IntBuffer renderbuffers) {
        GL30C.glDeleteRenderbuffers(renderbuffers);
    }

    public void glDeleteRenderbuffers(int renderbuffer) {
        GL30C.glDeleteRenderbuffers(renderbuffer);
    }

    public void glDeleteShader(int shader) {
        GL30C.glDeleteShader(shader);
    }

    public void glDeleteTextures(IntBuffer textures) {
        GL30C.glDeleteTextures(textures);
    }

    public void glDeleteTextures(int texture) {
        GL30C.glDeleteTextures(texture);
    }

    public void glDepthFunc(int func) {
        GL30C.glDepthFunc(func);
    }

    public void glDepthMask(boolean flag) {
        GL30C.glDepthMask(flag);
    }

    public void glDepthRangef(float n, float f) {
        GL41C.glDepthRangef(n, f);
    }

    public void glDetachShader(int program, int shader) {
        GL30C.glDetachShader(program, shader);
    }

    public void glDisable(int cap) {
        GL30C.glDisable(cap);
    }

    public void glDisableVertexAttribArray(int index) {
        GL30C.glDisableVertexAttribArray(index);
    }

    public void glDrawArrays(int mode, int first, int count) {
        GL30C.glDrawArrays(mode, first, count);
    }

    public void glDrawElements(int mode, int count, int type, long indices) {
        GL30C.glDrawElements(mode, count, type, indices);
    }

    public void glDrawElements(int mode, int type, ByteBuffer indices) {
        GL30C.glDrawElements(mode, type, indices);
    }

    public void glDrawElements(int mode, ByteBuffer indices) {
        GL30C.glDrawElements(mode, indices);
    }

    public void glDrawElements(int mode, ShortBuffer indices) {
        GL30C.glDrawElements(mode, indices);
    }

    public void glDrawElements(int mode, IntBuffer indices) {
        GL30C.glDrawElements(mode, indices);
    }

    public void glEnable(int cap) {
        GL30C.glEnable(cap);
    }

    public void glEnableVertexAttribArray(int index) {
        GL30C.glEnableVertexAttribArray(index);
    }

    public void glFinish() {
        GL30C.glFinish();
    }

    public void glFlush() {
        GL30C.glFlush();
    }

    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GL30C.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        GL30C.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public void glFrontFace(int mode) {
        GL30C.glFrontFace(mode);
    }

    public void glGenBuffers(IntBuffer buffers) {
        GL30C.glGenBuffers(buffers);
    }

    public int glGenBuffers() {
        return GL30C.glGenBuffers();
    }

    public void glGenerateMipmap(int target) {
        GL30C.glGenerateMipmap(target);
    }

    public void glGenFramebuffers(IntBuffer framebuffers) {
        GL30C.glGenFramebuffers(framebuffers);
    }

    public int glGenFramebuffers() {
        return GL30C.glGenFramebuffers();
    }

    public void glGenRenderbuffers(IntBuffer renderbuffers) {
        GL30C.glGenRenderbuffers(renderbuffers);
    }

    public int glGenRenderbuffers() {
        return GL30C.glGenRenderbuffers();
    }

    public void glGenTextures(IntBuffer textures) {
        GL30C.glGenTextures(textures);
    }

    public int glGenTextures() {
        return GL30C.glGenTextures();
    }

    public void glGetActiveAttrib(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GL30C.glGetActiveAttrib(program, index, length, size, type, name);
    }

    public String glGetActiveAttrib(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GL30C.glGetActiveAttrib(program, index, bufSize, size, type);
    }

    public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        return GL30C.glGetActiveAttrib(program, index, size, type);
    }

    public void glGetActiveUniform(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GL30C.glGetActiveUniform(program, index, length, size, type, name);
    }

    public String glGetActiveUniform(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GL30C.glGetActiveUniform(program, index, bufSize, size, type);
    }

    public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        return GL30C.glGetActiveUniform(program, index, size, type);
    }

    public void glGetAttachedShaders(int program, IntBuffer count, IntBuffer shaders) {
        GL30C.glGetAttachedShaders(program, count, shaders);
    }

    public int glGetAttribLocation(int program, ByteBuffer name) {
        return GL30C.glGetAttribLocation(program, name);
    }

    public int glGetAttribLocation(int program, CharSequence name) {
        return GL30C.glGetAttribLocation(program, name);
    }

    public void glGetBooleanv(int pname, ByteBuffer data) {
        GL30C.glGetBooleanv(pname, data);
    }

    public boolean glGetBoolean(int pname) {
        return GL30C.glGetBoolean(pname);
    }

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        GL30C.glGetBufferParameteriv(target, pname, params);
    }

    public int glGetBufferParameteri(int target, int pname) {
        return GL30C.glGetBufferParameteri(target, pname);
    }

    public int glGetError() {
        return GL30C.glGetError();
    }

    public void glGetFloatv(int pname, FloatBuffer data) {
        GL30C.glGetFloatv(pname, data);
    }

    public float glGetFloat(int pname) {
        return GL30C.glGetFloat(pname);
    }

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        GL30C.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    public int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname) {
        return GL30C.glGetFramebufferAttachmentParameteri(target, attachment, pname);
    }

    public void glGetIntegerv(int pname, IntBuffer data) {
        GL30C.glGetIntegerv(pname, data);
    }

    public int glGetInteger(int pname) {
        return GL30C.glGetInteger(pname);
    }

    public void glGetProgramiv(int program, int pname, IntBuffer params) {
        GL30C.glGetProgramiv(program, pname, params);
    }

    public int glGetProgrami(int program, int pname) {
        return GL30C.glGetProgrami(program, pname);
    }

    public void glGetProgramInfoLog(int program, IntBuffer length, ByteBuffer infoLog) {
        GL30C.glGetProgramInfoLog(program, length, infoLog);
    }

    public String glGetProgramInfoLog(int program, int bufSize) {
        return GL30C.glGetProgramInfoLog(program, bufSize);
    }

    public String glGetProgramInfoLog(int program) {
        return GL30C.glGetProgramInfoLog(program);
    }

    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        GL30C.glGetRenderbufferParameteriv(target, pname, params);
    }

    public int glGetRenderbufferParameteri(int target, int pname) {
        return GL30C.glGetRenderbufferParameteri(target, pname);
    }

    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        GL30C.glGetShaderiv(shader, pname, params);
    }

    public int glGetShaderi(int shader, int pname) {
        return GL30C.glGetShaderi(shader, pname);
    }

    public void glGetShaderInfoLog(int shader, IntBuffer length, ByteBuffer infoLog) {
        GL30C.glGetShaderInfoLog(shader, length, infoLog);
    }

    public String glGetShaderInfoLog(int shader, int bufSize) {
        return GL30C.glGetShaderInfoLog(shader, bufSize);
    }

    public String glGetShaderInfoLog(int shader) {
        return GL30C.glGetShaderInfoLog(shader);
    }

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
        GL41C.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public void glGetShaderSource(int shader, IntBuffer length, ByteBuffer source) {
        GL30C.glGetShaderSource(shader, length, source);
    }

    public String glGetShaderSource(int shader, int bufSize) {
        return GL30C.glGetShaderSource(shader, bufSize);
    }

    public String glGetShaderSource(int shader) {
        return GL30C.glGetShaderSource(shader);
    }

    public String glGetString(int name) {
        return GL30C.glGetString(name);
    }

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        GL30C.glGetTexParameterfv(target, pname, params);
    }

    public float glGetTexParameterf(int target, int pname) {
        return GL30C.glGetTexParameterf(target, pname);
    }

    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        GL30C.glGetTexParameteriv(target, pname, params);
    }

    public int glGetTexParameteri(int target, int pname) {
        return GL30C.glGetTexParameteri(target, pname);
    }

    public void glGetUniformfv(int program, int location, FloatBuffer params) {
        GL30C.glGetUniformfv(program, location, params);
    }

    public float glGetUniformf(int program, int location) {
        return GL30C.glGetUniformf(program, location);
    }

    public void glGetUniformiv(int program, int location, IntBuffer params) {
        GL30C.glGetUniformiv(program, location, params);
    }

    public int glGetUniformi(int program, int location) {
        return GL30C.glGetUniformi(program, location);
    }

    public int glGetUniformLocation(int program, ByteBuffer name) {
        return GL30C.glGetUniformLocation(program, name);
    }

    public int glGetUniformLocation(int program, CharSequence name) {
        return GL30C.glGetUniformLocation(program, name);
    }

    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
        GL30C.glGetVertexAttribfv(index, pname, params);
    }

    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
        GL30C.glGetVertexAttribiv(index, pname, params);
    }

    public void glGetVertexAttribPointerv(int index, int pname, PointerBuffer pointer) {
        GL30C.glGetVertexAttribPointerv(index, pname, pointer);
    }

    public long glGetVertexAttribPointer(int index, int pname) {
        return GL30C.glGetVertexAttribPointer(index, pname);
    }

    public void glHint(int target, int mode) {
        GL30C.glHint(target, mode);
    }

    public boolean glIsBuffer(int buffer) {
        return GL30C.glIsBuffer(buffer);
    }

    public boolean glIsEnabled(int cap) {
        return GL30C.glIsEnabled(cap);
    }

    public boolean glIsFramebuffer(int framebuffer) {
        return GL30C.glIsFramebuffer(framebuffer);
    }

    public boolean glIsProgram(int program) {
        return GL30C.glIsProgram(program);
    }

    public boolean glIsRenderbuffer(int renderbuffer) {
        return GL30C.glIsRenderbuffer(renderbuffer);
    }

    public boolean glIsShader(int shader) {
        return GL30C.glIsShader(shader);
    }

    public boolean glIsTexture(int texture) {
        return GL30C.glIsTexture(texture);
    }

    public void glLineWidth(float width) {
        GL30C.glLineWidth(width);
    }

    public void glLinkProgram(int program) {
        GL30C.glLinkProgram(program);
    }

    public void glPixelStorei(int pname, int param) {
        GL30C.glPixelStorei(pname, param);
    }

    public void glPolygonOffset(float factor, float units) {
        GL30C.glPolygonOffset(factor, units);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReleaseShaderCompiler() {
        GL41C.glReleaseShaderCompiler();
    }

    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        GL30C.glRenderbufferStorage(target, internalformat, width, height);
    }

    public void glSampleCoverage(float value, boolean invert) {
        GL30C.glSampleCoverage(value, invert);
    }

    public void glScissor(int x, int y, int width, int height) {
        GL30C.glScissor(x, y, width, height);
    }

    public void glShaderBinary(IntBuffer shaders, int binaryformat, ByteBuffer binary) {
        GL41C.glShaderBinary(shaders, binaryformat, binary);
    }

    public void glShaderSource(int shader, PointerBuffer string, IntBuffer length) {
        GL30C.glShaderSource(shader, string, length);
    }

    public void glShaderSource(int shader, CharSequence... string) {
        GL30C.glShaderSource(shader, string);
    }

    public void glShaderSource(int shader, CharSequence string) {
        GL30C.glShaderSource(shader, string);
    }

    public void glStencilFunc(int func, int ref, int mask) {
        GL30C.glStencilFunc(func, ref, mask);
    }

    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        GL30C.glStencilFuncSeparate(face, func, ref, mask);
    }

    public void glStencilMask(int mask) {
        GL30C.glStencilMask(mask);
    }

    public void glStencilMaskSeparate(int face, int mask) {
        GL30C.glStencilMaskSeparate(face, mask);
    }

    public void glStencilOp(int fail, int zfail, int zpass) {
        GL30C.glStencilOp(fail, zfail, zpass);
    }

    public void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        GL30C.glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ByteBuffer pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, long pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ShortBuffer pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, IntBuffer pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, FloatBuffer pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexParameterf(int target, int pname, float param) {
        GL30C.glTexParameterf(target, pname, param);
    }

    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        GL30C.glTexParameterfv(target, pname, params);
    }

    public void glTexParameteri(int target, int pname, int param) {
        GL30C.glTexParameteri(target, pname, param);
    }

    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        GL30C.glTexParameteriv(target, pname, params);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, ByteBuffer pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, long pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, ShortBuffer pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, IntBuffer pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, FloatBuffer pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glUniform1f(int location, float v0) {
        GL30C.glUniform1f(location, v0);
    }

    public void glUniform1fv(int location, FloatBuffer value) {
        GL30C.glUniform1fv(location, value);
    }

    public void glUniform1i(int location, int v0) {
        GL30C.glUniform1i(location, v0);
    }

    public void glUniform1iv(int location, IntBuffer value) {
        GL30C.glUniform1iv(location, value);
    }

    public void glUniform2f(int location, float v0, float v1) {
        GL30C.glUniform2f(location, v0, v1);
    }

    public void glUniform2fv(int location, FloatBuffer value) {
        GL30C.glUniform2fv(location, value);
    }

    public void glUniform2i(int location, int v0, int v1) {
        GL30C.glUniform2i(location, v0, v1);
    }

    public void glUniform2iv(int location, IntBuffer value) {
        GL30C.glUniform2iv(location, value);
    }

    public void glUniform3f(int location, float v0, float v1, float v2) {
        GL30C.glUniform3f(location, v0, v1, v2);
    }

    public void glUniform3fv(int location, FloatBuffer value) {
        GL30C.glUniform3fv(location, value);
    }

    public void glUniform3i(int location, int v0, int v1, int v2) {
        GL30C.glUniform3i(location, v0, v1, v2);
    }

    public void glUniform3iv(int location, IntBuffer value) {
        GL30C.glUniform3iv(location, value);
    }

    public void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        GL30C.glUniform4f(location, v0, v1, v2, v3);
    }

    public void glUniform4fv(int location, FloatBuffer value) {
        GL30C.glUniform4fv(location, value);
    }

    public void glUniform4i(int location, int v0, int v1, int v2, int v3) {
        GL30C.glUniform4i(location, v0, v1, v2, v3);
    }

    public void glUniform4iv(int location, IntBuffer value) {
        GL30C.glUniform4iv(location, value);
    }

    public void glUniformMatrix2fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix2fv(location, transpose, value);
    }

    public void glUniformMatrix3fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix3fv(location, transpose, value);
    }

    public void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix4fv(location, transpose, value);
    }

    public void glUseProgram(int program) {
        GL30C.glUseProgram(program);
    }

    public void glValidateProgram(int program) {
        GL30C.glValidateProgram(program);
    }

    public void glVertexAttrib1f(int index, float x) {
        GL30C.glVertexAttrib1f(index, x);
    }

    public void glVertexAttrib1fv(int index, FloatBuffer v) {
        GL30C.glVertexAttrib1fv(index, v);
    }

    public void glVertexAttrib2f(int index, float x, float y) {
        GL30C.glVertexAttrib2f(index, x, y);
    }

    public void glVertexAttrib2fv(int index, FloatBuffer v) {
        GL30C.glVertexAttrib2fv(index, v);
    }

    public void glVertexAttrib3f(int index, float x, float y, float z) {
        GL30C.glVertexAttrib3f(index, x, y, z);
    }

    public void glVertexAttrib3fv(int index, FloatBuffer v) {
        GL30C.glVertexAttrib3fv(index, v);
    }

    public void glVertexAttrib4f(int index, float x, float y, float z, float w) {
        GL30C.glVertexAttrib4f(index, x, y, z, w);
    }

    public void glVertexAttrib4fv(int index, FloatBuffer v) {
        GL30C.glVertexAttrib4fv(index, v);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            ByteBuffer pointer) {
        GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
        GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            ShortBuffer pointer) {
        GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            IntBuffer pointer) {
        GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            FloatBuffer pointer) {
        GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public void glViewport(int x, int y, int width, int height) {
        GL30C.glViewport(x, y, width, height);
    }

    public void glBufferData(int target, short[] data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, int[] data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferData(int target, float[] data, int usage) {
        GL30C.glBufferData(target, data, usage);
    }

    public void glBufferSubData(int target, long offset, short[] data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, int[] data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glBufferSubData(int target, long offset, float[] data) {
        GL30C.glBufferSubData(target, offset, data);
    }

    public void glDeleteBuffers(int[] buffers) {
        GL30C.glDeleteBuffers(buffers);
    }

    public void glDeleteFramebuffers(int[] framebuffers) {
        GL30C.glDeleteFramebuffers(framebuffers);
    }

    public void glDeleteRenderbuffers(int[] renderbuffers) {
        GL30C.glDeleteRenderbuffers(renderbuffers);
    }

    public void glDeleteTextures(int[] textures) {
        GL30C.glDeleteTextures(textures);
    }

    public void glGenBuffers(int[] buffers) {
        GL30C.glGenBuffers(buffers);
    }

    public void glGenFramebuffers(int[] framebuffers) {
        GL30C.glGenFramebuffers(framebuffers);
    }

    public void glGenRenderbuffers(int[] renderbuffers) {
        GL30C.glGenRenderbuffers(renderbuffers);
    }

    public void glGenTextures(int[] textures) {
        GL30C.glGenTextures(textures);
    }

    public void glGetActiveAttrib(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name) {
        GL30C.glGetActiveAttrib(program, index, length, size, type, name);
    }

    public void glGetActiveUniform(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name) {
        GL30C.glGetActiveUniform(program, index, length, size, type, name);
    }

    public void glGetAttachedShaders(int program, int[] count, int[] shaders) {
        GL30C.glGetAttachedShaders(program, count, shaders);
    }

    public void glGetBufferParameteriv(int target, int pname, int[] params) {
        GL30C.glGetBufferParameteriv(target, pname, params);
    }

    public void glGetFloatv(int pname, float[] data) {
        GL30C.glGetFloatv(pname, data);
    }

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params) {
        GL30C.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    public void glGetIntegerv(int pname, int[] data) {
        GL30C.glGetIntegerv(pname, data);
    }

    public void glGetProgramiv(int program, int pname, int[] params) {
        GL30C.glGetProgramiv(program, pname, params);
    }

    public void glGetProgramInfoLog(int program, int[] length, ByteBuffer infoLog) {
        GL30C.glGetProgramInfoLog(program, length, infoLog);
    }

    public void glGetRenderbufferParameteriv(int target, int pname, int[] params) {
        GL30C.glGetRenderbufferParameteriv(target, pname, params);
    }

    public void glGetShaderiv(int shader, int pname, int[] params) {
        GL30C.glGetShaderiv(shader, pname, params);
    }

    public void glGetShaderInfoLog(int shader, int[] length, ByteBuffer infoLog) {
        GL30C.glGetShaderInfoLog(shader, length, infoLog);
    }

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int[] precision) {
        GL41C.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public void glGetShaderSource(int shader, int[] length, ByteBuffer source) {
        GL30C.glGetShaderSource(shader, length, source);
    }

    public void glGetTexParameterfv(int target, int pname, float[] params) {
        GL30C.glGetTexParameterfv(target, pname, params);
    }

    public void glGetTexParameteriv(int target, int pname, int[] params) {
        GL30C.glGetTexParameteriv(target, pname, params);
    }

    public void glGetUniformfv(int program, int location, float[] params) {
        GL30C.glGetUniformfv(program, location, params);
    }

    public void glGetUniformiv(int program, int location, int[] params) {
        GL30C.glGetUniformiv(program, location, params);
    }

    public void glGetVertexAttribfv(int index, int pname, float[] params) {
        GL30C.glGetVertexAttribfv(index, pname, params);
    }

    public void glGetVertexAttribiv(int index, int pname, int[] params) {
        GL30C.glGetVertexAttribiv(index, pname, params);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, short[] pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, int[] pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
        GL30C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void glShaderBinary(int[] shaders, int binaryformat, ByteBuffer binary) {
        GL41C.glShaderBinary(shaders, binaryformat, binary);
    }

    public void glShaderSource(int shader, PointerBuffer string, int[] length) {
        GL30C.glShaderSource(shader, string, length);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, short[] pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, int[] pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, float[] pixels) {
        GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glTexParameterfv(int target, int pname, float[] params) {
        GL30C.glTexParameterfv(target, pname, params);
    }

    public void glTexParameteriv(int target, int pname, int[] params) {
        GL30C.glTexParameteriv(target, pname, params);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, short[] pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, int[] pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int type, float[] pixels) {
        GL30C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public void glUniform1fv(int location, float[] value) {
        GL30C.glUniform1fv(location, value);
    }

    public void glUniform1iv(int location, int[] value) {
        GL30C.glUniform1iv(location, value);
    }

    public void glUniform2fv(int location, float[] value) {
        GL30C.glUniform2fv(location, value);
    }

    public void glUniform2iv(int location, int[] value) {
        GL30C.glUniform2iv(location, value);
    }

    public void glUniform3fv(int location, float[] value) {
        GL30C.glUniform3fv(location, value);
    }

    public void glUniform3iv(int location, int[] value) {
        GL30C.glUniform3iv(location, value);
    }

    public void glUniform4fv(int location, float[] value) {
        GL30C.glUniform4fv(location, value);
    }

    public void glUniform4iv(int location, int[] value) {
        GL30C.glUniform4iv(location, value);
    }

    public void glUniformMatrix2fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix2fv(location, transpose, value);
    }

    public void glUniformMatrix3fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix3fv(location, transpose, value);
    }

    public void glUniformMatrix4fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix4fv(location, transpose, value);
    }

    public void glVertexAttrib1fv(int index, float[] v) {
        GL30C.glVertexAttrib1fv(index, v);
    }

    public void glVertexAttrib2fv(int index, float[] v) {
        GL30C.glVertexAttrib2fv(index, v);
    }

    public void glVertexAttrib3fv(int index, float[] v) {
        GL30C.glVertexAttrib3fv(index, v);
    }

    public void glVertexAttrib4fv(int index, float[] v) {
        GL30C.glVertexAttrib4fv(index, v);
    }

    public void glReadBuffer(int src) {
        GL30C.glReadBuffer(src);
    }

    public void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices) {
        GL30C.glDrawRangeElements(mode, start, end, count, type, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, int type, ByteBuffer indices) {
        GL30C.glDrawRangeElements(mode, start, end, type, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, ByteBuffer indices) {
        GL30C.glDrawRangeElements(mode, start, end, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, ShortBuffer indices) {
        GL30C.glDrawRangeElements(mode, start, end, indices);
    }

    public void glDrawRangeElements(int mode, int start, int end, IntBuffer indices) {
        GL30C.glDrawRangeElements(mode, start, end, indices);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ByteBuffer pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, long pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ShortBuffer pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, IntBuffer pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, FloatBuffer pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, ByteBuffer pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, long pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, ShortBuffer pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, IntBuffer pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, FloatBuffer pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
            int width, int height) {
        GL30C.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
            int border, int imageSize, long data) {
        GL30C.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, data);
    }

    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
            int border, ByteBuffer data) {
        GL30C.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, data);
    }

    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int imageSize, long data) {
        GL30C.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                imageSize, data);
    }

    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, ByteBuffer data) {
        GL30C.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data);
    }

    public void glGenQueries(IntBuffer ids) {
        GL30C.glGenQueries(ids);
    }

    public int glGenQueries() {
        return GL30C.glGenQueries();
    }

    public void glDeleteQueries(IntBuffer ids) {
        GL30C.glDeleteQueries(ids);
    }

    public void glDeleteQueries(int id) {
        GL30C.glDeleteQueries(id);
    }

    public boolean glIsQuery(int id) {
        return GL30C.glIsQuery(id);
    }

    public void glBeginQuery(int target, int id) {
        GL30C.glBeginQuery(target, id);
    }

    public void glEndQuery(int target) {
        GL30C.glEndQuery(target);
    }

    public void glGetQueryiv(int target, int pname, IntBuffer params) {
        GL30C.glGetQueryiv(target, pname, params);
    }

    public int glGetQueryi(int target, int pname) {
        return GL30C.glGetQueryi(target, pname);
    }

    public void glGetQueryObjectuiv(int id, int pname, IntBuffer params) {
        GL30C.glGetQueryObjectuiv(id, pname, params);
    }

    public int glGetQueryObjectui(int id, int pname) {
        return GL30C.glGetQueryObjectui(id, pname);
    }

    public boolean glUnmapBuffer(int target) {
        return GL30C.glUnmapBuffer(target);
    }

    public void glGetBufferPointerv(int target, int pname, PointerBuffer params) {
        GL30C.glGetBufferPointerv(target, pname, params);
    }

    public long glGetBufferPointer(int target, int pname) {
        return GL30C.glGetBufferPointer(target, pname);
    }

    public void glDrawBuffers(IntBuffer bufs) {
        GL30C.glDrawBuffers(bufs);
    }

    public void glDrawBuffers(int buf) {
        GL30C.glDrawBuffers(buf);
    }

    public void glUniformMatrix2x3fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix2x3fv(location, transpose, value);
    }

    public void glUniformMatrix3x2fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix3x2fv(location, transpose, value);
    }

    public void glUniformMatrix2x4fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix2x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x2fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix4x2fv(location, transpose, value);
    }

    public void glUniformMatrix3x4fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix3x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x3fv(int location, boolean transpose, FloatBuffer value) {
        GL30C.glUniformMatrix4x3fv(location, transpose, value);
    }

    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
            int dstY1, int mask, int filter) {
        GL30C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {
        GL30C.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }

    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
        GL30C.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access) {
        return GL30C.glMapBufferRange(target, offset, length, access);
    }

    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access, ByteBuffer old_buffer) {
        return GL30C.glMapBufferRange(target, offset, length, access, old_buffer);
    }

    public void glFlushMappedBufferRange(int target, long offset, long length) {
        GL30C.glFlushMappedBufferRange(target, offset, length);
    }

    public void glBindVertexArray(int array) {
        GL30C.glBindVertexArray(array);
    }

    public void glDeleteVertexArrays(IntBuffer arrays) {
        GL30C.glDeleteVertexArrays(arrays);
    }

    public void glDeleteVertexArrays(int array) {
        GL30C.glDeleteVertexArrays(array);
    }

    public void glGenVertexArrays(IntBuffer arrays) {
        GL30C.glGenVertexArrays(arrays);
    }

    public int glGenVertexArrays() {
        return GL30C.glGenVertexArrays();
    }

    public boolean glIsVertexArray(int array) {
        return GL30C.glIsVertexArray(array);
    }

    public void glGetIntegeri_v(int target, int index, IntBuffer data) {
        GL30C.glGetIntegeri_v(target, index, data);
    }

    public int glGetIntegeri(int target, int index) {
        return GL30C.glGetIntegeri(target, index);
    }

    public void glBeginTransformFeedback(int primitiveMode) {
        GL30C.glBeginTransformFeedback(primitiveMode);
    }

    public void glEndTransformFeedback() {
        GL30C.glEndTransformFeedback();
    }

    public void glBindBufferRange(int target, int index, int buffer, long offset, long size) {
        GL30C.glBindBufferRange(target, index, buffer, offset, size);
    }

    public void glBindBufferBase(int target, int index, int buffer) {
        GL30C.glBindBufferBase(target, index, buffer);
    }

    public void glTransformFeedbackVaryings(int program, PointerBuffer varyings, int bufferMode) {
        GL30C.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    public void glTransformFeedbackVaryings(int program, CharSequence[] varyings, int bufferMode) {
        GL30C.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    public void glTransformFeedbackVaryings(int program, CharSequence varying, int bufferMode) {
        GL30C.glTransformFeedbackVaryings(program, varying, bufferMode);
    }

    public void glGetTransformFeedbackVarying(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name) {
        GL30C.glGetTransformFeedbackVarying(program, index, length, size, type, name);
    }

    public String glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return GL30C.glGetTransformFeedbackVarying(program, index, bufSize, size, type);
    }

    public String glGetTransformFeedbackVarying(int program, int index, IntBuffer size, IntBuffer type) {
        return GL30C.glGetTransformFeedbackVarying(program, index, size, type);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, ByteBuffer pointer) {
        GL30C.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, long pointer) {
        GL30C.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, ShortBuffer pointer) {
        GL30C.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glVertexAttribIPointer(int index, int size, int type, int stride, IntBuffer pointer) {
        GL30C.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public void glGetVertexAttribIiv(int index, int pname, IntBuffer params) {
        GL30C.glGetVertexAttribIiv(index, pname, params);
    }

    public int glGetVertexAttribIi(int index, int pname) {
        return GL30C.glGetVertexAttribIi(index, pname);
    }

    public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params) {
        GL30C.glGetVertexAttribIuiv(index, pname, params);
    }

    public int glGetVertexAttribIui(int index, int pname) {
        return GL30C.glGetVertexAttribIui(index, pname);
    }

    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
        GL30C.glVertexAttribI4i(index, x, y, z, w);
    }

    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
        GL30C.glVertexAttribI4ui(index, x, y, z, w);
    }

    public void glVertexAttribI4iv(int index, IntBuffer v) {
        GL30C.glVertexAttribI4iv(index, v);
    }

    public void glVertexAttribI4uiv(int index, IntBuffer v) {
        GL30C.glVertexAttribI4uiv(index, v);
    }

    public void glGetUniformuiv(int program, int location, IntBuffer params) {
        GL30C.glGetUniformuiv(program, location, params);
    }

    public int glGetUniformui(int program, int location) {
        return GL30C.glGetUniformui(program, location);
    }

    public int glGetFragDataLocation(int program, ByteBuffer name) {
        return GL30C.glGetFragDataLocation(program, name);
    }

    public int glGetFragDataLocation(int program, CharSequence name) {
        return GL30C.glGetFragDataLocation(program, name);
    }

    public void glUniform1ui(int location, int v0) {
        GL30C.glUniform1ui(location, v0);
    }

    public void glUniform2ui(int location, int v0, int v1) {
        GL30C.glUniform2ui(location, v0, v1);
    }

    public void glUniform3ui(int location, int v0, int v1, int v2) {
        GL30C.glUniform3ui(location, v0, v1, v2);
    }

    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
        GL30C.glUniform4ui(location, v0, v1, v2, v3);
    }

    public void glUniform1uiv(int location, IntBuffer value) {
        GL30C.glUniform1uiv(location, value);
    }

    public void glUniform2uiv(int location, IntBuffer value) {
        GL30C.glUniform2uiv(location, value);
    }

    public void glUniform3uiv(int location, IntBuffer value) {
        GL30C.glUniform3uiv(location, value);
    }

    public void glUniform4uiv(int location, IntBuffer value) {
        GL30C.glUniform4uiv(location, value);
    }

    public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
        GL30C.glClearBufferiv(buffer, drawbuffer, value);
    }

    public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value) {
        GL30C.glClearBufferuiv(buffer, drawbuffer, value);
    }

    public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
        GL30C.glClearBufferfv(buffer, drawbuffer, value);
    }

    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {
        GL30C.glClearBufferfi(buffer, drawbuffer, depth, stencil);
    }

    public String glGetStringi(int name, int index) {
        return GL30C.glGetStringi(name, index);
    }

    public void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size) {
        GL41C.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }

    public void glGetUniformIndices(int program, PointerBuffer uniformNames, IntBuffer uniformIndices) {
        GL41C.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public void glGetActiveUniformsiv(int program, IntBuffer uniformIndices, int pname, IntBuffer params) {
        GL41C.glGetActiveUniformsiv(program, uniformIndices, pname, params);
    }

    public int glGetUniformBlockIndex(int program, ByteBuffer uniformBlockName) {
        return GL41C.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public int glGetUniformBlockIndex(int program, CharSequence uniformBlockName) {
        return GL41C.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
        GL41C.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    public int glGetActiveUniformBlocki(int program, int uniformBlockIndex, int pname) {
        return GL41C.glGetActiveUniformBlocki(program, uniformBlockIndex, pname);
    }

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, IntBuffer length,
            ByteBuffer uniformBlockName) {
        GL41C.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize) {
        return GL41C.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize);
    }

    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return GL41C.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        GL41C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    public void glDrawArraysInstanced(int mode, int first, int count, int instancecount) {
        GL41C.glDrawArraysInstanced(mode, first, count, instancecount);
    }

    public void glDrawElementsInstanced(int mode, int count, int type, long indices, int instancecount) {
        GL41C.glDrawElementsInstanced(mode, count, type, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, int type, ByteBuffer indices, int instancecount) {
        GL41C.glDrawElementsInstanced(mode, type, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, ByteBuffer indices, int instancecount) {
        GL41C.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, ShortBuffer indices, int instancecount) {
        GL41C.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public void glDrawElementsInstanced(int mode, IntBuffer indices, int instancecount) {
        GL41C.glDrawElementsInstanced(mode, indices, instancecount);
    }

    public long glFenceSync(int condition, int flags) {
        return GL41C.glFenceSync(condition, flags);
    }

    public boolean glIsSync(long sync) {
        return GL41C.glIsSync(sync);
    }

    public void glDeleteSync(long sync) {
        GL41C.glDeleteSync(sync);
    }

    public int glClientWaitSync(long sync, int flags, long timeout) {
        return GL41C.glClientWaitSync(sync, flags, timeout);
    }

    public void glWaitSync(long sync, int flags, long timeout) {
        GL41C.glWaitSync(sync, flags, timeout);
    }

    public void glGetInteger64v(int pname, LongBuffer data) {
        GL41C.glGetInteger64v(pname, data);
    }

    public long glGetInteger64(int pname) {
        return GL41C.glGetInteger64(pname);
    }

    public void glGetSynciv(long sync, int pname, IntBuffer length, IntBuffer values) {
        GL41C.glGetSynciv(sync, pname, length, values);
    }

    public int glGetSynci(long sync, int pname, IntBuffer length) {
        return GL41C.glGetSynci(sync, pname, length);
    }

    public void glGetInteger64i_v(int target, int index, LongBuffer data) {
        GL41C.glGetInteger64i_v(target, index, data);
    }

    public long glGetInteger64i(int target, int index) {
        return GL41C.glGetInteger64i(target, index);
    }

    public void glGetBufferParameteri64v(int target, int pname, LongBuffer params) {
        GL41C.glGetBufferParameteri64v(target, pname, params);
    }

    public long glGetBufferParameteri64(int target, int pname) {
        return GL41C.glGetBufferParameteri64(target, pname);
    }

    public void glGenSamplers(IntBuffer samplers) {
        GL41C.glGenSamplers(samplers);
    }

    public int glGenSamplers() {
        return GL41C.glGenSamplers();
    }

    public void glDeleteSamplers(IntBuffer samplers) {
        GL41C.glDeleteSamplers(samplers);
    }

    public void glDeleteSamplers(int sampler) {
        GL41C.glDeleteSamplers(sampler);
    }

    public boolean glIsSampler(int sampler) {
        return GL41C.glIsSampler(sampler);
    }

    public void glBindSampler(int unit, int sampler) {
        GL41C.glBindSampler(unit, sampler);
    }

    public void glSamplerParameteri(int sampler, int pname, int param) {
        GL41C.glSamplerParameteri(sampler, pname, param);
    }

    public void glSamplerParameteriv(int sampler, int pname, IntBuffer param) {
        GL41C.glSamplerParameteriv(sampler, pname, param);
    }

    public void glSamplerParameterf(int sampler, int pname, float param) {
        GL41C.glSamplerParameterf(sampler, pname, param);
    }

    public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param) {
        GL41C.glSamplerParameterfv(sampler, pname, param);
    }

    public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params) {
        GL41C.glGetSamplerParameteriv(sampler, pname, params);
    }

    public int glGetSamplerParameteri(int sampler, int pname) {
        return GL41C.glGetSamplerParameteri(sampler, pname);
    }

    public void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params) {
        GL41C.glGetSamplerParameterfv(sampler, pname, params);
    }

    public float glGetSamplerParameterf(int sampler, int pname) {
        return GL41C.glGetSamplerParameterf(sampler, pname);
    }

    public void glVertexAttribDivisor(int index, int divisor) {
        GL41C.glVertexAttribDivisor(index, divisor);
    }

    public void glBindTransformFeedback(int target, int id) {
        GL41C.glBindTransformFeedback(target, id);
    }

    public void glDeleteTransformFeedbacks(IntBuffer ids) {
        GL41C.glDeleteTransformFeedbacks(ids);
    }

    public void glDeleteTransformFeedbacks(int id) {
        GL41C.glDeleteTransformFeedbacks(id);
    }

    public void glGenTransformFeedbacks(IntBuffer ids) {
        GL41C.glGenTransformFeedbacks(ids);
    }

    public int glGenTransformFeedbacks() {
        return GL41C.glGenTransformFeedbacks();
    }

    public boolean glIsTransformFeedback(int id) {
        return GL41C.glIsTransformFeedback(id);
    }

    public void glPauseTransformFeedback() {
        GL41C.glPauseTransformFeedback();
    }

    public void glResumeTransformFeedback() {
        GL41C.glResumeTransformFeedback();
    }

    public void glGetProgramBinary(int program, IntBuffer length, IntBuffer binaryFormat, ByteBuffer binary) {
        GL41C.glGetProgramBinary(program, length, binaryFormat, binary);
    }

    public void glProgramBinary(int program, int binaryFormat, ByteBuffer binary) {
        GL41C.glProgramBinary(program, binaryFormat, binary);
    }

    public void glProgramParameteri(int program, int pname, int value) {
        GL41C.glProgramParameteri(program, pname, value);
    }

    public void glInvalidateFramebuffer(int target, IntBuffer attachments) {
        GL43C.glInvalidateFramebuffer(target, attachments);
    }

    public void glInvalidateFramebuffer(int target, int attachment) {
        GL43C.glInvalidateFramebuffer(target, attachment);
    }

    public void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height) {
        GL43C.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }

    public void glInvalidateSubFramebuffer(int target, int attachment, int x, int y, int width, int height) {
        GL43C.glInvalidateSubFramebuffer(target, attachment, x, y, width, height);
    }

    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        GL42C.glTexStorage2D(target, levels, internalformat, width, height);
    }

    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
        GL42C.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    public void glGetInternalformativ(int target, int internalformat, int pname, IntBuffer params) {
        GL42C.glGetInternalformativ(target, internalformat, pname, params);
    }

    public int glGetInternalformati(int target, int internalformat, int pname) {
        return GL42C.glGetInternalformati(target, internalformat, pname);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, short[] pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, int[] pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, float[] pixels) {
        GL30C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, short[] pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, int[] pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int type, float[] pixels) {
        GL30C.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public void glGenQueries(int[] ids) {
        GL30C.glGenQueries(ids);
    }

    public void glDeleteQueries(int[] ids) {
        GL30C.glDeleteQueries(ids);
    }

    public void glGetQueryiv(int target, int pname, int[] params) {
        GL30C.glGetQueryiv(target, pname, params);
    }

    public void glGetQueryObjectuiv(int id, int pname, int[] params) {
        GL30C.glGetQueryObjectuiv(id, pname, params);
    }

    public void glDrawBuffers(int[] bufs) {
        GL30C.glDrawBuffers(bufs);
    }

    public void glUniformMatrix2x3fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix2x3fv(location, transpose, value);
    }

    public void glUniformMatrix3x2fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix3x2fv(location, transpose, value);
    }

    public void glUniformMatrix2x4fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix2x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x2fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix4x2fv(location, transpose, value);
    }

    public void glUniformMatrix3x4fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix3x4fv(location, transpose, value);
    }

    public void glUniformMatrix4x3fv(int location, boolean transpose, float[] value) {
        GL30C.glUniformMatrix4x3fv(location, transpose, value);
    }

    public void glDeleteVertexArrays(int[] arrays) {
        GL30C.glDeleteVertexArrays(arrays);
    }

    public void glGenVertexArrays(int[] arrays) {
        GL30C.glGenVertexArrays(arrays);
    }

    public void glGetIntegeri_v(int target, int index, int[] data) {
        GL30C.glGetIntegeri_v(target, index, data);
    }

    public void glGetTransformFeedbackVarying(int program, int index, int[] length, int[] size, int[] type,
            ByteBuffer name) {
        GL30C.glGetTransformFeedbackVarying(program, index, length, size, type, name);
    }

    public void glGetVertexAttribIiv(int index, int pname, int[] params) {
        GL30C.glGetVertexAttribIiv(index, pname, params);
    }

    public void glGetVertexAttribIuiv(int index, int pname, int[] params) {
        GL30C.glGetVertexAttribIuiv(index, pname, params);
    }

    public void glVertexAttribI4iv(int index, int[] v) {
        GL30C.glVertexAttribI4iv(index, v);
    }

    public void glVertexAttribI4uiv(int index, int[] v) {
        GL30C.glVertexAttribI4uiv(index, v);
    }

    public void glGetUniformuiv(int program, int location, int[] params) {
        GL30C.glGetUniformuiv(program, location, params);
    }

    public void glUniform1uiv(int location, int[] value) {
        GL30C.glUniform1uiv(location, value);
    }

    public void glUniform2uiv(int location, int[] value) {
        GL30C.glUniform2uiv(location, value);
    }

    public void glUniform3uiv(int location, int[] value) {
        GL30C.glUniform3uiv(location, value);
    }

    public void glUniform4uiv(int location, int[] value) {
        GL30C.glUniform4uiv(location, value);
    }

    public void glClearBufferiv(int buffer, int drawbuffer, int[] value) {
        GL30C.glClearBufferiv(buffer, drawbuffer, value);
    }

    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value) {
        GL30C.glClearBufferuiv(buffer, drawbuffer, value);
    }

    public void glClearBufferfv(int buffer, int drawbuffer, float[] value) {
        GL30C.glClearBufferfv(buffer, drawbuffer, value);
    }

    public void glGetUniformIndices(int program, PointerBuffer uniformNames, int[] uniformIndices) {
        GL41C.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public void glGetActiveUniformsiv(int program, int[] uniformIndices, int pname, int[] params) {
        GL41C.glGetActiveUniformsiv(program, uniformIndices, pname, params);
    }

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params) {
        GL41C.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int[] length,
            ByteBuffer uniformBlockName) {
        GL41C.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public void glGetInteger64v(int pname, long[] data) {
        GL41C.glGetInteger64v(pname, data);
    }

    public void glGetSynciv(long sync, int pname, int[] length, int[] values) {
        GL41C.glGetSynciv(sync, pname, length, values);
    }

    public void glGetInteger64i_v(int target, int index, long[] data) {
        GL41C.glGetInteger64i_v(target, index, data);
    }

    public void glGetBufferParameteri64v(int target, int pname, long[] params) {
        GL41C.glGetBufferParameteri64v(target, pname, params);
    }

    public void glGenSamplers(int[] samplers) {
        GL41C.glGenSamplers(samplers);
    }

    public void glDeleteSamplers(int[] samplers) {
        GL41C.glDeleteSamplers(samplers);
    }

    public void glSamplerParameteriv(int sampler, int pname, int[] param) {
        GL41C.glSamplerParameteriv(sampler, pname, param);
    }

    public void glSamplerParameterfv(int sampler, int pname, float[] param) {
        GL41C.glSamplerParameterfv(sampler, pname, param);
    }

    public void glGetSamplerParameteriv(int sampler, int pname, int[] params) {
        GL41C.glGetSamplerParameteriv(sampler, pname, params);
    }

    public void glGetSamplerParameterfv(int sampler, int pname, float[] params) {
        GL41C.glGetSamplerParameterfv(sampler, pname, params);
    }

    public void glDeleteTransformFeedbacks(int[] ids) {
        GL41C.glDeleteTransformFeedbacks(ids);
    }

    public void glGenTransformFeedbacks(int[] ids) {
        GL41C.glGenTransformFeedbacks(ids);
    }

    public void glGetProgramBinary(int program, int[] length, int[] binaryFormat, ByteBuffer binary) {
        GL41C.glGetProgramBinary(program, length, binaryFormat, binary);
    }

    public void glInvalidateFramebuffer(int target, int[] attachments) {
        GL43C.glInvalidateFramebuffer(target, attachments);
    }

    public void glInvalidateSubFramebuffer(int target, int[] attachments, int x, int y, int width, int height) {
        GL43C.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }

    public void glGetInternalformativ(int target, int internalformat, int pname, int[] params) {
        GL42C.glGetInternalformativ(target, internalformat, pname, params);
    }

    static class TIMImpl implements TexImageMultisample {

        public void glTexImage2DMultisample(@NativeType("GLenum") int target, @NativeType("GLsizei") int samples,
                @NativeType("GLint") int internalformat, @NativeType("GLsizei") int width,
                @NativeType("GLsizei") int height, @NativeType("GLboolean") boolean fixedsamplelocations) {
            GL32C.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
        }

        public void glTexImage3DMultisample(@NativeType("GLenum") int target, @NativeType("GLsizei") int samples,
                @NativeType("GLint") int internalformat, @NativeType("GLsizei") int width,
                @NativeType("GLsizei") int height, @NativeType("GLsizei") int depth,
                @NativeType("GLboolean") boolean fixedsamplelocations) {
            GL32C.glTexImage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
        }

    }

    static class PIQImpl implements ProgramInterfaceQuery {

        public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
            ARBProgramInterfaceQuery.glGetProgramInterfaceiv(program, programInterface, pname, params);
        }

        public int glGetProgramInterfacei(int program, int programInterface, int pname) {
            return ARBProgramInterfaceQuery.glGetProgramInterfacei(program, programInterface, pname);
        }

        public int glGetProgramResourceIndex(int program, int programInterface, ByteBuffer name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceIndex(program, programInterface, name);
        }

        public int glGetProgramResourceIndex(int program, int programInterface, CharSequence name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceIndex(program, programInterface, name);
        }

        public void glGetProgramResourceName(int program, int programInterface, int index, IntBuffer length,
                ByteBuffer name) {
            ARBProgramInterfaceQuery.glGetProgramResourceName(program, programInterface, index, length, name);
        }

        public String glGetProgramResourceName(int program, int programInterface, int index, int bufSize) {
            return ARBProgramInterfaceQuery.glGetProgramResourceName(program, programInterface, index, bufSize);
        }

        public String glGetProgramResourceName(int program, int programInterface, int index) {
            return ARBProgramInterfaceQuery.glGetProgramResourceName(program, programInterface, index);
        }

        public void glGetProgramResourceiv(int program, int programInterface, int index, IntBuffer props,
                IntBuffer length, IntBuffer params) {
            ARBProgramInterfaceQuery.glGetProgramResourceiv(program, programInterface, index, props, length, params);
        }

        public int glGetProgramResourceLocation(int program, int programInterface, ByteBuffer name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceLocation(program, programInterface, name);
        }

        public int glGetProgramResourceLocation(int program, int programInterface, CharSequence name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceLocation(program, programInterface, name);
        }

        public int glGetProgramResourceLocationIndex(int program, int programInterface, ByteBuffer name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceLocationIndex(program, programInterface, name);
        }

        public int glGetProgramResourceLocationIndex(int program, int programInterface, CharSequence name) {
            return ARBProgramInterfaceQuery.glGetProgramResourceLocationIndex(program, programInterface, name);
        }

        public void glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params) {
            ARBProgramInterfaceQuery.glGetProgramInterfaceiv(program, programInterface, pname, params);
        }

        public void glGetProgramResourceName(int program, int programInterface, int index, int[] length,
                ByteBuffer name) {
            ARBProgramInterfaceQuery.glGetProgramResourceName(program, programInterface, index, length, name);
        }

        public void glGetProgramResourceiv(int program, int programInterface, int index, int[] props, int[] length,
                int[] params) {
            ARBProgramInterfaceQuery.glGetProgramResourceiv(program, programInterface, index, props, length, params);
        }
    }

}
