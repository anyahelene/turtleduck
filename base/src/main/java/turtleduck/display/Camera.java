package turtleduck.display;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class Camera {
	public final Vector4f defaultCameraPosition = new Vector4f(0f, 0f, 25f, 1f);
	private final Quaternionf defaultCameraOrientation = new Quaternionf(new AxisAngle4f(0, 0, 0, -1));
	public final Vector4f position = new Vector4f(defaultCameraPosition);
	public final Quaternionf orientation = new Quaternionf(defaultCameraOrientation);
	public final Matrix4f viewMatrix = new Matrix4f();
	public final Matrix4f viewMatrixInv = new Matrix4f();
	public final Matrix4f projectionMatrix = new Matrix4f();
	public final Matrix4f projectionMatrixInv = new Matrix4f();
	public double fov = 50;
	protected double zoom;
	public int revision = 0;
	Viewport viewport;

	public Camera(Viewport vp) {
		viewport = vp;
	}

	/**
	 * Reverse project a screen position to a position in object space.
	 *
	 * @param mousePos A screen position
	 * @return A new vector representing a position somewhere on a line through the
	 *         camera and the given screen position
	 */
	public Vector3f unproject(Vector2f mousePos) {
		Vector3f v = new Vector3f(mousePos, 0f);
		projectionMatrixInv.transformProject(v);
		viewMatrixInv.transformPosition(v);

		return v;
	}

	/**
	 * Reverse project a screen position to a position in object space, store result
	 * in dest.
	 *
	 * @param mousePos A screen position
	 * @return dest
	 */
	public Vector3f unproject(Vector2i mousePos, Vector3f dest) {
		dest.set(mousePos, 0);
		projectionMatrixInv.transformProject(dest);
		viewMatrixInv.transformPosition(dest);

		return dest;
	}

	/**
	 * Project pos according to current view and projection matrices.
	 *
	 * @param pos
	 * @return A new vector, P*V*pos
	 */
	public Vector3f project(Vector3f pos) {
		Vector3f v = new Vector3f(pos);
		viewMatrix.transformPosition(v);
		projectionMatrix.transformProject(v);

		return v;
	}

	public void zoomIn() {
		fov(fov / 1.05);
	}

	public void zoomOut() {
		fov(fov * 1.05);
	}

	public void fov(double fov) {
		if (fov < 10.0f) {
			this.fov = 10.0f;
		} else if (fov > 120.0f) {
			this.fov = 120.0f;
		} else {
			this.fov = fov;
		}
		this.zoom = 50.0 / this.fov;
		updateProjection();
	}

	public abstract void updateProjection();

	public abstract void updateView();

	public static class OrthoCamera extends Camera {

		public OrthoCamera(Viewport vp) {
			super(vp);
		}

		public void updateProjection() {
			float w = (float) viewport.width(), h = (float) viewport.height();
			projectionMatrix.setOrtho(-w / 2, w / 2, -h / 2, h / 2, -1, 1);
//			projectionMatrix.scale(1, 1, 1);
			projectionMatrix.scale((float) zoom);
			projectionMatrix.scale(((float) viewport.viewWidth()) / viewport.screenWidth(),
					((float) viewport.viewHeight()) / viewport.screenHeight(), 1);
			projectionMatrix.invertOrtho(projectionMatrixInv);
			revision++;
		}

		public void updateView() {
			orientation.get(viewMatrix);
			viewMatrix.translate(-position.x, -position.y, 0);

			viewMatrix.invertAffine(viewMatrixInv);
			revision++;
		}

	}

	public static class PerspectiveCamera extends Camera {
		public PerspectiveCamera(Viewport vp) {
			super(vp);
		}

		public void updateProjection() {
			float w = (float) viewport.width(), h = (float) viewport.height();
			projectionMatrix.setPerspective((float) Math.toRadians(fov), (float) viewport.aspect(), 1f, 1000.0f);
			projectionMatrix.invertPerspective(projectionMatrixInv);
			revision++;
		}

		public void updateView() {
			orientation.get(viewMatrix);
			viewMatrix.translate(-position.x, -position.y, -position.z).scale(1f / 16f);
			viewMatrix.invertAffine(viewMatrixInv);
			revision++;
		}

	}
}
