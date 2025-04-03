package turtleduck.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface IOUtilReader {

    IOUtilReader fileName(String fileName);

    IOUtilReader knownSize(long knownSize);

    byte[] toBytes() throws IOException;

    ByteBuffer toByteBuffer() throws IOException;

    IOUtilReader allocator(Function<Integer, ByteBuffer> allocator);

    Stream<String> toLines() throws IOException;

}