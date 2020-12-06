//Adapted from ASM:
//
//ASM: a very small and fast Java bytecode manipulation framework
//Copyright (c) 2000-2011 INRIA, France Telecom
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions
//are met:
//1. Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//3. Neither the name of the copyright holders nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
//THE POSSIBILITY OF SUCH DAMAGE.
package turtleduck.shell.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TextifierSupport;
import org.objectweb.asm.util.TraceSignatureVisitor;

/**
 * A {@link Printer} that prints a disassembled view of the classes it visits.
 *
 * @author Eric Bruneton
 */
public class Htmlifier extends Printer {

	/** The type of internal names. See {@link #appendDescriptor}. */
	public static final int INTERNAL_NAME = 0;

	/** The type of field descriptors. See {@link #appendDescriptor}. */
	public static final int FIELD_DESCRIPTOR = 1;

	/** The type of field signatures. See {@link #appendDescriptor}. */
	public static final int FIELD_SIGNATURE = 2;

	/** The type of method descriptors. See {@link #appendDescriptor}. */
	public static final int METHOD_DESCRIPTOR = 3;

	/** The type of method signatures. See {@link #appendDescriptor}. */
	public static final int METHOD_SIGNATURE = 4;

	/** The type of class signatures. See {@link #appendDescriptor}. */
	public static final int CLASS_SIGNATURE = 5;

	/** The type of method handle descriptors. See {@link #appendDescriptor}. */
	public static final int HANDLE_DESCRIPTOR = 9;

	private static final String CLASS_SUFFIX = ".class";
	private static final String DEPRECATED = "// DEPRECATED\n";
	private static final String RECORD = "// RECORD\n";
	private static final String INVISIBLE = " // invisible\n";

	private static final List<String> FRAME_TYPES = Collections
			.unmodifiableList(Arrays.asList("T", "I", "F", "D", "J", "N", "U"));

	/** The indentation of class members at depth level 1 (e.g. fields, methods). */
	protected String tab = "  ";

	/**
	 * The indentation of class elements at depth level 2 (e.g. bytecode
	 * instructions in methods).
	 */
	protected String tab2 = "    ";

	/**
	 * The indentation of class elements at depth level 3 (e.g. switch cases in
	 * methods).
	 */
	protected String tab3 = "      ";

	/** The indentation of labels. */
	protected String ltab = "   ";

	/** The names of the labels. */
	protected Map<Label, String> labelNames;

	/** The access flags of the visited class. */
	private int access;

	/** The number of annotation values visited so far. */
	private int numAnnotationValues;

	private Label label;
	private int lineNumber;
	private int currentOffset;
	private Collection<String> targets;
	private List<Instruction> insns = new ArrayList<>();

