
import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import jquery from 'jquery';
import animals from './animals.txt';
import hints from './hints.txt';
import { turtleduck } from './js/TurtleDuck';
import { fileSystem, FileSystem } from './js/FileSystem';
import { History } from './js/History';
import { Component } from './js/Component';
import { TilingWM, TilingWindow } from './js/TilingWM';
import { PyController } from './js/pycontroller';
import { ShellServiceProxy } from './js/ShellServiceProxy';
import { MDRender } from './js/MDRender';
import { Camera } from './js/Media';
import { GridDisplayServer } from './js/GridDisplay';
import { html, render } from 'uhtml';
import { Storage } from './js/Storage';
import { timeAgo } from './js/TimeAgo';
import { TShell } from './js/TShell';
import * as lodash from 'lodash-es';
import i18next from 'i18next';
import * as goose from './js/goose';
import getopts from 'getopts';
//import { XTermJS } from './XTermJS';
//import ace from "ace-builds";
//import "ace-builds/webpack-resolver";
//var ace = require('ace-builds/src-noconflict/ace')
import defaultConfig from './config.json';
import { after } from 'lodash-es';

var imports = {
	SockJS, Mousetrap, jquery, animals, hints, fileSystem, FileSystem,
	History, Component, TilingWM, TilingWindow, PyController, ShellServiceProxy,
	MDRender, Camera, GridDisplayServer, html, render, Storage, i18next, goose, timeAgo,
	lodash, TShell, getopts
};
console.log(turtleduck);
globalThis.imports = imports;
globalThis.turtleduck = turtleduck;
globalThis.goose = goose;
turtleduck.MDRender = MDRender;
turtleduck.Camera = Camera;
turtleduck.Camera.addSubscription('copy', 'builtin', 'qr', 'Copy', '📋', 'Copy to clipboard');
turtleduck.Camera.addSubscription('copy', 'builtin', 'camera', 'Copy', '📋', 'Copy to clipboard');
turtleduck.md = new MDRender({});
turtleduck.fileSystem = fileSystem;
turtleduck.gridDisplay = new GridDisplayServer();
turtleduck.history = new History(fileSystem);
turtleduck.defaultConfig = defaultConfig;
turtleduck.configs = [/*override */{}, /*session*/{}, /*user*/{}, /*remote*/{}, defaultConfig];
turtleduck.storage = new Storage();
turtleduck.storage.init().then(ctx => {
	turtleduck.cwd = ctx;
	turtleduck.tshell = new TShell(turtleduck);
});
turtleduck.i18next = i18next;
turtleduck.appendToConsole = function (style) {
	if (turtleduck.shellComponent) {
		const shell = turtleduck.shellComponent.current();
		if (shell) {
			return shell.terminal.appendBlock(style);
		}
	}
}

turtleduck.consolePrinter = function (style) {
	if (turtleduck.shellComponent) {
		const shell = turtleduck.shellComponent.current();
		if (shell) {
			const element = shell.terminal.appendBlock(style);
			return turtleduck.elementPrinter(element, null, () => shell.terminal.scrollIntoView());
		}
	}
}

turtleduck.elementPrinter = function(element, style, afterPrint) {
	const wrapperElt = element.closest('main');
	const outputContainer = element.closest('.terminal-out-container');
	if(typeof style === 'string') {
		element = element.appendChild(html.node`<div class=${style}></div>`);
	}
	if(!afterPrint && wrapperElt && outputContainer) {
		afterPrint = () => {
			outputContainer.scrollTop = 0;
			wrapperElt.scrollTop = wrapperElt.scrollHeight-wrapperElt.offsetHeight;
		};
	}
	console.log(element, style, afterPrint);
	let cr = false;
	return {
		print: text => {
			let old = element.textContent;
			if (cr) {
				old = old.trim().replace(/.+$/, "");
			}
			cr = text.endsWith("\r");
			element.textContent = old + text;
			if(afterPrint) {
				afterPrint();
			} 
		}
	}	
}
//turtleduck.config = jquery.extend(true, {}, defaultConfig);
//turtleduck.configSource = {};

