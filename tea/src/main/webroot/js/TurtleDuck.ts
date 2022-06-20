import { StorageContext } from './Storage';
import type { History, HistorySession } from './History';
import type { Settings } from './Settings';
import SubSystem from './SubSystem';
export { History, HistorySession };
declare global {
	interface Window {
		EyeDropper: new () => {
			open(): Promise<{ sRGBHex: string } | undefined>;
		}
	}
}

function proxy<Name extends keyof TurtleDuck, Type extends TurtleDuck[Name]>(name: Name, subsys?: string): Type {
	if (subsys === undefined)
		subsys = name;
	const obj = new Proxy({} as Type, {
		get(target, p, receiver) {
			console.trace(`using proxy for ${name}.${String(p)}`);
			const funcObj: any = {
				async [p](...args: any[]) {
					await SubSystem.waitFor(subsys);
					const api = turtleduck[name];
					if (api) {
						const fn = api[p];
						if (typeof fn === 'function') {
							console.trace(`calling proxy for ${name}.${String(p)}`);
							return await fn(args);
						}
					}
					throw new TypeError(`${String(p)} is not a function`);
				}
			}
			return funcObj[p];
		}
	});
	return obj;
}
function wrap<Type, Prop extends keyof Type, R, F extends Type[Prop] & ((...args: any[]) => Promise<R>)>(name: Prop): (...args: Parameters<F>) => Promise<R> {
	return async (...args: Parameters<F>): Promise<R> => {
		await SubSystem.waitFor('');
		const obj: Type = turtleduck['history'] as unknown as Type;
		if (obj) {
			const fn = obj[name] as F;
			return await fn(...args);
		}
	};
};
type AsyncMethodOf<Type, Name extends keyof Type> = Type[Name] extends ((...args: infer Args) => Promise<infer R>) ? [Args, R] : never;
type IsAsync<Type, Name extends keyof Type> = Type[Name] extends ((...args: infer Args) => Promise<infer R>) ? Name : never;
type OnlyPromises<Type> = {
	[Property in keyof Type as IsAsync<Type, Property>]: Type[Property];
}

type foo = AsyncMethodOf<History, 'get'>;
let bar: foo[0];
function wrap2<Type, Name extends keyof OnlyPromises<Type>>(propName: Name, subsys: string)
	: (...args: AsyncMethodOf<Type, Name>[0]) => Promise<AsyncMethodOf<Type, Name>[1]> {
	type Args = AsyncMethodOf<Type, Name>[0];
	type Ret = Promise<AsyncMethodOf<Type, Name>[1]>;
	type Fn = (...args: Args) => Ret;
	return async (...args: Args): Ret => {
		await SubSystem.waitFor('');
		const obj: Type = turtleduck[subsys] as unknown as Type;
		if (obj && typeof obj[propName] === 'function') {
			const fn = obj[propName] as unknown as Fn;
			return await fn(...args);
		}
		throw Error(`SubSystem not ready: '${subsys}'`)
	};
};
type phist = OnlyPromises<StorageContext>;
let foo: phist;

wrap2<History, 'get'>('get', 'history');


interface TurtleDuck {
	eyeDropper(): Promise<string | undefined>,
	handleKey(key: string, button?: HTMLElement, event?: Event): Promise<any>;
	client: any;
	userlog(msg: string): void;
	cwd: StorageContext;
	history: History;
	settings: Settings;
	makeProxy: typeof proxy;
}
export const turtleduck: TurtleDuck = {
	/** TODO: EyeDropper https://developer.mozilla.org/en-US/docs/Web/API/EyeDropper */
	async eyeDropper(): Promise<string | undefined> {
		if (window.EyeDropper) {
			const dropper = new window.EyeDropper();
			const result = await dropper.open(); // .open({ signal: abortController.signal });
			if (result && result.sRGBHex) {
				return result.sRGBHex;
			}
		}
		return Promise.resolve(undefined);
	},
	async handleKey(key, button, event) { return Promise.resolve(); },
	userlog(msg) { turtleduck.client.userlog(msg); },
	history: proxy('history'),
	cwd: proxy('cwd', 'storage'),
	makeProxy: proxy
} as TurtleDuck;

turtleduck['phistory'] = proxy('history');



