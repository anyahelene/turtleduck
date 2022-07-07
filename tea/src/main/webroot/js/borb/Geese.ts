
export class GooseElement extends HTMLElement {
    constructor() {
        super();
    }
}

export const prefix = 'gs-';
export function tagName(name:string, revision:number = 0) {
    const suffix = revision > 0 ? `_${revision}` : '';
    return `${prefix}${name}${suffix}`;
}

export function assert<T>(cond : T|(() => T), recover : (() => void) | any, ...args : any[]) : T{
    if(typeof cond === 'function') {
        cond = (cond as () => T)();
    }
    if(typeof recover !== 'function') {
        args = [recover, ...args];
        recover = undefined;
    }
    if(!cond) {
        console.error("Assertion failed: ", ...args);
        if(recover) {
            try {
                recover();
            } catch(e) {
                console.error("   also, recovery function failed: ", e);
            }
        }
    }
    return cond;
}