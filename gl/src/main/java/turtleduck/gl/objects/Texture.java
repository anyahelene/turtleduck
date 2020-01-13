package turtleduck.gl.objects;

import static turtleduck.gl.objects.Util.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL40.*;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;

public class Texture extends DataHandle<Texture, Texture.TextureData> {
	private static final Map<String, TextureData> textures = new HashMap<>();
	private TextureWriter writer = null;

	private Texture(TextureData data) {
		super(data);
	}

	public static TextureParams create() {
		return new TextureParams();
	}

	public static Texture loadWithDefaults(String pathName) throws IOException {
		return loadWithParams(pathName, null);
	}

	public static Texture loadWithParams(String pathName, TextureParams params) throws IOException {
		if (textures.containsKey(pathName)) {
			TextureData td = textures.get(pathName);
			// TODO: check that params match
			return new Texture(td.open());
		}
		if (params == null) {
			params = new TextureParams();
		}
		int tex = glGenTextures();
		ByteBuffer imageBuffer = null;
		ByteBuffer image = null;
		try {
			glBindTexture(params.type, tex);
			glTexParameteri(params.type, GL_TEXTURE_WRAP_S, params.wrapS);
			if (params.dim > 1) {
				glTexParameteri(params.type, GL_TEXTURE_WRAP_T, params.wrapT);
			}
			if (params.dim > 2) {
				glTexParameteri(params.type, GL_TEXTURE_WRAP_R, params.wrapR);
			}
			glTexParameteri(params.type, GL_TEXTURE_MAG_FILTER, params.magFilter);
			glTexParameteri(params.type, GL_TEXTURE_MIN_FILTER, params.minFilter);
			IntBuffer w = BufferUtils.createIntBuffer(1);
			IntBuffer h = BufferUtils.createIntBuffer(1);
			IntBuffer comp = BufferUtils.createIntBuffer(1);
			imageBuffer = ioResourceToByteBuffer(pathName, 8 * 1024);

			if (!stbi_info_from_memory(imageBuffer, w, h, comp)) {
				throw new IOException("Failed to read image information: " + stbi_failure_reason());
			}
			int channels = ((IntBuffer) comp.rewind()).get();
			comp.rewind();
			image = stbi_load_from_memory(imageBuffer, w, h, comp, channels);
			imageBuffer = null;
			if (image == null) {
				throw new IOException("Failed to load image: " + stbi_failure_reason());
			}
			int intformat = GL_RGB8, format = GL_RGB;
			if (channels == 4) {
				intformat = GL_RGBA8;
				format = GL_RGBA;
			} else if (channels == 1) {
				intformat = GL_R8;
				format = GL_RED;
			}
			glTexImage2D(params.type, 0, intformat, w.get(0), h.get(0), 0, format, GL_UNSIGNED_BYTE, image);
			stbi_image_free(image);
			image = null;
			if (params.genMipMap) {
				glGenerateMipmap(params.type);
			}

			TextureData data = new TextureData(tex, params.type, w.get(0), h.get(0), 0, channels, pathName);
			textures.put(pathName, data);
			tex = 0;
			return new Texture(data);
		} finally {
			if (tex != 0) {
				glDeleteTextures(tex);
				textures.remove(pathName);
			}
			if (image != null) {
				stbi_image_free(image);
			}
		}
	}

	public static Texture createBufferWithParams(String name, ByteBuffer buf, int format, TextureParams params) {
		if (params == null) {
			params = new TextureParams();
		}
		int tex = glGenTextures();
		try {
			glBindTexture(params.type, tex);
			glTexParameteri(params.type, GL_TEXTURE_WRAP_S, params.wrapS);
			if (params.width <= 0) {
				throw new IllegalArgumentException("Width must be > 0");
			}
			if (params.dim > 1) {
				glTexParameteri(params.type, GL_TEXTURE_WRAP_T, params.wrapT);
				if (params.height <= 0) {
					throw new IllegalArgumentException("Height must be > 0");
				}
			}
			if (params.dim > 2) {
				glTexParameteri(params.type, GL_TEXTURE_WRAP_R, params.wrapR);
				if (params.depth <= 0) {
					throw new IllegalArgumentException("Depth must be > 0");
				}
			}
			glTexParameteri(params.type, GL_TEXTURE_MAG_FILTER, params.magFilter);
			glTexParameteri(params.type, GL_TEXTURE_MIN_FILTER, params.minFilter);

			if (params.genMipMap) {
				glGenerateMipmap(params.type);
			}
			if (buf == null) {
				if (params.multisample) {
					glTexImage2DMultisample(params.getType(), 4, GL_RGB8, params.width, params.height, true);
				} else {
					glTexImage2D(params.type, 0, GL_RGBA8, params.width, params.height, params.depth, GL_RGBA,
							GL_UNSIGNED_BYTE, (ByteBuffer) null);
				}
				// for depth buffer:
				// glTexImage2D(params.type, 0, GL_DEPTH_COMPONENT, params.width, params.height,
				// params.depth, GL_DEPTH_COMPONENT, GL_FLOAT (?), (ByteBuffer)null);

				// for stencil buffer:
				// glTexImage2D(params.type, 0, GL_STENCIL_ATTACHMENT, params.width,
				// params.height, params.depth, GL_STENCIL_ATTACHMENT, GL_UNSIGNED_BYTE,
				// (ByteBuffer)null);

				// for depth+stencil buffer:
				// glTexImage2D(params.type, 0, GL_DEPTH24_STENCIL8, params.width,
				// params.height, params.depth, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8,
				// (ByteBuffer)null);
			} else {
				if (format == 0) {
					format = formatFromChannels(params.channels);
				}
				int intformat = internalFormatFromFormat(format);
				glTexImage2D(params.type, 0, intformat, params.width, params.height, 0, format, GL_UNSIGNED_BYTE, buf);
			}
			TextureData data = new TextureData(tex, params.type, params.width, params.height, params.depth, 3, name);
			tex = 0;
			glBindTexture(params.type, 0);
			return new Texture(data);
		} finally {
			if (tex != 0) {
				glDeleteTextures(tex);
			}
		}
	}

