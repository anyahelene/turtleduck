package turtleduck.shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.spi.ExecutionControl.ClassBytecodes;
import jdk.jshell.spi.ExecutionControl.ClassInstallException;
import jdk.jshell.spi.ExecutionControl.EngineTerminationException;
import jdk.jshell.spi.ExecutionControl.InternalException;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import turtleduck.annotations.Icon;
import turtleduck.shell.bytecode.Htmlifier;

public class LocalLoaderDelegate implements LoaderDelegate {
	protected final static Logger logger = LoggerFactory.getLogger(LocalLoaderDelegate.class);
	private final Map<String, Class<?>> classes = new HashMap<>();
	private final LocalClassLoader loader;
	private final URL url;

	public LocalLoaderDelegate(ClassLoader loader) {
		try {
			url = new URL(null, "jshell://", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					logger.info("open: " + u);
					return null;
				}
			});
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		this.loader = new LocalClassLoader(url, loader);
	}

	@Override
	public void load(ClassBytecodes[] cbcs)
			throws ClassInstallException, NotImplementedException, EngineTerminationException {
		boolean[] installed = new boolean[cbcs.length];
		try {
			for (ClassBytecodes cbc : cbcs) {
				logger.info("Loading bytecode for " + cbc.name());
				loader.load(cbc);
			}
			int i = 0;
			for (ClassBytecodes cbc : cbcs) {
				Class<?> cls = loader.loadClass(cbc.name());
				classes.put(cbc.name(), cls);
				installed[i++] = true;
				cls.getDeclaredMethods();
			}
		} catch (ClassNotFoundException e) {
			throw new ClassInstallException("failed to load: " + e.getMessage(), installed);
		}
	}

	@Override
	public void classesRedefined(ClassBytecodes[] cbcs) {
		logger.info("Reoading bytecode for " + Arrays.toString(cbcs));
		for (ClassBytecodes cbc : cbcs) {
			logger.info("Reloading bytecode for " + cbc.name());
			loader.load(cbc);
			/*
			 * if (classes.containsKey(cbc.name())) { logger.info("Removing class " +
			 * cbc.name()); }
			 */
		}
	}

	@Override
	public void addToClasspath(String path) throws EngineTerminationException, InternalException {
		logger.info("adding class path: " + path);
		try {
			loader.addToClasspath(url);
		} catch (MalformedURLException e) {
			throw new InternalException(e.getMessage());
		}

	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		logger.info("finding class: " + name);
		Class<?> c = classes.get(name);
		if (c == null) {
			c = Class.forName(name, true, loader);
//			c = loader.loadClass(name);
			if (c != null)
				classes.put(name, c);
			else
				throw new ClassNotFoundException(name);
		}
		return c;
	}

	public byte[] bytecodeOf(String name) {
		return loader.bytecodes.get(name);
	}
	
	public String htmlOf(String name) {
		byte[] bs = loader.bytecodes.get(name);
		if(bs != null) {
			ClassReader cr = new ClassReader(bs);
			Htmlifier htmlifier = new Htmlifier();
			StringWriter writer = new StringWriter();
			cr.accept(new TraceClassVisitor(null, htmlifier, new PrintWriter(writer)), //
					 ClassReader.SKIP_FRAMES);
			return 	writer.toString();
		} else {
			return null;
		}
	}
	
	public String iconOf(String name) {
		return loader.icons.get(name);
	}
	static class LocalClassLoader extends URLClassLoader {
		protected final Map<String, byte[]> bytecodes = new HashMap<>();
		protected final Map<String, String> icons = new HashMap<>();

		public LocalClassLoader(URL url, ClassLoader parent) {
			super(new URL[] { url }, parent);
		}

		public void addToClasspath(URL url) throws MalformedURLException {
			super.addURL(url);
		}

		public void load(ClassBytecodes cbc) {
			byte[] bs = cbc.bytecodes();
			String name = cbc.name();
			logger.info("load: " + name + " (" + bs.length + ")");
//			printByteCode(new ClassReader(bs));
			bytecodes.put(name, bs);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			logger.info("loadClass: " + name);
			Class<?> cls = super.loadClass(name);
			logger.info("         → " + cls);
			Icon iconAnno = cls.getAnnotation(Icon.class);
			if(iconAnno != null) {
				logger.info("        * found icon: " + iconAnno.value());
				icons.put(name.replaceFirst("^.*\\.",  ""), iconAnno.value());
				icons.put(name, iconAnno.value());
				logger.info(""+icons);
			}
			return cls;
		}

	    protected Class<?> findClass(String moduleName, String name) {
			logger.info("loadClass: " + moduleName + "." + name);
	        if (moduleName == null) {
	            try {
	                return findClass(name);
	            } catch (ClassNotFoundException ignore) { }
	        }
	        return null;
	    }
		@Override
		public Package[] getPackages() {
			Package[] packages = super.getPackages();
			logger.info("getPackages: " + Arrays.toString(packages));
			return packages;
		}
		

		@Override
		public URL findResource(String name) {
			logger.info("findResource: " + name);
			URL url = super.findResource(name);
			logger.info("         → " + url);
			return url;
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			logger.info("loader.findClass: " + name);
			byte[] bs = bytecodes.get(name);
			if (bs == null) {
				logger.info("loader.super.findClass: " + name);
				return super.findClass(name);
			}
			
			return defineClass(name, bs, 0, bs.length);
		}
		
		public static void printByteCode(ClassReader classReader) {
			logger.info("Loaded " + classReader.getClassName());
			// Textifier takes care of printing the instructions
			Htmlifier htmlifier = new Htmlifier();
			// TraceClassVisitor visits the class and calls the textifier
			classReader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out)), //
					ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		}
	}
}
