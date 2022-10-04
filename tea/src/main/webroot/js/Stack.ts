import { result } from 'lodash-es';
import { Hole, html } from 'uhtml';

type JSONData = string | number | { [key: string]: JSONData } | JSONData[];
interface Loc {
    kind: string;
    value: string | number;
    offset: number;
}
interface VarEntry {
    kind: string;
    name: string;
    loc: Loc;
    type: [string, number, any];
}
type Frame = { pc: number; end_pc: number; table: VarEntry[] };
interface SubProgram {
    name: string;
    kind: string;
    variables: Record<string, VarEntry>;
    pc: number;
    length: number;
    return: JSONData;
    frame_base: Frame[];
    frame: Frame;
    compilationUnit: string;
}

function div(clazz: string, style: string = '', text: Element | string = ''): HTMLElement {
    const elt = document.createElement('div');
    if (clazz) elt.className = clazz;
    if (style) elt.setAttribute('style', style);
    if (text) {
        if (typeof text === 'string') elt.innerText = text;
        else elt.appendChild(text);
    }
    return elt;
}

export class StackDisplay {
    data: JSONData;
    subPrograms: SubProgram[] = [];
    grid: HTMLElement[];
    cell: HTMLElement;
    row: number;
    legend: Hole[] = [];
    startRow: number;
    fileName: string;
    base: number;
    constructor(data: JSONData) {
        this.data = data;
        this.fileName = data['file'];
        console.warn(this.fileName);
        const compilationUnits: JSONData[] = data['value'];
        compilationUnits.forEach((cu) => {
            const sps: JSONData[] = cu['value'];
            sps.forEach((sp) => {
                sp['compilationUnit'] = cu['name'];
                this.subPrograms.push(sp as unknown as SubProgram);
            });
        });
    }

    display() {
        const elt = div('stack-frames');
        elt.classList.add('hex');
        this.subPrograms.reverse();
        const elts = this.subPrograms.map((sp) => this.displayStackFrame(sp));
        elt.replaceChildren(...elts);
        const head = elt.insertBefore(document.createElement('h1'), elt.firstChild);
        head.innerText = `Stack Frame Layouts: ${this.fileName}`;
        return elt;
    }
    pushByte(address: number, bg: number, name: string, data: string, startAddress: number) {
        const pos = address < 0 ? (0x100000000 + address) % 8 : address % 8;
        // console.log('pushByte pos=%d, addr=%d, name=%s, date=%s', pos, address, name, data);
        if (pos === 0 || !this.cell || this.cell.dataset.name !== name) {
            const title = name ? `${this.formatAddr(address)} (${name})` : `{off} (padding)`;
            if (pos === 0 && this.cell) {
                this.row++; // clear current row
            }
            if (this.cell?.dataset?.name !== name) this.startRow = this.row;
            this.cell = document.createElement('div');
            this.cell.dataset.name = name;
            this.cell.dataset.pos = `${pos}`;
            this.cell.dataset.lastPos = `${pos}`;
            this.cell.dataset.offset = this.formatAddr(address);
            this.cell.dataset.offsetX = this.formatHex(address);
            this.cell.dataset.startOffset = this.formatAddr(startAddress);
            this.cell.textContent = data;
            this.cell.classList.add('byte');
            this.cell.title = title;
            if (bg >= 0) this.cell.classList.add(`bg-${bg}`);
            else this.cell.classList.add('bg-p');
            // if (pos === 0 || !this.row) {
            //     this.row = document.createElement('tr');
            //     this.grid.push(this.row);
            // }
            this.grid.push(this.cell);
            this.cell.style.gridColumnStart = `${pos + 1}`;
            this.cell.style.gridColumnEnd = `${pos + 2}`;
            this.cell.style.gridRow = `${this.row}/${this.row}`;
        } else {
            const title = name
                ? `${this.cell.dataset.startOffset}…${this.formatAddr(address)} (${name})`
                : `${this.cell.dataset.startOffset}…${this.formatAddr(address)} (padding)`;
            this.cell.title = title;
            this.cell.dataset.lastPos = `${pos}`;
            this.cell.style.gridColumnEnd = `${pos + 2}`;
            this.cell.textContent = this.cell.textContent + data;
        }
    }
    displayStackFrame(sp: SubProgram) {
        // console.log(sp.name);
        const startPc = sp.pc;
        const framePc = sp.frame.pc || sp.pc;
        let items: VarEntry[] = [];
        //console.log(JSON.stringify(sp, null, '  '));
        for (const v in sp.variables) {
            items.push(sp.variables[v]);
        }
        items.push(...sp.frame.table);
        items = items.filter((v) => v.loc.kind === 'fbreg');
        items.sort((a, b) => (a.loc.value as number) - (b.loc.value as number));
        const cfa = sp.frame.table.find((e) => (e.name = 'CFA'));
        this.base = cfa?.loc?.offset || 0;
        let address = (items[0]?.loc.value as number) || 0;
        address = address & ~15;
        const table: Hole[] = [];
        this.cell = null;
        this.grid = [];
        this.grid.push(div('byte head', 'grid-area:1/1/1/9', 'Memory'));
        this.grid.push(div('offset head', 'grid-row:1', '@'));
        this.grid.push(div('name head', 'grid-row:1', 'Name'));
        this.grid.push(div('type head', 'grid-row:1', 'Type'));
        this.grid.push(div('size head', 'grid-row:1', 'Size'));
        this.grid.push(div('kind head', 'grid-row:1', 'Kind'));
        this.legend = [];
        this.row = 2;
        let bg = 0;
        let lastTableRow = 0;
        items.forEach((v) => {
            v.loc.value = v.loc.value as number;
            let start = address;
            while (address < (v.loc.value as number)) {
                this.pushByte(address++, -1, '', '', start);
            }
            let bytes = fitName(v.name, v.type[1]);
            bytes.forEach((b) => {
                this.pushByte(address++, bg, v.name, b, v.loc.value as number);
            });
            // get the first unused row after this entry's start row
            lastTableRow = Math.max(lastTableRow + 1, this.startRow);
            const style = `grid-row: ${lastTableRow} / ${lastTableRow}`;
            this.grid.push(div('offset', style, this.formatBoth(v.loc.value as number, 2)));
            this.grid.push(div(`name bg-${bg}`, style, v.name));
            this.grid.push(div('type', style, v.type[0]));
            this.grid.push(div('size', style, `${v.type[1]}`));
            this.grid.push(div('kind', style, v.kind));
            bg = (bg + 1) % 4;
            /*
            let rows = Math.floor((v.type[1] + 3) / 4);
            const span = [];
            console.log(v.name, bytes);
            let b = bytes.slice(0, 4);
            bytes = bytes.slice(4);
            list.push(
                html`<tr
                    >${b.map((s) => html`<td>${s}</td>`)}<td rowspan=${rows}>${v.loc.value}</td
                    ><td rowspan=${rows}>${v.name}</td><td rowspan=${rows}>${v.type[0]}</td
                    ><td rowspan=${rows}>${v.type[1]}</td><td rowspan=${rows}>${v.kind}</td></tr
                >`,
            );
            while (bytes.length >= 4) {
                const b = bytes.slice(0, 4);
                bytes = bytes.slice(4);
                list.push(html`<tr>${b.map((s) => html`<td>${s}</td>`)}</tr>`);
            }
            */
        });
        const startAddress = address;
        let bytes = fitName("…caller's frame", 8);
        let i = 0;
        while (address <= 0) {
            this.pushByte(address++, bg, 'previous', bytes[i++] || '', startAddress);
        }
        if (cfa) {
            lastTableRow = Math.max(lastTableRow + 1, this.row);
            this.grid.push(
                div(
                    'offset',
                    `grid-area: ${lastTableRow} / 9 / ${lastTableRow} / 10;font-style:italic`,
                    '',
                ),
            );
            const loc =
                (cfa.loc.value as string) +
                (cfa.loc.offset < 0 ? `${cfa.loc.offset}` : `+${cfa.loc.offset}`);
            this.grid.push(
                div(
                    'name',
                    `grid-area: ${lastTableRow} / 10 / ${lastTableRow} / 14;font-style:italic`,
                    `${cfa.kind} (${cfa.name}) = ${loc}`,
                ),
            );
        }
        while (address < 8) {
            this.pushByte(address++, bg, 'previous', bytes[i++] || '', startAddress);
        }

        const elt = div('stack-frame');
        const head = elt.appendChild(document.createElement('h2'));
        head.innerText = `stack frame of ${`<${sp.name}+${
            framePc - startPc
        }>`} @ 0x${startPc.toString(16)}`;
        const mem = elt.appendChild(div('memory'));
        mem.replaceChildren(...this.grid);
        return elt;
    }

