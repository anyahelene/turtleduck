package turtleduck.grid;

import static org.junit.jupiter.api.Assertions.*;
import static turtleduck.grid.GridDirection.*;

import org.junit.jupiter.api.Test;
class GridDirectionTest {

	@Test
	void degrees() {
		assertEquals(0, EAST.getDegrees());
		assertEquals(45, NORTHEAST.getDegrees());
		assertEquals(90, NORTH.getDegrees());
		assertEquals(135, NORTHWEST.getDegrees());
		assertEquals(180, WEST.getDegrees());
		assertEquals(225, SOUTHWEST.getDegrees());
		assertEquals(270, SOUTH.getDegrees());
		assertEquals(315, SOUTHEAST.getDegrees());
	}

	@Test
	void reverse() {
		assertEquals(WEST, EAST.reverse());
		assertEquals(SOUTHWEST, NORTHEAST.reverse());
		assertEquals(SOUTH, NORTH.reverse());
		assertEquals(SOUTHEAST, NORTHWEST.reverse());
		assertEquals(EAST, WEST.reverse());
		assertEquals(NORTHEAST, SOUTHWEST.reverse());
		assertEquals(NORTH, SOUTH.reverse());
		assertEquals(NORTHWEST, SOUTHEAST.reverse());
	}

	@Test
	void left90() {
		assertEquals(NORTH, EAST.left90());
		assertEquals(NORTHWEST, NORTHEAST.left90());
		assertEquals(WEST, NORTH.left90());
		assertEquals(SOUTHWEST, NORTHWEST.left90());
		assertEquals(SOUTH, WEST.left90());
		assertEquals(SOUTHEAST, SOUTHWEST.left90());
		assertEquals(EAST, SOUTH.left90());
		assertEquals(NORTHEAST, SOUTHEAST.left90());
	}

	@Test
	void right90() {
		assertEquals(NORTH, EAST.right90().reverse());
		assertEquals(NORTHWEST, NORTHEAST.right90().reverse());
		assertEquals(WEST, NORTH.right90().reverse());
		assertEquals(SOUTHWEST, NORTHWEST.right90().reverse());
		assertEquals(SOUTH, WEST.right90().reverse());
		assertEquals(SOUTHEAST, SOUTHWEST.right90().reverse());
		assertEquals(EAST, SOUTH.right90().reverse());
		assertEquals(NORTHEAST, SOUTHEAST.right90().reverse());
	}
	void left45() {
		assertEquals(NORTH, EAST.left45().left45());
		assertEquals(NORTHWEST, NORTHEAST.left45().left45());
		assertEquals(WEST, NORTH.left45().left45());
		assertEquals(SOUTHWEST, NORTHWEST.left45().left45());
		assertEquals(SOUTH, WEST.left45().left45());
		assertEquals(SOUTHEAST, SOUTHWEST.left45().left45());
		assertEquals(EAST, SOUTH.left45().left45());
		assertEquals(NORTHEAST, SOUTHEAST.left45().left45());
	}

	@Test
	void right45() {
		assertEquals(NORTH, EAST.right45().right45().reverse());
		assertEquals(NORTHWEST, NORTHEAST.right45().right45().reverse());
		assertEquals(WEST, NORTH.right45().right45().reverse());
		assertEquals(SOUTHWEST, NORTHWEST.right45().right45().reverse());
		assertEquals(SOUTH, WEST.right45().right45().reverse());
		assertEquals(SOUTHEAST, SOUTHWEST.right45().right45().reverse());
		assertEquals(EAST, SOUTH.right45().right45().reverse());
		assertEquals(NORTHEAST, SOUTHEAST.right45().right45().reverse());
	}
}
