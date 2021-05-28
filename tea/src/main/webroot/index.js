import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import jquery from 'jquery';
import animals from './animals.txt';
import { Remarkable } from 'remarkable';
import hljs from 'highlight.js'
import Dexie from 'dexie';
var async = Dexie.async,
    spawn = Dexie.spawn;

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

turtleduck.db = new Dexie('tddb');
turtleduck.db.version(1).stores({
	paths: 'path,modtime,kind,inode',
	files: '++inode,next' 
});

turtleduck.Path = turtleduck.db.paths.defineClass({
		path: String,
		kind: String,
		inode: Number
});
turtleduck.Path.prototype.log = function() {
		console.log("log path:", JSON.stringify(this));	
}
turtleduck.Path.prototype.save = function() {
		console.log("save path:", JSON.stringify(this));
		return turtleduck.db.paths.put(this);
}
turtleduck.Path.prototype.read = function() {
		console.log("read path:", JSON.stringify(this));
		return turtleduck.db.files.get(this.inode).then(inode => {
			if(inode)
				return inode.read();
			else
				return '';
		});
}
turtleduck.Path.prototype.write = function(newData) {
		console.log("write path:", JSON.stringify(this));
		const path = this;
		return turtleduck.db.transaction('rw', [turtleduck.db.paths, turtleduck.db.files],
			async () => {
				const inode = await turtleduck.db.files.get(path.inode || -1);
				if(inode) {
					path.inode = await inode.write(newData);
				}
				else {
					path.inode = await turtleduck.db.files.add({data: newData});
				}
				path.save();
				console.log("path written:", JSON.stringify(path));					
			});
}
turtleduck.File = turtleduck.db.files.defineClass({
		data: String,
		next: Number,
		modtime: Date,
});
turtleduck.File.prototype.save = function() {
		console.log("save file:", JSON.stringify(this));
		return turtleduck.db.files.put(this);
}
turtleduck.File.prototype.log = function() {
		console.log(JSON.stringify(this));	
}

function patch(data, diff) {
	return "patch(" + data + ", "+ diff + ")";
}

function diff(from, to) {
	return "diff("+ from + ", " + to + ")";
}
turtleduck.File.prototype.read = function() {
		if(this.next > 0) {
			return turtleduck.db.files.get(this.next)
				.then(nextFile => patch(nextFile.read(), this.data));
		} else {
			return Promise.resolve(this.data);
		}
}

turtleduck.File.prototype.write = function(newData) {
		if(this.next > 0) {
			return turtleduck.db.files.get(this.next)
				.then(nextFile => nextFile.write(newData));
		} else if(this.data) {
			let last = this;
			return turtleduck.db.files.add({data: newData, next: 0, date: new Date()}).then(function(id) {
				console.log("overwrite: ", id);
				last.data = diff(newData, last.data);
				last.next = id;
				return turtleduck.db.files.put(last).then(() => Promise.resolve(id));
				
			})
		} else {
			this.data = newData;
			return turtleduck.db.files.put(this);
		}
}

turtleduck.fileSystem = {
	
	list: function() {
		let result = [];
		for(let i = 0; i < turtleduck.localStorage.length; i++) {
			let key = turtleduck.localStorage.key(i);
			if(key.startsWith("file://")) {
				result.push(key.substring(7));
			}
		}
		return result;
	},
	
	read: function(filename) {
		if(!filename.startsWith("file://")) {
			filename = "file://" + filename;
		}
		return turtleduck.localStorage.getItem(filename);
	},
	
	write: function(filename, data) {
		if(!filename.startsWith("file://")) {
			filename = "file://" + filename;
		}
		return turtleduck.localStorage.setItem(filename, data);
	}
	
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
			turtleduck.editor.view.focus();
			break;
		case "f7":
			turtleduck.xtermjs.focus();
			break;
		case "~f8":
			if (jquery("#page").hasClass('main-alone')) {
				jquery("#page").toggleClass('main-alone', false);
				jquery("#page").toggleClass('figure-alone', false);
				jquery("#page").toggleClass('main-and-figure', true);
			}
			jquery('#screen').focus();
			break;
		case "f8":
			const nextLayout = jquery(this).attr('data-target');
			if(nextLayout)
				page.toggleClass(nextLayout, true);
			turtleduck.xtermjs_fitAddon.fit();
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
	
	// focus
	['editor', 'screen', 'console', 'explorer'].forEach(id => {
		const elt = jquery('#'+id);
		elt.focusin(() => elt.toggleClass('focused', true));
		elt.focusout(() => elt.toggleClass('focused', false));
	});
	
	const resizeObserver = new ResizeObserver(entries => {
		console.log('Console size changed');
	})
	resizeObserver.observe(document.getElementById('console'));
})

window.SockJS = SockJS;
window.Terminal = Terminal;
window.FitAddon = FitAddon;
window.Mousetrap = Mousetrap;
window.Remarkable = Remarkable;
window.hljs = hljs;
window.$ = jquery;
//window.ace = ace;
