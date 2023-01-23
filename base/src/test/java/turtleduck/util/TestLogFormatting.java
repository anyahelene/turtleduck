package turtleduck.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import turtleduck.geometry.Direction;

public class TestLogFormatting {

    

	@Test
	public void test1() {
		List<Object> result = Logging.formatMessage("Hello, {}!", "World");
		assertEquals(Arrays.asList("Hello, ", "World", "!"), result);
	}

	@Test
	public void test2() {
		List<Object> result = Logging.formatMessage("Hello, \\{}{}!", "World");
		assertEquals(Arrays.asList("Hello, ", "{}", "World", "!"), result);
	}

	@Test
	public void test3() {
		List<Object> result = Logging.formatMessage("{}{}", "World", "Domination");
		assertEquals(Arrays.asList("World", "Domination"), result);
	}

	@Test
	public void test4() {
		List<Object> result = Logging.formatMessage("{}{}", "World");
		assertEquals(Arrays.asList("World", null), result);
	}
}
