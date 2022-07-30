//import {EditorState} from "@codemirror/state"
//import {EditorView, keymap} from "@codemirror/view"
//import {defaultKeymap} from "@codemirror/commands"
import { EditorView, Decoration, keymap, WidgetType, showPanel, hoverTooltip, DecorationSet } from "@codemirror/view"
import { EditorState, EditorSelection, StateField, StateEffect, ChangeDesc, ChangeSpec, SelectionRange, Extension, Text } from "@codemirror/state"
import { indentWithTab, indentMore, indentLess, insertNewlineAndIndent, historyField } from "@codemirror/commands"
import { syntaxTree, getIndentation, indentUnit, IndentContext, LanguageSupport, StreamLanguage } from '@codemirror/language';
import { autocompletion, completionStatus, currentCompletions, prevSnippetField } from "@codemirror/autocomplete";
import { StyleModule } from "style-mod";
import { basicSetup } from "@codemirror/basic-setup"
import { java } from "@codemirror/lang-java"
import { cpp } from "@codemirror/lang-cpp"
import { python } from "@codemirror/lang-python"
import { html } from "@codemirror/lang-html"
import { markdown, insertNewlineContinueMarkup } from "@codemirror/lang-markdown"
import { css } from "@codemirror/lang-css"
import { z80 } from "@codemirror/legacy-modes/mode/z80"
import { oneDark } from "@codemirror/theme-one-dark"
import { darkDuck, darkDuckHighlightSpec, darkDuckHighlighter } from "./themes/dark-duck"
import { closeLintPanel, lintKeymap, linter, nextDiagnostic, openLintPanel, setDiagnostics, Diagnostic, Action } from "@codemirror/lint"
import { NodeProp } from '@lezer/common';
import { highlightTree, classHighlighter, tags } from '@lezer/highlight';
//import { listTags } from "isomorphic-git";

function isBetweenBrackets(state, pos) {
	if (/\(\)|\[\]|\{\}/.test(state.sliceDoc(pos - 1, pos + 1)))
		return { from: pos, to: pos };
	let context = syntaxTree(state).resolve(pos);
	let before = context.childBefore(pos), after = context.childAfter(pos), closedBy;
	if (before && after && before.to <= pos && after.from >= pos &&
		(closedBy = before.type.prop(NodeProp.closedBy)) && closedBy.indexOf(after.name) > -1 &&
		state.doc.lineAt(before.to).from == state.doc.lineAt(after.from).from)
		return { from: before.to, to: after.from };
	return null;
}

class PromptWidget extends WidgetType {
	checked = false;
	constructor(public prompt: string) { super() }

	eq(other: PromptWidget) { return other.prompt == this.prompt }

	toDOM() {
		let wrap = document.createElement("span")
		wrap.setAttribute("aria-hidden", "true")
		wrap.className = "cm-boolean-toggle"
		let box = wrap.appendChild(document.createElement("input"))
		box.type = "checkbox"
		box.checked = this.checked;
		return wrap
	}

	ignoreEvent() { return false }

}

export function stdConfig() {
	return [basicSetup, EditorState.tabSize.of(4), markKeymap, keymap.of([{ key: 'Tab', run: indentMore, shift: indentLess }]), darkDuck];
}
const configs = { '': [] };
function langConfig(lang: string) {
	if (configs[lang]) {
		return configs[lang];
	} else {
		var langext = undefined;

		if (lang == "java" || lang == "jsh") {
			langext = java();
		} else if (lang == "python") {
			langext = python();
		} else if (lang == "html") {
			langext = html();
		} else if (lang == "markdown") {
			langext = markdown();
		} else if (lang == "chat") {
			langext = markdown({ addKeymap: false });
		} else if (lang == "css") {
			langext = css();
		} else if (lang == "cpp") {
			langext = [cpp(), indentUnit.of("    ")];
		} else if (lang == "z80") {
			console.log("z80");
			langext = new LanguageSupport(StreamLanguage.define(z80));
			console.log(langext);
		} else if (lang == "plain") {
			langext = [];
		}
		if (langext) {
			configs[lang] = langext;
			return langext;
		} else {
			throw Error("No configuration found for " + lang);
		}
	}
}
function fontConfig(elt: HTMLElement) {
	var fontFamily = window.getComputedStyle(elt).fontFamily;
	if (!fontFamily)
		fontFamily = window.getComputedStyle(document.body).fontFamily;
	const myFontTheme = EditorView.theme({
		'.cm-scroller': {
			//  fontSize: "18px"
			fontFamily: fontFamily
			//,textShadow: "0 0 .1rem currentColor"
		}/*,
		  '.cm-scroller .cm-line':{
			opacity: ".9"
		  },
		  '.cm-scroller .cm-line.cm-activeLine':{
			opacity: "1"
		  }
*/
	});
	return myFontTheme;
}
const addMark = StateEffect.define<{ from: number, to: number }>();
const markField = StateField.define<DecorationSet>({
	create() {
		return Decoration.none;
	},
	update(marks, tr) {
		marks = marks.map(tr.changes);
		for (let e of tr.effects) {
			if (e.is(addMark)) {
				marks = marks.update({
					add: [markDecoration.range(e.value.from, e.value.to)]
				});
			}
		}
		return marks
	},
	provide: f => EditorView.decorations.from(f)
});

