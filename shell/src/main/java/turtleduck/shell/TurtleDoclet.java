package turtleduck.shell;

import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import turtleduck.util.Array;
import turtleduck.util.Dict;

public class TurtleDoclet implements Doclet {

	private Reporter reporter;
	private Locale locale;
	private DocTrees docTrees;
	private Types types;

	@Override
	public void init(Locale locale, Reporter reporter) {
		this.locale = locale;
		this.reporter = reporter;
	}

	@Override
	public String getName() {
		return "TurtleDoclet";
	}

	@Override
	public Set<? extends Option> getSupportedOptions() {
		Set<? extends Option> options = new HashSet<>();
		return options;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_13;
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		types = environment.getTypeUtils();
		docTrees = environment.getDocTrees();
		environment.getSpecifiedElements().forEach(e -> {
			e.accept(new TDElementVisitor(), "");
			reporter.print(Diagnostic.Kind.NOTE, e, e.getKind().toString());
		});
		return true;
	}

	class TDElementVisitor implements ElementVisitor<Dict, String> {

		@Override
		public Dict visit(Element e, String scope) {
			reporter.print(Diagnostic.Kind.NOTE, e, "visit: " + e.getKind().toString());
			return null;
		}

		@Override
		public Dict visitPackage(PackageElement e, String scope) {
			reporter.print(Diagnostic.Kind.NOTE, e, "visitPackage: " + e.getKind().toString());
			return null;
		}

		@Override
		public Dict visitType(TypeElement e, String scope) {
			reporter.print(Diagnostic.Kind.NOTE, e, "visitType: " + e.getKind().toString());
			e.getEnclosedElements().forEach(elt -> elt.accept(this, e.getQualifiedName().toString()));
			e.getInterfaces().forEach(iface -> {
				TypeElement ifaceElt = (TypeElement) types.asElement(iface);
				ifaceElt.getEnclosedElements().forEach(elt -> {
					elt.accept(this, ifaceElt.getQualifiedName().toString());
				});
			});
			return null;
		}

		@Override
		public Dict visitVariable(VariableElement e, String scope) {
			reporter.print(Diagnostic.Kind.NOTE, e, "visitVariable: " + e.getKind().toString());
			Dict dict = Dict.create();
			dict.put("kind", e.getKind().toString());
			dict.put("name", e.getSimpleName());
			dict.put("info", e.toString());
			return dict;
		}

		@Override
		public Dict visitExecutable(ExecutableElement e, String scope) {
			Dict dict = Dict.create();
			ExecutableType asType = (ExecutableType) e.asType();
			System.out.printf("%s.%s(%s)%s%n", scope, e.getSimpleName(), asType.getParameterTypes(), asType.getReturnType());
			Set<Modifier> modifiers = e.getModifiers();
			List<? extends VariableElement> parameters = e.getParameters();
			TypeMirror receiverType = e.getReceiverType();
			TypeMirror returnType = e.getReturnType();
			List<? extends TypeMirror> thrownTypes = e.getThrownTypes();
			List<? extends TypeParameterElement> typeParameters = e.getTypeParameters();
			ElementKind kind = e.getKind();
			Array arr = Array.create();
			for (VariableElement elt : parameters) {
				arr.add(elt.accept(this, scope));
			}
			dict.put("params", arr);
			arr = Array.create();
			for (TypeParameterElement elt : typeParameters) {
				arr.add(elt.accept(this, scope));
			}
			dict.put("typeParams", arr);
			dict.put("kind", kind.toString());
			dict.put("receiver", link(receiverType));
			dict.put("return", link(returnType));
			dict.put("name", scope + "." + e.getSimpleName());
			reporter.print(Diagnostic.Kind.NOTE, e, "visitExecutable: " + dict.toJson());
			DocCommentTree docCommentTree = docTrees.getDocCommentTree(e);
			if (docCommentTree != null)
				reporter.print(Diagnostic.Kind.NOTE, e, docCommentTree.toString());
			return null;
		}

		@Override
		public Dict visitTypeParameter(TypeParameterElement e, String scope) {
			Dict dict = Dict.create();
			reporter.print(Diagnostic.Kind.NOTE, e, "visitTypeParameter: " + e.getKind().toString());
			dict.put("kind", e.getKind().toString());
			dict.put("name", e.getSimpleName());
			dict.put("info", e.toString());
			return dict;
		}

		@Override
		public Dict visitUnknown(Element e, String scope) {
			reporter.print(Diagnostic.Kind.NOTE, e, "visitUnknown: " + e.getKind().toString());
			return null;
		}

	}

	public Object link(TypeMirror receiverType) {
		// TODO Auto-generated method stub
		return null;
	}

}
