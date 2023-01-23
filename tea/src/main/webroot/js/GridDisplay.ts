import { Messaging, Message, Payload, BaseConnection } from '../borb/Messaging';
import { uniqueIdNumber } from '../borb/Common';
import Systems from '../borb/SubSystem';
import Settings from '../borb/Settings';
import { BorbBaseElement } from '../borb/BaseElement';
import { tagName, uniqueId } from '../borb/Common';
import { html, render } from 'uhtml';
import { turtleduck } from './TurtleDuck';

const revision: number =
    import.meta.webpackHot && import.meta.webpackHot.data
        ? import.meta.webpackHot.data['revision'] + 1
        : 0;
const previousVersion: typeof _self =
    import.meta.webpackHot && import.meta.webpackHot.data
        ? import.meta.webpackHot.data['self']
        : undefined;
const styleRef = 'css/grid-display.css';
const GRID_SELECTOR = '.grid',
    GRID_CELL_SELECTOR = '.grid > div';
const cellStyles = {
    fill: {
        position: 'absolute',
        width: '100%',
        height: '100%',
    },
    center: {
        position: 'absolute',
        transform: 'translate(-50%, -50%)',
        top: '50%',
        left: '50%',
    },
    bottom: {
        position: 'absolute',
        text_align: 'center',
        transform: 'translate(-50%, -50%)',
        bottom: '0%',
        left: '50%',
    },
};
export class GridConnection extends BaseConnection {
    constructor(router: typeof Messaging, public grid: GridDisplayElement, id: string) {
        super(router, id);
    }

    deliverRemote(msg: Message, transfers: Transferable[]): void {
        const msg_type = msg.header.msg_type;
        if (!msg_type.startsWith('_')) {
            const m = this.grid[msg_type] as (msg: Payload) => Promise<Payload>;
            console.log('grid connection got request', msg, transfers, m);
            if (typeof m === 'function') {
                Promise.resolve(m.bind(this.grid)(msg.content)).then((reply) => {
                    if (reply) this.deliverHost(this.router.reply(msg, reply));
                });
            }
        }
    }
}

class GridDisplayElement extends BorbBaseElement {
    static tag = tagName('grid-display', revision);

    private _observer: MutationObserver = new MutationObserver((muts) => this.queueUpdate(true));
    private _elt: HTMLDivElement;
    private _styleRules: Record<string, Record<string, string>>;
    private _gridStyle: HTMLStyleElement = document.createElement('style');
    private _conn: GridConnection;
    gridTitle: string;
    gridWidth: number;
    gridHeight: number;
    private _letterText: string | boolean;
    private _letterStyles: boolean;
    constructor() {
        super(['css/common.css', styleRef]);
    }
    indexOf(x: number, y: number) {
        return y * this.gridWidth + x;
    }
    upgrade(old: GridDisplayElement) {
        this._elt = old._elt;
        this._styleRules = old._styleRules;
        this._conn = old._conn;
        this._conn.grid = this;
        this.gridTitle = old.gridTitle;
        this.gridWidth = old.gridWidth;
        this.gridHeight = old.gridHeight;
        this._letterStyles = old._letterStyles;
        this._letterText = old._letterText;
        old._conn = old._elt = old._styleRules = null;
    }
    init(title: string, width: number, height: number, initial: string) {
        uniqueId('grid-display', this);
        if (!this._conn) this._conn = new GridConnection(Messaging, this, this.id);
        if (title) this.setAttribute('frame-title', title);
        else if (!this.hasAttribute('frame-title'))
            this.setAttribute('frame-title', 'Grid Display');
        this.gridWidth = Number(width);
        this.gridHeight = Number(height);
        this._elt = document.createElement('div');
        this._elt.className = 'grid';
        this._elt.style.gridTemplateColumns = `repeat(${this.gridWidth}, 1fr)`;
        this._elt.style.gridTemplateRows = `repeat(${this.gridHeight}, 1fr)`;
        this._elt.style.display = 'grid';
        this._styleRules = {};
        this._letterText = true;
        this._letterStyles = true;
        for (var y = 0; y < this.gridHeight; y++) {
            for (var x = 0; x < this.gridWidth; x++) {
                const col = document.createElement('div');
                var dir = '';
                if (y === 0) dir += 'grid-edge-N';
                else if (y === this.gridHeight - 1) dir += 'grid-edge-S';
                if (x === 0) dir += ' grid-edge-W';
                else if (x === this.gridWidth - 1) dir += ' grid-edge-E';

                col.dataset.y = `${y}`;
                col.dataset.x = `${x}`;
                col.dataset.dir = dir;
                //col.style.gridColumn = `${x + 1}`;
                //col.style.gridRow = `${y + 1}`;
                this._setCell(col, initial || '');
                this._elt.appendChild(col);
            }
        }
    }
    connectedCallback() {
        super.connectedCallback();
        if (this.isConnected) {
            console.log('connected', this.tagName, this);
            if (!this.shadowRoot) {
                console.log('creating shadow root');
                this.attachShadow({ mode: 'open' });
            }

            console.log('element added to page.', this);
            this._observer.observe(this, {
                childList: true,
                attributeFilter: ['frame-title', 'grid-width', 'grid-height'],
            });
            this.queueUpdate();
        }
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        this._observer.disconnect();
        // DragNDrop.detachDropZone(this._header);
        console.log('removed from page.', this);
    }
    update() {
        if (this.isConnected && this.shadowRoot) {
            this._setStyle();
            render(this.shadowRoot, html`${this.styles}${this._gridStyle || ''}${this._elt || ''}`);
        }
    }

