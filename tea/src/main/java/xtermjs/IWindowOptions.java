package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Enable various window manipulation and report features (CSI Ps ; Ps ; Ps t).
 *
 * Most settings have no default implementation, as they heavily rely on
 * the embedding environment.
 *
 * To implement a feature, create a custom CSI hook like this:
 * ```ts
 * term.parser.addCsiHandler({final: 't'}, params => {
 *   const ps = params[0];
 *   switch (ps) {
 *     case XY:
 *       ...            // your implementation for option XY
 *       return true;   // signal Ps=XY was handled
 *   }
 *   return false;      // any Ps that was not handled
 * });
 * ```
 *
 * Note on security:
 * Most features are meant to deal with some information of the host machine
 * where the terminal runs on. This is seen as a security risk possibly leaking
 * sensitive data of the host to the program in the terminal. Therefore all options
 * (even those without a default implementation) are guarded by the boolean flag
 * and disabled by default.
 */
public interface IWindowOptions extends JSObject {

	/**
	 * Ps=1 De-iconify window. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getRestoreWin();

	/**
	 * Ps=1 De-iconify window. No default implementation.
	 */
	@JSProperty
	@Optional
	void setRestoreWin(boolean val);

	/**
	 * Ps=2 Iconify window. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getMinimizeWin();

	/**
	 * Ps=2 Iconify window. No default implementation.
	 */
	@JSProperty
	@Optional
	void setMinimizeWin(boolean val);

	/**
	 * Ps=3 ; x ; y Move window to [x, y]. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getSetWinPosition();

	/**
	 * Ps=3 ; x ; y Move window to [x, y]. No default implementation.
	 */
	@JSProperty
	@Optional
	void setSetWinPosition(boolean val);

	/**
	 * Ps = 4 ; height ; width Resize the window to given `height` and `width` in
	 * pixels. Omitted parameters should reuse the current height or width. Zero
	 * parameters should use the display's height or width. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	boolean getSetWinSizePixels();

	/**
	 * Ps = 4 ; height ; width Resize the window to given `height` and `width` in
	 * pixels. Omitted parameters should reuse the current height or width. Zero
	 * parameters should use the display's height or width. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	void setSetWinSizePixels(boolean val);

	/**
	 * Ps=5 Raise the window to the front of the stacking order. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	boolean getRaiseWin();

	/**
	 * Ps=5 Raise the window to the front of the stacking order. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	void setRaiseWin(boolean val);

	/**
	 * Ps=6 Lower the xterm window to the bottom of the stacking order. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	boolean getLowerWin();

	/**
	 * Ps=6 Lower the xterm window to the bottom of the stacking order. No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	void setLowerWin(boolean val);

	/** Ps=7 Refresh the window. */
	@JSProperty
	@Optional
	boolean getRefreshWin();

	/** Ps=7 Refresh the window. */
	@JSProperty
	@Optional
	void setRefreshWin(boolean val);

	/**
	 * Ps = 8 ; height ; width Resize the text area to given height and width in
	 * characters. Omitted parameters should reuse the current height or width. Zero
	 * parameters use the display's height or width. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getSetWinSizeChars();

	/**
	 * Ps = 8 ; height ; width Resize the text area to given height and width in
	 * characters. Omitted parameters should reuse the current height or width. Zero
	 * parameters use the display's height or width. No default implementation.
	 */
	@JSProperty
	@Optional
	void setSetWinSizeChars(boolean val);

	/**
	 * Ps=9 ; 0 Restore maximized window. Ps=9 ; 1 Maximize window (i.e., resize to
	 * screen size). Ps=9 ; 2 Maximize window vertically. Ps=9 ; 3 Maximize window
	 * horizontally. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getMaximizeWin();

	/**
	 * Ps=9 ; 0 Restore maximized window. Ps=9 ; 1 Maximize window (i.e., resize to
	 * screen size). Ps=9 ; 2 Maximize window vertically. Ps=9 ; 3 Maximize window
	 * horizontally. No default implementation.
	 */
	@JSProperty
	@Optional
	void setMaximizeWin(boolean val);

	/**
	 * Ps=10 ; 0 Undo full-screen mode. Ps=10 ; 1 Change to full-screen. Ps=10 ; 2
	 * Toggle full-screen. No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getFullscreenWin();

	/**
	 * Ps=10 ; 0 Undo full-screen mode. Ps=10 ; 1 Change to full-screen. Ps=10 ; 2
	 * Toggle full-screen. No default implementation.
	 */
	@JSProperty
	@Optional
	void setFullscreenWin(boolean val);

	/**
	 * Ps=11 Report xterm window state. If the xterm window is non-iconified, it
	 * returns "CSI 1 t". If the xterm window is iconified, it returns "CSI 2 t". No
	 * default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetWinState();

	/**
	 * Ps=11 Report xterm window state. If the xterm window is non-iconified, it
	 * returns "CSI 1 t". If the xterm window is iconified, it returns "CSI 2 t". No
	 * default implementation.
	 */
	@JSProperty
	@Optional
	void setGetWinState(boolean val);

