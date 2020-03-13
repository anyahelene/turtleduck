package turtleduck.jfx;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.drawing.Drawing;
import turtleduck.text.TextMode;
import turtleduck.text.TextWindow;
import turtleduck.text.impl.WindowImpl;
import turtleduck.text.Attribute;
import turtleduck.text.Attributes;
import turtleduck.text.Region;
import turtleduck.text.TextFont;
import turtleduck.turtle.Path;

public class JfxTextWindow extends WindowImpl<JfxScreen> {
	private final javafx.scene.canvas.Canvas fxCanvas;
	protected final GraphicsContext context;

	public JfxTextWindow(String id, TextMode mode, JfxScreen screen, double width, double height,
			javafx.scene.canvas.Canvas canvas) {
		super(id, mode, screen, width, height);
		fxCanvas = canvas;
		context = canvas.getGraphicsContext2D();
	}

	@Override
	protected void redraw(int x0, int y0, int x1, int y1, Attributes attrs) {
		Region region = Region.flow(x0, y0, x1, y1);
		System.out.println("redraw(): " + region);
		GraphicsContext context = fxCanvas.getGraphicsContext2D();
		cells.forEachElement(region, elt -> {
			double px0 = (elt.x() - 1) * getCharWidth(), py0 = (elt.y() - 1) * getCharHeight();
			double px1 = (elt.x() + elt.width() - 1) * getCharWidth(), py1 = (elt.y()) * getCharHeight();
			Paint regionBg = elt.attributes().get(Attribute.ATTR_BACKGROUND);
			String s = elt.toString();
			System.out.printf("%d,%d+%d+%d [%s]\n", elt.x(), elt.y(), elt.width(), elt.height(), elt.toString());
			if (true)
				System.out.printf("redraw(): Area to clear: (%2f,%2f)–(%2f,%2f), %s%n", px0, py0, px1, py1,
						elt.attributes());
			if (regionBg != null && regionBg != Colors.TRANSPARENT) {
				context.setFill(JfxColor.toJfxPaint(regionBg));
				context.fillRect(px0, py0, px1 - px0, py1 - py0);
			} else {
				context.clearRect(px0, py0, px1 - px0, py1 - py0);
			}
			TextFont font = elt.attributes().get(Attribute.ATTR_FONT);
			if (font == null)
				font = JfxTextFont.FONT_ZXSPECTRUM7;
			if (!s.equals("")) {
				context.setFill(JfxColor.toJfxPaint(elt.attributes().get(Attribute.ATTR_FOREGROUND)));
				font.drawTextAt(this, (elt.x() - 1) * getCharWidth(), elt.y() * getCharHeight(), s,
						textMode.getCharWidth() / textMode.getCharBoxSize(), 0, null);
			}
		});
	}

