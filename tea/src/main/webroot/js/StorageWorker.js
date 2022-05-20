//import git from 'isomorphic-git'
//import http from 'isomorphic-git/http/web'
//import MagicPortal from 'magic-portal'
//import LightningFS from '@isomorphic-git/lightning-fs'
const LightningFS = require("@isomorphic-git/lightning-fs");
const Git = require("isomorphic-git");
const GitHttp = require("isomorphic-git/http/web/index.js");
const MagicPortal = require("magic-portal/dist/index.js");

console.log("mp:", MagicPortal);
console.log("lfs:", LightningFS);
console.log("git:", Git);
console.log("githttp:", GitHttp);

const portal = new MagicPortal(self);
self.addEventListener("message", ({ data }) => console.log("from main thread: ", data));
console.warn("StorageWorker init", self, self.performance.timeOrigin, self.foo);


(async () => {
  let ui = await portal.get("ui");
  let config = await ui.config();
  let fs = new LightningFS(config.fsName || 'fs', config);
  let pfs = fs.promises;

  portal.set("fsWorker", {
    mkdir: async (filepath, opts) => pfs.mkdir(filepath, opts), // (mode) -> void
    rmdir: async (filepath, opts) => pfs.rmdir(filepath, opts), // -> void
    readdir: async (filepath, opts) => pfs.readdir(filepath, opts), // -> string[]
    writeFile: async (filepath, data, opts) => pfs.writeFile(filepath, data, opts), // (mode, encoding) -> void
    readFile: async (filepath, opts) => pfs.readFile(filepath, opts), // (url, urlauto, encoding) -> string | uint8array
    unlink: async (filepath, opts) => pfs.unlink(filepath, opts), // -> void
    rename: async (oldFilepath, newFilepath, opts) => pfs.rename(oldFilepath, newFilepath, opts), // -> void
    stat: async (filepath, opts) => pfs.stat(filepath, opts), // -> Stats
    lstat: async (filepath, opts) => pfs.lstat(filepath, opts), // -> Stats
    symlink: async (target, filepath, opts) => pfs.symlink(target, filepath, opts), // -> void
    readlink: async (filepath, opts) => pfs.readlink(filepath, opts), // -> string
    backFile: async (filepath, opts) => pfs.backFile(filepath, opts), // (mode) -> void
    du: async (filepath) => pfs.du(filepath), // -> number
  });
  let dir = '/git';
  portal.set("gitWorker", {
    setDir: async _dir => {
      dir = _dir;
    },
    clone: async args => {
      return git.clone({
        ...args,
        fs,
        http: GitHttp,
        dir,
        onProgress(evt) {
          ui.progress(evt);
        },
        onMessage(msg) {
          ui.print(msg);
        },
        onAuth(url) {
          console.log(url);
          return ui.fill(url);
        },
        onAuthFailure({ url, auth }) {
          return ui.rejected({ url, auth });
        }
      });
    },
    listBranches: args => git.listBranches({ ...args, fs, dir }),
    listFiles: args => git.listFiles({ ...args, fs, dir }),
    log: args => git.log({ ...args, fs, dir })
  });
})();

