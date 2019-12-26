package turtleduck;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit test for simple App.
 */
public class AppTest {


    @Test
    @DisplayName("dummy test")
    public void dummyTest()
    {
        assertTrue( true );
    }
    

	@ParameterizedTest(name = "{0} + {1} = {2}")
	@CsvSource({
			"0,    1,   1",
			"1,    2,   3",
			"49,  51, 100",
			"1,  100, 101"
	})
	public void add(int first, int second, int expectedResult) {
		assertEquals(expectedResult, first + second);
	}
}