const markDecoration = Decoration.mark({ class: "cm-underline" });

const markTheme = EditorView.baseTheme({
	".cm-underline": { textDecoration: "underline wavy 1px red" }
})
export function markSelection(view: EditorView) {
	let effects: StateEffect<unknown>[] = view.state.selection.ranges
		.filter(r => !r.empty)
		.map(({ from, to }) => addMark.of({ from, to }))
	if (!effects.length) return false

	if (!view.state.field(markField, false))
		effects.push(StateEffect.appendConfig.of([markField,
			markTheme]));
	view.dispatch({ effects });
	return true
}

export function markRange(view: EditorView, from: number, to: number) {
	let effects: StateEffect<unknown>[] = [addMark.of({ from, to })];
	if (!effects.length) return false

	if (!view.state.field(markField, false))
		effects.push(StateEffect.appendConfig.of([markField,
			markTheme]))
	view.dispatch({ effects })
	return true
}

export const markKeymap = keymap.of([{
	key: "Mod-h",
	preventDefault: true,
	run: markSelection
}])

export const wordHover = hoverTooltip((view, pos, side) => {
	let { from, to, text } = view.state.doc.lineAt(pos)
	let start = pos, end = pos
	while (start > from && /\w/.test(text[start - from - 1])) start--
	while (end < to && /\w/.test(text[end - from])) end++
	if ((start == pos && side < 0) || (end == pos && side > 0))
		return null
	return {
		pos: start,
		end,
		above: true,
		create(view) {
			let dom = document.createElement("div")
			dom.textContent = text.slice(start - from, end - from)
			return { dom }
		}
	};
})



export class TDEditor {
	historyField = historyField;
	prevSnippetField = prevSnippetField;
	EditorView: typeof EditorView;
	view: EditorView;
	$markField: StateField<DecorationSet>;
	$addMark: any;
	_debugState: boolean;
	_after_paste?: () => void;
	_paste_to_file: { accept: (filename: string, text: string, language: string) => void };
	constructor(private name: string, private outer: HTMLElement, private elt: HTMLElement, text: string, lang: string, private exts: Extension[], private preExts: Extension[] = []) {
		// super(name, outer, tdstate);

		console.log("TDEditor(%s,%o,%o,%o,%o,%o)", name, elt, text, exts, preExts)

		elt.addEventListener("dragenter", e => {
			if (e.dataTransfer.types.includes("text/plain"))
				e.preventDefault();
		}, true);
		elt.addEventListener("drop", e => {
			console.log(e.dataTransfer);
			this.paste(e.dataTransfer.getData("text/plain"));
			e.preventDefault();
		}, true);
		const state = this.createState(lang, text);
		this.EditorView = EditorView;
		this.view = new EditorView({ state: state, parent: elt, root: document });
		this.$markField = markField;
		this.$addMark = addMark;
		this._debugState = false;

	}

	scrollDOM() {
		return this.view.scrollDOM;
	}

