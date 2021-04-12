//import {EditorState} from "@codemirror/state"
//import {EditorView, keymap} from "@codemirror/view"
//import {defaultKeymap} from "@codemirror/commands"
import {EditorState, EditorView, basicSetup} from "@codemirror/basic-setup"
import {java} from "@codemirror/lang-java"
import {oneDark} from "@codemirror/theme-one-dark"


class TDEditor {
	constructor(elt, text) {
		this.elt = elt;
		const state = this.createState(text);
		this.view = new EditorView({state: state, parent: elt});
	}
	
	state() {
		return this.view.state;
	}
	switchState(newState) {
		const oldState = this.view.state;
		this.view.setState(newState);
		return oldState;
	}
	
	createState(text) {
		const state = EditorState.create({
			doc: text,
     		extensions: [basicSetup, java()]
  		});
		return state;
	}

}

window.TDEditor = TDEditor;

window.turtleduck.createEditor = function(elt,text) {
	//let startState = EditorState.create({
	//  doc: "Hello World",
	//  extensions: [keymap.of(defaultKeymap), java()]
	//})
	
	//let view = new EditorView({
	//  state: startState,
	//  parent: elt
	//})
	
	let editor = new TDEditor(elt,text);

	return editor;
}

