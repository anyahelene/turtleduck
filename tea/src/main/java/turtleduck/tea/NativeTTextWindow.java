package turtleduck.tea;

import turtleduck.display.Layer;
import turtleduck.text.Attributes;
import turtleduck.text.CodePoint;
import turtleduck.text.TextCursor;
import turtleduck.text.TextMode;
import turtleduck.text.TextWindow;
import turtleduck.text.CodePoint.CodePoints;
import turtleduck.text.impl.CursorImpl;
import xtermjs.Terminal;

public class NativeTTextWindow implements TextWindow {
	private Terminal terminal;

	protected NativeTTextWindow(Terminal term) {
		terminal = term;
	}

	@Override
	public Layer clear() {
		terminal.clear();
		return this;
	}

	@Override
	public Layer layerToBack() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Layer layerToFront() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public double width() {
		return terminal.getElement().getClientWidth();
	}

	@Override
	public double height() {
		return terminal.getElement().getClientHeight();
	}

	@Override
	public Layer show() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Layer hide() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public String id() {
		return terminal.getElement().getAttribute("id");
	}

	@Override
	public TextWindow textMode(TextMode mode) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TextWindow textMode(TextMode mode, boolean adjustDisplayAspect) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TextMode textMode() {
		return null;
	}

	@Override
	public int pageWidth() {
		return terminal.getCols();
	}

	@Override
	public int pageHeight() {
		return terminal.getRows();
	}

	@Override
	public TextCursor cursor() {
		return new NativeTCursorImpl(this);
	}

	@Override
	public TextWindow flush() {
		return this;
	}

	@Override
	public TextWindow buffering(boolean enabled) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public boolean buffering() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TextWindow clearRegion(int x0, int y0, int x1, int y1, CodePoint cp, Attributes attrs) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public String charAt(int x, int y) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public CodePoint codePointAt(int x, int y) {
		// TODO Auto-generated method stub
		return CodePoints.REPLACEMENT_CHARACTER;
	}

	@Override
	public void redraw() {
		terminal.refresh(1, terminal.getRows());
	}

	@Override
	public TextWindow scroll(int lines) {
		terminal.scrollLines(lines);
		return this;
	}

	@Override
	public boolean autoScroll(boolean autoScroll) {
		return false;
	}

	@Override
	public TextWindow set(int x, int y, CodePoint cp, Attributes attrs) {
		terminal.write("\u001b\u005b" + y + ";" + x + "H" + cp.toString());
		return this;
	}

	@Override
	public Attributes attributesAt(int x, int y) {
		return null;
	}

	@Override
	public TextWindow cycleMode(boolean b) {
		// TODO Auto-generated method stub
		return this;
	}

}
