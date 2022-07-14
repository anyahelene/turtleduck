
import SockJS from 'sockjs-client';
import Mousetrap from 'mousetrap';
import 'mousetrap/plugins/global-bind/mousetrap-global-bind';
import animals from './animals.txt';
import hints from './hints.txt';
import { turtleduck } from './js/TurtleDuck';
import { fileSystem, FileSystem } from './js/FileSystem';
import history from './js/History';
import { Component } from './js/Component';
import { TilingWM, TilingWindow } from './js/TilingWM';
import { PyController } from './js/pycontroller';
import { ShellServiceProxy } from './js/ShellServiceProxy';
import { MDRender } from './js/borb/MDRender';
import { Camera } from './js/Media';
import { GridDisplayServer } from './js/GridDisplay';
import { html, render } from 'uhtml';
import { Storage } from './js/Storage';
import { timeAgo } from './js/TimeAgo';
import { TShell } from './js/TShell';
import * as lodash from 'lodash-es';
import i18next from 'i18next';
import borb from './js/borb';
import getopts from 'getopts';
import defaultConfig from './config.json';
import SubSystem from './js/SubSystem';
import Settings from './js/Settings';

var imports = {
	SockJS, Mousetrap, animals, hints, fileSystem, FileSystem,
	history, Component, TilingWM, TilingWindow, PyController, ShellServiceProxy,
	MDRender, Camera, GridDisplayServer, html, render, Storage, i18next, borb, timeAgo,
	lodash, TShell, getopts, SubSystem, Settings
};

console.log(turtleduck);
globalThis.imports = imports;
globalThis.turtleduck = turtleduck;
globalThis.borb = borb;
turtleduck.mdRender = MDRender;
turtleduck.Camera = Camera;
turtleduck.Camera.addSubscription('copy', 'builtin', 'qr', 'Copy', '📋', 'Copy to clipboard');
turtleduck.Camera.addSubscription('copy', 'builtin', 'camera', 'Copy', '📋', 'Copy to clipboard');
turtleduck.md = new MDRender({});
turtleduck.fileSystem = fileSystem;
turtleduck.gridDisplay = new GridDisplayServer();
turtleduck.defaultConfig = defaultConfig;

Object.defineProperty(turtleduck, 'cwd', { get: () => turtleduck.storage.cwd });
turtleduck.getConfig = (...args) => turtleduck.settings.getConfig(...args);
turtleduck.setConfig = (...args) => turtleduck.settings.setConfig(...args);
turtleduck.saveConfig = (...args) => turtleduck.settings.saveConfig(...args);
SubSystem.setup(turtleduck);
SubSystem.waitFor('settings').then((sys) => {
	console.warn("CONFIGS:", sys.api.configs);
	sys.api.configs[4] = defaultConfig;
});

turtleduck.i18next = i18next;


turtleduck.TilingWM = TilingWM;
turtleduck.TilingWindow = TilingWindow;
turtleduck.wm = new TilingWM('mid', 32, 16);
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

SubSystem.waitFor('history').then(async () => {
	const ss = await turtleduck.history.sessions();

	if (!turtleduck.settings.getConfig('session.name')) {
		const name = ss.length > 0 ? ss[0].session : turtleduck.animals.random();
		const cfg = { session: { name: name } };
		turtleduck.settings.setConfig(cfg, 'session');
		turtleduck.settings.saveConfig('session');
	}
}
);

turtleduck.pyController = new PyController(true, turtleduck.getConfig('session.name'));
turtleduck.shellServiceProxy = new ShellServiceProxy(turtleduck.pyController);


turtleduck.tabSelect = function (tabsId, key) {
	let previous = undefined;
	document.querySelectorAll('[data-tab="' + tabsId + '"]').forEach(elt => {
		const thisKey = elt.dataset.tabKey;
		const isDef = elt.dataset.tabDefine === 'true';
		if (isDef) {
			if (elt.classList.contains('selected')) {
				previous = thisKey;
			}
			elt.classList.toggle('selected', thisKey === key);
		} else {
			if (thisKey === key) {
				elt.style.setProperty('display', 'block');
			} else {
				elt.style.setProperty('display', 'none');
			}
		}
	});
	return previous;
}

