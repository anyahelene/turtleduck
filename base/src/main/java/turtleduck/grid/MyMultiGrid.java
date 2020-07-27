package turtleduck.grid;

import java.util.ArrayList;
import java.util.List;

public class MyMultiGrid<T> extends MyGrid<List<T>> implements MultiGrid<T> {

	public MyMultiGrid(Area area) {
		super(area, (l) -> new ArrayList<T>());
	}

	public MyMultiGrid(int width, int height) {
		super(width, height, (l) -> new ArrayList<T>());
	}

}
