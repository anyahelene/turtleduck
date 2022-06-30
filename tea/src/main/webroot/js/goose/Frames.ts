import SubSystem from '../SubSystem';
import { Hole, html, render } from "uhtml";
import { turtleduck } from '../TurtleDuck';
import { GooseElement, tagName } from './Geese';
import { data } from './Styles';
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
class DragState {
    _draggedElement: HTMLElement;
    _dragSource: GSFrame;
    _srcPlaceholder: HTMLElement;
    _dstPlaceholder: HTMLElement;
    _srcNav: HTMLElement;
    _count = 0;
    constructor(src: GSFrame, elt: HTMLElement, ev: DragEvent) {
        console.log('dragsession', src, elt, ev);
        this._dragSource = src;
        this._draggedElement = elt;
        this._srcNav = elt.parentElement;
        this._srcPlaceholder = html.node`<span></span>`;
        this._dstPlaceholder = elt.cloneNode(true) as HTMLElement;
        this._dstPlaceholder.setAttribute('role', 'presentation');
        this._dstPlaceholder.style.pointerEvents = 'none';
        this._dstPlaceholder.style.background = '#f00';
        this._dstPlaceholder.className = '';
        ev.dataTransfer.effectAllowed = "move";
        ev.dataTransfer.setData("application/x-goose-tabpanel", elt.getAttribute('aria-controls'));
        ev.dataTransfer.setDragImage(elt, elt.offsetWidth / 2, -elt.offsetHeight);
        console.log(ev.dataTransfer);
        elt.classList.add('dragging');
        elt.insertAdjacentElement('beforebegin', this._srcPlaceholder);
    }

    endSession() {
        console.log(this._count, 'srcPlaceholder parent', this._srcPlaceholder.parentElement, 'dstPlaceholder parent', this._dstPlaceholder.parentElement);
        this.endDropAttempt();
        this._draggedElement.classList.remove('dragging');
        this._draggedElement.classList.remove('moving');
        //this._srcPlaceholder.remove();
        this._srcPlaceholder.replaceWith(this._draggedElement);
        this._dstPlaceholder.remove();
        GSFrame._dragSession = undefined;

    }

