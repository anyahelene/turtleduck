package turtleduck.messaging;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

//import io.vertx.core.Promise;

public interface KernelInfo {
	public static final String MSG_TYPE = "kernel_info_reply";
	Key<String> PROTOCOL_VERSION = Key.strKey("protocol_version");
	Key<String> IMPLEMENTATION = Key.strKey("implementation");
	Key<String> IMPLEMENTATION_VERSION = Key.strKey("implementation_version");
	Key<String> BANNER = Key.strKey("banner", "");
	Key<Dict> LANGUAGE_INFO = Key.dictKey("language_info");
	Key<Array> HELP_LINKS = Key.arrayKey("help_links", () -> Array.of(Dict.class));
	Key<String> NAME = Key.strKey("name", "");
	Key<String> VERSION = Key.strKey("version", "");
	Key<String> MIMETYPE = Key.strKey("mimetype", "");
	Key<String> FILE_EXTENSION = Key.strKey("file_extension", "");
	Key<String> PYGMENTS_LEXER = Key.strKey("pygments_lexer", "");
	Key<Object> CODEMIRROR_MODE = Key.key("codemirror_mode", Object.class, () -> null);
	Key<String> NBCONVERT_EXPORTER = Key.strKey("nbconvert_exporter", "");

}