	/**
	 * Constructs a new {@link Htmlifier}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the {@link #Htmlifier(int)} version.
	 *
	 * @throws IllegalStateException If a subclass calls this constructor.
	 */
	public Htmlifier() {
		this(/* latest api = */ Opcodes.ASM9, null);
		if (getClass() != Htmlifier.class) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Constructs a new {@link Htmlifier}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the {@link #Htmlifier(int)} version.
	 *
	 * @param targets set of names of members that should be printed
	 * 
	 * @throws IllegalStateException If a subclass calls this constructor.
	 */
	public Htmlifier(Collection<String> targets) {
		this(/* latest api = */ Opcodes.ASM9, targets);
		if (getClass() != Htmlifier.class) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Constructs a new {@link Htmlifier}.
	 *
	 * @param api     the ASM API version implemented by this visitor. Must be one
	 *                of {@link Opcodes#ASM4}, {@link Opcodes#ASM5},
	 *                {@link Opcodes#ASM6}, {@link Opcodes#ASM7},
	 *                {@link Opcodes#ASM8} or {@link Opcodes#ASM9}.
	 * @param targets set of names of members that should be printed
	 */
	protected Htmlifier(final int api, Collection<String> targets) {
		super(api);
		this.targets = targets;
	}

// -----------------------------------------------------------------------------------------------
// Classes
// -----------------------------------------------------------------------------------------------

	@Override
	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {
		if ((access & Opcodes.ACC_MODULE) != 0) {
			// Modules are printed in visitModule.
			return;
		}
		this.access = access;
		int majorVersion = version & 0xFFFF;
		int minorVersion = version >>> 16;
		Map<String, Object> map = new HashMap<>();
		map.put("majorVersion", majorVersion);
		map.put("minorVersion", minorVersion);

		stringBuilder.setLength(0);
		stringBuilder.append("// class version ").append(majorVersion).append('.').append(minorVersion).append(" (")
				.append(version).append(")\n");
		if ((access & Opcodes.ACC_DEPRECATED) != 0) {
			map.put("deprecated", true);
		}
		if ((access & Opcodes.ACC_RECORD) != 0) {
			map.put("record", true);
		}
		appendRawAccess(map, access);

		appendDescriptor(CLASS_SIGNATURE, signature);
		if (signature != null) {
			appendJavaDeclaration(map, name, signature);
		}

		appendAccess(map, access & ~(Opcodes.ACC_SUPER | Opcodes.ACC_MODULE));
		if ((access & Opcodes.ACC_ANNOTATION) != 0) {
			map.put("kind", "@interface");
		} else if ((access & Opcodes.ACC_INTERFACE) != 0) {
			map.put("kind", "interface");
		} else if ((access & Opcodes.ACC_ENUM) == 0) {
			map.put("kind", "class");
		}
		appendDescriptor(INTERNAL_NAME, name);

		if (superName != null && !"java/lang/Object".equals(superName)) {
			stringBuilder.append(" extends ");
			appendDescriptor(INTERNAL_NAME, superName);
		}
		if (interfaces != null && interfaces.length > 0) {
			stringBuilder.append(" implements ");
			for (int i = 0; i < interfaces.length; ++i) {
				appendDescriptor(INTERNAL_NAME, interfaces[i]);
				if (i != interfaces.length - 1) {
					stringBuilder.append(' ');
				}
			}
		}
		stringBuilder.append(" {\n\n");

//		text.add(stringBuilder.toString());
	}

	@Override
	public void visitSource(final String file, final String debug) {
		stringBuilder.setLength(0);
		if (file != null) {
			stringBuilder.append(tab).append("// compiled from: ").append(file).append('\n');
		}
		if (debug != null) {
			stringBuilder.append(tab).append("// debug info: ").append(debug).append('\n');
		}
		if (stringBuilder.length() > 0) {
//			text.add(stringBuilder.toString());
		}
	}

	@Override
	public Printer visitModule(final String name, final int access, final String version) {
		stringBuilder.setLength(0);
		if ((access & Opcodes.ACC_OPEN) != 0) {
			stringBuilder.append("open ");
		}
		stringBuilder.append("module ").append(name).append(" { ").append(version == null ? "" : "// " + version)
				.append("\n\n");
//		text.add(stringBuilder.toString());
		return addNewHtmlifier(null);
	}

	@Override
	public void visitNestHost(final String nestHost) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("NESTHOST ");
		appendDescriptor(INTERNAL_NAME, nestHost);
		stringBuilder.append('\n');
//		text.add(stringBuilder.toString());
	}

	@Override
	public void visitOuterClass(final String owner, final String name, final String descriptor) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("OUTERCLASS ");
		appendDescriptor(INTERNAL_NAME, owner);
		stringBuilder.append(' ');
		if (name != null) {
			stringBuilder.append(name).append(' ');
		}
		appendDescriptor(METHOD_DESCRIPTOR, descriptor);
		stringBuilder.append('\n');
//		text.add(stringBuilder.toString());
	}

	@Override
	public Htmlifier visitClassAnnotation(final String descriptor, final boolean visible) {
//		text.add("\n");
		return visitAnnotation(descriptor, visible);
	}

	@Override
	public Printer visitClassTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
//		text.add("\n");
		return visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitClassAttribute(final Attribute attribute) {
//		text.add("\n");
		visitAttribute(attribute);
	}

	@Override
	public void visitNestMember(final String nestMember) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("NESTMEMBER ");
		appendDescriptor(INTERNAL_NAME, nestMember);
		stringBuilder.append('\n');
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitPermittedSubclass(final String permittedSubclass) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("PERMITTEDSUBCLASS ");
		appendDescriptor(INTERNAL_NAME, permittedSubclass);
		stringBuilder.append('\n');
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab);
		Map<String, Object> map = new HashMap<>();
		appendRawAccess(map, access & ~Opcodes.ACC_SUPER);
		stringBuilder.append(tab);
		appendAccess(map, access);
		stringBuilder.append("INNERCLASS ");
		appendDescriptor(INTERNAL_NAME, name);
		stringBuilder.append(' ');
		appendDescriptor(INTERNAL_NAME, outerName);
		stringBuilder.append(' ');
		appendDescriptor(INTERNAL_NAME, innerName);
		stringBuilder.append('\n');
//		text.add(stringBuilder.toString());
	}

	@Override
	public Printer visitRecordComponent(final String name, final String descriptor, final String signature) {
		Map<String, Object> map = new HashMap<>();
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("RECORDCOMPONENT ");
		if (signature != null) {
			stringBuilder.append(tab);
			appendDescriptor(FIELD_SIGNATURE, signature);
			stringBuilder.append(tab);
			appendJavaDeclaration(map, name, signature);
		}

		stringBuilder.append(tab);

		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append(' ').append(name);

		stringBuilder.append('\n');
//		text.add(stringBuilder.toString());
		return addNewHtmlifier(null);
	}

	@Override
	public Htmlifier visitField(final int access, final String name, final String descriptor, final String signature,
			final Object value) {
		if (targets != null && !targets.contains(name + descriptor))
			return null;
		Map<String, Object> map = new HashMap<>();
		stringBuilder.setLength(0);
		stringBuilder.append('\n');
		if ((access & Opcodes.ACC_DEPRECATED) != 0) {
			stringBuilder.append(tab).append(DEPRECATED);
		}
		stringBuilder.append(tab);
		appendRawAccess(map, access);
		if (signature != null) {
			stringBuilder.append(tab);
			appendDescriptor(FIELD_SIGNATURE, signature);
			stringBuilder.append(tab);
			appendJavaDeclaration(map, name, signature);
		}

		stringBuilder.append(tab);
		appendAccess(map, access);

		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append(' ').append(name);
		if (value != null) {
			stringBuilder.append(" = ");
			if (value instanceof String) {
				stringBuilder.append('\"').append(value).append('\"');
			} else {
				stringBuilder.append(value);
			}
		}

		stringBuilder.append('\n');
//		text.add(stringBuilder.toString());
		return addNewHtmlifier(null);
	}

	@Override
	public Htmlifier visitMethod(final int access, final String name, final String descriptor, final String signature,
			final String[] exceptions) {
		if (targets != null && !targets.contains(name + descriptor))
			return null;

		Map<String, Object> map = new HashMap<>();
		text.add("<method namedesc=\"" + name + descriptor + "\">\n");
		stringBuilder.setLength(0);
		stringBuilder.append('\n');
		if ((access & Opcodes.ACC_DEPRECATED) != 0) {
			stringBuilder.append(tab).append(DEPRECATED);
		}
		stringBuilder.append(tab);
		appendRawAccess(map, access);

		if (signature != null) {
			stringBuilder.append(tab);
			appendDescriptor(METHOD_SIGNATURE, signature);
			stringBuilder.append(tab);
			appendJavaDeclaration(map, name, signature);
		}
		/*
		 * stringBuilder.append(tab); appendAccess(map, access & ~(Opcodes.ACC_VOLATILE
		 * | Opcodes.ACC_TRANSIENT)); if ((access & Opcodes.ACC_NATIVE) != 0) {
		 * stringBuilder.append("native "); } if ((access & Opcodes.ACC_VARARGS) != 0) {
		 * stringBuilder.append("varargs "); } if ((access & Opcodes.ACC_BRIDGE) != 0) {
		 * stringBuilder.append("bridge "); } if ((this.access & Opcodes.ACC_INTERFACE)
		 * != 0 && (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_STATIC)) == 0) {
		 * stringBuilder.append("default "); }
		 */
		stringBuilder.append(StringEscapeUtils.escapeHtml4(name));
		appendDescriptor(METHOD_DESCRIPTOR, descriptor);
		if (exceptions != null && exceptions.length > 0) {
			stringBuilder.append(" throws ");
			for (String exception : exceptions) {
				appendDescriptor(INTERNAL_NAME, exception);
				stringBuilder.append(' ');
			}
		}

		stringBuilder.append('\n');
		text.add("<h6>" + stringBuilder.toString() + "{</h6>\n");
		return addNewHtmlifier(null);
	}

	@Override
	public void visitClassEnd() {
		text.add("}\n");
	}

// -----------------------------------------------------------------------------------------------
// Modules
// -----------------------------------------------------------------------------------------------

	@Override
	public void visitMainClass(final String mainClass) {
		stringBuilder.setLength(0);
		stringBuilder.append("  // main class ").append(mainClass).append('\n');
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitPackage(final String packaze) {
		stringBuilder.setLength(0);
		stringBuilder.append("  // package ").append(packaze).append('\n');
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitRequire(final String require, final int access, final String version) {
		Map<String, Object> map = new HashMap<>();
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("requires ");
		if ((access & Opcodes.ACC_TRANSITIVE) != 0) {
			stringBuilder.append("transitive ");
		}
		if ((access & Opcodes.ACC_STATIC_PHASE) != 0) {
			stringBuilder.append("static ");
		}
		stringBuilder.append(require).append(';');
		appendRawAccess(map, access);
		if (version != null) {
			stringBuilder.append("  // version ").append(version).append('\n');
		}
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitExport(final String packaze, final int access, final String... modules) {
		visitExportOrOpen("exports ", packaze, access, modules);
	}

	@Override
	public void visitOpen(final String packaze, final int access, final String... modules) {
		visitExportOrOpen("opens ", packaze, access, modules);
	}

	private void visitExportOrOpen(final String method, final String packaze, final int access,
			final String... modules) {
		Map<String, Object> map = new HashMap<>();
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append(method);
		stringBuilder.append(packaze);
		if (modules != null && modules.length > 0) {
			stringBuilder.append(" to");
		} else {
			stringBuilder.append(';');
		}
		appendRawAccess(map, access);
		if (modules != null && modules.length > 0) {
			for (int i = 0; i < modules.length; ++i) {
				stringBuilder.append(tab2).append(modules[i]);
				stringBuilder.append(i != modules.length - 1 ? ",\n" : ";\n");
			}
		}
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitUse(final String use) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("uses ");
		appendDescriptor(INTERNAL_NAME, use);
		stringBuilder.append(";\n");
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitProvide(final String provide, final String... providers) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("provides ");
		appendDescriptor(INTERNAL_NAME, provide);
		stringBuilder.append(" with\n");
		for (int i = 0; i < providers.length; ++i) {
			stringBuilder.append(tab2);
			appendDescriptor(INTERNAL_NAME, providers[i]);
			stringBuilder.append(i != providers.length - 1 ? ",\n" : ";\n");
		}
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitModuleEnd() {
		// Nothing to do.
	}

// -----------------------------------------------------------------------------------------------
// Annotations
// -----------------------------------------------------------------------------------------------

// DontCheck(OverloadMethodsDeclarationOrder): overloads are semantically different.
	@Override
	public void visit(final String name, final Object value) {
		visitAnnotationValue(name);
		if (value instanceof String) {
			visitString((String) value);
		} else if (value instanceof Type) {
			visitType((Type) value);
		} else if (value instanceof Byte) {
			visitByte(((Byte) value).byteValue());
		} else if (value instanceof Boolean) {
			visitBoolean(((Boolean) value).booleanValue());
		} else if (value instanceof Short) {
			visitShort(((Short) value).shortValue());
		} else if (value instanceof Character) {
			visitChar(((Character) value).charValue());
		} else if (value instanceof Integer) {
			visitInt(((Integer) value).intValue());
		} else if (value instanceof Float) {
			visitFloat(((Float) value).floatValue());
		} else if (value instanceof Long) {
			visitLong(((Long) value).longValue());
		} else if (value instanceof Double) {
			visitDouble(((Double) value).doubleValue());
		} else if (value.getClass().isArray()) {
			stringBuilder.append('{');
			if (value instanceof byte[]) {
				byte[] byteArray = (byte[]) value;
				for (int i = 0; i < byteArray.length; i++) {
					maybeAppendComma(i);
					visitByte(byteArray[i]);
				}
			} else if (value instanceof boolean[]) {
				boolean[] booleanArray = (boolean[]) value;
				for (int i = 0; i < booleanArray.length; i++) {
					maybeAppendComma(i);
					visitBoolean(booleanArray[i]);
				}
			} else if (value instanceof short[]) {
				short[] shortArray = (short[]) value;
				for (int i = 0; i < shortArray.length; i++) {
					maybeAppendComma(i);
					visitShort(shortArray[i]);
				}
			} else if (value instanceof char[]) {
				char[] charArray = (char[]) value;
				for (int i = 0; i < charArray.length; i++) {
					maybeAppendComma(i);
					visitChar(charArray[i]);
				}
			} else if (value instanceof int[]) {
				int[] intArray = (int[]) value;
				for (int i = 0; i < intArray.length; i++) {
					maybeAppendComma(i);
					visitInt(intArray[i]);
				}
			} else if (value instanceof long[]) {
				long[] longArray = (long[]) value;
				for (int i = 0; i < longArray.length; i++) {
					maybeAppendComma(i);
					visitLong(longArray[i]);
				}
			} else if (value instanceof float[]) {
				float[] floatArray = (float[]) value;
				for (int i = 0; i < floatArray.length; i++) {
					maybeAppendComma(i);
					visitFloat(floatArray[i]);
				}
			} else if (value instanceof double[]) {
				double[] doubleArray = (double[]) value;
				for (int i = 0; i < doubleArray.length; i++) {
					maybeAppendComma(i);
					visitDouble(doubleArray[i]);
				}
			}
			stringBuilder.append('}');
		}
		text.add(stringBuilder.toString());
	}

	private void visitInt(final int value) {
		stringBuilder.append(value);
	}

	private void visitLong(final long value) {
		stringBuilder.append(value).append('L');
	}

	private void visitFloat(final float value) {
		stringBuilder.append(value).append('F');
	}

	private void visitDouble(final double value) {
		stringBuilder.append(value).append('D');
	}

	private void visitChar(final char value) {
		stringBuilder.append("(char)").append((int) value);
	}

	private void visitShort(final short value) {
		stringBuilder.append("(short)").append(value);
	}

	private void visitByte(final byte value) {
		stringBuilder.append("(byte)").append(value);
	}

	private void visitBoolean(final boolean value) {
		stringBuilder.append(value);
	}

	private void visitString(final String value) {
		appendString(stringBuilder, value);
	}

	private void visitType(final Type value) {
		stringBuilder.append(value.getClassName()).append(CLASS_SUFFIX);
	}

	@Override
	public void visitEnum(final String name, final String descriptor, final String value) {
		visitAnnotationValue(name);
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('.').append(value);
		text.add(stringBuilder.toString());
	}

	@Override
	public Htmlifier visitAnnotation(final String name, final String descriptor) {
		visitAnnotationValue(name);
		stringBuilder.append('@');
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('(');
		text.add(stringBuilder.toString());
		return addNewHtmlifier(")");
	}

	@Override
	public Htmlifier visitArray(final String name) {
		visitAnnotationValue(name);
		stringBuilder.append('{');
		text.add(stringBuilder.toString());
		return addNewHtmlifier("}");
	}

	@Override
	public void visitAnnotationEnd() {
		// Nothing to do.
	}

	private void visitAnnotationValue(final String name) {
//		stringBuilder.setLength(0);
//		maybeAppendComma(numAnnotationValues++);
//		if (name != null) {
//			stringBuilder.append(name).append('=');
//		}
	}

// -----------------------------------------------------------------------------------------------
// Record components
// -----------------------------------------------------------------------------------------------

	@Override
	public Htmlifier visitRecordComponentAnnotation(final String descriptor, final boolean visible) {
		return visitAnnotation(descriptor, visible);
	}

	@Override
	public Printer visitRecordComponentTypeAnnotation(final int typeRef, final TypePath typePath,
			final String descriptor, final boolean visible) {
		return visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitRecordComponentAttribute(final Attribute attribute) {
		visitAttribute(attribute);
	}

	@Override
	public void visitRecordComponentEnd() {
		// Nothing to do.
	}

// -----------------------------------------------------------------------------------------------
// Fields
// -----------------------------------------------------------------------------------------------

	@Override
	public Htmlifier visitFieldAnnotation(final String descriptor, final boolean visible) {
		return visitAnnotation(descriptor, visible);
	}

	@Override
	public Printer visitFieldTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
		return visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitFieldAttribute(final Attribute attribute) {
		visitAttribute(attribute);
	}

	@Override
	public void visitFieldEnd() {
		// Nothing to do.
	}

// -----------------------------------------------------------------------------------------------
// Methods
// -----------------------------------------------------------------------------------------------

	@Override
	public void visitParameter(final String name, final int access) {
//		Map<String, Object> map = new HashMap<>();
//		stringBuilder.setLength(0);
//		stringBuilder.append(tab2).append("// parameter ");
//		appendAccess(map, access);
//		stringBuilder.append(' ').append((name == null) ? "<no name>" : name).append('\n');
//		text.add(stringBuilder.toString());
	}

	@Override
	public Htmlifier visitAnnotationDefault() {
		text.add(tab2 + "default=");
		return addNewHtmlifier("\n");
	}

	@Override
	public Htmlifier visitMethodAnnotation(final String descriptor, final boolean visible) {
		return visitAnnotation(descriptor, visible);
	}

	@Override
	public Printer visitMethodTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
		return visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public Htmlifier visitAnnotableParameterCount(final int parameterCount, final boolean visible) {
//		stringBuilder.setLength(0);
//		stringBuilder.append(tab2).append("// annotable parameter count: ");
//		stringBuilder.append(parameterCount);
//		stringBuilder.append(visible ? " (visible)\n" : " (invisible)\n");
//		text.add(stringBuilder.toString());
		return this;
	}

	@Override
	public Htmlifier visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
//		stringBuilder.setLength(0);
//		stringBuilder.append(tab2).append('@');
//		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
//		stringBuilder.append('(');
//		text.add(stringBuilder.toString());
//
//		stringBuilder.setLength(0);
//		stringBuilder.append(visible ? ") // parameter " : ") // invisible, parameter ").append(parameter).append('\n');
//		return addNewHtmlifier(stringBuilder.toString());
		return this;
	}

	@Override
	public void visitMethodAttribute(final Attribute attribute) {
		visitAttribute(attribute);
	}

	@Override
	public void visitCode() {
		currentOffset = 0;
	}

	@Override
	public void visitFrame(final int type, final int numLocal, final Object[] local, final int numStack,
			final Object[] stack) {
		stringBuilder.setLength(0);
		stringBuilder.append(ltab);
		stringBuilder.append("FRAME ");
		switch (type) {
		case Opcodes.F_NEW:
		case Opcodes.F_FULL:
			stringBuilder.append("FULL [");
			appendFrameTypes(numLocal, local);
			stringBuilder.append("] [");
			appendFrameTypes(numStack, stack);
			stringBuilder.append(']');
			break;
		case Opcodes.F_APPEND:
			stringBuilder.append("APPEND [");
			appendFrameTypes(numLocal, local);
			stringBuilder.append(']');
			break;
		case Opcodes.F_CHOP:
			stringBuilder.append("CHOP ").append(numLocal);
			break;
		case Opcodes.F_SAME:
			stringBuilder.append("SAME");
			break;
		case Opcodes.F_SAME1:
			stringBuilder.append("SAME1 ");
			appendFrameTypes(1, stack);
			break;
		default:
			throw new IllegalArgumentException();
		}
		stringBuilder.append('\n');
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitInsn(final int opcode) {
		Instruction insn = beginInstruction(opcode);
		currentOffset += 1;

	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(opcode == Opcodes.NEWARRAY ? TYPES[operand] : Integer.toString(operand));
		switch (opcode) {
		case Opcodes.BIPUSH:
		case Opcodes.NEWARRAY:
			currentOffset += 2;
			break;
		case Opcodes.SIPUSH:
			currentOffset += 3;
		}
		currentOffset += 1;
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(new Var(var));
		if (opcode > Opcodes.ALOAD && opcode < Opcodes.IALOAD)
			currentOffset += 1;
		else if (opcode > Opcodes.ASTORE && opcode < Opcodes.IASTORE)
			currentOffset += 1;
		else if (var > 255)
			currentOffset += 4;
		else
			currentOffset += 2;
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(type); // INTERNAL_NAME
		currentOffset += 3;
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(owner + "." + name); // INTERNAL_NAME
		insn.args.add(descriptor); // FIELD_DESCRIPTOR
		currentOffset += 3;
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor,
			final boolean isInterface) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(owner + "." + name); // INTERNAL_NAME
		insn.args.add(descriptor); // METHOD_DESCRIPTOR
		if (isInterface) {
			insn.args.add("(itf)");
		}
		if (opcode == Opcodes.INVOKEINTERFACE)
			currentOffset += 5;
		else
			currentOffset += 3;
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle,
			final Object... bootstrapMethodArguments) {
		Instruction insn = beginInstruction(Opcodes.INVOKEDYNAMIC);
		insn.args.add(name);
		insn.args.add(descriptor); // METHOD_DESCRIPTOR
		stringBuilder.setLength(0);
		stringBuilder.append(" [");
		stringBuilder.append('\n');
		stringBuilder.append(tab3);
		appendHandle(bootstrapMethodHandle);
		stringBuilder.append('\n');
		stringBuilder.append(tab3).append("// arguments:");
		if (bootstrapMethodArguments.length == 0) {
			stringBuilder.append(" none");
		} else {
			stringBuilder.append('\n');
			for (Object value : bootstrapMethodArguments) {
				stringBuilder.append(tab3);
				if (value instanceof String) {
					Printer.appendString(stringBuilder, (String) value);
				} else if (value instanceof Type) {
					Type type = (Type) value;
					if (type.getSort() == Type.METHOD) {
						appendDescriptor(METHOD_DESCRIPTOR, type.getDescriptor());
					} else {
						visitType(type);
					}
				} else if (value instanceof Handle) {
					appendHandle((Handle) value);
				} else {
					stringBuilder.append(value);
				}
				stringBuilder.append(", \n");
			}
			stringBuilder.setLength(stringBuilder.length() - 3);
		}
		stringBuilder.append('\n');
		stringBuilder.append(tab2).append("]\n");
		insn.args.add(stringBuilder.toString());
		currentOffset += 5;
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		Instruction insn = beginInstruction(opcode);
		insn.args.add(label(label));
		currentOffset += 3; // TODO: won't work with wide jump
	}

	@Override
	public void visitLabel(final Label lbl) {
		label = lbl;
		System.out.println("Label " + lbl + " = " + label(lbl) + " insn: " + (insns.isEmpty() ? "" : insns.get(insns.size()-1)));
	}

	@Override
	public void visitLdcInsn(final Object value) {
		Instruction insn = beginInstruction(Opcodes.LDC);
		if (value instanceof String) {
			stringBuilder.setLength(0);
			Printer.appendString(stringBuilder, (String) value);
			insn.args.add(stringBuilder.toString());
		} else if (value instanceof Type) {
			insn.args.add(((Type) value).getDescriptor() + CLASS_SUFFIX);
		} else {
			insn.args.add(value);
		}
		currentOffset += 2;
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		stringBuilder.setLength(0);
		Instruction insn = beginInstruction(Opcodes.IINC);
		insn.args.add(new Var(var));
		insn.args.add(increment);
		if (var > 255 || increment < Byte.MIN_VALUE || increment > Byte.MAX_VALUE)
			currentOffset += 6;
		else
			currentOffset += 3;
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
		stringBuilder.setLength(0);
		Instruction insn = beginInstruction(Opcodes.TABLESWITCH);
		for (int i = 0; i < labels.length; ++i) {
			stringBuilder.append(tab3).append(min + i).append(": ");
			stringBuilder.append(label(labels[i]));
			stringBuilder.append('\n');
		}
		stringBuilder.append(tab3).append("default: ");
		stringBuilder.append(label(dflt));
		text.add(stringBuilder.toString());
		currentOffset += 12;
		currentOffset += 4 * labels.length;

	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
		stringBuilder.setLength(0);
		Instruction insn = beginInstruction(Opcodes.LOOKUPSWITCH);

		for (int i = 0; i < labels.length; ++i) {
			stringBuilder.append(tab3).append(keys[i]).append(": ");
			stringBuilder.append(label(labels[i]));
			stringBuilder.append('\n');
		}
		stringBuilder.append(tab3).append("default: ");
		stringBuilder.append(label(dflt));
		text.add(stringBuilder.toString());
		currentOffset += 8;
		currentOffset += 8 * labels.length;

	}

	@Override
	public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
		stringBuilder.setLength(0);
		Instruction insn = beginInstruction(Opcodes.MULTIANEWARRAY);
		insn.args.add(descriptor); // FIELD_DESCRIPTOR
		insn.args.add(numDimensions);
		currentOffset += 4;
	}

	@Override
	public Printer visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
		return visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
		stringBuilder.setLength(0);
		stringBuilder.append("TRYCATCHBLOCK ");
		stringBuilder.append(label(start));
		stringBuilder.append(' ');
		stringBuilder.append(label(end));
		stringBuilder.append(' ');
		stringBuilder.append(label(handler));
		stringBuilder.append(' ');
		appendDescriptor(INTERNAL_NAME, type);
		text.add(stringBuilder.toString());
	}

	@Override
	public Printer visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab2).append("TRYCATCHBLOCK @");
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('(');
		text.add(stringBuilder.toString());

		stringBuilder.setLength(0);
		stringBuilder.append(") : ");
		appendTypeReference(typeRef);
		stringBuilder.append(", ").append(typePath);
		stringBuilder.append(visible ? "\n" : INVISIBLE);
		return addNewHtmlifier(stringBuilder.toString());
	}

	@Override
	public void visitLocalVariable(final String name, final String descriptor, final String signature,
			final Label start, final Label end, final int index) {
		boolean alive = false;
		System.out.println("visitLocalVariable " + name + " " + start + "–" + end);
		for (Instruction insn : insns) {
			if (insn.label == start)
				alive = true;
			if (alive) {
				for (Object o : insn.args) {
					if (o instanceof Var) {
						Var v = (Var) o;
						if (v.slot == index) {
							v.name = name;
							v.desc = descriptor;
						}
					}
				}
				if (insn.label == end)
					return;
			}
		}
	}

	@Override
	public Printer visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start,
			final Label[] end, final int[] index, final String descriptor, final boolean visible) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab2).append("LOCALVARIABLE @");
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('(');
		text.add(stringBuilder.toString());