    formatAddr(address: number, padTo = 0) {
        const sign = ['-', ' ', '+'][Math.sign(address + this.base) + 1];
        return (
            sign +
            Math.abs(address + this.base)
                .toString()
                .padStart(padTo, '0')
        );
    }
    formatHex(address: number, padTo = 0) {
        const sign = ['-', ' ', '+'][Math.sign(address + this.base) + 1];
        return (
            sign +
            Math.abs(address + this.base)
                .toString(16)
                .padStart(padTo, '0')
        );
    }
    formatBoth(address: number, padTo = 0) {
        const elt = document.createElement('span');
        const decElt = elt.appendChild(document.createElement('span'));
        decElt.className = 'dec';
        decElt.innerText = this.formatAddr(address, padTo);
        const hexElt = elt.appendChild(document.createElement('span'));
        hexElt.className = 'hex';
        hexElt.innerText = this.formatHex(address, padTo);
        return elt;
    }
}

export function displayStack(data: JSONData): StackDisplay {
    return new globalThis.StackDisplay(data);
}

function fitName(name: string, size: number) {
    if (name.length > size * 2) {
        name = name.slice(0, size - 1) + '…';
    }
    if (name.length <= size) {
        const right = Math.floor(size - name.length); // / 2);
        // console.log(name, name.length, size, right);
        name = name + ' '.repeat(right); //+ '-'.repeat(size - name.length - right);
        const result = name.split('').map((s) => s + ' ');
        return result;
    } else if (name.length <= size * 2) {
        const right = Math.floor(2 * size - name.length); // / 2);
        // console.log(name, name.length, size, size * 2, right);
        name = name + ' '.repeat(right); //+ '-'.repeat(2 * size - name.length - right);
        const result = [];
        for (let i = 0; i < size; i++) {
            result.push(name.at(i * 2) + name.at(i * 2 + 1));
        }
        // console.log('=>', name, result);
        return result;
    }
    throw new Error('Function not implemented.');
}
if (import.meta.webpackHot) {
    import.meta.webpackHot.accept(
        './ShellParser_gen.js',
        function (outdated) {
            console.log(outdated);
        },
        (err, context) => {
            console.error('HMR failed:', err, context);
        },
    );
    import.meta.webpackHot.accept();
    import.meta.webpackHot.addDisposeHandler((data) => {
        globalThis.StackDisplay = StackDisplay;
    });
}
globalThis.StackDisplay = StackDisplay;
