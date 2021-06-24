package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("ShellServiceProxy")
public interface ShellService extends ExecuteService, CodeService {
	/**
	 * Request/(Reply): Code to be executed. In a reply, this is the code that was
	 * actually executed, which may be different (e.g, minor syntax corrections,
	 * such as missing semicolon)
	 */
	Key<String> CODE = CodeService.CODE;
	/**
	 * Request/Reply: The code location
	 */
	Key<String> LOC = Key.strKey("loc");
	/**
	 * Reply:
	 */
	Key<String> VALUE = Key.strKey("value");
	Key<String> PROMPT = TerminalService.PROMPT;
	/**
	 * Request: Additional options to the evaluator
	 */
	Key<Dict> OPTIONS = Key.dictKey("opts", Dict::create);

	/**
	 * Reply: The kind of snippet that was evaluated.
	 * 
	 * One of error, expression, import, method, statement, type or var; with
	 * optional subtypes provided after a dot. (E.g., <code>var.decl.init</code>)
	 */
	Key<String> SNIP_KIND = Key.strKey("snipkind");
	/**
	 * Reply: Identifier for an evaluated snippet; this will match the id sent in
	 * any Explorer updates
	 */
	Key<String> SNIP_ID = Key.strKey("snipid");
	Key<String> SNIP_NS = Key.strKey("snipns");
	Key<String> DOC = Key.strKey("doc");
	/**
	 * Request/Reply: A numeric reference provided by the caller
	 */
	Key<Integer> REF = Key.intKey("ref");
	/**
	 * Reply: True if evaluation involved executing code (i.e., not just declaring
	 * something)
	 */
	Key<Boolean> EXEC = Key.boolKey("exec");
	/**
	 * Reply: True if source code is complete, false if more input is needed
	 */
	Key<Boolean> COMPLETE = Key.boolKey("complete", true);
	/**
	 * Reply: True if evaluation involved defining/declaring something
	 * 
	 * @see #SYMBOL
	 */
	Key<Boolean> DEF = Key.boolKey("def");
	Key<Boolean> PERSISTENT = Key.boolKey("persistent");
	Key<Boolean> ACTIVE = Key.boolKey("active");

	/**
	 * Reply: The symbol that was (re)declared/defined, if any.
	 * 
	 * @see #DEF
	 */
	Key<String> NAME = Key.strKey("name");
	Key<String> SIGNATURE = Key.strKey("signature");
	Key<Array> NAMES = Key.arrayKey("names");
	Key<Array> DOCS = Key.arrayKey("docs");
	/**
	 * Reply: The full name of symbol that was (re)declared/defined, if any.
	 * 
	 * Includes type and parameters
	 * 
	 * @see #DEF
	 */
	Key<String> FULL_NAME = Key.strKey("fullname");
	/**
	 * Reply: The type of the result, if any.
	 * 
	 * @see #VALUE
	 */
	Key<String> TYPE = Key.strKey("type");

	/**
	 * Icon for the result type, if any
	 */
	Key<String> ICON = Key.strKey("icon");
	/**
	 * Reply: An array of multiple eval replies, if the input code was split into
	 * multiple snippets
	 */
	Key<Array> MULTI = Key.arrayKey("multi", () -> Array.of(Dict.class));
	/**
	 * Reply: An array of error/diagnostic messages.
	 * 
	 * Message fields include "msg", "start", "end", "pos"
	 */
	Key<Array> DIAG = Key.arrayKey("diag", () -> Array.of(Dict.class));
	/**
	 * Reply: An exception, if one was thrown.
	 * 
	 * Includes "exception" (exception class name), "message" (the message), "trace"
	 * (array with stack trace), and optional "cause" (another exception)
	 */
	Key<Dict> EXCEPTION = Key.dictKey("exception", null);
	Key<String> TEXT = TerminalService.TEXT;
	Key<Integer> HEAP_USE = Key.intKey("heapUse");
	Key<Integer> HEAP_TOTAL = Key.intKey("heapTotal");
	Key<Integer> HEAP_MAX = Key.intKey("heapMax");
	Key<Double> CPU_TIME = Key.key("cpuTime", Double.class, 0.0);

	@Request(replyType = "evalReply", replyFields = { "REF", "VALUE", "SNIP_KIND", "SNIP_ID", "CODE", "DEF", "SYMBOL",
			"MULTI", "DIAG", "EXCEPTION" })
	Async<Dict> eval(@MessageField("CODE") String code, @MessageField("REF") int ref,
			@MessageField("OPTIONS") Dict opts);

	@Request(type = "refresh", noReply = true)
	Async<Dict> refresh();
}
