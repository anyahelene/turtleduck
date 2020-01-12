package turtleduck.text.impl;

import turtleduck.text.Attributed;
import turtleduck.text.Attributes;

public class AttributedImpl<T> implements Attributed<T> {
	protected T data;
	protected Attributes attrs;

	public AttributedImpl(T data, Attributes attrs) {
		this.data = data;
		this.attrs = attrs;
	}

	@Override
	public T data() {
		return data;
	}

	@Override
	public Attributes attributes() {
		return attrs;
	}

}
