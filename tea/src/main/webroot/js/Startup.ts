import { python } from '@codemirror/lang-python';
import { Messaging } from '../borb';
import { defineLang } from '../borb/CodeMirror';
import Systems from '../borb/SubSystem';
import Terminals from '../borb/Terminal';
import { Chatter } from './Chatter';
import { Languages } from './Language';
import { shell } from './ShellLanguage';
import Storage from './Storage';
import { TShell } from './TShell';
import { turtleduck } from './TurtleDuck';

defineLang('shell', shell());

Systems.waitFor(Messaging).then(async () => {
    turtleduck.chatter = new Chatter();
    turtleduck.builtinLanguages = { chat: turtleduck.chatter };

    await Systems.waitFor(Storage);
    await Storage.cwd.chdir('/work', true);
    turtleduck.tshell = new TShell(Storage.cwd);
    turtleduck.builtinLanguages['tshell'] = turtleduck.tshell;

    await Systems.waitFor(Terminals);
    console.warn('Terminals ready!');

    const sh = (globalThis.sh = Languages.get('tshell'));
    await sh.load();
    sh.mainTerminal.println(sh.icon + ' TShell ready!');
    sh.mainTerminal.focus();
    if (false) {
        const py = (globalThis.py = Languages.get('python'));
        sh.mainTerminal.println(py.icon + ' Loading Python...');
        await py.load(null, true);
        sh.mainTerminal.println(py.icon + ' Python ready!');
    }
    if (window.location.search) {
        const usp = new URLSearchParams(window.location.search);
        const tshellCmds = usp.getAll('tshell');
        tshellCmds.forEach((cmd) => {
            queueMicrotask(() => {
                sh.mainShell.onEnter(cmd, -1, null, null);
            });
        });
    }

    const shell = { runCommand: (cmd: string) => cmd };

    const display = document.getElementById('display'); // HTML element where program will be displayed
    if (window.location.search) {
        // do we have URL parameters?
        // decode parameters
        const usp = new URLSearchParams(window.location.search);
        // display the program text
        const program = usp.get('program');
        display.innerHTML = '<pre class="javaCode">' + program + '</pre>';
        // run the commands
        const shellCmds = usp.getAll('commands');
        shellCmds.forEach((cmd) => {
            shell.runCommand(cmd);
        });
    }

    //  globalThis.chat = await Languages.create('chat');
});
