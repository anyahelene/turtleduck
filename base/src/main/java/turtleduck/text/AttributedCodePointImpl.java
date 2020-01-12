package turtleduck.text;

@Deprecated
public class AttributedCodePointImpl implements AttributedCodePoint {
	int codepoint;
	Attributes attrs;
	
	@Override
	public int value() {
		return codepoint;
	}

	@Override
	public CodePoint data() {
		return CodePoint.codePoint(codepoint);
	}

	@Override
	public Attributes attributes() {
		return attrs;
	}

	@Override
	public <T> T get(Attribute<T> attr) {
		return attrs.get(attr);
	}

	@Override
	public boolean isSet(Attribute<?> attr) {
		return attrs.isSet(attr);
	}

	@Override
	public AttributeBuilder change() {
		return null;
	}

	@Override
	public String toHtml() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toCss() {
		// TODO Auto-generated method stub
		return null;
	}

}
