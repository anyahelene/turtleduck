package turtleduck.pixmap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer; // !i // !b
import java.nio.IntBuffer; // i

import org.joml.Vector4f; // G

import turtleduck.colors.Color;
import turtleduck.util.MathUtil;

class PixmapImpl_CH__T__Template implements Pixmap_CH__T__Template {

    private static final int BYTE_SIZE = Float.BYTES;
    private ByteBuffer data;
    private final int width;
    private final int height;
    private final int channels;

    public PixmapImpl_CH__T__Template(int width, int height, int channels, ByteBuffer data) {
        super();
        this.data = data;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public int byteOffset(int x, int y) {
        return (x + y * width) * channels * BYTE_SIZE;
    }

    public float get(int x, int y, int ch) {
        int idx = byteOffset(x, y);
        return data.getFloat(idx + ch * BYTE_SIZE);
    }

    public float r(int x, int y) { // R
        int idx = byteOffset(x, y); // R
        return data.getFloat(idx + 0 * BYTE_SIZE); // R
    } // R

    public float g(int x, int y) { // G
        int idx = byteOffset(x, y); // G
        return data.getFloat(idx + 1 * BYTE_SIZE); // G
    } // G

    public float b(int x, int y) { // B
        int idx = byteOffset(x, y); // B
        return data.getFloat(idx + 2 * BYTE_SIZE); // B
    } // B

    public float a(int x, int y) { // A
        int idx = byteOffset(x, y); // A
        return data.getFloat(idx + 3 * BYTE_SIZE); // A
    } // A

    public Pixmap_CH__T__Template set(int x, int y, int ch, float value) {
        int idx = byteOffset(x, y);
        data.putFloat(idx + ch * BYTE_SIZE, value);
        return this;
    }

    public Pixmap_CH__T__Template r(int x, int y, float value) { // R
        int idx = byteOffset(x, y); // R
        data.putFloat(idx + 0 * BYTE_SIZE, value); // R
        return this; // R
    } // R

    public Pixmap_CH__T__Template g(int x, int y, float value) { // G
        int idx = byteOffset(x, y); // G
        data.putFloat(idx + 1 * BYTE_SIZE, value); // G
        return this; // G
    } // G

    public Pixmap_CH__T__Template b(int x, int y, float value) { // B
        int idx = byteOffset(x, y); // B
        data.putFloat(idx + 2 * BYTE_SIZE, value); // B
        return this; // B
    } // B

    public Pixmap_CH__T__Template a(int x, int y, float value) { // A
        int idx = byteOffset(x, y); // A
        data.putFloat(idx + 3 * BYTE_SIZE, value); // A
        return this; // A
    } // A

    public Pixmap_CH__T__Template foreachLocation(LocationConsumer consumer) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(this, x, y);
            }
        }
        return this;
    }

    public Pixmap_CH__T__Template foreach(PixelConsumer_CH__T__Template consumer) {
        for (int i = 0, y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(x, y//
                        , data.getFloat(i) // R
                        , data.getFloat(i + BYTE_SIZE) // G
                        , data.getFloat(i + 2 * BYTE_SIZE) // B
                        , data.getFloat(i + 3 * BYTE_SIZE) // A
                );
                i += channels * BYTE_SIZE;
            }
        }
        return this;
    }

    public Pixmap_CH__T__Template foreach(PixelConsumer_T__Template consumer) {
        float[] array = new float[channels];
        for (int i = 0, y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                array[0] = data.getFloat(i); // R
                array[0] = data.getFloat(i + BYTE_SIZE); // G
                array[0] = data.getFloat(i + 2 * BYTE_SIZE); // B
                array[0] = data.getFloat(i + 3 * BYTE_SIZE); // A
                consumer.accept(x, y, array);
                i += channels * BYTE_SIZE;
            }
        }
        return this;
    }

    public Pixmap_CH__T__Template map(PixelFunction_CH__T__Template fun) { // G
        Vector4f p = new Vector4f(); // G
        int len = width * height * channels * BYTE_SIZE; // G
        for (int i = 0; i < len; i += channels * BYTE_SIZE) { // G
            p.x = data.getFloat(i + 0 * BYTE_SIZE); // G
            p.y = data.getFloat(i + 1 * BYTE_SIZE); // G
            p.z = data.getFloat(i + 2 * BYTE_SIZE); // B
            p.w = data.getFloat(i + 3 * BYTE_SIZE); // A
            Vector4f q = fun.apply(p); // G
            data.putFloat(i + 0 * BYTE_SIZE, (float) q.x); // G
            data.putFloat(i + 1 * BYTE_SIZE, (float) q.y); // G
            data.putFloat(i + 2 * BYTE_SIZE, (float) q.z); // B
            data.putFloat(i + 3 * BYTE_SIZE, (float) q.w); // A
        } // G
        return this; // G
    } // G

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int dataSize() {
        return BYTE_SIZE;
    }

    @Override
    public ByteBuffer byteBuffer() {
        return data;
    }

    @Override // !i // !b
    public FloatBuffer floatBuffer() { // !i // !b
        return data.asFloatBuffer(); // !i // !b
    } // !i // !b

    @Override // i
    public IntBuffer intBuffer() { // i
        return data.asIntBuffer(); // i
    } // i

    @Override
    public int channels() {
        return channels;
    }

    @Override
    public Color get(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pixmap_CH__T__Template set(int x, int y //
            , float r // R
            , float g // G
            , float b // B
            , float a// A
    ) {
        int idx = byteOffset(x, y);
        data.putFloat(idx + 0 * BYTE_SIZE, r); // R
        data.putFloat(idx + 1 * BYTE_SIZE, g); // G
        data.putFloat(idx + 2 * BYTE_SIZE, b); // B
        data.putFloat(idx + 3 * BYTE_SIZE, a); // A

        return this;
    }

    @Override
    public Pixmap_CH__T__Template set(int x, int y, Color c) {
        int idx = byteOffset(x, y);
        int col = c.toARGB(); // b
        data.put(idx + 0 * BYTE_SIZE, (byte) ((col >> 16) & 0xff)); // R // b
        data.put(idx + 1 * BYTE_SIZE, (byte) ((col >> 8) & 0xff)); // G // b
        data.put(idx + 2 * BYTE_SIZE, (byte) ((col >> 0) & 0xff)); // B // b
        data.put(idx + 3 * BYTE_SIZE, (byte) ((col >> 24) & 0xff)); // A // b
        data.putFloat(idx + 0 * BYTE_SIZE, c.red()); // R // !b // !s // !i
        data.putFloat(idx + 1 * BYTE_SIZE, c.green()); // G // !b // !s // !i
        data.putFloat(idx + 2 * BYTE_SIZE, c.blue()); // B // !b // !s // !i
        data.putFloat(idx + 3 * BYTE_SIZE, c.alpha()); // A // !b // !s // !i
        data.putInt(idx + 0 * BYTE_SIZE, MathUtil.toIntUNorm(c.red())); // R // i
        data.putInt(idx + 1 * BYTE_SIZE, MathUtil.toIntUNorm(c.green())); // G // i
        data.putInt(idx + 2 * BYTE_SIZE, MathUtil.toIntUNorm(c.blue())); // B // i
        data.putInt(idx + 3 * BYTE_SIZE, MathUtil.toIntUNorm(c.alpha())); // A // i
        data.putShort(idx + 0 * BYTE_SIZE, MathUtil.toShortUNorm(c.red())); // R // s
        data.putShort(idx + 1 * BYTE_SIZE, MathUtil.toShortUNorm(c.green())); // G // s
        data.putShort(idx + 2 * BYTE_SIZE, MathUtil.toShortUNorm(c.blue())); // B // s
        data.putShort(idx + 3 * BYTE_SIZE, MathUtil.toShortUNorm(c.alpha())); // A // s

        return this;
    }

// 1    public Pixmap_CH__T__Template map(PixelFunction_CH__T__Template fun) {
// 1        int len = width * height * channels * BYTE_SIZE;
// 1        for (int i = 0; i < len; i += channels * BYTE_SIZE) {
// 1            data.putFloat(i, fun.apply(data.getFloat(i)));
// 1        }
// 1        return this;
// 1    } 

}
