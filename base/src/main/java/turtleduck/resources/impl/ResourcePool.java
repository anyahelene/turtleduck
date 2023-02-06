package turtleduck.resources.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import turtleduck.resources.Disposable;
import turtleduck.resources.ResourceKind;
import turtleduck.resources.ResourceHandle;

public class ResourcePool {
    private Map<ResourceKind, List<GarbageCollectibleResource<?>>> resources = new HashMap<>();
    private Map<ResourceKind, Integer> usage = new HashMap<>();

    <T extends Disposable> ResourceHandle<T> createHandle(ResourceKind kind, T data) {
        ResourceHandle<T> h = new ResourceHandleImpl<>(data);
        GarbageCollectibleResource<T> gcr = new GarbageCollectibleResource<>(data, h);
        List<GarbageCollectibleResource<?>> list = resources.get(kind);
        if (list == null) {
            list = new ArrayList<>();
            resources.put(kind, list);
        }
        int u = usage.getOrDefault(kind, 0);
        usage.put(kind, u + data.size());
        list.add(gcr);
        return h;

    }

    public synchronized void gc() {
        resources.forEach((k, list) -> {
            int[] u = { usage.getOrDefault(k, 0) };
            list.removeIf(gcr -> {
                int size = gcr.size();
                if (gcr.tryDispose()) {
                    u[0] -= size;
                    return true;
                }
                return false;
            });
            usage.put(k, u[0]);
        });
    }

    public synchronized void disposeAll() {
        resources.forEach((k, list) -> {
            list.forEach(gcr -> {
                gcr.forceDispose();
            });
        });
        usage.clear();
        resources.clear();
    }

    record ResourceHandleImpl<T> (T data) implements ResourceHandle<T> {

    }

    public String toString() {
        return "ResourcePool(" + usage.toString() + ")";
    }
    
    static class GarbageCollectibleResource<T extends Disposable> {
        protected WeakReference<ResourceHandle<T>> handle;
        protected T data;

        protected GarbageCollectibleResource(T data, ResourceHandle<T> handle) {
            this.data = data;
            this.handle = new WeakReference<ResourceHandle<T>>(handle);
        }

        public T get() {
            if (handle.get() == null)
                throw new IllegalStateException("Handle expired");
            return data;
        }

        public int size() {
            if (data != null)
                return data.size();
            else
                return 0;
        }

        /**
         * @return True if the object is in use (there is a reference to its handle)
         */
        public boolean isInUse() {
            return handle.get() != null && data != null;
        }

        /**
         * Dispose of the object if there are no more references to it.
         * 
         * @return True if object was disposed of (now or previously)
         */
        public synchronized boolean tryDispose() {
            var h = handle.get();
            if (h == null) {
                var d = data;
                data = null;
                d.dispose();
            }
            return data == null;
        }

        /**
         * Forcibly dispose of the object.
         * 
         */
        public synchronized void forceDispose() {
            handle.clear();
            if (data != null) {
                data.dispose();
                data = null;
            }
        }

    }


}
