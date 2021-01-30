package turtleduck.display;

public interface DisplaySection {
	DisplaySection hide();

	DisplaySection show();

	DisplaySection show(boolean enabled);

	DisplaySection focus();

	DisplaySection maximized(boolean enabled);
}
