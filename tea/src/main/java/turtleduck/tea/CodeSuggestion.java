package turtleduck.tea;


import turtleduck.text.TextCursor;

public class CodeSuggestion implements Comparable<CodeSuggestion> {

	private String input, expanded, insert;
	private int anchor;
	private String cont;
	private boolean tmatch;
	private String signature;

	public CodeSuggestion(int anchor, String input, String continuation, String signature, boolean matchesType) {
		this.input = input;
		this.anchor = anchor;
		this.cont = continuation;
		this.signature = signature != null ? signature : continuation;
		this.tmatch = matchesType;

	}

	@Override
	public int compareTo(CodeSuggestion o) {
		int i = cont.compareTo(o.cont);
		if (i != 0)
			return i;
		i = -Boolean.compare(tmatch, o.tmatch);
//		if (i != 0)
		return i;
		/*
		 * String sig1 = signature(), sig2 = o.signature(); if (sig1 != null && sig2 ==
		 * null) return -1; else if (sig1 == null && sig2 != null) return 1; else if
		 * (sig1 == null && sig2 == null) return 0; else return sig1.compareTo(sig2);
		 */
	}

	public String continuation() {
		return cont;
	}

	public void printContinuation(TextCursor printer) {
		int replace = input.length() - anchor;
		printer.print(cont.substring(replace));
	}
	public String expansion() {
		if (expanded == null)
			expanded = input.substring(0, anchor) + cont;
		return expanded;
	}


	public String signature() {
		return signature;
	}

	public boolean matchesType() {
		return tmatch;
	}

}
