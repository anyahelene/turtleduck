package turtleduck.jfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.text.Printer;
import turtleduck.text.PrinterImpl;
import turtleduck.text.TextFont;
import turtleduck.text.TextMode;

public class JfxPrinter extends PrinterImpl<JfxScreen> implements Printer {
	TextFont FONT_MONOSPACED = new JfxTextFont("Monospaced", 27.00, TextMode.CHAR_BOX_SIZE, 3.4000, -6.7000, 1.5000,
			1.0000, true);
	TextFont FONT_LMMONO = new JfxTextFont("lmmono10-regular.otf", 30.00, TextMode.CHAR_BOX_SIZE, 4.0000, -8.5000,
			1.5000, 1.0000, true);
	TextFont FONT_ZXSPECTRUM7 = new JfxTextFont("ZXSpectrum-7.otf", 22.00, TextMode.CHAR_BOX_SIZE, 3.1000, -3.8000,
			1.0000, 1.0000, true);
	/**
	 * TTF file can be found here: http://users.teilar.gr/~g1951d/ in this ZIP file:
	 * http://users.teilar.gr/~g1951d/Symbola.zip
	 * <p>
	 * (Put the extracted Symbola.ttf in src/inf101/v18/gfx/fonts/)
	 */
	TextFont FONT_SYMBOLA = new JfxTextFont("Symbola.ttf", 26.70, TextMode.CHAR_BOX_SIZE, -0.4000, -7.6000, 1.35000,
			1.0000, true);
	/**
	 * TTF file can be found here:
	 * http://www.kreativekorp.com/software/fonts/c64.shtml
	 */
	TextFont FONT_GIANA = new JfxTextFont("Giana.ttf", 25.00, TextMode.CHAR_BOX_SIZE, 4.6000, -5.0000, 1.0000, 1.0000,
			true);
	/**
	 * TTF file can be found here:
	 * http://www.kreativekorp.com/software/fonts/c64.shtml
	 */
	TextFont FONT_C64 = new JfxTextFont("PetMe64.ttf", 31.50, TextMode.CHAR_BOX_SIZE, 0.0000, -4.000, 1.0000, 1.0000,
			true);

	Canvas canvas;

	public JfxPrinter(String layerId, JfxScreen jfxScreen, Canvas jfxCanvas) {
		super(layerId, jfxScreen, new JfxCanvas(layerId + ".canvas", jfxCanvas));
		this.canvas = jfxCanvas;
		font = FONT_LMMONO;
	}

	protected void redrawTextPage(int x0, int y0, int x1, int y1) {
		/*
		 * System.out.printf("redrawTextPage benchmark");
		 * System.out.printf("  %5s %5s %7s %4s %5s %5s %5s%n", "ms", "chars",
		 * "ms/char", "mode", "indir", "inv", "fake"); for (int m = -1; m < 8; m++) {
		 * long t0 = System.currentTimeMillis(); int n = 0;
		 */
		if (textPage == null)
			return;
		GraphicsContext context = canvas.getGraphicsContext2D();
		try {
			context.save();
			double px0 = (x0 - 1) * getCharWidth(), py0 = (y0 - 1) * getCharHeight();
			double px1 = x1 * getCharWidth(), py1 = y1 * getCharHeight();
			if (DEBUG_REDRAW)
				System.out.printf("redrawTextPage(): Area to clear: (%2f,%2f)–(%2f,%2f)%n", px0, py0, px1, py1);
			if (background != null && background != Colors.TRANSPARENT) {
				context.setFill(JfxColor.toJfxPaint(background));
				context.fillRect(px0, py0, px1 - px0, py1 - py0);
			} else {
				context.clearRect(px0, py0, px1 - px0, py1 - py0);
			}
			for (int tmpY = y0; tmpY <= y1; tmpY++) {
				Char[] line = lineBuffer.get(tmpY - 1);
				for (int tmpX = x0; tmpX <= x1; tmpX++) {
					Char c = line[tmpX - 1];
					if (c != null) {
						context.save();
						try {
							context.setFill(JfxColor.toJfxPaint(c.fill));
							context.setStroke(JfxColor.toJfxPaint(c.stroke));
							Paint bg = c.bg == background ? null : c.bg;
							font.drawTextNoClearAt(textPage, (tmpX - 1) * getCharWidth(), tmpY * getCharHeight(), c.s,
									textMode.getCharWidth() / textMode.getCharBoxSize(), c.mode/* m */, bg);
						} finally {
							context.restore();
						}
						// n++;

					}
				}
			}
		} finally {
			context.restore();

		}
		/*
		 * long t = System.currentTimeMillis() - t0; if (m >= 0)
		 * System.out.printf("  %5d %5d %7.4f %4d %5b %5b %5b%n", t, n, ((double) t) /
		 * n, m, (m & 3) != 0, (m & 1) != 0, (m & 4) != 0); } System.out.println();
		 */
	}

	protected void clearCanvas() {

	}

	@Override
	public turtleduck.turtle.Canvas canvas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDrawChar(int x, int y, Char c) {
		GraphicsContext context = canvas.getGraphicsContext2D();

		context.save();
		try {
			context.setFill(JfxColor.toJfxPaint(c.fill));
			context.setStroke(JfxColor.toJfxPaint(c.stroke));
			font.drawTextAt(textPage, (x - 1) * getCharWidth(), y * getCharHeight(), c.s,
					textMode.getCharWidth() / textMode.getCharBoxSize(), c.mode, c.bg);
		} finally {
			context.restore();
		}
	}

	public void drawCharCells() {
		if (screen != null) {
			GraphicsContext context = ((JfxScreen)screen).background.getGraphicsContext2D();
			screen.clearBackground();
			double w = getCharWidth();
			double h = getCharHeight();
			context.save();
			context.setGlobalBlendMode(BlendMode.EXCLUSION);
			context.setFill(Color.WHITE.deriveColor(0.0, 1.0, 1.0, 0.1));
			for (int x = 0; x < getLineWidth(); x++) {
				for (int y = 0; y < getPageHeight(); y++) {
					if ((x + y) % 2 == 0)
						context.fillRect(x * w, y * h, w, h);
				}
			}
			context.restore();
		}
	}

}
