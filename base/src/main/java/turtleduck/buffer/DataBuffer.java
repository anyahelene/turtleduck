package turtleduck.buffer;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import turtleduck.colors.Color;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface DataBuffer {

	int putByte(int address, int value);

	int putShort(int address, int value);

	int putInt(int address, int value);

	int putLong(int address, int value);

	int putFloat(int address, float value);

	int putDouble(int address, double value);

	int putVec2(int address, float x, float y);

	default int putVec2(int address, Vector2fc value) {
		return putVec2(address, value.x(), value.y());
	}

	int putVec3(int address, float x, float y, float z);

	default int putVec3(int address, Vector2fc value, float z) {
		return putVec3(address, value.x(), value.y(), z);
	}

	default int putVec3(int address, Vector3fc value) {
		return putVec3(address, value.x(), value.y(), value.z());
	}

	int putVec4(int address, float x, float y, float z, float w);

	int putColor(int address, Color c);

	default int putVec2(int address, Direction dir) {
		return putVec2(address, (float) dir.dirX(), (float) dir.dirY());
	}

	default int putVec4(int address, Vector2fc value, float z, float w) {
		return putVec4(address, value.x(), value.y(), z, w);
	}

	default int putVec4(int address, Vector3fc value, float w) {
		return putVec4(address, value.x(), value.y(), value.z(), w);
	}

	default int putVec4(int address, Vector4fc value) {
		return putVec4(address, value.x(), value.y(), value.z(), value.w());
	}

	default int putVec2(int address, Point value) {
		return putVec2(address, (float) value.x(), (float) value.y());
	}

	default int putVec3(int address, Direction dir) {
		return putVec3(address, (float) dir.dirX(), (float) dir.dirY(), (float) dir.dirZ());
	}

	default int putVec3(int address, Point value) {
		return putVec3(address, (float) value.x(), (float) value.y(), (float) value.z());
	}

	default int putVec3(int address, Point value, double z) {
		return putVec3(address, (float) value.x(), (float) value.y(), (float) z);
	}

	default int putVec4(int address, Point value, double w) {
		return putVec4(address, (float) value.x(), (float) value.y(), (float) value.z(), (float) w);
	}

	default int putVec4(int address, Direction dir) {
		return putVec4(address, (float) dir.dirX(), (float) dir.dirY(), (float) dir.dirZ(), 0);
	}

}
