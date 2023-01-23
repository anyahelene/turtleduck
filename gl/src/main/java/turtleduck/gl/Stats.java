package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

import turtleduck.FrameInfo;
import turtleduck.display.Screen;
import turtleduck.util.TextUtil;

public class Stats implements FrameInfo {
    private static final double SMOOTHING = 0.99;
    private double lastFrameTime;
    private double lastPrintTime;
    private double deltaTime;
    private double currentFrameTime;
    private double currentRenderTime;
    private double targetFps = 60.0;
    private Stat frames = new Stat("frames", "fps", true);
    private Stat render = new Stat("gfx", "s", false);
    private Stat steps = new Stat("model", "s", false);

    public float startFrame() {
        lastFrameTime = currentFrameTime;
        currentFrameTime = currentRenderTime = glfwGetTime();
        deltaTime = (float) (currentFrameTime - lastFrameTime);
        if (lastFrameTime == 0)
            deltaTime = 0;
        frames.addSample(deltaTime);
        // System.err.println(deltaTime);
        return (float) deltaTime;
    }

    public float startRender() {
        currentRenderTime = glfwGetTime();
        double delta = currentRenderTime - currentFrameTime;
        steps.addSample(delta);
        // System.err.println(deltaTime);
        return (float) delta;
    }

    public void endFrame(boolean sleep) {
        double now = glfwGetTime();
        double delta = now - currentRenderTime;
        render.addSample(delta);

        double dt = (float) (now - currentFrameTime);
        long sleepTime = (long) Math.max(0, 1000 * (1.0 / targetFps - dt));
        if (sleep && sleepTime > 0) {
            try {
                // System.err.println("Sleeping " + sleepTime + " ms");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
        if (currentFrameTime - lastPrintTime > 1) {
            lastPrintTime = currentFrameTime;
            System.err.printf("frame %5.0f: %s gfx: %s model: %s%n", frames.n, frames, render, steps);
            frames.reset();
            render.reset();
            steps.reset();
        }
    }

    public int currentFrame() {
        return (int) frames.n;
    }

    public double currentTime() {
        return currentFrameTime;
    }

    public double deltaTime() {
        return deltaTime;
    }

    static class Stat implements FrameInfo.FrameStats {
        double total = 0, sincePrint = 0, runningAverage = 0, max = Double.NEGATIVE_INFINITY,
                min = Double.POSITIVE_INFINITY, current;
        double n = 0;
        int nSincePrint = 0;
        private String name;
        private String unit;
        private boolean invert;

        protected Stat(String name, String unit, boolean invert) {
            this.name = name;
            this.unit = unit;
            this.invert = invert;
        }

        void addSample(double value) {
            current = value;
            sincePrint += value;
            nSincePrint++;
            if (n++ == 0) {
                runningAverage = value;
            } else {
                runningAverage = SMOOTHING * runningAverage + (1 - SMOOTHING) * value;
            }
            if (n > 100) {
                total += value;
                max = Math.max(max, value);
                min = Math.min(min, value);
            }
        }
        
        void reset() {
            sincePrint = 0;
            nSincePrint = 0;
        }

        @Override
        public String toString() {
            int p = TextUtil.findSIPrefix(periodAverage());
            String u = unit != null ? TextUtil.prefixes.get(p) + unit : "";
            double scale = TextUtil.PREFIX_SCALE10[p];

            return String.format("%5.1f %s, %.1f ≤ %.1f ≤ %.1f %-4s  ", periodAverage() * scale, u,
                    min() * scale, runningAverage() * scale, max() * scale, u);
        }

        private double value(double val) {
            if (invert)
                val = 1 / val;
            if (Double.isInfinite(val))
                val = 0;
            return val;
        }

        @Override
        public double total() {
            return value(total);
        }

        @Override
        public double min() {
            return invert ? value(max) : value(min);
        }

        @Override
        public double max() {
            return invert ? value(min) : value(max);
        }

        @Override
        public double runningAverage() {
            return value(runningAverage);
        }

        @Override
        public double count() {
            return n;
        }

        @Override
        public double periodAverage() {
            return value(sincePrint / nSincePrint);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String unit() {
            return unit;
        }

    }

    @Override
    public double maxDeltaTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double realDeltaTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FrameStats fpsStats() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FrameStats modelStats() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FrameStats renderStats() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Screen mainScreen() {
        // TODO Auto-generated method stub
        return null;
    }
}