		stringBuilder.setLength(0);
		stringBuilder.append(") : ");
		appendTypeReference(typeRef);
		stringBuilder.append(", ").append(typePath);
		for (int i = 0; i < start.length; ++i) {
			stringBuilder.append(" [ ");
			stringBuilder.append(label(start[i]));
			stringBuilder.append(" - ");
			stringBuilder.append(label(end[i]));
			stringBuilder.append(" - ").append(index[i]).append(" ]");
		}
		stringBuilder.append(visible ? "\n" : INVISIBLE);
		return addNewHtmlifier(stringBuilder.toString());
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		label = start;
		start.info = line;
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		stringBuilder.setLength(0);
		stringBuilder.append("<maxstack n=").append(maxStack).append("></maxstack>\n");

		stringBuilder.append("<maxlocals n=").append(maxLocals).append("></maxlocals>\n");
		text.add(stringBuilder.toString());
	}

	@Override
	public void visitMethodEnd() {
		stringBuilder.setLength(0);
		stringBuilder.append("<instructions>");
		int line = -1;
		for (Instruction insn : insns) {
			if (insn.label != null && insn.label.info instanceof Integer)
				line = (int) insn.label.info;
			insn.line = line;
			stringBuilder.append(insn.toString());
		}
		stringBuilder.append("</instructions>");
		stringBuilder.append("<h6>}</h6>\n");

		stringBuilder.append("</method>");
		text.add(stringBuilder.toString());
	}

