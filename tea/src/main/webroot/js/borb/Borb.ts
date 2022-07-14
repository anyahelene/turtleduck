
export class BorbElement extends HTMLElement {
    constructor() {
        super();
    }
}

export const prefix = 'borb-';
export function tagName(name: string, revision: number = 0) {
    const suffix = revision > 0 ? `_${revision}` : '';
    return `${prefix}${name}${suffix}`;
}

export function assert<T>(cond: T | (() => T), recover: (() => void) | any, ...args: any[]): T {
    if (typeof cond === 'function') {
        cond = (cond as () => T)();
    }
    if (typeof recover !== 'function') {
        args = [recover, ...args];
        recover = undefined;
    }
    if (!cond) {
        console.error("Assertion failed: ", ...args);
        if (recover) {
            try {
                recover();
            } catch (e) {
                console.error("   also, recovery function failed: ", e);
            }
        }
    }
    return cond;
}

export function upgradeElements(previousTag: string, newElementConstructor: new () => BorbElement) {
    for (const oldElt of document.getElementsByTagName(previousTag)) {
        const newElt = new newElementConstructor();
        console.log("upgrading", oldElt, "to", newElt);
        oldElt.getAttributeNames().forEach(attrName => {
            console.log('set', attrName, oldElt.getAttribute(attrName));
            newElt.setAttribute(attrName, oldElt.getAttribute(attrName));
        });
        newElt.replaceChildren(...oldElt.childNodes);
        console.log('replacedChildren:', newElt.children);
        oldElt.parentElement.replaceChild(newElt, oldElt);
    }
}