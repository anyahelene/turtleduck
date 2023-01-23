package turtleduck.bitmap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer; // !b // !i
import java.nio.IntBuffer; // i

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
interface FloatPixmap_Template {
    Class<Float> DATA_TYPE = Float.class;

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
    float get(int x, int y, int ch);

    /**
     * Set the value of a channel at (x,y)
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param ch    The channel
     * @param value The new value for (x,y)
     */
    FloatPixmap_Template set(int x, int y, int ch, float value);

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    FloatPixmap_Template foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each value in the bitmap.
     * 
     * The consumer should not store a reference to the array – the array will be
     * reused for the next call, so this is unsafe.
     * 
     * @param consumer A consumer, will receive an array of {@link #channels()} elements.
     */
    FloatPixmap_Template foreach(PixelConsumer_T__Template consumer);

    public interface PixelConsumer_T__Template {
        void accept(int x, int y, float[] pixel);
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
        void accept(FloatPixmap_Template bitmap, int x, int y);
    }

    FloatBuffer floatBuffer(); // !b // !i
    IntBuffer intBuffer(); // i
}
