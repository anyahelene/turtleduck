package turtleduck.geometry;

import java.util.Arrays;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Orientation {
	private Quaternionf quat = new Quaternionf().setAngleAxis(-Math.PI / 2, 0, 0, 1);// .lookAlong(0, -1, 0, 0, 0, 1);

	public double azimuth() {
		return quat.angle();
	}

	public Vec3 axisUp() {
		return null;
	}

	public Vec3 axisForward() {
		return null;
	}

	public static void main(String[] args) {
		Orientation orient = new Orientation();
		Vector3f euler = new Vector3f();
		orient.quat.getEulerAnglesXYZ(euler);
		float deg = (float) (180 / Math.PI);
		euler.mul(deg, deg, deg);
		System.out.println(euler);
		Vector3f v = new Vector3f();
		System.out.println(orient.quat.transformPositiveX(v));
		orient.quat.rotateLocalZ((float) (Math.PI / 2));
		System.out.println(orient.quat.transformPositiveX(v));
		orient.quat.rotateLocalZ((float) (Math.PI / 2));
		System.out.println(orient.quat.transformPositiveX(v));
		orient.quat.rotateLocalZ((float) (Math.PI / 2));
		System.out.println(orient.quat.transformPositiveX(v));
		orient.quat.rotateLocalX((float) (Math.PI / 2));
		System.out.println(orient.quat.transformPositiveX(v));
		orient.quat.rotateLocalZ((float) (Math.PI / 2));
		System.out.println(orient.quat.transformPositiveX(v));

		System.out.println(String.format("%.30f", Math.sin(Math.PI)));
		for (int i = 0; i <= 360; i += 5) {
			double y = -Math.cos(Math.toRadians(i)), x = Math.sin(Math.toRadians(i));
			int atan2 = MasBearing.atan2(x, y);
			System.out.printf("%4d → (%.25f,%.25f) → %10d %.25f %7.4f\n", i, x, y, atan2, MasBearing.sin(atan2), MasBearing.bradToDegrees(atan2));

		}
//		System.exit(0);
		for (int i = -72; i <= 72; i++) {
			for (int j = -1; j <= 1; j++) {
				int b = MasBearing.degreesToBrad(i * 5) + j;
				System.out.printf("%4d° = %6.2f° = %12d = %17.12f° = %15.12f\n", i * 5, //
						Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(i * 5)), Math.cos(Math.toRadians(i * 5)))), //
						b, MasBearing.bradToDegrees(b), MasBearing.bradToRadians(b));
			}
		}
		int a = -540;
		int m = MasBearing.degreesToBrad(a);
		System.out.printf("%4d° = %6.2f° = %12d = %17.12f° = %15.12f\n", a, //
				Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(a)), Math.cos(Math.toRadians(a)))), m,
				MasBearing.bradToDegrees(m), MasBearing.bradToRadians(m));
//		System.exit(0);
		Bearing b = Bearing.absolute(90);
		System.out.println(b);
		for (int i = 0; i < 50; i++) {
			double d = i * 15;
			Bearing abs = Bearing.absolute(d);
			Bearing rel = Bearing.relative(d);
			System.out.printf("a=%9.6f, abs=%s (%4.2f), dx=%5.2f, dy=%5.2f, rel=%s, nav=%7s, rnv=%7s\n", d, abs, abs.azimuth(),
					abs.dirX(), abs.dirY(), rel,
					abs.toNavString(), rel.toNavString());
			System.out.println(abs.add(rel));
			System.out.printf("%.2f,%.2f → %7.2f°\n", abs.dirX(), abs.dirY(),
					Math.toDegrees(Math.atan2(abs.dirX(), -abs.dirY())));
		}
//	for(Double d : Arrays.asList(0.0, Math.PI, -Math.PI, Math.PI-1e-6, Math.PI+1e-6, 2*Math.PI-1e-6, 2*Math.PI-1e-6, 2*Math.PI)) {
	}
}
