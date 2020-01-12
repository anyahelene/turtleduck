package turtleduck;

import turtleduck.text.Position;
import turtleduck.text.Region;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class RegionTest {
	private static Region page = Region.rectangular(1, 1, 80, 40);
	private static List<Region> regions = Arrays.asList(Region.rectangular(10, 10, 20, 20), //
			Region.flow(10, 10, 20, 20));

	@Test
	public void streamContains() {
		for (Region reg : regions)
			streamContainsProperty(reg);
	}

	public void streamContainsProperty(Region reg) {
		reg.posStream(page).forEach(pos -> {
			{
				assertTrue(reg.contains(pos), String.format("%s.contains(%s)", reg, pos));
			}
		});
		reg.posStream(page).parallel().forEach(pos -> {
			{
				assertTrue(reg.contains(pos), String.format("%s.contains(%s)", reg, pos));
			}
		});
	}

	@Test
	public void streamSize() {
		for (Region reg : regions) {
			streamSizeProperty(reg);
		}
	}

	public void streamSizeProperty(Region reg) {
		Stream<Position> stream = reg.posStream(page);
		long size = stream.spliterator().estimateSize();
		stream = reg.posStream(page);
		assertEquals(stream.count(), size);
		stream = reg.posStream(page).parallel();
		assertEquals(stream.count(), size);
	}

	@Test
	public void rectStreamSize() {
		for (Region reg : regions) {
			if (reg.isRectangular())
				rectStreamSizeProperty1(reg);
			else
				flowStreamSizeProperty1(reg);
		}
	}

	public void rectStreamSizeProperty1(Region reg) {
		int w = reg.endColumn() - reg.column() + 1, h = reg.endLine() - reg.line() + 1;
		assertEquals(w * h, reg.posStream(page).spliterator().estimateSize(), "" + reg);
		Stream<Position> stream = reg.posStream(page);
		Position[] array = stream.toArray(i -> new Position[i]);
		System.out.println(reg + " → " + Arrays.toString(array));
		assertEquals(w * h, array.length, "" + reg);
	}

	public void flowStreamSizeProperty1(Region reg) {
		int size = 0;
		if (reg.line() == reg.endLine()) {
			size = reg.endColumn() - reg.column() + 1;
		} else {
			int firstLine = page.endColumn() - reg.column() + 1;
			int lastLine = reg.endColumn() - page.column() + 1;
			size = firstLine + lastLine;
			if (reg.line() + 1 < reg.endLine()) {
				int columns = (page.endColumn() - page.column() + 1);
				int fullLines = (reg.endLine() - reg.line() - 1);
				size += columns * fullLines;
			}
		}
		assertEquals(size, reg.posStream(page).spliterator().estimateSize(), "" + reg);
		Stream<Position> stream = reg.posStream(page).parallel();
		Position[] array = stream.toArray(i -> new Position[i]);
		assertEquals(size, array.length, "" + reg);
	}
}
