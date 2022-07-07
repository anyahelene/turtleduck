import SubSystem from '../SubSystem';
import { BorbElement, tagName } from './Borb';
import { Hole, html, render } from "uhtml";
import { turtleduck } from '../TurtleDuck';
import type { MDRender } from './MDRender';
const styleRef = 'css/markdown.css';

class BorbDocument extends HTMLElement {
    static tag = tagName('document');
    styleChanged() {
        throw new Error('Method not implemented.');
    }
    textElement: HTMLElement;
    filename: string;
    mdRender: any;
    constructor() {
        super();
        this.textElement = document.createElement('section');
        this.textElement.classList.add('text');
    }

    displayText(filename?: string, title?: string, text?: string, closeable = false) {
        if (!title && !this.title) {
            if (filename) {
                this.title = filename.replace(/^.*\//, "");
            } else
                this.title = "Markdown";
        }
        if (!filename) {
            filename = this.id;
        }
        this.filename = filename;
        this.engine(filename).render_unsafe(this.textElement, text ?? '');
    }
    engine(url: URL | string): any {
        if (!this.mdRender) {
            this.mdRender = new turtleduck.mdRender({ html: true, hrefPrefix: `${url}`.replace(/[^\/]*$/, '') });
        }
        return this.mdRender;
    }
    initFromUrl(url: URL | string, title?: string, closeable = false) {
        const path = url instanceof URL ? url.pathname : url;
        if (!title) {
            title = path.replace(/^.*\//, "");
        }
        this.title = title;
        this.textElement.innerText = 'loading...';

        fetch(url, {
            method: 'GET',
            headers: { Accept: 'text/markdown, text/plain, text/*;q=0.9' }
        }).then(async res => {
            if (res.status === 200) {
                const txt = await res.text();
                await this.engine(url).render_unsafe(this.textElement, txt);
            } else {
                this.textElement.innerText = `Error loading ${url}: ${res.status} ${res.statusText}`
                console.error("Unexpected request result:", url, res);
            }
        });
    }

    closeHandler(ev: Event) {
        console.log("closing document: name=%s, title=%s", this.id, this.title, this);
    }
    focus(options?: FocusOptions) {
        super.focus(options)
    }
    select(): void { }

    connectedCallback() {
        this.appendChild(this.textElement);
    }

    disconnectedCallback() {
        this.removeChild(this.textElement);

    }
}



export const Documents = { BorbDocument, styleRef };
export default Documents;

SubSystem.declare('borb/documents', Documents)
    .depends('dom')
    .start((self, dep) => {
        console.groupCollapsed(`defining mddisplay: ${self.name}`);
        customElements.define(BorbDocument.tag, BorbDocument);
        console.groupEnd();
        return Documents;
    }).register();
