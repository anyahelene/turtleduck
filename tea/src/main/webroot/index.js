import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import jquery from 'jquery';
import animals from './animals.txt';
import { Remarkable } from 'remarkable';
import hljs from 'highlight.js'


//import ace from "ace-builds";
//import "ace-builds/webpack-resolver";
//var ace = require('ace-builds/src-noconflict/ace')


var turtleduck = window['turtleduck'] || {};
window.turtleduck = turtleduck;


//ace.config.loadModule("ace/ext/language_tools", function(m) { turtleduck.editor_language_tools = m; });
//ace.config.loadModule('ace/ext/options', function(m) { turtleduck.editor_options = m; });

const animalList = animals.split(/\s+/).filter(function(a) { return a.length > 0; })
turtleduck.animals = {
	pranimals: animalList.filter(function(a) { return !a.startsWith("-"); }),
	postimals: animalList.filter(function(a) { return !(a.endsWith("-") || a.endsWith(":")); }),

	random: function() {
		const a = this.pranimals[Math.floor(Math.random() * this.pranimals.length)];
		const b = this.postimals[Math.floor(Math.random() * this.postimals.length)];
		return (a + " " + b).replaceAll(/(:|- -?| -)/g, '');
	}
}

function storageAvailable(type) {
	var storage;
	try {
		storage = window[type];
		var x = '__storage_test__';
		storage.setItem(x, x);
		storage.removeItem(x);
		return true;
	}
	catch (e) {
		return e instanceof DOMException && (
			// everything except Firefox
			e.code === 22 ||
			// Firefox
			e.code === 1014 ||
			// test name field too, because code might not be present
			// everything except Firefox
			e.name === 'QuotaExceededError' ||
			// Firefox
			e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
			// acknowledge QuotaExceededError only if there's something already stored
			(storage && storage.length !== 0);
	}
}

turtleduck.hasLocalStorage = storageAvailable('localStorage');
turtleduck.hasSessionStorage = storageAvailable('sessionStorage');
turtleduck.localStorage = storageAvailable('localStorage') ? window.localStorage : {
	dict: {},
	getItem: function(key) { return dict[key]; },
	setItem: function(key, value) { dict[key] = value; },
	removeItem: function(key) { dict[key] = undefined; },
};

turtleduck.sessionStorage = storageAvailable('sessionStorage') ? window.sessionStorage : {
	dict: {},
	getItem: function(key) { return dict[key]; },
	setItem: function(key, value) { dict[key] = value; },
	removeItem: function(key) { dict[key] = undefined; },
};

turtleduck.generateSessionName = function(regen = false) {
	if (regen) {
		turtleduck.sessionName = turtleduck.animals.random();
	}
	else {
		turtleduck.sessionName = turtleduck.sessionStorage.getItem('turtleduck.sessionName') || turtleduck.animals.random();
		turtleduck.sessionStorage.setItem('turtleduck.sessionName', turtleduck.sessionName);
	}
	return turtleduck.sessionName;
}

turtleduck.generateSessionName();

turtleduck.md = new Remarkable();



function ctrl(key) {
	return ['ctrl+' + key, 'command+' + key];
}
function meta(key) {
	return ['alt+' + key, 'meta+' + key];
}
function handleKey(key, button, event) {
	switch (key) {
		case "f6":
			if (jquery("#page").hasClass('figure-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			jquery('#editor textarea').focus();
			break;
		case "f7":
			turtleduck.jshell.focus();
			break;
		case "f8":
			if (jquery("#page").hasClass('main-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			break;
		case "f9":
			//var alone = jquery("#page").hasClass('main-alone');
			//jquery("#page").toggleClass('main-alone', !alone);
			//jquery("#page").toggleClass('main-and-figure', alone);
			var coding = jquery("#page").hasClass('mainly-coding');
			jquery("#page").toggleClass('mainly-coding', !coding);
			jquery("#page").toggleClass('mainly-figure', coding);
			jquery("#page").toggleClass('main-alone', false);
			jquery("#page").toggleClass('main-and-figure', false);
			turtleduck.editor.resize();
			turtleduck.jshell_fitAddon.fit();
			break;
		case "help":
			jquery("#page").toggleClass('show-splash-help');
			break;
		default:
			console.log(key, button, event);
			turtleduck.actions.handle(key, event);
	}
}

turtleduck.handleKey = handleKey;

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

	jquery('[data-toggle]').each(function(index) {
		const button = this;
		const toggleType = jquery(this).attr('data-toggle');
		const ref = jquery(this).attr('href') || jquery(this).attr('data-target');
		const target = jquery(ref);
		if (toggleType == 'collapse') {
			jquery(button).click(function(e) {
				var active = jquery(button).hasClass('open');
				jquery(button).toggleClass('open', !active);
				target.toggleClass('open', !active);
				return false;
			});
		} else if (toggleType == 'button') {
			jquery(button).click(function(e) {
				var active = jquery(button).hasClass('active');
				jquery(button).toggleClass('active', !active);
				target.toggleClass('active', !active);
				return false;
			});
		}
	});

	jquery('[data-reset-onclick]').each(function() {
		console.log("data-reset-onclick");
		console.log(this);
		const cls = jquery(this).attr('data-reset-onclick');
		jquery(this).find('a').each(function() {
			jquery(this).click(function(e) {
				jquery('#page').removeClass(cls);
			});
		});
	});

	jquery('[data-paste]').each(function(index) {
		const link = this;
		const ref = jquery(this).attr('href').replace('#', '') || jquery(this).attr('data-target');
		jquery(link).click(function(e) {
			const target = window.turtleduck[ref];;
			const text = jquery(link).attr('data-paste');
			target.paste(text);
			target.focus();
			return false;
		});
	});

	jquery('[data-from]').each(function() {
		jquery(this).text(turtleduck.sessionStorage.getItem(jquery(this).attr('data-from')) || "");
	});

});


window.SockJS = SockJS;
window.Terminal = Terminal;
window.FitAddon = FitAddon;
window.Mousetrap = Mousetrap;
window.Remarkable = Remarkable;
window.hljs = hljs;
window.$ = jquery;
//window.ace = ace;
