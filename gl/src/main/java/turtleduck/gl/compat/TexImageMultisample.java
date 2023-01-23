package turtleduck.gl.compat;

import org.lwjgl.system.NativeType;

public interface TexImageMultisample extends Extension {
    // --- [ glTexImage2DMultisample ] ---

    /**
     * Establishes the data storage, format, dimensions, and number of samples of a
     * 2D multisample texture's image.
     *
     * @param target               the target of the operation. One of:<br>
     *                             <table>
     *                             <tr>
     *                             <td>{@link #GL_TEXTURE_2D_MULTISAMPLE
     *                             TEXTURE_2D_MULTISAMPLE}</td>
     *                             <td>{@link #GL_PROXY_TEXTURE_2D_MULTISAMPLE
     *                             PROXY_TEXTURE_2D_MULTISAMPLE}</td>
     *                             </tr>
     *                             </table>
     * @param samples              the number of samples in the multisample
     *                             texture's image
     * @param internalformat       the internal format to be used to store the
     *                             multisample texture's image.
     *                             {@code internalformat} must specify a
     *                             color-renderable, depth-renderable,
     *                             or stencil-renderable format.
     * @param width                the width of the multisample texture's image, in
     *                             texels
     * @param height               the height of the multisample texture's image, in
     *                             texels
     * @param fixedsamplelocations whether the image will use identical sample
     *                             locations and the same number of samples for all
     *                             texels in the image, and the sample locations
     *                             will not
     *                             depend on the internal format or size of the
     *                             image
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/gl4/glTexImage2DMultisample">Reference Page</a>
     */
    void glTexImage2DMultisample(@NativeType("GLenum") int target, @NativeType("GLsizei") int samples,
            @NativeType("GLint") int internalformat, @NativeType("GLsizei") int width,
            @NativeType("GLsizei") int height, @NativeType("GLboolean") boolean fixedsamplelocations);

    // --- [ glTexImage3DMultisample ] ---

    /**
     * Establishes the data storage, format, dimensions, and number of samples of a
     * 3D multisample texture's image.
     *
     * @param target               the target of the operation. One of:<br>
     *                             <table>
     *                             <tr>
     *                             <td>{@link #GL_TEXTURE_2D_MULTISAMPLE_ARRAY
     *                             TEXTURE_2D_MULTISAMPLE_ARRAY}</td>
     *                             <td>{@link #GL_PROXY_TEXTURE_2D_MULTISAMPLE_ARRAY
     *                             PROXY_TEXTURE_2D_MULTISAMPLE_ARRAY}</td>
     *                             </tr>
     *                             </table>
     * @param samples              the number of samples in the multisample
     *                             texture's image
     * @param internalformat       the internal format to be used to store the
     *                             multisample texture's image.
     *                             {@code internalformat} must specify a
     *                             color-renderable, depth-renderable,
     *                             or stencil-renderable format.
     * @param width                the width of the multisample texture's image, in
     *                             texels
     * @param height               the height of the multisample texture's image, in
     *                             texels
     * @param depth                the depth of the multisample texture's image, in
     *                             texels
     * @param fixedsamplelocations whether the image will use identical sample
     *                             locations and the same number of samples for all
     *                             texels in the image, and the sample locations
     *                             will not
     *                             depend on the internal format or size of the
     *                             image
     * 
     * @see <a target="_blank" href=
     *      "http://docs.gl/gl4/glTexImage3DMultisample">Reference Page</a>
     */
    void glTexImage3DMultisample(@NativeType("GLenum") int target, @NativeType("GLsizei") int samples,
            @NativeType("GLint") int internalformat, @NativeType("GLsizei") int width,
            @NativeType("GLsizei") int height, @NativeType("GLsizei") int depth,
            @NativeType("GLboolean") boolean fixedsamplelocations);

    default String name() {
        return "TexImageMultisample";
    }
}
