package turtleduck.messaging;

import turtleduck.async.Async;
import turtleduck.util.Dict;

public interface MessageMonitor extends Async<Dict> {
	void cancel();
	boolean isSent();
	boolean isCancelled();
	boolean isReplied();
}
