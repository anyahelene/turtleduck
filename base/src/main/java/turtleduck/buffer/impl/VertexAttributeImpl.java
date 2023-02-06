package turtleduck.buffer.impl;

import java.nio.ByteBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.VertexLayout.Type;
import turtleduck.colors.Color;

public abstract class VertexAttributeImpl<T> implements VertexAttribute<T> {
    final String name;
    final int location;
    final int size;
    final int index;
    int offset;
    Type type;

    public VertexAttributeImpl(String name, int location, int offset, int size, int index, Type type) {
        super();
        this.name = name;
        this.location = location;
        this.offset = offset;
        this.size = size;
        this.index = index;
        this.type = type;
    }

    @Override
    public String toString() {
        String s = String.format("%d: %s %s", location, type, name);
        if (size > 1)
            s = s + "[" + size + "]+" + offset;
        return s;
    }

    @Override
    public String toLine() {
        return String.format("  location %d: %d %s at offset %d", location, size, type.name(), offset);
    }

    @Override
    public int numBytes() {
        return type.compact ? type.bytes : size * type.bytes;
    }

    @Override
    public int numElements() {
        return size;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String baseType() {
        return type.toString();
    }

    @Override
    public int glType() {
        return type.glType;
    }

    @Override
    public int location() {
        return location;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public boolean isNormalized() {
        return type.norm;
    }

    @Override
    public int numLocations() {
        return 1;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public T read(ByteBuffer source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer dest, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer dest, float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer dest, float x, float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer dest, float x, float y, float z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer dest, float x, float y, float z, float w) {
        throw new UnsupportedOperationException();
    }

    public static class DataField1i extends VertexAttributeImpl<Integer> {
        public DataField1i(String name, int location, int offset, int index) {
            super(name, location, offset, 1, index, Type.FLOAT);
        }

        public void write(ByteBuffer dest, int value) {
            dest.putInt(value);
        }

        public void write(ByteBuffer dest, Integer value) {
            dest.putInt(value);
        }

        public void write(ByteBuffer dest, float x) {
        }

        public void write(ByteBuffer dest, float x, float y) {
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
        }
    }

    public static class DataField1f extends VertexAttributeImpl<Float> {
        public DataField1f(String name, int location, int offset, int index) {
            super(name, location, offset, 1, index, Type.FLOAT);
        }

        public Float read(ByteBuffer source) {
            return source.getFloat();
        }

        public void write(ByteBuffer dest, float value) {
            dest.putFloat(value);
        }

        public void write(ByteBuffer dest, Float value) {
            dest.putFloat(value);
        }

        public void write(ByteBuffer dest, float x, float y) {
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
        }
    }

    public static class DataField2f extends VertexAttributeImpl<Vector2f> {
        public DataField2f(String name, int location, int offset, int index) {
            super(name, location, offset, 2, index, Type.FLOAT);
        }

        public Vector2f read(ByteBuffer source) {
            Vector2f v = new Vector2f().set(source);
            source.position(source.position() + 8);
            return v;
        }

        public void write(ByteBuffer dest, Vector2f value) {
            value.get(dest);
            dest.position(dest.position() + 8);
        }

        public void write(ByteBuffer dest, float x, float y) {
            dest.putFloat(x);
            dest.putFloat(y);
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
        }
    }

    public static class DataField3f extends VertexAttributeImpl<Vector3f> {
        public DataField3f(String name, int location, int offset, int index) {
            super(name, location, offset, 3, index, Type.FLOAT);
        }

        public Vector3f read(ByteBuffer source) {
            Vector3f v = new Vector3f().set(source);
            source.position(source.position() + 12);
            return v;
        }

        public void write(ByteBuffer dest, Vector3f value) {
            value.get(dest);
            dest.position(dest.position() + 12);
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
            dest.putFloat(x);
            dest.putFloat(y);
            dest.putFloat(z);
        }
    }

    public static class DataField4f extends VertexAttributeImpl<Vector4f> {
        public DataField4f(String name, int location, int offset, int index) {
            super(name, location, offset, 4, index, Type.FLOAT);
        }

        public Vector4f read(ByteBuffer source) {
            Vector4f v = new Vector4f().set(source);
            source.position(source.position() + 16);
            return v;
        }

        public void write(ByteBuffer dest, Vector4f value) {
            value.get(dest);
            dest.position(dest.position() + 16);
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
            dest.putFloat(x);
            dest.putFloat(y);
            dest.putFloat(z);
            dest.putFloat(1);
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
            dest.putFloat(x);
            dest.putFloat(y);
            dest.putFloat(z);
            dest.putFloat(w);
        }

    }

    public static class DataField4cf extends VertexAttributeImpl<Color> {
        public DataField4cf(String name, int location, int offset, int index) {
            super(name, location, offset, 4, index, Type.FLOAT);
        }

        public Color read(ByteBuffer source) {
            int r = source.getShort() & 0xffff;
            int g = source.getShort() & 0xffff;
            int b = source.getShort() & 0xffff;
            int a = source.getShort() & 0xffff;
            return Color.color(r / 65535.0, g / 65535.0, b / 65535.0, a / 65535.0);
        }

        public void write(ByteBuffer dest, Color color) {
            dest.putFloat(color.red());
            dest.putFloat(color.green());
            dest.putFloat(color.blue());
            dest.putFloat(color.alpha());
        }

        public void write(ByteBuffer dest, float x, float y, float z) {
            dest.putFloat(x);
            dest.putFloat(y);
            dest.putFloat(z);
            dest.putFloat(1);
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
            dest.putFloat(x);
            dest.putFloat(y);
            dest.putFloat(z);
            dest.putFloat(w);
        }

    }

    public static class DataField_10_10_10_2c extends VertexAttributeImpl<Color> {
        public DataField_10_10_10_2c(String name, int location, int offset, int index) {
            super(name, location, offset, 4, index, Type.UNSIGNED_2_10_10_10);
            if (!type.norm)
                throw new IllegalArgumentException("Must be a normalized type format");
        }

        public Color read(ByteBuffer source) {
            int c = source.getInt();
            int r = (c >>> 0) & 0x3ff;
            int g = (c >>> 10) & 0x3ff;
            int b = (c >>> 20) & 0x3ff;
            int a = (c >>> 30) & 0x003;
            return Color.color(r / 1023.0, g / 1023.0, b / 1023.0, a / 3.0);
        }

        public void write(ByteBuffer dest, Color color) {
            int c = (Math.round(color.alpha() * 3)) << 30
                    | (Math.round(color.blue() * 1023)) << 20
                    | (Math.round(color.green() * 1023)) << 10
                    | (Math.round(color.red() * 1023)) << 0;
            dest.putInt(c);
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
            int c = (Math.round(w * 3)) << 30
                    | (Math.round(z * 1023)) << 20
                    | (Math.round(y * 1023)) << 10
                    | (Math.round(x * 1023)) << 0;
            dest.putInt(c);

        }
    }
    public static class DataField_10_10_10_2 extends VertexAttributeImpl<Vector4f> {
        // TODO: implement two's complement
        public DataField_10_10_10_2(String name, int location, int offset, int index) {
            super(name, location, offset, 4, index, Type.SIGNED_2_10_10_10);
            if (!type.norm)
                throw new IllegalArgumentException("Must be a normalized type format");
        }

        public Vector4f read(ByteBuffer source) {
            int c = source.getInt();
            int r = (c >>> 0) & 0x3ff;
            int g = (c >>> 10) & 0x3ff;
            int b = (c >>> 20) & 0x3ff;
            int a = (c >>> 30) & 0x003;
            return new Vector4f(r / 1023f, g / 1023f, b / 1023f, a / 3f);
        }

        public void write(ByteBuffer dest, Color color) {
            int c = (Math.round(color.alpha() * 3)) << 30
                    | (Math.round(color.blue() * 1023)) << 20
                    | (Math.round(color.green() * 1023)) << 10
                    | (Math.round(color.red() * 1023)) << 0;
            dest.putInt(c);
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
            int c = (Math.round(w * 3)) << 30
                    | (Math.round(z * 1023)) << 20
                    | (Math.round(y * 1023)) << 10
                    | (Math.round(x * 1023)) << 0;
            dest.putInt(c);

        }
    }

    public static class DataField4c extends VertexAttributeImpl<Color> {
        public DataField4c(String name, int location, int offset, int index) {
            super(name, location, offset, 4, index, Type.NORM_SHORT);
        }

        public Color read(ByteBuffer source) {
            int r = source.getShort() & 0xffff;
            int g = source.getShort() & 0xffff;
            int b = source.getShort() & 0xffff;
            int a = source.getShort() & 0xffff;
            return Color.color(r / 65535.0, g / 65535.0, b / 65535.0, a / 65535.0);
        }

        public void write(ByteBuffer dest, Color color) {
            dest.putShort((short) Math.round(color.red() * 65535));
            dest.putShort((short) Math.round(color.green() * 65535));
            dest.putShort((short) Math.round(color.blue() * 65535));
            dest.putShort((short) Math.round(color.alpha() * 65535));
        }

        public void write(ByteBuffer dest, float x, float y, float z, float w) {
            dest.putShort((short) Math.round(x * 65535));
            dest.putShort((short) Math.round(y * 65535));
            dest.putShort((short) Math.round(z * 65535));
            dest.putShort((short) Math.round(w * 65535));
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + location;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + offset;
        result = prime * result + size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VertexAttributeImpl)) {
            return false;
        }
        VertexAttributeImpl<?> other = (VertexAttributeImpl<?>) obj;
        if (location != other.location) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        return true;
    }
}