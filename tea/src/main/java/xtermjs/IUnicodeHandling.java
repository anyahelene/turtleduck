package xtermjs;

import java.util.List;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;


/**
 * (EXPERIMENTAL) Unicode handling interface.
 */
public interface IUnicodeHandling extends JSObject {

	/**
	 * Register a custom Unicode version provider.
	 */
	void register(IUnicodeVersionProvider provider);

	/**
	 * Registered Unicode versions.
	 */
	@JSProperty
	List<String> getVersions();

	/**
	 * Getter/setter for active Unicode version.
	 */
	@JSProperty
	String getActiveVersion();

	/**
	 * Getter/setter for active Unicode version.
	 */
	@JSProperty
	void setActiveVersion(String val);
}