	/**
	 * Ps=13 Report xterm window position. Result is "CSI 3 ; x ; y t". Ps=13 ; 2
	 * Report xterm text-area position. Result is "CSI 3 ; x ; y t". No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetWinPosition();

	/**
	 * Ps=13 Report xterm window position. Result is "CSI 3 ; x ; y t". Ps=13 ; 2
	 * Report xterm text-area position. Result is "CSI 3 ; x ; y t". No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	void setGetWinPosition(boolean val);

	/**
	 * Ps=14 Report xterm text area size in pixels. Result is "CSI 4 ; height ;
	 * width t". Ps=14 ; 2 Report xterm window size in pixels. Result is "CSI 4 ;
	 * height ; width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetWinSizePixels();

	/**
	 * Ps=14 Report xterm text area size in pixels. Result is "CSI 4 ; height ;
	 * width t". Ps=14 ; 2 Report xterm window size in pixels. Result is "CSI 4 ;
	 * height ; width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	void setGetWinSizePixels(boolean val);

	/**
	 * Ps=15 Report size of the screen in pixels. Result is "CSI 5 ; height ; width
	 * t". No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetScreenSizePixels();

	/**
	 * Ps=15 Report size of the screen in pixels. Result is "CSI 5 ; height ; width
	 * t". No default implementation.
	 */
	@JSProperty
	@Optional
	void setGetScreenSizePixels(boolean val);

	/**
	 * Ps=16 Report xterm character cell size in pixels. Result is "CSI 6 ; height ;
	 * width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetCellSizePixels();

	/**
	 * Ps=16 Report xterm character cell size in pixels. Result is "CSI 6 ; height ;
	 * width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	void setGetCellSizePixels(boolean val);

	/**
	 * Ps=18 Report the size of the text area in characters. Result is "CSI 8 ;
	 * height ; width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetWinSizeChars();

	/**
	 * Ps=18 Report the size of the text area in characters. Result is "CSI 8 ;
	 * height ; width t". Has a default implementation.
	 */
	@JSProperty
	@Optional
	void setGetWinSizeChars(boolean val);

	/**
	 * Ps=19 Report the size of the screen in characters. Result is "CSI 9 ; height
	 * ; width t". No default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetScreenSizeChars();

	/**
	 * Ps=19 Report the size of the screen in characters. Result is "CSI 9 ; height
	 * ; width t". No default implementation.
	 */
	@JSProperty
	@Optional
	void setGetScreenSizeChars(boolean val);

	/**
	 * Ps=20 Report xterm window's icon label. Result is "OSC L label ST". No
	 * default implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetIconTitle();

	/**
	 * Ps=20 Report xterm window's icon label. Result is "OSC L label ST". No
	 * default implementation.
	 */
	@JSProperty
	@Optional
	void setGetIconTitle(boolean val);

	/**
	 * Ps=21 Report xterm window's title. Result is "OSC l label ST". No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	boolean getGetWinTitle();

	/**
	 * Ps=21 Report xterm window's title. Result is "OSC l label ST". No default
	 * implementation.
	 */
	@JSProperty
	@Optional
	void setGetWinTitle(boolean val);

	/**
	 * Ps=22 ; 0 Save xterm icon and window title on stack. Ps=22 ; 1 Save xterm
	 * icon title on stack. Ps=22 ; 2 Save xterm window title on stack. All variants
	 * have a default implementation.
	 */
	@JSProperty
	@Optional
	boolean getPushTitle();

	/**
	 * Ps=22 ; 0 Save xterm icon and window title on stack. Ps=22 ; 1 Save xterm
	 * icon title on stack. Ps=22 ; 2 Save xterm window title on stack. All variants
	 * have a default implementation.
	 */
	@JSProperty
	@Optional
	void setPushTitle(boolean val);

	/**
	 * Ps=23 ; 0 Restore xterm icon and window title from stack. Ps=23 ; 1 Restore
	 * xterm icon title from stack. Ps=23 ; 2 Restore xterm window title from stack.
	 * All variants have a default implementation.
	 */
	@JSProperty
	@Optional
	boolean getPopTitle();

	/**
	 * Ps=23 ; 0 Restore xterm icon and window title from stack. Ps=23 ; 1 Restore
	 * xterm icon title from stack. Ps=23 ; 2 Restore xterm window title from stack.
	 * All variants have a default implementation.
	 */
	@JSProperty
	@Optional
	void setPopTitle(boolean val);

	/**
	 * Ps>=24 Resize to Ps lines (DECSLPP). DECSLPP is not implemented. This
	 * settings is also used to enable / disable DECCOLM (earlier variant of
	 * DECSLPP).
	 */
	@JSProperty
	@Optional
	boolean getSetWinLines();

	/**
	 * Ps>=24 Resize to Ps lines (DECSLPP). DECSLPP is not implemented. This
	 * settings is also used to enable / disable DECCOLM (earlier variant of
	 * DECSLPP).
	 */
	@JSProperty
	@Optional
	void setSetWinLines(boolean val);
}