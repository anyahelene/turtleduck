export const turtleduck = window['turtleduck'] || {};

turtleduck.deps = {
	dependencies: {},
	addListener: function (dep, lis) {
		const list = this.dependencies[dep] || [];
		list.push(lis);
		this.dependencies[dep] = list;
	},
	trigger: function (dep) {
		const list = this.dependencies[dep] || [];
		list.forEach(lis => lis(dep));
	}
};

/** TODO: EyeDropper https://developer.mozilla.org/en-US/docs/Web/API/EyeDropper */
export async function eyeDropper() {
	if (globalThis.EyeDropper) {
		const dropper = new EyeDropper();
		const result = await dropper.open(); // .open({ signal: abortController.signal });
		if (result && result.sRGBHex) {
			return result.sRGBHex;
		}
	}
}
