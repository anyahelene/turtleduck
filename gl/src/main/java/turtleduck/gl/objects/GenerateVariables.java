package turtleduck.gl.objects;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GenerateVariables {
	public static String[] baseTypes = { "int", "uint", "float", "double" };
	public static int[] baseTypeSizes = { 4, 4, 4, 8 };
	public static String[] baseJavaTypes = { "int", "int", "float", "double" };
	public static String[] baseGlTypes = { "GL_INT", "GL_UNSIGNED_INT", "GL_FLOAT", "GL_DOUBLE" };
	public static String[] bufferTypes = { "IntBuffer", "IntBuffer", "FloatBuffer", "DoubleBuffer" };
	public static String[] vecLetters = { "i", "u", "", "d" };
	public static String[] baseObjectTypes = { "Integer", "Integer", "Float", "Double" };
	public static String[] prefixes = { "GL32C.", "GL32C.", "GL32C.", "GL40C." };
	private static String[] vectorElements = { "x", "y", "z", "w" };

	public static void main(String[] as) throws FileNotFoundException, IOException {
		Map<String, String> glslTypes = new HashMap<>();
		Map<String, String> glTypes = new HashMap<>();
		Map<String, String> jTypes = new HashMap<>();

		String output = "";

		// System.out.println("import static org.lwjgl.opengl.GL40.*;");
		// System.out.println("import org.joml.*;");

		String init = "";
//		inti += "\tstatic class TypeDesc {\n";
//		init += "\t\tpublic final int id;\n\t\tpublic final String name;\n";
//		init += "\t\tpublic final Class<?> jomlClass;\n\t\tpublic final int rows;\n\t\tpublic final int cols;\n";
//		init += "\n\t\tpublic TypeDesc(int id, String name, Class<?> jomlClass, int m, int n) {\n";
//		init += "\t\t\tthis.id = id;\n\t\t\tthis.name = name;\n\t\t\tthis.jomlClass = jomlClass;\n\t\t\tthis.rows = m;\n\t\t\tthis.cols = n;\n\t\t}\n\t}\n\n";
//		init += "\tstatic public final Map<Integer, TypeDesc> GL_TYPES = new HashMap<>();\n";
//		init += "\tstatic public final Map<String, TypeDesc> GLSL_TYPES = new HashMap<>();\n";
//		init += "\tstatic public final Map<Class<?>, TypeDesc> JOML_TYPES = new HashMap<>();\n";
		init += "\n\tstatic {\n\t\tTypeDesc type;\n";
		for (int b = 0; b < baseTypes.length; b++) {
			String baseType = baseTypes[b];
			String baseJavaType = baseJavaTypes[b];
			String baseObjectType = baseObjectTypes[b];
			for (int i = 1; i <= 4; i++) {
				int J = i > 2 && b >= 2 ? 4 : 1;
				for (int j = 1; j <= J; j++) {
					String letter = baseType.substring(0, 1);
					String jLetter = baseJavaType.substring(0, 1);
					String glType = baseGlTypes[b];
					String prefix = prefixes[b];
					String type = baseType;
					String jType = baseJavaType;
					String className = "Uniform";
					letter = letter.equals("u") ? "ui" : letter;
					if (i == 1) {
						className += i + letter;
						jType = baseObjectType;
						output += String.format("\tstatic class %s extends AbstractUniform<%s> {\n\n", className,
								baseObjectType);
						output += String.format(
								"\t\tpublic %s get(%s unused) {\n\t\t\treturn %sglGetUniform%s(program.id(), loc);\n\t\t}\n",
								baseObjectType, baseObjectType, prefix, letter);
						output += String.format(
								"\t\tpublic %s get() {\n\t\t\treturn %sglGetUniform%s(program.id(), loc);\n\t\t}\n",
								baseObjectType, prefix, letter);
						output += String.format("\t\tpublic void set(%s val) {\n" //
								+ "\t\t\tprogram.bind();\n" + "\t\t\t%sglUniform%d%s(loc, val);\n\t\t}\n\n",
								baseObjectType, prefix, i, letter);
					} else if (j == 1) {
						String args = "";
						type = vecLetters[b] + "vec" + i;
						jType = "Vector" + i + jLetter;
						className += "Vec" + i + letter;
						glType += "_VEC" + i;

						output += String.format("\tstatic class %s extends AbstractUniform<%s> {\n\n", className,
								jType);
						for (int k = 0; k < i; k++) {
							args += ", val." + vectorElements[k];
						}
						output += String.format("\t\tpublic %s get(%s dest) {\n" //
								+ "\t\t\ttmpBuf.rewind().limit(size());\n" //
								+ "\t\t\t%sglGetUniform%sv(program.id(), loc, tmpBuf.as%s());\n" //
								+ "\t\t\treturn dest.set(tmpBuf.as%s());\n" + "\t\t}\n\n", jType, jType, prefix, letter,
								bufferTypes[b], bufferTypes[b]);
						output += String.format("\t\tpublic %s get() {\n" //
								+ "\t\t\ttmpBuf.rewind().limit(size());\n" //
								+ "\t\t\t%sglGetUniform%sv(program.id(), loc, tmpBuf.as%s());\n" //
								+ "\t\t\treturn new %s(tmpBuf.as%s());\n" + "\t\t}\n\n", jType, prefix, letter,
								bufferTypes[b], jType, bufferTypes[b]);
						output += String.format("\t\tpublic void set(%s val) {\n" //
								+ "\t\t\tprogram.bind();\n" + "\t\t\t%sglUniform%d%s(loc%s);\n\t\t}\n\n", jType, prefix,
								i, letter, args);
					} else {
						if (i != j && i > 2 && j != i - 1) {
							continue;
						}
						String suf = String.valueOf(i);
						if (i != j) {
							suf += "x" + j;
						}
						type = vecLetters[b] + "mat" + suf;
						jType = "Matrix" + suf + jLetter;
						className += "Mat" + suf + letter;
						glType += "_MAT" + suf;

						output += String.format("\tstatic class %s extends AbstractUniform<%s> {\n\n", className,
								jType);

						output += String.format("\t\tpublic %s get(%s dest) {\n" //
								+ "\t\t\ttmpBuf.rewind().limit(size());\n" //
								+ "\t\t\t%sglGetUniform%sv(program.id(), loc, tmpBuf.as%s());\n" //
								+ "\t\t\treturn dest.set(tmpBuf.as%s());\n" + "\t\t}\n\n", jType, jType, prefix, letter,
								bufferTypes[b], bufferTypes[b]);
						output += String.format("\t\tpublic %s get() {\n" //
								+ "\t\t\ttmpBuf.rewind().limit(size());\n" //
								+ "\t\t\t%sglGetUniform%sv(program.id(), loc, tmpBuf.as%s());\n" //
								+ "\t\t\treturn new %s(tmpBuf.as%s());\n" + "\t\t}\n\n", jType, prefix, letter,
								bufferTypes[b], jType, bufferTypes[b]);
						output += String.format("\t\tpublic void set(%s val)\n\t{\n" //
								+ "\t\t\tval.get(tmpBuf);\n" //
								+ "\t\t\ttmpBuf.limit(size());\n" //
								+ "\t\t\tprogram.bind();\n"
								+ "\t\t\t%sglUniformMatrix%d%sv(loc, false, tmpBuf.as%s());\n\t\t}\n\n", jType, prefix,
								i, letter, bufferTypes[b]);
					}
					output += String.format("\t\tpublic String typeName() {\n\t\t\treturn \"%s\";\n\t\t}\n\n", type); //
					output += String.format("\t\tpublic int typeId() {\n\t\t\treturn %s%s;\n\t\t}\n\n", prefix, glType); //
					output += String.format("\t\tpublic int size() {\n\t\t\treturn %d;\n\t\t}\n\n",
							baseTypeSizes[b] * i * j);

					output += String.format("\t}\n\n");
					glslTypes.put(type, className);
					glTypes.put(prefix + glType, className);
					jTypes.put(jType, className);
					init += String.format("\n\t\ttype = new TypeDesc(%s%s, \"%s\", \"%s\", %s.class, %d, %d);\n", prefix, glType,
							type, baseType, jType, i, j);
					init += String.format("\t\tGL_TYPES.put(%s%s, type);\n",prefix, glType);
					init += String.format("\t\tGLSL_TYPES.put(\"%s\", type);\n", type);
					init += String.format("\t\tJOML_TYPES.put(%s.class, type);\n", jType);
				}
			}
		}
		init += "\t}\n\n";
//		System.out.println(init);
//		System.out.println(glslTypes);
//		System.out.println(glslTypes.size());
//		System.out.println(glTypes);
//		System.out.println(glTypes.size());
//		System.out.println(jTypes);
//		System.out.println(jTypes.size());

		output += "\t@SuppressWarnings(\"unchecked\")\n";
		output += "\tprotected static <T> AbstractUniform<T> createVariable(String typeName) {\n";
		output += "\t\tswitch(typeName) {\n";
		for (Entry<String, String> entry : glslTypes.entrySet()) {
			output += String.format("\t\tcase \"%s\":\n", entry.getKey());
			output += String.format("\t\t\treturn (AbstractUniform<T>) new %s();\n", entry.getValue());
		}
		output += "\t\tdefault:\n";
		output += "\t\t\tthrow new IllegalArgumentException(typeName);\n";
		output += "\t\t}\n";
		output += "\t}\n\n";

		output += "\t@SuppressWarnings(\"unchecked\")\n";
		output += "\tprotected static <T> AbstractUniform<T> createVariable(int type) {\n";
		output += "\t\tswitch(type) {\n";
		for (Entry<String, String> entry : glTypes.entrySet()) {
			output += String.format("\t\tcase %s:\n", entry.getKey());
			output += String.format("\t\t\treturn (AbstractUniform<T>) new %s();\n", entry.getValue());
		}
		output += "\t\tdefault:\n";
		output += "\t\t\tthrow new IllegalArgumentException(String.valueOf(type));\n";
		output += "\t\t}\n";
		output += "\t}\n\n";

		output += "\t@SuppressWarnings(\"unchecked\")\n";
		output += "\tprotected static <T> AbstractUniform<T> createVariable(Class<T> clazz) {\n";
		output += "\t\tif(clazz == null) {\n";
		output += "\t\t\tthrow new IllegalArgumentException();\n";
		for (Entry<String, String> entry : jTypes.entrySet()) {
			output += String.format("\t\t} else if(clazz == %s.class) {\n", entry.getKey());
			output += String.format("\t\t\treturn (AbstractUniform<T>) new %s();\n", entry.getValue());
		}
		output += "\t\t} else {\n";
		output += "\t\t\tthrow new IllegalArgumentException(clazz.getName());\n";
		output += "\t\t}\n";
		output += "\t}\n\n";

		StringBuffer buf = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new FileReader(as[0]))) {
			boolean skip[] = { false };
			String mainContent = init + output;
			reader.lines().forEachOrdered((line) -> {
				if (line.contains("### BEGIN GENERATED CONTENT ###")) {
					skip[0] = true;
				} else if (line.contains("### END GENERATED CONTENT ###")) {
					skip[0] = false;
					buf.append("	// ### BEGIN GENERATED CONTENT ###\n");
					buf.append(mainContent);
					buf.append("	// ### END GENERATED CONTENT ###\n");
				} else if (!skip[0]) {
					buf.append(line);
					buf.append("\n");
				}
			});
		}

		try (PrintWriter writer = new PrintWriter(as[0])) {
			writer.print(buf);
		}

	}
}