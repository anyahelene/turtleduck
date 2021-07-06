import { EditorView } from '@codemirror/view';
import { HighlightStyle, tags } from '@codemirror/highlight';

// Using https://github.com/one-dark/vscode-one-dark-theme/ as reference for the colors
const type = "#cc0", name = "#3f3", op = "#56b6c2", invalid = "#ffffff", def = "#8f8", comment = "#880", // Brightened compared to original to increase contrast
func = "#3f3", string = "#98c379", bool = "#d19a66", keyword = "#f6f", //
darkBackground = "#121", highlightBackground = "#000", background = "#111", //
selection = "#3E4451", cursor = "#f00";
/// The editor theme styles for One Dark.
const darkDuckTheme = EditorView.theme({
    "&": {
        color: def,
        backgroundColor: background,
        "& ::selection": { backgroundColor: selection },
        caretColor: cursor
    },
    ".cm-cursor": { display: "block", borderLeftColor: "#800", borderLeftWidth: ".5em" },
	".cm-cursorLayer": { mixBlendMode: "exclusion" },
    "&.cm-focused .cm-cursor, .focused .cm-cursor": { borderLeftColor: cursor },
    "&.cm-focused .cm-cursor-secondary": { borderLeftColor: "#ff0" },
    ".cm-cursor-secondary": { borderLeftColor: "#880" },
    "&.cm-focused .cm-selectionBackground, .cm-selectionBackground": { backgroundColor: selection },
    ".cm-panels": { backgroundColor: darkBackground, color: def },
    ".cm-panels.cm-panels-top": { borderBottom: "1px solid #8808" },
    ".cm-panels.cm-panels-bottom": { borderTop: "1px solid #8808" },
    ".cm-searchMatch": {
        backgroundColor: "#72a1ff59",
        outline: "1px solid #457dff"
    },
    ".cm-searchMatch.cm-searchMatch-selected": {
        backgroundColor: "#6199ff2f"
    },
    ".cm-activeLine": { backgroundColor: background, textShadow: "none" },
    ".cm-selectionMatch": { backgroundColor: "#aafe661a" },
    ".cm-matchingBracket, .cm-nonmatchingBracket": {
        backgroundColor: "#bad0f847",
        outline: "1px solid #515a6b"
    },
    ".cm-gutters": {
		background: "linear-gradient(90deg, #220f 0%, #2200 100%)",
        backgroundColor: background,
        color: comment,
        border: "none"
		//borderRight: "1px solid #8808"
    },
    ".cm-lineNumbers .cm-gutterElement": { color: "inherit" },
    ".cm-foldPlaceholder": {
        backgroundColor: "transparent",
        border: "none",
        color: "#ddd"
    },
    ".cm-tooltip": {
        border: "1px solid #181a1f",
        backgroundColor: darkBackground
    },
    ".cm-tooltip-autocomplete": {
        "& > ul > li[aria-selected]": {
            backgroundColor: highlightBackground,
            color: def
        }
    }
}, { dark: true });
/// The highlighting style for code in the One Dark theme.
const darkDuckHighlightStyle = HighlightStyle.define([
    { tag: tags.keyword,
        color: keyword },
    { tag: [tags.name, tags.deleted, tags.number, tags.character, tags.propertyName, tags.macroName, tags.variableName, tags.separator],
        color: name },
    { tag: [tags.function(tags.variableName), tags.labelName],
        color: func },
    { tag: [tags.color, tags.constant(tags.name), tags.standard(tags.name)],
        color: bool },
    { tag: [tags.definition(tags.name)],
        color: def },
    { tag: [tags.typeName, tags.className,  tags.changed, tags.annotation, tags.modifier, tags.self, tags.namespace],
        color: type },
    { tag: [tags.operator, tags.operatorKeyword, tags.url, tags.escape, tags.regexp, tags.link, tags.special(tags.string)],
        color: op },
    { tag: [tags.meta, tags.comment],
        color: comment },
    { tag: tags.strong,
        fontWeight: "bold" },
    { tag: tags.emphasis,
        fontStyle: "italic" },
    { tag: tags.link,
        color: comment,
        textDecoration: "underline" },
    { tag: tags.heading,
        fontWeight: "bold",
        color: name },
    { tag: [tags.atom, tags.bool, tags.special(tags.variableName)],
        color: bool },
    { tag: [tags.processingInstruction, tags.string, tags.inserted],
        color: string },
    { tag: tags.invalid,
        color: invalid },
]);
/// Extension to enable the One Dark theme (both the editor theme and
/// the highlight style).
const darkDuck = [darkDuckTheme, darkDuckHighlightStyle];

export { darkDuck, darkDuckHighlightStyle, darkDuckTheme };