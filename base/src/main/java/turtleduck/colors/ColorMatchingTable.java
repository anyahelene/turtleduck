package turtleduck.colors;

import org.joml.Vector3d;

public interface ColorMatchingTable {

    Vector3d lookup(double wavelength, Vector3d dest);

}