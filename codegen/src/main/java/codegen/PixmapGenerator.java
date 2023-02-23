package codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PixmapGenerator {
	/**
	 * Typenames converted to boxed type names
	 */
	private static final Map<String, String> boxedNames = new HashMap<>();
	/**
	 * Typenames as used in ByteBuffers get* / set* methods
	 */
	private static final Map<String, String> bufferNames = new HashMap<>();
	private static final String BASE_NAME = "Pixmap";
	static String[] todo = { //
			"1f", "1i", "1b", "1s", //
			"2f", "2i", "2b", "2s", //
			"3f", "3i", "3b", "3s", //
			"4f", "4i", "4b", "4s" };

	static String[] baseTypes = { "f", "i", "b", "s" };
	private static Path outDir;

	static {
		boxedNames.put("byte", "Byte");
		boxedNames.put("short", "Short");
		boxedNames.put("int", "Integer");
		boxedNames.put("long", "Long");
		boxedNames.put("float", "Float");
		boxedNames.put("double", "Double");
		boxedNames.put("char", "Character");
		bufferNames.put("byte", "");
		bufferNames.put("short", "Short");
		bufferNames.put("int", "Int");
		bufferNames.put("long", "Long");
		bufferNames.put("float", "Float");
		bufferNames.put("double", "Double");
		bufferNames.put("char", "Char");
	}

	public static void main(String[] args) throws IOException {
		Path srcDir = Path.of(args[0]);
		outDir = Path.of(args[1]);
		Files.createDirectories(outDir);

		for (String s : todo)
			new Generator(srcDir.resolve(BASE_NAME + "_CH__T__Template.java"), outDir, s).generate();
		;

		for (String s : baseTypes)
			new Generator(srcDir.resolve("Float" + BASE_NAME + "_Template.java"), outDir, "0" + s).generate();
		;

		for (String s : todo)
			new Generator(srcDir.resolve(BASE_NAME + "Impl_CH__T__Template.java"), outDir, s).generate();
		;
	}

	static class Generator {
		String typeName;
		String vectorName;
		int byteSize;
		int channels;
		char typeChar;
		String s;
		Path srcPath;
		Path path;

		Generator(Path srcPath, Path path, String s) {
			this.s = s;
			this.srcPath = srcPath;
			this.path = path;
			channels = s.charAt(0) - '0';
			if (channels < 0 || channels > 4)
				throw new IllegalArgumentException(s);
			typeChar = s.charAt(1);
			vectorName = "Vector" + s;
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
		}

		public void generate() throws IOException {
			List<String> lines = Files.readAllLines(srcPath);
			StringBuffer sb = new StringBuffer();
			sb.append("/*\n");
			sb.append(" * WARNING: DO NOT EDIT!\n");
			sb.append(" * This file is automatically generated from ");
			sb.append(srcPath.getFileName().toString());
			sb.append(" by ");
			sb.append(PixmapGenerator.class.getTypeName());
			sb.append("\n */\n\n");
			for (String l : lines) {
				String line = replace(l);
				if (line.contains("this.channels = channels;"))
					continue;
				if (channels < 2 && line.matches(".*// [GBA].*"))
					continue;
				if (channels < 3 && line.matches(".*// [BA].*"))
					continue;
				if (channels < 4 && line.contains("// A"))
					continue;
				if (channels != 1 && line.contains("// 1"))
					continue;
				if (channels != 2 && line.contains("// 2"))
					continue;
				if (channels != 3 && line.contains("// 3"))
					continue;
				if (channels != 4 && line.contains("// 4"))
					continue;
				if ("bsifd".codePoints()
						.anyMatch(c -> typeChar != c && line.contains("// " + String.valueOf((char) c))))
					continue;
				if (line.contains("// !" + typeChar))
					continue;
				sb.append(line.replaceAll("// !?[RGBA1234bsifd]", ""));
				sb.append("\n");
			}
			String outFile = replace(srcPath.getFileName().toString());
			System.out.println("writing " + path.resolve(outFile));
			Files.writeString(path.resolve(outFile), sb.toString());
		}

		public String replace(String line) {
			line = line.replace("_CH_", "" + channels).replace("_T_", "" + typeChar);
			line = line.replace("_TYPE_", typeName).replace("_Template", "");
			line = line.replace("_BOXED_", boxedNames.get(typeName));
			line = line.replace("_BYTE_SIZE_", "" + byteSize);
			line = line.replaceFirst("^class", "public class");
			line = line.replaceFirst("^interface", "public interface");
			line = line.replace("BYTE_SIZE = 4", "BYTE_SIZE = " + byteSize);
			line = line.replace("float", typeName);
			line = line.replace("putFloat", "put" + bufferNames.get(typeName));
			line = line.replace("getFloat", "get" + bufferNames.get(typeName));
			line = line.replace("_CTYPE_", bufferNames.get(typeName));
			line = line.replace("Float", boxedNames.get(typeName));
			line = line.replace("final int channels;", "static final int channels = " + channels + ";");
			line = line.replace(", int channels,", ",");
			line = line.replace("Vector4f", vectorName);
			line = line.replace("getByte", "get").replace("putByte", "put");
			return line;
		}
	}
}
