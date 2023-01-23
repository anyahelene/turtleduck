package turtleduck.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class TextUtilTest {
    static Random random = new Random();

    static DoubleStream testValues() {
        double[] values = { 0.0, 1.0, 27.0, 100.0, 976.0, 999.0, 1000.0, 1001.0, 1023.0, 1024.0, 1025.0, 9762.0,
                10289.0, 0.1, 0.001,
                0.000_004_2, 0.000_000_000_000_99 };
        return DoubleStream.concat(DoubleStream.of(values), DoubleStream.of(values).map(v -> -v));
    }

    static DoubleStream randomValues() {
        Builder builder = DoubleStream.builder();
        for (int i = 0; i < 1000; i++) {
            builder.add(random.nextInt());
            builder.add(1.0 / random.nextInt());
        }
        return builder.build();
    }

    @MethodSource("testValues")
    @ParameterizedTest(name = "1 <= {0}*prefix < 1000")
    public void checkMagnitude10(double number) {
        int p = TextUtil.findSIPrefix(number);
        double n = Math.abs(number * TextUtil.PREFIX_SCALE10[p]);
        assertTrue(n >= 1 || p == 0 || number == 0, String.format("%f %s < 1", n, TextUtil.prefixes.get(p)));
        assertTrue(n < 1000 || p == TextUtil.prefixes.size(),
                String.format("%g/%s → %.20f %s >= 1000", number, TextUtil.prefixes.get(p), n,
                        TextUtil.prefixes.get(p)));
    }

    @MethodSource("testValues")
    @ParameterizedTest(name = "1 <= {0}*prefix < 1024")
    public void checkMagnitude2(double number) {
        int p = TextUtil.findBinaryPrefix(number);
        double n = Math.abs(number * TextUtil.PREFIX_SCALE2[p]);
        assertTrue(n >= 1 || p == 0 || number == 0, String.format("%f %s < 1", n, TextUtil.prefixes.get(p)));
        assertTrue(n < 1024 || p == TextUtil.prefixes.size(),
                String.format("%f %s >= 1024", n, TextUtil.prefixes.get(p)));
    }

    @MethodSource("randomValues")
    @ParameterizedTest(name = "checkSIPrefix({0})")
    public void checkSIPrefix(double number) {
        String prefix = TextUtil.prefixes.get(TextUtil.findSIPrefix(number));
        if (number >= 1e15)
            ;
        else if (number >= 1e12)
            assertEquals("T", prefix);
        else if (number >= 1e9)
            assertEquals("G", prefix);
        else if (number >= 1e6)
            assertEquals("M", prefix);
        else if (number >= 1e3)
            assertEquals("k", prefix);
        else if (number >= 1)
            assertEquals("", prefix);
        else if (number >= 1e-3)
            assertEquals("m", prefix);
        else if (number >= 1e-6)
            assertEquals("µ", prefix);
        else if (number >= 1e-9)
            assertEquals("n", prefix);
    }

    @MethodSource("randomValues")
    @ParameterizedTest(name = "checkBinaryPrefix({0})")
    public void checkBinaryPrefix(double number) {
        String prefix = TextUtil.prefixes.get(TextUtil.findBinaryPrefix(number));
        number = Math.abs(number);
        if (number >= 1L << 50)
            ;
        else if (number >= 1L << 40)
            assertEquals("T", prefix);
        else if (number >= 1L << 30)
            assertEquals("G", prefix);
        else if (number >= 1L << 20)
            assertEquals("M", prefix);
        else if (number >= 1L << 10)
            assertEquals("k", prefix);
        else if (number >= 1)
            assertEquals("", prefix);
        else if (number >= 1.0 / (1L << 10))
            assertEquals("m", prefix);
        else if (number >= 1.0 / (1L << 20))
            assertEquals("µ", prefix);
        else if (number >= 1.0 / (1L << 30))
            assertEquals("n", prefix);
    }
}
