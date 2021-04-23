//import {EditorState} from "@codemirror/state"
//import {EditorView, keymap} from "@codemirror/view"
//import {defaultKeymap} from "@codemirror/commands"
import {EditorView, Decoration, keymap} from "@codemirror/view"
import {EditorState, StateField, StateEffect} from "@codemirror/state"
import {defaultTabBinding,insertNewlineAndIndent} from "@codemirror/commands"
import { syntaxTree } from '@codemirror/language';

import {basicSetup} from "@codemirror/basic-setup"
import {java} from "@codemirror/lang-java"
import {html} from "@codemirror/lang-html"
import {markdown} from "@codemirror/lang-markdown"
import {css} from "@codemirror/lang-css"
import {oneDark} from "@codemirror/theme-one-dark"
import {darkDuck} from "./darktheme.js"
import {hoverTooltip} from "@codemirror/tooltip"
import {closeLintPanel, lintKeymap, linter, nextDiagnostic, openLintPanel, setDiagnostics} from "@codemirror/lint"
import { NodeProp } from 'lezer-tree';

console.log(document.body);

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



const configs = {};
function config(elt, lang) {
	if(configs[lang] == undefined) {
		var langext = undefined;
		const myFontTheme = EditorView.theme({
		  '.cm-scroller':{
		  //  fontSize: "18px",
			fontFamily: window.getComputedStyle(elt).fontFamily,
			textShadow: "0 0 .2rem currentColor"
		  }
		});		if(lang == "java") {
			langext = java();
		} else if(lang == "html") {
			langext = html();
		} else if(lang == "markdown") {
			langext = markdown();
		} else if(lang == "css") {
			langext = css();
		}
		return [basicSetup, langext, EditorView.lineWrapping, markKeymap,
					keymap.of(defaultTabBinding), oneDark, myFontTheme, wordHover];
	}
	
	return configs[lang];
}
const addMark = StateEffect.define();

const markField = StateField.define({
	create() {
    	return Decoration.none;
  	},
  	update(marks, tr) {
    	marks = marks.map(tr.changes);
    	for (let e of tr.effects) if (e.is(addMark)) {
      	marks = marks.update({
       	 add: [markDecoration.range(e.value.from, e.value.to)]
      	})
   	 }
    return marks
  },
  provide: f => EditorView.decorations.from(f)
});

const markDecoration = Decoration.mark({class: "cm-underline"});

const markTheme = EditorView.baseTheme({
  ".cm-underline": { textDecoration: "underline wavy 1px red" }
})
export function markSelection(view) {
  let effects = view.state.selection.ranges
    .filter(r => !r.empty)
    .map(({from, to}) => addMark.of({from, to}))
  if (!effects.length) return false

  if (!view.state.field(markField, false))
    effects.push(StateEffect.appendConfig.of([markField,
                                              markTheme]))
  view.dispatch({effects})
  return true
}

export function markRange(view, from, to) {
  let effects = [addMark.of({from, to})];
  if (!effects.length) return false

  if (!view.state.field(markField, false))
    effects.push(StateEffect.appendConfig.of([markField,
                                              markTheme]))
  view.dispatch({effects})
  return true
}

export const markKeymap = keymap.of([{
  key: "Mod-h",
  preventDefault: true,
  run: markSelection
}])

export const wordHover = hoverTooltip((view, pos, side) => {
  let {from, to, text} = view.state.doc.lineAt(pos)
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
      return {dom}
    }
  };
})

class TDEditor {
	constructor(elt, text,exts) {
		console.log("TDEditor(%o,%o,%o)", elt, text, exts)
		this.elt = elt;
		this.exts = exts;

		const state = this.createState(text);
		this.EditorView = EditorView;
		this.view = new EditorView({state: state, parent: elt});
		this.$markField = markField;
		this.$addMark = addMark;

	}
	
	state() {
		return this.view.state;
	}
	switchState(newState) {
		const oldState = this.view.state;
		this.view.setState(newState);
		return oldState;
	}
	
	addMark(from, to) {
		markRange(this.view, from, to);
	}
	createState(text) {
		const state = EditorState.create({
			doc: text,
     		extensions: this.exts
  		});
		return state;
	}

	mark(spec) {
		return Decoration.mark(spec);
	}
	
	widgetDecor(spec) {
		return Decoration.widget(spec);
	}
	
	replaceDecor(spec) {
		return Decoration.replace(spec);
	}
	
	lineDecor(spec) {
		return Decoration.line(spec);
	}
	
	diagnostic(from, to, severity, message, actions = []) {
		return {from: from, to: to, severity: severity, message: message, actions: actions};
	}
	
	setDiagnostics(state, diags) {
		return setDiagnostics(state, diags);
	}
}

window.TDEditor = TDEditor;

window.turtleduck.createEditor = function(elt,text) {
	let editor = new TDEditor(elt,text,[config(elt,"java")]);

	return editor;
}

function enter({ state, dispatch }) {
    let changes = state.changeByRange(({ from, to }) => {
        let between =  isBetweenBrackets(state, from);
		console.log("enter key pressed: from=%o, to=%o, between=%o, state=%o", from, to, between, state);
		return { range: {from,to}};
	})
}
function arrowUp(view) {
	console.log("arrowUp");
}
function arrowDown(view) {
	console.log("arrowUp");
}

window.turtleduck.createLineEditor = function(elt,text) {
	let editor = new TDEditor(elt,text, [
		keymap.of([{ key: "Enter", run: enter, shift: insertNewlineAndIndent }]),
		config(elt,"java"),
		keymap.of([{ key: "ArrowUp", run: arrowUp }, { key: "ArrowDown", run: arrowDown }])
	]);

	return editor;
}
