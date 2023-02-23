package turtleduck.pixmap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer; // !b // !i
import java.nio.IntBuffer; // i

import turtleduck.colors.Color;

/**
 * Interface for with an unknown number of colour channels and unknown
 * underlying datatype.
 * 
 * A pixmap is a two-dimensional grid of pixels, with each pixel having one or
 * more colour components (“channels”). This interface provides generic
 * {@link #getColor(int, int)} and {@link #set(int, int, Color)} methods
 * to read or write pixels.
 * 
 * For global operations, you can iterate over all pixel locations with
 * {@link #foreachLocation(turtleduck.bitmap.Pixmap.LocationConsumer)},
 * and each pixel value with {@link #foreach(ColorPixelConsumer)} or
 * {@link #map(ColorPixelFunction)}.
 * 
 * @author anya
 *
 */
public interface Pixmap {

    static BytePixmap createBytePixmap(int width, int height, int depth) {
        return createBytePixmap(width, height, depth, null);
    }

    static BytePixmap createBytePixmap(int width, int height, int depth, ByteBuffer data) {
        switch (depth) {
            case 1:
                return new PixmapImpl1b(width, height, data);
            case 2:
                return new PixmapImpl2b(width, height, data);
            case 3:
                return new PixmapImpl3b(width, height, data);
            case 4:
                return new PixmapImpl4b(width, height, data);
            default:
                throw new IllegalArgumentException("depth " + depth);
        }
    }

    static FloatPixmap createFloatPixmap(int width, int height, int depth) {
        return createFloatPixmap(width, height, depth, null);
    }

    static FloatPixmap createFloatPixmap(int width, int height, int depth, ByteBuffer data) {
        switch (depth) {
            case 1:
                return new PixmapImpl1f(width, height, data);
            case 2:
                return new PixmapImpl2f(width, height, data);
            case 3:
                return new PixmapImpl3f(width, height, data);
            case 4:
                return new PixmapImpl4f(width, height, data);
            default:
                throw new IllegalArgumentException("depth " + depth);
        }
    }

    static IntegerPixmap createIntegerPixmap(int width, int height, int depth) {
        return createIntegerPixmap(width, height, depth, null);
    }

    static IntegerPixmap createIntegerPixmap(int width, int height, int depth, ByteBuffer data) {
        switch (depth) {
            case 1:
                return new PixmapImpl1i(width, height, data);
            case 2:
                return new PixmapImpl2i(width, height, data);
            case 3:
                return new PixmapImpl3i(width, height, data);
            case 4:
                return new PixmapImpl4i(width, height, data);
            default:
                throw new IllegalArgumentException("depth " + depth);
        }
    }

    static ShortPixmap createShortPixmap(int width, int height, int depth) {
        return createShortPixmap(width, height, depth, null);
    }

    static ShortPixmap createShortPixmap(int width, int height, int depth, ByteBuffer data) {
        switch (depth) {
            case 1:
                return new PixmapImpl1s(width, height, data);
            case 2:
                return new PixmapImpl2s(width, height, data);
            case 3:
                return new PixmapImpl3s(width, height, data);
            case 4:
                return new PixmapImpl4s(width, height, data);
            default:
                throw new IllegalArgumentException("depth " + depth);
        }
    }

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
     * @return Size in bytes of a single value.
     */
    int dataSize();

    /**
     * Read the color value at (x,y)
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
    Pixmap setColor(int x, int y, Color c);

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    Pixmap foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each value in the bitmap.
     * 
     * The consumer should not store a reference to the array – the array will be
     * reused for the next call, so this is unsafe.
     * 
     * @param consumer A consumer, will receive an array of {@link #channels()}
     *                 elements.
     */
    Pixmap foreach(ColorPixelConsumer consumer);

    /**
     * Do something for each value in the bitmap.
     * 
     * The consumer should not store a reference to the array – the array will be
     * reused for the next call, so this is unsafe.
     * 
     * @param consumer A consumer, will receive an array of {@link #channels()}
     *                 elements.
     */
    Pixmap map(ColorPixelFunction consumer);

    public interface ColorPixelConsumer {
        void accept(int x, int y, Color pixel);
    }

    public interface ColorPixelFunction {
        Color apply(int x, int y, Color pixel);
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

    public interface LocationConsumer {
        void accept(Pixmap bitmap, int x, int y);
    }

}