// -----------------------------------------------------------------------------------------------
// Common methods
// -----------------------------------------------------------------------------------------------

	/**
	 * Prints a disassembled view of the given annotation.
	 *
	 * @param descriptor the class descriptor of the annotation class.
	 * @param visible    {@literal true} if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values.
	 */
// DontCheck(OverloadMethodsDeclarationOrder): overloads are semantically different.
	public Htmlifier visitAnnotation(final String descriptor, final boolean visible) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append('@');
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('(');
		text.add(stringBuilder.toString());
		return addNewHtmlifier(visible ? ")\n" : ") // invisible\n");
	}

	/**
	 * Prints a disassembled view of the given type annotation.
	 *
	 * @param typeRef    a reference to the annotated type. See
	 *                   {@link TypeReference}.
	 * @param typePath   the path to the annotated type argument, wildcard bound,
	 *                   array element type, or static inner type within 'typeRef'.
	 *                   May be {@literal null} if the annotation targets 'typeRef'
	 *                   as a whole.
	 * @param descriptor the class descriptor of the annotation class.
	 * @param visible    {@literal true} if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values.
	 */
	public Htmlifier visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
			final boolean visible) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append('@');
		appendDescriptor(FIELD_DESCRIPTOR, descriptor);
		stringBuilder.append('(');
		text.add(stringBuilder.toString());

		stringBuilder.setLength(0);
		stringBuilder.append(") : ");
		appendTypeReference(typeRef);
		stringBuilder.append(", ").append(typePath);
		stringBuilder.append(visible ? "\n" : INVISIBLE);
		return addNewHtmlifier(stringBuilder.toString());
	}

	/**
	 * Prints a disassembled view of the given attribute.
	 *
	 * @param attribute an attribute.
	 */
	public void visitAttribute(final Attribute attribute) {
		stringBuilder.setLength(0);
		stringBuilder.append(tab).append("ATTRIBUTE ");
		appendDescriptor(-1, attribute.type);

		if (attribute instanceof TextifierSupport) {
			if (labelNames == null) {
				labelNames = new HashMap<>();
			}
			((TextifierSupport) attribute).textify(stringBuilder, labelNames);
		} else {
			stringBuilder.append(" : unknown\n");
		}

		text.add(stringBuilder.toString());
	}

