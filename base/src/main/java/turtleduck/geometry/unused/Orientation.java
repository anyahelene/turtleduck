package turtleduck.geometry.unused;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import turtleduck.colors.Colors;
import turtleduck.geometry.Direction;
import turtleduck.geometry.impl.AngleImpl;
import turtleduck.terminal.TerminalInputStream;
import turtleduck.terminal.TerminalPrintStream;

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
		TerminalInputStream stream = new TerminalInputStream();
		TerminalPrintStream p = new TerminalPrintStream(null);
		stream.write("foobar");
		BufferedReader stream2 = new BufferedReader(new InputStreamReader(stream));
		try {
			p.println(stream2.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.print(42);
		p.println("42");
		p.write(new byte[] {(byte) 0xf0, (byte) 0x90, (byte) 0x8d, (byte) 0x88});
		p.write(new byte[] {0x24});
		p.write(new byte[] {(byte) 0xc2, (byte) 0xa2});
		p.write(new byte[] {(byte) 0xe0, (byte) 0xa4, (byte) 0xb9});
		p.write(new byte[] {(byte) 0xe2, (byte) 0x82, (byte) 0xac});
		p.write(new byte[] {(byte) 0xed, (byte) 0x95, (byte) 0x9c});
		int isum = 0, dsum = 0;
		for (int k = 0; k < 2; k++) {
			int sum = 0;
			long t = System.currentTimeMillis();
			for (int j = 0; j < 512; j++)
				for (int i = 0; i < 16384; i++) {
					Colors.Gamma.gammaCompress(i, 16383);
				}
			isum += (System.currentTimeMillis() - t);
			System.out.println("i: " + (System.currentTimeMillis() - t) + "    " + isum);
			 t = System.currentTimeMillis();
			for (int j = 0; j < 512; j++)
				for (int i = 0; i < 16384; i++) {
					Colors.Gamma.gammaCompress(i/16383.0);
				}
			dsum += (System.currentTimeMillis() - t);
			System.out.println("d: " + (System.currentTimeMillis() - t) + "    " + dsum);
		}
//		System.exit(0);
		for (int i = 0; i <= 255; i++) {
			double d = i / 255.0;
			double l = Colors.Gamma.gammaExpand(d);
			int li = Colors.Gamma.gammaExpand(i, 255);
			int lo = Colors.Gamma.gammaCompress(li, 4095);
			double dl = Colors.Gamma.gammaCompress(li / 4095.0);
			System.out.printf("%6s → %6s → %6s → %6s → % .13f → % .13f\n", i, li, lo, d, l, dl);
		}
		System.exit(0);
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

//		System.exit(0);
		for (int i = -72; i <= 72; i++) {
			for (int j = -1; j <= 1; j++) {
				int b = AngleImpl.degreesToMilliArcSec(i * 5) + j;
				System.out.printf("%4d° = %6.2f° = %12d = %17.12f° = %15.12f\n", i * 5, //
						Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(i * 5)), Math.cos(Math.toRadians(i * 5)))), //
						b, AngleImpl.milliArcSecToDegrees(b), AngleImpl.milliArcSecToRadians(b));
			}
		}
		int a = -540;
		int m = AngleImpl.degreesToMilliArcSec(a);
		System.out.printf("%4d° = %6.2f° = %12d = %17.12f° = %15.12f\n", a, //
				Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(a)), Math.cos(Math.toRadians(a)))), m,
				AngleImpl.milliArcSecToDegrees(m), AngleImpl.milliArcSecToRadians(m));
//		System.exit(0);
		Direction b = Direction.absolute(90);
		System.out.println(b);
		for (int i = 0; i < 50; i++) {
			double d = i * 15;
			Direction abs = Direction.absolute(d);
			Direction rel = Direction.relative(d);
			System.out.printf("a=%9.6f, abs=%s (%4.2f), dx=%5.2f, dy=%5.2f, rel=%s, nav=%7s, rnv=%7s\n", d, abs,
					abs.degrees(), abs.dirX(), abs.dirY(), rel, abs.toNavString(), rel.toNavString());
			System.out.println(abs.add(rel));
			System.out.printf("%.2f,%.2f → %7.2f°\n", abs.dirX(), abs.dirY(),
					Math.toDegrees(Math.atan2(abs.dirX(), -abs.dirY())));
		}
//	for(Double d : Arrays.asList(0.0, Math.PI, -Math.PI, Math.PI-1e-6, Math.PI+1e-6, 2*Math.PI-1e-6, 2*Math.PI-1e-6, 2*Math.PI)) {
	}
}