	highlightTree(prompt: HTMLElement) {
		const state = this.view.state;
		const doc = state.doc;
		const result = document.createElement('div');
		result.setAttribute('class', 'cm-content');
		let line: HTMLElement = null;
		const tree = syntaxTree(this.view.state);
		let pos = 0;


		function newline() {
			if (line != null)
				result.appendChild(line);
			line = document.createElement('div');
			line.setAttribute('class', 'cm-line');
		}
		newline();
		if (prompt)
			line.appendChild(prompt);

		function nolight(to: number) {
			if (to > pos) {
				const text = doc.sliceString(pos, to, "\n");
				const lines = text.split("\n");
				while (lines.length > 0) {
					const lineText = lines.shift();
					if (lineText.length > 0) {
						line.appendChild(document.createTextNode(lineText));
					}
					if (lines.length > 0) {
						newline();
					}
				}
				pos = to;
			}
		}
		function highlight(from: number, to: number, classes: string) {
			if (from > pos) {
				nolight(from);
			}
			pos = to;
			const elt = document.createElement('span');
			elt.setAttribute('class', classes);
			elt.textContent = doc.sliceString(from, to, "\n");
			line.appendChild(elt);
		}
		function join(cls1: string, cls2: string) {
			if (cls1 == null)
				return cls2;
			else if (cls2 == null)
				return cls1;
			else
				return cls1 + ' ' + cls2;
		}
		highlightTree(tree, [darkDuckHighlighter, classHighlighter], highlight);
		//	join(darkDuckHighlightStyle.match(tag, scope), classHighlightStyle.match(tag, scope)), highlight);
		nolight(doc.length);
		if (line.childElementCount > 0)
			result.appendChild(line);
		return result;
	}

	wrapper() {
		return this.elt;
	}

	syntaxTree() {
		return syntaxTree(this.view.state);
	}
	state() {
		return this.view.state;
	}
	focus() {
		// this.select();  TODO
		this.view.focus();
		return "TDEditor";
	}
	paste(text: string | Text, cursorAdj = 0) {
		const tr = this.view.state.replaceSelection(text);
		if (cursorAdj != 0 && tr.selection instanceof EditorSelection) {
			const move = EditorSelection.create(tr.selection.ranges.map(r => {
				if (r.empty) {
					(r as { from: number }).from--;
					(r as { to: number }).to--;
				} else {
					(r as { to: number }).to--;
				}
				return r;
			}), tr.selection.mainIndex);
		}
		tr.scrollIntoView = true;
		this.view.dispatch(tr);
		if (this._after_paste) {
			this._after_paste();
		}
	}
	paste_to_file(filename = '', text = '', language = '') {
		this._paste_to_file.accept(filename, text, language);
	}
	switchState(newState: EditorState) {
		if (this._debugState)
			console.log("switchState");
		const oldState = this.view.state;
		this.view.setState(newState);
		if (this._debugState)
			console.log(newState);
		return oldState;
	}

	addMark(from: number, to: number) {
		markRange(this.view, from, to);
	}
	createState(lang: string, text: string, pos?: number) {
		let selection: { anchor: number; };
		if (this._debugState)
			console.log("createState: ", lang, text, text.length, pos);
		if (pos) {
			if (pos < 0) {
				selection = { anchor: text.length + 1 + pos };
			} else {
				selection = { anchor: pos };
			}
		}
		if (this._debugState)
			console.log("createState: ", text, selection);
		const state = EditorState.create({
			doc: text,
			extensions: [this.preExts, langConfig(lang), this.exts],
			selection: selection
		});
		if (this._debugState)
			console.log("createState: ", state);
		return state;
	}

	mark(spec: Parameters<typeof Decoration.mark>[0]): Decoration {
		return Decoration.mark(spec);
	}

	widgetDecor(spec: Parameters<typeof Decoration.widget>[0]) {
		return Decoration.widget(spec);
	}

	replaceDecor(spec: Parameters<typeof Decoration.replace>[0]) {
		return Decoration.replace(spec);
	}

	lineDecor(spec: Parameters<typeof Decoration.line>[0]) {
		return Decoration.line(spec);
	}

	diagnostic(from: number, to: number, severity: ("info" | "warning" | "error"), message: string, actions: Action[] = []): Diagnostic {
		return { from, to, severity, message, actions };
	}

	setDiagnostics(state: EditorState, diags: readonly Diagnostic[]) {
		return setDiagnostics(state, diags);
	}
}

export const createEditor = function (elt: HTMLElement, text: string, lang = "java") {
	const outer = elt;
	const elts = elt.getElementsByClassName('wrapper');
	if (elts[0])
		elt = elts[0] as HTMLElement;

	let editor = new TDEditor(outer.id, outer, elt, lang, text, [fontConfig(elt), stdConfig()]);

	return editor;
}

