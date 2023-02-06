package turtleduck.buffer;

import java.nio.ByteBuffer;

import turtleduck.buffer.VertexLayout.Type;

public interface VertexAttribute<T> {

    String toString();

    String toLine();

    int numBytes();

    int numElements();

    String name();

    String baseType();

    int glType();

    int location();

    int offset();

    boolean isNormalized();

    int numLocations();

    Type type();

    T read(ByteBuffer source);

    void write(ByteBuffer dest, T value);

    void write(ByteBuffer dest, float x);

    void write(ByteBuffer dest, float x, float y);

    void write(ByteBuffer dest, float x, float y, float z);

    void write(ByteBuffer dest, float x, float y, float z, float w);

    int hashCode();

    boolean equals(Object obj);

}