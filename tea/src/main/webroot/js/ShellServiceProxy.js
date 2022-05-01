
class ShellServiceProxy {
	constructor(py, target = 'ShellService') {
		this.py = py;
		this.target = target;
	}
	executeRequest(code, silent, store_history, user_expressions, allow_stdin, stop_on_error) {
		const args = { code: code, silent: silent, store_history: store_history, user_expressions: user_expressions, allow_stdin: allow_stdin, stop_on_error: stop_on_error };
		return this.py.run(this.target + '.executeRequest(**js.__args.to_py())', { __args: args });
	}

	inspect(code, cursorPos, detailLevel) {
		const args = { code: code, cursorPos: cursorPos, detailLevel: detailLevel };
		return this.py.run(this.target + '.inspect(**js.__args.to_py())', { __args: args });
	}

	complete(code, cursorPos, detailLevel) {
		const args = { code: code, cursorPos: cursorPos, detailLevel: detailLevel };
		return this.py.run(this.target + '.complete(**js.__args.to_py())', { __args: args });
	}

	refresh() {
		const args = {};
		return this.py.run(this.target + '.refresh(**js.__args.to_py())', { __args: args });
	}

	eval(code, ref, opts) {
		const args = { code: code, ref: ref, opts: opts };
		return this.py.run(this.target + '.eval(**js.__args.to_py())', { __args: args });
	}

}

export { ShellServiceProxy };

