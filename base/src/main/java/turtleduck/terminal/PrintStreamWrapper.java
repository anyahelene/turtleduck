package turtleduck.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;

public abstract class PrintStreamWrapper extends PrintStream {
	public PrintStreamWrapper() {
		super(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				throw new IOException("This method should never be called! Missing override in TerminalPrintStream?");
			}
		}, true); // TODO: add UTF-8 argument and make it work with TeaVM
	}

	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	public abstract void flush();

	public void close() {
		super.close();
	}

	public boolean checkError() {
		return false;
	}

	public void write(int b) {
		writeByte(b);
	}

	protected abstract void writeByte(int b);

	protected abstract void writeString(String s);

	public void write(byte[] buf, int off, int len) {
		synchronized (this) {
			for (int i = off; i < len; i++)
				writeByte(buf[i] & 0xff);
		}
	}

	public void print(boolean b) {
		print(String.valueOf(b));
	}

	public void print(char c) {
		print(String.valueOf(c));
	}

	public void print(int i) {
		print(String.valueOf(i));
	}

	public void print(long l) {
		print(String.valueOf(l));
	}

	public void print(float f) {
		print(String.valueOf(f));
	}

	public void print(double d) {
		print(String.valueOf(d));
	}

	public void print(char[] s) {
		print(String.valueOf(s));
	}

	public void print(String s) {
		writeString(String.valueOf(s));
	}

	public void print(Object obj) {
		print(String.valueOf(obj));
	}

	public void println() {
		print("\n");
	}

	public void println(boolean x) {
		println(String.valueOf(x));
	}

	public void println(char x) {
		println(String.valueOf(x));
	}

	public void println(int x) {
		println(String.valueOf(x));
	}

	public void println(long x) {
		println(String.valueOf(x));
	}

	public void println(float x) {
		println(String.valueOf(x));
	}

	public void println(double x) {
		println(String.valueOf(x));
	}

	public void println(char[] x) {
		println(String.valueOf(x));
	}

	public void println(String x) {
		print((x != null ? x : "null") + "\n");
	}

	public void println(Object x) {
		println(String.valueOf(x));
	}

	public PrintStream printf(String format, Object... args) {
		return super.format(format, args);
	}

	public PrintStream printf(Locale l, String format, Object... args) {
		return super.format(l, format, args);
	}

	public PrintStream format(String format, Object... args) {
		return super.format(format, args);
	}

	public PrintStream format(Locale l, String format, Object... args) {
		return super.format(l, format, args);
	}

	public PrintStream append(CharSequence csq) {
		return super.append(csq);
	}

	public PrintStream append(CharSequence csq, int start, int end) {
		return super.append(csq, start, end);
	}

	public PrintStream append(char c) {
		return super.append(c);
	}

}
