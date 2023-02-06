package turtleduck.buffer;

import turtleduck.buffer.impl.VertexLayoutImpl;

import static turtleduck.buffer.impl.VertexLayoutImpl.*;

public interface VertexLayout {
    static LayoutBuilder create() {
        return new VertexLayoutImpl();
    }

    public <T> VertexAttribute<T> attribute(int index);

    public <T> VertexAttribute<T> attribute(String name);
    public <T> VertexAttribute<T> attribute(String name, Class<T> cls);

    public Type inputFormat(String name);
    public Type inputFormat(int index);
    
    public InputFormatBuilder specifyInputFormat();

    public int numBytes();

    public int numAttributes();

    public int numLocations();

    public enum Type {

        FLOAT(4, GL_FLOAT, false, true), //
        NORM_BYTE(1, GL_UNSIGNED_BYTE, true, false), NORM_SHORT(2, GL_UNSIGNED_SHORT, true, false),
        UNSIGNED_BYTE(1, GL_UNSIGNED_BYTE, false, false), UNSIGNED_SHORT(2, GL_UNSIGNED_SHORT, false, false),
        INTEGER(4, GL_INT, false, true), //
        UNSIGNED_2_10_10_10(4,  GL_UNSIGNED_INT_2_10_10_10_REV, true, false, new int[] {10,10,10,2}),//
        SIGNED_2_10_10_10(4,  GL_SIGNED_INT_2_10_10_10_REV, true, true, new int[] {10,10,10,2})//
        ;

        public final int[] bits;
        public final int bytes;
        public int glType;
        public boolean norm;
        public boolean compact;
        public boolean signed;
        Type(int bytes, int glType, boolean norm, boolean signed) {
            this.bytes = bytes;
            this.glType = glType;
            this.norm = norm;
            this.bits = new int[] {bytes*8,bytes*8,bytes*8,bytes*8};
            this.compact = false;
            this.signed = signed;
        }
        Type(int bytes, int glType, boolean norm, boolean signed, int[] bits) {
            this.bytes = bytes;
            this.glType = glType;
            this.norm = norm;
            this.bits = bits;
            this.compact = true;
            this.signed = signed;
      }
        public String toString() {
            return this.name() + "_" + this.glType;
        }
    };

    interface LayoutBuilder {
        public <T> LayoutBuilder declare(String name, Class<T> type);
        public <T> LayoutBuilder declare(String name, int location, Class<T> type);
        public <T> LayoutBuilder declare(String name, String role, Class<T> type);

        public VertexLayout done();
    }

    interface InputFormatBuilder {
        public InputFormatBuilder setInputFormat(String name, Type type);

        public VertexLayout done();
    }
}
