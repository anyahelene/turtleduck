// based on https://pyodide.org/en/stable/usage/webworker.html


class PyController {
	constructor() {
		this.pyodideWorker = new Worker('./js/pywebworker.js');
		this.pong = 0;
		this.onmessage(e => {});
	}
	
	onterminate(handler) {
		this._onterminate = handler;
	}
	
	onmessage(handler) {
		console.log("onmessage: ", handler)
		this.pyodideWorker.onmessage = msg => {
			if(msg.data === "pong") {
				console.log("received pong", msg);
				this.pong++;
			} else {
				handler(msg);
			}
		};
	}
	
	ping() {
		this.pong--;
		this.pyodideWorker.postMessage("ping");
	}
	onerror(handler) {
			console.log("onerror: ", handler)
	this.pyodideWorker.onerror = handler;
	}
	
	postMessage(msg) {
		console.log("postmessage: ", msg)
		this.pyodideWorker.postMessage(msg);
	}
	
	_post(msg, onSuccess, onError) {
		const _oldOnSuccess = this.pyodideWorker.onmessage;
		const _oldOnError = this.pyodideWorker.onerror;
		this.pyodideWorker.onmessage = m => {
			this.pyodideWorker.onmessage = _oldOnSuccess;
			return onSuccess(m);
		};
		this.pyodideWorker.onerror = m => {
			this.pyodideWorker.onerror = _oldOnError;
			return onSuccess(m);
		};
		this.pyodideWorker.postMessage(msg);
	}
	
	run(script, context, onSuccess, onError) {
		this._post({
 	       ...context,
 	       python: script,
 	   }, onSuccess, onError);
	}
	send(to, msg, onSuccess, onError){
		this._post({
       		_msg: msg,
        	to: to,
    	}, onSuccess, onError);
	}
	
	terminate() {
		if(this.pyodideWorker) {
			this.pyodideWorker.terminate();
			this.pyodideWorker = null;
			if(this._onterminate) {
				this._onterminate();
			}
		}
	}
}

export { PyController };
