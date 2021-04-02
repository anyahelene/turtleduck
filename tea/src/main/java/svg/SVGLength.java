package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGLength extends JSObject {
	// Length Unit Types
	int SVG_LENGTHTYPE_UNKNOWN = 0;
	int SVG_LENGTHTYPE_NUMBER = 1;
	int SVG_LENGTHTYPE_PERCENTAGE = 2;
	int SVG_LENGTHTYPE_EMS = 3;
	int SVG_LENGTHTYPE_EXS = 4;
	int SVG_LENGTHTYPE_PX = 5;
	int SVG_LENGTHTYPE_CM = 6;
	int SVG_LENGTHTYPE_MM = 7;
	int SVG_LENGTHTYPE_IN = 8;
	int SVG_LENGTHTYPE_PT = 9;
	int SVG_LENGTHTYPE_PC = 10;

	@JSProperty
	int getUnitType();

	@JSProperty
	double getValue();

	@JSProperty
	void setValue(double value);

	@JSProperty
	double getValueInSpecifiedUnits();

	@JSProperty
	void setValueInSpecifiedUnits(double value);

	@JSProperty
	String getValueAsString();

	void newValueSpecifiedUnits(int unitType, double valueInSpecifiedUnits);

	void convertToSpecifiedUnits(int unitType);
}
