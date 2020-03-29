package turtleduck.text;

import turtleduck.util.Strings;

public interface GraphemeConsumer {
	public static final int FLAG_START = 0x01;
	public static final int FLAG_END = 0x02;
	public static final int FLAG_NON_EMPTY = 0x04;
	public static final int FLAG_CONTROL_SEQUENCE = 0x08;
	public static final int FLAG_KEY = 0x08;
	
	public static final int MASK_SINGLE = FLAG_START | FLAG_END | FLAG_NON_EMPTY;
	
	void accept(String s, int flags);


	public static class GraphemeConsumerPrinter implements GraphemeConsumer {

		@Override
		public void accept(String s, int flags) {
			System.out.printf("%s<%s>%s%n", ((flags & FLAG_START) != 0) ? "" : "…", Strings.escape(s), ((flags & FLAG_END) != 0) ? "" : "…");
		}
	}
}