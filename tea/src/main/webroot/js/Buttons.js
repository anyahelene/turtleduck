import { html, render } from "uhtml";
import { turtleduck } from './TurtleDuck';
import Mousetrap from 'mousetrap';

let uniqueId = 0;
/** maps name to command object */
const commands = {};
/** maps  */
const buttons = {};
/** maps button id to command name */
const defaultBindings = {};
const ctrlSymbols = { mac: '⌘', caret: '⌃', iso: '⎈' }
const keyboardSymbols = { ctrl: ctrlSymbols.mac, shift: "⇧", tab: "↹", option: "⌥" };

/** The honk-command element represents a command that can be bound to a key or button.
 * 
 * @field name The unique id of the command
 * @field data-bind Default key/button binding
 * @field data-icon Default button/menu icon (emoji)
 * @field data-icon-src Image source for icon
 * @field data-alt-icon Alternative button/menu icon
 * @field data-text Default button/menu text
 * @field data-text-LL Localised text
 */
class HonkCommand extends HTMLElement {
	constructor() {
		super();
	}
	connectedCallback() {
		console.log('element added to page.', this, this.isConnected);
		this.style.display = 'none';
		this.name = this.getAttribute('name');
	}
	/** Run this command
	 * 
	 * @param elt (optional) The element that triggered the command
	 * @param event (optional)  The event that triggered the command
	 */
	async run(elt=this, event=null) {
		console.log("command.run", this.name, elt, event);
		await turtleduck.handleKey(this.name, elt, event);
	}
}
const styleRef = 'css/buttons.css';

function loadCommand(element) {
	if (element) {
		const cmd = { name: element.getAttribute('name'), element: element, ...element.dataset };
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

/** A button element that can be bound to a `honk-command`.
 * 
 * @field id The button's unique id
 * @field data-shortcut Default key shortcut
 */
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

		this.clickHandler = this.click.bind(this);
	}

	static get observedAttributes() { return ['class']; }

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
		if(this.classList.contains('disabled')) {
			console.log("click on disabled button: ", this);
			return;
		}
		console.log("click", this);
		/*
		const active = this.classList.contains('active');
		this.classList.add('active');

		// visual effect
		if (typeof this.timeoutId == "number") {
			window.clearTimeout(this.timeoutId);
		}
		this.timeoutId = window.setTimeout(() =>  {
			this.timeoutId = undefined;
			this.classList.remove('active');
		}, 300);

		if (!active) { // debounce
			*/
			await this.run(e);
		//}
	}

	async run(e) {
		const cmd = this.command;
		if(cmd) {
			console.log("Run command", cmd, "button", this, "event", e);
			if(cmd.element) {
				await cmd.element.run(this, e);
			} else {
				await turtleduck.handleKey(cmd.name, this, e);
			}
		} else {
			console.warn("No command bound to ", this, e);
			await turtleduck.handleKey(this.id, this, e);
		} 
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

		this.dataset.currentBinding = command.name;
		if(shortcut && shortcut !== '(not implemented)')
			Mousetrap.bindGlobal(shortcut, this.clickHandler);

		console.log(this.id, command, icon, shortcut, shortcutText, keys);
		return html`<link rel="stylesheet" href="${styleRef}">
      <button id="${this.id}" onclick=${this.clickHandler} class="${classList}" type="button">
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

export { HonkButton, HonkCommand, commands, buttons, ctrlSymbols, keyboardSymbols };
