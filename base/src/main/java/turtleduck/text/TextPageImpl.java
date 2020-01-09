package turtleduck.text;

import java.util.ArrayList;
import java.util.List;

public class TextPageImpl implements TextPage {
	public static final int STANDARD_PAGE_LENGTH = 40;
	public static final int STANDARD_PAGE_WIDTH = 132;
	protected final List<List<AttributedCodePoint>> page = new ArrayList<>();

	protected List<AttributedCodePoint> line(int x, int y) {
		List<AttributedCodePoint> line = null;
		if (y == page.size()) {
			page.add(new ArrayList<>());
		} else if (y > page.size()) {
			if (y < STANDARD_PAGE_LENGTH) {
				for (int i = 0; i < STANDARD_PAGE_LENGTH; i++)
					page.add(new ArrayList<>());
			} else {
				throw new IndexOutOfBoundsException("" + x + "," + y);
			}
		}

		line = page.get(y);
		return line;
	}

	protected void expandLine(int x, List<AttributedCodePoint> line) {
		if (x >= line.size()) {
			for (int i = 0; i < STANDARD_PAGE_WIDTH; i++)
				line.add(new AttributedCodePointImpl());
		}
	}

	@Override
	public <T> TextPage insertAt(int x, int y, Attributed<T> s) {
		T data = s.data();
		if (data instanceof CodePoint)
			return insertAt(x, y, (CodePoint) data, s.attributes());
		else
			return insertAt(x, y, String.valueOf(data), s.attributes());
	}

	@Override
	public <T> TextPage replaceAt(int x, int y, Attributed<T> s) {
		T data = s.data();
		if (data instanceof CodePoint)
			return replaceAt(x, y, (CodePoint) data, s.attributes());
		else
			return replaceAt(x, y, String.valueOf(data), s.attributes());
	}

	@Override
	public TextPage insertAt(int x, int y, String s, Attributes attrs) {
		int loc[] = { x, y };
		CodePoint.stream(s).forEach(cp -> {
			insertAt(loc, cp, attrs);
		});
		return this;
	}

	private void insertAt(int[] loc, CodePoint cp, Attributes attrs) {
		List<AttributedCodePoint> line = line(loc[0], loc[1]);
		switch (cp.value()) {
		case '\b':
			if (loc[0] > 0) {
				loc[0]--;
			}
			break;
		case '\f':
		case '\n':
			if (loc[0] == 0) {
				page.add(loc[1]++, new ArrayList<>());
			} else if (loc[0] < line.size() / 2) {
				List<AttributedCodePoint> sub = line.subList(0, loc[0]);
				page.add(++loc[1], new ArrayList<>(sub));
				sub.clear();
			} else if (loc[0] < line.size() - 1) {
				List<AttributedCodePoint> sub = line.subList(loc[0], line.size());
				page.add(loc[1]++,new ArrayList<>(sub));
				sub.clear();
			} // otherwise, the line is already ended
			loc[0] = 0;
			break;
		case '\r':
			// ignore CR
			break;
//		case '\t':
//			break;
		default:
			line.add(loc[0]++, new AttributedCodePointImpl());
		}
	}

	@Override
	public TextPage replaceAt(int x, int y, String s, Attributes attrs) {
		return this;
	}

	@Override
	public TextPage insertAt(int x, int y, CodePoint cp, Attributes attrs) {
		return this;
	}

	@Override
	public TextPage replaceAt(int x, int y, CodePoint cp, Attributes attrs) {
		return this;
	}

	@Override
	public TextPage deleteAt(int x, int y) {
		return this;
	}

	@Override
	public TextPage clearAt(int x, int y) {
		return this;
	}

	@Override
	public CodePoint codePointAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attributes attrsAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

}
