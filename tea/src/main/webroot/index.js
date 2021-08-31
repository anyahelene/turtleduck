import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import jquery from 'jquery';
import animals from './animals.txt';
import hints from './hints.txt';
import { vfs, fileSystem, FileSystem } from './FileSystem';
import { History } from './History';
import { Component } from './Component';
import { TilingWM, TilingWindow} from './TilingWM';
import { PyController } from './js/pycontroller';
import { ShellServiceProxy } from './ShellServiceProxy';
import { MDRender } from './js/MDRender';
import { Camera } from './js/Media';
//import { XTermJS } from './XTermJS';
//import ace from "ace-builds";
//import "ace-builds/webpack-resolver";
//var ace = require('ace-builds/src-noconflict/ace')
import defaultConfig from './config.json';

var turtleduck = window['turtleduck'] || {};
window.turtleduck = turtleduck;
turtleduck.MDRender = MDRender;
turtleduck.Camera = Camera;
turtleduck.Camera.addSubscription('copy','builtin', 'qr','Copy','📋', 'Copy to clipboard');
turtleduck.Camera.addSubscription('copy','builtin', 'camera','Copy','📋', 'Copy to clipboard');
turtleduck.md = new MDRender({});
turtleduck.fileSystem = fileSystem;
turtleduck.vfs = vfs;
turtleduck.history = new History(fileSystem);
turtleduck.defaultConfig = defaultConfig;
turtleduck.configs = [/*override */{}, /*session*/{}, /*user*/{}, /*remote*/{}, defaultConfig];
//turtleduck.config = jquery.extend(true, {}, defaultConfig);
//turtleduck.configSource = {};
turtleduck.pyController = new PyController();
turtleduck.shellServiceProxy = new ShellServiceProxy(turtleduck.pyController);
//turtleduck.mergeConfig = function(config = {}) {
//	turtleduck.config = jquery.extend(true, turtleduck.config, config);
//}
turtleduck.TilingWM = TilingWM;
turtleduck.TilingWindow = TilingWindow;
turtleduck.wm = new TilingWM('mid', 32, 16);

turtleduck._getByPath = function(path, obj) {
	const ps = path.split(".");

	ps.forEach(name => {
		if(obj !== undefined) {
			obj = obj[name];
		}
	});
	
	return obj;		
}
turtleduck._getConfig = function(path, configs) {
	for(var i in configs) {
		const result = turtleduck._getByPath(path, configs[i]);
		if(result !== undefined) {
			return result;
		}
	}
	return undefined;
}
turtleduck.getConfig = function(path) {
	return turtleduck._getConfig(path, turtleduck.configs);
}
turtleduck.setConfig = function(config, source) {
	const src = ['override','session','user','remote','default'].indexOf(source);
	if(src >= 0) {
		turtleduck.configs[src] = jquery.extend(true, turtleduck.configs[src], config);
		console.log("setConfig", config, source, "=>", turtleduck.configs[src]);
	}
}
turtleduck.openCamera = function(config) {
	const elt = document.getElementById('camera');
	if(elt) {
		if(!turtleduck.camera) {
			turtleduck.camera = new turtleduck.Camera();
		} 
		turtleduck.camera.attach(elt);
		elt.classList.add('active');
		return turtleduck.camera.initialize(config);
	}
}

turtleduck.closeCamera = function(now = false) {
	const elt = document.getElementById('camera');
	if(elt) {
		elt.classList.remove('active');		
	}
	if(turleduck.camera) {
		turtleduck.camera.dispose();
		
	}
}

