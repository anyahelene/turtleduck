package turtleduck.util;

import java.util.Arrays;
import java.util.List;

public class TextUtil {
    public static final List<String> prefixes = List.of("y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T",
            "P", "E", "Z", "Y");
    public static final int[] PREFIX_POW2 = new int[prefixes.size()];
    public static final int[] PREFIX_POW10 = new int[prefixes.size()];
    public static final double[] PREFIX_SCALE2 = new double[prefixes.size()];
    public static final double[] PREFIX_SCALE10 = new double[prefixes.size()];
    public static final int NO_PREFIX = prefixes.indexOf("");

    static {
        for (int i = NO_PREFIX; i < prefixes.size(); i++) {
            PREFIX_POW10[i] = (i - NO_PREFIX) * 3;
            PREFIX_POW2[i] = (i - NO_PREFIX) * 10;
        }
        for (int i = NO_PREFIX - 1; i >= 0; i--) {
            PREFIX_POW10[i] = (i - NO_PREFIX) * 3;
            PREFIX_POW2[i] = (i - NO_PREFIX) * 10;
        }
        for (int i = 0; i < prefixes.size(); i++) {
            PREFIX_SCALE2[i] = Math.pow(2, -PREFIX_POW2[i]);
            PREFIX_SCALE10[i] = Math.pow(10, -PREFIX_POW10[i]);
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(PREFIX_POW2));
        System.out.println(Arrays.toString(PREFIX_POW10));
        double n = 1;
        for (int i = 0; i < 31; i++) {
            int p = findSIPrefix(n * .99);
            System.out.printf("%6.6f %1s %.0f %f %f%n", (n * .99) * Math.pow(2, -PREFIX_POW2[p]), prefixes.get(p),
                    n * .99, Math.log10(n * .99) / 3, Math.floor(Math.log10(n * .99) / 3));
            p = findSIPrefix(n);
            System.out.printf("%6.6f %1s %g%n", n * Math.pow(10, -PREFIX_POW10[p]), prefixes.get(p), n);
            p = findSIPrefix(n * 1.01);
            System.out.printf("%6.6f %1s %.0f%n", (n * 1.01) * Math.pow(2, -PREFIX_POW2[p]), prefixes.get(p), n * 1.01);
            n /= 10;
        }
    }

    public static int findBinaryPrefix(double number) {
        int exp = Math.floorDiv(Math.getExponent(number), 10);
        int prefix = exp + NO_PREFIX;
        prefix = Math.max(0, Math.min(prefix, prefixes.size() - 1));
        return prefix;
    }

    public static int findSIPrefix(double number) {
        if (number == 0)
            return NO_PREFIX;
        int exp = (int) Math.floor(Math.log10(Math.abs(number)) / 3);
        int prefix = exp + NO_PREFIX;
        prefix = Math.max(0, Math.min(prefix, prefixes.size() - 1));
        return prefix;
    }

    public static String humanFriendlySI(double number) {
        int p = findSIPrefix(number);
        return String.format("%.1f %s", number*PREFIX_SCALE10[p], prefixes.get(p));
    }

    public static String humanFriendlyBinary(double number) {
        int p = findBinaryPrefix(number);
        return String.format("%.1f %s%s", number*PREFIX_SCALE10[p], prefixes.get(p), p != NO_PREFIX ? "i" : "");
    }
}
