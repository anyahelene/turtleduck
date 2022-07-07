import SubSystem from '../SubSystem';
import { Hole, html, render } from "uhtml";
import { turtleduck } from '../TurtleDuck';
import { BorbElement, tagName, assert } from './Borb';
import { data } from './Styles';
import IndexedMap from './IndexedMap';
import { uniqueId } from 'lodash-es';
//import 'css/frames.css';


type TabEvent = "show" | "hide";

type TabCallback = (data: any, tabEvent: TabEvent, origEvent: Event) => void;

const styleRef = 'css/frames.css';

const revision: number = import.meta.webpackHot && import.meta.webpackHot.data ? import.meta.webpackHot.data['revision'] + 1 : 0;
const OldFrames: typeof Frames = import.meta.webpackHot && import.meta.webpackHot.data ? import.meta.webpackHot.data['Frames'] : undefined;

class BorbPanel extends HTMLElement {

}
class BorbTab extends BorbElement {
    static tag = tagName('tab', revision);
    _target?: HTMLElement;
    get targetElement(): HTMLElement {
        if (!this._target)
            this.connectedCallback();
        return this._target;
    }
    get target(): string {
        return this.getAttribute('target');
    }
    set target(tgt: string) {
        this.setAttribute('target', tgt);
        this._target = undefined;
    }
    connectedCallback() {
        this._target = document.getElementById(this.target);
    }

    disconnectedCallback() {
        this._target = undefined;
    }

    adoptedCallback() {
        this.connectedCallback();
    }

}

const dragImage = new Image(1, 1);
dragImage.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';
dragImage.style.userSelect = 'none';
dragImage.style.opacity = '0.1';
dragImage.style.background = 'none';

//const dragImage = (html.node`<img width="0" height="0" style="background:green!important;opacity:0%" id="transparent-pixel" src="">`;

class FrameDragState {
    _frame: BorbFrame;
    _srcNav: HTMLElement;
    _count = 0;
    _offsetX: number;
    _offsetY: number;
    _startX: number;
    _startY: number;
    _handler: (e: DragEvent) => void;
    constructor(src: BorbFrame, ev: DragEvent) {
        console.log('dragsession[frame]', src, ev);
        this._frame = src;
        this._offsetX = src.offsetLeft;
        this._offsetY = src.offsetTop;
        this._startX = ev.pageX;
        this._startY = ev.pageY;
        this._frame.style.transitionProperty = '';
        ev.dataTransfer.effectAllowed = "move";
        ev.dataTransfer.setData("application/x-borb-frame", src.id);
        src.appendChild(dragImage);
        console.log(window.getComputedStyle(dragImage));
        ev.dataTransfer.setDragImage(dragImage, -9999, -9999);
        console.log(ev.dataTransfer);
        src.classList.add('dragging');
        src.style.zIndex = '99';
        this._handler = (e: DragEvent) => this.dragHandler(e, src);
        document.addEventListener('dragover', this._handler);
    }

    endSession() {
        console.log(this._count);
        this.endDropAttempt();
        this._frame.classList.remove('dragging');
        this._frame.classList.remove('moving');
        this._frame.style.transitionProperty = 'left,top';
        this._frame.style.top = '0';
        this._frame.style.left = '0';
        document.removeEventListener('dragover', this._handler);
        BorbFrame._dragSession = undefined;

    }

