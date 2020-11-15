package turtleduck.scene.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import turtleduck.geometry.Box3;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.scene.Camera;
import turtleduck.scene.RenderContext;
import turtleduck.scene.SceneContainer;
import turtleduck.scene.SceneGroup3;
import turtleduck.scene.SceneNode;
import turtleduck.scene.SceneObject3;
import turtleduck.scene.SceneVisitor;
import turtleduck.scene.SceneWorld;

public abstract class SceneImpl implements SceneNode {

//	@Override
//	public <U, C extends RenderContext> U accept(SceneVisitor<U, C> visitor, C context) {
//		return visitor.visitNode(this, context);
//	}

	public static class Container<T extends Container<T>> extends SceneImpl implements SceneContainer<T> {
		protected List<SceneNode> elements = new ArrayList<>();

		@Override
		public <U, C extends RenderContext<C>> U accept(SceneVisitor<U, C> visitor, C context) {
			return visitor.visitContainer((SceneContainer<?>) this, context);
		}

		@Override
		public Iterator<SceneNode> iterator() {
			return elements.iterator();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T add(SceneNode elt) {
			elements.add(elt);
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T remove(SceneNode elt) {
			elements.remove(elt);
			return (T) this;
		}

		@Override
		public int size() {
			return elements.size();
		}

		@Override
		public void forEach(Consumer<? super SceneNode> fun) {
			elements.forEach(fun);
		}

		@Override
		public <U> U reduce(U identity, BiFunction<U, ? super SceneNode, U> accumulator) {
			U result = identity;
			for (SceneNode n : elements) {
				result = accumulator.apply(result, n);
			}
			return result;
		}

	}

	public static class Group<T extends SceneGroup3<T>> extends Object3<T> implements SceneGroup3<T> {
		protected List<SceneNode> elements = new ArrayList<>();

		@Override
		public Iterator<SceneNode> iterator() {
			return elements.iterator();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T add(SceneNode elt) {
			elements.add(elt);
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T remove(SceneNode elt) {
			elements.remove(elt);
			return (T) this;
		}

		@Override
		public int size() {
			return elements.size();
		}

		@Override
		public void forEach(Consumer<? super SceneNode> fun) {
			elements.forEach(fun);
		}

		@Override
		public <U> U reduce(U identity, BiFunction<U, ? super SceneNode, U> accumulator) {
			U result = identity;
			for (SceneNode n : elements) {
				result = accumulator.apply(result, n);
			}
			return result;
		}

		@Override
		public <U, C extends RenderContext<C>> U accept(SceneVisitor<U, C> visitor, C context) {
			orientation.toQuaternion(quat);
			Matrix4f m = context.matrix();
			m.translate((float) position.x, (float) position.y, (float) position.z);
			m.scale(scale);
			m.rotateAroundAffine(quat, (float) pivot.x, (float) pivot.y, (float) pivot.z, m);
			return visitor.visitGroup3(this, context);
		}

		@Override
		public SceneObject3<?> createObject() {
			SceneObject3<?> obj =  new Object3();
			add(obj);
			return obj;
		}

		@Override
		public SceneGroup3<?> createGroup() {
			SceneGroup3<?> obj = new Group();
			add(obj);
			return obj;
		}

		@Override
		public Camera createCamera() {
			Camera obj = new CameraImpl();
			add(obj);
			return obj;
		}
	}

	public static class World extends Group<SceneWorld> implements SceneWorld {

	}

	public static class Object3<T extends SceneObject3<T>> extends SceneImpl implements SceneObject3<T> {
		protected Orientation orientation = Orientation.FORWARD;
		protected Quaternionf quat = new Quaternionf();
		protected Vector3d position = new Vector3d();
		protected Vector3d pivot = new Vector3d();
		protected float scale = 1;
		protected Point posPoint, pivotPoint;

		@Override
		public double x() {
			return position.x;
		}

		@Override
		public double y() {
			return position.y;
		}

		@Override
		public double z() {
			return position.z;
		}

		@Override
		public double dirX() {
			return 0;
		}

		@Override
		public double dirY() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double dirZ() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Point pivot() {
			if (pivotPoint == null)
				pivotPoint = Point.point(pivot);
			return pivotPoint;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T pivot(Point p) {
			p.toVector(pivot);
			pivotPoint = null;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T move(double dx, double dy, double dz) {
			position.add(dx, dy, dz);
			posPoint = null;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T moveTo(double x, double y, double z) {
			position.set(x, y, z);
			posPoint = null;
			return (T) this;
		}

		@Override
		public T move(Direction dir, double dist) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Point position() {
			if (posPoint == null)
				posPoint = Point.point(position);
			return posPoint;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T yaw(double angle) {
			orientation = orientation.yaw(angle);
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T pitch(double angle) {
			orientation = orientation.pitch(angle);
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T roll(double angle) {
			orientation = orientation.roll(angle);
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T yawTo(double angle) {
			// TODO Auto-generated method stub
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T pitchTo(double angle) {
			// TODO Auto-generated method stub
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T rollTo(double angle) {
			// TODO Auto-generated method stub
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T orient(Orientation dir) {
			// TODO Auto-generated method stub
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T orientTo(Orientation dir) {
			orientation = dir;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T scale(double xScale, double yScale, double zScale) {
			scale = (float) xScale;
			return (T) this;
		}

		@Override
		public Orientation orientation() {
			return orientation;
		}

		@Override
		public Point scale() {
			return Point.point(scale, scale, scale);
		}

		@Override
		public Box3 boundingBox() {
			// TODO Auto-generated method stub
			return null;
		}

		public Quaterniond toQuaternion(Quaterniond dest) {
			return orientation.toQuaternion(dest);
		}

		public Vector3d toVector(Vector3d dest) {
			return dest.set(position);
		}

		public Matrix4f toMatrix(Matrix4f dest) {
			orientation.toQuaternion(quat);
			dest.translation((float) position.x, (float) position.y, (float) position.z);
			dest.rotateAroundAffine(quat, (float) pivot.x, (float) pivot.y, (float) pivot.z,
					dest);
			return dest;
		}

		@Override
		public <U, C extends RenderContext<C>> U accept(SceneVisitor<U, C> visitor, C context) {
			orientation.toQuaternion(quat);
			Matrix4f m = context.matrix();
			m.translate((float) position.x, (float) position.y, (float) position.z);
			m.scale(scale);
			m.rotateAroundAffine(quat, (float) pivot.x, (float) pivot.y, (float) pivot.z, m);
			return visitor.visitObject3(this, context);
		}
	}

	public static class CameraImpl extends Object3<Camera> implements Camera {
		protected double fov = 30;
		private double farClip;
		private double nearClip;
		private boolean perspective = true;

		@Override
		public boolean isOrthographic() {
			return !perspective;
		}

		@Override
		public boolean isPerspective() {
			return perspective;
		}

		@Override
		public double fieldOfView() {
			return fov;
		}

		@Override
		public Camera orthographic() {
			perspective = false;
			return this;
		}

		@Override
		public Camera perspective() {
			perspective = true;
			return this;
		}

		@Override
		public Camera fieldOfView(double fov) {
			this.fov = fov;
			return this;
		}

		@Override
		public Camera farClip(double distance) {
			this.farClip = distance;
			return this;
		}

		@Override
		public Camera nearClip(double distance) {
			this.nearClip = distance;
			return this;
		}

		@Override
		public double farClip() {
			return farClip;
		}

		@Override
		public double nearClip() {
			return nearClip;
		}

		@Override
		public <U, C extends RenderContext<C>> U accept(SceneVisitor<U, C> visitor, C context) {
			orientation.toQuaternion(quat);
			Matrix4f m = context.matrix();
			m.translate((float) position.x, (float) position.y, (float) position.z);
			m.rotateAroundAffine(quat, (float) pivot.x, (float) pivot.y, (float) pivot.z, m);
			return visitor.visitObject3(this, context);
		}

		public Matrix4f toMatrix(Matrix4f dest) {
			orientation.toQuaternion(quat);
			dest.translation((float) position.x, (float) position.y, (float) position.z);
			dest.rotateAroundAffine(quat, (float) pivot.x, (float) pivot.y, (float) pivot.z,
					dest);
			return dest;
		}
	}
}
