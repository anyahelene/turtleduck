//import {EditorState} from "@codemirror/state"
//import {EditorView, keymap} from "@codemirror/view"
//import {defaultKeymap} from "@codemirror/commands"
import {EditorView, Decoration, keymap, WidgetType} from "@codemirror/view"
import {EditorState, EditorSelection, StateField, StateEffect} from "@codemirror/state"
import {defaultTabBinding,insertNewlineAndIndent} from "@codemirror/commands"
import { syntaxTree } from '@codemirror/language';
import { highlightTree, classHighlightStyle } from '@codemirror/highlight';

import {basicSetup} from "@codemirror/basic-setup"
import {java} from "@codemirror/lang-java"
import {python} from "@codemirror/lang-python"
import {html} from "@codemirror/lang-html"
import {markdown, insertNewlineContinueMarkup} from "@codemirror/lang-markdown"
import {css} from "@codemirror/lang-css"
import {oneDark} from "@codemirror/theme-one-dark"
import {darkDuck, darkDuckHighlightStyle} from "./darktheme.js"
import {hoverTooltip} from "@codemirror/tooltip"
import {showPanel} from "@codemirror/panel"
import {closeLintPanel, lintKeymap, linter, nextDiagnostic, openLintPanel, setDiagnostics} from "@codemirror/lint"
import { NodeProp } from 'lezer-tree';
import { Component } from './Component';

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
	constructor(prompt) { super() }

	eq(other) { return other.prompt == this.prompt }

	toDOM() {
	    let wrap = document.createElement("span")
	    wrap.setAttribute("aria-hidden", "true")
	    wrap.className = "cm-boolean-toggle"
	    let box = wrap.appendChild(document.createElement("input"))
	    box.type = "checkbox"
	    box.checked = this.checked
	    return wrap
	}
	
	ignoreEvent() { return false }

}