    endDropAttempt() {
        this._draggedElement.classList.remove('move');
        this._dstPlaceholder.remove();
        this._draggedElement.remove();
        this._count = 0;
    }
    dragHandler(e: DragEvent, dest: GSFrame) {
        const dataTransfer = e.dataTransfer;
        const elt = e.currentTarget as HTMLElement;
        let target = (e.target as HTMLElement).closest('button');
        console.info("drag event", e.currentTarget === e.target, this._count, e, "elt=", elt, "target=", target);
        if (e.type == 'dragend') {
            this.endSession();
        } else if (e.type == 'dragover') {
            e.preventDefault();
            if (target?.getAttribute('role') === 'tab') {
                const dst = this._draggedElement; 
                console.log(e.offsetX, target.offsetWidth, target.offsetLeft, dst.offsetLeft);
                if (target.parentElement === dst.parentElement && target.offsetLeft > dst.offsetLeft) {
                    target.insertAdjacentElement('afterend', dst);
                } else {
                    target.insertAdjacentElement('beforebegin', dst);
                }
            } else if((target = (e.target as HTMLElement).querySelector('nav.tabs'))) {
                target.appendChild(this._draggedElement);
            }
            //            console.log("dragOver", e, e.offsetX, elt.offsetWidth);
        } else if (e.type == 'dragenter' && dataTransfer) {
            if (this._count === 0 && dataTransfer.getData('application/x-goose-tabpanel')) {
                this._draggedElement.classList.add('move');
                e.preventDefault();
            }
            this._count++;
        } else if (e.type == 'dragleave') {
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
class GSFrame extends HTMLElement {
    static tag = tagName('frame', revision);
    static _dragSession?: DragState;
    _clickHandler: (e: MouseEvent) => void;
    _dragHandler: (e: DragEvent) => void;
    _nav?: HTMLElement;
    observer: MutationObserver;
    selected?: HTMLElement;
    records: any[] = [];
    mutationHandler: (mut: (MutationRecord | { addedNodes: HTMLCollection | Node[], removedNodes: HTMLCollection | Node[] })[]) => void;
    childObserver: MutationObserver;
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
                if (GSFrame._dragSession) {
                    console.warn("new drag started while old session still active", GSFrame._dragSession, e)
                    GSFrame._dragSession.endSession();
                }
                GSFrame._dragSession = new DragState(this, e.currentTarget as HTMLElement, e);
                // e.preventDefault();
                console.log('starting new drag session', GSFrame._dragSession, e);
            } else if (GSFrame._dragSession) {
                console.log('continuing drag session', GSFrame._dragSession, e);
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
        this.childObserver = new MutationObserver((muts) => {
            this.queueUpdate();
        });

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
    tabNameOf(elt: HTMLElement) {
        if (!elt)
            return '';
        if ('tabName' in elt)
            return elt['tabName'];
        if (elt.hasAttribute('tab-name'))
            return elt.getAttribute('tab-name');
        if (elt.dataset.tabName)
            return elt.dataset.tabName;
    }
    prevTab() {
        this.id
    }
    update() {
        this.doUpdate = false;
        if (this.childListChanged)
            this.updateChildren();
        const children = ([...this.children] as HTMLElement[]).filter(e => this.tabNameOf(e) && !e.hidden);
        console.log("update", this, children);
        if (!this.selected) {
            this.selected = children[0];
            console.log("fallback to", this.selected);
        }
        const maxIcon = false ? '🗗' : '🗖';
        const minIcon = '🗕';
        const frameTitle = this.frameTitle;
        const dh = this._dragHandler;
        render(this.shadowRoot as Node,
            html`${this._style}
                <header class=${frameTitle ? '' : 'no-title'} ondrop=${dh} ondragenter=${dh} ondragleave=${dh} ondragover=${dh}>
                    <nav class="tabs" role="tablist" ref=${((elt: HTMLElement) => this._nav = elt)}>${children.map(e => this.makeButton(e))}</nav>
                    ${frameTitle ? html`<h1>${frameTitle}</h1>` : ''}
                    <nav class="window-tools"><button class="min-button">${minIcon}</button><button class="max-button">${maxIcon}</button></nav>
                </header>
                ${this.classList.contains('no-tabs') ?
                    html`<slot></slot>` :
                    html`<slot name=${this.panelIdOf(this.selected) || '<none>'}><div class="empty-slot"></div></slot>`
                }`);
    }
    panelIdOf(elt?: HTMLElement): string {
        if (elt instanceof GSTab)
            return elt.target;
        else if (elt)
            return elt.id;
        else
            return undefined;
    }
    makeButton(elt: HTMLElement) {
        const dh = this._dragHandler;
        return html`<button role="tab" onclick=${this._clickHandler} ondragstart=${dh} ondragend=${dh}  draggable="true" aria-selected=${elt === this.selected ? "true" : "false"} aria-controls=${this.panelIdOf(elt)} type="button"><div>${this.tabNameOf(elt)}</div></button>`

    }
    updateChildren() {
        this.childListChanged = false;
        this.childObserver.disconnect();
        let selected: HTMLElement = undefined;
        for (const elt of this.children) {
            console.log("FRAME CHILD", elt.cloneNode(false));
            if (elt instanceof HTMLElement) {
                if (!elt.id)
                    turtleduck.uniqueId(this.id, elt);
                if (elt === this.selected && elt.title && !elt.hidden)
                    selected = elt;
                elt.slot = this.classList.contains('no-tabs') ? '' : elt.id;
                elt.setAttribute('aria-selected', "false");
                if (!(elt instanceof GSTab))
                    elt.setAttribute('role', 'tabpanel');
                this.childObserver.observe(elt, { attributeFilter: ['tab-name', 'frame-title', 'data-tab-name', 'data-frame-title', 'hidden'] });
            }
        }
        this.selected = selected;
    }

    static get observedAttributes() { return ['frame-title']; }

    connectedCallback() {
        console.log('element added to page.', this, this.isConnected);
        if (!this.shadowRoot)
            this.attachShadow({ mode: "open" });
        this.observer.observe(this, {
            childList: true
        });

        turtleduck.uniqueId("tabbedFrame", this);
        turtleduck.styles.attach(styleRef, this.styleChangedHandler);
        this.mutationHandler([]);
    }

    disconnectedCallback() {
        this.observer.disconnect();
        this.childObserver.disconnect();
        turtleduck.styles.detach(styleRef, this.styleChangedHandler);
        console.log('removed from page.', this);
    }

    adoptedCallback() {
        console.log('moved to new page.', this);
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        console.log('element attributes changed.', name, oldValue, newValue);
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