    endDropAttempt() {
        this._frame.classList.remove('move');
        this._count = 0;
    }
    dragHandler(e: DragEvent, dest: BorbFrame) {
        const dataTransfer = e.dataTransfer;
        if (e.type == 'dragend') {
            this.endSession();
        } else if (e.type == 'dragover') {
            const offsetX = e.pageX - this._startX;
            const offsetY = e.pageY - this._startY;
            this._frame.style.left = `${offsetX}px`;
            this._frame.style.top = `${offsetY}px`;
            console.log(this._startX, this._startY, (e as any).layerX, (e as any).layerY);
        } else if (e.type == 'dragenter' && dataTransfer) {
            console.info("drag enter", e.currentTarget === e.target, this._count, e);
            if (this._count === 0 && dataTransfer.getData('application/x-borb-tabpanel')) {
                this._frame.classList.add('move');
                e.preventDefault();
            }
            this._count++;
        } else if (e.type == 'dragleave') {
            console.info("drag leave", e.currentTarget === e.target, this._count, e);
            this._count--;
            console.log(this._count);
            if (this._count === 0) {
                this.endDropAttempt();
            }
            e.preventDefault();
        } else if (e.type == 'drop') {
            this.endDropAttempt();
        }
    }

}
class TabDragState {
    //    _dragSource: BorbFrame;
    _srcPlaceholder: HTMLElement;
    _count = 0;
    _tab: TabEntry;
    _origTabs: Element[][];
    _dropping = false;
    constructor(src: BorbFrame, tab: TabEntry, ev: DragEvent) {
        console.log('dragsession', src, tab, ev);
        this._tab = tab;
        this._srcPlaceholder = html.node`<span></span>`;
        ev.dataTransfer.effectAllowed = "move";
        ev.dataTransfer.setData("application/x-borb-tabpanel", this._tab.element.id);
        ev.dataTransfer.setDragImage(tab, tab.offsetWidth / 2, -tab.offsetHeight);
        tab.classList.add('dragging');
        //elt.insertAdjacentElement('beforebegin', this._srcPlaceholder);
        this._origTabs = this._saveTabPositions();
    }
    _saveTabPositions() {
        const result = [];
        document.querySelectorAll(`${BorbFrame.tag} nav.tabs`).forEach(nav => {
            result.push([...nav.children]);
        });
        return result;
    }
    endSession() {
        console.log(this._count, 'srcPlaceholder parent', this._srcPlaceholder?.parentElement);
        this.endDropAttempt();
        this._tab.classList.remove('dragging', 'move');
        BorbFrame._dragSession = undefined;

    }

    endDropAttempt() {
        console.log("endDropAttempt button parent", this._tab.parentElement, "src parent", this._srcPlaceholder.parentElement);
        this._tab.classList.remove('move');
        assert(!this._dropping || this._srcPlaceholder.parentElement, "srcPlaceholder has parent");
        if (this._srcPlaceholder.parentElement)
            this._srcPlaceholder.replaceWith(this._tab);
        const newTabs = this._saveTabPositions();
        this._origTabs.forEach((ts, idx) => {
            ts.forEach((t, i) => {
                assert(t === newTabs[idx][i], "Tab buttons changed: ", t, "!==", newTabs[idx][i], "origtabs=", this._origTabs, "newtabs=", newTabs);
            });
        });
        this._dropping = false;
        this._count = 0;
    }
    beginDropAttempt(dest: BorbFrame) {
        console.log("beginDropAttempt", this, this._tab, dest);
        assert(!this._srcPlaceholder.parentElement, "!srcPlaceholder.parentElement");
        if (!this._srcPlaceholder.parentElement)
            this._tab.replaceWith(this._srcPlaceholder);
        this._tab.classList.add('move');
        this._dropping = true;
    }
    dragHandler(e: DragEvent, dest: BorbFrame) {
        const dataTransfer = e.dataTransfer;
        const elt = e.currentTarget as HTMLElement;
        if (e.type == 'dragend') {
            this.endSession();
        } else if (e.type == 'dragover') {
            if (!this._dropping)
                return;
            let target = (e.target as HTMLElement).closest('button');
            e.preventDefault();
            if (target === this._tab) {
                // do nothing
            } else if (target?.getAttribute('role') === 'tab') {
                const dst = this._tab;
                console.log(e.offsetX, target.offsetWidth, target.offsetLeft, dst.offsetLeft);
                if (target.parentElement === dst.parentElement && target.offsetLeft > dst.offsetLeft) {
                    target.insertAdjacentElement('afterend', dst);
                } else {
                    target.insertAdjacentElement('beforebegin', dst);
                }
            } else {
                dest._nav.appendChild(this._tab);
            }
        } else if (e.type == 'dragenter') {
            if (this._count === 0 && dataTransfer?.getData('application/x-borb-tabpanel') && this._tab.canDropTo(dest)) {
                this.beginDropAttempt(dest);
                e.preventDefault();
            }
            this._count++;
        } else if (e.type == 'dragleave') {
            this._count--;
            console.log(this._count);
            if (this._count === 0 && this._dropping) {
                console.log("enter button parent", this._tab.parentElement, "src parent", this._srcPlaceholder.parentElement);
                this.endDropAttempt();
                e.preventDefault();
            }
            assert(this._count >= 0, "count >= 0", this._count);
        } else if (e.type == 'drop') {
            console.log('drop', dest, this._tab);
            const nextButton = this._tab.nextElementSibling as TabEntry;
            const nextElement = nextButton?.element;
            this.endDropAttempt();
            console.log(this._tab.element, this._tab, nextButton, nextElement);
            if (nextElement) {
                if (nextElement !== this._tab.element) {
                    nextElement.insertAdjacentElement('beforebegin', this._tab.element);
                    this._tab.frame.queueUpdate(true);
                    if (dest != this._tab.frame)
                        dest.queueUpdate(true);
                    e.preventDefault();
                }
            } else {
                dest.appendChild(this._tab.element);
                this._tab.frame.queueUpdate(true);
                if (dest != this._tab.frame)
                    dest.queueUpdate(true);
                e.preventDefault();
            }
        }
    }

}
class TabEntry extends HTMLButtonElement {
    static nameAttrs = ['tab-title', 'frame-title', 'data-tab-title', 'data-frame-title'];
    static tag = tagName('tab-button-internal', revision);
    element: HTMLElement;
    observer: MutationObserver;
    frame: BorbFrame;
    constructor() {
        super();
    }

