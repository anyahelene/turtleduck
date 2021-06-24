import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import jquery from 'jquery';
import animals from './animals.txt';
import { Remarkable } from 'remarkable';
import hljs from 'highlight.js'
import { fileSystem, FileSystem } from './FileSystem';
import { History } from './History';
import { Component } from './Component';
//import { XTermJS } from './XTermJS';
//import ace from "ace-builds";
//import "ace-builds/webpack-resolver";
//var ace = require('ace-builds/src-noconflict/ace')


var turtleduck = window['turtleduck'] || {};
window.turtleduck = turtleduck;

turtleduck.fileSystem = fileSystem;
turtleduck.history = new History(fileSystem);


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

turtleduck.alwaysShowSplashHelp = function(enable = true) {
	if(enable)
		turtleduck.localStorage.setItem('alwaysShowSplashHelp', true);
	else
		turtleduck.localStorage.removeItem('alwaysShowSplashHelp');
};

turtleduck.tabSelect = function(tabsId, key) {
	let previous = undefined;
	jquery('[data-tab="'+tabsId+'"]').each(function(index,elt) {
		const thisKey = jquery(elt).attr('data-tab-key');
		const isDef = jquery(elt).attr('data-tab-define') == 'true';
		if(isDef) {
			if(jquery(elt).hasClass('selected')) {
				previous = thisKey;
			}
			if(thisKey == key) {
				jquery(elt).toggleClass('selected', true);
			} else {
				jquery(elt).toggleClass('selected', false);
			}
		} else {
			if(thisKey == key) {
				elt.style.setProperty('display', 'block');
			} else {
				elt.style.setProperty('display', 'none');
			}			
		}
	});
	return previous;
}



function ctrl(key) {
	return ['ctrl+' + key, 'command+' + key];
}
function meta(key) {
	return ['alt+' + key, 'meta+' + key];
}
function handleKey(key, button, event) {
	const page = jquery('#page');
	function unsetLayout() {
		const layouts = page.attr('data-layouts').split(' ');
		let next = '';
		foreach((layout,idx) => {
			if(page.hasClass(layout))
				next = layouts[(idx+1)%layouts.length];
			page.toggleClass(layout,false);
		});
		return next;
	}
	
	switch (key) {
		case "f6":
			unsetLayout();
			page.toggleClass('main-and-figure', true);
			if(turtleduck.editor)
				turtleduck.editor.focus();
			break;
		case "f7":
			if(turtleduck.shell)
				turtleduck.shell.focus();
			break;
		case "~f8":
			if (jquery("#page").hasClass('main-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			turtleduck.screen.focus();
			break;
		case "f8":
			const nextLayout = jquery(this).attr('data-target');
			if(nextLayout)
				page.toggleClass(nextLayout, true);
			turtleduck.terminal.fit();
			break;
		case "help":
			jquery("#page").toggleClass('show-splash-help');
			break;
		case "esc":
			if(page.hasClass('show-splash-help')) {
				page.toggleClass('show-splash-help', false);
			}
			break;
		default:
			console.log(key, button, event);
			turtleduck.actions.handle(key, event);
	}
}

turtleduck.handleKey = handleKey;

turtleduck.activateToggle = function(element, toggleClass, target, ...targets) {
	const jqtgt = jquery(target);
	jquery(element).click(function(e) {
		var active = jqtgt.hasClass(toggleClass);
		console.log(jqtgt, active);
		jqtgt.toggleClass(toggleClass, !active);
		targets.forEach(elt => jquery(elt).toggleClass(toggleClass, !active));
		console.log("done");
		return false;
	});
}

turtleduck.activateDrag = function(element, type, value) {
	element.addEventListener("dragstart", e => {
		e.dataTransfer.setData(type, value);
		e.preventDefault();
		return false;
	});
}

turtleduck.createComponent = (name, element) => new Component(name, element, turtleduck);

turtleduck.activatePaste = function(element, target, text, cursorAdj = 0, then = null) {
	element.addEventListener("click", e => {
		try {
			console.log("clicked");
			var comp = null;
			if(target === "currentTarget") {
				if(turtleduck.lastFocus)
					comp = turtleduck.lastFocus;
				else
					comp = turtleduck.shell;
			} else if(turtleduck[target]) {
				comp = turtleduck[target];
			} else
				console.error("paste: ", target, "does not exist");
			console.log("comp:", comp, comp.paste);
			if(comp !== null && comp.paste)
				comp.paste(text, cursorAdj);
			e.preventDefault();
			if(then !== null) {
				then(element);
			}
		} catch(e) {
			console.error(e);
			throw e;
		}
		return false;
	}, false);
}
turtleduck.currentFocus = null;
turtleduck.lastFocus = null;


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
		const shortcutText = shortcut.replace('ctrl+', '⌘').replace('shift+', '↑');
		jquery(this).prepend(jquery('<span class="icon"><span>'+jquery(this).attr('data-icon')+'</span></span>'))
		jquery(this).prepend(jquery('<span></span>').addClass('bg'));
		jquery(this).attr('data-shortcut-text', shortcutText);
		jquery(this).append(jquery('<span class="shortcut"><span>'+shortcut+'</span></span>'))
		//jquery(this).find(".icon").prepend(jquery('<span>'+shortcutText+'</span>'));//.attr('data-shortcut-text', shortcutText)
		Mousetrap.bindGlobal(shortcut, keyHandler);
		jquery(this).click(handler);
	});
	
	Mousetrap.bindGlobal('esc', e => handleKey('esc', null, e));

	jquery('[data-tab-define]').each((idx,elt) => {
		const key = jquery(elt).attr('data-tab-key');
		const tabs = jquery(elt).attr('data-tab');
		jquery(elt).click(e => { turtleduck.tabSelect(tabs, key); });
		if(jquery(elt).hasClass('selected')) {
			turtleduck.tabSelect(tabs, key);
		}
	});
	
	jquery('[data-toggle]').each(function(index) {
		const button = this;
		const toggleType = jquery(this).attr('data-toggle');
		const ref = jquery(this).attr('href') || jquery(this).attr('data-target');
		const target = jquery(ref);
		turtleduck.activateToggle(this, toggleType, ref);
	/*	if (toggleType == 'collapse') {
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
		}*/
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

jquery(document).ready(() => {
	const mqlDark = window.matchMedia('(prefers-color-scheme: dark)')
	const mqlLight = window.matchMedia('(prefers-color-scheme: light)')
	function handleColorPreference(mql) {
		if(mql.matches) {
			const dark = mql.media.endsWith('dark)');
			jquery('#page').toggleClass('light', !dark);
			jquery('#page').toggleClass('dark', dark);
			console.log(mql);
			console.log(document.getElementById('page').classList);
		}	
	}

	handleColorPreference(mqlDark);
	//handleColorPreference(mqlLight);
	mqlDark.onchange = handleColorPreference;
	mqlLight.onchange = handleColorPreference;
	
	if(turtleduck.localStorage.getItem('alwaysShowSplashHelp') || !turtleduck.localStorage.getItem('hasSeenSplashHelp')) {
		jquery('#page').toggleClass('show-splash-help', true);
		turtleduck.localStorage.setItem('hasSeenSplashHelp', true);
	}
	
	const resizeObserver = new ResizeObserver(entries => {
		console.log('Console size changed');
	})
	resizeObserver.observe(document.getElementById('shell'));
})

window.SockJS = SockJS;
window.Mousetrap = Mousetrap;
window.Remarkable = Remarkable;
window.hljs = hljs;
window.$ = jquery;
//window.ace = ace;
