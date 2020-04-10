package turtleduck.shell;

public class SnippetNS {
	private final String prefix;
	private int next = 0;
	
	public SnippetNS(String prefix) {
		super();
		this.prefix = prefix;
	}

	public String nextId() {
		return prefix + next++;
	}
}
