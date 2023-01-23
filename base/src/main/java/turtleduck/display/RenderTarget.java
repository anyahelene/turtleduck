package turtleduck.display;

public interface RenderTarget {
    /**
     * @return Width in virtual coordinates
     */
    double width();

    /**
     * @return Height in virtual coordinates
     */
    double height();

    /**
     * @return Actual width of the underlying framebuffer (in pixels)
     */
    int framebufferWidth();

    /**
     * @return Actual height of the underlying framebuffer (in pixels)
     */
    int framebufferHeight();

    /**
     * @return Number of color channels, usually 0–4
     */
    int colorChannels();

    /**
     * @return Number of bits per color channel
     */
    int colorBits();

    /**
     * @return Number of bits in the depth channel, or 0
     */
    int depthBits();

    /**
     * @return Number of bits in the stencil channel, or 0
     */
    int stencilBits();

    interface RenderBufferBuilder {
        RenderBufferBuilder viewport(Viewport viewport);

        RenderBufferBuilder width(double width);

        RenderBufferBuilder height(double width);

        RenderBufferBuilder framebufferWidth(double fbWidth);

        RenderBufferBuilder framebufferHeight(double fbHeight);

        RenderBufferBuilder colors(int colorChannels);

        RenderBufferBuilder depth(int depthBits);

        RenderBufferBuilder stencil(int stencilBits);

        RenderTarget done();
    }
}
