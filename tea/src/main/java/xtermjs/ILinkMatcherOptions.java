package xtermjs;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * An object containing options for a link matcher.
 */
public interface ILinkMatcherOptions extends JSObject {

	/**
	 * The index of the link from the regex.match(text) call. This defaults to 0
	 * (for regular expressions without capture groups).
	 */
	@JSProperty
	@Optional
	int getMatchIndex();

	/**
	 * The index of the link from the regex.match(text) call. This defaults to 0
	 * (for regular expressions without capture groups).
	 */
	@JSProperty
	@Optional
	void setMatchIndex(int val);

	/**
	 * A callback that validates whether to create an individual link, pass whether
	 * the link is valid to the callback.
	 */
	@JSProperty
	@Optional
	BiConsumer<String, Consumer<Boolean>> getValidationCallback();

	/**
	 * A callback that validates whether to create an individual link, pass whether
	 * the link is valid to the callback.
	 */
	@JSProperty
	@Optional
	void setValidationCallback(BiConsumer<String, Consumer<Boolean>> callback);

	/**
	 * A callback that fires when the mouse hovers over a link for a moment.
	 */
	@JSProperty
	@Optional
	ToolTipCallback getTooltipCallback();

	/**
	 * A callback that fires when the mouse hovers over a link for a moment.
	 */
	@JSProperty
	@Optional
	void setTooltipCallback(ToolTipCallback callback);

	/**
	 * A callback that fires when the mouse leaves a link. Note that this can happen
	 * even when tooltipCallback hasn't fired for the link yet.
	 */
	@JSProperty
	@Optional
	Runnable getLeaveCallback();

	/**
	 * A callback that fires when the mouse leaves a link. Note that this can happen
	 * even when tooltipCallback hasn't fired for the link yet.
	 */
	@JSProperty
	@Optional
	void setLeaveCallback(Runnable callback);

	/**
	 * The priority of the link matcher, this defines the order in which the link
	 * matcher is evaluated relative to others, from highest to lowest. The default
	 * value is 0.
	 */
	@JSProperty
	@Optional
	int getPriority();

	/**
	 * The priority of the link matcher, this defines the order in which the link
	 * matcher is evaluated relative to others, from highest to lowest. The default
	 * value is 0.
	 */
	@JSProperty
	@Optional
	void setPriority(int val);

	/**
	 * A callback that fires when the mousedown and click events occur that
	 * determines whether a link will be activated upon click. This enables only
	 * activating a link when a certain modifier is held down, if not the mouse
	 * event will continue propagation (eg. double click to select word).
	 */
	@JSProperty
	@Optional
	MouseCallback getWillLinkActivate();

	/**
	 * A callback that fires when the mousedown and click events occur that
	 * determines whether a link will be activated upon click. This enables only
	 * activating a link when a certain modifier is held down, if not the mouse
	 * event will continue propagation (eg. double click to select word).
	 */
	@JSProperty
	@Optional
	void setWillLinkActivate(MouseCallback callback);
}