/// <reference path="./ShellParser_gen.d.ts">
import { parser } from './ShellParser_gen';
import { Tree, TreeCursor, SyntaxNode, SyntaxNodeRef } from '@lezer/common';
import { isString } from 'lodash-es';
import { assert } from '../borb/Common';
import { Hole, html } from 'uhtml';
import { styleTags, tags as t } from '@lezer/highlight';
import {
    foldNodeProp,
    foldInside,
    indentNodeProp,
    LRLanguage,
    LanguageSupport,
} from '@codemirror/language';
import { completeFromList } from '@codemirror/autocomplete';

export const shellHighlighting = styleTags({
    'if then elif else fi': t.controlKeyword,
    'RedirectOp PipeOp LogicOp': [t.operator, t.emphasis],
    'Variable! */Variable': t.variableName,
    'Escaped EscapedSQuote EscapedDQuote bSlash escaped!': t.escape,
    Number: t.number,
    'CommandWord/...': [t.function(t.variableName), t.strong],
    'DQuoted DQuoted/Plain DQuoted/DQuoteChars SQuoted SQuoted/SQuoteChars': [t.string, t.quote],
    '⚠!': t.invalid,
});

const parserWithMetadata = parser.configure({
    props: [
        shellHighlighting,
        indentNodeProp.add({
            Else: (context) => context.column(context.node.from) + context.unit,
        }),
        foldNodeProp.add({
            Elsen: foldInside,
        }),
    ],
});
export const shellLanguage = LRLanguage.define({
    parser: parserWithMetadata,
    languageData: { commentTokens: { line: '#' } },
});

export const shellCompletion = shellLanguage.data.of({
    autocomplete: completeFromList([{ label: 'if', type: 'keyword' }]),
});

export function shell() {
    return new LanguageSupport(shellLanguage, []);
}
