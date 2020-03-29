package turtleduck.text;


public class Graphemizer {
	private GraphemeConsumer consumer;
	private StringBuilder lastEmitted = null;
	private StringBuilder buffer = new StringBuilder();
	private int mode = 0;
	private int flags = 0;
	private boolean csiEnabled = true, csiWait = true, csiInput = false;
	private char first = 0;
	private int codePoint = 0;

	public Graphemizer() {
		this.consumer = new GraphemeConsumer.GraphemeConsumerPrinter();
	}

	public Graphemizer(GraphemeConsumer consumer) {
		this.consumer = consumer;
	}

	public Graphemizer csiInputMode(boolean enabled) {
		csiInput = enabled;
		return this;
	}

	public Graphemizer csiWait(boolean enabled) {
		csiWait = enabled;
		return this;
	}

	public Graphemizer csiEnabled(boolean enabled) {
		csiEnabled = enabled;
		return this;
	}

	public synchronized Graphemizer add(String source) {
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);

			if (c >= 0xd800 && c <= 0xdbff) { // high surrogate
				first = c;
				continue;
			} else if (c >= 0xdc00 && c <= 0xdfff) { // low surrogate
				if (first != 0) {
					codePoint = Character.toCodePoint(first, c);
				}
			} else {
				codePoint = c;
			}

			processCodePoint: while (true) {
				if (mode == 0) {
					if (codePoint == '\r') {
						emit();
						mode = codePoint;
						shift(codePoint);
					} else if (csiEnabled && codePoint == 0x1b) { // ESC
						emit();
						mode = codePoint;
						shift(codePoint);
					} else {
						int type = Character.getType(codePoint);
						System.out.printf("Format: %d, %x%n", type, codePoint);
						switch (type) {
						case Character.MODIFIER_SYMBOL:
						case Character.COMBINING_SPACING_MARK:
						case Character.NON_SPACING_MARK:
						case Character.ENCLOSING_MARK:
							shift(codePoint);
							break;
						case Character.CONTROL:
							emit();
							shift(codePoint);
							emit();
							break;
						case Character.FORMAT:
							System.out.printf("Format: %x%n", codePoint);
							if (codePoint == 0x200d) { // zero-width joiner
								shift(codePoint);
								mode = '+';
							} else if (codePoint == 0x200c) { // zero-width non-joiner
								shift(codePoint);
							} else {
								emit();
								shift(codePoint);
								emit();
							}
							break;
						default:
							emit();
							shift(codePoint);
							break;
						}
					}
				} else if (mode == '\r') { // CR+LF, maybe?
					if (codePoint == 'n') {
						shift(codePoint);
						emit();
					} else {
						emit();
						continue processCodePoint;
					}
				} else if (mode == 0x1b) {
					if (codePoint == '[') { // CSI
						mode = codePoint;
						flags |= GraphemeConsumer.FLAG_CONTROL_SEQUENCE;
						shift(codePoint);
					} else if (codePoint == ']') { // OSC
						mode = codePoint;
						flags |= GraphemeConsumer.FLAG_CONTROL_SEQUENCE;
						shift(codePoint);
					} else {
						flags |= GraphemeConsumer.FLAG_KEY;
						shift(codePoint);
						emit();
					}
				} else if (mode == '+') {
					shift(codePoint);
					mode = 0;
				} else if (mode == '[') { // CSI
					if (codePoint >= 0x30 && codePoint <= 0x3f) { // parameter byte
						shift(codePoint);
					} else if (codePoint >= 0x20 && codePoint <= 0x2f) { // intermediate byte
						shift(codePoint);
					} else if (codePoint >= 0x40 && codePoint <= 0x7e) { // final byte
						shift(codePoint);
						emit();
						mode = 0;
					} else { // error, emit sequence and start over
						emit();
						continue processCodePoint;
					}
				} else if (mode == ']') { // OSC
					shift(codePoint);
					if (codePoint == '\b') {
						emit();
					} else if (codePoint == 0x1b) { // maybe start of ST?
						mode = 'S';
					}
				} else if (mode == 'S') { // OSC + start of ST
					shift(codePoint);
					if (codePoint == '\\') { // yes, it's the string terminator
						emit();
					} else if (codePoint != 0x1b) { // no, still part of OSC
						mode = ']';
					}
				}
				break;
			}
		}
		return this;
	}

	public synchronized Graphemizer put(String s) {
		add(s);
		flush();
		return this;
	}

	public synchronized Graphemizer flush() {
		if (!csiWait || !(mode == 0x1b || mode == '[' || mode == ']' || mode == 'S'))
			emitTentative();
		return this;
	}

	private void shift(int codePoint) {
		buffer.appendCodePoint(codePoint);
	}

	private void emit() {
		String output = "";
		flags |= GraphemeConsumer.FLAG_END;
		if (buffer.length() != 0) {
			flags |= GraphemeConsumer.FLAG_NON_EMPTY;
			output = buffer.toString();
			buffer.delete(0, buffer.length());
		}
		if (lastEmitted != null) {
			lastEmitted.append(output);
			consumer.accept(output, flags);
			lastEmitted = null;
		} else if (!output.isEmpty()) {
			consumer.accept(output, flags);
		}
		mode = 0;
		flags = GraphemeConsumer.FLAG_START;
	}

	private void emitTentative() {
		if (buffer.length() == 0)
			return;
		String output = buffer.toString();
		if (lastEmitted == null) {
			lastEmitted = buffer;
			buffer = new StringBuilder();
			consumer.accept(output, flags);
		} else {
			lastEmitted.append(buffer);
			buffer.delete(0, buffer.length());
			consumer.accept(output, flags);
		}
		flags &= ~GraphemeConsumer.FLAG_START;
	}
}