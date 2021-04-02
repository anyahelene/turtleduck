package turtleduck.shell;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author anya
 *
 */
public class Location {
	final String protocol;
	final String host;
	final String path;
	final int start;
	final int length;
	final int pos;
	final URI uri;

	public Location(String protocol, String host, String path, String input) {
		this(protocol, host, path, 0, input.length());
	}

	public Location(String protocol, String host, String path, int start, int length) {
		this(protocol, host, path, start, length, -1);
	}

	public Location(String protocol, String host, String path, int start, int length, int pos) {
		super();
		this.protocol = protocol;
		this.host = host;
		this.path = path;
		this.start = start;
		this.length = length;
		this.pos = (pos < 0 || pos > length) ? -1 : pos;
		String ssp = null;
		if (host != null) {
			if (path != null)
				ssp = host + (path.startsWith("/") ? "" : "/") + path;
			else
				ssp = host;
		} else if (path != null)
			ssp = path;
		String fragment = null;
		if (start >= 0) {
			fragment = String.valueOf(start);
			if (length >= 0)
				fragment += "+" + length;
			if (pos >= 0)
				fragment += "@" + pos;
		}
		try {
			this.uri = new URI(protocol, ssp, fragment);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Location forward(int n) {
		if (n == 0) {
			return this;
		}
		if (start < 0) {
			throw new IllegalArgumentException("Not tracing position");
		}
		if (length >= 0 && n > length) {
			throw new IllegalArgumentException("End of source reached");
		}
		return new Location(protocol, host, path, start + n, length - n, pos - n);
	}

	public Location shorten(int n) {
		if (n == 0) {
			return this;
		}
		if (start < 0) {
			throw new IllegalArgumentException("Not tracing position");
		}
		if (length >= 0 && n > length) {
			throw new IllegalArgumentException("End of source reached");
		}
		return new Location(protocol, host, path, start, length - n, pos);
	}

	public Location pos(int p) {
		if (p > length) {
			throw new IllegalArgumentException("End of source reached");
		}
		return new Location(protocol, host, path, start, length, p);

	}

	/**
	 * Keep n characters at the end of the string.
	 * 
	 * Afterwards, length will be n, and start will be origStart + (origLength - n)
	 * 
	 * @param n
	 * @return
	 */
	public Location keep(int n) {
		if (n == length) {
			return this;
		}
		if (start < 0) {
			throw new IllegalArgumentException("Not tracing position");
		}
		if (length >= 0 && n > length) {
			throw new IllegalArgumentException("End of source reached");
		}
		return new Location(protocol, host, path, start + (length - n), n, pos - (length - n));
	}

	public String substring(String s) {
		if (start < 0) {
			return s;
		} else if (length < 0) {
			return s.substring(start);
		} else {
			return (s + "_").substring(start, start + length);
		}
	}

	public int start() {
		return start;
	}

	public int end() {
		return start + length;
	}

	public int length() {
		return length;
	}

	public Location length(int l) {
		return new Location(protocol, host, path, start, l);
	}

	public String toString() {
		return uri.toString();
	}

	public Location relativeRegion(int regStart, int regLength) {
		if (regStart == 0 && regLength == length) {
			return this;
		}
		if (start < 0) {
			throw new IllegalArgumentException("Not tracing position");
		}
		if (length >= 0 && (regStart + regLength) > length) {
			throw new IllegalArgumentException("End of source reached");
		}
		return new Location(protocol, host, path, start + regStart, regLength, pos - regStart);

	}
}
