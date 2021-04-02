package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGAngle extends JSObject {
	// Angle Unit Types
	int SVG_ANGLETYPE_UNKNOWN = 0;
	int SVG_ANGLETYPE_UNSPECIFIED = 1;
	int SVG_ANGLETYPE_DEG = 2;
	int SVG_ANGLETYPE_RAD = 3;
	int SVG_ANGLETYPE_GRAD = 4;

	@JSProperty
	int getUnitType();

	@JSProperty
	double getValue();

	@JSProperty
	double getValueInSpecifiedUnits();

	@JSProperty
	String getValueAsString();

	@JSProperty
	void getValue(double value);

	@JSProperty
	void getValueInSpecifiedUnits(double value);

}
