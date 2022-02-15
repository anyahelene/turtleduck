package turtleduck.geometry;

import org.joml.Vector3d;

public interface PositionVector {
	double x();
	double y();
	double z();
	
	default Vector3d toVector(Vector3d dest) {
		return new Vector3d(x(), y(), z());
	}
}
