package turtleduck.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;

import java.util.Random;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import turtleduck.AbstractEqualsTest;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.display.impl.ViewportBuilderImpl;
import turtleduck.geometry.impl.AngleImpl;
import turtleduck.testutil.generators.DirectionGenerator;
import turtleduck.util.Logging;

public class ViewportTest {
	ViewportBuilder vpb;

	@BeforeEach
	public void setup() {
		vpb = new ViewportBuilderImpl(null);
		vpb.screenArea(0, 0, 1920, 1080);
	}

	@Test
	public void viewportTestFit1() {
		vpb.aspectWide().width(1280).fit();
		Viewport vp = vpb.done();
		assertEquals(1280, vp.width());
		assertEquals((1280 * 9) / 16, vp.height());
	}

	@Test
	public void viewportTestFit2() {
		vpb.height(1280).fit();
		Viewport vp = vpb.done();
		assertEquals((1280 * 16) / 9, vp.width());
		assertEquals(1280, vp.height());
	}
	@Test
	public void viewportTestFit3() {
		vpb.aspectWide().width(3200).fit();
		Viewport vp = vpb.done();
		assertEquals(3200, vp.width());
		assertEquals((3200 * 9) / 16, vp.height());
	}

	@Test
	public void viewportTestFit4() {
		vpb.height(2400).fit();
		Viewport vp = vpb.done();
		assertEquals((2400 * 16) / 9, vp.width());
		assertEquals(2400, vp.height());
	}
	@Test
	public void viewportTestFit5() {
		vpb.height(2400).width(2400).fit();
		Viewport vp = vpb.done();
		assertEquals(2400, vp.width());
		assertEquals(2400, vp.height());
	}
	@Test
	public void viewportTestExtend1() {
		vpb.width(1280).height(960).extend();
		Viewport vp = vpb.done();
		assertEquals(1706, vp.width());
		assertEquals(960, vp.height());
		assertEquals(vp.screenWidth(), vp.viewWidth());
		assertEquals(vp.screenHeight(), vp.viewHeight());
	}
	@Test
	public void viewportTestExtend2() {
		vpb.width(960).height(1280).extend();
		Viewport vp = vpb.done();
		assertEquals(2275, vp.width());
		assertEquals(1280, vp.height());
		assertEquals(vp.screenWidth(), vp.viewWidth());
		assertEquals(vp.screenHeight(), vp.viewHeight());
	}
	@Test
	public void viewportTest4() {
		vpb.width(1280).height(960).clip();
		Viewport vp = vpb.done();
		assertEquals(1280, vp.width());
		assertEquals(720, vp.height());
		assertEquals(vp.screenWidth(), vp.viewWidth());
		assertEquals(vp.screenHeight(), vp.viewHeight());
	}

	@Test
	public void viewportTestNativeAspect1() {
		vpb.aspectNative().width(1280).fit();
		Viewport vp = vpb.done();
		assertEquals(1280, vp.width());
		assertEquals((1280 * 9) / 16, vp.height());
	}
}
