package turtleduck.annotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedAnnotationTypes({ "turtleduck.annotations.MessageProtocol", "turtleduck.annotations.MessageDispatch" })
@SupportedSourceVersion(SourceVersion.RELEASE_13)
public class ProcessAnnotations extends AbstractProcessor {
	protected static final Logger logger = null;// LoggerFactory.getLogger(GenerateHandlers.class);

	public ProcessAnnotations() {
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Annotations: " + annotations);
		for (TypeElement anno : annotations) {
			System.out.println("Processsing " + anno);
			if (anno.getQualifiedName().toString().equals("turtleduck.annotations.MessageProtocol")) {
				for (Element elt : roundEnv.getElementsAnnotatedWith(anno)) {
					if (elt.getKind() == ElementKind.INTERFACE) {
						TypeElement te = (TypeElement) elt;
						ProtocolProcessor pp = new ProtocolProcessor(te.getAnnotation(MessageProtocol.class), te);
						pp.process();
//						System.out.println(pp.generateProxy());
						Filer filer = processingEnv.getFiler();
						try {
							JavaFileObject jfobj = filer.createSourceFile(pp.fullName);
							try (PrintWriter out = new PrintWriter(jfobj.openWriter())) {
								out.println(pp.generateProxy());
							}
							FileObject fobj = filer.createResource(StandardLocation.SOURCE_OUTPUT, pp.packageName,
									pp.className + ".js");
							try (PrintWriter out = new PrintWriter(fobj.openWriter())) {
								System.out.println(pp.generateJsPyProxy());
								out.println(pp.generateJsPyProxy());
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			} else if (anno.getQualifiedName().toString().equals("turtleduck.annotations.MessageDispatch")) {
				for (Element elt : roundEnv.getElementsAnnotatedWith(anno)) {
					if (elt.getKind() == ElementKind.CLASS) {
						TypeElement te = (TypeElement) elt;
						ProtocolProcessor pp = new ProtocolProcessor(te.getAnnotation(MessageDispatch.class), te);
						pp.process();
//						System.out.println(pp.generateDispatch());
						Filer filer = processingEnv.getFiler();
						JavaFileObject fobj;
						try {
							fobj = filer.createSourceFile(pp.fullName);
							try (PrintWriter out = new PrintWriter(fobj.openWriter())) {
								out.println(pp.generateDispatch());
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}
		return false;
	}

	class ProtocolProcessor {
		final Deque<TypeElement> todo = new ArrayDeque<>();
		final List<TypeElement> interfaces = new ArrayList<>();
		final Map<String, HandlerMethod> handlers = new HashMap<>();
		final String className;
		final TypeElement type;
		final String packageName;
		final String fullName;
		final String origClass;

		ProtocolProcessor(MessageProtocol mp, TypeElement type) {
			String s = mp.value();
			if (s.contains(".")) {
				this.fullName = s;
				this.packageName = fullName.substring(0, fullName.lastIndexOf('.'));
				this.className = fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
			} else {
				this.packageName = "turtleduck.messaging.generated";
				this.className = s;
				this.fullName = packageName + "." + className;
			}
			todo.add(type);
			this.type = type;
			this.origClass = type.getQualifiedName().toString();
		}

		ProtocolProcessor(MessageDispatch pd, TypeElement type) {
			String s = pd.value();
			if (s.contains(".")) {
				this.fullName = s;
				this.packageName = fullName.substring(0, fullName.lastIndexOf('.'));
				this.className = fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
			} else {
				this.packageName = "turtleduck.messaging.generated";
				this.className = s;
				this.fullName = packageName + "." + className;
			}
			type.getInterfaces().forEach(tm -> {
				if (tm.getKind() == TypeKind.DECLARED) {
					TypeElement sup = (TypeElement) ((DeclaredType) tm).asElement();
					todo.add(sup);
				}
			});
			this.type = type;
			this.origClass = type.getQualifiedName().toString();
		}

		void process() {
			Set<Name> seen = new HashSet<>();

			while (!todo.isEmpty()) {
				TypeElement iface = todo.poll();
				seen.add(iface.getQualifiedName());
				if (iface.getKind() != ElementKind.INTERFACE)
					continue;
				boolean interesting = false;
				for (Element elt : iface.getEnclosedElements()) {
					if (elt.getKind() == ElementKind.METHOD) {
						Request t = elt.getAnnotation(Request.class);
						if (t != null) {
							interesting = true;
							HandlerMethod hm = new HandlerMethod(t, iface, (ExecutableElement) elt);
							if (handlers.containsKey(hm.msgType))
								throw new IllegalArgumentException("duplicate message type: " + hm.msgType);
							handlers.put(hm.msgType, hm);
						}
					}
				}
				if (interesting)
					interfaces.add(iface);
				iface.getInterfaces().forEach(tm -> {
					if (tm.getKind() == TypeKind.DECLARED) {
						TypeElement sup = (TypeElement) ((DeclaredType) tm).asElement();
						if (!seen.contains(sup.getQualifiedName())) {
							seen.add(sup.getQualifiedName());
							todo.add(sup);
						} else {
							logger.info("Seen again: {}", sup);
						}
					}
				});
			}
		}

		public String generateProxy() {
			StringBuilder cls = new StringBuilder();
			cls.append("package ").append(packageName).append(";\n\n");
			cls.append("import turtleduck.messaging.Message;\n"//
					+ "import turtleduck.messaging.Router;\n"//
					+ "import turtleduck.messaging.MessageWriter;\n"//
					+ "import turtleduck.util.Dict;\n"//
					+ "import turtleduck.async.Async;\n"//
					+ "\n");
			// cls.append("import turtleduck.util.Key;\n" //
//					+ "import java.util.function.Function;\n"//
//					+ "import turtleduck.async.Async;\n");
			cls.append("public class ").append(className).append(" implements ");
			cls.append(type.getQualifiedName().toString());
//			String comma = "";
//			for (Class<?> iface : interfaces) {
//				cls.append(comma).append(iface.getName().replace('$', '.'));
//				comma = ", ";
//			}
			cls.append(" {\n\n");
			cls.append("\tprotected final String peer;\n");
			cls.append("\tprotected final Router router;\n");
//			cls.append("\tprotected final BiFunction<String, Message, Async<Dict>> transport;\n\n");
			cls.append("\tpublic ").append(className)
					.append("(String peer, Router router) {\n");
			cls.append("\t\tthis.peer = peer;\n");
			cls.append("\t\tthis.router = router;\n");
			cls.append("\t}\n\n");
			for (HandlerMethod h : handlers.values()) {
				cls.append(h.encoder());
			}
			cls.append("}\n");

			return cls.toString();

		}

		public String generateJsPyProxy() {
			StringBuilder cls = new StringBuilder();
			cls.append("import { asyncRun } from './py-worker';\n\n");

			cls.append("class ").append(className).append(" {\n");
			String prefix = type.getSimpleName().toString();
			cls.append("\tconstructor(target = '").append(prefix).append("') {\n");
			cls.append("\t\tthis.target = target;\n");
			cls.append("\t}\n");
			List<String> funs = new ArrayList<>();
			for (HandlerMethod h : handlers.values()) {
				funs.add(h.method.getSimpleName().toString());
				cls.append(h.jspycoder(prefix));
			}
			cls.append("\t}\n\n");

			String objName = className.substring(0, 1).toLowerCase() + className.substring(1);
			cls.append("const ").append(objName).append(" = new ").append(className).append("()\n\n");
			cls.append("export { ").append(objName).append(" };\n");

			return cls.toString();

		}

		public String generateDispatch() {
			StringBuilder cls = new StringBuilder();
			cls.append("package ").append(packageName).append(";\n\n");
			cls.append(""// "import java.util.function.Function;\n"//
					+ "import turtleduck.messaging.Message;\n"//
					+ "import turtleduck.messaging.Dispatch;\n"//
//					+ "import turtleduck.messaging.MessageWriter;\n"//
					+ "import turtleduck.util.Dict;\n"//
					+ "import java.util.List;\n"//
					+ "import java.util.Arrays;\n"//
					+ "import turtleduck.async.Async;\n"//
					+ "\n");
			cls.append("public class ").append(className).append(" implements Dispatch<").append(origClass)
					.append("> {\n\n");
			cls.append("\tprotected final ").append(origClass).append(" handler;\n");
			cls.append("\tprotected final List<String> requestTypes = Arrays.asList(");
			cls.append(handlers.values().stream().map(h -> "\"" + h.msgType + "\"").collect(Collectors.joining(", ")));
			cls.append(");\n");
			cls.append("\tprotected final List<String> replyTypes = Arrays.asList(");
			cls.append(
					handlers.values().stream().map(h -> "\"" + h.replyType + "\"").collect(Collectors.joining(", ")));
			cls.append(");\n\n");
			cls.append("\tpublic ").append(className).append("(").append(origClass).append(" handler) {\n");
			cls.append("\t\tif(handler == null) throw new NullPointerException();\n");
			cls.append("\t\tthis.handler = handler;\n");
			cls.append("\t}\n\n");
			cls.append("\tpublic Async<Message> dispatch(Message msg) {\n");
			cls.append("\t\tAsync<Dict> reply = null;\n");
			cls.append("\t\tString replyType = \"ok_reply\";\n");
			cls.append("\t\ttry {\n");
			cls.append("\t\t\tswitch(msg.msgType()) {\n");
			for (HandlerMethod h : handlers.values()) {
				cls.append(h.decoder());
			}
			cls.append("\t\t\t  default:\n");
			cls.append("\t\t\t\treturn Async.failed(\"No handler for %s\", msg.msgType());\n");
			cls.append("\t\t\t}\n");
			cls.append("\t\t\tif(reply != null) {\n");
			cls.append("\t\t\tString type = replyType;\n");
			cls.append("\t\t\t\treturn reply.map(content -> msg.reply(type).content(content).done()) //\n");
			cls.append("\t\t\t\t\t.mapFailure(content -> msg.reply(\"error_reply\").content(content).done());\n");
			cls.append("\t\t\t} else {\n");
			cls.append("\t\t\t\treturn Async.nothing();\n");
			cls.append("\t\t\t}\n");
			cls.append("\t\t} catch (Throwable t) {\n");
			cls.append("\t\t\treturn Async.succeeded(msg.errorReply(t).done());\n");
			cls.append("\t\t}\n\n");
			cls.append("\t}\n\n");
			cls.append("\tpublic List<String> requestTypes() {\n\t\treturn requestTypes;\n\t}\n\n");
			cls.append("\tpublic List<String> replyTypes() {\n\t\treturn replyTypes;\n\t}\n");
			cls.append("}\n");

			return cls.toString();

		}
	}

	static class Param {
		VariableElement javaParam;
//		Key<?> key;
		int pos;
		boolean optional;
		public String keyRef;
		String decoder;
		String encoder;

		void content() {
			decoder = String.format("msg.content(%s)", keyRef);
			encoder = String.format("\t\tmw.putContent(%s, %s);\n", keyRef, javaParam.getSimpleName());
		}

		void context() {
			decoder = "ctx";
			encoder = "";
		}

		String declaration() {
			return String.format("%s %s", javaParam.asType().toString().replace('$', '.'), javaParam.getSimpleName());
		}
	}

	static class HandlerMethod {
		ExecutableElement method;
		List<Param> params = new ArrayList<>();
		String msgType;
		String contextName = "ctx";
		String replyType;
		String clsName;

		HandlerMethod(Request req, TypeElement cls, ExecutableElement m) {
			this.method = m;
			this.msgType = req.type();
			this.replyType = req.replyType();
			if (msgType.isEmpty())
				msgType = m.getSimpleName() + "_request";
			if (replyType.isEmpty())
				replyType = msgType.replaceAll("_request$", "") + "_reply";
			List<? extends VariableElement> jps = m.getParameters();
			System.out.println(m.getEnclosingElement());
			System.out.println(m.getReceiverType());
			clsName = cls.getQualifiedName().toString().replace('$', '.');
			int i = 0;
			for (VariableElement jp : jps) {
				System.out.println("param: " + jp);
				Param p = new Param();
				params.add(p);
				p.javaParam = jp;
				p.pos = i++;
				MessageField mf = jp.getAnnotation(MessageField.class);
				if (mf != null) {
					String keyRef = mf.value();
					p.keyRef = clsName + "." + keyRef;
					p.content();
					/*
					 * try { Field field = cls.getField(keyRef);
					 * 
					 * if (field != null) { Object object = field.get(null); if (object instanceof
					 * Key<?>) { p.key = (Key<?>) object; p.content(); } else { throw new
					 * IllegalArgumentException(keyRef + " not a key: " + object); } } else { throw
					 * new IllegalArgumentException("Can't find key " + keyRef); } } catch
					 * (NoSuchFieldException | SecurityException | IllegalAccessException e) {
					 * logger.error("Trouble accessing field", e); throw new
					 * IllegalArgumentException("Can't find key " + keyRef, e); }
					 */
//				} else if (jp.getType() == RequestContext.class) {
//					p.context();
//					contextName = jp.getName();
//				} else if (jp.getType() == MessageContext.class) {
//					p.context();
//					contextName = jp.getName();
				} else {
//					p.key = Key.key(jp.getName(), jp.getType());
					p.keyRef = String.format("turtleduck.util.Key.key(\"%s\",%s.class)", jp.getSimpleName(),
							jp.asType().toString().replace('$', '.'));
					p.content();
				}
				if (jp.getAnnotation(Optional.class) != null)
					p.optional = true;
			}
		}

		public String jspycoder(String prefix) {
			String r = method.getReturnType().toString();
			return String.format("\t%s(%s) {\n"//
					+ "\t\tconst args = {%s};\n" + "\t\treturn asyncRun(this.target+'.%s(**js.__args.to_py())', {__args: args});\n" //
					+ "\t}\n\n", //
					method.getSimpleName(), // .substring(method.getSimpleName().lastIndexOf('.') + 1),
					params.stream().map(p -> p.javaParam.getSimpleName().toString()).collect(Collectors.joining(", ")), //
					params.stream()
							.map(p -> String.format("%s: %s", p.javaParam.getSimpleName().toString(),
									p.javaParam.getSimpleName().toString()))
							.collect(Collectors.joining(", ")), //
					method.getSimpleName());
		}

		public String decoder() {
			return String.format(
					"\t\t\t  case \"%s\":\n\t\t\t\treply = handler.%s(%s);\n\t\t\t\treplyType = \"%s\";\n\t\t\t\tbreak;\n", //
					msgType, method.getSimpleName(), //
					params.stream().map(p -> p.decoder).collect(Collectors.joining(", ")), replyType);
		}

		public String encoder() {
			String r = method.getReturnType().toString();
			return String.format("\tpublic %s %s(%s) {\n"//
					+ "\t\tMessageWriter mw = Message.writeTo(peer, \"%s\");\n" //
					+ "%s\n" //
				//	+ "\t\treturn transport.apply(peer, mw.done());\n\t}\n", //
					+ "\t\treturn router.send(mw.done());\n\t}\n", //
					r, //
					method.getSimpleName(), // .substring(method.getSimpleName().lastIndexOf('.') + 1),
					params.stream().map(p -> p.declaration()).collect(Collectors.joining(", ")), msgType, //
					params.stream().map(p -> p.encoder).collect(Collectors.joining()), contextName);
		}
	}
}