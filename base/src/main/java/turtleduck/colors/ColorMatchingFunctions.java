package turtleduck.colors;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import turtleduck.util.IOUtil;

public class ColorMatchingFunctions {
    private static Map<String, ColorMatchingTable> tables = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(ColorMatchingFunctions.class);

    public static ColorMatchingTable table(String name) {
        ColorMatchingTable table = tables.get(name);
        if (table == null) {
            try {
                table = loadTable(name);
            } catch (IOException e) {
                logger.error("Failed to load color matching table", e);
                table = new DummyColorMatchingTableImpl();
            }
            tables.put(name, table);
        }
        return table;
    }

    private static ColorMatchingTable loadTable(String name) throws IOException {
        String fileName = "colors/" + name + ".csv";
        List<String> lines = IOUtil.instance.fromStream(ColorMatchingFunctions.class.getResourceAsStream(fileName))
                .fileName(fileName).toLines().toList();
        ColorMatchingTableImpl table = new ColorMatchingTableImpl();
        table.data = new double[lines.size() * 3];

        DoubleBuffer buffer = DoubleBuffer.wrap(table.data);
        lines.forEach(l -> {
            String[] split = l.split(",");
            int wavelength = (int) (10 * Double.valueOf(split[0]));

            if (table.offset == 0) {
                table.offset = wavelength;
            } else if (table.step == 0) {
                table.step = wavelength - table.offset;
            }
            table.last = wavelength;

            for (int i = 1; i < 4; i++) {
                buffer.put(Double.parseDouble(split[i]));
            }
        });
        return table;
    }

    static class DummyColorMatchingTableImpl implements ColorMatchingTable {

        @Override
        public Vector3d lookup(double wavelength, Vector3d dest) {
            if (dest == null)
                dest = new Vector3d();
            dest.set(0);
            return dest;
        }
    }

    static class ColorMatchingTableImpl implements ColorMatchingTable {
        double offset;
        double last;
        double step;
        double[] data;

        @Override
        public Vector3d lookup(double wavelength, Vector3d dest) {
            if (dest == null)
                dest = new Vector3d();
            wavelength *= 10;
            int index = 0;
            if (wavelength < offset) {
                index = 0;
            } else if (wavelength > last) {
                index = data.length - 3;
            } else {
                index = (int) Math.round(3 * (wavelength - offset) / step);
            }
            dest.x = data[index];
            dest.y = data[index + 1];
            dest.z = data[index + 2];
            return dest;
        }
    }
}
