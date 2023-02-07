package turtleduck.pixmap;

import org.joml.Vector4f; // G

import turtleduck.colors.Color;

/**
 * Interface for _TYPE_-pixmaps with _CH_ colour channels. // G
 * Interface for _TYPE_-pixmaps with a single colour channel. // 1
 * 
 * A pixmap is a two-dimensional grid of pixels, with each pixel having one or
 * more colour components (“channels”). This interface provides generic
 * {@link #get(int, int, int)} and {@link #set(int, int, int, _TYPE_)} methods
 * to read or write from arbitrary channels, as well as specific methods for
 * accessing a single channel/colour component.
 * 
 * For global operations, you can iterate over all pixel locations with
 * {@link #foreachLocation(turtleduck.bitmap.FloatPixmap_Template.LocationConsumer)},
 * and each pixel value with {@link #foreach(PixelConsumer_CH_T_Template)} or
 * {@link #map(PixelFunction_CH_T_Template)}.
 * 
 * @author anya
 *
 */
interface Pixmap_CH__T__Template extends FloatPixmap_Template {

    /**
     * @return Number of channels (values per pixel) of the pixmap (always _CH_ for
     *         Pixmap_CH__T__Template).
     */
    int channels();

    /**
     * Read the value of a component at (x,y)
     * 
     * @param x  X coordinate
     * @param y  Y coordinate
     * @param ch The channel (always 0) // 1
     * @param ch The channel (0–1) // 2
     * @param ch The channel (0–2) // 3
     * @param ch The channel (0–3) // 4
     * @return The value of channel ch at (x,y)
     */
    float get(int x, int y, int ch);

    /**
     * Read the color value at (x,y)
     * 
     * Color components interpreted as
     * linear color values in single-precision float format (0.0–1.0) // f
     * linear color values in double-precision float format (0.0–1.0) // d
     * sRGB color values, as normalized unsigned bytes (0–255) // b
     * linear color values, as normalized unsigned shorts (0–65535) // s
     * linear color values, as normalized unsigned ints (0–(2^32-1)) // i
     * 
     * The green and blue channels will be set equal to the red channel, // 1
     * and alpha channel will be 1.0 // 1
     * The blue channel will be set to 0.0 and the alpha channel to 1.0 // 2
     * The alpha channel will be set to 1.0 // 3
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return The color at (x,y)
     */
    Color get(int x, int y);

    /**
     * Read the value of the channel 0 (red) component // R
     * // R
     * 
     * @param x X coordinate // R
     * @param y Y coordinate // R
     * @return The value at (x,y) // R
     */
    float r(int x, int y); // R

    /**
     * Read the value of the channel 1 (green) component // G
     * // G
     * 
     * @param x X coordinate // G
     * @param y Y coordinate // G
     * @return The value at (x,y) // G
     */
    float g(int x, int y); // G

    /**
     * Read the value of the channel 2 (blue) component // B
     * // B
     * 
     * @param x X coordinate // B
     * @param y Y coordinate // B
     * @return The value at (x,y) // B
     */
    float b(int x, int y); // B

    /**
     * Read the value of the channel 3 (alpha) component // A
     * // A
     * 
     * @param x X coordinate // A
     * @param y Y coordinate // A
     * @return The value at (x,y) // A
     */
    float a(int x, int y); // A

    /**
     * Set the value of a channel at (x,y)
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param ch    The channel (always 0) // 1
     * @param ch    The channel (0–1) // 2
     * @param ch    The channel (0–2) // 3
     * @param ch    The channel (0–3) // 4
     * @param value The new value for (x,y)
     */
    Pixmap_CH__T__Template set(int x, int y, int ch, float value);

    /**
     * Set the color value at (x,y)
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param r Red component // R
     * @param g Green component // G
     * @param b Blue component // B
     * @param a Alpha component // A
     */
    Pixmap_CH__T__Template set(int x, int y //
            ,float r // R
            ,float g // G
            ,float b // B
            ,float a// A
    );

    /**
     * Set the color value at (x,y)
     * 
     * Color components are stored as
     * linear color values in single-precision float format (0.0–1.0) // f
     * linear color values in double-precision float format (0.0–1.0) // d
     * sRGB color values, as normalized unsigned bytes (0–255) // b
     * linear color values, as normalized unsigned shorts (0–65535) // s
     * linear color values, as normalized unsigned ints (0–(2^32-1)) // i
     * 
     * The green, blue and alpha channels will be discarded // 1
     * The blue and alpha channels will be discarded // 2
     * The alpha channel will be discarded // 3
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param color The color
     */
    Pixmap_CH__T__Template set(int x, int y, Color c);

    /**
     * Set the value of the channel 0 (red) component // R
     * // R
     * 
     * @param x     X coordinate // R
     * @param y     Y coordinate // R
     * @param value The new red value for (x,y) // R
     */
    Pixmap_CH__T__Template r(int x, int y, float value); // R

    /**
     * Set the value of the channel 0 (red) component // G
     * // G
     * 
     * @param x     X coordinate // G
     * @param y     Y coordinate // G
     * @param value The new red value for (x,y) // G
     */
    Pixmap_CH__T__Template g(int x, int y, float value); // G

    /**
     * Set the value of the channel 0 (red) component // B
     * // B
     * 
     * @param x     X coordinate // B
     * @param y     Y coordinate // B
     * @param value The new red value for (x,y) // B
     */
    Pixmap_CH__T__Template b(int x, int y, float value); // B

    /**
     * Set the value of the channel 0 (red) component // A
     * // A
     * 
     * @param x     X coordinate // A
     * @param y     Y coordinate // A
     * @param value The new alpha value for (x,y) // A
     */
    Pixmap_CH__T__Template a(int x, int y, float value); // A

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    Pixmap_CH__T__Template foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each value in the bitmap
     * 
     * @param consumer A consumer, will receive (r) // 1
     * @param consumer A consumer, will receive (r,g) // 2
     * @param consumer A consumer, will receive (r,g,b) // 3
     * @param consumer A consumer, will receive (r,g,b,a) // 4
     */
    Pixmap_CH__T__Template foreach(PixelConsumer_CH__T__Template consumer);

    Pixmap_CH__T__Template foreach(PixelConsumer_T__Template consumer);

    /**
     * Transform each value in the bitmap.
     * 
     * @param fun A function
     */
    Pixmap_CH__T__Template map(PixelFunction_CH__T__Template fun);

    public interface PixelFunction_CH__T__Template { // G
        Vector4f apply(Vector4f p);// G
    }// G

// 1    public interface PixelFunction_CH__T__Template {
// 1        float apply(float x);
// 1    }

    public interface PixelConsumer_CH__T__Template {
        void accept(int x, int y //
                , float r // R
                , float g // G
                , float b // B
                , float a // A
        );
    }

}