turtleduck.displayPopup = function(title, text, caption, style) {
	const elt = document.getElementById("popup");
	if(elt) {
		elt.className = "popup active " + style;
		elt.querySelector("h1").innerText = title;
		elt.querySelector("blockquote").innerText = text;
		elt.querySelector("figcaption").innerText = caption;

		document.getElementById("page").addEventListener("click",
			(e) => {console.log("f17", e); elt.className = "popup";},
			{once: true});
	}
}
/*
function setConfig(path, dstDict, srcDict, source) {
	console.log("setConfig", path, dstDict, srcDict, source);
	Object.getOwnPropertyNames(srcDict).forEach(prop => {
		console.log("prop: ", prop);
		const value = srcDict[prop];
		const subpath = (path === '' ? '' : path + '.') + prop;
		if(typeof(value) === 'object' && !Array.isArray(value)) {
			console.log("set " + subpath + "…");
			setConfig(subpath, dstDict[prop] || {}, value, source);
		} else if(source === 'user' || turtleduck.configSource[subpath] !== 'user') {
			console.log("set " + subpath, dstDict[prop], "→", value);
			dstDict[prop] = value;
			turtleduck.configSource[subpath] = source;
		} else {
			console.log("not overriding " + subpath + ": " + source + "<=" + turtleduck.configSource[subpath], dstDict[prop], "→", value);

		}
	});
}
turtleduck.setConfig = function(config, source = "user") {
	setConfig('', turtleduck.config, config, source);
}
function pruneConfig(config, defaultConfig) {
	if(defaultConfig === undefined)
		return false;
	var same = true;
	Object.getOwnPropertyNames(config).forEach(prop => {
		console.log("prop: ", prop);
		console.log(JSON.stringify(config[prop]), "==", JSON.stringify(defaultConfig[prop]), "?")
		if(JSON.stringify(config[prop]) === JSON.stringify(defaultConfig[prop])) {
			console.log("yes!");
			delete config[prop];
		} else if(typeof(config[prop]) === 'object'  && !Array.isArray(config[prop])) {
			if(pruneConfig(config[prop], defaultConfig[prop])) {
				delete config[prop];
			} else {
				same = false;
			}
		} else {
			same = false;
		}
	});
	return same;
}
*/
turtleduck.saveConfig = function(source = 'all') {
	try {
		if(source === 'all' || source === 'session') {
			turtleduck.sessionStorage.setItem('turtleduck.sessionConfig', JSON.stringify(turtleduck.configs[1]));
		}
	} catch(e) {
		console.error(e);
	}
	try {
		if(source === 'all' || source === 'user') {
			const dict = jquery.extend(true, {}, turtleduck.configs[2]);
			delete dict.session;
			turtleduck.localStorage.setItem('turtleduck.userConfig', JSON.stringify(dict));
		}
	} catch(e) {
		console.error(e);
	}
	try {
		if(source === 'all' || source === 'remote') {
			const dict = jquery.extend(true, {}, turtleduck.configs[3]);
			delete dict.session;
			turtleduck.localStorage.setItem('turtleduck.remoteConfig', JSON.stringify(dict));
		}
	} catch(e) {
		console.error(e);
	}
}
turtleduck.loadConfig = function() {
	try {
		turtleduck.configs[1] = JSON.parse(turtleduck.sessionStorage.getItem('turtleduck.sessionConfig')) || {};
	} catch(e) {
		console.error(e);
	}
	try {
		turtleduck.configs[2] = JSON.parse(turtleduck.localStorage.getItem('turtleduck.userConfig')) || {};
	} catch(e) {
		console.error(e);
	}
	try {
		turtleduck.configs[3] = JSON.parse(turtleduck.localStorage.getItem('turtleduck.remoteConfig')) || {};
	} catch(e) {
		console.error(e);
	}
}

