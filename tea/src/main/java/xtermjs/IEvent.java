package xtermjs;

import java.util.function.Consumer;

import org.teavm.jso.JSObject;

/**
 * An event that can be listened to.
 * @returns an `IDisposable` to stop listening.
 */
public interface IEvent<T> extends JSObject {
	IDisposable listener(Consumer<T> callback);
	// (listener: (arg1: T, arg2: U) => any): IDisposable;

}