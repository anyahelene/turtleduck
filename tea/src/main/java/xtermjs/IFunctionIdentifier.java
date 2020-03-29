package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Data type to register a CSI, DCS or ESC callback in the parser
 * in the form:
 *    ESC I..I F
 *    CSI Prefix P..P I..I F
 *    DCS Prefix P..P I..I F data_bytes ST
 *
 * with these rules/restrictions:
 * - prefix can only be used with CSI and DCS
 * - only one leading prefix byte is recognized by the parser
 *   before any other parameter bytes (P..P)
 * - intermediate bytes are recognized up to 2
 *
 * For custom sequences make sure to read ECMA-48 and the resources at
 * vt100.net to not clash with existing sequences or reserved address space.
 * General recommendations:
 * - use private address space (see ECMA-48)
 * - use max one intermediate byte (technically not limited by the spec,
 *   in practice there are no sequences with more than one intermediate byte,
 *   thus parsers might get confused with more intermediates)
 * - test against other common emulators to check whether they escape/ignore
 *   the sequence correctly
 *
 * Notes: OSC command registration is handled differently (see addOscHandler)
 *        APC, PM or SOS is currently not supported.
 */
public interface IFunctionIdentifier extends JSObject {

	/**
	 * Optional prefix byte, must be in range \x3c .. \x3f. Usable in CSI and DCS.
	 */
	@JSProperty
	@Optional
	String getPrefix();

	/**
	 * Optional prefix byte, must be in range \x3c .. \x3f. Usable in CSI and DCS.
	 */
	@JSProperty
	@Optional
	void setPrefix(String val);

	/**
	 * Optional intermediate bytes, must be in range \x20 .. \x2f. Usable in CSI,
	 * DCS and ESC.
	 */
	@JSProperty
	@Optional
	String getIntermediates();

	/**
	 * Optional intermediate bytes, must be in range \x20 .. \x2f. Usable in CSI,
	 * DCS and ESC.
	 */
	@JSProperty
	@Optional
	void setIntermediates(String val);

	/**
	 * Final byte, must be in range \x40 .. \x7e for CSI and DCS, \x30 .. \x7e for
	 * ESC.
	 */
	@JSProperty
	String getFinal();

	/**
	 * Final byte, must be in range \x40 .. \x7e for CSI and DCS, \x30 .. \x7e for
	 * ESC.
	 */
	@JSProperty
	void setFinal(String val);
}