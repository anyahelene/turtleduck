import SubSystem from '../SubSystem';
import { Hole, html, render } from "uhtml";
import { turtleduck } from '../TurtleDuck';
import { GooseElement, tagName, assert } from './Geese';
import { data } from './Styles';
import IndexedMap from './IndexedMap';
import { uniqueId } from 'lodash-es';
//import 'css/frames.css';


type TabEvent = "show" | "hide";

type TabCallback = (data: any, tabEvent: TabEvent, origEvent: Event) => void;

const styleRef = 'css/frames.css';

const revision: number = import.meta.webpackHot && import.meta.webpackHot.data ? import.meta.webpackHot.data['revision'] + 1 : 0;
const OldFrames: typeof Frames = import.meta.webpackHot && import.meta.webpackHot.data ? import.meta.webpackHot.data['Frames'] : undefined;

class GSPanel extends HTMLElement {

}
class GSTab extends GooseElement {
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
    _frame: GSFrame;
    _srcNav: HTMLElement;
    _count = 0;
    _offsetX: number;
    _offsetY: number;
    _startX: number;
    _startY: number;
    _handler: (e: DragEvent) => void;
    constructor(src: GSFrame, ev: DragEvent) {
        console.log('dragsession[frame]', src, ev);
        this._frame = src;
        this._offsetX = src.offsetLeft;
        this._offsetY = src.offsetTop;
        this._startX = ev.pageX;
        this._startY = ev.pageY;
        this._frame.style.transitionProperty = '';
        ev.dataTransfer.effectAllowed = "move";
        ev.dataTransfer.setData("application/x-goose-frame", src.id);
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
        GSFrame._dragSession = undefined;

    }

