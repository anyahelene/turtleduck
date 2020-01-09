package turtleduck.text;

import java.util.function.BiFunction;

import turtleduck.colors.Paint;
import turtleduck.display.Layer;

public interface Printer extends Layer {


	void addToCharBuffer(String string);

	void beginningOfLine();

	void beginningOfPage();

	void clear();

	void clearAt(int x, int y);

	void clearLine(int y);

	void clearRegion(int x, int y, int width, int height);

	int constrainY(int y);

	int constrainYOrScroll(int y);

	void cycleMode(boolean adjustDisplayAspect);

	void drawCharCells();

	Paint getBackground(int x, int y);

	boolean getBold();

	String getChar(int x, int y);

	double getCharHeight();

	double getCharWidth();

	Paint getColor(int x, int y);

	TextFont getFont();

	boolean getItalics();

	/**
	 * @return the leftMargin
	 */
	int getLeftMargin();

	int getLineWidth();

	int getPageHeight();

	boolean getReverseVideo();

	TextMode getTextMode();

	/**
	 * @return the topMargin
	 */
	int getTopMargin();

	int getVideoMode();

	int getX();

	int getY();

	boolean isFilled(int x, int y);

	void layerToBack();

	void layerToFront();

	void move(int deltaX, int deltaY);

	void moveHoriz(int dist);

	void moveTo(int newX, int newY);

	void moveVert(int dist);

	void plot(int x, int y);

	void plot(int x, int y, BiFunction<Integer, Integer, Integer> op);

	void print(String s);

	void print(String s, Paint paint);

	void printAt(int atX, int atY, String s);

	void printAt(int atX, int atY, String s, Paint ink);

	void println();

	void println(String s);

	void redrawTextPage();

	void resetAttrs();

	void resetFull();

	void restoreCursor();

	void saveCursor();

	void scrollDown();

	void scrollUp();

	boolean setAutoScroll(boolean autoScroll);

	void setBackground(int x, int y, Paint bg);

	void setBackground(Paint bgColor);

	void setBold(boolean enabled);

	void setColor(int x, int y, Paint fill);

	void setFill(Paint fill);

	void setFont(TextFont font);

	void setInk(Paint ink);

	void setItalics(boolean enabled);

	/**
	 */
	void setLeftMargin();

	/**
	 * @param leftMargin
	 *            the leftMargin to set
	 */
	void setLeftMargin(int leftMargin);

	void setReverseVideo(boolean enabled);

	void setStroke(Paint stroke);

	void setTextMode(TextMode mode);

	void setTextMode(TextMode mode, boolean adjustDisplayAspect);

	void setTopMargin();

	/**
	 * @param topMargin
	 *            the topMargin to set
	 */
	void setTopMargin(int topMargin);

	void setVideoAttrDisabled(int attr);

	void setVideoAttrEnabled(int attr);

	void setVideoAttrs(int attr);

	void unplot(int x, int y);

	/**
	 * Redraw the part of the page that has changed since last redraw.
	 */
	void redrawDirty();

	/**
	 * With buffered printing, nothing is actually drawn until
	 * {@link #redrawDirty()} or {@link #redrawTextPage()} is called.
	 * 
	 * @param buffering
	 *            Whether to use buffering
	 */
	void setBuffering(boolean buffering);

	/**
	 * @return True if buffering is enabled
	 * @see #setBuffering(boolean)
	 */
	boolean getBuffering();

	void scroll(int i);

	boolean hasInput();

	void sendInput(String string);

	void write(CodePoint codePoint);

}