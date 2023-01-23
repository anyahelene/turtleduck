/*
 * WARNING: DO NOT EDIT!
 * This file is automatically generated from Pixmap_CH__T__Template.java by turtleduck.bitmap.BitmapGenerator
 */

package turtleduck.bitmap;

import org.joml.Vector3i; 

/**
 * Interface for short-pixmaps with 3 colour channels. 
 * 
 * A pixmap is a two-dimensional grid of pixels, with each pixel having one or
 * more colour components (“channels”). This interface provides generic
 * {@link #get(int, int, int)} and {@link #set(int, int, int, short)} methods
 * to read or write from arbitrary channels, as well as specific methods for
 * accessing a single channel/colour component.
 * 
 * For global operations, you can iterate over all pixel locations with
 * {@link #foreachLocation(turtleduck.bitmap.ShortPixmap.LocationConsumer)},
 * and each pixel value with {@link #foreach(PixelConsumer3T)} or
 * {@link #map(PixelFunction3T)}.
 * 
 * @author anya
 *
 */
public interface Pixmap3s extends ShortPixmap {

    /**
     * @return Number of channels (values per pixel) of the pixmap (always 3 for Pixmap3s).
     */
    int channels();

    /**
     * Read the value of a component at (x,y)
     * 
     * @param x  X coordinate
     * @param y  Y coordinate
     * @param ch The channel (0–2) 
     * @return The value of channel ch at (x,y)
     */
    short get(int x, int y, int ch);

    /**
     * Read the value of the channel 0 (red) component 
     * 
     * 
     * @param x X coordinate 
     * @param y Y coordinate 
     * @return The value at (x,y) 
     */
    short r(int x, int y); 

    /**
     * Read the value of the channel 1 (green) component 
     * 
     * 
     * @param x X coordinate 
     * @param y Y coordinate 
     * @return The value at (x,y) 
     */
    short g(int x, int y); 

    /**
     * Read the value of the channel 2 (blue) component 
     * 
     * 
     * @param x X coordinate 
     * @param y Y coordinate 
     * @return The value at (x,y) 
     */
    short b(int x, int y); 

    /**
     * 
     */

    /**
     * Set the value of a channel at (x,y)
     * 
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param ch    The channel (0–2) 
     * @param value The new value for (x,y)
     */
    Pixmap3s set(int x, int y, int ch, short value);

    /**
     * Set the value of the channel 0 (red) component 
     * 
     * 
     * @param x     X coordinate 
     * @param y     Y coordinate 
     * @param value The new red value for (x,y) 
     */
    Pixmap3s r(int x, int y, short value); 

    /**
     * Set the value of the channel 0 (red) component 
     * 
     * 
     * @param x     X coordinate 
     * @param y     Y coordinate 
     * @param value The new red value for (x,y) 
     */
    Pixmap3s g(int x, int y, short value); 

    /**
     * Set the value of the channel 0 (red) component 
     * 
     * 
     * @param x     X coordinate 
     * @param y     Y coordinate 
     * @param value The new red value for (x,y) 
     */
    Pixmap3s b(int x, int y, short value); 

    /**
     * 
     */

    /**
     * Do something for each location in the bitmap
     * 
     * @param consumer A consumer, will receive (this bitmap, x, y)
     */
    Pixmap3s foreachLocation(LocationConsumer consumer);

    /**
     * Do something for each value in the bitmap
     * 
     * @param consumer A consumer, will receive (r,g,b) 
     */
    Pixmap3s foreach(PixelConsumer3s consumer);
    Pixmap3s foreach(PixelConsumers consumer);

    /**
     * Transform each value in the bitmap.
     * 
     * @param fun A function
     */
    Pixmap3s map(PixelFunction3s fun);

    public interface PixelFunction3s { 
        Vector3i apply(Vector3i p);
    }


    public interface PixelConsumer3s {
        void accept(int x, int y //
                , short r 
                , short g 
                , short b 
        );
    }

}
