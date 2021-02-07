package turtleduck.display;


public final class Display {
	protected static final int HEADER = 0, FOOTER = 1, EDITOR = 2, NAVIGATOR = 3, GRAPHICS = 4, TERMINAL = 5;
	protected static DisplaySection[] sections = new DisplaySection[5];
	protected static DisplayLayout defaultLayout = new DisplayLayout();
	protected static int focused = -1;
	protected static int maximized = -1;
	protected static int visible = 0;

	static {
		for (int i = 0; i < sections.length; i++) {
			sections[i] = new DisplaySectionImpl(i);
			show(i);
		}
	}

	public static DisplayLayout saveLayout() {
		DisplayLayout layout = new DisplayLayout();
		layout.focused = focused;
		layout.maximized = maximized;
		layout.visible = visible;
		return layout;
	}

	public static void restoreLayout(DisplayLayout layout) {
		focused = layout.focused;
		maximized = layout.maximized;
		visible = layout.visible;
		update();
	}

	protected static void show(int secNum) {
		visible |= (1 << secNum);
	}

	protected static void hide(int secNum) {
		visible &= ~(1 << secNum);
	}

	static protected void update() {

	}

	public static class DisplayLayout {
		int focused;
		int maximized;
		int visible;
	}

	static class DisplaySectionImpl implements DisplaySection {
		protected int secNum;

		public DisplaySectionImpl(int secNum) {
			this.secNum = secNum;
		}

		@Override
		public DisplaySection hide() {
			Display.hide(secNum);
			update();
			return this;
		}

		@Override
		public DisplaySection show() {
			Display.show(secNum);
			update();
			return this;
		}

		@Override
		public DisplaySection focus() {
			Display.focused = secNum;
			update();
			return this;
		}

		@Override
		public DisplaySection show(boolean enabled) {
			if (enabled)
				Display.show(secNum);
			else
				Display.hide(secNum);
			update();
			return this;
		}

		@Override
		public DisplaySection maximized(boolean enabled) {
			if (enabled)
				Display.maximized = secNum;
			else if (Display.maximized == secNum)
				Display.maximized = -1;
			update();

			return this;
		}

	}
}
