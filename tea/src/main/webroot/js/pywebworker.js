importScripts('../py/pyodide.js')

console.warn("PyWebWorker init", self);

const routes = {
	init_python: '',
	eval_request: 'ShellService'
}

util = {
	id: function(arg) { return arg; },
	log: function(arg) { console.log(arg); }
};


isMessage = function(msg) {
	if(msg instanceof Map) {
		return msg.has('content') && msg.has('header');
	} else {
		return 'content' in msg && 'header' in msg;
	}
}


waiting_for = null;

clients = [];

/** Whether any clients are already connected */
existing = false;


function connect(port) {
	pymsg = function(e) {
		console.log(e);
		port.postMessage({header: {msg_type: 'python_status', msg_id:"i1"},
			content: { status: e}})
	}
	pyerr = function(e) {
		console.error(e);
		port.postMessage({header: {msg_type: 'python_error', msg_id:"i1"},
			content: { status: e}})
	}
	function send(msg) {
		if(self.debug) {
			console.log("posting: ", msg);	
		}
		port.postMessage(msg);
	}
	const queue = [];
	
	port.onmessage = async function(e) {
	  try {
	    const data = e.data;
	 
		if(data === "ping") {
			console.log("received ping", e);
			port.postMessage("pong");
			return;
		} else if(data === "debug") {
			console.log("enabling debug", e);
			self.debug = true;
			return;
		} else if(data === "!debug") {
			console.log("disabling debug", e);
			self.debug = false;
			return;
		}
		
	     if (!self.loadPyodide.inProgress) {
			queue.push(e);
			console.log("loading pyodide");
			port.postMessage({header: {msg_type: 'python_status', msg_id:"i0"},
			content: { status: 'Loading Pyodide...', wait: true}});
			self.pyodide = await loadPyodide({indexURL : '../py/', fullStdLib: false});
			if(!self.pyodide) {
		    	port.postMessage({header: {msg_type: 'error_reply', msg_id:"e"}, content: {status: 'error', ename: 'InitializationError', evalue : 'loading pyodide failed', traceback : []}}); // if you prefer onerror events
				return;
			}
			console.log(self.pyodide._module); // provoke unhashable type bug
			port.postMessage({header: {msg_type: 'python_status', msg_id:"i1"}, content: { status: 'Pyodide loaded.'}});
			while(queue.length) {
				const msg = queue.shift();
				console.log("processing queue – message: ", msg.data);
				await port.onmessage(msg);
			}
			console.log("pyodide ready!", self.pyodide);
			return;
	    } else if(!self.pyodide) {
			console.log("pyodide not ready, enqueuing:", data);
			queue.push(e);
			return;
		}
		
	    if (data.python) {
		     for (let key of Object.keys(data)) {
		      if (key !== 'python') {
		        // Keys other than python must be arguments for the python script.
		        // Set them on self, so that `from js import key` works.
		        self[key] = data[key];
		      }
		    }
			await self.pyodide.loadPackagesFromImports(content.code);
		    let res = await self.pyodide.runPythonAsync(data.python);
	        port.postMessage({status : 'ok', results : res});
	    } else if (data.header) {
			const msg_id = data.header.msg_id;
			const msg_type = data.header.msg_type;
			const content = data.content;
			console.log("msg_type", msg_type);
			if(msg_type === 'hello') {
				console.log('hello', content);
				const id = self.clients.push(port);
				port.postMessage({header: {msg_type: 'welcome', ref_id: msg_id, msg_id: "r" + msg_id},
					content: {status: 'ok', existing: self.existing, id: id}});
				self.existing = true;
				
			} else if(msg_type === 'goodbye') {
				console.log('goodbye', content);
				const id = self.clients.indexOf(port);
				if(id >= 0) {
					self.clients.splice(id, 1);
				}
			} else if(msg_type === 'shutdown') {
				console.log('shutdown', content);
				const id = self.clients.indexOf(port);
				self.close();
			} else if(msg_type === 'init_python') {
				console.log("init_python:", content.code);
				self._send = send;
			    await self.pyodide.loadPackagesFromImports(content.code);
				let res = await self.pyodide.runPythonAsync(content.code, pymsg, pyerr);
				console.log("Python result: ", res);
	       		port.postMessage({header: {msg_type: 'init_python_reply', ref_id: msg_id, msg_id:"r"+msg_id},
					 content: {status: 'ok'}});
			//} else if(msg_type === 'failure' || msg_type === 'error_reply') {
				// ignore
			} else {
				self._msg = data;
				self._send = send;
		        self.pyodide.runPythonAsync("ShellService.receive()", self.pymsg, self.pyerr).then(res => {
					console.log("Python result: ", res);
					if(res instanceof Map) {
						if(!(res.has('content') && res.has('header'))) {
							res = {header: {msg_type: msg_type + '_reply', ref_id: msg_id, msg_id:"r"+msg_id},
									content: res};
						} else {
							res.get('header').set('ref_id', msg_id);
						}
					} else if (res) {
						res = {header: {msg_type: msg_type + '_reply', ref_id: msg_id, msg_id:"r"+msg_id},
									content: {result: res}};
					}
					if(res)
			      	  port.postMessage(res);
				})
			}
	    }
	  } catch (e) {
	    // if you prefer messages with the error
	    console.log(e)
	    port.postMessage({header: {msg_type: 'error_reply', msg_id:"e"}, content: {status: 'error', ename: e.name, evalue : e.message, traceback : e.stack.split('\n')}}); // if you prefer onerror events
	    // setTimeout(() => { throw err; });
	  }
	}
}


console.warn("Starting worker", self);
if(self.constructor.name === 'SharedWorkerGlobalScope') {
	console.warn("Starting shared worker!");
	self.onconnect = function(e) {
		connect(e.ports[0]);
	}
} else {
	console.warn("Starting dedicated worker!");
	connect(self);
}

