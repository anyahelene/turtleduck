package turtleduck.display;

import org.joml.AxisAngle4f;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class Camera {
    public final Vector4f defaultCameraPosition = new Vector4f(0f, 0f, 768, 1);
    private final Quaternionf defaultCameraOrientation = new Quaternionf(new AxisAngle4f(0, 0, 0, -1));
    public final Vector4f position = new Vector4f(defaultCameraPosition);
    public final Quaternionf orientation = new Quaternionf(defaultCameraOrientation);
    public final Quaternionf worldOrientation = new Quaternionf(new AxisAngle4f(0, 0, 0, -1));
    public final Matrix4f viewMatrix = new Matrix4f();
    public final Matrix4f viewMatrixInv = new Matrix4f();
    public final Matrix4f projectionMatrix = new Matrix4f();
    public final Matrix4f projectionMatrixInv = new Matrix4f();
    public final Matrix4f projViewMatrix = new Matrix4f();
    public final Matrix4f projViewMatrixInv = new Matrix4f();
    public final FrustumIntersection frustum = new FrustumIntersection();
   public double fov = 50;
    protected double zoom;
    public int revision = 0;
    public final Viewport viewport;

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
        if (position.z > 16)
            position.z = Math.max(position.z - 25, 16);
        else
            fov(fov / 1.025);
        updateBoth();
    }

    public void zoomOut() {
        if (position.z < 1024)
            position.z = Math.min(position.z + 25, 1024);
        else
            fov(fov * 1.025);
        updateBoth();
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

    public void updateBoth() {
        updateProjection();
        updateView();
        projViewMatrix.set(projectionMatrix);
        projViewMatrix.mul(viewMatrix);
        projViewMatrix.invertAffine(projViewMatrixInv);
        frustum.set(projViewMatrix);
        
    }
    public static class OrthoCamera extends Camera {

        public OrthoCamera(Viewport vp) {
            super(vp);
        }

        public void updateProjection() {
            float w = (float) viewport.width(), h = (float) viewport.height();
            projectionMatrix.setOrtho(-w / 2, w / 2, -h / 2, h / 2, 0, -1);
//			projectionMatrix.scale(1, 1, 1);
            projectionMatrix.scale((float) zoom, (float) zoom, 1);
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
        float near, far;

        public PerspectiveCamera(Viewport vp) {
            super(vp);
        }

        public void updateProjection() {
            float w = (float) viewport.width(), h = (float) viewport.height();
            near = position.z / 2;
            far = position.z + 1024f;
            projectionMatrix.setPerspective((float) Math.toRadians(fov), (float) viewport.aspect(), near, far);
                   
            projectionMatrix.invertPerspective(projectionMatrixInv);
            revision++;

        }

        public void updateView() {
            orientation.get(viewMatrix);
            viewMatrix.identity();
            Vector4f pos = position;
            viewMatrix.rotate(orientation);
            viewMatrix.translate(-pos.x, -pos.y, -pos.z).scale(1f);
            viewMatrix.rotate(worldOrientation);
            viewMatrix.invertAffine(viewMatrixInv);
            // System.out.println(position);
            revision++;
        }

        public void updateBoth() {
            super.updateBoth();
            Vector4f vec = new Vector4f();
            vec.set(-1, -1, .99f, 1);
            projViewMatrixInv.transform(vec);
            vec.div(vec.w);
            System.out.print(vec + " ");
            Vector4f vec2 = new Vector4f();
            vec2.set(1, 1, 0, 1);
            projViewMatrixInv.transform(vec2);
            vec2.div(vec2.w);
            System.out.print(vec2 + " ");
            Vector4f vec3 = new Vector4f();
            vec3.set(-1, -1, 1, 1);
            projViewMatrixInv.transform(vec3);
            vec3.div(vec3.w);
            System.out.print(vec3 + " ");
            Vector4f vec4 = new Vector4f();
            vec4.set(-1, -1, -1, 1);
            projViewMatrixInv.transform(vec4);
            vec4.div(vec4.w);
            System.out.print(vec4 + " ");
            System.out.printf("%.2fx%.2fx%.2f%n", vec2.x-vec.x, vec2.y-vec.y,vec4.z-vec3.z);

        }
    }
}
