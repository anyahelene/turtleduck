package turtleduck.gl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class FileWatcher {
    static final FileSystem fileSys = FileSystems.getDefault();
    static WatchService watcher;
    static Map<Path, WatchedDirectory> paths = new HashMap<>();
    static Map<WatchKey, WatchedDirectory> keys = new IdentityHashMap<>();
    static {
        try {
            watcher = fileSys.newWatchService();
        } catch (UnsupportedOperationException | IOException e) {
            watcher = null;
        }
    }

    public static void watchFile(Path path, Runnable handler) {
        if (!Files.isDirectory(path)) {
            Path name = path.getFileName();
            path = path.getParent();
            WatchedDirectory wdir = watchDirectory(path);
            if (wdir != null) {
                System.err.println("Watching " + path + ":" + name);
                wdir.handlers.put(name, handler);
            }
        }
    }

    static WatchedDirectory watchDirectory(Path path) {
        try {
            if (path.getFileSystem() != fileSys || !Files.isDirectory(path))
                return null;
            if (paths.containsKey(path))
                return paths.get(path);
            WatchKey key = path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);
            System.err.println("Watching " + key.watchable());
            WatchedDirectory wdir = new WatchedDirectory();
            wdir.key = key;
            paths.put(path, wdir);
            keys.put(key, wdir);
            return wdir;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void poll() {
        WatchKey key;
        while ((key = watcher.poll()) != null) {
            WatchedDirectory wdir = keys.get(key);
            if (wdir != null) {
                List<WatchEvent<?>> events = key.pollEvents();
                for (WatchEvent<?> e : events) {
                    System.out.println("Change event: " + e.kind() + " on " + e.context() + " in " + key.watchable());
                    Runnable runnable = wdir.handlers.get(e.context());
                    if (runnable != null) {
                        System.out.println("file changed: " + e.context() + ", " + e);
                        runnable.run();
                    }
                }
                key.reset();
            }
        }
    }

    static class WatchedDirectory {
        WatchKey key;
        Map<Path, Runnable> handlers = new HashMap<>();
    }
}
