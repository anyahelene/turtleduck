package turtleduck.gl.compat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Callback;

public interface Gles30 {

    /** Enable or disable wireframe mode
     * 
     * No effect on OpenGL ES.
     * 
     * @param enable
     * @return
     */
    boolean wireframe(boolean enable);
    /**
     * Set polygon mode to GL_LINE
     * 
     * @param face Specifies the polygons that mode applies to. Must be
     *             GL_FRONT_AND_BACK for front- and back-facing polygons.
     * @return false if wireframe mode is not supported
     * 
     */
    default boolean glPolygonModeLine(int face) {
        return false;
    }

    /**
     * Set polygon mode to GL_FILL
     * 
     * This is the default in OpenGL, and the only option in OpenGl ES.
     * 
     * @param face Specifies the polygons that mode applies to. Must be
     *             GL_FRONT_AND_BACK for front- and back-facing polygons.
     * 
     */
    default void glPolygonModeFill(int face) {

    }

    /**
     * Set polygon mode to GL_POINT
     * 
     * @param face Specifies the polygons that mode applies to. Must be
     *             GL_FRONT_AND_BACK for front- and back-facing polygons.
     * @return false if point mode is not supported
     * 
     */
    default boolean glPolygonModePoint(int face) {
        return false;
    }

    /**
     * <a target="_blank" href="http://docs.gl/es3/glActiveTexture">Reference
     * Page</a>
     */
    void glActiveTexture(int texture);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glAttachShader">Reference
     * Page</a>
     */
    void glAttachShader(int program, int shader);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindAttribLocation">Reference
     * Page</a>
     */
    void glBindAttribLocation(int program, int index, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindAttribLocation">Reference
     * Page</a>
     */
    void glBindAttribLocation(int program, int index, CharSequence name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindBuffer">Reference Page</a>
     */
    void glBindBuffer(int target, int buffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindFramebuffer">Reference
     * Page</a>
     */
    void glBindFramebuffer(int target, int framebuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindRenderbuffer">Reference
     * Page</a>
     */
    void glBindRenderbuffer(int target, int renderbuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindTexture">Reference Page</a>
     */
    void glBindTexture(int target, int texture);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBlendColor">Reference Page</a>
     */
    void glBlendColor(float red, float green, float blue, float alpha);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBlendEquation">Reference
     * Page</a>
     */
    void glBlendEquation(int mode);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glBlendEquationSeparate">Reference Page</a>
     */
    void glBlendEquationSeparate(int modeRGB, int modeAlpha);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBlendFunc">Reference Page</a>
     */
    void glBlendFunc(int sfactor, int dfactor);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBlendFuncSeparate">Reference
     * Page</a>
     */
    void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference Page</a>
     */
    void glBufferData(int target, long size, int usage);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference Page</a>
     */
    void glBufferData(int target, ByteBuffer data, int usage);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference Page</a>
     */
    void glBufferData(int target, ShortBuffer data, int usage);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference Page</a>
     */
    void glBufferData(int target, IntBuffer data, int usage);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference Page</a>
     */
    void glBufferData(int target, FloatBuffer data, int usage);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     * Page</a>
     */
    void glBufferSubData(int target, long offset, ByteBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     * Page</a>
     */
    void glBufferSubData(int target, long offset, ShortBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     * Page</a>
     */
    void glBufferSubData(int target, long offset, IntBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     * Page</a>
     */
    void glBufferSubData(int target, long offset, FloatBuffer data);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glCheckFramebufferStatus">Reference Page</a>
     */
    int glCheckFramebufferStatus(int target);

    /** <a target="_blank" href="http://docs.gl/es3/glClear">Reference Page</a> */
    void glClear(int mask);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearColor">Reference Page</a>
     */
    void glClearColor(float red, float green, float blue, float alpha);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearDepthf">Reference Page</a>
     */
    void glClearDepthf(float d);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearStencil">Reference
     * Page</a>
     */
    void glClearStencil(int s);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glColorMask">Reference Page</a>
     */
    void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCompileShader">Reference
     * Page</a>
     */
    void glCompileShader(int shader);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCompressedTexImage2D">Reference
     * Page</a>
     */
    void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int imageSize, long data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCompressedTexImage2D">Reference
     * Page</a>
     */
    void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            ByteBuffer data);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glCompressedTexSubImage2D">Reference Page</a>
     */
    void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            int imageSize, long data);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glCompressedTexSubImage2D">Reference Page</a>
     */
    void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
            ByteBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCopyTexImage2D">Reference
     * Page</a>
     */
    void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCopyTexSubImage2D">Reference
     * Page</a>
     */
    void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCreateProgram">Reference
     * Page</a>
     */
    int glCreateProgram();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCreateShader">Reference
     * Page</a>
     */
    int glCreateShader(int type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCullFace">Reference Page</a>
     */
    void glCullFace(int mode);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteBuffers">Reference
     * Page</a>
     */
    void glDeleteBuffers(IntBuffer buffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteBuffers">Reference
     * Page</a>
     */
    void glDeleteBuffers(int buffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteFramebuffers">Reference
     * Page</a>
     */
    void glDeleteFramebuffers(IntBuffer framebuffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteFramebuffers">Reference
     * Page</a>
     */
    void glDeleteFramebuffers(int framebuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteProgram">Reference
     * Page</a>
     */
    void glDeleteProgram(int program);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteRenderbuffers">Reference
     * Page</a>
     */
    void glDeleteRenderbuffers(IntBuffer renderbuffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteRenderbuffers">Reference
     * Page</a>
     */
    void glDeleteRenderbuffers(int renderbuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteShader">Reference
     * Page</a>
     */
    void glDeleteShader(int shader);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteTextures">Reference
     * Page</a>
     */
    void glDeleteTextures(IntBuffer textures);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteTextures">Reference
     * Page</a>
     */
    void glDeleteTextures(int texture);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDepthFunc">Reference Page</a>
     */
    void glDepthFunc(int func);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDepthMask">Reference Page</a>
     */
    void glDepthMask(boolean flag);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDepthRangef">Reference Page</a>
     */
    void glDepthRangef(float n, float f);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDetachShader">Reference
     * Page</a>
     */
    void glDetachShader(int program, int shader);

    /** <a target="_blank" href="http://docs.gl/es3/glDisable">Reference Page</a> */
    void glDisable(int cap);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDisableVertexAttribArray">Reference Page</a>
     */
    void glDisableVertexAttribArray(int index);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawArrays">Reference Page</a>
     */
    void glDrawArrays(int mode, int first, int count);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawElements">Reference
     * Page</a>
     */
    void glDrawElements(int mode, int count, int type, long indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawElements">Reference
     * Page</a>
     */
    void glDrawElements(int mode, int type, ByteBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawElements">Reference
     * Page</a>
     */
    void glDrawElements(int mode, ByteBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawElements">Reference
     * Page</a>
     */
    void glDrawElements(int mode, ShortBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawElements">Reference
     * Page</a>
     */
    void glDrawElements(int mode, IntBuffer indices);

    /** <a target="_blank" href="http://docs.gl/es3/glEnable">Reference Page</a> */
    void glEnable(int cap);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glEnableVertexAttribArray">Reference Page</a>
     */
    void glEnableVertexAttribArray(int index);

    /** <a target="_blank" href="http://docs.gl/es3/glFinish">Reference Page</a> */
    void glFinish();

    /** <a target="_blank" href="http://docs.gl/es3/glFlush">Reference Page</a> */
    void glFlush();

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glFramebufferRenderbuffer">Reference Page</a>
     */
    void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glFramebufferTexture2D">Reference
     * Page</a>
     */
    void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glFrontFace">Reference Page</a>
     */
    void glFrontFace(int mode);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenBuffers">Reference Page</a>
     */
    void glGenBuffers(IntBuffer buffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenBuffers">Reference Page</a>
     */
    int glGenBuffers();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenerateMipmap">Reference
     * Page</a>
     */
    void glGenerateMipmap(int target);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenFramebuffers">Reference
     * Page</a>
     */
    void glGenFramebuffers(IntBuffer framebuffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenFramebuffers">Reference
     * Page</a>
     */
    int glGenFramebuffers();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenRenderbuffers">Reference
     * Page</a>
     */
    void glGenRenderbuffers(IntBuffer renderbuffers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenRenderbuffers">Reference
     * Page</a>
     */
    int glGenRenderbuffers();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenTextures">Reference Page</a>
     */
    void glGenTextures(IntBuffer textures);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenTextures">Reference Page</a>
     */
    int glGenTextures();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveAttrib">Reference
     * Page</a>
     */
    void glGetActiveAttrib(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveAttrib">Reference
     * Page</a>
     */
    String glGetActiveAttrib(int program, int index, int bufSize, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveAttrib">Reference
     * Page</a>
     */
    String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveUniform">Reference
     * Page</a>
     */
    void glGetActiveUniform(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveUniform">Reference
     * Page</a>
     */
    String glGetActiveUniform(int program, int index, int bufSize, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveUniform">Reference
     * Page</a>
     */
    String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetAttachedShaders">Reference
     * Page</a>
     */
    void glGetAttachedShaders(int program, IntBuffer count, IntBuffer shaders);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetAttribLocation">Reference
     * Page</a>
     */
    int glGetAttribLocation(int program, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetAttribLocation">Reference
     * Page</a>
     */
    int glGetAttribLocation(int program, CharSequence name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBooleanv">Reference Page</a>
     */
    void glGetBooleanv(int pname, ByteBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBooleanv">Reference Page</a>
     */
    boolean glGetBoolean(int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferParameter">Reference
     * Page</a>
     */
    void glGetBufferParameteriv(int target, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferParameter">Reference
     * Page</a>
     */
    int glGetBufferParameteri(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetError">Reference Page</a>
     */
    int glGetError();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetFloatv">Reference Page</a>
     */
    void glGetFloatv(int pname, FloatBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetFloatv">Reference Page</a>
     */
    float glGetFloat(int pname);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetFramebufferAttachmentParameter">Reference Page</a>
     */
    void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetFramebufferAttachmentParameter">Reference Page</a>
     */
    int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetIntegerv">Reference Page</a>
     */
    void glGetIntegerv(int pname, IntBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetIntegerv">Reference Page</a>
     */
    int glGetInteger(int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgram">Reference Page</a>
     */
    void glGetProgramiv(int program, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgram">Reference Page</a>
     */
    int glGetProgrami(int program, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgramInfoLog">Reference
     * Page</a>
     */
    void glGetProgramInfoLog(int program, IntBuffer length, ByteBuffer infoLog);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgramInfoLog">Reference
     * Page</a>
     */
    String glGetProgramInfoLog(int program, int bufSize);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgramInfoLog">Reference
     * Page</a>
     */
    String glGetProgramInfoLog(int program);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetRenderbufferParameter">Reference Page</a>
     */
    void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetRenderbufferParameter">Reference Page</a>
     */
    int glGetRenderbufferParameteri(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShader">Reference Page</a>
     */
    void glGetShaderiv(int shader, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShader">Reference Page</a>
     */
    int glGetShaderi(int shader, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderInfoLog">Reference
     * Page</a>
     */
    void glGetShaderInfoLog(int shader, IntBuffer length, ByteBuffer infoLog);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderInfoLog">Reference
     * Page</a>
     */
    String glGetShaderInfoLog(int shader, int bufSize);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderInfoLog">Reference
     * Page</a>
     */
    String glGetShaderInfoLog(int shader);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetShaderPrecisionFormat">Reference Page</a>
     */
    void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderSource">Reference
     * Page</a>
     */
    void glGetShaderSource(int shader, IntBuffer length, ByteBuffer source);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderSource">Reference
     * Page</a>
     */
    String glGetShaderSource(int shader, int bufSize);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetShaderSource">Reference
     * Page</a>
     */
    String glGetShaderSource(int shader);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetString">Reference Page</a>
     */
    String glGetString(int name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     * Page</a>
     */
    void glGetTexParameterfv(int target, int pname, FloatBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     * Page</a>
     */
    float glGetTexParameterf(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     * Page</a>
     */
    void glGetTexParameteriv(int target, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     * Page</a>
     */
    int glGetTexParameteri(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    void glGetUniformfv(int program, int location, FloatBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    float glGetUniformf(int program, int location);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    void glGetUniformiv(int program, int location, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    int glGetUniformi(int program, int location);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniformLocation">Reference
     * Page</a>
     */
    int glGetUniformLocation(int program, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniformLocation">Reference
     * Page</a>
     */
    int glGetUniformLocation(int program, CharSequence name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    void glGetVertexAttribfv(int index, int pname, FloatBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    void glGetVertexAttribiv(int index, int pname, IntBuffer params);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetVertexAttribPointerv">Reference Page</a>
     */
    void glGetVertexAttribPointerv(int index, int pname, PointerBuffer pointer);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetVertexAttribPointerv">Reference Page</a>
     */
    long glGetVertexAttribPointer(int index, int pname);

    /** <a target="_blank" href="http://docs.gl/es3/glHint">Reference Page</a> */
    void glHint(int target, int mode);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsBuffer">Reference Page</a>
     */
    boolean glIsBuffer(int buffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsEnabled">Reference Page</a>
     */
    boolean glIsEnabled(int cap);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsFramebuffer">Reference
     * Page</a>
     */
    boolean glIsFramebuffer(int framebuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsProgram">Reference Page</a>
     */
    boolean glIsProgram(int program);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsRenderbuffer">Reference
     * Page</a>
     */
    boolean glIsRenderbuffer(int renderbuffer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsShader">Reference Page</a>
     */
    boolean glIsShader(int shader);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsTexture">Reference Page</a>
     */
    boolean glIsTexture(int texture);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glLineWidth">Reference Page</a>
     */
    void glLineWidth(float width);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glLinkProgram">Reference Page</a>
     */
    void glLinkProgram(int program);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glPixelStorei">Reference Page</a>
     */
    void glPixelStorei(int pname, int param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glPolygonOffset">Reference
     * Page</a>
     */
    void glPolygonOffset(float factor, float units);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glReleaseShaderCompiler">Reference Page</a>
     */
    void glReleaseShaderCompiler();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glRenderbufferStorage">Reference
     * Page</a>
     */
    void glRenderbufferStorage(int target, int internalformat, int width, int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glSampleCoverage">Reference
     * Page</a>
     */
    void glSampleCoverage(float value, boolean invert);

    /** <a target="_blank" href="http://docs.gl/es3/glScissor">Reference Page</a> */
    void glScissor(int x, int y, int width, int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glShaderBinary">Reference
     * Page</a>
     */
    void glShaderBinary(IntBuffer shaders, int binaryformat, ByteBuffer binary);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glShaderSource">Reference
     * Page</a>
     */
    void glShaderSource(int shader, PointerBuffer string, IntBuffer length);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glShaderSource">Reference
     * Page</a>
     */
    void glShaderSource(int shader, CharSequence... string);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glShaderSource">Reference
     * Page</a>
     */
    void glShaderSource(int shader, CharSequence string);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilFunc">Reference Page</a>
     */
    void glStencilFunc(int func, int ref, int mask);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilFuncSeparate">Reference
     * Page</a>
     */
    void glStencilFuncSeparate(int face, int func, int ref, int mask);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilMask">Reference Page</a>
     */
    void glStencilMask(int mask);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilMaskSeparate">Reference
     * Page</a>
     */
    void glStencilMaskSeparate(int face, int mask);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilOp">Reference Page</a>
     */
    void glStencilOp(int fail, int zfail, int zpass);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glStencilOpSeparate">Reference
     * Page</a>
     */
    void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ByteBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, long pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, ShortBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, IntBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, FloatBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexParameterf">Reference
     * Page</a>
     */
    void glTexParameterf(int target, int pname, float param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexParameter">Reference
     * Page</a>
     */
    void glTexParameterfv(int target, int pname, FloatBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexParameteri">Reference
     * Page</a>
     */
    void glTexParameteri(int target, int pname, int param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexParameter">Reference
     * Page</a>
     */
    void glTexParameteriv(int target, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     * Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            ByteBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     * Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            long pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     * Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            ShortBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     * Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            IntBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     * Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            FloatBuffer pixels);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1f(int location, float v0);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1fv(int location, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1i(int location, int v0);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1iv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2f(int location, float v0, float v1);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2fv(int location, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2i(int location, int v0, int v1);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2iv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3f(int location, float v0, float v1, float v2);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3fv(int location, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3i(int location, int v0, int v1, int v2);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3iv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4f(int location, float v0, float v1, float v2, float v3);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4fv(int location, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4i(int location, int v0, int v1, int v2, int v3);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4iv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix2fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix3fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glUseProgram">Reference Page</a>
     */
    void glUseProgram(int program);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glValidateProgram">Reference
     * Page</a>
     */
    void glValidateProgram(int program);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib1f(int index, float x);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib1fv(int index, FloatBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib2f(int index, float x, float y);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib2fv(int index, FloatBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib3f(int index, float x, float y, float z);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib3fv(int index, FloatBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib4f(int index, float x, float y, float z, float w);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttrib4fv(int index, FloatBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribPointer">Reference
     * Page</a>
     */
    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribPointer">Reference
     * Page</a>
     */
    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribPointer">Reference
     * Page</a>
     */
    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ShortBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribPointer">Reference
     * Page</a>
     */
    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, IntBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribPointer">Reference
     * Page</a>
     */
    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, FloatBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glViewport">Reference Page</a>
     */
    void glViewport(int x, int y, int width, int height);

    /**
     * Array version of: {@link #glBufferData BufferData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference
     *      Page</a>
     */
    void glBufferData(int target, short[] data, int usage);

    /**
     * Array version of: {@link #glBufferData BufferData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference
     *      Page</a>
     */
    void glBufferData(int target, int[] data, int usage);

    /**
     * Array version of: {@link #glBufferData BufferData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferData">Reference
     *      Page</a>
     */
    void glBufferData(int target, float[] data, int usage);

    /**
     * Array version of: {@link #glBufferSubData BufferSubData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     *      Page</a>
     */
    void glBufferSubData(int target, long offset, short[] data);

    /**
     * Array version of: {@link #glBufferSubData BufferSubData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     *      Page</a>
     */
    void glBufferSubData(int target, long offset, int[] data);

    /**
     * Array version of: {@link #glBufferSubData BufferSubData}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glBufferSubData">Reference
     *      Page</a>
     */
    void glBufferSubData(int target, long offset, float[] data);

    /**
     * Array version of: {@link #glDeleteBuffers DeleteBuffers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glDeleteBuffers">Reference
     *      Page</a>
     */
    void glDeleteBuffers(int[] buffers);

    /**
     * Array version of: {@link #glDeleteFramebuffers DeleteFramebuffers}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glDeleteFramebuffers">Reference Page</a>
     */
    void glDeleteFramebuffers(int[] framebuffers);

    /**
     * Array version of: {@link #glDeleteRenderbuffers DeleteRenderbuffers}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glDeleteRenderbuffers">Reference Page</a>
     */
    void glDeleteRenderbuffers(int[] renderbuffers);

    /**
     * Array version of: {@link #glDeleteTextures DeleteTextures}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glDeleteTextures">Reference
     *      Page</a>
     */
    void glDeleteTextures(int[] textures);

    /**
     * Array version of: {@link #glGenBuffers GenBuffers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenBuffers">Reference
     *      Page</a>
     */
    void glGenBuffers(int[] buffers);

    /**
     * Array version of: {@link #glGenFramebuffers GenFramebuffers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenFramebuffers">Reference
     *      Page</a>
     */
    void glGenFramebuffers(int[] framebuffers);

    /**
     * Array version of: {@link #glGenRenderbuffers GenRenderbuffers}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGenRenderbuffers">Reference Page</a>
     */
    void glGenRenderbuffers(int[] renderbuffers);

    /**
     * Array version of: {@link #glGenTextures GenTextures}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenTextures">Reference
     *      Page</a>
     */
    void glGenTextures(int[] textures);

    /**
     * Array version of: {@link #glGetActiveAttrib GetActiveAttrib}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetActiveAttrib">Reference
     *      Page</a>
     */
    void glGetActiveAttrib(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name);

    /**
     * Array version of: {@link #glGetActiveUniform GetActiveUniform}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetActiveUniform">Reference Page</a>
     */
    void glGetActiveUniform(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name);

    /**
     * Array version of: {@link #glGetAttachedShaders GetAttachedShaders}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetAttachedShaders">Reference Page</a>
     */
    void glGetAttachedShaders(int program, int[] count, int[] shaders);

    /**
     * Array version of: {@link #glGetBufferParameteriv GetBufferParameteriv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetBufferParameter">Reference Page</a>
     */
    void glGetBufferParameteriv(int target, int pname, int[] params);

    /**
     * Array version of: {@link #glGetFloatv GetFloatv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetFloatv">Reference
     *      Page</a>
     */
    void glGetFloatv(int pname, float[] data);

    /**
     * Array version of: {@link #glGetFramebufferAttachmentParameteriv
     * GetFramebufferAttachmentParameteriv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetFramebufferAttachmentParameter">Reference
     *      Page</a>
     */
    void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params);

    /**
     * Array version of: {@link #glGetIntegerv GetIntegerv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetIntegerv">Reference
     *      Page</a>
     */
    void glGetIntegerv(int pname, int[] data);

    /**
     * Array version of: {@link #glGetProgramiv GetProgramiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetProgram">Reference
     *      Page</a>
     */
    void glGetProgramiv(int program, int pname, int[] params);

    /**
     * Array version of: {@link #glGetProgramInfoLog GetProgramInfoLog}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetProgramInfoLog">Reference Page</a>
     */
    void glGetProgramInfoLog(int program, int[] length, ByteBuffer infoLog);

    /**
     * Array version of: {@link #glGetRenderbufferParameteriv
     * GetRenderbufferParameteriv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetRenderbufferParameter">Reference Page</a>
     */
    void glGetRenderbufferParameteriv(int target, int pname, int[] params);

    /**
     * Array version of: {@link #glGetShaderiv GetShaderiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetShader">Reference
     *      Page</a>
     */
    void glGetShaderiv(int shader, int pname, int[] params);

    /**
     * Array version of: {@link #glGetShaderInfoLog GetShaderInfoLog}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetShaderInfoLog">Reference Page</a>
     */
    void glGetShaderInfoLog(int shader, int[] length, ByteBuffer infoLog);

    /**
     * Array version of: {@link #glGetShaderPrecisionFormat
     * GetShaderPrecisionFormat}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetShaderPrecisionFormat">Reference Page</a>
     */
    void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int[] precision);

    /**
     * Array version of: {@link #glGetShaderSource GetShaderSource}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetShaderSource">Reference
     *      Page</a>
     */
    void glGetShaderSource(int shader, int[] length, ByteBuffer source);

    /**
     * Array version of: {@link #glGetTexParameterfv GetTexParameterfv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     *      Page</a>
     */
    void glGetTexParameterfv(int target, int pname, float[] params);

    /**
     * Array version of: {@link #glGetTexParameteriv GetTexParameteriv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetTexParameter">Reference
     *      Page</a>
     */
    void glGetTexParameteriv(int target, int pname, int[] params);

    /**
     * Array version of: {@link #glGetUniformfv GetUniformfv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference
     *      Page</a>
     */
    void glGetUniformfv(int program, int location, float[] params);

    /**
     * Array version of: {@link #glGetUniformiv GetUniformiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference
     *      Page</a>
     */
    void glGetUniformiv(int program, int location, int[] params);

    /**
     * Array version of: {@link #glGetVertexAttribfv GetVertexAttribfv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     *      Page</a>
     */
    void glGetVertexAttribfv(int index, int pname, float[] params);

    /**
     * Array version of: {@link #glGetVertexAttribiv GetVertexAttribiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     *      Page</a>
     */
    void glGetVertexAttribiv(int index, int pname, int[] params);

    /**
     * Array version of: {@link #glReadPixels ReadPixels}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference
     *      Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, short[] pixels);

    /**
     * Array version of: {@link #glReadPixels ReadPixels}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference
     *      Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, int[] pixels);

    /**
     * Array version of: {@link #glReadPixels ReadPixels}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glReadPixels">Reference
     *      Page</a>
     */
    void glReadPixels(int x, int y, int width, int height, int format, int type, float[] pixels);

    /**
     * Array version of: {@link #glShaderBinary ShaderBinary}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glShaderBinary">Reference
     *      Page</a>
     */
    void glShaderBinary(int[] shaders, int binaryformat, ByteBuffer binary);

    /**
     * Array version of: {@link #glShaderSource ShaderSource}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glShaderSource">Reference
     *      Page</a>
     */
    void glShaderSource(int shader, PointerBuffer string, int[] length);

    /**
     * Array version of: {@link #glTexImage2D TexImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference
     *      Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, short[] pixels);

    /**
     * Array version of: {@link #glTexImage2D TexImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference
     *      Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, int[] pixels);

    /**
     * Array version of: {@link #glTexImage2D TexImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage2D">Reference
     *      Page</a>
     */
    void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, float[] pixels);

    /**
     * Array version of: {@link #glTexParameterfv TexParameterfv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexParameter">Reference
     *      Page</a>
     */
    void glTexParameterfv(int target, int pname, float[] params);

    /**
     * Array version of: {@link #glTexParameteriv TexParameteriv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexParameter">Reference
     *      Page</a>
     */
    void glTexParameteriv(int target, int pname, int[] params);

    /**
     * Array version of: {@link #glTexSubImage2D TexSubImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     *      Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            short[] pixels);

    /**
     * Array version of: {@link #glTexSubImage2D TexSubImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     *      Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            int[] pixels);

    /**
     * Array version of: {@link #glTexSubImage2D TexSubImage2D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage2D">Reference
     *      Page</a>
     */
    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
            float[] pixels);

    /**
     * Array version of: {@link #glUniform1fv Uniform1fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform1fv(int location, float[] value);

    /**
     * Array version of: {@link #glUniform1iv Uniform1iv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform1iv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform2fv Uniform2fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform2fv(int location, float[] value);

    /**
     * Array version of: {@link #glUniform2iv Uniform2iv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform2iv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform3fv Uniform3fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform3fv(int location, float[] value);

    /**
     * Array version of: {@link #glUniform3iv Uniform3iv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform3iv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform4fv Uniform4fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform4fv(int location, float[] value);

    /**
     * Array version of: {@link #glUniform4iv Uniform4iv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform4iv(int location, int[] value);

    /**
     * Array version of: {@link #glUniformMatrix2fv UniformMatrix2fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix2fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix3fv UniformMatrix3fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix3fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix4fv UniformMatrix4fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix4fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glVertexAttrib1fv VertexAttrib1fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttrib1fv(int index, float[] v);

    /**
     * Array version of: {@link #glVertexAttrib2fv VertexAttrib2fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttrib2fv(int index, float[] v);

    /**
     * Array version of: {@link #glVertexAttrib3fv VertexAttrib3fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttrib3fv(int index, float[] v);

    /**
     * Array version of: {@link #glVertexAttrib4fv VertexAttrib4fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttrib4fv(int index, float[] v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glReadBuffer">Reference Page</a>
     */
    void glReadBuffer(int src);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawRangeElements">Reference
     * Page</a>
     */
    void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawRangeElements">Reference
     * Page</a>
     */
    void glDrawRangeElements(int mode, int start, int end, int type, ByteBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawRangeElements">Reference
     * Page</a>
     */
    void glDrawRangeElements(int mode, int start, int end, ByteBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawRangeElements">Reference
     * Page</a>
     */
    void glDrawRangeElements(int mode, int start, int end, ShortBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawRangeElements">Reference
     * Page</a>
     */
    void glDrawRangeElements(int mode, int start, int end, IntBuffer indices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ByteBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, long pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, ShortBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, IntBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, FloatBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     * Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, ByteBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     * Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, long pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     * Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, ShortBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     * Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, IntBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     * Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, FloatBuffer pixels);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCopyTexSubImage3D">Reference
     * Page</a>
     */
    void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
            int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCompressedTexImage3D">Reference
     * Page</a>
     */
    void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int imageSize, long data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCompressedTexImage3D">Reference
     * Page</a>
     */
    void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            ByteBuffer data);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glCompressedTexSubImage3D">Reference Page</a>
     */
    void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, int imageSize, long data);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glCompressedTexSubImage3D">Reference Page</a>
     */
    void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
            int depth, int format, ByteBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenQueries">Reference Page</a>
     */
    void glGenQueries(IntBuffer ids);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenQueries">Reference Page</a>
     */
    int glGenQueries();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteQueries">Reference
     * Page</a>
     */
    void glDeleteQueries(IntBuffer ids);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteQueries">Reference
     * Page</a>
     */
    void glDeleteQueries(int id);

    /** <a target="_blank" href="http://docs.gl/es3/glIsQuery">Reference Page</a> */
    boolean glIsQuery(int id);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBeginQuery">Reference Page</a>
     */
    void glBeginQuery(int target, int id);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glEndQuery">Reference Page</a>
     */
    void glEndQuery(int target);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetQuery">Reference Page</a>
     */
    void glGetQueryiv(int target, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetQuery">Reference Page</a>
     */
    int glGetQueryi(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetQueryObject">Reference
     * Page</a>
     */
    void glGetQueryObjectuiv(int id, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetQueryObject">Reference
     * Page</a>
     */
    int glGetQueryObjectui(int id, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glUnmapBuffer">Reference Page</a>
     */
    boolean glUnmapBuffer(int target);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferPointerv">Reference
     * Page</a>
     */
    void glGetBufferPointerv(int target, int pname, PointerBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferPointerv">Reference
     * Page</a>
     */
    long glGetBufferPointer(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawBuffers">Reference Page</a>
     */
    void glDrawBuffers(IntBuffer bufs);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawBuffers">Reference Page</a>
     */
    void glDrawBuffers(int buf);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawBuffers">Reference Page</a>
     */
    default void glDrawBuffer(int buf) {
        glDrawBuffers(buf);
    }

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix2x3fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix3x2fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix2x4fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix4x2fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix3x4fv(int location, boolean transpose, FloatBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniformMatrix4x3fv(int location, boolean transpose, FloatBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBlitFramebuffer">Reference
     * Page</a>
     */
    void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
            int mask, int filter);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glRenderbufferStorageMultisample">Reference Page</a>
     */
    void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glFramebufferTextureLayer">Reference Page</a>
     */
    void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glMapBufferRange">Reference
     * Page</a>
     */
    ByteBuffer glMapBufferRange(int target, long offset, long length, int access);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glMapBufferRange">Reference
     * Page</a>
     */
    ByteBuffer glMapBufferRange(int target, long offset, long length, int access, ByteBuffer old_buffer);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glFlushMappedBufferRange">Reference Page</a>
     */
    void glFlushMappedBufferRange(int target, long offset, long length);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindVertexArray">Reference
     * Page</a>
     */
    void glBindVertexArray(int array);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteVertexArrays">Reference
     * Page</a>
     */
    void glDeleteVertexArrays(IntBuffer arrays);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteVertexArrays">Reference
     * Page</a>
     */
    void glDeleteVertexArrays(int array);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenVertexArrays">Reference
     * Page</a>
     */
    void glGenVertexArrays(IntBuffer arrays);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenVertexArrays">Reference
     * Page</a>
     */
    int glGenVertexArrays();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsVertexArray">Reference
     * Page</a>
     */
    boolean glIsVertexArray(int array);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetIntegeri_v">Reference
     * Page</a>
     */
    void glGetIntegeri_v(int target, int index, IntBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetIntegeri_v">Reference
     * Page</a>
     */
    int glGetIntegeri(int target, int index);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glBeginTransformFeedback">Reference Page</a>
     */
    void glBeginTransformFeedback(int primitiveMode);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glEndTransformFeedback">Reference
     * Page</a>
     */
    void glEndTransformFeedback();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindBufferRange">Reference
     * Page</a>
     */
    void glBindBufferRange(int target, int index, int buffer, long offset, long size);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindBufferBase">Reference
     * Page</a>
     */
    void glBindBufferBase(int target, int index, int buffer);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glTransformFeedbackVaryings">Reference Page</a>
     */
    void glTransformFeedbackVaryings(int program, PointerBuffer varyings, int bufferMode);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glTransformFeedbackVaryings">Reference Page</a>
     */
    void glTransformFeedbackVaryings(int program, CharSequence[] varyings, int bufferMode);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glTransformFeedbackVaryings">Reference Page</a>
     */
    void glTransformFeedbackVaryings(int program, CharSequence varying, int bufferMode);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetTransformFeedbackVarying">Reference Page</a>
     */
    void glGetTransformFeedbackVarying(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type,
            ByteBuffer name);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetTransformFeedbackVarying">Reference Page</a>
     */
    String glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetTransformFeedbackVarying">Reference Page</a>
     */
    String glGetTransformFeedbackVarying(int program, int index, IntBuffer size, IntBuffer type);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribIPointer">Reference
     * Page</a>
     */
    void glVertexAttribIPointer(int index, int size, int type, int stride, ByteBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribIPointer">Reference
     * Page</a>
     */
    void glVertexAttribIPointer(int index, int size, int type, int stride, long pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribIPointer">Reference
     * Page</a>
     */
    void glVertexAttribIPointer(int index, int size, int type, int stride, ShortBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribIPointer">Reference
     * Page</a>
     */
    void glVertexAttribIPointer(int index, int size, int type, int stride, IntBuffer pointer);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    void glGetVertexAttribIiv(int index, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    int glGetVertexAttribIi(int index, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    void glGetVertexAttribIuiv(int index, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     * Page</a>
     */
    int glGetVertexAttribIui(int index, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttribI4i(int index, int x, int y, int z, int w);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttribI4ui(int index, int x, int y, int z, int w);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttribI4iv(int index, IntBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     * Page</a>
     */
    void glVertexAttribI4uiv(int index, IntBuffer v);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    void glGetUniformuiv(int program, int location, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference Page</a>
     */
    int glGetUniformui(int program, int location);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetFragDataLocation">Reference
     * Page</a>
     */
    int glGetFragDataLocation(int program, ByteBuffer name);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetFragDataLocation">Reference
     * Page</a>
     */
    int glGetFragDataLocation(int program, CharSequence name);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1ui(int location, int v0);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2ui(int location, int v0, int v1);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3ui(int location, int v0, int v1, int v2);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4ui(int location, int v0, int v1, int v2, int v3);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform1uiv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform2uiv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform3uiv(int location, IntBuffer value);

    /** <a target="_blank" href="http://docs.gl/es3/glUniform">Reference Page</a> */
    void glUniform4uiv(int location, IntBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference Page</a>
     */
    void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference Page</a>
     */
    void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference Page</a>
     */
    void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClearBufferfi">Reference
     * Page</a>
     */
    void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetStringi">Reference Page</a>
     */
    String glGetStringi(int name, int index);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glCopyBufferSubData">Reference
     * Page</a>
     */
    void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniformIndices">Reference
     * Page</a>
     */
    void glGetUniformIndices(int program, PointerBuffer uniformNames, IntBuffer uniformIndices);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetActiveUniforms">Reference
     * Page</a>
     */
    void glGetActiveUniformsiv(int program, IntBuffer uniformIndices, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniformBlockIndex">Reference
     * Page</a>
     */
    int glGetUniformBlockIndex(int program, ByteBuffer uniformBlockName);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetUniformBlockIndex">Reference
     * Page</a>
     */
    int glGetUniformBlockIndex(int program, CharSequence uniformBlockName);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetActiveUniformBlock">Reference Page</a>
     */
    void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetActiveUniformBlock">Reference Page</a>
     */
    int glGetActiveUniformBlocki(int program, int uniformBlockIndex, int pname);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetActiveUniformBlockName">Reference Page</a>
     */
    void glGetActiveUniformBlockName(int program, int uniformBlockIndex, IntBuffer length, ByteBuffer uniformBlockName);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetActiveUniformBlockName">Reference Page</a>
     */
    String glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGetActiveUniformBlockName">Reference Page</a>
     */
    String glGetActiveUniformBlockName(int program, int uniformBlockIndex);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glUniformBlockBinding">Reference
     * Page</a>
     */
    void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDrawArraysInstanced">Reference
     * Page</a>
     */
    void glDrawArraysInstanced(int mode, int first, int count, int instancecount);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDrawElementsInstanced">Reference Page</a>
     */
    void glDrawElementsInstanced(int mode, int count, int type, long indices, int instancecount);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDrawElementsInstanced">Reference Page</a>
     */
    void glDrawElementsInstanced(int mode, int type, ByteBuffer indices, int instancecount);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDrawElementsInstanced">Reference Page</a>
     */
    void glDrawElementsInstanced(int mode, ByteBuffer indices, int instancecount);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDrawElementsInstanced">Reference Page</a>
     */
    void glDrawElementsInstanced(int mode, ShortBuffer indices, int instancecount);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDrawElementsInstanced">Reference Page</a>
     */
    void glDrawElementsInstanced(int mode, IntBuffer indices, int instancecount);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glFenceSync">Reference Page</a>
     */
    long glFenceSync(int condition, int flags);

    /** <a target="_blank" href="http://docs.gl/es3/glIsSync">Reference Page</a> */
    boolean glIsSync(long sync);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteSync">Reference Page</a>
     */
    void glDeleteSync(long sync);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glClientWaitSync">Reference
     * Page</a>
     */
    int glClientWaitSync(long sync, int flags, long timeout);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glWaitSync">Reference Page</a>
     */
    void glWaitSync(long sync, int flags, long timeout);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInteger64v">Reference
     * Page</a>
     */
    void glGetInteger64v(int pname, LongBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInteger64v">Reference
     * Page</a>
     */
    long glGetInteger64(int pname);

    /** <a target="_blank" href="http://docs.gl/es3/glGetSync">Reference Page</a> */
    void glGetSynciv(long sync, int pname, IntBuffer length, IntBuffer values);

    /** <a target="_blank" href="http://docs.gl/es3/glGetSync">Reference Page</a> */
    int glGetSynci(long sync, int pname, IntBuffer length);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInteger">Reference Page</a>
     */
    void glGetInteger64i_v(int target, int index, LongBuffer data);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInteger">Reference Page</a>
     */
    long glGetInteger64i(int target, int index);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferParameter">Reference
     * Page</a>
     */
    void glGetBufferParameteri64v(int target, int pname, LongBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetBufferParameter">Reference
     * Page</a>
     */
    long glGetBufferParameteri64(int target, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenSamplers">Reference Page</a>
     */
    void glGenSamplers(IntBuffer samplers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGenSamplers">Reference Page</a>
     */
    int glGenSamplers();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteSamplers">Reference
     * Page</a>
     */
    void glDeleteSamplers(IntBuffer samplers);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glDeleteSamplers">Reference
     * Page</a>
     */
    void glDeleteSamplers(int sampler);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsSampler">Reference Page</a>
     */
    boolean glIsSampler(int sampler);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glBindSampler">Reference Page</a>
     */
    void glBindSampler(int unit, int sampler);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glSamplerParameteri">Reference
     * Page</a>
     */
    void glSamplerParameteri(int sampler, int pname, int param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glSamplerParameter">Reference
     * Page</a>
     */
    void glSamplerParameteriv(int sampler, int pname, IntBuffer param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glSamplerParameterf">Reference
     * Page</a>
     */
    void glSamplerParameterf(int sampler, int pname, float param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glSamplerParameter">Reference
     * Page</a>
     */
    void glSamplerParameterfv(int sampler, int pname, FloatBuffer param);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetSamplerParameter">Reference
     * Page</a>
     */
    void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetSamplerParameter">Reference
     * Page</a>
     */
    int glGetSamplerParameteri(int sampler, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetSamplerParameter">Reference
     * Page</a>
     */
    void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetSamplerParameter">Reference
     * Page</a>
     */
    float glGetSamplerParameterf(int sampler, int pname);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glVertexAttribDivisor">Reference
     * Page</a>
     */
    void glVertexAttribDivisor(int index, int divisor);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glBindTransformFeedback">Reference Page</a>
     */
    void glBindTransformFeedback(int target, int id);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDeleteTransformFeedbacks">Reference Page</a>
     */
    void glDeleteTransformFeedbacks(IntBuffer ids);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glDeleteTransformFeedbacks">Reference Page</a>
     */
    void glDeleteTransformFeedbacks(int id);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGenTransformFeedbacks">Reference Page</a>
     */
    void glGenTransformFeedbacks(IntBuffer ids);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glGenTransformFeedbacks">Reference Page</a>
     */
    int glGenTransformFeedbacks();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glIsTransformFeedback">Reference
     * Page</a>
     */
    boolean glIsTransformFeedback(int id);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glPauseTransformFeedback">Reference Page</a>
     */
    void glPauseTransformFeedback();

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glResumeTransformFeedback">Reference Page</a>
     */
    void glResumeTransformFeedback();

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetProgramBinary">Reference
     * Page</a>
     */
    void glGetProgramBinary(int program, IntBuffer length, IntBuffer binaryFormat, ByteBuffer binary);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glProgramBinary">Reference
     * Page</a>
     */
    void glProgramBinary(int program, int binaryFormat, ByteBuffer binary);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glProgramParameteri">Reference
     * Page</a>
     */
    void glProgramParameteri(int program, int pname, int value);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glInvalidateFramebuffer">Reference Page</a>
     */
    void glInvalidateFramebuffer(int target, IntBuffer attachments);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glInvalidateFramebuffer">Reference Page</a>
     */
    void glInvalidateFramebuffer(int target, int attachment);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glInvalidateSubFramebuffer">Reference Page</a>
     */
    void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height);

    /**
     * <a target="_blank" href=
     * "http://docs.gl/es3/glInvalidateSubFramebuffer">Reference Page</a>
     */
    void glInvalidateSubFramebuffer(int target, int attachment, int x, int y, int width, int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexStorage2D">Reference
     * Page</a>
     */
    void glTexStorage2D(int target, int levels, int internalformat, int width, int height);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glTexStorage3D">Reference
     * Page</a>
     */
    void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInternalformat">Reference
     * Page</a>
     */
    void glGetInternalformativ(int target, int internalformat, int pname, IntBuffer params);

    /**
     * <a target="_blank" href="http://docs.gl/es3/glGetInternalformat">Reference
     * Page</a>
     */
    int glGetInternalformati(int target, int internalformat, int pname);

    /**
     * Array version of: {@link #glTexImage3D TexImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference
     *      Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, short[] pixels);

    /**
     * Array version of: {@link #glTexImage3D TexImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference
     *      Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, int[] pixels);

    /**
     * Array version of: {@link #glTexImage3D TexImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexImage3D">Reference
     *      Page</a>
     */
    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
            int format, int type, float[] pixels);

    /**
     * Array version of: {@link #glTexSubImage3D TexSubImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     *      Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, short[] pixels);

    /**
     * Array version of: {@link #glTexSubImage3D TexSubImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     *      Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, int[] pixels);

    /**
     * Array version of: {@link #glTexSubImage3D TexSubImage3D}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glTexSubImage3D">Reference
     *      Page</a>
     */
    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
            int format, int type, float[] pixels);

    /**
     * Array version of: {@link #glGenQueries GenQueries}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenQueries">Reference
     *      Page</a>
     */
    void glGenQueries(int[] ids);

    /**
     * Array version of: {@link #glDeleteQueries DeleteQueries}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glDeleteQueries">Reference
     *      Page</a>
     */
    void glDeleteQueries(int[] ids);

    /**
     * Array version of: {@link #glGetQueryiv GetQueryiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetQuery">Reference
     *      Page</a>
     */
    void glGetQueryiv(int target, int pname, int[] params);

    /**
     * Array version of: {@link #glGetQueryObjectuiv GetQueryObjectuiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetQueryObject">Reference
     *      Page</a>
     */
    void glGetQueryObjectuiv(int id, int pname, int[] params);

    /**
     * Array version of: {@link #glDrawBuffers DrawBuffers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glDrawBuffers">Reference
     *      Page</a>
     */
    void glDrawBuffers(int[] bufs);

    /**
     * Array version of: {@link #glUniformMatrix2x3fv UniformMatrix2x3fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix2x3fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix3x2fv UniformMatrix3x2fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix3x2fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix2x4fv UniformMatrix2x4fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix2x4fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix4x2fv UniformMatrix4x2fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix4x2fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix3x4fv UniformMatrix3x4fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix3x4fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glUniformMatrix4x3fv UniformMatrix4x3fv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniformMatrix4x3fv(int location, boolean transpose, float[] value);

    /**
     * Array version of: {@link #glDeleteVertexArrays DeleteVertexArrays}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glDeleteVertexArrays">Reference Page</a>
     */
    void glDeleteVertexArrays(int[] arrays);

    /**
     * Array version of: {@link #glGenVertexArrays GenVertexArrays}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenVertexArrays">Reference
     *      Page</a>
     */
    void glGenVertexArrays(int[] arrays);

    /**
     * Array version of: {@link #glGetIntegeri_v GetIntegeri_v}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetIntegeri_v">Reference
     *      Page</a>
     */
    void glGetIntegeri_v(int target, int index, int[] data);

    /**
     * Array version of: {@link #glGetTransformFeedbackVarying
     * GetTransformFeedbackVarying}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetTransformFeedbackVarying">Reference Page</a>
     */
    void glGetTransformFeedbackVarying(int program, int index, int[] length, int[] size, int[] type, ByteBuffer name);

    /**
     * Array version of: {@link #glGetVertexAttribIiv GetVertexAttribIiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     *      Page</a>
     */
    void glGetVertexAttribIiv(int index, int pname, int[] params);

    /**
     * Array version of: {@link #glGetVertexAttribIuiv GetVertexAttribIuiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetVertexAttrib">Reference
     *      Page</a>
     */
    void glGetVertexAttribIuiv(int index, int pname, int[] params);

    /**
     * Array version of: {@link #glVertexAttribI4iv VertexAttribI4iv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttribI4iv(int index, int[] v);

    /**
     * Array version of: {@link #glVertexAttribI4uiv VertexAttribI4uiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glVertexAttrib">Reference
     *      Page</a>
     */
    void glVertexAttribI4uiv(int index, int[] v);

    /**
     * Array version of: {@link #glGetUniformuiv GetUniformuiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetUniform">Reference
     *      Page</a>
     */
    void glGetUniformuiv(int program, int location, int[] params);

    /**
     * Array version of: {@link #glUniform1uiv Uniform1uiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform1uiv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform2uiv Uniform2uiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform2uiv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform3uiv Uniform3uiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform3uiv(int location, int[] value);

    /**
     * Array version of: {@link #glUniform4uiv Uniform4uiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glUniform">Reference
     *      Page</a>
     */
    void glUniform4uiv(int location, int[] value);

    /**
     * Array version of: {@link #glClearBufferiv ClearBufferiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference
     *      Page</a>
     */
    void glClearBufferiv(int buffer, int drawbuffer, int[] value);

    /**
     * Array version of: {@link #glClearBufferuiv ClearBufferuiv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference
     *      Page</a>
     */
    void glClearBufferuiv(int buffer, int drawbuffer, int[] value);

    /**
     * Array version of: {@link #glClearBufferfv ClearBufferfv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glClearBuffer">Reference
     *      Page</a>
     */
    void glClearBufferfv(int buffer, int drawbuffer, float[] value);

    /**
     * Array version of: {@link #glGetUniformIndices GetUniformIndices}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetUniformIndices">Reference Page</a>
     */
    void glGetUniformIndices(int program, PointerBuffer uniformNames, int[] uniformIndices);

    /**
     * Array version of: {@link #glGetActiveUniformsiv GetActiveUniformsiv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetActiveUniforms">Reference Page</a>
     */
    void glGetActiveUniformsiv(int program, int[] uniformIndices, int pname, int[] params);

    /**
     * Array version of: {@link #glGetActiveUniformBlockiv GetActiveUniformBlockiv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetActiveUniformBlock">Reference Page</a>
     */
    void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params);

    /**
     * Array version of: {@link #glGetActiveUniformBlockName
     * GetActiveUniformBlockName}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetActiveUniformBlockName">Reference Page</a>
     */
    void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int[] length, ByteBuffer uniformBlockName);

    /**
     * Array version of: {@link #glGetInteger64v GetInteger64v}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetInteger64v">Reference
     *      Page</a>
     */
    void glGetInteger64v(int pname, long[] data);

    /**
     * Array version of: {@link #glGetSynciv GetSynciv}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetSync">Reference
     *      Page</a>
     */
    void glGetSynciv(long sync, int pname, int[] length, int[] values);

    /**
     * Array version of: {@link #glGetInteger64i_v GetInteger64i_v}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGetInteger">Reference
     *      Page</a>
     */
    void glGetInteger64i_v(int target, int index, long[] data);

    /**
     * Array version of: {@link #glGetBufferParameteri64v GetBufferParameteri64v}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetBufferParameter">Reference Page</a>
     */
    void glGetBufferParameteri64v(int target, int pname, long[] params);

    /**
     * Array version of: {@link #glGenSamplers GenSamplers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glGenSamplers">Reference
     *      Page</a>
     */
    void glGenSamplers(int[] samplers);

    /**
     * Array version of: {@link #glDeleteSamplers DeleteSamplers}
     * 
     * @see <a target="_blank" href="http://docs.gl/es3/glDeleteSamplers">Reference
     *      Page</a>
     */
    void glDeleteSamplers(int[] samplers);

    /**
     * Array version of: {@link #glSamplerParameteriv SamplerParameteriv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glSamplerParameter">Reference Page</a>
     */
    void glSamplerParameteriv(int sampler, int pname, int[] param);

    /**
     * Array version of: {@link #glSamplerParameterfv SamplerParameterfv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glSamplerParameter">Reference Page</a>
     */
    void glSamplerParameterfv(int sampler, int pname, float[] param);

    /**
     * Array version of: {@link #glGetSamplerParameteriv GetSamplerParameteriv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetSamplerParameter">Reference Page</a>
     */
    void glGetSamplerParameteriv(int sampler, int pname, int[] params);

    /**
     * Array version of: {@link #glGetSamplerParameterfv GetSamplerParameterfv}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetSamplerParameter">Reference Page</a>
     */
    void glGetSamplerParameterfv(int sampler, int pname, float[] params);

    /**
     * Array version of: {@link #glDeleteTransformFeedbacks
     * DeleteTransformFeedbacks}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glDeleteTransformFeedbacks">Reference Page</a>
     */
    void glDeleteTransformFeedbacks(int[] ids);

    /**
     * Array version of: {@link #glGenTransformFeedbacks GenTransformFeedbacks}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGenTransformFeedbacks">Reference Page</a>
     */
    void glGenTransformFeedbacks(int[] ids);

    /**
     * Array version of: {@link #glGetProgramBinary GetProgramBinary}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetProgramBinary">Reference Page</a>
     */
    void glGetProgramBinary(int program, int[] length, int[] binaryFormat, ByteBuffer binary);

    /**
     * Array version of: {@link #glInvalidateFramebuffer InvalidateFramebuffer}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glInvalidateFramebuffer">Reference Page</a>
     */
    void glInvalidateFramebuffer(int target, int[] attachments);

    /**
     * Array version of: {@link #glInvalidateSubFramebuffer
     * InvalidateSubFramebuffer}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glInvalidateSubFramebuffer">Reference Page</a>
     */
    void glInvalidateSubFramebuffer(int target, int[] attachments, int x, int y, int width, int height);

    /**
     * Array version of: {@link #glGetInternalformativ GetInternalformativ}
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/es3/glGetInternalformat">Reference Page</a>
     */
    void glGetInternalformativ(int target, int internalformat, int pname, int[] params);

    ProgramInterfaceQuery getProgramInterfaceQuery();

    TexImageMultisample optTexImageMultisample();
    default boolean hasProgramInterfaceQuery() {
        return getProgramInterfaceQuery() != null;
    }

    Callback enableDebug();
    
    
}