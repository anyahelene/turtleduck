import { parse, serialize, URIComponents } from 'uri-js';

export class Location {
    static readonly FRAGMENT_PATTERN =
        /^(\d+)?(?:L(\d+))?(?:C(\d+))?(?:\+(\d+))?(?:@(\d+))?$/;
    readonly _start?: number;
    readonly _length?: number;
    readonly _pos?: number;
    readonly _uri: URIComponents;
    readonly _line?: number;
    readonly _column?: number;
    _endColumn: number;

    static fromString(locstr: string) {
        if (locstr) {
            try {
                return new Location(parse(locstr));
            } catch (e) {
                console.error('invalid URI: ', locstr, e);
            }
        }
    }

    constructor(
        uri: URIComponents,
        start?: number,
        length?: number,
        pos?: number,
        line?: number,
        column?: number,
    ) {
        this._uri = uri;

        if (uri.fragment) {
            const hash = uri.fragment || '';
            const match = hash.match(Location.FRAGMENT_PATTERN);
            if (match?.[1]) start = parseInt(match[1]);
            if (match?.[2]) line = parseInt(match[2]);
            if (match?.[3]) column = parseInt(match[3]);
            if (match?.[4]) length = parseInt(match[4]);
            if (match?.[5]) pos = parseInt(match[5]);
        }
        if (uri.query) {
            const params = new URLSearchParams(uri.query);
        }
        let frag = '';
        if (!isNaN(start) && start >= 0) {
            this._start = start;
            frag = `${start}`;
        }
        if (!isNaN(line) && line >= 1) {
            this._line = line;
            frag = `${frag}L${line}`;
        }
        if (!isNaN(column) && column >= 1) {
            this._column = column;
            frag = `${frag}C${column}`;
        }
        if (!isNaN(length) && length >= 0) {
            this._length = length;
            frag = `${frag}+${length}`;
        }
        if (!isNaN(pos) && pos >= 0) {
            this._pos = pos;
            frag = `${frag}@${pos}`;
        }
        if (frag) uri.fragment = frag;
    }

    public get scheme(): string {
        return this._uri.scheme;
    }
    public get host(): string {
        return this._uri.host;
    }
    public get path(): string {
        return this._uri.path;
    }

    public get start(): number {
        return this._start;
    }

    public get end(): number {
        return typeof this._start === 'number'
            ? this._start + this._length
            : undefined;
    }

    public get length(): number {
        return this._length;
    }

    toString() {
        return serialize(this._uri);
    }
    static fromParts(
        scheme: string,
        host: string,
        path: string,
        inputOrStart?: string | number,
        length?: number,
        pos?: number,
    ) {
        let start: number = undefined;
        if (typeof inputOrStart === 'number') {
            start = inputOrStart;
        } else if (typeof inputOrStart === 'string') {
            start = 0;
            length = inputOrStart.length;
        }

        return new Location(
            {
                scheme,
                host,
                path,
            },
            start,
            length,
            pos,
        );
    }

    public forward(n: number): Location {
        if (n == 0) {
            return this;
        }
        if (!this.hasStart()) {
            throw new Error('Not tracing position');
        }
        if (this.hasLength() && n > this._length) {
            throw new Error('End of source reached');
        }
        return new Location(
            this._uri,
            this._start + n,
            this._length - n,
            this._pos - n,
        );
    }

    public shorten(n: number) {
        if (n === 0) {
            return this;
        }
        if (!this.hasStart()) {
            throw new Error('Not tracing position');
        }
        if (this.hasLength() && n > this._length) {
            throw new Error('End of source reached');
        }
        return new Location(
            this._uri,
            this._start,
            this._length - n,
            this._pos,
        );
    }

    public withPos(p: number) {
        if (p > this._length) {
            throw new Error('End of source reached');
        }
        return new Location(this._uri, this._start, this._length, p);
    }

    /**
     * Keep n characters at the end of the string.
     *
     * Afterwards, length will be n, and start will be origStart + (origLength - n)
     *
     * @param n
     * @return
     */
    public keep(n: number): Location {
        if (n == length) {
            return this;
        }
        if (!this.hasStart()) {
            throw new Error('Not tracing position');
        }
        if (this.hasLength() && n > length) {
            throw new Error('End of source reached');
        }
        return new Location(
            this._uri,
            this._start + (this._length - n),
            n,
            this._pos - (this._length - n),
        );
    }

    public substring(s: string): string {
        if (!this.hasStart()) {
            return s;
        } else if (!this.hasLength()) {
            return s.substring(this._start);
        } else {
            return (s + '_').substring(this._start, this._start + this._length);
        }
    }

    public before(s: string): string {
        if (this.hasStart()) {
            return s.substring(0, this._start);
        } else if (this.hasLine()) {
            let [start, end, line] = nthLineOf(this._line, s);
            if (!isNaN(this._column)) start = start + this._column - 1;
            return s.substring(0, start);
        } else {
            return '';
        }
    }

    public after(s: string): string {
        if (this.hasStart() && this.hasLength()) {
            return s.substring(this._start + this._length);
        } else if (this.hasLine() && this.hasLength()) {
            let [start, end, line] = nthLineOf(this._line, s);
            if (!isNaN(this._column)) start = start + this._column - 1;
            return s.substring(start + this._length);
        }
    }

    public splitLine(s: string, line?: string): [string, string, string] {
        console.log('splitline', s, this);
        if (!isNaN(this._line)) {
            if (line === undefined) line = nthLineOf(this._line, s)[2];
            let startCol = 0,
                endCol = line.length;
            console.log(startCol, endCol);
            if (!isNaN(this._column)) {
                startCol = this._column - 1;
                endCol = startCol;
                console.log('with column', startCol, endCol);
            }

            if (!isNaN(this._endColumn)) {
                endCol = this._endColumn;
                console.log('with end columne', startCol, endCol);
            }

            const r: [string, string, string] = [
                line.substring(0, startCol),
                line.substring(startCol, endCol),
                line.substring(endCol),
            ];
            console.log('=>', r);
            return r;
        }
        if (!isNaN(this._start)) {
            const start = this._start,
                stop = this._start + (this._length || 1);
            let begin = s.lastIndexOf('\n', start);
            if (begin < 0) begin = 0;
            let end = s.indexOf('\n', stop);
            if (end < 0) end = s.length;
            return [
                s.substring(begin, start),
                s.substring(start, stop),
                s.substring(stop, end),
            ];
        }
        return ['', s, ''];
    }

    public relativeRegion(regStart: number, regLength: number) {
        if (regStart == 0 && regLength == length) {
            return this;
        }
        if (this.hasStart()) {
            throw new Error('Not tracing position');
        }
        if (length >= 0 && regStart + regLength > length) {
            throw new Error('End of source reached');
        }
        return new Location(
            this._uri,
            this._start + regStart,
            regLength,
            this._pos - regStart,
        );
    }

    public hasStart(): boolean {
        return !isNaN(this._start);
    }
    public hasLength(): boolean {
        return !isNaN(this._length);
    }
    public hasLine(): boolean {
        return !isNaN(this._line);
    }
}

function nthLineOf(n: number, s: string): [number, number, string] {
    let l = 1,
        start = 0,
        end = 0;
    while (l++ < n) {
        const i = s.indexOf('\n', start);
        if (i >= 0) start = i + 1;
        else {
            start = s.length;
            break;
        }
    }

    end = s.indexOf('\n', start);
    if (end < 0) end = s.length;
    return [start, end, s.substring(start, end)];
}
