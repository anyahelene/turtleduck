package turtleduck.bitmap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

public class BitmapGenerator {
	static String[] todo = { "1f", "1i", "3b", "3s", "3f", "4b", "4s", "4f" };
	private static List<String> lines;
	private static Path outDir;

	public static void main(String[] args) throws IOException {
		lines = Files.readAllLines(Path.of(args[0]));
		outDir = Path.of(args[1]);
		Files.createDirectories(outDir);
		for (String s : todo)
			generate(s);
	}

	private static void generate(String s) throws IOException {
		int channels = s.charAt(0) - '0';
		if (channels < 1 || channels > 4)
			throw new IllegalArgumentException(s);
		char typeChar = s.charAt(1);
		String typeName;
		String vectorName = "Vector" + s;
		int byteSize;
		switch (typeChar) {
		case 'b':
			typeName = "byte";
			vectorName = "Vector" + channels + "i";
			byteSize = 1;
			break;
		case 's':
			typeName = "short";
			vectorName = "Vector" + channels + "i";
			byteSize = 2;
			break;
		case 'i':
			typeName = "int";
			byteSize = 4;
			break;
		case 'f':
			typeName = "float";
			byteSize = 4;
			break;
		case 'd':
			typeName = "double";
			byteSize = 8;
			break;
		default:
			throw new IllegalArgumentException(s);
		}

		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			line = line.replace("_Template", s);
			line = line.replaceFirst("^class", "public class");
			line = line.replace("BYTE_SIZE = 4", "BYTE_SIZE = " + byteSize);
			line = line.replace("float", typeName);
			line = line.replace("Float", typeName.substring(0, 1).toUpperCase() + typeName.substring(1));
			line = line.replace("final int channels;", "static final int channels = " + channels + ";");
			line = line.replace(", int channels,", ",");
			line = line.replace("Vector4f", vectorName);
			line = line.replace("getByte", "get").replace("putByte", "put");
			if (line.contains("this.channels = channels;"))
				continue;
			if (channels < 2 && line.matches(".*// [GBA].*"))
				continue;
			if (channels < 3 && line.matches(".*// [BA].*"))
				continue;
			if (channels < 4 && line.contains("// A"))
				continue;
			if (channels > 1 && line.contains("// 1"))
				continue;
			line = line.replaceAll("// [RGBA1]", "");
			sb.append(line);
			sb.append("\n");
		}
		Files.writeString(outDir.resolve("Bitmap" + s + ".java"), sb.toString());
	}
}