turtleduck.userlog = msg => turtleduck.client.userlog(msg);
//turtleduck.mergeConfig = function(config = {}) {
//	turtleduck.config = jquery.extend(true, turtleduck.config, config);
//}
turtleduck.TilingWM = TilingWM;
turtleduck.TilingWindow = TilingWindow;
turtleduck.wm = new TilingWM('mid', 32, 16);

turtleduck._getByPath = function (path, obj) {
	const ps = path.split(".");

	ps.forEach(name => {
		if (obj !== undefined) {
			obj = obj[name];
		}
	});

	return obj;
}
turtleduck._getConfig = function (path, configs) {
	for (var i in configs) {
		const result = turtleduck._getByPath(path, configs[i]);
		if (result !== undefined) {
			return result;
		}
	}
	return undefined;
}
turtleduck.getConfig = function (path) {
	return turtleduck._getConfig(path, turtleduck.configs);
}
turtleduck.setConfig = function (config, source) {
	const src = ['override', 'session', 'user', 'remote', 'default'].indexOf(source);
	if (src >= 0) {
		turtleduck.configs[src] = jquery.extend(true, turtleduck.configs[src], config);
		console.log("setConfig", config, source, "=>", turtleduck.configs[src]);
	}
}
turtleduck.openCamera = function (config) {
	const elt = document.getElementById('camera');
	if (elt) {
		if (!turtleduck.camera) {
			turtleduck.camera = new turtleduck.Camera();
		}
		turtleduck.camera.attach(elt);
		elt.classList.add('active');
		return turtleduck.camera.initialize(config);
	}
}

turtleduck.closeCamera = function (now = false) {
	const elt = document.getElementById('camera');
	if (elt) {
		elt.classList.remove('active');
	}
	if (turleduck.camera) {
		turtleduck.camera.dispose();

	}
}

turtleduck.openFiles = function (ctx) {
	if (!ctx)
		ctx = turtleduck.cwd;

	var elt;
	return ctx.readdir().then(res => {
		console.log("readdir():", res);
		const data = {
			'files': "Files",
			'fileList': res.map(file => html`<a href="#">${file}</a>`)
		}
		elt = turtleduck.displayDialog("file-dialog", data);
	});
}

turtleduck.unique = 0;
turtleduck.instantiateTemplate = function (templateType, data = {}) {
	const tmpls = document.getElementById("templates");
	console.log("looking for ", templateType, "in", tmpls);
	if (tmpls) {
		const tmpl = tmpls.querySelector(`[data-template=${templateType}]`);
		if (tmpl) {
			console.log("found template", tmpl);
			const tmp = document.createElement("div");
			const instance = tmpl.cloneNode(true);
			const id = turtleduck.unique++;
			tmp.appendChild(instance);
			tmp.querySelectorAll('[id]').forEach(elt => {
				elt.id = `${elt.id}_${id}`;
			});
			tmp.querySelectorAll('[data-text]').forEach(elt => {
				elt.innerText = elt.dataset.text;
			});
			tmp.querySelectorAll('[data-from]').forEach(elt => {
				const arg = data[elt.dataset.from] || '';
				render(elt, arg);
				/*
				console.log(elt, arg);
				if(typeof arg === 'string') {
					elt.innerText = arg;
				} else if(Array.isArray(arg)) {
					elt.replaceChildren(...arg);
				} else {
					elt.replaceChildren(arg);
				}*/
			});
			tmp.querySelectorAll('[data-from-list]').forEach(elt => {
				const args = data[elt.dataset.fromList] || [];
				console.log(elt, args);
				render(elt, html`${args.map(c => html`<li>${c}</li>`)}`);
			});
			if (instance.classList.contains("dismissable")) {
				instance.classList.add("show");
			}
			console.log("instantiated: ", instance);
			return instance;
		}
	} else {
		throw `Template not found: ${templateType}`;
	}
}
turtleduck.displayDialog = function (dialogType, data = {}) {
	const tmpl = turtleduck.instantiateTemplate(dialogType, data);
	if (tmpl) {
		const elt = document.getElementById("mid");
		const insertHere = elt.querySelector(':scope > [data-insert-here]');
		if (insertHere) {
			insertHere.insertBefore(tmpl);
		} else {
			elt.appendChild(tmpl);
		}
		return elt;
	}
}
turtleduck.displayPopup = function (title, text, caption, style) {
	const elt = document.getElementById("popup");
	if (elt) {
		elt.className = "popup dismissable show " + style;
		elt.querySelector("h1").innerText = title;
		elt.querySelector("blockquote").innerText = text;
		elt.querySelector("figcaption").innerText = caption;
	}
	return elt;
}

