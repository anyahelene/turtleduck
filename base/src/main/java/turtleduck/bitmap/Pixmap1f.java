/*
 * WARNING: DO NOT EDIT!
 * This file is automatically generated from Pixmap_CH__T__Template.java by turtleduck.bitmap.BitmapGenerator
 */

package turtleduck.bitmap;


/**
 * Interface for float-pixmaps with a single colour channel. 
 * 
 * A pixmap is a two-dimensional grid of pixels, with each pixel having one or
 * more colour components (“channels”). This interface provides generic
 * {@link #get(int, int, int)} and {@link #set(int, int, int, float)} methods
 * to read or write from arbitrary channels, as well as specific methods for
 * accessing a single channel/colour component.
 * 
 * For global operations, you can iterate over all pixel locations with
 * {@link #foreachLocation(turtleduck.bitmap.FloatPixmap.LocationConsumer)},
 * and each pixel value with {@link #foreach(PixelConsumer1T)} or
 * {@link #map(PixelFunction1T)}.
 * 
 * @author anya
 *
 */
public interface Pixmap1f extends FloatPixmap {

    /**
     * @return Number of channels (values per pixel) of the pixmap (always 1 for Pixmap1f).
     */
    int channels();

    /**
     * Read the value of a component at (x,y)
     * 
     * @param x  X coordinate
     * @param y  Y coordinate
     * @param ch The channel (always 0) 
     * @return The value of channel ch at (x,y)
     */
    float get(int x, int y, int ch);

    /**
     * Read the value of the channel 0 (red) component 
     * 
     * 
     * @param x X coordinate 
     * @param y Y coordinate 
     * @return The value at (x,y) 
     */
    float r(int x, int y); 

    /**
     * 
     */

    /**
     * 
     */

    /**
     * 
     */

    /**
     * Set the value of a channel at (x,y)
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param ch    The channel (always 0) 
     * @param value The new value for (x,y)
     */
    Pixmap1f set(int x, int y, int ch, float value);

    /**
     * Set the value of the channel 0 (red) component 
     * 
     * 
     * @param x     X coordinate 
     * @param y     Y coordinate 
     * @param value The new red value for (x,y) 
     */
    Pixmap1f r(int x, int y, float value); 

    /**
     * 
     */

    /**
     * 
     */

    /**
     * 
     */

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    Pixmap1f foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each value in the bitmap
     * 
     * @param consumer A consumer, will receive (r) 
     */
    Pixmap1f foreach(PixelConsumer1f consumer);
    Pixmap1f foreach(PixelConsumerf consumer);

    /**
     * Transform each value in the bitmap.
     * 
     * @param fun A function
     */
    Pixmap1f map(PixelFunction1f fun);


    public interface PixelFunction1f {
        float apply(float x);
    }

    public interface PixelConsumer1f {
        void accept(int x, int y //
                , float r 
        );
    }

}
