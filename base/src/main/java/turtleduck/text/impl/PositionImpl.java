package turtleduck.text.impl;

import turtleduck.text.Position;

public class PositionImpl implements Position {
	protected final int column, line;
	
	public PositionImpl(int column, int line) {
		super();
		this.column = column;
		this.line = line;
	}

	@Override
	public int column() {
		return column;
	}

	@Override
	public int line() {
		return line;
	}

	@Override
	public String toString() {
		return String.format("(%d,%d)", column, line);
	}

}