turtleduck.checkLogin = function (resolve, reject) {
	fetch("login/whoami").then(res => res.json()).then(res => {
		console.log("whoami:", res);
		if (res["status"] === "ok") {
			resolve(res);
		} else if (res["redirect"]) {
			var win;
			turtleduck.checkLogin_callback = () => {
				if (win)
					win.close();
				delete turtleduck.checkLogin_callback;
				resolve({});
			};
			win = window.open(res["redirect"] + "?redirect=login/whoami", "turtleduck-login", "popup");
			console.log("Opened login window: ", win);
		}
	});
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
turtleduck.saveConfig = function (source = 'all') {
	try {
		if (source === 'all' || source === 'session') {
			turtleduck.sessionStorage.setItem('turtleduck.sessionConfig', JSON.stringify(turtleduck.configs[1]));
		}
	} catch (e) {
		console.error(e);
	}
	try {
		if (source === 'all' || source === 'user') {
			const dict = jquery.extend(true, {}, turtleduck.configs[2]);
			delete dict.session;
			turtleduck.localStorage.setItem('turtleduck.userConfig', JSON.stringify(dict));
		}
	} catch (e) {
		console.error(e);
	}
	try {
		if (source === 'all' || source === 'remote') {
			const dict = jquery.extend(true, {}, turtleduck.configs[3]);
			delete dict.session;
			turtleduck.localStorage.setItem('turtleduck.remoteConfig', JSON.stringify(dict));
		}
	} catch (e) {
		console.error(e);
	}
}
turtleduck.loadConfig = function () {
	try {
		turtleduck.configs[1] = JSON.parse(turtleduck.sessionStorage.getItem('turtleduck.sessionConfig')) || {};
	} catch (e) {
		console.error(e);
	}
	try {
		turtleduck.configs[2] = JSON.parse(turtleduck.localStorage.getItem('turtleduck.userConfig')) || {};
	} catch (e) {
		console.error(e);
	}
	try {
		turtleduck.configs[3] = JSON.parse(turtleduck.localStorage.getItem('turtleduck.remoteConfig')) || {};
	} catch (e) {
		console.error(e);
	}
}

const saveConfigTimers = {};
function autoSaveConfig(source) {
	if (saveConfigTimers[source]) {
		clearTimeout(saveConfigTimers[source]);
	}
	saveConfigTimers[source] = window.setTimeout(() => {
		console.log("autosaved config ", source);
		turtleduck.saveConfig(source);
		saveConfigTimers[source] = undefined;
	}, 3000);
}

//ace.config.loadModule("ace/ext/language_tools", function(m) { turtleduck.editor_language_tools = m; });
//ace.config.loadModule('ace/ext/options', function(m) { turtleduck.editor_options = m; });

const animalList = animals.split(/\s+/).filter(function (a) { return a.length > 0; })
turtleduck.animals = {
	pranimals: animalList.filter(function (a) { return !a.startsWith("-"); }),
	postimals: animalList.filter(function (a) { return !(a.endsWith("-") || a.endsWith(":")); }),

	random: function () {
		const a = this.pranimals[Math.floor(Math.random() * this.pranimals.length)];
		const b = this.postimals[Math.floor(Math.random() * this.postimals.length)];
		return (a + " " + b).replaceAll(/(:|- -?| -)/g, '');
	}
}
const hintList = hints.split(/\n/).filter(a => a.length > 0).map(q => {
	const m = q.match(/^(“[^”]*”)\s*[-–—]\s*([^,]+)\s*,?\s*(.*)$/)
	if (m) {
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
	getItem: function (key) { return dict[key]; },
	setItem: function (key, value) { dict[key] = value; },
	removeItem: function (key) { dict[key] = undefined; },
};

turtleduck.sessionStorage = storageAvailable('sessionStorage') ? window.sessionStorage : {
	dict: {},
	getItem: function (key) { return dict[key]; },
	setItem: function (key, value) { dict[key] = value; },
	removeItem: function (key) { dict[key] = undefined; },
};


turtleduck.alwaysShowSplashHelp = function (enable = true) {
	if (enable)
		turtleduck.localStorage.setItem('alwaysShowSplashHelp', true);
	else
		turtleduck.localStorage.removeItem('alwaysShowSplashHelp');
};


turtleduck.loadConfig();
turtleduck.history.sessions().then(ss => {
	if (!turtleduck.getConfig('session.name')) {
		const name = ss.length > 0 ? ss[0].session : turtleduck.animals.random();
		const cfg = { session: { name: name } };
		turtleduck.setConfig(cfg, 'session');
		turtleduck.saveConfig('session');
	}
});

turtleduck.pyController = new PyController(true, turtleduck.getConfig('session.name'));
turtleduck.shellServiceProxy = new ShellServiceProxy(turtleduck.pyController);


turtleduck.tabSelect = function (tabsId, key) {
	let previous = undefined;
	jquery('[data-tab="' + tabsId + '"]').each(function (index, elt) {
		const thisKey = jquery(elt).attr('data-tab-key');
		const isDef = jquery(elt).attr('data-tab-define') == 'true';
		if (isDef) {
			if (jquery(elt).hasClass('selected')) {
				previous = thisKey;
			}
			if (thisKey == key) {
				jquery(elt).toggleClass('selected', true);
			} else {
				jquery(elt).toggleClass('selected', false);
			}
		} else {
			if (thisKey == key) {
				elt.style.setProperty('display', 'block');
			} else {
				elt.style.setProperty('display', 'none');
			}
		}
	});
	return previous;
}

turtleduck.updateInfo = function () {
	jquery('[data-from]').each(function () {
		const froms = (jquery(this).attr('data-from') || "").split("||");
		for (var i = 0; i < froms.length; i++) {
			const from = froms[i].trim();
			if (from) {
				const val = turtleduck.getConfig(from);
				if (val) {
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

function linkClickHandler(e) {
	e.preventDefault();
	const link = e.target.closest("a");
	if (link && link.href) {
		handleKey(link.href, link, e);
	} else {
		console.error("linkClickHandler: no link found", e, link);
	}
}
async function handleKey(key, button, event) {
	const page = jquery('#page');
	function unsetLayout() {
		const layouts = page.attr('data-layouts').split(' ');
		let next = '';
		layouts.forEach((layout, idx) => {
			if (page.hasClass(layout))
				next = layouts[(idx + 1) % layouts.length];
			page.toggleClass(layout, false);
		});
		return next;
	}
	var params = undefined;
	if (event.header && event.header.msg_type === key) { // it's actually a message!
		params = event.content;
		// nop these
		event.stopPropagation = () => { };
		event.preventDefault = () => { };
	}
	var m = key.match(/^([a-z]*):\/\/(.*)$/);
	if (m != null) {
		const url = new URL(key);
		params = { path: url.pathname.replace(/^\/\//, '') };
		url.searchParams.forEach((v, k) => params[k] = v);
		console.log('handleKey decoded url', key, m[1], params);
		key = m[1];
	}
	console.log("handleKey", key, button, event);

	switch (key) {
		case "explorer":
			return turtleduck.openFiles();
		case "code":
			if (turtleduck.pyshell)
				turtleduck.pyshell.focus();
			else if (turtleduck.jshell)
				turtleduck.jshell.focus();
			break;
		case "code-gfx":
			const nextLayout = jquery(this).attr('data-target');
			if (nextLayout)
				page.toggleClass(nextLayout, true);
			turtleduck.terminal.fit();
			break;
		case "help":
			jquery("#page").toggleClass('show-splash-help');
			break;
		case "esc":
			if (page.hasClass('show-splash-help')) {
				page.toggleClass('show-splash-help', false);
			}
			break;
		case "projects":

			break;
		case "quality":
			let code = turtleduck.editor.current().state().sliceDoc(0);
			fetch("https://master-thesis-web-backend-prod.herokuapp.com/analyse", {
				method: "POST",
				headers: {
					'Content-Type': 'text/plain;charset=utf-8',
					'cache-control': 'no-cache',
					'pragma': 'no-cache'
				  },
				body: code}).then(res => {
				if(res.ok) {
					res.json().then(data => {
						document.querySelector("#screen .text").innerText = JSON.stringify(data, null, "    ");
					});
				}
			});
			break;
		case "hints": {
			const qs = turtleduck.hints.random();

			turtleduck.displayPopup("HINT", qs[0], qs[1], "hints");

			break;
		}
		case "focus": {
			const win = turtleduck[params.path];
			//console.log("focus:", param, win);
			if (win) {
				win.focus();
			}
			break;
		}
		case "snap": {
			const config = { mode: 'camera', once: true };
			if (params) {
				config.params = params;
				config.mirror = false;
			}
			const r = turtleduck.openCamera(config);
			event.stopPropagation();
			event.preventDefault();
			return r;
		}
		case "qrscan": {
			const config = { mode: 'qr', once: true };
			if (params) {
				config.params = params;
				config.mirror = false;
			}
			const r = turtleduck.openCamera(config);
			event.stopPropagation();
			event.preventDefault();
			return r;
		}
		case "open-camera": {
			const r = turtleduck.openCamera({ mode: 'camera' });
			event.stopPropagation();
			event.preventDefault();
			return r;
		}
		case "open-qr": {
			const r = turtleduck.openCamera({ mode: 'qr' });
			event.stopPropagation();
			event.preventDefault();
			return r;
		}
		case "tooltip:sessionInfo": {
			const projectName = turtleduck.getConfig('session.project');
			const sessionName = turtleduck.getConfig('session.name');
			const renderShells = s => s.shells.map(sh => html`<span class="icon"><span class=${`shell-type-${sh[0]}`}></span><span class="icon-text">${sh[0]} (${sh[1]})</span></span>`);
			const renderSession = s => { console.log(s); return html`<li class="item-with-icon session-entry"><a onclick=${linkClickHandler} href="${`session://${s.session}`}">${s.session} <span class="time-ago">(${timeAgo(s.date)})</span> <span class="icon-list">${renderShells(s)}</span></a></li>`; };
			return turtleduck.history.sessions().then(ss => {
				render(button, html`<ul class="session-list">${ss.map(renderSession)}</ul>
					<dl>
						<dt>Session</dt><dd>${sessionName}</dd>
						<dt>Project</dt><dd>${projectName ? projectName : html`<input type="text" name="projectName"></input>`}</dd>
					</dl>`);

			});
			/*return fileSystem.list("/home/projects/").map(files -> {
				HTMLElement l = element("ul");
				for(TDFile file : files) {
					String n = file.name();
					l.appendChild(element("li", element("a", attr("href", "?project=" + n), n)));
				}
				list.appendChild(l);
				return Promise.Util.resolve(list);
			}).mapFailure(err -> Promise.Util.resolve(list));
			*/
		}
		case "tooltip:storageInfo": {
			return turtleduck.storage.info().then(info => {
				var askButton = '';
				if (!info.persisted) {
					askButton = html.node`<button id="requestPersistence" type="button">Allow persistent storage</button>`;
					askButton.addEventListener("click", async function (e) {
						return handleKey(askButton.id, askButton, e).then(r => { console.log("handleKey", r); return r; });
					});
				}
				render(button, html.node`<dl><dt>Storage</dt><dd>${info.persisted ? "persistent" : "not persistent"}</dd>
					${info.usage ? html`<dt>Usage</dt><dd>${info.usage}</dd>` : ""}</dl>
					${askButton}`);
				return button;
			});
		}
		case "requestPersistence": {
			return turtleduck.storage.requestPersistence().then(res => {
				button.textContent = res ? "OK!" : "Rejected";
			});
		}
		default:
			if (button.dataset.showMenu) {
				const elt = document.getElementById(button.dataset.showMenu);
				console.log("show: ", elt);
				elt.classList.add("show");
			} else if (button.classList.contains('not-implemented')) {
				if (Math.random() < .5) {
					button.classList.add('disappear');
				} else {
					turtleduck.displayPopup("Warning", "Please do not press this button again.", "", "warning");
				}
				turtleduck.userlog("Sorry! Not implemented. 😕");
			} else {
				//console.log(key, button, event);
				const r = turtleduck.actions.handle(key, { button: button }, event);
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

turtleduck.activateToggle = function (element, toggleClass, target, ...targets) {
	const jqtgt = jquery(target);
	jquery(element).click(function (e) {
		var active = jqtgt.hasClass(toggleClass);
		//console.log(jqtgt, active);
		jqtgt.toggleClass(toggleClass, !active);
		targets.forEach(elt => jquery(elt).toggleClass(toggleClass, !active));
		//console.log("done");
		return false;
	});
}

turtleduck.activateDrag = function (element, type, value) {
	element.addEventListener("dragstart", e => {
		e.dataTransfer.setData(type, value);
		e.preventDefault();
		return false;
	});
}

turtleduck.createComponent = (name, element) => new Component(name, element, turtleduck);

turtleduck.activatePaste = function (element, target, text, cursorAdj = 0, then = null) {
	element.addEventListener("click", e => {
		try {
			//console.log("clicked:");
			//console.log("lastfocus:", turtleduck.lastFocus);
			var comp = null;
			if (target === "currentTarget") {
				if (turtleduck.lastFocus)
					comp = turtleduck.lastFocus;
				else
					comp = turtleduck.shell;
			} else if (turtleduck[target]) {
				comp = turtleduck[target];
			} else
				console.error("paste: ", target, "does not exist");
			//console.log("comp:", comp, comp.paste);
			if (comp !== null && comp.paste)
				comp.paste(text, cursorAdj);
			e.preventDefault();
			if (then !== null) {
				then(element);
			}
		} catch (e) {
			console.error(e);
			throw e;
		}
		return false;
	}, false);
}
turtleduck.currentFocus = null;
turtleduck.lastFocus = null;
turtleduck.changeButton = function (button, icon, text) {
	if (typeof (button) === 'string')
		button = document.getElementById(button);
	if (!button)
		return;
	const iconElt = button.querySelector('.icon span');
	const textElt = button.querySelector('.the-text');
	console.log("icon/title elts: ", iconElt, textElt);
	if (iconElt) {
		iconElt.innerText = icon;
	}
	if (textElt) {
		textElt.innerText = text;
	}
	console.log("icon/title elts after: ", iconElt, textElt);
}

turtleduck.trackMouse = function (element, coordElement) {
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

turtleduck.dismissElements = e => {
	const elts = document.querySelectorAll(".dismissable.show");
	elts.forEach(elt => {
		console.log("Dismiss", elt, "?", e);
		var container = elt;
		if (elt.classList.contains("tooltip")) {
			container = elt.parentElement;
		}
		if (!container.contains(e.target)) {
			console.log("Yes!", elt);
			elt.classList.remove("show");
		} else {
			console.log("No, clicked inside");
		}
	});
};

jquery(function () {
	jquery('button[data-shortcut]').each(function (index) {
		const button = this;
		const handler = async function (e) {
			return handleKey(button.id, button, e).then(r => { console.log("handleKey", r); return r; });
		};
		const keyHandler = function (e) {

			return false;

		};
		const shortcut = this.classList.contains('not-implemented') ? '(not implemented)' : this.dataset.shortcut;
		const shortcutText = shortcut.replace('ctrl+', '⌘').replace('shift+', '↑');
		const icon = this.dataset.icon;
		if (icon) {
			jquery(this).prepend(jquery('<span class="icon"><span>' + icon + '</span></span>'));
		}
		if (shortcut) {
			jquery(this).prepend(jquery('<span></span>').addClass('bg'));
			jquery(this).attr('data-shortcut-text', shortcutText);
			jquery(this).append(jquery('<span class="shortcut"><span>' + shortcut + '</span></span>'))
			Mousetrap.bindGlobal(shortcut, keyHandler);
		}
		//jquery(this).find(".icon").prepend(jquery('<span>'+shortcutText+'</span>'));//.attr('data-shortcut-text', shortcutText)
		jquery(this).click(handler);
	});

	Mousetrap.bindGlobal('esc', e => handleKey('esc', null, e));

	document.documentElement.addEventListener("click", turtleduck.dismissElements);

	jquery('[data-tab-define]').each((idx, elt) => {
		const key = jquery(elt).attr('data-tab-key');
		const tabs = jquery(elt).attr('data-tab');
		jquery(elt).click(e => { turtleduck.tabSelect(tabs, key); });
		if (jquery(elt).hasClass('selected')) {
			turtleduck.tabSelect(tabs, key);
		}
	});

	jquery('[data-toggle]').each(function (index) {
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

	jquery('[data-reset-onclick]').each(function () {
		console.log("data-reset-onclick");
		console.log(this);
		const cls = jquery(this).attr('data-reset-onclick');
		jquery(this).find('a').each(function () {
			jquery(this).click(function (e) {
				jquery('#page').removeClass(cls);
			});
		});
	});

	jquery('[data-paste]').each(function (index) {
		const link = this;
		const ref = jquery(this).attr('href').replace('#', '') || jquery(this).attr('data-target');
		jquery(link).click(function (e) {
			const target = window.turtleduck[ref];;
			const text = jquery(link).attr('data-paste');
			target.paste(text);
			target.focus();
			return false;
		});
	});


	jquery('[data-menu]').each(function (index) {
		const menu = this;
		const id = this.dataset.menu;
		const select = this.dataset.select;
		this.tabIndex = -1;
		//this.addEventListener("mouseout", e => { menu.classList.remove("show"); });
		this.addEventListener("focusout", e => { menu.classList.remove("show"); });
		this.addEventListener("click", e => {
			const target = e.target;
			if (target.classList.contains("menu-entry")) {
				const data = { select: select };
				const dset = target.dataset;
				for (var k in dset) {
					data[k] = dset[k];
				}
				menu.classList.remove("show");
				const r = turtleduck.actions.handle('menu:' + id, data, event);
			}
			e.stopPropagation();
		});
	});

	jquery('[data-tooltip]').each(function (index) {
		const id = this.dataset.tooltip;
		const tipElt = document.createElement("div");
		tipElt.className = "tooltip dismissable";
		this.appendChild(tipElt);
		this.style.position = 'relative';
		var timer;
		var clicked = false;
		const unremove = () => {
			window.clearTimeout(timer);
			timer = undefined;
		};
		const remove = () => {
			if (!clicked) {
				tipElt.classList.add("fade3");
				timer = window.setTimeout(() => {
					tipElt.classList.remove("show");
					timer = undefined;
				}, 3000);
			}
		}
		this.addEventListener("mouseenter", async e => {
			tipElt.classList.remove("fade3");
			if (timer !== undefined) {
				unremove();
			} else if (!tipElt.classList.contains("show")) {
				const r = await handleKey('tooltip:' + id, tipElt, event);
				console.log("tooltip: ", r);
				tipElt.classList.add("show");
				clicked = false;
			}
			/*			if(r) {
							tipElt.replaceChildren(r);
						} else {
							tipElt.replaceChildren([]);
							tipElt.classList.remove("show");			
						}*/

		});
		this.addEventListener("click", async e => {
			if (timer !== undefined) {
				unremove();
			}
			clicked = true;
		});
		this.addEventListener("mouseleave", e => {
			remove();
		});
	});
	jquery('[data-below]').each(function (index) {
		console.log(index, this);
		const belowElt = this;
		var elt = document.createElement("div");
		elt.className = "ns-resizer";
		//belowElt.insertBefore(elt, this.firstElementChild);
	});
	jquery('[data-left-of]').each(function (index) {
		console.log(index, this);
		const leftElt = this;
		var elt = document.createElement("div");
		elt.className = "ew-resizer";
		const rightElts = this.dataset.leftOf.split(",").map(id => document.getElementById(id)).filter(e => e);
		console.log("leftElt", leftElt, "rightElts", rightElts);
		var column, minColumn, maxColumn;
		const midElt = leftElt.parentElement;
		const mouseMoveListener = function (e) {
			e.stopPropagation();
			e.preventDefault();
			console.log(e.clientX, midElt.clientWidth, 32 * e.clientX / midElt.clientWidth);
			const c = Math.min(maxColumn, Math.max(minColumn, 1 + Math.round(32 * e.clientX / midElt.clientWidth)));
			if (c !== column) {
				column = c;
				console.log("column: ", column)
				leftElt.style.gridColumnEnd = column;
				rightElts.forEach(re => { re.style.gridColumnStart = column; });
			}
		};
		elt.addEventListener("mousedown", e => {
			const style = window.getComputedStyle(leftElt);
			minColumn = 1 + Math.max(1, parseInt(style.gridColumnStart));
			maxColumn = 32;
			rightElts.forEach(re => {
				var c = parseInt(window.getComputedStyle(re).gridColumnEnd);
				if (c < 0)
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
turtleduck.initializeWM = function () {
	if (!mqlPortrait) {
		mqlPortrait = window.matchMedia('(max-width: 899px)');
		mqlPortrait.onchange = turtleduck.initializeWM;
	}
	console.log('media size change: ', mqlPortrait)
	if (mqlPortrait.matches) {
		turtleduck.layoutPrefs = turtleduck.getConfig('prefs.layout-portrait');
	} else {
		turtleduck.layoutPrefs = turtleduck.getConfig('prefs.layout');
	}
	if (turtleduck.layoutSpec && turtleduck.layoutPrefs)
		turtleduck.wm.initialize(turtleduck.layoutSpec, turtleduck.layoutPrefs);
}

jquery(document).ready(() => {
	const mqlDesktop = window.matchMedia('(hover: hover) and (pointer: fine)');
	function handleDesktop(mql) {
		if (turtleduck.isDesktop !== undefined) {
			console.warn("Desktop mode changed to ", mql.matches);
		}
		turtleduck.isDesktop = mql.matches;
	}
	handleDesktop(mqlDesktop);
	mqlDesktop.onchange = handleDesktop;
	const mqlDark = window.matchMedia('(prefers-color-scheme: dark)')
	const mqlLight = window.matchMedia('(prefers-color-scheme: light)')
	function handleColorPreference(mql) {
		if (mql.matches) {
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

	if (false) {
		if (turtleduck.localStorage.getItem('alwaysShowSplashHelp') || !turtleduck.localStorage.getItem('hasSeenSplashHelp')) {
			jquery('#page').toggleClass('show-splash-help', true);
			turtleduck.localStorage.setItem('hasSeenSplashHelp', true);
		}
	}

	const resizeObserver = new ResizeObserver(entries => {
		console.log('Console size changed');
	})
	resizeObserver.observe(document.getElementById('shell'));


})

turtleduck._initializationComplete = function (err) {
	if (err) {
		console.error(err);
	}
	turtleduck.layoutSpec = turtleduck.getConfig('layout');
	turtleduck.initializeWM();
	turtleduck.wm.onchange((wm, sizes) => {
		//turtleduck.setConfig({"prefs":{"layout":sizes}}, "session");
		//autoSaveConfig('session');
	});
	turtleduck.client.route('qrscan', msg => {
		console.log('routing qrscan', msg);
		const config = { mode: 'qr', once: true, params: msg, mirror: false };
		return turtleduck.openCamera(config).then(qrcode => {
			console.log('got qrcode', qrcode);
			return Promise.resolve({
				header: { to: msg.header.from, msg_type: 'qrscan_reply', ref_id: msg.header.msg_id, msg_id: 'r' + msg.header.msg_id },
				content: { text: qrcode, status: 'ok' }
			});
		}).catch(err => {
			console.warn('got error instead of qrcode', err);
			return Promise.resolve({
				header: { to: msg.header.from, msg_type: 'qrscan_reply', ref_id: msg.header.msg_id, msg_id: 'r' + msg.header.msg_id },
				content: { error: err, status: 'error' }
			});
		});
		return
	});
	turtleduck.client.route('grid-create', msg => turtleduck.gridDisplay.create(msg));
	turtleduck.client.route('grid-update', msg => turtleduck.gridDisplay.update(msg));
	turtleduck.client.route('grid-style', msg => turtleduck.gridDisplay.style(msg));
	turtleduck.client.route('grid-dispose', msg => turtleduck.gridDisplay.dispose(msg));
}
window.SockJS = SockJS;
window.Mousetrap = Mousetrap;
window.$ = jquery;
//window.ace = ace;
