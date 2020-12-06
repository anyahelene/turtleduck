import SockJS from 'sockjs-client';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';


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
		if(this.data[key] === undefined)
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
		if(this.data[key] === undefined)
			return defaultValue;
		else
			return this.data[key];
	}
}

window.SockJS = SockJS;
window.Terminal = Terminal;
window.FitAddon = FitAddon;
window.MessageRepr = MessageRepr;
