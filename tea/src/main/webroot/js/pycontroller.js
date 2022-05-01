// based on https://pyodide.org/en/stable/usage/webworker.html


class PyController {
	constructor(shared = false, name = undefined) {
		const q = false ? '?' + name : '';
		if (shared) {
			this.pyodideWorker = new SharedWorker('./js/pywebworker.js' + q);
			this.port = this.pyodideWorker.port;
			this.shared = true;
		} else {
			this.pyodideWorker = new Worker('./js/pywebworker.js' + q, name);
			this.port = this.pyodideWorker;
			this.shared = false;
		}
		this.pong = 0;
		this.onmessage(e => { });
	}

	onterminate(handler) {
		this._onterminate = handler;
	}

	onmessage(handler) {
		console.log("onmessage: ", handler)
		this.port.onmessage = msg => {
			if (msg.data === "pong") {
				console.log("received pong", msg);
				this.pong++;
			} else {
				handler(msg);
			}
		};
	}

	ping() {
		this.pong--;
		this.port.postMessage("ping");
	}
	debug(enable) {
		if (enable)
			this.port.postMessage("debug");
		else
			this.port.postMessage("!debug");
	}
	onerror(handler) {
		console.log("onerror: ", handler)
		this.port.onerror = handler;
	}

	postMessage(msg) {
		console.log("postmessage: ", msg)
		this.port.postMessage(msg);
	}

	_post(msg, onSuccess, onError) {
		const _oldOnSuccess = this.port.onmessage;
		const _oldOnError = this.port.onerror;
		this.port.onmessage = m => {
			this.port.onmessage = _oldOnSuccess;
			return onSuccess(m);
		};
		this.port.onerror = m => {
			this.port.onerror = _oldOnError;
			return onError(m);
		};
		this.port.postMessage(msg);
	}

	run(script, context, onSuccess, onError) {
		this._post({
			...context,
			python: script,
		}, onSuccess, onError);
	}
	send(to, msg, onSuccess, onError) {
		this._post({
			_msg: msg,
			to: to,
		}, onSuccess, onError);
	}

	close() {
		if (this.shared) {
			if (this.pyodideWorker) {
				this.port.postMessage({
					header: { msg_type: 'goodbye', msg_id: "exit" },
					content: {}
				});
				this.pyodideWorker = null;
				if (this._onclose) {
					this._onclose();
				}
			}
		} else {
			this.terminate();
		}
	}
	terminate() {
		if (this.pyodideWorker) {
			this.port.postMessage({
				header: { msg_type: 'goodbye', msg_id: "exit" },
				content: {}
			});
			this.pyodideWorker.terminate();
			this.pyodideWorker = null;
			if (this._onterminate) {
				this._onterminate();
			}
		}
	}
}

export { PyController };
