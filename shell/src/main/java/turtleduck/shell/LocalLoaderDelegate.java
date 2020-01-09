package turtleduck.shell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.spi.ExecutionControl.ClassBytecodes;
import jdk.jshell.spi.ExecutionControl.ClassInstallException;
import jdk.jshell.spi.ExecutionControl.EngineTerminationException;
import jdk.jshell.spi.ExecutionControl.InternalException;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class LocalLoaderDelegate implements LoaderDelegate {
	private final Map<String, Class<?>> classes = new HashMap<>();
	private final LocalClassLoader loader;
	private final URL url;

	public LocalLoaderDelegate(ClassLoader loader) {
		try {
			url = new URL(null, "jshell://", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					System.out.println("open: " + u);
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
		System.out.println("Loading bytecode for " + Arrays.toString(cbcs));
		boolean[] installed = new boolean[cbcs.length];
		try {
			for (ClassBytecodes cbc : cbcs) {
				System.out.println("Loading bytecode for " + cbc.name());
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
		System.out.println("Reoading bytecode for " + Arrays.toString(cbcs));
		for (ClassBytecodes cbc : cbcs) {
			System.out.println("Reloading bytecode for " + cbc.name());
			loader.load(cbc);
			/*
			 * if (classes.containsKey(cbc.name())) { System.out.println("Removing class " +
			 * cbc.name()); }
			 */
		}
	}

	@Override
	public void addToClasspath(String path) throws EngineTerminationException, InternalException {
		System.out.println("adding class path: " + path);
		try {
			loader.addToClasspath(url);
		} catch (MalformedURLException e) {
			throw new InternalException(e.getMessage());
		}

	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		System.out.println("finding class: " + name);
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

	static class LocalClassLoader extends URLClassLoader {
		private final Map<String, byte[]> bytecodes = new HashMap<>();

		public LocalClassLoader(URL url, ClassLoader parent) {
			super(new URL[] { url }, parent);
		}

		public void addToClasspath(URL url) throws MalformedURLException {
			super.addURL(url);
		}

		public void load(ClassBytecodes cbc) {
			byte[] bs = cbc.bytecodes();
			String name = cbc.name();
			bytecodes.put(name, bs);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			System.out.println("loadClass: " + name);
			Class<?> cls = super.loadClass(name);
			System.out.println("         → " + cls);
			return cls;
		}

	    protected Class<?> findClass(String moduleName, String name) {
			System.out.println("loadClass: " + moduleName + "." + name);
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
			System.out.println("getPackages: " + Arrays.toString(packages));
			return packages;
		}
		

		@Override
		public URL findResource(String name) {
			System.out.println("findResource: " + name);
			URL url = super.findResource(name);
			System.out.println("         → " + url);
			return url;
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			System.out.println("loader.findClass: " + name);
			byte[] bs = bytecodes.get(name);
			if (bs == null) {
				System.out.println("loader.super.findClass: " + name);
				return super.findClass(name);
			}
			
			return defineClass(name, bs, 0, bs.length);
		}
	}
}
