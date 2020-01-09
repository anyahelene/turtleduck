package turtleduck.text;

public interface TextPage {
	<T> TextPage insertAt(int x, int y, Attributed<T> s);
	<T> TextPage replaceAt(int x, int y, Attributed<T> s);
	TextPage insertAt(int x, int y, String s, Attributes attrs);
	TextPage replaceAt(int x, int y, String s, Attributes attrs);
	TextPage insertAt(int x, int y, CodePoint cp, Attributes attrs);
	TextPage replaceAt(int x, int y, CodePoint cp, Attributes attrs);
	TextPage deleteAt(int x, int y);
	TextPage clearAt(int x, int y);
	CodePoint codePointAt(int x, int y);
	Attributes attrsAt(int x, int y);
}
