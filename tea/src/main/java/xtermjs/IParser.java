package xtermjs;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.teavm.jso.JSObject;

/**
 * Allows hooking into the parser for custom handling of escape sequences.
 */
public interface IParser extends JSObject {

	/**
	 * Adds a handler for CSI escape sequences.
	 * 
	 * @param id       Specifies the function identifier under which the callback
	 *                 gets registered, e.g. {final: 'm'} for SGR.
	 * @param callback The function to handle the sequence. The callback is called
	 *                 with the numerical params. If the sequence has subparams the
	 *                 array will contain subarrays with their numercial values.
	 *                 Return true if the sequence was handled; false if we should
	 *                 try a previous handler (set by addCsiHandler or
	 *                 setCsiHandler). The most recently added handler is tried
	 *                 first.
	 * @return An IDisposable you can call to remove this handler.
	 */

	IDisposable registerCsiHandler(IFunctionIdentifier id, Predicate<List<Object>> callback);

	/**
	 * Adds a handler for DCS escape sequences.
	 * 
	 * @param id       Specifies the function identifier under which the callback
	 *                 gets registered, e.g. {intermediates: '$' final: 'q'} for
	 *                 DECRQSS.
	 * @param callback The function to handle the sequence. Note that the function
	 *                 will only be called once if the sequence finished
	 *                 sucessfully. There is currently no way to intercept smaller
	 *                 data chunks, data chunks will be stored up until the sequence
	 *                 is finished. Since DCS sequences are not limited by the
	 *                 amount of data this might impose a problem for big payloads.
	 *                 Currently xterm.js limits DCS payload to 10 MB which should
	 *                 give enough room for most use cases. The function gets the
	 *                 payload and numerical parameters as arguments. Return true if
	 *                 the sequence was handled; false if we should try a previous
	 *                 handler (set by addDcsHandler or setDcsHandler). The most
	 *                 recently added handler is tried first.
	 * @return An IDisposable you can call to remove this handler.
	 */
	IDisposable registerDcsHandler(IFunctionIdentifier id, BiPredicate<String, List<Object>> callback);

	/**
	 * Adds a handler for ESC escape sequences.
	 * 
	 * @param id       Specifies the function identifier under which the callback
	 *                 gets registered, e.g. {intermediates: '%' final: 'G'} for
	 *                 default charset selection.
	 * @param callback The function to handle the sequence. Return true if the
	 *                 sequence was handled; false if we should try a previous
	 *                 handler (set by addEscHandler or setEscHandler). The most
	 *                 recently added handler is tried first.
	 * @return An IDisposable you can call to remove this handler.
	 */
	IDisposable registerEscHandler(IFunctionIdentifier id, Supplier<Boolean> handler);

	/**
	 * Adds a handler for OSC escape sequences.
	 * 
	 * @param ident    The number (first parameter) of the sequence.
	 * @param callback The function to handle the sequence. Note that the function
	 *                 will only be called once if the sequence finished
	 *                 sucessfully. There is currently no way to intercept smaller
	 *                 data chunks, data chunks will be stored up until the sequence
	 *                 is finished. Since OSC sequences are not limited by the
	 *                 amount of data this might impose a problem for big payloads.
	 *                 Currently xterm.js limits OSC payload to 10 MB which should
	 *                 give enough room for most use cases. The callback is called
	 *                 with OSC data string. Return true if the sequence was
	 *                 handled; false if we should try a previous handler (set by
	 *                 addOscHandler or setOscHandler). The most recently added
	 *                 handler is tried first.
	 * @return An IDisposable you can call to remove this handler.
	 */
	IDisposable registerOscHandler(int ident, Predicate<String> callback);

}