    _setCell(cell: HTMLDivElement, val: string) {
        cell.className = cell.dataset.dir;
        if (this._letterText === 'word') {
            const elt = document.createElement('span');
            elt.innerText = val;
            cell.appendChild(elt);
        } else {
            val.split('').forEach((letter) => {
                letter = letter.trim();
                if (letter) {
                    const elt = document.createElement('span');
                    if (this._letterStyles) {
                        elt.classList.add(`span-style-${letter}`);
                        cell.classList.add(`cell-style-${letter}`);
                    }
                    cell.appendChild(elt);
                    if (this._letterText) elt.innerText = letter;
                }
            });
        }
    }

    set(x: number, y: number, value: string) {
        this.grid_update({ updates: [{ x, y, value }] });
    }

    grid_config({
        letterStyles,
        letterText,
    }: {
        letterStyles: boolean;
        letterText: boolean | 'word';
    }) {
        if (letterStyles !== undefined) this._letterStyles = !!letterStyles;
        if (letterText !== undefined)
            this._letterText = letterText === 'word' ? 'word' : !!letterText;
    }
    grid_update({ updates }: { updates: { x: number; y: number; value: string }[] }) {
        let error: string;
        if (updates) {
            updates.forEach((u: { x: number; y: number; value: string }) => {
                const x = u.x,
                    y = u.y;
                if (x >= 0 && x < this.gridWidth && y >= 0 && y < this.gridHeight) {
                    const cell = this._elt.children[this.indexOf(x, y)] as HTMLDivElement;
                    cell.replaceChildren();
                    this._setCell(cell, u.value);
                } else {
                    error = 'out of bounds: ' + JSON.stringify(u);
                }
            });
        }
    }
    _styleRulesFor(selector: string) {
        let rules = this._styleRules[selector];
        if (!rules) {
            rules = this._styleRules[selector] = {};
        }
        return rules;
    }
    /**
     *
     * The property `cell-style` applies predefined styles to cell contents (`fill`, `center` or `bottom`).
     * Properties stating with `cell-` apply to the outer cell `div` (e.g., use for setting cell size or background), while
     * other properties apply to the inner (text-containing) `span`(s).
     *
     * For example, for layered images/icons, set `cell-style: fill` for the `cell` selector, then set semi-transparent
     * `background` for each selector letter.
     *
     * Set the property `reset` to drop all styles for the selector.
     *
     * @param param0
     * @returns
     */
    grid_style({
        selector,
        value,
        styleset,
        property,
    }: {
        selector: string;
        value?: string;
        styleset?: Record<string, string>;
        property?: string;
    }) {
        const sels = selector.split(',').map((spec) => {
            console.log('selector spec:', spec);
            const [sel1, sel2, letter] = this._styleSelector(spec);
            return [sel1, sel2];
        });
        // selects the outer div of a cell (or all cells, or the grid)
        const selectorDiv = sels.map((sel) => sel[0]).join(', ');
        // selects the inside span of a cell
        const selectorSpan = sels.map((sel) => sel[1]).join(', ');
        console.log('selector:', selectorDiv, selectorSpan);

        if (property === 'reset' || styleset?.['reset']) {
            this._styleRules[selectorDiv] = {};
            this._styleRules[selectorSpan] = {};
            this.queueUpdate();
            return Promise.resolve();
        }
        if (!styleset) {
            styleset = {};
            if (property) styleset[property] = value || '';
        }
        const rulesDiv = this._styleRulesFor(selectorDiv);
        const rulesSpan = this._styleRulesFor(selectorSpan);
        console.log('styleSet', styleset);
        for (let prop in styleset) {
            if (prop === 'cell-style') {
                const style = cellStyles[styleset[prop]] || {};
                for (let p in style) {
                    rulesSpan[p.replace('_', '-')] = style[p] || '';
                }
            } else if (prop.startsWith('cell-'))
                rulesDiv[prop.slice(5).replace('_', '-')] = styleset[prop] || '';
            else rulesSpan[prop.replace('_', '-')] = styleset[prop] || '';
        }
        console.log(rulesDiv, rulesSpan);
        this.queueUpdate();
        return Promise.resolve();
    }