turtleduck.updateInfo = function () {
	document.querySelectorAll('[data-from]').forEach(elt => {
		const froms = (elt.dataset.from || "").split("||");
		for (var i = 0; i < froms.length; i++) {
			const from = froms[i].trim();
			if (from) {
				const val = turtleduck.getConfig(from);
				if (val) {
					elt.innerText = val;
					return;
				}
			}
		}
		elt.innerText = "";
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
	const page = document.getElementById('page');

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
			break;
		case "help":
			document.getElementById("page").classList.toggle('show-splash-help');
			break;
		case "esc":
			document.getElementById("page").classList.toggle('show-splash-help', false);
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
				body: code
			}).then(res => {
				if (res.ok) {
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
	element.addEventListener('click', e => {
		console.log("activateToggle", target, targets);
		let active = target.classList.contains(toggleClass);
		if (toggleClass.startsWith('+')) {
			active = false;
		} else if (toggleClass.startsWith('-')) {
			active = true;
		};
		toggleClass = toggleClass.replaceAll(/^\+|-/g, '');
		target.classList.toggle(toggleClass, !active);
		targets.forEach(elt => elt.classList.toggle(toggleClass, !active));
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

window.addEventListener('DOMContentLoaded', loadedEvent => {
	Mousetrap.bindGlobal('esc', e => handleKey('esc', null, e));

	SubSystem.register({
		name: 'dom'
	});

	document.documentElement.addEventListener("click", turtleduck.dismissElements);

	document.querySelectorAll('[data-tab-define]').forEach(elt => {
		const key = elt.dataset.tabKey;
		const tabs = elt.dataset.tab;
		elt.addEventListener('click', e => { turtleduck.tabSelect(tabs, key); });
		if (elt.classList.contains('selected')) {
			turtleduck.tabSelect(tabs, key);
		}
	});

	document.querySelectorAll('[data-toggle]').forEach(button => {
		const toggleType = button.dataset.toggle;
		const ref = button.getAttribute('href') || button.dataset.target;
		turtleduck.activateToggle(button, toggleType, document.querySelector(ref));
	});

	document.querySelectorAll('[data-paste]').forEach(link => {
		const ref = link.getAttribute('href').replace('#', '') || link.dataset.target;
		turtleduck.activatePaste(link, ref, link.dataset.paste);
	});

	document.querySelectorAll('[data-tooltip]').forEach(elt => {
		const id = elt.dataset.tooltip;
		const tipElt = document.createElement("div");
		tipElt.className = "tooltip dismissable";
		elt.appendChild(tipElt);
		elt.style.position = 'relative';
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
		elt.addEventListener("mouseenter", async e => {
			tipElt.classList.remove("fade3");
			if (timer !== undefined) {
				unremove();
			} else if (!tipElt.classList.contains("show")) {
				const r = await handleKey('tooltip:' + id, tipElt, event);
				console.log("tooltip: ", r);
				tipElt.classList.add("show");
				clicked = false;
			}
		});
		elt.addEventListener("click", async e => {
			if (timer !== undefined) {
				unremove();
			}
			clicked = true;
		});
		elt.addEventListener("mouseleave", e => {
			remove();
		});
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

window.addEventListener('DOMContentLoaded', loadedEvent => {
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
			document.querySelector('#page').classList.toggle('light', !dark);
			document.querySelector('#page').classList.toggle('dark', dark);
			console.log(mql);
			console.log(document.getElementById('page').classList);
		}
	}
	handleColorPreference(mqlDark);
	//handleColorPreference(mqlLight);
	mqlDark.onchange = handleColorPreference;
	mqlLight.onchange = handleColorPreference;

	turtleduck.runningOnSafari = window.safari !== undefined;

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
	});
	turtleduck.client.route('grid-create', msg => turtleduck.gridDisplay.create(msg));
	turtleduck.client.route('grid-update', msg => turtleduck.gridDisplay.update(msg));
	turtleduck.client.route('grid-style', msg => turtleduck.gridDisplay.style(msg));
	turtleduck.client.route('grid-dispose', msg => turtleduck.gridDisplay.dispose(msg));
}
window.SockJS = SockJS;
window.Mousetrap = Mousetrap;

import framesStyle from  './css/frames.scss';
import buttonStyle from './css/buttons.scss';
//import markdownStyle from './css/markdown.scss';

if (import.meta.webpackHot) {
	console.warn("WebpackHot enabled");
	turtleduck.webpackHot =  import.meta.webpackHot;
    import.meta.webpackHot.accept(['./css/frames.scss','./css/buttons.scss', './css/markdown.scss'], function (outdated) {
		outdated.forEach(dep => {
			turtleduck.styles.update(dep.replace('./','').replace('.scss', '.css'));
		});
	}, (err, context) => {
		console.error("HMR failed:", err, context);
	});
  //  import.meta.webpackHot.accept('./css/frames.scss?raw', function (...args) {
//		console.warn("frames", args);
//	});
}