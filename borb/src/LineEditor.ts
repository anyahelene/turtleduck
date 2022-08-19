import {
    indentLess,
    indentMore,
    insertNewlineAndIndent,
} from '@codemirror/commands';
import { insertNewlineContinueMarkup } from '@codemirror/lang-markdown';
import {
    getIndentation,
    IndentContext,
    syntaxTree,
} from '@codemirror/language';
import {
    ChangeSpec,
    EditorSelection,
    EditorState,
    StateCommand,
    Transaction,
} from '@codemirror/state';
import { EditorView, keymap } from '@codemirror/view';
import { html } from 'uhtml';
import { sysId } from './Common';
import Editor, {
    fontConfig,
    isBetweenBrackets,
    stdConfig,
    TDEditor,
} from './Editor';
import { LineHistory, HistorySession, Entry } from './LineHistory';
import Settings from './Settings';
import SubSystem from './SubSystem';
import type { BorbTerminal } from './Terminal';
const revision: number =
    import.meta.webpackHot && import.meta.webpackHot.data
        ? import.meta.webpackHot.data['revision'] + 1
        : 0;
const previousVersion: typeof _self =
    import.meta.webpackHot && import.meta.webpackHot.data
        ? import.meta.webpackHot.data['self']
        : undefined;
interface EditorService {
    createLineEditor(
        elt: HTMLElement,
        text: string,
        lang: string,
        handler: (key: string, state: unknown) => boolean,
        root: Document | ShadowRoot,
    ): Editor;
}
interface Shell {
    eval(line: string, outputElement?: HTMLElement): Promise<number>;
    history: HistorySession;
    languageName: string;
    shellName: string;
}
interface Language {
    name: string;
    shellName: string;
    editMode: string;
}
interface Editor {
    focus(): void;
    attach(elt: HTMLElement): void;
    dispose(): void;
}
interface Prompt {
    more?: string;
}
const historyKeys = {
    arrowUp: 'prev',
    arrowDown: 'next',
    pageUp: 'first',
    pageDown: 'last',
};
export class LineEditor {
    public editor: TDEditor;
    public history: HistorySession;
    public terminal: BorbTerminal;
    histCommands: { [key: string]: () => Promise<Entry> };
    private _seq = 1;
    public beforeEnter: (line: string, LineEditor: this) => boolean;
    public afterEnter: (line: string, LineEditor: this) => void;
    public onEnter: (
        line: string,
        lineId: number,
        outputElement: HTMLElement,
        lineEditor: this,
    ) => Promise<Prompt>;
    focus(): void {
        this.editor.focus();
    }

    constructor(term: BorbTerminal, lang?: Language) {
        this.terminal = term;
        this.histCommands = {
            arrowUp: () => this.history.prev(),
            arrowDown: () => this.history.next(),
            pageUp: () => this.history.first(),
            pageDown: () => this.history.last(),
        };
        this.terminal.status = 'waiting';

        const shellName = lang?.shellName || '';
        const settings = SubSystem.getApi<typeof Settings>('borb/settings');
        const session = settings.getConfig('session.name', '_');
        const historyId = (session + '/' + shellName).replace(' ', '');
        console.log('waiting for terminal and history...');
        Promise.all([
            this.terminal.whenReady.then(() => {
                console.log('terminal ready');
                this.terminal.lineEditor = this;
                this.editor = createLineEditor(
                    this.terminal.inputElement,
                    '',
                    lang?.editMode || 'plain',
                    (key, state, dispatch) =>
                        this.handleKey(key, state, dispatch),
                    this.terminal.shadowRoot,
                );
            }),
            LineHistory.forSession(historyId).then((hist) => {
                console.log('history ready');
                this.history = hist;
            }),
        ]).then(() => {
            this.terminal.status = 'ready';
        });
    }

    handleKey(
        key: string,
        state: EditorState,
        dispatch: (tr: Transaction) => void,
    ): boolean {
        console.log('Terminal input:', key);
        const line = state.sliceDoc(0);
        if (!this.history) return false;
        if (key === 'enter') {
            if (!this.beforeEnter || this.beforeEnter(line, this)) {
                this.enter(state, dispatch)
                    .then((res) => {
                        if (res && res.more) {
                            this.history.edit(res.more);
                            replaceDoc(
                                this.editor.state(),
                                this.editor.view.dispatch,
                                res.more,
                            );
                        }
                        if (this.afterEnter) this.afterEnter(line, this);
                        return res;
                    })
                    .catch((err) => {
                        console.error('INTERNAL ERROR', err);
                        this.terminal.print(`*** Internal error: ${err}`);
                    })
                    .finally(() => (this.terminal.status = 'ready'));
                return true;
            } else {
                return false;
            }
        } else if (this.histCommands[key]) {
            this.history
                .edit(line)
                .then(this.histCommands[key])
                .then((entry) => {
                    console.log('%s, got %o', key, entry);
                    if (entry) {
                        replaceDoc(state, dispatch, entry.data, -1);
                    }
                });
            return true;
        }
        return false;
    }