    init(elt: HTMLElement, frame: BorbFrame): this {
        if (!elt.id)
            elt.id = uniqueId();
        this.element = elt;
        this.frame = frame;
        this.draggable = true;
        this.setAttribute('role', 'tab');
        this.setAttribute('aria-controls', this.panel.id ?? '');
        this.type = 'button';
        this.observer = new MutationObserver(() => this.update());
        this.observer.observe(this.element, { attributeFilter: TabEntry.nameAttrs });
        this.element.slot = this.frame.classList.contains('no-tabs') ? '' : this.element.id;
        if (!this.panel.hasAttribute('role')) {
            this.panel.setAttribute('role', 'tabpanel');
        }
        this.addEventListener('click', this.frame._clickHandler);
        ['dragstart', 'dragend'].forEach(ename => this.addEventListener(ename, this.frame._dragHandler));
        this.update();
        return this;
    }
    canDropTo(dest: BorbFrame): boolean {
        if (this.frame === dest) // can always move within frame
            return true;
        if (dest.classList.contains('no-tabs') // dest doesn't accept tabs
            || this.element !== this.panel) // tab can't leave frame
            return false;
        return true;
    }
    get panel() {
        if ((this.element as BorbTab).targetElement) {
            return (this.element as BorbTab).targetElement;
        } else {
            return this.element;
        }
    }
    dispose() {
        ['dragstart', 'dragend'].forEach(ename => this.removeEventListener(ename, this.frame._dragHandler));
        this.remove();
        this.observer.disconnect();
    }

    get hidden(): boolean {
        return this.element.hidden || !this.tabTitle;
    }

    get tabTitle(): string {
        for (const name of TabEntry.nameAttrs) {
            if (this.element.hasAttribute(name))
                return this.element.getAttribute(name);
        }
        return '';
    }

    get slotId(): string {
        return this.element.slot;
    }
    update() {
        render(this, html`<span>${this.tabTitle}</span>`);
    }
}

