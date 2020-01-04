package turtleduck;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import turtleduck.colors.Colors;

public class ColorTest {
	private static IntStream intValues() {
		return IntStream.range(0, 256);
	}
	private static DoubleStream doubleValues() {
		return intValues().mapToDouble((i) -> i/255.0);
	}

	@ParameterizedTest(name = "delinearize(linearize({0})) = {0}")
	@MethodSource("doubleValues")
	public void add(double c) {
		assertEquals(c, Colors.Gamma.gammaCompress(Colors.Gamma.gammaExpand(c)), 1e-9);
	}
	
	@ParameterizedTest(name = "delinearize(linearize({0})) = {0}")
	@MethodSource("intValues")
	public void add(int c) {
		assertEquals(c, Colors.Gamma.gammaCompress(Colors.Gamma.gammaExpand(c, 255), 4095));
	}
	
	@ParameterizedTest(name = "linearize_tbl({0}) = linearize({0})")
	@MethodSource("intValues")
	public void linTable(int c) {
		boolean using = Colors.Gamma.USE_TABLE;
		try {
			Colors.Gamma.USE_TABLE = true;
			int exT = Colors.Gamma.gammaExpand(c*2, 255);
			int gT = Colors.Gamma.gammaCompress(c*17, 4095);
			Colors.Gamma.USE_TABLE = false;
			int exC = Colors.Gamma.gammaExpand(c*2, 255);
			int gC = Colors.Gamma.gammaCompress(c*17, 4095);
			assertEquals(exC, exT);
			assertEquals(gC, gT);
		} finally {
			Colors.Gamma.USE_TABLE = using;
		}
	}
}
