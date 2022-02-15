package turtleduck.gl;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import turtleduck.buffer.DataField;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.geometry.Orientation;
import turtleduck.gl.GLLayer.DrawObject;
import turtleduck.gl.GLLayer.GLPathWriter;
import turtleduck.gl.GLLayer.GLPathWriter3;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.VertexArray;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.Pen;

public class PathRenderer3 {
	static final float HALF_PI = (float) (Math.PI / 2);
	static final float TWO_PI = (float) (Math.PI * 2);
	private ShaderProgram shader;
	private Vector3d fromVec = new Vector3d();
	private Vector3d fromDir = new Vector3d();
	private Vector3d toVec = new Vector3d();
	private Vector3f tmp2 = new Vector3f();
//	private Vector3f offset = new Vector3f();
	private Vector3d normal = new Vector3d();
	private Vector2f texCoord = new Vector2f();
	private Vector3f tmp = new Vector3f();
	private DataField<Vector3f> aPos;
	private DataField<Color> aColor;
	private DataField<Vector3f> aNormal;
	private DataField<Vector2f> aTexCoord;
	private List<Integer> indices = new ArrayList<>();
	private int index = -1;
	private VertexArray array;

	public PathRenderer3(ShaderProgram program) {
		this.shader = program;
		VertexArrayFormat format = shader.format();
		aPos = format.setField("aPos", Vector3f.class);
		aColor = format.setField("aColor", Color.class);
		aNormal = format.setField("aNormal", Vector3f.class);
		aTexCoord = format.setField("aTexCoord", Vector2f.class);
	}

	public void drawPaths(GLPathWriter paths, DrawObject obj) {
		array = obj.array;
		index = obj.offset;
		PathStroke stroke;
		indices.clear();
		while ((stroke = paths.nextStroke()) != null) {
			List<PathPoint> points = stroke.points();

			if (points.size() > 1) {
				PathPoint from = points.get(0);
				PathPoint to = points.get(1);
				Pen pen = from.pen();

				for (int i = 1; i < points.size(); i++) {
					from = points.get(i - 1);
					to = points.get(i);

					drawLine(from, to, pen, i == 1, i == points.size() - 1);
					obj.blend |= pen.strokeColor().opacity() < 1;

					pen = to.pen();

				}
			}
		}
		obj.nVertices = obj.array.nVertices() - obj.offset;
		obj.indices = indices.stream().mapToInt(i -> i).toArray();
	}

	public void drawLine(PathPoint from, PathPoint to, Pen pen, boolean isStart, boolean isEnd) {
		from.point().toVector(fromVec);
		to.point().toVector(toVec);
		if (fromVec.equals(toVec))
			return;
		Orientation orient = from.orientation();
		if(orient == null)
			orient = Orientation.orientation(from.point(), to.point());
		orient.directionVector(fromDir);
		float w = (float) (pen.strokeWidth() / 2);
		Color color = pen.strokeColor();
//		fromDir.add(toDir).mul(.5f);
		orient.normalVector(normal);

		if (Double.isNaN(fromVec.lengthSquared()))
			System.out.println("fromVec: " + fromVec);
		if (Double.isNaN(toVec.lengthSquared()))
			System.out.println("toVec: " + toVec);

		float x = (float) fromDir.x, y = (float) fromDir.y, z = (float) fromDir.z;
		float n = Math.max(3, (int) Math.log((4 * w) * (4 * w)));
		n = 4;
		if (isStart) {
			int idx = index;
			for (int i = 3; i <= n; i++) {
				indices.add(index);
				indices.add(idx + 2);
				indices.add(idx + 4);
				idx += 2;
			}
		} else {
			int previdx = index - (int) (2 * (n + 1));
			int idx = index;
			for (float i = 0; i < n; i++) {
				indices.add(previdx + 3);
				indices.add(previdx + 1);
				indices.add(idx + 0);
				indices.add(idx + 2);
				indices.add(previdx + 3);
				indices.add(idx + 0);
				previdx += 2;
				idx += 2;
			}
		}
		if (isEnd) {
			int idx = index;
			for (int i = 3; i <= n; i++) {
				indices.add(index + 1);
				indices.add(idx + 5);
				indices.add(idx + 3);
				idx += 2;
			}
		}

		for (float i = n; i >= 0; i--) {
			if (i == 0 || i == n)
				normal.get(tmp2);
			else {
				normal.get(tmp2);
				tmp2.rotateAxis((i / n) * TWO_PI, x, y, z);
			}
			array.begin()//
					.put(aPos, tmp.set(fromVec).fma(w, tmp2))//
					.put(aColor, color)//
					.put(aNormal, tmp2) //
					.put(aTexCoord, texCoord.set(i / n, 0))//
					.end();

			array.begin()//
					.put(aPos, tmp.set(toVec).fma(w, tmp2))//
					.put(aColor, color)//
					.put(aNormal, tmp2) //
					.put(aTexCoord, texCoord.set(i / n, 1))//
					.end();
			if (i != n) {
				indices.add(index + 0);
				indices.add(index + 1);
				indices.add(index + 2);
				indices.add(index + 2);
				indices.add(index + 1);
				indices.add(index + 3);
				index += 2;
			}

		}

		index += 2;
	}
}