export const highlight = {
	darkDuckStyle: {},
	classStyle: {},
	classHighlighter,
	darkDuck,
	tags
};


// hack to get style rules as list instead of newline-separated string
declare module 'style-mod' {
	interface StyleModule {
		rules: string[]
	}
}
// for each style rule, split, e.g., ".ͼu {color: #cc0;}" into ".ͼu": "{color: #cc0;}"
darkDuckHighlighter.module.rules.forEach((rule: string) => {
	highlight.darkDuckStyle[rule.replace(/\s*{.*$/, "")] = rule.replace(/^[^{]*{/, "{");
});
// build definitions for classHighlighter css classes based on defs from darkDuckHighlighter
console.groupCollapsed("editor styles");
for (let tag in tags) {
	let t = tags[tag];
	if (t.set) {
		let ddStyleClass = '.' + darkDuckHighlighter.style([t]);
		let clsStyleClass = '.' + classHighlighter.style([t]);
		console.log(ddStyleClass, clsStyleClass);
		highlight.classStyle[clsStyleClass] = highlight.darkDuckStyle[ddStyleClass] || '{}'
	}
}
console.groupEnd();


export const createLineEditor = function (elt: HTMLElement, text: string, lang: string, handler: (arg0: string, arg1: any) => any) {
	function enter({ state, dispatch }) {
		let isComplete = true;
		let changes = state.changeByRange((range: { from: number; to: any; anchor: number; head: any; }) => {
			console.groupCollapsed("enter key pressed at ", range);
			try {
				let text = state.sliceDoc(range.from)
				console.log("text: ", JSON.stringify(text));
				// check if we're in the middle of the text
				if (text.length > 0 && (!text.match(/^\r?\n/) || text.startsWith('/'))) { // TODO: line-break setting?
					isComplete = true;
					return { range };
				}
				let explode = range.from == range.to && isBetweenBrackets(state, range.from);
				let cx = new IndentContext(state, { simulateBreak: range.from, simulateDoubleBreak: !!explode });
				let indent = getIndentation(cx, range.from);
				console.log("indent0: ", indent);
				if (indent == null)
					indent = /^\s*/.exec(state.doc.lineAt(range.from).text)[0].length;
				console.log("indent1: ", indent, "explode: ", explode);
				if (indent || explode)
					isComplete = false;

				const tree = syntaxTree(state);
				console.log("tree", tree);
				let context = tree.resolve(range.anchor);
				console.log("context", context);
				console.log("enter key pressed: from=%o, to=%o, anchor=%o, head=%o, state=%o", range.from, range.to, range.anchor, range.head, state);
				return { range };
			} finally {
				console.groupEnd();
			}
		})
		console.log("changes: ", changes);
		if (isComplete) {
			return handler("enter", state);
		} else {
			return insertNewlineAndIndent({ state, dispatch });
		}
	}
	function tab({ state, dispatch }) {
		let changes = state.changeByRange(range => {
			console.log("tab key pressed: from=%o, to=%o, anchor=%o, head=%o, state=%o", range.from, range.to, range.anchor, range.head, state);
			let context = syntaxTree(state).resolve(range.from);
			console.log("context", context);
			return { range };
		});
		console.log("changes: ", changes);
		return indentMore({ state, dispatch });
	}
	const shiftEnter = (lang === 'markdown' || lang === 'chat') ? insertNewlineContinueMarkup : insertNewlineAndIndent
	function arrowUp({ state, dispatch }) {
		return handler("arrowUp", state);
	}

	function arrowDown({ state, dispatch }) {
		return handler("arrowDown", state);
	}

	const outer = elt;
	const elts = elt.getElementsByClassName('wrapper');
	if (elts[0])
		elt = elts[0] as HTMLElement;

	let editor = new TDEditor(outer.id, outer, elt, text, lang, [
		fontConfig(elt), stdConfig(),
		EditorView.theme({
			'.cm-lineNumbers .cm-gutterElement': {
				display: "none"
			}
		}),
		keymap.of([{ key: "ArrowUp", run: arrowUp }, { key: "ArrowDown", run: arrowDown }])
	],
		[keymap.of([{ key: "Enter", run: enter, shift: shiftEnter }, { key: "Tab", run: tab, shift: indentLess }])]);

	editor._after_paste = () => {
		outer.scrollIntoView({ block: "end", inline: "nearest" });
	};
	return editor;
}