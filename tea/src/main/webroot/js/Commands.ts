import { html, render } from 'uhtml';
import { Message } from '../borb/Messaging';
import Settings, { Config } from '../borb/Settings';
import { timeAgo } from './TimeAgo';
import { turtleduck } from './TurtleDuck';
import { Storage } from './Storage';
import { Session } from '../borb/History';

interface EventMessage extends Message {
    stopPropagation?: () => void;
    preventDefault?: () => void;
}
export async function handleKey(
    key: string,
    button: HTMLElement,
    event: Message | Event,
) {
    function stopPropagation() {
        if (event instanceof Event) event.stopPropagation();
    }
    function preventDefault() {
        if (event instanceof Event) event.preventDefault();
    }
    const page = document.getElementById('page');

    var params = undefined;

    if (event['header']?.msg_type === key) {
        // it's actually a message!
        params = (event as Message).content;
    }
    var m = key.match(/^([a-z]*):\/\/(.*)$/);
    if (m != null) {
        const url = new URL(key);
        params = { path: url.pathname.replace(/^\/\//, '') };
        url.searchParams.forEach((v, k) => (params[k] = v));
        console.log('handleKey decoded url', key, m[1], params);
        key = m[1];
    }
    console.log('handleKey', key, button, event);

    switch (key) {
        case 'explorer':
            return turtleduck.openFiles();
        case 'code':
            // TODO: select focus on current shell tab
            break;
        case 'code-gfx':
            break;
        case 'help':
            document
                .getElementById('page')
                .classList.toggle('show-splash-help');
            break;
        case 'esc':
            document
                .getElementById('page')
                .classList.toggle('show-splash-help', false);
            break;
        case 'projects':
            break;
        case 'quality':
            let code = turtleduck.editor.current().state().sliceDoc(0);
            fetch(
                'https://master-thesis-web-backend-prod.herokuapp.com/analyse',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain;charset=utf-8',
                        'cache-control': 'no-cache',
                        pragma: 'no-cache',
                    },
                    body: code,
                },
            ).then((res) => {
                if (res.ok) {
                    res.json().then((data) => {
                        const txt = document.querySelector('#screen .text');
                        if (txt instanceof HTMLElement)
                            txt.innerText = JSON.stringify(data, null, '    ');
                    });
                }
            });
            break;
        case 'hints': {
            const qs = turtleduck.hints.random();

            turtleduck.displayPopup('HINT', qs[0], qs[1], 'hints');

            break;
        }
        case 'focus': {
            const win = turtleduck[params.path];
            //console.log("focus:", param, win);
            if (win) {
                win.focus();
            }
            break;
        }
        case 'language': {
            turtleduck.client.loadLanguage('python');
            break;
        }
        case 'snap': {
            const config: Config = { mode: 'camera', once: true };
            if (params) {
                config.params = params;
                config.mirror = false;
            }
            const r = turtleduck.openCamera(config);
            stopPropagation();
            preventDefault();
            return r;
        }
        case 'qrscan': {
            const config: Config = { mode: 'qr', once: true };
            if (params) {
                config.params = params;
                config.mirror = false;
            }
            const r = turtleduck.openCamera(config);
            stopPropagation();
            preventDefault();
            return r;
        }
        case 'open-camera': {
            const r = turtleduck.openCamera({ mode: 'camera' });
            stopPropagation();
            preventDefault();
            return r;
        }
        case 'open-qr': {
            const r = turtleduck.openCamera({ mode: 'qr' });
            stopPropagation();
            preventDefault();
            return r;
        }
        case 'tooltip:sessionInfo': {
            const projectName = Settings.getConfig('session.project', '');
            const sessionName = Settings.getConfig('session.name', '');
            const renderShells = (s: Session) =>
                s.shells.map(
                    (sh) =>
                        html`<span class="icon"
                            ><span class=${`shell-type-${sh[0]}`}></span
                            ><span class="icon-text"
                                >${sh[0]} (${sh[1]})</span
                            ></span
                        >`,
                );
            const renderSession = (s: Session) => {
                console.log(s);
                return html`<li class="item-with-icon session-entry">
                    <a
                        onclick=${linkClickHandler}
                        href="${`session://${s.session}`}"
                        >${s.session}
                        <span class="time-ago">(${timeAgo(s.date)})</span>
                        <span class="icon-list">${renderShells(s)}</span></a
                    >
                </li>`;
            };
            return turtleduck.history.sessions().then((ss: Session[]) => {
                render(
                    button,
                    html`<ul class="session-list">
                            ${ss.map(renderSession)}
                        </ul>
                        <dl>
                            <dt>Session</dt>
                            <dd>${sessionName}</dd>
                            <dt>Project</dt>
                            <dd>
                                ${projectName
                                    ? projectName
                                    : html`<input type="text" name="projectName"></input>`}
                            </dd>
                        </dl>`,
                );
            });
            /*return fileSystem.list("/home/projects/").map(files -> {
				HTMLElement l = element("ul");
				for(TDFile file : files) {
					String n = file.name();
					l.appendChild(element("li", element("a", attr("href", "?project=" + n), n)));
				}
				list.appendChild(l);
				return Promise.Util.resolve(list);
			}).mapFailure(err -> Promise.Util.resolve(list));
			*/
        }
        case 'tooltip:storageInfo': {
            return turtleduck.storage.info().then((info) => {
                let askButton: HTMLElement;
                if (!info.persisted) {
                    askButton = html.node`<button id="requestPersistence" type="button">Allow persistent storage</button>`;
                    askButton.addEventListener('click', async function (e) {
                        return handleKey(askButton.id, askButton, e).then(
                            (r) => {
                                console.log('handleKey', r);
                                return r;
                            },
                        );
                    });
                }
                render(
                    button,
                    html.node`<dl><dt>Storage</dt><dd>${
                        info.persisted ? 'persistent' : 'not persistent'
                    }</dd>
					${
                        info.usage
                            ? html`<dt>Usage</dt>
                                  <dd>${info.usage}</dd>`
                            : ''
                    }</dl>
					${askButton || ''}`,
                );
                return button;
            });
        }
        case 'requestPersistence': {
            return turtleduck.storage.requestPersistence().then((res) => {
                button.textContent = res ? 'OK!' : 'Rejected';
            });
        }
        default:
            if (button?.dataset.showMenu) {
                const elt = document.getElementById(button.dataset.showMenu);
                console.log('show: ', elt);
                elt.classList.add('show');
            } else if (button?.classList.contains('not-implemented')) {
                if (Math.random() < 0.5) {
                    button.classList.add('disappear');
                } else {
                    turtleduck.displayPopup(
                        'Warning',
                        'Please do not press this button again.',
                        '',
                        'warning',
                    );
                }
                turtleduck.userlog('Sorry! Not implemented. 😕');
            } else {
                //console.log(key, button, event);
                const r = turtleduck.client.actions.handle(
                    key,
                    { button: button },
                    event,
                );
                //console.log("r =>", r);
                stopPropagation();
                preventDefault();
                return r;
            }
    }
    stopPropagation();
    preventDefault();
    return false;
}

function linkClickHandler(e) {
    e.preventDefault();
    const link = e.target.closest('a');
    if (link && link.href) {
        handleKey(link.href, link, e);
    } else {
        console.error('linkClickHandler: no link found', e, link);
    }
}
