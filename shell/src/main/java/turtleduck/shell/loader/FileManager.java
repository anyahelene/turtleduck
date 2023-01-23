package turtleduck.shell.loader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

@Deprecated
public class FileManager implements StandardJavaFileManager {
    public static boolean debug = false;
    private final StandardJavaFileManager delegate;
    private boolean moduleSourcePath = false;

    public FileManager(StandardJavaFileManager delegate) {
        if (debug)
            System.err.println("FileManager.<init>: " + delegate);
        this.delegate = delegate;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        var result = delegate.getClassLoader(location);
        if (debug)
            System.err.println("FileManager.getClassLoader: " + location + " → " + result);
        return result;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
            throws IOException {
        var result = delegate.list(location, packageName, kinds, recurse);
//		System.err
//				.println("FileManager.list: " + Arrays.asList(location, packageName, kinds, recurse) + " → " + result);
        return result;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        var result = delegate.inferBinaryName(location, file);
        if (false && debug)
            System.err.println("FileManager.inferBinaryName: " + Arrays.asList(location, file) + " → " + result);
        return result;
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        try {
            var result = delegate.handleOption(current, remaining);
            if (debug)
                System.err.println("FileManager.handleOption: " + Arrays.asList(current, remaining) + " → " + result);
            return result;
        } catch (IllegalArgumentException e) {
            if (debug)
                System.err.println("FileManager.handleOption: " + Arrays.asList(current, remaining) + " → " + e);
            if (current.contains("source-path")) {
                // System.out.println(remaining.next());
                return false;
            }
            return false;
        }
    }

    @Override
    public boolean hasLocation(Location location) {
        var result = delegate.hasLocation(location);
        if (false && debug)
            System.err.println("FileManager.hasLocation: " + Arrays.asList(location) + " → " + result);
        return result;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        var result = delegate.getJavaFileForInput(location, className, kind);
        if (debug && kind == Kind.SOURCE)
            System.err.println(
                    "FileManager.getJavaFileForInput: " + Arrays.asList(location, className, kind) + " → " + result);
        return result;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
            throws IOException {
        var result = delegate.getJavaFileForOutput(location, className, kind, sibling);
        if (debug && kind == Kind.SOURCE)
            System.err.println("FileManager.getJavaFileForOutput: " + Arrays.asList(location, className, kind, sibling)
                    + " → " + result);
        return result;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        var result = delegate.getFileForInput(location, packageName, relativeName);
        if (debug)
            System.err.println(
                    "FileManager.getFileForInput: " + Arrays.asList(location, packageName, relativeName) + " → "
                            + result);
        return result;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
            throws IOException {
        var result = delegate.getFileForOutput(location, packageName, relativeName, sibling);
        if (debug)
            System.err.println(
                    "FileManager.getFileForInput: " + Arrays.asList(location, packageName, sibling) + " → " + result);
        return result;
    }

    @Override
    public void flush() throws IOException {
        if (debug)
            System.err.println("FileManager.flush");
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        if (debug)
            System.err.println("FileManager.flush");
        delegate.close();
    }

    @Override
    public int isSupportedOption(String option) {
        var result = delegate.isSupportedOption(option);
        if (debug)
            System.err.println("FileManager.isSupportedOption: " + option + " → " + result);
        return result;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        var result = delegate.isSameFile(a, b);
        if (debug)
            System.err.println("FileManager.isSameFile: " + Arrays.asList(a, b) + " → " + result);
        return result;
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        var result = delegate.getJavaFileObjectsFromFiles(files);
        if (debug)
            System.err.println("FileManager.getJavaFileObjectsFromFiles: " + Arrays.asList(files) + " → " + result);
        return result;
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        var result = delegate.getJavaFileObjects(files);
        if (debug)
            System.err.println("FileManager.getJavaFileObjects: " + Arrays.asList(files) + " → " + result);
        return result;
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        var result = delegate.getJavaFileObjectsFromStrings(names);
        if (debug)
            System.err.println("FileManager.getJavaFileObjectsFromStrings: " + Arrays.asList(names) + " → " + result);
        return result;
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        var result = delegate.getJavaFileObjects(names);
        if (debug)
            System.err.println("FileManager.getJavaFileObjects: " + Arrays.asList(names) + " → " + result);
        return result;
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
        if (debug)
            System.err.println("FileManager.setLocation: " + Arrays.asList(location, files));
        delegate.setLocation(location, files);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        var result = delegate.getLocation(location);
        if (debug)
            System.err.println("FileManager.getLocation: " + Arrays.asList(location) + " → " + result);
        return result;
    }

    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        var result = delegate.getLocationForModule(location, moduleName);
        if (false && debug)
            System.err.println(
                    "FileManager.getLocationForModule: " + Arrays.asList(location, moduleName) + " → " + result);
        return result;
    }

    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        var result = delegate.getLocationForModule(location, fo);
        if (debug)
            System.err.println("FileManager.getLocationForModule: " + Arrays.asList(location, fo) + " → " + result);
        return result;
    }

    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws IOException {
        var result = delegate.getServiceLoader(location, service);
        System.err.println("FileManager.getServiceLoader: " + Arrays.asList(location, service) + " → " + result);
        return result;
    }

    public String inferModuleName(Location location) throws IOException {
        var result = delegate.inferModuleName(location);
        if (false && debug)
            System.err.println("FileManager.inferModuleName: " + Arrays.asList(location) + " → " + result);
        return result;
    }

    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        var result = delegate.listLocationsForModules(location);
        if (debug)
            System.err.println("FileManager.listLocationsForModules: " + Arrays.asList(location) + " → " + result);
        return result;
    }

    public boolean contains(Location location, FileObject fo) throws IOException {
        if (debug)
            System.err.print("FileManager.contains: " + Arrays.asList(location, fo));
        if(location == StandardLocation.SOURCE_PATH || location == StandardLocation.MODULE_SOURCE_PATH)
            return true;
        if(fo.getClass().getSimpleName().contains("SourceMemoryJavaFileObject")) {
            System.err.println(" → false");
            return false;
        }
        var result = delegate.contains(location, fo);
        if (debug)
            System.err.println(" → " + result + ": " + fo.getCharContent(true).subSequence(0, 20));
        return result;
    }

}