class BorbFrame extends HTMLElement {
    static tag = tagName('frame', revision);
    static _dragSession?: TabDragState | FrameDragState;
    _tabs: Map<HTMLElement, TabEntry> = new Map();
    _clickHandler: (e: MouseEvent) => void;
    _dragHandler: (e: DragEvent) => void;
    _nav: HTMLElement;
    observer: MutationObserver;
    selected?: TabEntry;
    records: any[] = [];
    mutationHandler: (mut: (MutationRecord | { addedNodes: HTMLCollection | Node[], removedNodes: HTMLCollection | Node[] })[]) => void;
    childObserver: (muts: MutationRecord[]) => void;
    childListChanged = true;
    doUpdate = false;
    _style: HTMLStyleElement;
    styleChangedHandler: (ev: Event) => void;
    constructor() {
        super();
        this._style = turtleduck.styles.get(styleRef);
        this._clickHandler = (e: MouseEvent) => {
            if (e.currentTarget instanceof TabEntry) {
                this.selected = e.currentTarget;
                this.queueUpdate();
            }
        };
        this._dragHandler = (e: DragEvent) => {
            console.log(e);
            if (e.type == 'dragstart') {
                const elt = e.currentTarget as HTMLElement;
                if (BorbFrame._dragSession) {
                    console.warn("new drag started while old session still active", BorbFrame._dragSession, e)
                    BorbFrame._dragSession.endSession();
                }
                if (elt instanceof TabEntry)
                    BorbFrame._dragSession = new TabDragState(this, elt, e);
                else
                    BorbFrame._dragSession = new FrameDragState(this, e);
                console.debug('starting new drag session', BorbFrame._dragSession, e);
            } else if (BorbFrame._dragSession) {
                console.debug('continuing drag session', BorbFrame._dragSession, e);
                BorbFrame._dragSession.dragHandler(e, this);
            } else {
                console.warn("drag event with no session: ", e);
            }
            return false;
        };
        this.mutationHandler = (muts) => {
            this.queueUpdate(true);
        };
        this.styleChangedHandler = (ev: Event) => this.styleChanged();
        this.observer = new MutationObserver(this.mutationHandler);
        this.childObserver = (muts: MutationRecord[]) => this.queueUpdate();
        this._nav = document.createElement('nav');
        this._nav.classList.add('tabs');
        this._nav.setAttribute('role', 'tablist');
    }

    queueUpdate(childListChanged = false) {
        this.childListChanged ||= childListChanged;
        if (!this.doUpdate) {
            this.doUpdate = true;
            queueMicrotask(() => this.update());
        }
    }
    get frameTitle(): string {
        let title = this.hasAttribute('frame-title') ? this.getAttribute('frame-title') : '<tab-title>';
        let tabTitle = this.selected ? this.selected.tabTitle : '';
        return title.replace('<tab-title>', tabTitle);
    }

    prevTab() {
        this.id
    }
    update() {
        if (!this.isConnected)
            return;
        this.doUpdate = false;
        if (this.childListChanged)
            this.updateChildren();

        const maxIcon = false ? '🗗' : '🗖';
        const minIcon = '🗕';
        const frameTitle = this.frameTitle;
        const dh = this._dragHandler;
        this._tabs.forEach(tab => tab.setAttribute('aria-selected', String(tab === this.selected)));
        //console.log("render", this, this, this.isConnected, this.shadowRoot, frameTitle, dh, minIcon, maxIcon);
        try {
            render(this.shadowRoot,
                html`${this._style}  
                <header class=${frameTitle ? '' : 'no-title'} ondrop=${dh} ondragenter=${dh} ondragleave=${dh} ondragover=${dh}>
                    ${this._nav}
                    <h1 draggable="true" ondragstart=${dh} ondragend=${dh}>${frameTitle ?? ''}</h1>
                    <nav class="window-tools"><button class="min-button">${minIcon}</button><button class="max-button">${maxIcon}</button></nav>
                </header>
                ${this.classList.contains('no-tabs') ?
                        html`<slot></slot>` :
                        html`<slot name=${this.selected?.panel.slot || '<none>'}><div class="empty-slot"></div></slot>`
                    }`);
        } catch (ex) {
            console.error("Frame.update", this, ex);
            throw ex;
        }
    }
    newTab(elt: HTMLElement): TabEntry {
        const entry = new TabEntry().init(elt, this);
        console.log("Frames: adding tab", entry);
        this._tabs.set(elt, entry);
        return entry;
    }
    delTab(elt: HTMLElement) {
        const entry = this._tabs.get(elt);
        console.log("Frames: removing tab", entry);
        if (entry) {
            entry.dispose();
            this._tabs.delete(elt);
        }
    }
    updateChildren() {
        this.childListChanged = false;
        let selected: TabEntry = undefined;
        const removedChildren = new Set(this._tabs.keys());
        console.log("updateChildren before", ...this._nav.children);
        this._nav.replaceChildren();
        for (const elt of this.children) {
            if (elt instanceof HTMLElement) {
                removedChildren.delete(elt);
                let entry = this._tabs.get(elt) ?? this.newTab(elt);
                if (!entry.hidden) {
                    this._nav.appendChild(entry);
                    if (entry === this.selected)
                        selected = entry;
                }
            }
        }
        console.log("updateChildren after", ...this._nav.children);
        removedChildren.forEach(elt => this.delTab(elt));
        if (selected)
            this.selected = selected;
        else
            this.selected = this._nav.children?.[0] as TabEntry;
    }

