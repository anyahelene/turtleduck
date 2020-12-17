import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import jquery from 'jquery';

class MessageRepr {
	constructor(json) {
		this.data = json === undefined ? {} : JSON.parse(json);
	}


	put(key, val) {
		this.data[key] = val;
	}


	toJson() {
		return JSON.stringify(this.data);
	}

	getArray(key) {
		console.log(this.data[key]);
		if (this.data[key] === undefined)
			return [];
		else
			return this.data[key];
	}
	//
	// @Override
	// public <U extends Message> List<U> getList(String key) {
	// return null;
	// }

	get(key, defaultValue) {
		if (this.data[key] === undefined)
			return defaultValue;
		else
			return this.data[key];
	}
}
function ctrl(key) {
	return ['ctrl+' + key, 'command+' + key];
}
function meta(key) {
	return ['alt+' + key, 'meta+' + key];
}
function handleKey(key, button, event) {
	switch (key) {
		case "f6":
			if(jquery("#page").hasClass('figure-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			jquery('#editor textarea').focus();
			break;
		case "f7":
			window.xtermjs_wrap_terminal.focus();
			break;
		case "f8":
			if(jquery("#page").hasClass('main-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			break;
		case "f9":
			var alone = jquery("#page").hasClass('main-alone');
			jquery("#page").toggleClass('main-alone', !alone);
			jquery("#page").toggleClass('main-and-figure', alone);
			break;
		default:
			console.log(key, button, event);
	}
}

jquery(function() {
	jquery('[data-shortcut]').each(function(index) {
		const button = this;
		const handler = function(e) {
			handleKey(button.id, button, e);
			return false;
		};
		const keyHandler = function(e) {
			var active = jquery(button).hasClass('active');
			jquery(button).toggleClass('active', true);

			if (typeof button.timeoutID == "number") {
				window.clearTimeout(button.timeoutID);
			}
			button.timeoutID = window.setTimeout(function() {
				button.timeoutID = undefined;
				jquery(button).toggleClass('active', false);
			}, 300);
			if (!active) {
				handler(e);
			}
			return false;

		};
		const shortcut = jquery(this).attr('data-shortcut');
		jquery(this).attr('data-shortcut-text', shortcut.replace('ctrl+', '⌘').replace('shift+', '↑'));
		Mousetrap.bindGlobal(shortcut, keyHandler);
		jquery(this).click(handler);
	});
});
window.SockJS = SockJS;
window.Terminal = Terminal;
window.FitAddon = FitAddon;
window.MessageRepr = MessageRepr;
window.Mousetrap = Mousetrap;
window.$ = jquery;
