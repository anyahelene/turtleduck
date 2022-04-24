import path from '@isomorphic-git/lightning-fs/src/path'
import MagicPortal from 'magic-portal'
import { turtleduck } from './TurtleDuck';


class StorageContext {
	constructor(fs, cwd) {
		this.fs = fs;
		this._path = path;
		this.cwd = this._path.normalize(cwd);
	}
	realpath(filepath) {
		return this._path.resolve(this.cwd, filepath);
	}
	async withCwd(filepath = '', opts) {
		const ctx = new StorageContext(this.fs, this.cwd);
		return ctx.chdir(filepath, opts);
	}
	async chdir(filepath = '', opts) {
		const newCwd = this.realpath(filepath);
		console.log("cd to: ", newCwd);
		return this.fs.stat(newCwd, opts).then(res => { this.cwd = newCwd; return this; });
	}
	async mkdir(filepath, opts) {
	      return this.fs.mkdir(this.realpath(filepath),opts);
	}
	async rmdir(filepath, opts) {
	      return this.fs.rmdir(this.realpath(filepath),opts);
	}
 	async readdir(filepath = '', opts) {
	console.log("readdir", filepath, opts)
	      const r =  this.fs.readdir(this.realpath(filepath),opts);
	      console.log("r", r);
	      return r;
	}
	async writefile(filepath, data, opts) {
	      return this.fs.writeFile(this.realpath(filepath),data,opts);
	}
	async readfile(filepath, opts) {
	      return this.fs.readFile(this.realpath(filepath),opts);
	}
	async unlink(filepath, opts) {
	      return this.fs.unlink(this.realpath(filepath),opts);
	}
	async rename(oldFilepath, newFilepath, opts) {
	      return this.fs.rename(this.realpath(oldFilepath), this.realpath(newFilepath), opts);
	}
	async stat(filepath = '', opts) {
	      return this.fs.stat(this.realpath(filepath),opts);
	}
	async lstat(filepath = '', opts) {
	      return this.fs.lstat(this.realpath(filepath),opts);
	}
	async symlink(target = '.', filepath, opts) {
	      return this.fs.symlink(target, this.realpath(filepath), opts);
	}
	async readlink(filepath = '', opts) {
	      return this.fs.readlink(this.realpath(filepath),opts);
	}
	async backFile(filepath, opts) {
	      return this.fs.backFile(this.realpath(filepath),opts);
	}
	async du(filepath = '') {
	      return this.fs.du(this.realpath(filepath));
	}
}
class Storage {
	
	constructor() {
		this._MagicPortal = MagicPortal;
		this._initialized = false;
	}
	
	context() {
	  return new StorageContext(this.fs, '/');
	}
	async init(fsConfig = {fsName : 'lfs'}) {
		console.warn("Storage init", fsConfig);
	  this.worker = new Worker(new URL("./StorageWorker.js", import.meta.url));
	  this.portal = new MagicPortal(this.worker);
	  this.worker.addEventListener("message", ({ data }) => console.log("from storage worker:", data));
	  const storage = this;
	  
	  this.ui = {
		async config() {
			return fsConfig;
		},
	    async print(message) {
		if(storage.printer) {
			storage.printer.print(message);
	      }
	    },
	    async progress(evt) {
			const progress = 100 * (evt.total ? evt.loaded / evt.total : 0.5);
			if(evt.total) {
				turtleduck.userlog(`${evt.phase}: ${evt.loaded} / ${evt.total}`);
			} else {
				turtleduck.userlog(`${evt.phase}`);
			}
	      //$("progress-txt").textContent = evt.phase;
	     // $("progress").value = evt.total ? evt.loaded / evt.total : 0.5;
	      return;
	    },
	    async fill(url) {
	      let username = window.prompt("Username:");
	      let password = window.prompt("Password:");
	      return { username, password };
	    },
	    async rejected({ url, auth }) {
	      window.alert("Authentication rejected");
	      return;
	    }
	  };
	  this.portal.set("ui", this.ui, {
	    void: ["print", "progress", "rejected"]
	  });
	  
	  this.fs = await this.portal.get("fsWorker");
	  this.git = await this.portal.get("gitWorker");
	  this._initialized = true;
	  console.log("File system ready:", fsConfig);
	  return this.context();
	}
	
	async clone(url, dest, proxy) {
		this.printer = turtleduck.consolePrinter("git");
		this.printer.print(`Cloning ${url} into /`);
	    await this.git.setDir(dest);
	
	    await this.git.clone({
	      corsProxy: proxy, // "https://cors.isomorphic-git.org",
	      url: url
	    });
	    this.printer = undefined;
		turtleduck.userlog("clone complete");
	}
	
	async info() {
		const requested = turtleduck.getConfig("storage.persistenceRequested") || false;
		const allowed = turtleduck.getConfig("storage.persistenceAllowed") || false;
		if (navigator.storage) {
			var persisted = false;
			var estimate = {};
			if(navigator.storage.persisted)
				persisted = await navigator.storage.persisted();
			if(navigator.storage.estimate)
				estimate = await navigator.storage.estimate();
			estimate.persisted = persisted;
			estimate.requested = requested;
			estimate.allowed = allowed;
			this.persisted = persisted;
			return estimate;
		} else {
			this.persisted = false;
			return {
				persisted: false,
				requested: requested
			}
		}
	}
	
	async requestPersistence() {
		if (navigator.storage && navigator.storage.persist) {
			return navigator.storage.persist().then(persistent => {
				if (persistent)
      				console.info("Storage is persistent: Storage will not be cleared except by explicit user action");
    			else
      				console.warn("Storage is not persistent: Storage may be cleared by the UA under storage pressure.");
      			turtleduck.setConfig({"storage.persistenceRequested":true, "storage.persistenceAllowed":persistent}, "user");
      			return persistent;
  			});
  		} else {
			return false;
		}
	}
	async showInfo() {
		const printer = turtleduck.consolePrinter("git");
	    const branches = await this.git.listBranches({ remote: "origin" });
	    printer.print("BRANCHES:\n" + branches.map(b => `  ${b}`).join("\n") + "\n");
	
	    const files = await this.git.listFiles({});
	    printer.print("FILES:\n" + files.map(b => `  ${b}`).join("\n") + "\n");
	
	    const commits = await this.git.log({});
	    printer.print("LOG:\n" +
	      commits
	        .map(c => `  ${c.oid.slice(0, 7)}: ${c.commit.message}`)
	        .join("\n") +
	      "\n");
	  }
}

export { Storage };