    endDropAttempt() {
        this._frame.classList.remove('move');
        this._count = 0;
    }
    dragHandler(e: DragEvent, dest: GSFrame) {
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
            if (this._count === 0 && dataTransfer.getData('application/x-goose-tabpanel')) {
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
    _dragSource: GSFrame;
    _srcPlaceholder: HTMLElement;
    _dstPlaceholder: HTMLElement;
    _count = 0;
    _tab: TabEntry;
    _origTabs: Element[][];
    _dropping = false;
    constructor(src: GSFrame, tab: TabEntry, ev: DragEvent) {
        const elt = tab.button;
        console.log('dragsession', src, elt, ev);
        this._dragSource = src;
        this._tab = tab;
        this._srcPlaceholder = html.node`<span>||</span>`;
        this._dstPlaceholder = elt.cloneNode(true) as HTMLElement;
        this._dstPlaceholder.setAttribute('role', 'presentation');
        this._dstPlaceholder.style.pointerEvents = 'none';
        this._dstPlaceholder.style.background = '#f00';
        this._dstPlaceholder.className = '';
        ev.dataTransfer.effectAllowed = "move";
        ev.dataTransfer.setData("application/x-goose-tabpanel", this._tab.element.id);
        ev.dataTransfer.setDragImage(elt, elt.offsetWidth / 2, -elt.offsetHeight);
        console.log(ev.dataTransfer);
        elt.classList.add('dragging');
        //elt.insertAdjacentElement('beforebegin', this._srcPlaceholder);
        this._origTabs = this._saveTabPositions();
    }
    _saveTabPositions() {
        const result = [];
        document.querySelectorAll(`${GSFrame.tag} nav.tabs`).forEach(nav => {
            result.push([...nav.children]);
        });
        return result;
    }
    endSession() {
        console.log(this._count, 'srcPlaceholder parent', this._srcPlaceholder?.parentElement, 'dstPlaceholder parent', this._dstPlaceholder.parentElement);
        this.endDropAttempt();
        this._tab.button.classList.remove('dragging');
        this._tab.button.classList.remove('moving');
        //this._srcPlaceholder.remove();
        GSFrame._dragSession = undefined;

    }

    endDropAttempt() {
        console.log("endDropAttempt button parent", this._tab.button.parentElement, "src parent", this._srcPlaceholder.parentElement);
        this._tab.button.classList.remove('move');
        this._dstPlaceholder.remove();
        this._tab.button.remove();
        assert(this._srcPlaceholder.parentElement, "srcPlaceholder has parent");
        if (this._srcPlaceholder.parentElement)
            this._srcPlaceholder.replaceWith(this._tab.button);
        const newTabs = this._saveTabPositions();
        this._origTabs.forEach((ts, idx) => {
            ts.forEach((t, i) => {
                assert(t === newTabs[idx][i], "Tab buttons changed: ", t, "!==", newTabs[idx][i], "origtabs=", this._origTabs, "newtabs=", newTabs);
            });
        });
        this._dropping = false;
        this._count = 0;
    }
    beginDropAttempt() {
        console.log("beginDropAttempt button parent", this._tab.button.parentElement, "src parent", this._srcPlaceholder.parentElement);
        if (!this._srcPlaceholder.parentElement)
            this._tab.button.replaceWith(this._srcPlaceholder);
        this._tab.button.classList.add('move');
        this._dropping = true;
    }
    dragHandler(e: DragEvent, dest: GSFrame) {
        const dataTransfer = e.dataTransfer;
        const elt = e.currentTarget as HTMLElement;
        if (e.type == 'dragend') {
            this.endSession();
        } else if (e.type == 'dragover') {
            let target = (e.target as HTMLElement).closest('button');
            e.preventDefault();
            if (target === this._tab.button) {
                // do nothing
            } else if (target?.getAttribute('role') === 'tab') {
                const dst = this._tab.button;
                console.log(e.offsetX, target.offsetWidth, target.offsetLeft, dst.offsetLeft);
                if (target.parentElement === dst.parentElement && target.offsetLeft > dst.offsetLeft) {
                    target.insertAdjacentElement('afterend', dst);
                } else {
                    target.insertAdjacentElement('beforebegin', dst);
                }
            } else if ((target = (e.target as HTMLElement).querySelector('nav.tabs'))) {
                target.appendChild(this._tab.button);
            }
            //            console.log("dragOver", e, e.offsetX, elt.offsetWidth);
        } else if (e.type == 'dragenter') {
            if (this._count === 0 && dataTransfer?.getData('application/x-goose-tabpanel') && !dest.classList.contains('no-tabs')) {
                this.beginDropAttempt();
                e.preventDefault();
            }
            this._count++;
        } else if (e.type == 'dragleave') {
            this._count--;
            console.log(this._count);
            if (this._count === 0 && this._dropping) {
                console.log("enter button parent", this._tab.button.parentElement, "src parent", this._srcPlaceholder.parentElement);
                this.endDropAttempt();
            }
            assert(this._count >= 0, "count >= 0", this._count);
            e.preventDefault();
        } else if (e.type == 'drop') {
            console.log('drop', dest, this._tab);
            const nextButton = this._tab.button.nextElementSibling as HTMLElement;
            const nextElement = dest._tabs.get(nextButton, 'button')?.element;
            this.endDropAttempt();
            console.log(this._tab.element, this._tab.button, nextButton, nextElement);
            if (nextElement) {
                if (nextElement !== this._tab.element) {
                    nextElement.insertAdjacentElement('beforebegin', this._tab.element);
                    this._dragSource.queueUpdate(true);
                    if (dest != this._dragSource)
                        dest.queueUpdate(true);
                    e.preventDefault();
                }
            } else {
                dest.appendChild(this._tab.element);
                this._dragSource.queueUpdate(true);
                if (dest != this._dragSource)
                    dest.queueUpdate(true);
                e.preventDefault();
            }

        }
    }

}
class TabEntry {
    static nameAttrs = ['tab-title', 'frame-title', 'data-tab-title', 'data-frame-title'];
    element: HTMLElement;
    panel: HTMLElement;
    button: HTMLElement;
    observer: MutationObserver;
    frame: GSFrame;
    constructor(elt: HTMLElement, frame: GSFrame) {
        if (!elt.id)
            elt.id = uniqueId();
        this.element = elt;
        this.frame = frame;
        this.panel = elt instanceof GSTab ? elt.targetElement : elt;
        this.observer = new MutationObserver(() => this.update());
        this.button = html.node`<button role="tab" draggable="true" aria-controls=${this.panel.id ?? ''} type="button"></button>`;
    }

    init(): this {
        this.observer.observe(this.element, { attributeFilter: TabEntry.nameAttrs });
        this.element.slot = this.frame.classList.contains('no-tabs') ? '' : this.element.id;
        if (!this.panel.hasAttribute('role')) {
            this.element.setAttribute('role', 'tabpanel');
        }
        this.button.addEventListener('click', this.frame._clickHandler);
        ['dragstart', 'dragend'].forEach(ename => this.button.addEventListener(ename, this.frame._dragHandler));
        this.update();
        return this;
    }

    dispose() {
        ['dragstart', 'dragend'].forEach(ename => this.button.removeEventListener(ename, this.frame._dragHandler));
        this.button.remove();
        this.observer.disconnect();
    }

    get hidden(): boolean {
        return this.element.hidden || !this.tabName;
    }

    get tabName(): string {
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
        render(this.button, html`<span>${this.tabName}</span>`);
    }
}

class GSFrame extends HTMLElement {
    static tag = tagName('frame', revision);
    static _dragSession?: TabDragState | FrameDragState;
    _tabs: IndexedMap<TabEntry, 'element'> = new IndexedMap('element', 'button');
    _tabList: TabEntry[];
    _clickHandler: (e: MouseEvent) => void;
    _dragHandler: (e: DragEvent) => void;
    _nav?: HTMLElement;
    observer: MutationObserver;
    selected?: HTMLElement;
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
            const target = e.currentTarget as HTMLElement;
            this.selected = document.getElementById(target.getAttribute('aria-controls'));
            this.queueUpdate();
        };
        this._dragHandler = (e: DragEvent) => {
            console.log(e);
            if (e.type == 'dragstart') {
                const elt = e.currentTarget as HTMLElement;
                if (GSFrame._dragSession) {
                    console.warn("new drag started while old session still active", GSFrame._dragSession, e)
                    GSFrame._dragSession.endSession();
                }
                if (elt instanceof HTMLButtonElement)
                    GSFrame._dragSession = new TabDragState(this, this._tabs.get(elt, 'button'), e);
                else
                    GSFrame._dragSession = new FrameDragState(this, e);
                // e.preventDefault();
                console.debug('starting new drag session', GSFrame._dragSession, e);
            } else if (GSFrame._dragSession) {
                console.debug('continuing drag session', GSFrame._dragSession, e);
                GSFrame._dragSession.dragHandler(e, this);
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
        let tabTitle = '';
        if (this.selected) {
            if ('frameTitle' in this.selected) {
                tabTitle = this.selected['frameTitle'];
            } else if (this.selected.hasAttribute('frame-title')) {
                tabTitle = this.selected.getAttribute('frame-title');
            } else if (this.selected.dataset.frameTitle) {
                tabTitle = this.selected.dataset.frameTitle;
            }
        }
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
        const selectedTab = this.selected ? this._tabs.get(this.selected) : undefined;
        const dh = this._dragHandler;
        console.trace("render", this, this, this.isConnected, this.shadowRoot, frameTitle, dh, this._tabList, minIcon, maxIcon);
        try {
            render(this.shadowRoot,
                html`${this._style}  
                <header class=${frameTitle ? '' : 'no-title'} ondrop=${dh} ondragenter=${dh} ondragleave=${dh} ondragover=${dh}>
                    <nav class="tabs" role="tablist" ref=${((elt: HTMLElement) => this._nav = elt)}>${this._tabList.map(e => { console.log(e.button); return e.button; })}</nav>
                    <h1 draggable="true" ondragstart=${dh} ondragend=${dh}>${frameTitle ?? ''}</h1>
                    <nav class="window-tools"><button class="min-button">${minIcon}</button><button class="max-button">${maxIcon}</button></nav>
                </header>
                ${this.classList.contains('no-tabs') ?
                        html`<slot></slot>` :
                        html`<slot name=${selectedTab?.slotId || '<none>'}><div class="empty-slot"></div></slot>`
                    }`);
        } catch (ex) {
            console.error("Frame.update", this, ex);
            throw ex;
        }
    }
    newTab(elt: HTMLElement): TabEntry {
        const entry = new TabEntry(elt, this).init();
        console.log("Frames: adding tab", entry);
        this._tabs.put(entry);
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
        let selected: HTMLElement = undefined;
        const removedChildren = new Set(this._tabs.keys());
        console.log("updateChildren before", this._tabList);
        this._tabList = [];
        for (const elt of this.children) {
            if (elt instanceof HTMLElement) {
                removedChildren.delete(elt);
                let entry = this._tabs.get(elt) ?? this.newTab(elt);
                if (!entry.hidden) {
                    this._tabList.push(entry);
                    if (elt === this.selected)
                        selected = elt;
                }
            }
        }
        console.log("updateChildren after", this._tabList);
        removedChildren.forEach(elt => this.delTab(elt));
        if (selected)
            this.selected = selected;
        else
            this.selected = this._tabList[0]?.element;
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
        this._tabList = [];
        this._tabs.clear().forEach(entry => entry.dispose());
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

export const Frames = { GSFrame, GSTab, GSPanel, styleRef, version: 9, revision };
export default Frames;

SubSystem.declare('goose/frames', Frames)
    .depends('dom')
    .start((self, dep) => {
        console.groupCollapsed("defining frames:");
        try {
            customElements.define(GSFrame.tag, GSFrame);
            customElements.define(GSTab.tag, GSTab);
            if (OldFrames) {
                document.querySelectorAll(OldFrames.GSFrame.tag).forEach(oldElt => {
                    const newElt = new GSFrame();
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