    async enter(state: EditorState, dispatch: (tr: Transaction) => void) {
        this.terminal.status = 'running';
        const line = state.sliceDoc();
        const id = this.history ? await this.history.enter(line) : this._seq++;
        const elt = this.editor.highlightTree(
            html.node`<span class="prompt" data-user="">[${id}]</span>`,
        );
        elt.classList.add('block');
        replaceDoc(state, dispatch, '');
        this.terminal.printElement(elt);
        if (this.onEnter) {
            const ret = this.onEnter(line, id, elt, this);
            return ret;
        }
    }
    disableHistory(): void {
        //
    }
}

function replaceDoc(
    state: EditorState,
    dispatch: (tr: Transaction) => void,
    data: string,
    cursor = 0,
) {
    const text = state.toText(data);
    const len = text.length;
    if (cursor < 0) cursor = cursor + 1 + len;
    cursor = Math.max(0, Math.min(len, cursor));
    dispatch(
        state.update({
            changes: {
                from: 0,
                to: state.doc.length,
                insert: text,
            },
            selection: EditorSelection.cursor(cursor),
            scrollIntoView: true,
        }),
    );
}
export const createLineEditor = function (
    elt: HTMLElement,
    text: string,
    lang: string,
    handler: (
        key: string,
        state: EditorState,
        dispatch: (tr: Transaction) => void,
    ) => boolean,
    root: Document | ShadowRoot = document,
) {
    const outer = elt;
    const elts = elt.getElementsByClassName('wrapper');
    if (elts[0]) elt = elts[0] as HTMLElement;

    const enter: StateCommand = ({ state, dispatch }) => {
        let isComplete = true;
        const changes = state.changeByRange((range) => {
            console.groupCollapsed('enter key pressed at ', range);
            try {
                const text = state.sliceDoc(range.from);
                console.log('text: ', JSON.stringify(text));
                // check if we're in the middle of the text
                if (
                    text.length > 0 &&
                    (!text.match(/^\r?\n/) || text.startsWith('/'))
                ) {
                    // TODO: line-break setting?
                    isComplete = true;
                    return { range };
                }
                const explode =
                    range.from == range.to &&
                    isBetweenBrackets(state, range.from);
                const cx = new IndentContext(state, {
                    simulateBreak: range.from,
                    simulateDoubleBreak: !!explode,
                });
                let indent = getIndentation(cx, range.from);
                console.log('indent0: ', indent);
                if (indent == null)
                    indent = /^\s*/.exec(state.doc.lineAt(range.from).text)[0]
                        .length;
                console.log('indent1: ', indent, 'explode: ', explode);
                if (indent || explode) isComplete = false;

                const tree = syntaxTree(state);
                console.log('tree', tree);
                const context = tree.resolve(range.anchor);
                console.log('context', context);
                console.log(
                    'enter key pressed: from=%o, to=%o, anchor=%o, head=%o, state=%o',
                    range.from,
                    range.to,
                    range.anchor,
                    range.head,
                    state,
                );
                return { range };
            } finally {
                console.groupEnd();
            }
        });
        console.log('changes: ', changes);
        if (isComplete) {
            return handler('enter', state, dispatch);
        } else {
            return insertNewlineAndIndent({ state, dispatch });
        }
    };
    const tab: StateCommand = ({ state, dispatch }) => {
        const changes = state.changeByRange((range) => {
            console.log(
                'tab key pressed: from=%o, to=%o, anchor=%o, head=%o, state=%o',
                range.from,
                range.to,
                range.anchor,
                range.head,
                state,
            );
            const context = syntaxTree(state).resolve(range.from);
            console.log('context', context);
            return { range };
        });
        console.log('changes: ', changes);
        return indentMore({ state, dispatch });
    };
    const shiftEnter =
        lang === 'markdown' || lang === 'chat'
            ? insertNewlineContinueMarkup
            : insertNewlineAndIndent;
    const arrowUp: StateCommand = ({ state, dispatch }) => {
        return handler('arrowUp', state, dispatch);
    };

    const arrowDown: StateCommand = ({ state, dispatch }) => {
        return handler('arrowDown', state, dispatch);
    };

    const editor = new TDEditor(
        outer.id,
        outer,
        elt,
        text,
        lang,
        [
            fontConfig(elt),
            stdConfig(),
            EditorView.theme({
                '.cm-lineNumbers .cm-gutterElement': {
                    display: 'none',
                },
            }),
            keymap.of([
                { key: 'ArrowUp', run: arrowUp },
                { key: 'ArrowDown', run: arrowDown },
            ]),
        ],
        [
            keymap.of([
                { key: 'Enter', run: enter, shift: shiftEnter },
                { key: 'Tab', run: tab, shift: indentLess },
            ]),
        ],
        root,
    );

    editor._after_paste = () => {
        outer.scrollIntoView({ block: 'end', inline: 'nearest' });
    };
    return editor;
};

const _self = {
    _id: sysId(import.meta.url),
    _revision: revision,
    LineEditor,
};

export const LineEditors = SubSystem.declare(_self)
    .reloadable(true)
    .depends('dom', LineHistory, Editor)
    .elements()
    .register();
export default LineEditors;

if (import.meta.webpackHot) {
    import.meta.webpackHot.accept();
    import.meta.webpackHot.addDisposeHandler((data) => {
        console.warn(`Unloading ${_self._id}`);
        data['revision'] = revision;
        data['self'] = _self;
    });
}
