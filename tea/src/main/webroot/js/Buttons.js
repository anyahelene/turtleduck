import { html, render } from "uhtml";
import { turtleduck } from './TurtleDuck';
let uniqueId = 0;
/** maps name to command object */
const commands = {};
/** maps  */
const buttons = {};
/** maps button id to command name */
const defaultBindings = {};
const ctrlSymbols = { mac: '⌘', caret: '⌃', iso: '⎈' }
const keyboardSymbols = { ctrl: ctrlSymbols.mac, shift: "⇧", tab: "↹", option: "⌥" };
class HonkCommand extends HTMLElement {
	constructor() {
		super();
		console.log("HonkCommand");
	}
	connectedCallback() {
		console.log('element added to page.', this, this.isConnected);
		this.style.display = 'none';
	}
}
const styleRef = 'css/buttons.css';

function loadCommand(element) {
	if (element) {
		const cmd = { name: element.getAttribute('name'), ...element.dataset };
		const binding = element.dataset.bind;
		console.log(cmd, binding);
		if (binding) {
			if (defaultBindings[binding]) {
				console.warn('extra binding for key', binding, ":", cmd, ", original is", defaultBindings[binding]);
			} else {
				defaultBindings[binding] = cmd;
			}
		}
		if (cmd.name) {
			if (commands[cmd.name]) {
				console.warn('extra definition for command', cmd.name, ":", cmd, ", original is", commands[cmd.name]);
			}
			commands[cmd.name] = cmd;
		}
		return cmd;
	}
}

class HonkButton extends HTMLElement {
	constructor(id = '', shortcut = '', command = undefined) {
		super();
		if (id)
			this.id = id;

		if (shortcut)
			this.dataset.shortcut = shortcut;
		if (command)
			this._command = command;
		this.attachShadow({ mode: "open" });

	}

	set command(cmd) {
		this._command = cmd;
		this.update();
	}
	get command() {
		if (this._command) {
			return this._command;
		}

		let cmd = defaultBindings[this.id];
		if (!cmd && cmd !== null) {
			const elt = document.querySelector(`honk-command[data-bind="${this.id}"]`);
			cmd = loadCommand(elt);
		}
		return cmd || { icon: this.dataset.icon || '', text: this.dataset.text || '' };

	}

	async click(e) {
		await turtleduck.handleKey(this.id, this, e);
	}
	connectedCallback() {
		console.log('element added to page.', this, this.isConnected);
		this.update();
	}

	disconnectedCallback() {
		console.log('removed from page.', this);
	}

	adoptedCallback() {
		console.log('moved to new page.', this);
	}

	attributeChangedCallback(name, oldValue, newValue) {
		console.log('element attributes changed.', name, oldValue, newValue);
		this.update();
	}
	styleChanged(ref) {
		console.log('style changed', this, ref);
	}
	template() {
		const command = this.command;
		const text = command.text;
		let icon = command.icon || this.dataset.icon || '';
		if (command.iconSrc || this.dataset.iconSrc)
			icon = html`<img src="${command.iconSrc || this.dataset.iconSrc}" style="height:1em" alt="${icon}" />`;
		const shortcut = this.classList.contains('not-implemented') ? '(not implemented)'
			: command.shortcut || this.dataset.shortcut || '';
		const keys = shortcut.split('+').map(s => html`<span>${keyboardSymbols[s] || s}</span>`)
		const shortcutText = shortcut.replace('ctrl+', '⌘').replace('shift+', '↑');
		const classList = `${icon ? "has-icon" : "no-icon"} ${text ? "has-text" : "no-text"}`;
		console.log(this.id, command, icon, shortcut, shortcutText, keys);
		return html`<link rel="stylesheet" href="${styleRef}">
      <button id="${this.id}" onclick=${this.click} class="${classList}" type="button">
		<span class="bg"></span><span class="icon">${icon}</span><span class="text">${command.text}</span><div class="shortcut">${keys}</div>
	  </button>
    `;
	}

	update() {
		render(this.shadowRoot, this.template());
	}
}

customElements.define("honk-button", HonkButton);
customElements.define("honk-command", HonkCommand);
turtleduck.deps.addListener(styleRef, e => {
	document.querySelectorAll('honk-button').forEach(elt => {
		elt.styleChanged(e);
	});
});

export { HonkButton, commands, buttons, ctrlSymbols, keyboardSymbols };