    _styleSelector(s: string): [string, string, string] {
        const spec = s.split(':');
        const edgeSpec = spec
            .slice(1)
            .map((arg) => (arg.match(/^([NSEW]|[NS][EW])$/) ? `.grid-edge-${arg[0]}` : `:${arg}`))
            .join('');
        if (spec[0] === 'grid') return [GRID_SELECTOR, GRID_SELECTOR, ''];
        else if (spec[0] === 'cell')
            return [
                `${GRID_CELL_SELECTOR}${edgeSpec}`,
                `${GRID_CELL_SELECTOR}${edgeSpec} span`,
                '',
            ];
        return [
            `${GRID_SELECTOR} .cell-style-${spec[0]}${edgeSpec}`,
            `${GRID_CELL_SELECTOR}${edgeSpec} .span-style-${spec[0]}`,
            spec[0],
        ];
    }

    _setStyle() {
        let ruleText = '';
        for (let key in this._styleRules) {
            const rules = this._styleRules[key];
            let rule = key + ' {\n';
            for (let prop in rules) {
                let value = rules[prop];
                rule += ` ${prop}: ${value};\n`;
            }
            rule += '}\n\n';
            ruleText += rule;
        }
        console.log(ruleText);
        this._gridStyle.textContent = ruleText;
    }

    dispose(msg: Payload) {
        if (this._conn) {
            this._conn.close();
            this._conn = null;
        }
        this.replaceChildren();
        this.remove();
    }
}

function demo(grid: GridDisplayElement) {
    let cancel = false,
        interval = 0;
    const canceller = () => {
        cancel = true;
        window.clearInterval(interval);
    };
    const data: string[][] = new Array(grid.gridHeight).fill(undefined);
    for (let i = 0; i < grid.gridHeight; i++) data[i] = new Array(grid.gridWidth).fill(' ');
    console.log(data);
    const step = () => {
        data[data.length - 1].forEach((e, i) => {
            if (Math.random() < 0.1) data[data.length - 1][i] = ' ';
        });
        for (let y = data.length - 1; y > 0; y--) {
            data[y].forEach((e, x) => {
                if (e === ' ') {
                    data[y][x] = data[y - 1][x];
                    data[y - 1][x] = ' ';
                }
            });
        }
        //const line = data.pop();
        data[0].forEach((e, i) => {
            data[0][i] = Math.random() < 0.1 ? '*' : ' '; // 'abcdefghijklmnopqrstuvwxyz'.charAt(Math.floor(26 * Math.random()));
        });
        //data.unshift(line);
        data.forEach((line, y) => {
            line.forEach((e, x) => {
                grid.set(x, y, e);
            });
        });
        //if (!cancel) window.requestAnimationFrame(step);
    };
    interval = window.setInterval(step, 100);
    //window.requestAnimationFrame(step);
    return canceller;
}
export const _self = {
    _id: 'grid_display',
    _revision: 0,
    demo,
    init() {
        Messaging.route(
            'grid_create',
            (msg: {
                title: string;
                width: number;
                height: number;
                initial: string;
                id: string;
            }) => {
                const id = msg.id ? `grid-display_${msg.id}` : uniqueId('grid-display');
                const builder = turtleduck.createPanel().panel(GridDisplayElement, id);
                const elt = builder.panelElement;
                console.log('grid_create', builder, elt);
                elt.init(
                    msg.title,
                    Number(msg.width) || 1,
                    Number(msg.height) || 1,
                    msg.initial || '',
                );
                builder.frame('screen').select().done();
                return Promise.resolve({ status: 'ok', id: elt.id });
            },
        );
    },
};

export const GridDisplay = Systems.declare(_self)
    .depends('dom', Messaging)
    .start(() => {
        _self.init();
        return _self;
    })
    .elements(GridDisplayElement)
    .register();

if (import.meta.webpackHot) {
    import.meta.webpackHot.accept();
    import.meta.webpackHot.addDisposeHandler((data) => {
        console.warn(`Unloading ${_self._id}`);
        data['revision'] = revision;
        data['self'] = _self;
    });
}

console.log('GridDisplay', GridDisplay);
