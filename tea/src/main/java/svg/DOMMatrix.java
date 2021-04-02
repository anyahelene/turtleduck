package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface DOMMatrix extends JSObject {
	@JSProperty
	void setA(double a);

	@JSProperty
	void setB(double b);

	@JSProperty
	void setC(double c);

	@JSProperty
	void setD(double d);

	@JSProperty
	void setE(double e);

	@JSProperty
	void setF(double f);

	@JSProperty
	double getA();

	@JSProperty
	double getB();

	@JSProperty
	double getC();

	@JSProperty
	double getD();

	@JSProperty
	double getE();

	@JSProperty
	double getF();

}
