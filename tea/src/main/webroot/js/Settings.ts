import { defaultsDeep, assign, get, set } from 'lodash-es';
import SubSystem from './SubSystem';

export type ConfigDict = { [cfgName: string]: Config };
export type Config = ConfigDict | Config[] | string | number | boolean;
const configs: ConfigDict[] = [{}, {}, {}, {}, {}];
export const configNames = ['override', 'session', 'user', 'remote', 'default'];


export function getConfig<T extends (string | Config)>(path: string, defaultResult: T): T {
	for (let c of configs) {
		const result = get(c, path);
		if (result !== undefined) {
			//if (typeof result === typeof defaultResult)
			return result as T; // TODO: check
		}
	}
	return defaultResult;
}
export function setConfig(config: Config, source: string | number) {
	let src = typeof source === 'number' ? source : configNames.indexOf(source);
	if (src >= 0) {
		configs[src] = assign(configs[src], config);
		console.log("setConfig", config, source, "=>", configs[src]);
	}
}

export function saveConfig(source = 'all') {
	try {
		if (source === 'all' || source === 'session') {
			sessionStorage.setItem('turtleduck.sessionConfig', JSON.stringify(configs[1]));
		}
	} catch (e) {
		console.error(e);
	}
	try {
		if (source === 'all' || source === 'user') {
			const dict : Config = assign({}, configs[2]);
			delete dict.session;
			localStorage.setItem('turtleduck.userConfig', JSON.stringify(dict));
		}
	} catch (e) {
		console.error(e);
	}
	try {
		if (source === 'all' || source === 'remote') {
			const dict = assign({}, configs[3]);
			delete dict.session;
			localStorage.setItem('turtleduck.remoteConfig', JSON.stringify(dict));
		}
	} catch (e) {
		console.error(e);
	}
}
export function loadConfig() {
	try {
		configs[1] = JSON.parse(sessionStorage.getItem('turtleduck.sessionConfig')) || {};
	} catch (e) {
		console.error(e);
	}
	try {
		configs[2] = JSON.parse(localStorage.getItem('turtleduck.userConfig')) || {};
	} catch (e) {
		console.error(e);
	}
	try {
		configs[3] = JSON.parse(localStorage.getItem('turtleduck.remoteConfig')) || {};
	} catch (e) {
		console.error(e);
	}
}

const saveConfigTimers: { [configName: string]: number } = {};
function autoSaveConfig(source: string) {
	if (saveConfigTimers[source]) {
		clearTimeout(saveConfigTimers[source]);
	}
	saveConfigTimers[source] = window.setTimeout(() => {
		console.log("autosaved config ", source);
		saveConfig(source);
		delete saveConfigTimers[source];
	}, 3000);
}
function storageAvailable(type: string) {
	var storage;
	try {
		storage = type === 'local' ? window.localStorage : window.sessionStorage;
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

export const hasLocalStorage = storageAvailable('localStorage');
export const hasSessionStorage = storageAvailable('sessionStorage');
export const localStorage = storageAvailable('localStorage') ? window.localStorage : {
	dict: {} as ({ [key: string]: any }),

	getItem(key: string) { return this.dict[key]; },

	setItem(key: string, value: any) { this.dict[key] = value; },
	removeItem(key: string) { delete this.dict[key]; },
};

export const sessionStorage = storageAvailable('sessionStorage') ? window.sessionStorage : {
	dict: {} as ({ [key: string]: any }),
	getItem(key: string) { return this.dict[key]; },
	setItem(key: string, value: string) { this.dict[key] = value; },
	removeItem(key: string) { delete this.dict[key]; },
};


const _Settings = {
	getConfig,
	setConfig,
	saveConfig,
	loadConfig,
	localStorage,
	sessionStorage,
	autoSaveConfig,
	configs,
};
export default _Settings;
export type Settings = typeof _Settings;

const systemSpec = {
	api: _Settings,
	depends: [],
	name: 'settings',
	start() {
		loadConfig();
	},
	stop() {
		saveConfig('all');
	}
};

SubSystem.register(systemSpec);