const saveConfigTimers = {};
function autoSaveConfig(source) {
	if(saveConfigTimers[source]) {
		clearTimeout(saveConfigTimers[source]);
	}
	saveConfigTimers[source] = window.setTimeout(() => {
		console.log("autosaved config ", source);
		turtleduck.saveConfig(source);
		saveConfigTimers[source] = undefined;
	}, 3000);
}
turtleduck.configs[0] = {
	session: { project: 'CoronaPass', private: true, offline: true}
}
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
const hintList = hints.split(/\n/).filter(a => a.length > 0).map(q => {
	const m = q.match(/^(“[^”]*”)\s*[-–—]\s*([^,]+)\s*,?\s*(.*)$/)
	if(m) {
		return [m[1], m[2], m[3]];
	} else {
		return null;
	}
}).filter(q => q);
turtleduck.hints = {
	list: hintList,
	random: () => hintList[Math.floor(Math.random() * hintList.length)]
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


turtleduck.alwaysShowSplashHelp = function(enable = true) {
	if(enable)
		turtleduck.localStorage.setItem('alwaysShowSplashHelp', true);
	else
		turtleduck.localStorage.removeItem('alwaysShowSplashHelp');
};

turtleduck.loadConfig();
if(!turtleduck.getConfig('session.name')) {
	const cfg = {session: {name: turtleduck.animals.random()}};
	turtleduck.setConfig(cfg, 'session');
	turtleduck.saveConfig('session');
}
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

turtleduck.updateInfo = function() {
	jquery('[data-from]').each(function() {
		const froms = (jquery(this).attr('data-from') || "").split("||");
		for(var i = 0; i < froms.length; i++) {
			const from = froms[i].trim();
			if(from) {
				const val = turtleduck.getConfig(from);
				if(val) {
					jquery(this).text(val);
					return;
				}
			}
		}
		jquery(this).text("");
	});
}

function ctrl(key) {
	return ['ctrl+' + key, 'command+' + key];
}
function meta(key) {
	return ['alt+' + key, 'meta+' + key];
}
async function handleKey(key, button, event) {
	const page = jquery('#page');
	function unsetLayout() {
		const layouts = page.attr('data-layouts').split(' ');
		let next = '';
		layouts.forEach((layout,idx) => {
			if(page.hasClass(layout))
				next = layouts[(idx+1)%layouts.length];
			page.toggleClass(layout,false);
		});
		return next;
	}
	var param = undefined;
	var m = key.match(/^([a-z]*):\/\/(.*)$/);
	if(m != null) {
		key = m[1];
		param = m[2];
	}
	console.log("handleKey", key, button, event);
	
	switch (key) {
		case "f6":
			if(turtleduck.pyshell)
				turtleduck.pyshell.focus();
			else if(turtleduck.jshell)
				turtleduck.jshell.focus();
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
		case "f17": {
			const qs = turtleduck.hints.random();

			turtleduck.displayPopup("HINT", qs[0], qs[1], "hints");

			break;
		}
		case "focus": {
			const win = turtleduck[param];
			//console.log("focus:", param, win);
			if(win) {
				win.focus();
			}
			break;
		}
		case "snap": {
			const config = {mode:'camera', once:true};
			if(param) {
				config.fake = param;
				config.mirror = false;
			}
			const r = turtleduck.openCamera(config);
			event.stopPropagation();
			event.preventDefault();
			return r;
		}
		case "qrscan": {
			const config = {mode:'qr', once:true};
			if(param) {
				config.fake = param;
				config.mirror = false;
			}
			const r = turtleduck.openCamera(config);
			event.stopPropagation();
			event.preventDefault();
			return r;			
		}
		case "open-camera": {
				const r = turtleduck.openCamera({mode:'camera'});
				event.stopPropagation();
				event.preventDefault();
				return r;
		}
		case "open-qr": {
				const r = turtleduck.openCamera({mode:'qr'});
				event.stopPropagation();
				event.preventDefault();
				return r;
		}
		default:
			if(button.dataset.showMenu) {
				const elt = document.getElementById(button.dataset.showMenu);
				console.log("show: ", elt);
				elt.classList.add("show");
			} else {
				//console.log(key, button, event);
				const r = turtleduck.actions.handle(key, {button:button}, event);
				//console.log("r =>", r);
				event.stopPropagation();
				event.preventDefault();
				return r;
			}
	}
	event.stopPropagation();
	event.preventDefault();
	return false;
}

turtleduck.handleKey = handleKey;

turtleduck.activateToggle = function(element, toggleClass, target, ...targets) {
	const jqtgt = jquery(target);
	jquery(element).click(function(e) {
		var active = jqtgt.hasClass(toggleClass);
		//console.log(jqtgt, active);
		jqtgt.toggleClass(toggleClass, !active);
		targets.forEach(elt => jquery(elt).toggleClass(toggleClass, !active));
		//console.log("done");
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
			//console.log("clicked:");
			//console.log("lastfocus:", turtleduck.lastFocus);
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
			//console.log("comp:", comp, comp.paste);
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
turtleduck.changeButton = function(button, icon, text) {
	if(typeof(button) === 'string')
		button = document.getElementById(button);
	if(!button)
		return;
	const iconElt = button.querySelector('.icon span');
	const textElt = button.querySelector('.the-text');
	console.log("icon/title elts: ", iconElt, textElt);
	if(iconElt) {
		iconElt.innerText = icon;
	}
	if(textElt) {
		textElt.innerText = text;
	}
	console.log("icon/title elts after: ", iconElt, textElt);
}

turtleduck.trackMouse = function(element, coordElement) {
	element.addEventListener("mousemove", e => {
		e = e || window.event;
		e.preventDefault();
		var p = new DOMPoint(e.clientX, e.clientY);
		var m = element.getScreenCTM().inverse();
		p = p.matrixTransform(m);
		coordElement.textContent = "(" + Math.round(p.x) + ","
				+ Math.round(p.y) + ")";
	});
}

jquery(function() {
	jquery('[data-shortcut]').each(function(index) {
		const button = this;
		const handler = async function(e) {
			return handleKey(button.id, button, e).then(r => {console.log("handleKey", r); return r;});
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
		const shortcut = this.classList.contains('not-implemented') ? '(not implemented)' : jquery(this).attr('data-shortcut');
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


	jquery('[data-menu]').each(function(index) {
		const menu = this;
		const id = this.dataset.menu;
		const select = this.dataset.select;
		this.tabIndex = -1;
		//this.addEventListener("mouseout", e => { menu.classList.remove("show"); });
		this.addEventListener("focusout", e => { menu.classList.remove("show"); });
		this.addEventListener("click", e => {
			const target = e.target;
			if(target.classList.contains("menu-entry")) {
				const data = {select: select};
				const dset = target.dataset;
				for(var k in dset) {
					data[k] = dset[k];
				}
				menu.classList.remove("show");
				const r = turtleduck.actions.handle('menu:'+id, data, event);
			}
			e.stopPropagation();
		});
	});
	
	jquery('[data-tooltip]').each(function(index) {
		const id = this.dataset.tooltip;
		const tipElt = document.createElement("div");
		tipElt.className = "tooltip";
		this.appendChild(tipElt);
		this.style.position = 'relative';
		this.addEventListener("mouseenter", async e => {
			const r = await turtleduck.actions.handle('tooltip:'+id, {}, event);
			tipElt.replaceChildren(r);
			tipElt.classList.add("show");

		});
		this.addEventListener("mouseleave", e => {
			tipElt.classList.remove("show");
		});
	});
	jquery('[data-below]').each(function(index) {
		console.log(index, this);
		const belowElt = this;
		var elt = document.createElement("div");
		elt.className = "ns-resizer";
		//belowElt.insertBefore(elt, this.firstElementChild);
	});
	jquery('[data-left-of]').each(function(index) {
		console.log(index, this);
		const leftElt = this;
		var elt = document.createElement("div");
		elt.className = "ew-resizer";
		const rightElts = this.dataset.leftOf.split(",").map(id => document.getElementById(id)).filter(e => e);
		console.log("leftElt", leftElt, "rightElts", rightElts);
		var column, minColumn, maxColumn;
		const midElt = leftElt.parentElement;
		const mouseMoveListener = function(e) {
			e.stopPropagation();
			e.preventDefault();
			console.log(e.clientX, midElt.clientWidth, 32 * e.clientX / midElt.clientWidth);
			const c = Math.min(maxColumn, Math.max(minColumn, 1 + Math.round(32 * e.clientX / midElt.clientWidth)));
			if(c !== column) {
				column = c;
				console.log("column: ", column)
				leftElt.style.gridColumnEnd = column;
				rightElts.forEach(re => {re.style.gridColumnStart = column;});
			}
		};
		elt.addEventListener("mousedown", e => {
			const style = window.getComputedStyle(leftElt);
			minColumn = 1+Math.max(1, parseInt(style.gridColumnStart));
			maxColumn = 32;
			rightElts.forEach(re => {
				var c = parseInt(window.getComputedStyle(re).gridColumnEnd);
				if(c < 0)
					c = c + 33;
				maxColumn = Math.min(maxColumn, c);
			});
			column = parseInt(style.gridColumnEnd) + 1;
			console.log("resize: min=", minColumn, "col=", column, "max=", maxColumn);
			const controller = new AbortController();
			const signal = controller.signal;
			midElt.addEventListener("mousemove", mouseMoveListener, {
				capture: true, signal: signal
			});
			document.body.addEventListener("mouseup", e2 => {
				console.log("mouseup!");
				controller.abort();
			}, { capture: true, once: true, signal: signal });
			document.body.addEventListener("mouseleave", e2 => {
				console.log("mouseleave!");
				controller.abort();
			}, { capture: false, once: true, signal: signal });
			e.stopPropagation();
			e.preventDefault();			
		});
		//this.insertBefore(elt, this.firstElementChild);
	});
});

var mqlPortrait;
turtleduck.initializeWM = function() {
	if(!mqlPortrait) {
		mqlPortrait = window.matchMedia('(max-width: 899px)');
		mqlPortrait.onchange = turtleduck.initializeWM;
	}
	console.log('media size change: ', mqlPortrait)
	if(mqlPortrait.matches) {
		turtleduck.layoutPrefs = turtleduck.getConfig('prefs.layout-portrait');		
	} else {
		turtleduck.layoutPrefs = turtleduck.getConfig('prefs.layout');
	}
	if(turtleduck.layoutSpec && turtleduck.layoutPrefs)
		turtleduck.wm.initialize(turtleduck.layoutSpec, turtleduck.layoutPrefs);
}

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

	turtleduck.runningOnSafari = window.safari !== undefined;
	
	if(false) {
	if(turtleduck.localStorage.getItem('alwaysShowSplashHelp') || !turtleduck.localStorage.getItem('hasSeenSplashHelp')) {
		jquery('#page').toggleClass('show-splash-help', true);
		turtleduck.localStorage.setItem('hasSeenSplashHelp', true);
	}
	}
	
	const resizeObserver = new ResizeObserver(entries => {
		console.log('Console size changed');
	})
	resizeObserver.observe(document.getElementById('shell'));
	

})

turtleduck._initializationComplete = function(err) {
	if(err) {
		console.error(err);
	}
	turtleduck.layoutSpec = turtleduck.getConfig('layout');
	turtleduck.initializeWM();
	turtleduck.wm.onchange((wm,sizes) => {
		//turtleduck.setConfig({"prefs":{"layout":sizes}}, "session");
		//autoSaveConfig('session');
	});	
}
window.SockJS = SockJS;
window.Mousetrap = Mousetrap;
window.$ = jquery;
//window.ace = ace;
