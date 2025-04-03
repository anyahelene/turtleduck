package turtleduck.colors;

import org.joml.Matrix3d;
import org.joml.Vector3d;

import turtleduck.annotations.Upd;

public interface WhitePoint {
    WhitePoint D65 = new WhitePointImpl(new Matrix3d(//
            3.240479, -1.537150, -0.498535, //
            -0.969256, 1.875992, 0.041556, //
            0.055648, -0.204043, 1.057311) //
            , 6500);

    double kelvin();

    /**
     * Transform a vector from XYZ tristimulus values to linear RGB colour, using this white point.
     * 
     * @param xyz A vector of tristimulus values to be converted
     * @return The same vector, transformed to linear RGB
     */
    Vector3d transformXYZtoRGB(@Upd Vector3d xyz);

    static record WhitePointImpl(Matrix3d matrix, double temperature) implements WhitePoint {

        @Override
        public double kelvin() {
            return temperature;
        }

        @Override
        public Vector3d transformXYZtoRGB(Vector3d xyz) {
            return matrix.transform(xyz);
        }

    }
}
