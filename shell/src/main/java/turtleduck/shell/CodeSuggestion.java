package turtleduck.shell;

import java.util.List;
import java.util.stream.Collectors;

import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import turtleduck.text.TextCursor;

public class CodeSuggestion implements Comparable<CodeSuggestion>, Suggestion {

    private String input, expanded, insert;
    private SourceCodeAnalysis sca;
    private int anchor;
    private String cont;
    private boolean tmatch;
    private List<String> signatures;

    public CodeSuggestion(String input, int anchor, Suggestion sg, SourceCodeAnalysis sca) {
        this.input = input;
        this.anchor = anchor;
        this.sca = sca;
        this.cont = sg.continuation();
        this.tmatch = sg.matchesType();

    }

    @Override
    public int compareTo(CodeSuggestion o) {
        int i = cont.compareTo(o.cont);
        if (i != 0)
            return i;
        i = -Boolean.compare(tmatch, o.tmatch);
        // if (i != 0)
        return i;
        /*
         * String sig1 = signature(), sig2 = o.signature(); if (sig1 != null && sig2 ==
         * null) return -1; else if (sig1 == null && sig2 != null) return 1; else if
         * (sig1 == null && sig2 == null) return 0; else return sig1.compareTo(sig2);
         */
    }

    @Override
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

    public void debug() {
        String ex = expansion();
        System.out.println(ex);
        System.out.println("  type: " + sca.analyzeType(ex, ex.length()));
        QualifiedNames names = sca.listQualifiedNames(ex, ex.length());
        System.out.println("  resolvable: " + names.isResolvable());
        System.out.println("  up-to-date: " + names.isUpToDate());
        if (names.getSimpleNameLength() > 0) {
            System.out.println("  fqdns:      " + names.getNames());
        }
        for (String s : signatures()) {
            System.out.println("    " + s);
        }
    }

    public List<String> signatures() {
        if (signatures == null) {
            signatures = sca.documentation(expansion(), expansion().length(), false)//
                    .stream().map(d -> d.signature()).collect(Collectors.toList());
        }
        return signatures;
    }

    @Override
    public boolean matchesType() {
        return tmatch;
    }

}