    // static get observedAttributes() { return ['frame-title']; }

    connectedCallback() {
        if (this.isConnected) {
            if (!this.shadowRoot)
                this.attachShadow({ mode: "open" });
            console.log('element added to page.', this, this.isConnected, this.shadowRoot);
            this.observer.observe(this, {
                childList: true,
                attributeFilter: ['frame-title']
            });

            turtleduck.uniqueId("tabbedFrame", this);
            turtleduck.styles.attach(styleRef, this.styleChangedHandler);
            this.mutationHandler([]);
        }
    }

    disconnectedCallback() {
        this.observer.disconnect();
        this._nav.replaceChildren();
        this._tabs.forEach(entry => entry.dispose());
        this._tabs.clear();
        turtleduck.styles.detach(styleRef, this.styleChangedHandler);
        console.log('removed from page.', this);
    }

    adoptedCallback() {
        console.log('moved to new page.', this);
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        console.log('element attributes changed.', this, this.shadowRoot, name, oldValue, newValue);
        this.update();
    }
    styleChanged() {
        this._style = turtleduck.styles.get(styleRef);
        this.update();
        console.log('style changed', this, styleRef, this._style);
    }


}

export const Frames = { BorbFrame, BorbTab, BorbPanel, styleRef, version: 9, revision };
export default Frames;

SubSystem.declare('borb/frames', Frames)
    .depends('dom')
    .start((self, dep) => {
        console.groupCollapsed("defining frames:");
        try {
            customElements.define(TabEntry.tag, TabEntry, { extends: 'button' });
            customElements.define(BorbFrame.tag, BorbFrame);
            customElements.define(BorbTab.tag, BorbTab);
            if (OldFrames) {
                document.querySelectorAll(OldFrames.BorbFrame.tag).forEach(oldElt => {
                    const newElt = new BorbFrame();
                    console.log("upgrading", oldElt, "to", newElt);
                    oldElt.getAttributeNames().forEach(attrName => {
                        console.log('set', attrName, oldElt.getAttribute(attrName));
                        newElt.setAttribute(attrName, oldElt.getAttribute(attrName));
                    });
                    newElt.replaceChildren(...oldElt.childNodes);
                    console.log('replacedChildren:', newElt.children);
                    oldElt.parentElement.replaceChild(newElt, oldElt);
                });
            }
        } finally {
            console.groupEnd();
        }
    })
    .register();


if (import.meta.webpackHot) {
    import.meta.webpackHot.accept();
    import.meta.webpackHot.accept(styleRef, () => {
        turtleduck.styles.update(styleRef);
    });
    import.meta.webpackHot.addDisposeHandler(data => {
        console.warn("Unloading TabbedFrame");
        data['revision'] = revision;
        data['Frames'] = Frames;
    });
}
