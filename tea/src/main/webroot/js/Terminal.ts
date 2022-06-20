import { html } from "uhtml";

export type Printer = { print: (text: string) => void };

declare const turtleduck: any;
export const Terminal = {
    appendToConsole(style: string) {
        if (turtleduck.shellComponent) {
            const shell = turtleduck.shellComponent.current();
            if (shell) {
                return shell.terminal.appendBlock(style);
            }
        }
    },

    consolePrinter(style: string) {
        if (turtleduck.shellComponent) {
            const shell = turtleduck.shellComponent.current();
            if (shell) {
                const element = shell.terminal.appendBlock(style);
                return turtleduck.elementPrinter(element, null, () => shell.terminal.scrollIntoView());
            }
        }
    },

    elementPrinter(element: HTMLElement, style?: string, afterPrint?: () => void) : Printer {
        const wrapperElt = element.closest('main');
        const outputContainer = element.closest('.terminal-out-container');
        if (typeof style === 'string') {
            element = element.appendChild(html.node`<div class=${style}></div>`);
        }
        if (!afterPrint && wrapperElt && outputContainer) {
            afterPrint = () => {
                outputContainer.scrollTop = 0;
                wrapperElt.scrollTop = wrapperElt.scrollHeight - wrapperElt.offsetHeight;
            };
        }
        console.log(element, style, afterPrint);
        let cr = false;
        return {
            print: (text: string) => {
                let old = element.textContent ?? '';
                if (cr) {
                    old = old.trim().replace(/.+$/, "");
                }
                cr = text.endsWith("\r");
                element.textContent = old + text;
                if (afterPrint) {
                    afterPrint();
                }
            }
        }
    }
}

export default Terminal;