// -----------------------------------------------------------------------------------------------
// Utility methods
// -----------------------------------------------------------------------------------------------

	private Instruction beginInstruction(int opcode) {
		Instruction insn = new Instruction();
		insn.opcode = opcode;
		insn.label = label;
		insn.offset = currentOffset;
		insns.add(insn);
		label = null;
		return insn;
	}

	/**
	 * Appends a string representation of the given access flags to
	 * {@link #stringBuilder}.
	 *
	 * @param accessFlags some access flags.
	 */
	private void appendAccess(Map<String, Object> map, final int accessFlags) {
		List<String> acc = new ArrayList<>();
		map.put("access", acc);
		if ((accessFlags & Opcodes.ACC_PUBLIC) != 0) {
			acc.add("public");
		}
		if ((accessFlags & Opcodes.ACC_PRIVATE) != 0) {
			acc.add("private");
		}
		if ((accessFlags & Opcodes.ACC_PROTECTED) != 0) {
			acc.add("protected");
		}
		if ((accessFlags & Opcodes.ACC_FINAL) != 0) {
			acc.add("final");
		}
		if ((accessFlags & Opcodes.ACC_STATIC) != 0) {
			acc.add("static");
		}
		if ((accessFlags & Opcodes.ACC_SYNCHRONIZED) != 0) {
			acc.add("synchronized");
		}
		if ((accessFlags & Opcodes.ACC_VOLATILE) != 0) {
			acc.add("volatile");
		}
		if ((accessFlags & Opcodes.ACC_TRANSIENT) != 0) {
			acc.add("transient");
		}
		if ((accessFlags & Opcodes.ACC_ABSTRACT) != 0) {
			acc.add("abstract");
		}
		if ((accessFlags & Opcodes.ACC_STRICT) != 0) {
			acc.add("strictfp");
		}
		if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0) {
			acc.add("synthetic");
		}
		if ((accessFlags & Opcodes.ACC_MANDATED) != 0) {
			acc.add("mandated");
		}
		if ((accessFlags & Opcodes.ACC_ENUM) != 0) {
			acc.add("enum");
		}
	}

	/**
	 * Appends the hexadecimal value of the given access flags to
	 * {@link #stringBuilder}.
	 *
	 * @param accessFlags some access flags.
	 */
	private void appendRawAccess(Map<String, Object> map, final int accessFlags) {
		map.put("accessFlags", accessFlags);
	}

	/**
	 * Appends an internal name, a type descriptor or a type signature to
	 * {@link #stringBuilder}.
	 *
	 * @param type  the type of 'value'. Must be one of {@link #INTERNAL_NAME},
	 *              {@link #FIELD_DESCRIPTOR}, {@link #FIELD_SIGNATURE},
	 *              {@link #METHOD_DESCRIPTOR}, {@link #METHOD_SIGNATURE},
	 *              {@link #CLASS_SIGNATURE} or {@link #HANDLE_DESCRIPTOR}.
	 * @param value an internal name, type descriptor or a type signature. May be
	 *              {@literal null}.
	 */
	protected void appendDescriptor(final int type, final String value) {
		if (type == CLASS_SIGNATURE || type == FIELD_SIGNATURE || type == METHOD_SIGNATURE) {
			if (value != null) {
				stringBuilder.append("// signature ").append(value).append('\n');
			}
		} else {
			stringBuilder.append(value);
		}
	}

	/**
	 * Appends the Java generic type declaration corresponding to the given
	 * signature.
	 *
	 * @param name      a class, field or method name.
	 * @param signature a class, field or method signature.
	 */
	private void appendJavaDeclaration(Map<String, Object> map, final String name, final String signature) {
		TraceSignatureVisitor traceSignatureVisitor = new TraceSignatureVisitor(access);
		new SignatureReader(signature).accept(traceSignatureVisitor);
		stringBuilder.append("// declaration: ");
		if (traceSignatureVisitor.getReturnType() != null) {
			stringBuilder.append(traceSignatureVisitor.getReturnType());
			stringBuilder.append(' ');
		}
		stringBuilder.append(name);
		stringBuilder.append(traceSignatureVisitor.getDeclaration());
		if (traceSignatureVisitor.getExceptions() != null) {
			stringBuilder.append(" throws ").append(traceSignatureVisitor.getExceptions());
		}
		stringBuilder.append('\n');
	}

	/**
	 * Appends the name of the given label to {@link #stringBuilder}. Constructs a
	 * new label name if the given label does not yet have one.
	 *
	 * @param label a label.
	 */
	protected String label(final Label label) {
		if (labelNames == null) {
			labelNames = new HashMap<>();
		}
		String name = labelNames.get(label);
		if (name == null) {
			name = "L" + labelNames.size();
			labelNames.put(label, name);
		}
		return name;
	}

	/**
	 * Appends a string representation of the given handle to
	 * {@link #stringBuilder}.
	 *
	 * @param handle a handle.
	 */
	protected void appendHandle(final Handle handle) {
		int tag = handle.getTag();
		stringBuilder.append("// handle kind 0x").append(Integer.toHexString(tag)).append(" : ");
		boolean isMethodHandle = false;
		switch (tag) {
		case Opcodes.H_GETFIELD:
			stringBuilder.append("GETFIELD");
			break;
		case Opcodes.H_GETSTATIC:
			stringBuilder.append("GETSTATIC");
			break;
		case Opcodes.H_PUTFIELD:
			stringBuilder.append("PUTFIELD");
			break;
		case Opcodes.H_PUTSTATIC:
			stringBuilder.append("PUTSTATIC");
			break;
		case Opcodes.H_INVOKEINTERFACE:
			stringBuilder.append("INVOKEINTERFACE");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKESPECIAL:
			stringBuilder.append("INVOKESPECIAL");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKESTATIC:
			stringBuilder.append("INVOKESTATIC");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKEVIRTUAL:
			stringBuilder.append("INVOKEVIRTUAL");
			isMethodHandle = true;
			break;
		case Opcodes.H_NEWINVOKESPECIAL:
			stringBuilder.append("NEWINVOKESPECIAL");
			isMethodHandle = true;
			break;
		default:
			throw new IllegalArgumentException();
		}
		stringBuilder.append('\n');
		stringBuilder.append(tab3);
		appendDescriptor(INTERNAL_NAME, handle.getOwner());
		stringBuilder.append('.');
		stringBuilder.append(handle.getName());
		if (!isMethodHandle) {
			stringBuilder.append('(');
		}
		appendDescriptor(HANDLE_DESCRIPTOR, handle.getDesc());
		if (!isMethodHandle) {
			stringBuilder.append(')');
		}
		if (handle.isInterface()) {
			stringBuilder.append(" itf");
		}
	}

	/**
	 * Appends a comma to {@link #stringBuilder} if the given number is strictly
	 * positive.
	 *
	 * @param numValues a number of 'values visited so far', for instance the number
	 *                  of annotation values visited so far in an annotation
	 *                  visitor.
	 */
	private void maybeAppendComma(final int numValues) {
		if (numValues > 0) {
			stringBuilder.append(", ");
		}
	}

	/**
	 * Appends a string representation of the given type reference to
	 * {@link #stringBuilder}.
	 *
	 * @param typeRef a type reference. See {@link TypeReference}.
	 */
	private void appendTypeReference(final int typeRef) {
		TypeReference typeReference = new TypeReference(typeRef);
		switch (typeReference.getSort()) {
		case TypeReference.CLASS_TYPE_PARAMETER:
			stringBuilder.append("CLASS_TYPE_PARAMETER ").append(typeReference.getTypeParameterIndex());
			break;
		case TypeReference.METHOD_TYPE_PARAMETER:
			stringBuilder.append("METHOD_TYPE_PARAMETER ").append(typeReference.getTypeParameterIndex());
			break;
		case TypeReference.CLASS_EXTENDS:
			stringBuilder.append("CLASS_EXTENDS ").append(typeReference.getSuperTypeIndex());
			break;
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
			stringBuilder.append("CLASS_TYPE_PARAMETER_BOUND ").append(typeReference.getTypeParameterIndex())
					.append(", ").append(typeReference.getTypeParameterBoundIndex());
			break;
		case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
			stringBuilder.append("METHOD_TYPE_PARAMETER_BOUND ").append(typeReference.getTypeParameterIndex())
					.append(", ").append(typeReference.getTypeParameterBoundIndex());
			break;
		case TypeReference.FIELD:
			stringBuilder.append("FIELD");
			break;
		case TypeReference.METHOD_RETURN:
			stringBuilder.append("METHOD_RETURN");
			break;
		case TypeReference.METHOD_RECEIVER:
			stringBuilder.append("METHOD_RECEIVER");
			break;
		case TypeReference.METHOD_FORMAL_PARAMETER:
			stringBuilder.append("METHOD_FORMAL_PARAMETER ").append(typeReference.getFormalParameterIndex());
			break;
		case TypeReference.THROWS:
			stringBuilder.append("THROWS ").append(typeReference.getExceptionIndex());
			break;
		case TypeReference.LOCAL_VARIABLE:
			stringBuilder.append("LOCAL_VARIABLE");
			break;
		case TypeReference.RESOURCE_VARIABLE:
			stringBuilder.append("RESOURCE_VARIABLE");
			break;
		case TypeReference.EXCEPTION_PARAMETER:
			stringBuilder.append("EXCEPTION_PARAMETER ").append(typeReference.getTryCatchBlockIndex());
			break;
		case TypeReference.INSTANCEOF:
			stringBuilder.append("INSTANCEOF");
			break;
		case TypeReference.NEW:
			stringBuilder.append("NEW");
			break;
		case TypeReference.CONSTRUCTOR_REFERENCE:
			stringBuilder.append("CONSTRUCTOR_REFERENCE");
			break;
		case TypeReference.METHOD_REFERENCE:
			stringBuilder.append("METHOD_REFERENCE");
			break;
		case TypeReference.CAST:
			stringBuilder.append("CAST ").append(typeReference.getTypeArgumentIndex());
			break;
		case TypeReference.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
			stringBuilder.append("CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT ").append(typeReference.getTypeArgumentIndex());
			break;
		case TypeReference.METHOD_INVOCATION_TYPE_ARGUMENT:
			stringBuilder.append("METHOD_INVOCATION_TYPE_ARGUMENT ").append(typeReference.getTypeArgumentIndex());
			break;
		case TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
			stringBuilder.append("CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT ").append(typeReference.getTypeArgumentIndex());
			break;
		case TypeReference.METHOD_REFERENCE_TYPE_ARGUMENT:
			stringBuilder.append("METHOD_REFERENCE_TYPE_ARGUMENT ").append(typeReference.getTypeArgumentIndex());
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Appends the given stack map frame types to {@link #stringBuilder}.
	 *
	 * @param numTypes   the number of stack map frame types in 'frameTypes'.
	 * @param frameTypes an array of stack map frame types, in the format described
	 *                   in {@link org.objectweb.asm.MethodVisitor#visitFrame}.
	 */
	private void appendFrameTypes(final int numTypes, final Object[] frameTypes) {
		for (int i = 0; i < numTypes; ++i) {
			if (i > 0) {
				stringBuilder.append(' ');
			}
			if (frameTypes[i] instanceof String) {
				String descriptor = (String) frameTypes[i];
				if (descriptor.charAt(0) == '[') {
					appendDescriptor(FIELD_DESCRIPTOR, descriptor);
				} else {
					appendDescriptor(INTERNAL_NAME, descriptor);
				}
			} else if (frameTypes[i] instanceof Integer) {
				stringBuilder.append(FRAME_TYPES.get(((Integer) frameTypes[i]).intValue()));
			} else {
				stringBuilder.append(label((Label) frameTypes[i]));
			}
		}
	}

	/**
	 * Creates and adds to {@link #text} a new {@link Htmlifier}, followed by the
	 * given string.
	 *
	 * @param endText the text to add to {@link #text} after the textifier. May be
	 *                {@literal null}.
	 * @return the newly created {@link Htmlifier}.
	 */
	private Htmlifier addNewHtmlifier(final String endText) {
		Htmlifier textifier = createHtmlifier();
		text.add(textifier.getText());
		if (endText != null) {
			text.add(endText);
		}
		return textifier;
	}

	/**
	 * Creates a new {@link Htmlifier}.
	 *
	 * @return a new {@link Htmlifier}.
	 */
	protected Htmlifier createHtmlifier() {
		return new Htmlifier(api, null);
	}

	static class Var {
		int slot;
		String name;
		String desc;

		public Var(int var) {
			slot = var;
		}

		public String toString() {
			if (name != null)
				return String.format("<var index=%d descriptor=\"%s\">%s</var>", slot, desc, name);
			else
				return String.format("<var index=%d>%d</var>", slot, slot);
		}
	}

	class Instruction {
		public int offset;
		int opcode;
		Label label;
		int line = -1;
		List<Object> args = new ArrayList<>();

		public String toString() {
			String lbl = "";
			if (label != null) {
				lbl = label(label);
			}
			StringBuilder sb = new StringBuilder();
			sb.append("<instruction offset=").append(offset);
			if (line >= 0) {
				sb.append(" lineNum=\"").append(line).append("\"");
				sb.append(" class=\"line-");
				if (line % 2 == 0) {
					sb.append("even\"");
				} else {
					sb.append("odd\"");
				}
			}
			if (lbl != "")
				sb.append(" label=\"").append(lbl).append("\"");
			sb.append(">");
			if (label != null) {
				sb.append("<linenum>").append(line).append("</linenum>");
				sb.append("<label>").append(lbl).append("</label>");
			} else {
				sb.append("<nolabel></nolabel><nolabel></nolabel>");
			}
			sb.append("<mnemonic opcode=\"").append(opcode).append("\">").append(OPCODES[opcode].toLowerCase())
					.append("</mnemonic>");
			sb.append("<args>");
			for (Object a : args) {
				if (a instanceof Var)
					sb.append(a);
				else
					sb.append(StringEscapeUtils.escapeHtml4(a.toString()));
				sb.append(" ");
			}
			sb.append("</args>");
			sb.append("</instruction>");
			return sb.toString();
		}
	}
}
