package turtleduck.util;

public class MathUtil {
    public static int gcd(int a, int b) {
        return (a == 0 || b == 0) ? Math.abs(a + b) : gcd(b, a % b);

    }

    public static short toShortUNorm(double val) {
        return (short) (clamp(val, 0, 1) * 0xffff);
    }

    public static byte toByteUNorm(double val) {
        return (byte) (clamp(val, 0, 1) * 0xff);
    }

    public static int toIntUNorm(double val) {
        return (int) (clamp(val, 0, 1) * 0xffffffff);
    }

    public static short toShortNorm(double val) {
        val = clamp(val, -1, 1);
        if (val >= 0) {
            return (short) (val * 0x7fff);
        } else {
            return (short) (0x10000 - (val * 0x8000));
        }
    }

    public static short toByteNorm(double val) {
        val = clamp(val, -1, 1);
        if (val >= 0) {
            return (short) (val * 0x7f);
        } else {
            return (short) (0x10000 - (val * 0x80));
        }
    }

    public static short toIntNorm(double val) {
        val = clamp(val, -1, 1);
        if (val >= 0) {
            return (short) (val * 0x7fff);
        } else {
            return (short) (0x100000000L - (val * 0x80000000));
        }
    }

    public static double clamp(double x, double min, double max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        else
            return x;
    }

    public static float clamp(float x, float min, float max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        else
            return x;
    }

    public static int clamp(long x, int min, int max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        else
            return (int) x;
    }

    public static short clamp(long x, short min, short max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        else
            return (short) x;
    }
}
