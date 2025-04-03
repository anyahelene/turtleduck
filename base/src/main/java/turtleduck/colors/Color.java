package turtleduck.colors;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.joml.Vector3d;

import turtleduck.annotations.Icon;

@Icon("🎨")
public interface Color {
    enum ColorModel {
        GREY, RGB, CMYK, HSL, HSV, XYZ, PALETTE
    };

    /**
     * @param rgb 0xRRGGBB
     * @return
     */
    static Color fromRGB(int rgb) {
        return new ColorRGB(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, 1f, false);
    }

    /**
     * @param argb 0xAARRGGBB
     * @return
     */
    static Color fromARGB(int argb) {
        return new ColorRGB(((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f, (argb & 0xff) / 255f,
                ((argb >> 24) & 0xff) / 255f, false);
    }

    /**
     * @param argb 0xRRGGBBAA
     * @return
     */
    static Color fromRGBA(int argb) {
        return new ColorRGB(((argb >> 24) & 0xff) / 255f, ((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f,
                ((argb >> 0) & 0xff) / 255f, false);
    }

    static Color fromString(String s) {
        if (s.startsWith("#")) {
            if (s.length() == 7)
                return Color.fromRGB(Integer.valueOf(s.substring(1), 16));
            else if (s.length() == 9)
                return Color.fromRGBA(Integer.valueOf(s.substring(1), 16));

        }

        throw new IllegalArgumentException("not a color: " + s);
    }

    /**
     * @param bgr 0xBBGGRR
     * @return
     */
    static Color fromBGR(int bgr) {
        return new ColorRGB((bgr & 0xff) / 255f, ((bgr >> 8) & 0xff) / 255f, ((bgr >> 24) & 0xff) / 255f, 1f, false);
    }

    /**
     * @param bgra 0xBBGGRRAA
     * @return
     */
    static Color fromBGRA(int bgra) {
        return new ColorRGB(((bgra >> 8) & 0xff) / 255f, ((bgra >> 16) & 0xff) / 255f, ((bgra >> 24) & 0xff) / 255f,
                (bgra & 0xff) / 255f, false);
    }

    static Color fromRGB(int r, int g, int b) {
        return fromRGBA(r, g, b, 255);
    }

    static Color fromRGB(byte r, byte g, byte b) {
        return fromRGBA(r & 0xff, g & 0xff, b & 0xff, 255);
    }

    static Color fromRGBA(byte r, byte g, byte b, byte a) {
        return fromRGBA(r & 0xff, g & 0xff, b & 0xff, a & 0xff);
    }

    static Color fromRGBA(int r, int g, int b, int a) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255)
            throw new IllegalArgumentException("Must be from 0 to 255: (" + r + ", " + g + ", " + b + ", " + a + ")");
        return new ColorRGB(r / 255f, g / 255f, b / 255f, a / 255f, false);
    }

    static Color fromBytes(byte[] bytes, int offset, PixelFormat format) {
        return format.decode(bytes, offset);
    }

    static Color fromXYZ(double x, double y, double z, WhitePoint wp) {
        Vector3d rgb = wp.transformXYZtoRGB(new Vector3d(x, y, z));
        return new ColorRGB((float) rgb.x, (float) rgb.y, (float) rgb.z, 1f, true);
    }
    /**
     * Create a new grey color
     * 
     * Equivalent to color(g, g, g, 1).
     * 
     * @param g The grey component (linear)
     * @return
     */
    static Color grey(double g) {
        return color(g, g, g, 1);
    }

    /**
     * Create a new color, from hue/saturation/value
     * 
     * @param h Hue, in degrees, 0–360
     * @param s Saturation, 0–1
     * @param v Value, 0–1
     * @return A color
     */
    static Color hsv(double h, double s, double v) {
        return ColorRGB.hsv((float) h, (float) s, (float) v, 1);
    }

    /**
     * Create a new color, from hue/saturation/value
     * 
     * @param h Hue, in degrees, 0–360
     * @param s Saturation, 0–1
     * @param v Value, 0–1
     * @param a Alpha/opacity, 0–1
     * @return A color
     */
    static Color hsva(double h, double s, double v, double a) {
        return ColorRGB.hsv((float) h, (float) s, (float) v, (float) a);
    }

    /**
     * Create a new color, from linear floating-point components.
     * 
     * Use {@link #sRGB(double, double, double, double)} if your color components
     * are in sRGB (compressed / non-linear) color space.
     * <p>
     * The colour components are assumed to be pre-multiplied – if they are not, do
     * <code>Color.color(r,g,b).mul(a)</code> instead.
     * 
     * @param r Red component, 0.0–1.0
     * @param g Green component, 0.0–1.0
     * @param b Blue component, 0.0–1.0
     * @param a Alpha component 0.0–1.0
     * @return A color
     */
    static Color color(double r, double g, double b, double a) {
        if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
            throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
        return new ColorRGB((float) r, (float) g, (float) b, (float) a, true);
    }

    /**
     * Create a new opaque color, from linear floating-point components.
     * 
     * Use {@link #sRGB(double, double, double)} if your color components are in
     * sRGB (compressed / non-linear) color space.
     * 
     * @param r Red component, 0.0–1.0
     * @param g Green component, 0.0–1.0
     * @param b Blue component, 0.0–1.0
     * @return A color
     */
    static Color color(double r, double g, double b) {
        return color(r, g, b, 1);
    }

    /**
     * Create a new opaque color, from sRGB floating-point components.
     * 
     * Use {@link #color(double, double, double)} if your color components is in
     * linear color space.
     * 
     * @param r Red component, 0.0–1.0
     * @param g Green component, 0.0–1.0
     * @param b Blue component, 0.0–1.0
     * @return A color
     */
    static Color sRGB(double r, double g, double b) {
        return sRGB(r, g, b, 1);
    }

    /**
     * Create a new color, from sRGB floating-point components.
     * 
     * Use {@link #color(double, double, double, double)} if your color components
     * is in linear color space.
     * <p>
     * The colour components are assumed to be pre-multiplied – if they are not, do
     * <code>Color.sRGB(r,g,b).mul(a)</code> instead.
     * 
     * @param r Red component, 0.0–1.0
     * @param g Green component, 0.0–1.0
     * @param b Blue component, 0.0–1.0
     * @param a Alpha component 0.0–1.0
     * @return A color
     */
    static Color sRGB(double r, double g, double b, double a) {
        if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
            throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
        return new ColorRGB((float) r, (float) g, (float) b, (float) a, false);
    }

    float red();

    float green();

    float blue();

    float alpha();

    /**
     * Set the value of the red component.
     *
     * @param r the desired red component
     * @return a new colour with <code>red() == r</code>, and the other components
     *         unchanged
     */
    Color red(double r);

    /**
     * Set the value of the green component.
     *
     * @param g the desired green component
     * @return a new colour with <code>green() == g</code>, and the other components
     *         unchanged
     */
    Color green(double g);

    /**
     * Set the value of the blue component.
     *
     * @param b the desired blue component
     * @return a new colour with <code>blue() == b</code>, and the other components
     *         unchanged
     */
    Color blue(double b);

    /**
     * Set the value of the alpha (transparency) component.
     * <p>
     * To make a colour transparent, use {@link #mul(double)} instead, this will
     * pre-multiply the alpha component making it possible to blend colours
     * smoothly.
     * 
     * @param a the desired alpha component
     * @return a new colour with <code>alpha() == a</code>, and the other components
     *         unchanged
     */
    Color alpha(double a);

    /**
     * Set the opacity of the colour.
     * <p>
     * To adjust the opacity, use {@link #mul(double)} instead, this will
     * pre-multiply the alpha component making it possible to blend colours
     * smoothly.
     * 
     * @param a the desired opacity
     * @return a new colour with <code>alpha() == a</code>, and the other components
     *         changed accordingly
     */
    // Color opacity(double a);

    Color mix(Color other, double proportion);

    IColorProperties properties();

    Color brighter();

    Color darker();

    Color asRGBA();

    Color asCMYK();

    Color perturb();

    Color perturb(double factor);

    /**
     * Multiply each (red,green,blue,alpha) component by the given coefficient.
     * <p>
     * This is the preferred way to make a colour (partially) transparent, as it
     * keeps the colour in pre-multiplied form, allowing it to be mixed correctly
     * with other colours.
     * <p>
     * To set the alpha component directly, use {@link #alpha(double)}. To multiply
     * only the alpha component, use <code>mul(1,1,1,a)</code>.
     * 
     * @param a coefficient
     * @return The colour <code>color(red()*a,green()*a,blue()*a,alpha()*a)</code>
     */
    Color mul(double a);

    /**
     * Multiply each (red,green,blue,alpha) component by the given coefficients.
     * <p>
     * This is the preferred way to make a colour (partially) transparent, as it
     * keeps the colour in pre-multiplied form, allowing it to be mixed correctly
     * with other colours.
     * <p>
     * To set components directly, use {@link #red(double)}, {@link #green(double)},
     * {@link #blue(double)}, {@link #alpha(double)} or
     * {@link #color(double, double, double, double)}. To multiply all components by
     * the same value, use {@link #mul(double)}.
     * 
     * @param r coefficient for the red component
     * @param g coefficient for the green component
     * @param b coefficient for the blue component
     * @param a coefficient for the alpha component
     * @return The colour <code>color(red()*r,green()*g,blue()*b,alpha()*a)</code>
     */
    Color mul(double r, double g, double b, double a);

    Color mul(Color top);

    Color screen(Color top);

    Color overlay(Color top);

    boolean isAdditive();

    boolean isSubtractive();

    <T> T as(Class<T> type);

    String toCss();

    String toSGRParam(int i);

    /**
     * Apply this colour to a string as text foreground, using Select Graphic
     * Rendition control sequences
     * 
     * @param s A string
     * @return The string, with control sequences applied
     */
    String applyFg(String s);

    /**
     * Apply this colour to a string as text background, using Select Graphic
     * Rendition control sequences
     * 
     * @param s A string
     * @return The string, with control sequences applied
     */
    String applyBg(String s);

    int toARGB();

    Color writeTo(short[] data, int offset);

    /**
     * Convert to hexadecimal string.
     * 
     * Each colour component is clamped to 0.0–1.0, converted an 8-bit integer
     * (0–255), and written as two hexadecimal characters, in R,G,B(,A) order. The
     * alpha component is included only if different from 1.0. The result is the
     * typical hex representation of colours, using non-linear sRGB components.
     * <p>
     * For example, <code>Colors.RED.toHex()</code> is <code>"FF0000"</code>,
     * <code>Color.fromRGB(0x010203).toHex()</code> is <code>"010203"</code>, and
     * <code>Color.color(0.25,0.50,0.75,0.5)</code> is <code>"89BCE180"</code>.
     * <p>
     * More generally,
     * 
     * <pre>
     * Color.fromRGB(Integer.valueOf(c.toHex(), 16)).equals(c)
     * </pre>
     * 
     * @return The colour as a hexadecimal string.
     */
    String toHex();

    <T> T map(RGBFunction<T> fun);

    <T> T map(RGBAFunction<T> fun);

    Color map(Function<Double, Double> fun);

    interface RGBFunction<T> {
        T apply(double r, double g, double b);
    }

    interface RGBAFunction<T> {
        T apply(double r, double g, double b, double ao);
    }

    String toCssFunctional();

    /**
     * Write the color to a buffer as sRGB bytes
     * 
     * The color components are compressed from linear form to sRGB, alpha remains
     * linear.
     * 
     * The byte order is ARGB (i.e., 0xaarrggbb in big endian order).
     * 
     * @param buf A byte buffer
     */
    void toARGB(ByteBuffer buf);

    /**
     * Write the color to a buffer as sRGB bytes
     * 
     * The color components are compressed from linear form to sRGB, alpha remains
     * linear.
     * 
     * The byte order is BGRA (i.e., 0xaarrggbb in little endian order).
     * 
     * @param buf A byte buffer
     */
    void toBGRA(ByteBuffer buf);
    
    /**
     * Write the color to a buffer as sRGB bytes
     * 
     * The color components are compressed from linear form to sRGB, alpha remains
     * linear.
     * 
     * The byte order is ARGB (i.e., 0xaarrggbb in big endian order).
     * 
     * @param buf A byte buffer
     * @param index byte index of first byte
     */
    void toARGB(ByteBuffer buf, int index);

    /**
     * Write the color to a buffer as sRGB bytes
     * 
     * The color components are compressed from linear form to sRGB, alpha remains
     * linear.
     * 
     * The byte order is BGRA (i.e., 0xaarrggbb in little endian order).
     * 
     * @param buf A byte buffer
     * @param index byte index of first byte
     */
    void toBGRA(ByteBuffer buf, int index);
}