	protected void redraw2(int x0, int y0, int x1, int y1, Attributes attrs) {
		x0 = Math.max(1, Math.min(pageWidth(), x0));
		y0 = Math.max(1, Math.min(pageWidth(), y0));
		x1 = Math.max(1, Math.min(pageWidth(), x1));
		y1 = Math.max(1, Math.min(pageWidth(), y1));
//		System.out.printf("redrawTextPage benchmark");
//		System.out.printf("  %5s %5s %7s %4s %5s %5s %5s%n", "ms", "chars", "ms/char", "mode", "indir", "inv", "fake");
//		for (int m = -1; m < 8; m++) {
//			long t0 = System.currentTimeMillis();
//			int n = 0;

		if (fxCanvas == null)
			return;
		GraphicsContext context = fxCanvas.getGraphicsContext2D();
		try {
			context.save();
			for (int tmpY = y0; tmpY <= y1; tmpY++) {
				Line line = cells.line(tmpY);
				double py0 = (tmpY - 1) * getCharHeight();
				double py1 = tmpY * getCharHeight();
				Paint regionBg = null;
				double px0 = (x0 - 1) * getCharWidth();
				double px1 = x0 * getCharWidth();
				String s = "";
				for (int tmpX = x0; tmpX <= x1 + 1; tmpX++) {
					Paint cellBg = null;
					if (tmpX <= x1) {
						Cell c = line.col(tmpX);
						s += c.toString();
						cellBg = c.background();
						if (tmpX % 2 == 1)
							cellBg = Colors.CYAN;
						if (regionBg == null) {
							regionBg = cellBg;
						}
					}
					if (regionBg != cellBg) {
						System.out.println("[" + s + "]");
						if (true)
							System.out.printf("redraw(): Area to clear: (%2f,%2f)–(%2f,%2f), %s%n", px0, py0, px1, py1,
									regionBg);
						if (regionBg != null && regionBg != Colors.TRANSPARENT) {
							context.setFill(JfxColor.toJfxPaint(regionBg));
							context.fillRect(px0, py0, px1 - px0, py1 - py0);
						} else {
							context.clearRect(px0, py0, px1 - px0, py1 - py0);
						}
						regionBg = cellBg;
						px0 = (tmpX - 1) * getCharWidth();
					}
					px1 = tmpX * getCharWidth();

				}
				for (int tmpX = x0; tmpX <= x1; tmpX++) {
					Cell c = line.col(tmpX);
					if (c != null && c.notNull()) {
						doDrawChar(tmpX, tmpY, c);
					}
				}

			}
		} catch (

		RuntimeException e) {
			e.printStackTrace();
			throw e;
		} finally {
			context.restore();

		}

		if (false) {
			try (PrintStream stream = new PrintStream(new FileOutputStream("/tmp/page.html"))) {
				stream.println(cells.toHtml());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

//			long t = System.currentTimeMillis() - t0;
//			if (m >= 0)
//				System.out.printf("  %5d %5d %7.4f %4d %5b %5b %5b%n", t, n, ((double) t) / n, m, (m & 3) != 0,
//						(m & 1) != 0, (m & 4) != 0);
//		}
//		System.out.println();

	}

	protected void doDrawChar(int x, int y, Cell c) {
		GraphicsContext context = fxCanvas.getGraphicsContext2D();

		context.save();
		System.out.print(c);
		try {
			context.setFill(JfxColor.toJfxPaint(c.foreground()));
//			context.setStroke(null);
			TextFont font = c.font();
			if (font == null)
				font = JfxTextFont.FONT_ZXSPECTRUM7;
			Font f = font.getFont(Font.class);

			font.drawTextAt(this, (x - 1) * getCharWidth(), y * getCharHeight(), c.codePoint().stringValue(),
					textMode.getCharWidth() / textMode.getCharBoxSize(), 0, null);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} finally {
			context.restore();
		}
	}

	public void drawCharCells() {
		if (screen != null) {
			GraphicsContext context = ((JfxScreen) screen).background.getGraphicsContext2D();
			screen.clearBackground();
			double w = getCharWidth();
			double h = getCharHeight();
			context.save();
//			context.setFill(Color.BLUE);
//			context.fillRect(0, 0, width(), height());
			context.setGlobalBlendMode(BlendMode.EXCLUSION);
			context.setFill(Color.WHITE.deriveColor(0.0, 1.0, 1.0, 0.1));
			for (int x = 0; x < pageWidth(); x++) {
				for (int y = 0; y < pageHeight(); y++) {
					if ((x + y) % 2 == 0)
						context.fillRect(x * w, y * h, w, h);
				}
			}
			context.restore();
		}
	}

	private double getCharHeight() {
		return textMode.getCharHeight();
	}

	private double getCharWidth() {
		return textMode.getCharWidth();
	}

	@Override
	public TextWindow clear() {
		super.clear();
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
		return this;
	}

	

	@Override
	public TextWindow show() {
		fxCanvas.setVisible(false);
		return this;
	}

	@Override
	public TextWindow hide() {
		fxCanvas.setVisible(true);
		return this;
	}

	@Override
	public TextWindow flush() {
		super.flush();
		return this;
	}

}
