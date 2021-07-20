importScripts('../py/pyodide.js')

const routes = {
	init_python: '',
	eval_request: 'ShellService'
}

pymsg = function(e) {
	console.log(e);
	self.postMessage({header: {msg_type: 'python_status', msg_id:"i1"},
		content: { status: e}})
}
pyerr = function(e) {
	console.error(e);
	self.postMessage({header: {msg_type: 'python_error', msg_id:"i1"},
		content: { status: e}})
}

isMessage = function(msg) {
	if(msg instanceof Map) {
		return msg.has('content') && msg.has('header');
	} else {
		return 'content' in msg && 'header' in msg;
	}
}

send = function(msg) {
	console.log("posting: ", msg);
	self.postMessage(msg);
}

onmessage = async function(e) {
  try {
    const data = e.data;
 
	if(data === "ping") {
		console.log("received ping", e);
		self.postMessage("pong");
		return;
	}
	
    if (typeof self.__pyodideLoading === "undefined") {
      self.postMessage({header: {msg_type: 'python_status', msg_id:"i0"},
		content: { status: 'Loading Pyodide...', wait: true}});
      await loadPyodide({indexURL : '../py/'});
       self.postMessage({header: {msg_type: 'python_status', msg_id:"i1"},
		content: { status: 'Pyodide loaded.'}});
    }
    if (data.python) {
	     for (let key of Object.keys(data)) {
	      if (key !== 'python') {
	        // Keys other than python must be arguments for the python script.
	        // Set them on self, so that `from js import key` works.
	        self[key] = data[key];
	      }
	    }
	    let res = await self.pyodide.runPythonAsync(data.python);
        self.postMessage({status : 'ok', results : res});
    } else if (data.header) {
		const msg_id = data.header.msg_id;
		const msg_type = data.header.msg_type;
		const content = data.content;
		console.log("msg_type", msg_type);
		if(msg_type === 'init_python') {
			let res = await self.pyodide.runPythonAsync(content.code, self.pymsg, self.pyerr);
			console.log("Python result: ", res);
       		self.postMessage({header: {msg_type: 'init_python_reply', ref_id: msg_id, msg_id:"r"+msg_id},
				 content: {status: 'ok'}});
		} else if(msg_type === 'failure' || msg_type === 'error_reply') {
			// ignore
		}else {
			self._msg = data;
	        let res = await self.pyodide.runPythonAsync(routes[msg_type] + ".receive()", self.pymsg, self.pyerr);
			console.log("Python result: ", res);
			if(res instanceof Map) {
				if(!(res.has('content') && res.has('header'))) {
					res = {header: {msg_type: msg_type + '_reply', ref_id: msg_id, msg_id:"r"+msg_id},
							content: res};
				} else {
					res.get('header').set('ref_id', msg_id);
				}
			} else {
				res = {header: {msg_type: msg_type + '_reply', ref_id: msg_id, msg_id:"r"+msg_id},
							content: {result: res}};
			}
	        self.postMessage(res);
		}
    }
  } catch (e) {
    // if you prefer messages with the error
    console.log(e)
    self.postMessage({header: {msg_type: 'error_reply', msg_id:"e"}, content: {status: 'error', ename: e.name, evalue : e.message, traceback : e.stack.split('\n')}}); // if you prefer onerror events
    // setTimeout(() => { throw err; });
  }
}