function stdConfig() {
	return [basicSetup, markKeymap, keymap.of(defaultTabBinding), darkDuck];
}
const configs = {'' : []};
function langConfig(lang) {
	if(configs[lang]) {
		return configs[lang];
	} else {
		var langext = undefined;

		if(lang == "java" || lang == "jsh") {
			langext = java();
		} else if(lang == "python") {
			langext = python();
		} else if(lang == "html") {
			langext = html();
		} else if(lang == "markdown") {
			langext = markdown();
		} else if(lang == "chat") {
			langext = markdown({addKeymap:false});
		} else if(lang == "css") {
			langext = css();
		} 
		if(langext) {
			configs[lang] = langext;
			return langext;
		} else {
			throw Error("No configuration found for " + lang);
		}
	}
}
function fontConfig(elt) {
	var fontFamily = window.getComputedStyle(elt).fontFamily;
	if(!fontFamily)
		fontFamily = window.getComputedStyle(document.body).fontFamily;
	const myFontTheme = EditorView.theme({
		  '.cm-scroller':{
		  //  fontSize: "18px"
			fontFamily:fontFamily
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




class TDEditor extends Component {
	constructor(name, outer, elt, text, lang, exts,tdstate, preExts = []) {
		super(name, outer, tdstate);

		console.log("TDEditor(%s,%o,%o,%o,%o,%o)", name, elt, text, exts, tdstate,preExts)
		this.elt = elt;
		this.exts = exts;
		this.preExts = preExts;

		elt.addEventListener("dragenter", e => {
			if(e.dataTransfer.types.includes("text/plain"))
				e.preventDefault();
		}, true);
		elt.addEventListener("drop", e => {
			this.paste(e.dataTransfer.getData("text/plain"));
			e.preventDefault();
		}, true);
		const state = this.createState(lang, text);
		this.EditorView = EditorView;
		this.view = new EditorView({state: state, parent: elt});
		this.$markField = markField;
		this.$addMark = addMark;
		this._debugState = false;

	}
	
	highlightTree(prompt) {
		const state = this.view.state;
		const doc = state.doc;
		const result = document.createElement('div');
		result.setAttribute('class', 'cm-content');
		let line = null;
		const tree = syntaxTree(this.view.state);
		let pos = 0;


		function newline() {
			if(line != null)
				result.appendChild(line);
			line = document.createElement('div');
			line.setAttribute('class', 'cm-line');
		}
		newline();
		if(prompt)
			line.appendChild(prompt);
		
		function nolight(to) {
			if(to > pos) {
				const text = doc.sliceString(pos,to,"\n");
				const lines = text.split("\n");
				while(lines.length > 0) {
					const lineText = lines.shift();
					if(lineText.length > 0) {
						line.appendChild(document.createTextNode(lineText));
					}
					if(lines.length > 0) {
						newline();
					}
				}
				pos = to;
			}
		}
		function highlight(from,to,classes) {
			if(from > pos) {
				nolight(from);
			}
			pos = to;
			const elt = document.createElement('span');
			elt.setAttribute('class', classes);
			elt.textContent = doc.sliceString(from,to,"\n");
			line.appendChild(elt);
		}
		function join(cls1, cls2) {
			if(cls1 == null)
				return cls2;
			else if(cls2 == null)
				return cls1;
			else
				return cls1 + ' ' + cls2;
		}
		highlightTree(tree, (tag,scope) => 
			join(darkDuckHighlightStyle.match(tag,scope), classHighlightStyle.match(tag,scope)), highlight);
		nolight(doc.length);
		if(line.childElementCount > 0)
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
		this.select();
		this.view.focus();
		return "TDEditor";
	}
	paste(text, cursorAdj = 0) {
		const tr = this.view.state.replaceSelection(text);
		if(cursorAdj != 0) {
			const move = EditorSelection.create(tr.selection.ranges.map(r => {
				if(r.empty) {
					r.from--;
					r.to--;
				} else {
					r.to--;
				}
				return r;
			}), tr.selection.mainIndex);
		}
		tr.scrollIntoView = true;
		this.view.dispatch(tr);
		if(this._after_paste) {
			this._after_paste();
		}
	}
	switchState(newState) {
		if(this._debugState)
			console.log("switchState");
		const oldState = this.view.state;
		this.view.setState(newState);
		if(this._debugState)
			console.log(newState);
		return oldState;
	}
	
	addMark(from, to) {
		markRange(this.view, from, to);
	}
	createState(lang, text, pos) {
		let selection;
		if(this._debugState)
			console.log("createState: ", lang, text, text.length, pos);
		if(pos) {
			if(pos < 0) {
				selection = { anchor: text.length + 1 + pos};
			} else {
				selection = { anchor: pos };
			}
		}
		if(this._debugState)
			console.log("createState: ", text, selection);
		const state = EditorState.create({
			doc: text,
     		extensions: [this.preExts, langConfig(lang), this.exts],
			selection: selection
  		});
		if(this._debugState)
			console.log("createState: ", state);
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

window.turtleduck.createEditor = function(elt,text,lang="java") {
	const outer = elt;
	const elts = elt.getElementsByClassName('wrapper');
	if(elts[0])
		elt = elts[0];
		
	let editor = new TDEditor(outer.id,outer,elt,lang,text,[fontConfig(elt), stdConfig()],window.turtleduck);

	return editor;
}

window.turtleduck.darkDuckStyle = {};
darkDuck[1].module.rules.forEach(rule => {
	window.turtleduck.darkDuckStyle[rule.replace(/\s*{.*$/, "")] = rule.replace(/^[^{]*{/, "{");
});
window.turtleduck.classStyle = {};
for(let x in classHighlightStyle.map) {
	let name = classHighlightStyle.map[x].replace(/cmt/g,'.cmt'), defClass = '.' + darkDuck[1].map[x];
	window.turtleduck.classStyle[name] = window.turtleduck.darkDuckStyle[defClass] || '{}';	
}

darkDuck[1].module.rules[0]


window.turtleduck.classHighlightStyle = classHighlightStyle;
window.turtleduck.darkDuck = darkDuck;

window.turtleduck.createLineEditor = function(elt,text,lang,handler) {
	function enter({ state, dispatch }) {
	    let changes = state.changeByRange(({ from, to }) => {
	        let between =  isBetweenBrackets(state, from);
			console.log("enter key pressed: from=%o, to=%o, between=%o, state=%o", from, to, between, state);
			return { range: {from,to}};
		})
		
		return handler("enter", state);
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
	if(elts[0])
		elt = elts[0];
		
	let editor = new TDEditor(outer.id, outer, elt, text, lang, [
		fontConfig(elt), stdConfig(),
		EditorView.theme({
		  '.cm-lineNumbers .cm-gutterElement':{
			display: "none"
		  }
		}),
		keymap.of([{ key: "ArrowUp", run: arrowUp }, { key: "ArrowDown", run: arrowDown }])
	], window.turtleduck,
	[keymap.of([{ key: "Enter", run: enter, shift: shiftEnter }])]);

	editor._after_paste = () => {
		outer.scrollTop = outer.scrollTopMax;
	};
	return editor;
}