	public static int formatFromChannels(int nChannels) {
		switch (nChannels) {
		case 0:
		case 4:
			return GL_RGBA8;
		case 1:
			return GL_R8;
		case 2:
			return GL_RG8;
		case 3:
			return GL_RGB8;
		default:
			throw new IllegalArgumentException("" + nChannels);
		}
	}

	public static int internalFormatFromFormat(int format) {
		switch (format) {
		case GL_R8:
			return GL_RED;
		case GL_RG8:
			return GL_RG;
		case GL_RGB8:
			return GL_RGB;
		case GL_RGBA8:
			return GL_RGBA;
		default:
			throw new UnsupportedOperationException("Don't know which format corresponds to " + format);
		}
	}

	public int getId() {
		return data().id();
	}

	public int getWidth() {
		return data().width;
	}

	public int getHeight() {
		return data().height;
	}

	public int getChannels() {
		return data().channels;
	}

	public String getName() {
		return data().name;
	}

	public String getPath() {
		return data().name;
	}

	/**
	 * framebuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0)
	 */
	public void framebuffer() {
		framebuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0);
	}

	/**
	 * Attach texture to the framebuffer
	 *
	 * @param target     GL_FRAMEBUFFER, GL_READ_FRAMEBUFFER or GL_DRAW_FRAMEBUFFER
	 * @param attachment Normally GL_COLOR_ATTACHMENT0; GL_DEPTH_ATTACHMENT for
	 *                   depth buffer, GL_STENCIL_ATTACHMENT for stencil buffer,
	 *                   GL_DEPTH_STENCIL_ATTACHMENT for both
	 */
	public void framebuffer(int target, int attachment) {
		TextureData data = data();
		glFramebufferTexture2D(target, attachment, data.type, data.id(), 0);
	}

	public void bind(int textureNum) {
		TextureData data = data();
		glActiveTexture(textureNum);
		glBindTexture(data.type, data.id());
	}

	public boolean hasAlpha() {
		return data().channels == 4;
	}

	public int dimensions() {
		switch (data().type) {
		case GL_TEXTURE_1D:
			return 1;
		case GL_TEXTURE_2D:
			return 2;
		case GL_TEXTURE_3D:
			return 3;
		default:
			throw new IllegalStateException("texture type " + data().type);
		}
	}

	static class TextureData extends DataObject {
		final int width;
		final int height;
		final int depth;
		final int channels;

		public TextureData(int tex, int t, int w, int h, int d, int c, String name) {
			super(tex, t, name);
			System.out.println("new texture: " + name + " id=" + tex);
			width = w;
			height = h;
			depth = d;
			channels = c;
		}

	}

	static class TextureParams {
		int magFilter = GL_LINEAR;
		int minFilter = GL_NEAREST_MIPMAP_LINEAR;
		int wrapS = GL_REPEAT;
		int wrapT = GL_REPEAT;
		int wrapR = GL_REPEAT;
		int type = GL_TEXTURE_2D;
		int dim = 2;
		int width = 0, height = 0, depth = 0;
		int channels = 0;
		boolean multisample = false;
		boolean genMipMap = true;

		public int getType() {
			switch (dim + (multisample ? 10 : 0)) {
			case 1:
				return GL_TEXTURE_1D;
			case 2:
				return GL_TEXTURE_2D;
			case 3:
				return GL_TEXTURE_3D;
			case 12:
				return GL_TEXTURE_2D_MULTISAMPLE;
			default:
				throw new IllegalStateException();
			}
		}

		public void dim(int d) {
			if (d < 1 || d > 3) {
				throw new IllegalArgumentException("" + d);
			}
			dim = d;
		}

		public TextureParams size(int width) {
			this.width = width;
			return this;
		}

		public TextureParams size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public TextureParams size(int width, int height, int depth) {
			this.width = width;
			this.height = height;
			this.depth = depth;
			return this;
		}

		public TextureParams multisample() {
			multisample = true;
			return this;
		}

		public TextureParams multisample(boolean enable) {
			multisample = enable;
			return this;
		}

		public TextureParams magFilter(int filter) {
			magFilter = filter != 0 ? filter : GL_LINEAR;
			return this;
		}

		public TextureParams minFilter(int filter) {
			minFilter = filter != 0 ? filter : GL_NEAREST_MIPMAP_LINEAR;
			return this;
		}

		public TextureParams filter(int filter) {
			minFilter = filter != 0 ? filter : GL_LINEAR;
			magFilter = filter != 0 ? filter : GL_NEAREST_MIPMAP_LINEAR;
			return this;
		}

		public TextureParams nearest() {
			filter(GL_NEAREST);
			return this;
		}

		public TextureParams linear() {
			filter(GL_LINEAR);
			return this;
		}

		public TextureParams channels(int nChannels) {
			channels = nChannels;
			return this;
		}

		public TextureParams mirroredRepeat() {
			wrap(GL_MIRRORED_REPEAT, GL_MIRRORED_REPEAT, GL_MIRRORED_REPEAT);
			return this;
		}

		public TextureParams repeat() {
			wrap(GL_REPEAT, GL_REPEAT, GL_REPEAT);
			return this;
		}

		public TextureParams clamp() {
			wrap(GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
			return this;
		}

		public TextureParams wrap(int sWrapMode, int tWrapMode, int rWrapMode) {
			wrapS = sWrapMode != 0 ? sWrapMode : GL_REPEAT;
			wrapT = tWrapMode != 0 ? tWrapMode : GL_REPEAT;
			wrapR = rWrapMode != 0 ? rWrapMode : GL_REPEAT;
			return this;
		}

		public TextureParams mipmap(boolean generate) {
			genMipMap = generate;
			return this;
		}

		public Texture load(String pathName) throws IOException {
			return loadWithParams(pathName, this);
		}

		public Texture read(ByteBuffer buf) {
			return createBufferWithParams(String.valueOf(hashCode()), buf, 0, this);
		}

		public Texture createBuffer() {
			return createBufferWithParams(String.valueOf(hashCode()), null, 0, this);
		}
	}

	@Override
	protected void dispose(int id, TextureData data) {
		glDeleteTextures(id);
	}

	@Override
	public void bind() {
		TextureData data = data();
		glBindTexture(data.type, data.id());
	}

	@Override
	protected Texture create(TextureData data) {
		return new Texture(data);
	}

	public TextureWriter writer() {
		if (writer == null) {
			writer = new TextureWriter();
		}

		return writer.open();
	}

	protected class TextureWriter implements AutoCloseable {
		protected TextureData data;
		protected ByteBuffer buffer;
		protected int width, height, channels;

		TextureWriter() {
			width = getWidth();
			height = getHeight();
			channels = 4; // getChannels();

			buffer = BufferUtils.createByteBuffer(width * height * channels);
			// glBindTexture(data.type, data.id());
			// glGetTexImage(data.type, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			// glBindTexture(data.type, 0);
			// for(int i = 0; i < buffer.capacity(); i++) {
			// buffer.put((byte) 0);
			// }
			// buffer.rewind();
			buffer.limit(width * height * channels);
			buffer.rewind();
		}

		protected TextureWriter open() {
			if (data != null) {
				throw new IllegalStateException("Writer already in use");
			}
			data = data().open();
			return this;
		}

		private int idx(int x, int y) {
			return (x + y * width) * channels;
		}

		public void set(int x, int y, int r, int g, int b, int a) {
			if (data == null) {
				throw new IllegalStateException("Writer no longer in use");
			}
			int i = idx(x, y);
			// System.out.println("(" + x + "," + y + ")=" + i + ": " + r + ", " + g + ", "
			// + b + ", " + a);
			buffer.put(i, (byte) r);
			buffer.put(i + 1, (byte) g);
			buffer.put(i + 2, (byte) b);
			buffer.put(i + 3, (byte) a);
		}

		public void done() {
			buffer.rewind();
			glBindTexture(data.type, data.id());
			System.out.println("binding texture for write: " + data.id());
			// for(int i = 0; i < buffer.capacity(); i++) {
			// System.out.print(buffer.get(i) + " ");
			// }
			// System.out.println();
			buffer.rewind();
			glTexImage2D(data.type, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			glBindTexture(data.type, 0);
			data.close();
			data = null;
		}

		@Override
		public void close() {
			done();
		}
	}
}
