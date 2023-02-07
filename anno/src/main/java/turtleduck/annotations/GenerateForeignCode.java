package turtleduck.annotations;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class GenerateForeignCode implements ElementVisitor<Void, Integer> {
    private static final EnumSet<ElementKind> INTERESTING_KINDS = EnumSet.of(ElementKind.INTERFACE, ElementKind.CLASS,
            ElementKind.ENUM);
    private static Map<String, String> typeMappings = new HashMap<>();
    private Set<? extends TypeElement> annotations;
    private RoundEnvironment roundEnv;
    private Set<Element> done = new HashSet<>();
    private Map<String, StringBuilder> outputs = new HashMap<>();
    private ProcessingEnvironment processingEnv;
    private StringBuilder builder;
    private Set<String> imports;
    private Types typeUtils;
    private String last = "";
    private boolean isInInterface = false;
    static {
        typeMappings.put("java.lang.String", "string");
        List.of("int", "short", "byte", "long", "float", "double").forEach(t -> typeMappings.put(t, "number"));
        List.of("Integer", "Short", "Byte", "Long", "Float", "Double")
                .forEach(t -> typeMappings.put("java.lang." + t, "number"));
        typeMappings.put("java.util.List", "<>[]");
        typeMappings.put("java.util.ArrayList", "<>[]");
        typeMappings.put("java.util.LinkedList", "<>[]");
        typeMappings.put("java.util.function.Consumer", "(() => void)");
        typeMappings.put("java.util.function.BiConsumer", "(() => void)");
        typeMappings.put("java.util.function.Function", "(() => <>)");
        typeMappings.put("java.util.function.BiFunction", "(() => <>)");
        typeMappings.put("java.util.function.Predicate", "(() => boolean)");
        typeMappings.put("java.util.function.BiPredicate", "(() => boolean)");

    }

    public GenerateForeignCode(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv,
            ProcessingEnvironment processingEnv) {
        this.annotations = annotations;
        this.roundEnv = roundEnv;
        this.processingEnv = processingEnv;
        this.typeUtils = processingEnv.getTypeUtils();
    }

    public boolean process() {
        for (Element e : roundEnv.getRootElements()) {
            System.out.println("Processing: " + e);
            imports = new HashSet<>();
            builder = getOutputFor(e);
            last = "";
            process(e, 0);
        }
        outputs.forEach((name, sb) -> {
            FileObject resource;
            try {
                resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", name + ".ts");
                try (OutputStream os = resource.openOutputStream()) {
                    os.write(sb.toString().getBytes(UTF_8));
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error writing file: " + name + ".ts", ex);
            }
        });
        return true;
    }

    public void process(Element e, int indent) {
        if (!done.add(e))
            return;
        if (INTERESTING_KINDS.contains(e.getKind()) && !processingEnv.getElementUtils().isDeprecated(e)) {
            try {
                visitType((TypeElement) e, indent);
            } catch (Exception ex) {
                throw new ProcessingException("Generator failed: " + ex, e);
            }
        }

    }

    private void append(String... args) {
        for (String arg : args) {
            if (arg.length() > 0 && "()};,\n ".indexOf(arg.charAt(0)) == -1 && !last.isEmpty() && !last.endsWith(" ")) {
                // System.out.print(" ");
                builder.append(" ");
            }
            // System.out.print(arg);
            builder.append(arg);
            last = arg;
        }
    }

    @Override
    public Void visit(Element e, Integer indent) {
        //System.out.println("visit:" + e);
        return null;
    }

    @Override
    public Void visitPackage(PackageElement e, Integer indent) {
        // ignore
        //append("visitPackage:" + e);
        return null;
    }

    @Override
    public Void visitType(TypeElement e, Integer indent) {
        append(javadocOf(e, indent), declarator(e), nameOf(e));
        boolean oldIsInInterface = isInInterface;
        isInInterface = e.getKind() == ElementKind.INTERFACE;

        List<? extends TypeParameterElement> typeParameters = e.getTypeParameters();
        if(!typeParameters.isEmpty()) {
            append("<");
            boolean first = true;
            for(TypeParameterElement elt : typeParameters) {
                if(first)
                    first = false;
                else
                    append(", ");
                visitTypeParameter(elt, 0);
            }
            append(">");
        }
        TypeMirror superclass = e.getSuperclass();
        if(superclass.getKind() != TypeKind.NONE) {
            append("extends", typeOf(superclass));
        }
        List<? extends TypeMirror> interfaces = e.getInterfaces();
        if(!interfaces.isEmpty()) {
            append(isInInterface ? "extends" : "implements", interfaces.stream().map(t -> typeOf(t)).collect(Collectors.joining(", ")));
        }
        append("{", newline(indent + 1));
        for (Element enclosed : e.getEnclosedElements()) {
            if (!processingEnv.getElementUtils().isDeprecated(e))
                enclosed.accept(this, indent + 1);
        }
        isInInterface = oldIsInInterface;
        append(newline(indent), "}", newline(indent));
        return null;
    }

    private String declarator(Element e) {
        List<Modifier> keep = new ArrayList<>(keepModifiers(e.getModifiers(), e.getKind()));
        if (isInInterface) {
            keep.removeAll(List.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.ABSTRACT));
        }
        keep.retainAll(e.getModifiers());
        List<Modifier> dropped = e.getModifiers().stream().filter(m -> !keep.contains(m)).collect(Collectors.toList());
        //if (!dropped.isEmpty())
         //   System.out.println("Dropping modifiers for " + e.getKind());
        String mods = keep.stream().map(m -> m.toString()).collect(Collectors.joining(" "));
        String keyword = e instanceof TypeElement ? e.getKind().toString().toLowerCase() : "";
        return String.join(" ", mods, keyword);
    }

    private List<Modifier> keepModifiers(Set<Modifier> modifiers, ElementKind kind) {

        switch (kind) {
            case ENUM_CONSTANT:
                return List.of();
            case EXCEPTION_PARAMETER:
            case PARAMETER:
            case FIELD:
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
//            case BINDING_VARIABLE:
//            case RECORD_COMPONENT:
                return List.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            case CLASS:
                return List.of(Modifier.STATIC,
                        Modifier.ABSTRACT);
            case ANNOTATION_TYPE:
            case ENUM:
            case INTERFACE:
                return List.of();// Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE);
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case METHOD:
            case PACKAGE:
            case MODULE:
            case OTHER:
//            case RECORD:
            case STATIC_INIT:
            case TYPE_PARAMETER:
                return List.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE, Modifier.STATIC,
                        Modifier.ABSTRACT);
            default:
                return List.of();
        }
    }

    private String newline(Integer indent) {
        return "\n" + " ".repeat(indent * 4);
    }

    private String modifiersOf(Element e) {
        return e.getModifiers().stream().map(m -> m.toString()).collect(Collectors.joining(" "));
    }

    @Override
    public Void visitVariable(VariableElement e, Integer indent) {
        if (indent > 0)
            append(javadocOf(e, indent), declarator(e), nameOf(e), ":", typeOf(e), ";", newline(indent));
        else
            append(javadocOf(e, indent), declarator(e), nameOf(e), ":", typeOf(e));
        // System.out.println("visitVariable:" + e);
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Integer indent) {
        append(javadocOf(e, indent), declarator(e), nameOf(e), "(");
        boolean first = true;
        for (VariableElement parameter : e.getParameters()) {
            if (!first)
                append(",");
            else
                first = false;
            visitVariable(parameter, 0);
        }
        append(") :", typeOf(e), ";", newline(indent));
       // System.out.println("visitExecutable:" + e);
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Integer indent) {
        append(e.toString());
        return null;
    }

    @Override
    public Void visitUnknown(Element e, Integer indent) {
        append("visitUnknown:" + e);
        return null;
    }

    private StringBuilder getOutputFor(Element element) {
        Element e = element;
        while (!(e instanceof PackageElement || e.getEnclosingElement() == null)) {
            e = e.getEnclosingElement();
        }
        if (e instanceof QualifiedNameable) {
            String baseName = ((QualifiedNameable) e).getQualifiedName().toString();
            StringBuilder builder = outputs.computeIfAbsent(baseName, key -> new StringBuilder());
            return builder;
        }
        throw new ProcessingException("Can't find qualified name for: " + element.getSimpleName(),
                element);
    }

    private String javadocOf(Element elt, int indent) {
        String ind = newline(indent + 1);
        String docComment = processingEnv.getElementUtils().getDocComment(elt);
        if (docComment != null) {
            docComment = docComment.stripTrailing().stripIndent().replaceAll("\n", ind);
            return newline(indent) + "/**" + ind + docComment + newline(indent) + "*/" + newline(indent);
        } else
            return "";
    }

    private String nameOf(Element elt) {
        return elt.getSimpleName().toString();
    }

    private String typeOf(TypeMirror type) {

        return type.accept(new TypeVisitor<String, Void>() {

            @Override
            public String visit(TypeMirror t, Void p) {
                return "visit()";
            }

            @Override
            public String visitPrimitive(PrimitiveType t, Void p) {
                String name = t.toString();
                return typeMappings.getOrDefault(name, name);
            }

            @Override
            public String visitNull(NullType t, Void p) {
                return "void";
            }

            @Override
            public String visitArray(ArrayType t, Void p) {
                return typeOf(t.getComponentType()) + "[]";
            }

            @Override
            public String visitDeclared(DeclaredType t, Void p) {
                String qName = typeUtils.erasure(t).toString();
                List<String> args = t.getTypeArguments().stream().map(arg -> typeOf(arg)).collect(Collectors.toList());
                String mapping = typeMappings.get(qName);
                if (mapping == null) {
                    imports.add(qName);
                    String name = qName.replaceAll("^.*\\.", "");
                    if (!args.isEmpty()) {
                        return name + "<" + String.join(",", args) + ">";
                    } else {
                        return name;
                    }
                } else if (mapping.contains("()")) {
                    if (mapping.contains("<>") && args.size() > 0) {
                        String ret = args.remove(args.size() - 1);
                        mapping = mapping.replace("<>", ret);
                    }
                    List<String> names = "abcdefghijklmnopqrstuvwxyz".codePoints()
                            .mapToObj(cp -> Character.toString(cp)).collect(Collectors.toList());
                    return mapping.replace("()",
                            "(" + args.stream().map(a -> names.remove(0) + " : " + a).collect(Collectors.joining(", "))
                                    + ")");
                } else {
                    return mapping.replace("<>", String.join(",", args));
                }
            }

            @Override
            public String visitError(ErrorType t, Void p) {
                return "ERROR";
            }

            @Override
            public String visitTypeVariable(TypeVariable t, Void p) {
                return t.toString();
            }

            @Override
            public String visitWildcard(WildcardType t, Void p) {
                return t.toString();
            }

            @Override
            public String visitExecutable(ExecutableType t, Void p) {
                String params = t.getParameterTypes().stream().map(alt -> typeOf(alt)).collect(Collectors.joining(","));
                return "(" + params + ") => " + typeOf(t.getReturnType());
            }

            @Override
            public String visitNoType(NoType t, Void p) {
                return "void";
            }

            @Override
            public String visitUnknown(TypeMirror t, Void p) {
                return t.toString();
            }

            @Override
            public String visitUnion(UnionType t, Void p) {
                return t.getAlternatives().stream().map(alt -> typeOf(alt)).collect(Collectors.joining(" | "));
            }

            @Override
            public String visitIntersection(IntersectionType t, Void p) {
                return t.getBounds().stream().map(alt -> typeOf(alt)).collect(Collectors.joining(" & "));
            }
        }, null);

    }

    private String typeOf(Element elt) {
        if (elt instanceof VariableElement) {
            return typeOf(((VariableElement) elt).asType());
        } else if (elt instanceof ExecutableElement) {
            return typeOf(((ExecutableElement) elt).getReturnType());
        } else
            return "";
    }

    class ProcessingException extends RuntimeException {
        private Element element;

        public ProcessingException(String msg, Element elt) {
            this.element = elt;
        }

        public Element element() {
            return element;
        }

        public void printMesssage() {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, getMessage(),
                    element);
        }
    }
}
