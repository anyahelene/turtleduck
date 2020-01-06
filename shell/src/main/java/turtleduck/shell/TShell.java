package turtleduck.shell;

import java.util.List;

import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Builder;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import turtleduck.colors.Colors;
import turtleduck.events.KeyCode;
import turtleduck.text.Printer;

public class TShell {

	private Printer printer;
	private JShell shell;
	private SourceCodeAnalysis sca;
	private String input = "";

	public TShell(Printer printer) {
		this.printer = printer;
		Builder builder = JShell.builder();
		shell = builder.build();
		sca = shell.sourceCodeAnalysis();

		printer.print("java> ");
	}

	public void arrowKey(KeyCode code) {
		if (code == KeyCode.BACK_SPACE) {
			if (input.length() > 0) {
				input = input.substring(0, input.length() - 1);
				printer.print("\b \b");
			}
		}
		// TODO Auto-generated method stub

	}

	public void charKey(String character) {
		if (character.equals("\r") || character.equals("\n"))
			enterKey();
		else if (character.equals("\t")) {
			int[] anchor = { 0 };
			List<Suggestion> sugs = sca.completionSuggestions(input, input.length(), anchor);
			for (Suggestion s : sugs) {
				System.out.println(input + "…" + s.continuation());
			}
		} else {
			input += character;
			printer.print(character);
			CompletionInfo info = sca.analyzeCompletion(input);
			System.out.println(info.completeness());
		}
	}

	public void enterKey() {
		CompletionInfo info = sca.analyzeCompletion(input);
		System.out.println(info.completeness());
		printer.println();
		List<SnippetEvent> eval = shell.eval(input);
		for (SnippetEvent e : eval) {
			shell.diagnostics(e.snippet()).forEach((diag) -> {
				long start = diag.getStartPosition();
				long end = diag.getEndPosition();
				long pos = diag.getPosition();
				if (diag.isError())
					printer.setInk(Colors.RED);
				else
					printer.setInk(Colors.YELLOW.darker());
				if (pos != Diag.NOPOS) {
					printer.moveHoriz((int) (6 + pos));
					printer.println("^");
				}
				printer.println("" + diag.getMessage(null));
				printer.setInk(Colors.BLACK);
			});
			Status status = e.status();
			printer.println("" + status);
			switch (status) {
			case DROPPED:
				break;
			case NONEXISTENT:
				break;
			case OVERWRITTEN:
				break;
			case RECOVERABLE_DEFINED:
				break;
			case RECOVERABLE_NOT_DEFINED:
				break;
			case REJECTED:
				printer.println("" + e.snippet().source());
				printer.println("" + e.snippet().kind());
				printer.println("" + e.snippet().subKind());
				break;
			case VALID:
				break;
			default:
				break;

			}
			JShellException exception = e.exception();
			if (exception != null) {
				printer.print(exception.getLocalizedMessage() + "\n", Colors.RED);
			}
			String value = e.value();
			if (value != null) {
				printer.print(value + "\n", Colors.BLUE);
			}
		}
		System.out.println("'" + input + "' → " + eval);
		input = "";
		printer.print("java> ");
	}

}
