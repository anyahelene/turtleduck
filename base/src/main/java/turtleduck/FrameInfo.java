package turtleduck;

import turtleduck.display.Screen;

public interface FrameInfo {

    /**
     * The frame counter increases by one for every graphics update.
     * 
     * @return Current frame number.
     */
    public int currentFrame();

    /**
     * {@link #currentTime()} is sum of {@link #deltaTime()} for all frames up to
     * {@link #currentFrame()}
     * 
     * @return Accumulated {@link #deltaTime()}, in seconds
     */
    public double currentTime();

    /**
     * Return elapsed time since last frame.
     * 
     * Delta time does not include time elapsed while graphics is paused, and the
     * value is capped at {@link #maxDeltaTime()} to filter out abnormal frame
     * times.
     * 
     * @return Elapsed time from start of last frame to start of this frame, in
     *         seconds
     */
    public double deltaTime();

    /**
     * The default is TODO times the average {@link #deltaTime()}.
     * 
     * @return Max value allowed for {@link #deltaTime()}.
     */
    public double maxDeltaTime();

    /**
     * Return elapsed real time since last frame.
     * 
     * This is the actual “wall clock” time, and includes time elapsed while
     * graphics is paused / program is suspended / etc.
     * 
     * @return Elapsed real time from start of last frame to start of this frame, in
     *         seconds
     */
    public double realDeltaTime();

    /**
     * @return FPS statistics
     */
    public FrameStats fpsStats();

    public FrameStats modelStats();

    public FrameStats renderStats();

    public Screen mainScreen();

    interface FrameStats {
        /**
         * @return Sum of all samples
         */
        double total();

        /**
         * @return Minimum sample value
         */
        double min();

        /**
         * @return Maximum sample value
         */
        double max();

        /**
         * @return Running average of samples
         */
        double runningAverage();

        /**
         * @return Number of samples
         */
        double count();

        /**
         * @return Average of samples since last time stats were checked
         */
        double periodAverage();

        /**
         * @return Name of the frame stat variable
         */
        String name();

        /**
         * @return Sample unit (usually seconds or frames per second)
         */
        String unit();
    }

}
