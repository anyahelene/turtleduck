package turtleduck.gl.objects;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.stb.STBTruetype;

import turtleduck.gl.objects.PolyPath.PolyPathWriter;

import static turtleduck.gl.objects.Util.ioResourceToByteBuffer;

public class Font {
	private STBTTFontinfo info;
	private ByteBuffer data;

	private Font(STBTTFontinfo info, ByteBuffer data) {
		this.info = info;
		this.data = data;
	}
	public static Font load(String pathName) throws IOException {
		ByteBuffer data = ioResourceToByteBuffer(pathName, 8 * 1024);
		STBTTFontinfo info = STBTTFontinfo.create();

		if(STBTruetype.stbtt_InitFont(info, data)) {
			return new Font(info, data);
		} else {
			throw new RuntimeException("Failed to load font " + pathName);
		}
	}

	public void dispose() {
		info.close();
		info = null;
	}

	public ByteBuffer getCodepointBitmap(int codepoint) {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer xoff = BufferUtils.createIntBuffer(1);
		IntBuffer yoff = BufferUtils.createIntBuffer(1);
		ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(info, 1, 1, codepoint, width, height, xoff, yoff);
		return bitmap;
	}

	public PolyPath getCodepointVertices(int codepoint) {
		PolyPath path = new PolyPath();
		PolyPathWriter p = path.writer();
		STBTTVertex.Buffer buffer = STBTruetype.stbtt_GetCodepointShape(info, codepoint);
		float scale = 1000f;

		for(STBTTVertex vert : buffer) {
			switch(vert.type()) {
			case STBTruetype.STBTT_vcubic:
			case STBTruetype.STBTT_vcurve:
			case STBTruetype.STBTT_vline:
				p.lineTo(vert.x()/scale, vert.y()/scale);
				break;
			case STBTruetype.STBTT_vmove:
				p.moveTo(vert.x()/scale, vert.y()/scale);
				break;
			}
		}
		p.close();


		buffer.free();
		return path;
	}


}
