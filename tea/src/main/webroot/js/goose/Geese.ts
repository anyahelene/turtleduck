
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
