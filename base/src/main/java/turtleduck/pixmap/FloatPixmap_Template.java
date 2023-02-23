package turtleduck.pixmap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer; // !b // !i
import java.nio.IntBuffer; // i

import turtleduck.colors.Color;

/**
 * Interface for _TYPE_-pixmaps with an unknown number of colour channels.
 * 
 * A pixmap is a two-dimensional grid of pixels, with each pixel having one or
 * more colour components (“channels”). This interface provides generic
 * {@link #get(int, int, int)} and {@link #set(int, int, int, _TYPE_)} methods
 * to read or write from arbitrary channels, as well as specific methods for
 * accessing a single channel/colour component.
 * 
 * For global operations, you can iterate over all pixel locations with
 * {@link #foreachLocation(turtleduck.bitmap.FloatPixmap_Template.LocationConsumer)},
 * and each pixel value with {@link #foreach(PixelConsumer_T__Template)} or
 * {@link #map(PixelFunction_T__Template)}.
 * 
 * @author anya
 *
 */
interface FloatPixmap_Template extends Pixmap {
    Class<Float> DATA_TYPE = Float.class;

    // R static FloatPixmap_Template create(int width, int height, int depth) {
    // R return Pixmap.createFloatPixmap_Template(width, height, depth);
    // R }
    // R static FloatPixmap_Template create(int width, int height, int depth, ByteBuffer data) {
    // R return Pixmap.createFloatPixmap_Template(width, height, depth, data);
    // R }

    /**
     * Find the buffer byte offset of particular pixel.
     */
    int byteOffset(int x, int y);

    /**
     * @return Width of the pixmap, in pixels.
     */
    int width();

    /**
     * @return Height of the pixmap, in pixels.
     */
    int height();

    /**
     * @return Number of channels (values per pixel) of the pixmap.
     */
    int channels();

    /**
     * For a pixmap of __TYPE__, this will be __BYTE_SIZE__.
     * 
     * @return Size in bytes of a single value. (Always __BYTE_SIZE__ for
     *         Pixmap_T__Template.)
     */
    int dataSize();

    /**
     * Read the value of a component at (x,y)
     * 
     * @param x  X coordinate
     * @param y  Y coordinate
     * @param ch The channel
     * @return The value of channel ch at (x,y)
     */
    float getChannel(int x, int y, int ch);

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
     * @param x X coordinate
     * @param y Y coordinate
     * @return The color at (x,y)
     */
    Color getColor(int x, int y);

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
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param color The color
     */
    FloatPixmap_Template setColor(int x, int y, Color c);

    /**
     * Set the value of a channel at (x,y)
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param ch    The channel
     * @param value The new value for (x,y)
     */
    FloatPixmap_Template setChannel(int x, int y, int ch, float value);

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    FloatPixmap_Template foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    FloatPixmap_Template foreachLocation(LocationConsumer_T__Template consumer);

    /**
     * Do something for each value in the bitmap.
     * 
     * The consumer should not store a reference to the array – the array will be
     * reused for the next call, so this is unsafe.
     * 
     * @param consumer A consumer, will receive an array of {@link #channels()}
     *                 elements.
     */
    FloatPixmap_Template foreach(PixelConsumer_T__Template consumer);

    public interface PixelConsumer_T__Template {
        void accept(int x, int y, float[] pixel);
    }

    public interface LocationConsumer_T__Template {
        void accept(FloatPixmap_Template bitmap, int x, int y);
    }

    /**
     * Retrieve the underlying byte buffer.
     * 
     * Change to the buffer may or may not be immediately reflected in the pixmap
     * and vice versa.
     * 
     * Changing the buffer's limit or position may result in undefined behaviour.
     * 
     * @return The underlying byte buffer.
     */
    ByteBuffer byteBuffer();

    FloatBuffer floatBuffer(); // !b // !i

    IntBuffer intBuffer(); // i
}
