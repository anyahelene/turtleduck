package turtleduck.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * From {@link org.lwjgl.demo.opengl.util.DemoUtils}.
 * 
 * <p>
 * Copyright © 2012-present Lightweight Java Game Library. All rights reserved.
 * License terms:
 * <a href="https://www.lwjgl.org/license">https://www.lwjgl.org/license</a>
 * 
 * @author <a href="https://www.lwjgl.org/">LWJGL</a>
 * 
 *
 */
public class IOUtil {
    public static final IOUtil instance = new IOUtil();
    private static final int BUFFER_SIZE = 8192;
    private Function<Integer, ByteBuffer> allocator;
    private boolean useGzip = true;

    public IOUtil() {
        this.allocator = null;
    }

    public IOUtil(Function<Integer, ByteBuffer> allocator) {
        this.allocator = allocator;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * 
     * @param url        the url to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     * 
     */
    public ByteBuffer urlToByteBuffer(URL url) throws IOException {
        ByteBuffer buffer;
        File file = new File(url.getFile());
        if (file.isFile() && file.length() > BUFFER_SIZE) {
            try (FileInputStream fis = new FileInputStream(file)) {
                try (FileChannel fc = fis.getChannel()) {
                    buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                }
            }
        } else {
            try (InputStream source = url.openStream()) {
                buffer = streamToByteBuffer(source, url.toString());
            }
        }
        return buffer;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * 
     * @param url        the url to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     * 
     */
    public ByteBuffer fileToByteBuffer(File file) throws IOException {
        return urlToByteBuffer(file.toURI().toURL());
    }

    public ByteBuffer streamToByteBuffer(InputStream source) throws IOException {
        return streamToByteBuffer(source, null);
    }

    /**
     * Read a stream into a ByteBuffer
     * 
     * @param source-The input stream
     * @param fileName   The file name (or null), for diagnostic purposes
     * @return A byteBuffer with the contents of the stream
     * @throws IOException
     */
    public ByteBuffer streamToByteBuffer(InputStream source, String fileName) throws IOException {
        if (source == null) {
            throw new FileNotFoundException(fileName != null ? fileName : "<input>");
        }
        if (useGzip && fileName != null && fileName.endsWith(".gz")) {
            source = new GZIPInputStream(source);
        }
        try {
            byte[] bytes = source.readAllBytes();

            if (allocator != null) {
                ByteBuffer buffer = allocator.apply(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                return buffer;
            } else {
                return ByteBuffer.wrap(bytes);
            }
        } catch (IOException e) {
            if (fileName != null)
                throw new IOException("While reading '" + fileName + "'", e);
            else
                throw e;
        }
    }

    /**
     * Read from stream into a specific ByteBuffer.
     * 
     * For normal operation, `size` should be the size of the file the `source` is
     * opened on, and `dest` should be a `ByteBuffer` with capacity of at least
     * `size`.
     * 
     * * If `size == 0`, `dest` will be reallocated to fit the available data.
     * 
     * * If `dest == null`, it will be allocated with the current allocator.
     * 
     * @param source   Source InputStream
     * @param dest     Destination ByteBuffer, or null
     * @param size     Expected file size, or 0
     * @param fileName The file name (or null), for diagnostic purposes
     * @return `dest`, or a newly allocated ByteBuffer, filled with the contents of
     *         the stream.
     * @throws IOException
     */
    public ByteBuffer streamToByteBuffer(InputStream source, ByteBuffer dest, int size, String fileName)
            throws IOException {
        int bufferSize = size == 0 ? BUFFER_SIZE : size;
        if (source == null) {
            throw new FileNotFoundException(fileName != null ? fileName : "<input>");
        }

        try {
            byte[] bytes = new byte[bufferSize];
            int totalRead = 0;
            while (true) {
                int bytesRead = source.read(bytes, 0, bytes.length);
                if (bytesRead == -1) {
                    break;
                }
                totalRead += bytesRead;
                if (dest == null || (size == 0 && dest.remaining() < bytesRead)) {
                    int needed = bytesRead < bufferSize ? totalRead : (int) (totalRead * 1.5);
                    dest = resizeBuffer(dest, Math.max(size, needed));
                }
                dest.put(bytes, 0, bytesRead);
            }
        } catch (IOException e) {
            if (fileName != null)
                throw new IOException("While reading '" + fileName + "'", e);
            else
                throw e;
        }
        if (dest == null)
            dest = resizeBuffer(null, 0);
        dest.flip();
        return dest;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * 
     * @param resourcePath the resource to read
     * @param clazz
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     * 
     */
    public ByteBuffer resourceToByteBuffer(String resourcePath, Class<?> clazz) throws IOException {
        try (InputStream source = clazz.getResourceAsStream(resourcePath)) {
            return streamToByteBuffer(source, resourcePath);
        }

    }

    private ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = allocator.apply(newCapacity);
        if (buffer != null) {
            buffer.flip();
            newBuffer.put(buffer);
        }
        return newBuffer;
    }

    public IOUtilReader fromFile(File file) {
        ByteReader br = new ByteReader();
        br.fileName = file.toString();
        br.allocator = allocator;
        if (file.isFile()) {
            br.knownSize = file.length();
        }
        try {
            br.stream = file.toURI().toURL().openStream();
        } catch (IOException e) {
            br.error = e;
        }
        return br;
    }

    public IOUtilReader fromURI(URI uri) {
        ByteReader br = new ByteReader();
        br.fileName = uri.toString();
        br.allocator = allocator;
        try {
            br.stream = uri.toURL().openStream();
        } catch (IOException e) {
            br.error = e;
        }
        return br;
    }

    public IOUtilReader fromStream(InputStream stream) {
        ByteReader br = new ByteReader();
        br.stream = stream;
        br.allocator = allocator;
        return br;
    }

    static class ByteReader implements IOUtilReader {
        String fileName;
        InputStream stream;
        long knownSize;
        ByteBuffer dest;
        IOException error;
        Function<Integer, ByteBuffer> allocator;

        @Override
        public IOUtilReader allocator(Function<Integer, ByteBuffer> allocator) {
            this.allocator = allocator;
            return this;
        }

        @Override
        public IOUtilReader fileName(String fileName) {
            if (fileName != null)
                this.fileName = fileName;
            return this;
        }

        @Override
        public IOUtilReader knownSize(long knownSize) {
            this.knownSize = knownSize;
            return this;
        }

        @Override
        public byte[] toBytes() throws IOException {
            try (InputStream s = ensureStream()) {
                // FileInputStream::readAllBytes() allocates the full array size if possible
                return s.readAllBytes();
            }
        }

        @Override
        public Stream<String> toLines() throws IOException {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ensureStream(), Charset.forName("UTF-8")))) {
                return reader.lines();
            }
        }

        @Override
        public ByteBuffer toByteBuffer() throws IOException {
            byte[] bytes = toBytes();

            if (dest != null) {
                dest.put(bytes);
                dest.flip();
            } else {
                dest = ByteBuffer.wrap(bytes);
            }
            return dest;
        }

        private IOException wrapError(IOException e) {
            if (fileName != null)
                return new IOException("While reading '" + fileName + "':", error);
            else
                return error;
        }

        private InputStream ensureStream() throws IOException {
            if (error != null) {
                throw wrapError(error);
            }

            if (stream == null) {
                throw new FileNotFoundException(fileName != null ? fileName : "<input>");
            }
            try {
                return fileName.endsWith(".gz") ? new GZIPInputStream(stream) : stream;
            } catch (IOException e) {
                throw wrapError(e);
            } finally {
                stream = null;
            }

        }
    }
}
