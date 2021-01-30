package turtleduck.bitmap;

import java.nio.ByteBuffer;

import org.joml.Vector4f; // G

class Bitmap_Template {
	private static final int BYTE_SIZE = 4;
	private ByteBuffer data;
	private final int width;
	private final int height;
	private final int channels;

	public Bitmap_Template(int width, int height, int channels, ByteBuffer data) {
		super();
		this.data = data;
		this.width = width;
		this.height = height;
		this.channels = channels;
	}

	int index(int x, int y) {
		return (x + y * width) * channels * BYTE_SIZE;
	}

	float get(int x, int y, int ch) {
		int idx = index(x, y);
		return data.getFloat(idx + ch * BYTE_SIZE);
	}

	float r(int x, int y) { // R
		int idx = index(x, y); // R
		return data.getFloat(idx + 0 * BYTE_SIZE); // R
	} // R

	float g(int x, int y) { // G
		int idx = index(x, y); // G
		return data.getFloat(idx + 1 * BYTE_SIZE); // G
	} // G

	float b(int x, int y) { // B
		int idx = index(x, y); // B
		return data.getFloat(idx + 2 * BYTE_SIZE); // B
	} // B

	float a(int x, int y) { // A
		int idx = index(x, y); // A
		return data.getFloat(idx + 3 * BYTE_SIZE); // A
	} // A

	void set(int x, int y, int ch, float value) {
		int idx = index(x, y);
		data.putFloat(idx + ch * BYTE_SIZE, value);
	}

	void r(int x, int y, float value) { // R
		int idx = index(x, y); // R
		data.putFloat(idx + 0 * BYTE_SIZE, value); // R
	} // R

	void g(int x, int y, float value) { // G
		int idx = index(x, y); // G
		data.putFloat(idx + 1 * BYTE_SIZE, value); // G
	} // G

	void b(int x, int y, float value) { // B
		int idx = index(x, y); // B
		data.putFloat(idx + 2 * BYTE_SIZE, value); // B
	} // B

	void a(int x, int y, float value) { // A
		int idx = index(x, y); // A
		data.putFloat(idx + 3 * BYTE_SIZE, value); // A
	} // A

	void foreachLocation(LocationConsumer consumer) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				consumer.accept(this, x, y);
			}
		}
	}

	void foreach(PixelConsumerFloat consumer) {
		int len = width * height * channels * BYTE_SIZE;
		for (int i = 0; i < len; i += channels * BYTE_SIZE) {
			consumer.accept(//
					data.getFloat(i) // R
					, data.getFloat(i + BYTE_SIZE) // G
					, data.getFloat(i + 2 * BYTE_SIZE) // B
					, data.getFloat(i + 3 * BYTE_SIZE) // A
			);
		}
	}

	void map(PixelFunctionFloat fun) { // G
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
	} // G

// 1	void map(PixelFunctionFloat fun) {
// 1		int len = width * height * channels * BYTE_SIZE;
// 1		for (int i = 0; i < len; i += channels * BYTE_SIZE) {
// 1			data.putFloat(i, fun.apply(data.getFloat(i)));
// 1		}
// 1	} 

	public interface PixelFunctionFloat { // G
		Vector4f apply(Vector4f p);// G
	}// G

// 1	public interface PixelFunctionFloat {
// 1		float apply(float x);
// 1	}

	public interface PixelConsumerFloat {
		void accept(//
				float r // R
				, float g // G
				, float b // B
				, float a // A
		);
	}

	public interface LocationConsumer {
		void accept(Bitmap_Template bitmap, int x, int y);
	}
}
