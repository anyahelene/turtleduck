import Dexie from 'dexie';
import { saveAs } from 'file-saver';
import JSZip from 'jszip';

var async = Dexie.async,
    spawn = Dexie.spawn;


const db = new Dexie('tdfs');
db.version(3).stores({
	files: '++inode,[name+parent+next],[parent+next],modtime,kind,ver',
	transactions: '++rev,modtime'
});
db.on("populate", function() {
	console.log("FileSystem: populating database");
    db.files.add({inode: 0, name: '', data: null, parent: 0, modtime: new Date(), kind: 'd', ver: 0});
});
db.open();

const File = db.files.defineClass({
		name: String,
		parent: Number,
		kind: String,
		data: String,
		next: Number,
		ver: Number,
		modtime: Date
});

File.prototype.log = function() {
		console.log("log file:", JSON.stringify(this));	
}
File.prototype.save = function() {
		console.log("save path:", JSON.stringify(this));
		return db.paths.put(this);
}
File.prototype.read = function() {
		console.log("read path:", JSON.stringify(this));
		if(this.next > 0) {
			return db.files.get(this.next)
				.then(nextFile => FileSystem.patch(nextFile.read(), this.data));
		} else {
			return Promise.resolve(this.data);
		}
}
File.prototype.write = function(newData) {
		console.log("write path:", JSON.stringify(this));
		const path = this;
			return db.transaction('rw', [db.files],
			() => {
				if(path.next > 0) {
					return db.files.get(this.next)
						.then(nextFile => nextFile.write(newData));
				} else if(path.data != null) {
					const last = path;
					return db.files.add({name: path.name, parent: path.parent, data: newData,
													kind: 'f', next: 0, ver: last.ver + 1, modtime: new Date()})
						.then(function(id) {
							console.log("overwrite: ", path.path);
							last.data = FileSystem.diff(newData, last.data);
							last.next = id;
							return db.files.put(last).then(() => Promise.resolve(id));		
						});	
				} else {
					path.data = newData;
					path.modtime = new Date();
					path.kind = 'f';
					console.log("path written:", JSON.stringify(path));					
					return db.files.put(path);
				}
			});
			
}

File.prototype.toString = function() {
		if(this.kind === 'd')
			return this.name + '/';
		else
			return this.name;
}


class FileSystem {
	
	/** */
	constructor(db, cwd = 0, project) {
		this.db = db;
		this.project = project;
		this.files = db.files;
		this.cwd = cwd;
		this._File = File;
		this._JSZip = JSZip;
		this._saveAs = saveAs;
		this._debug = false;
	}
	
	context(project, cwd = 0) {
		return new FileSystem(this.db, cwd, project);
	}
	list(dirname = '', filter = (e => true)) {
		const fs = this;
		const path = this._filenameToPath(dirname);
		const parent = dirname.startsWith('/') ? 0 : this.cwd;
		return this._stat(path, parent, '').then(f => {
			if(f === undefined) {
				return Promise.reject(new Error("file not found: " + dirname));
			} else if(f.kind == 'f') {
				return Promise.resolve(f);
			} else if(f.kind == 'd') {
				if(filter)
					return this.files.where(['parent', 'next']).equals([f.inode, 0]).filter(filter).sortBy('name');
				else
					return this.files.where(['parent', 'next']).equals([f.inode, 0]).sortBy('name');
			}
		});
	}
	
	_find(file, path = '', foreach) {
		if(file === undefined) {
			return Promise.resolve();
		} else {
			path = path + file.name;
			if(file.kind == 'd') {
				path = path + '/'
				foreach(path, file);
				return this.files.where(['parent', 'next']).equals([file.inode, 0])
					.each(f => this._find(f, path, foreach));
			} else {
				foreach(path, file);
				return Promise.resolve();
			}
		}
	}
	
	_zip(file, zip) {
		if(file === undefined) {
			return Promise.resolve();
		} else {
			if(file.kind == 'd') {
				const dir = zip.folder(file.name);
				return this.files.where(['parent', 'next']).equals([file.inode, 0])
					.each(f => this._zip(f, dir));
			} else {
				return file.read().then(data => zip.file(file.name, data));
			}
		}
	}
	/** */
	listOld() {
		const localStorage = window.turtleduck.localStorage;
		let result = [];
		for(let i = 0; i < localStorage.length; i++) {
			let key = localStorage.key(i);
			if(key.startsWith("file://")) {
				result.push(key.substring(7));
			}
		}
		return result;
	}
	
	/** */
	readOld(filename) {
		const localStorage = window.turtleduck.localStorage;
		if(!filename.startsWith("file://")) {
			filename = "file://" + filename;
		}
		return localStorage.getItem(filename);
	}
	
	/** */
	mkdir(filename, rev = -1) {
		const fs = this;
		const parent = filename.startsWith('/') ? 0 : this.cwd;
		const path = this._filenameToPath(filename);
		function mkParentDirs(name, path, parent, fullpath) {
			if(fs._debug)
				console.log("Creating missing directory", name, "in", fullpath);
			return this._mkdir(name, parent);
		}
		function mkdir(rev) {
			return this._stat(path, parent, '', mkParentDirs).then(f => {
					if(f === undefined) {
						return this._mkdir()
					} else {
						return Promise.resolve(f);
					}
				})
		}
		if(rev < 0) {
			return db.transaction('rw', [db.files,db.transactions],
				tx => {
					if(fs._debug)
						console.log("transaction: ", tx);
					return db.transactions.add({modtime: new Date()}).then(newrev => mkdir(newrev));
				});
		} else {
			return mkdir(rev);
		}	
	}
	
	_mkdir(name, parent, rev) {
		const fs = this;
		if(fs._debug)
			console.log("mkdir:", name, parent, rev);
		return this.files.add({name: name, data: null, parent: parent,
							kind: 'd', next: 0, ver: rev, modtime: new Date()})
						.then(id => this.files.get(id));
	}
	
	_mkfile(name, parent, rev) {
		const fs = this;
		if(fs._debug)
			console.log("mkfile:", name, parent, rev);
		return this.files.add({name: name, parent: parent, data: null, kind: 'f',
							next: 0, ver: rev, modtime: new Date()})
						.then(id => this.files.get(id));

	}
	/** */
	_stat(path, parent, fullpath, handler, rev = -1) {
		const fs = this;
		while(path[0] == "")
			path.shift();
		if(path.length == 0) {
			return this.files.get(parent);
		} else {
			let n = path.shift();
			if(fs._debug)
				console.log("looking for", n, path, parent, "in", fullpath);
			return this.files.where(['name','parent', 'next']).equals([n, parent, 0]).first().then(f => {
				if(!f && handler) {
					if(fs._debug)
						console.log("handling: ", n, path, parent, fullpath);
					return handler(n, path, parent, fullpath, rev).then(f => {
						if(!f) {
							return Promise.reject(new Error("file not found: " + fullpath + "/" + n));
						} else if(path.length == 0) {
							return Promise.resolve(f);
						} else {
							return this._stat(path, f.inode, fullpath + "/" + n, handler, rev);
						}});
				}
				if(!f) {
					if(fs._debug)
						console.log("not found");
					return Promise.reject(new Error("file not found: " + fullpath + "/" + n));
				} else if(path.length == 0) {
					return Promise.resolve(f);
				} else {
					return this._stat(path, f.inode, fullpath + "/" + n, handler, rev);
				}
			});
		}
	}
	
	/** */
	stat(filename = '') {
		const parent = filename.startsWith('/') ? 0 : this.cwd;
		if(this._debug)
			console.log('stat', filename, parent);
		return this._stat(this._filenameToPath(filename), parent, "");
	}
	/** */
	chdir(dirname = '') {
		const parent = dirname.startsWith('/') ? 0 : this.cwd;
		if(this._debug)
			console.log('chdir', dirname, parent);
		return this._stat(this._filenameToPath(dirname), parent, "").then(f => { this.cwd = f.inode; return f; });
	}
	
	/** */
	read(filename) {
		const parent = filename.startsWith('/') ? 0 : this.cwd;
		const fs = this;
		const path = this._filenameToPath(filename);
		return this._stat(path, parent, '').then(f => {
			if(f === undefined) {
				return Promise.reject(new Error("file not found: " + filename));
			} else {
				return Promise.resolve(f.data);
			}
		});
	}
	
	_filenameToPath(filename) {
		return filename.split("/");
	}
	/** */
	write(filename, data, rev = -1) {
		const parent = filename.startsWith('/') ? 0 : this.cwd;
		const fs = this;
		const path = this._filenameToPath(filename);
		function create(name, path, parent, fullpath, rev) {
			if(path.length == 0) {
				if(fs._debug)
					console.log("Creating missing file", name, "in", fullpath);
				return fs._mkfile(name, parent, rev);
			} else {
				if(fs._debug)
					console.log("Creating missing directory", name, "in", fullpath);
				return fs._mkdir(name, parent, rev);
			}
		}
		function write(rev) {
			return fs._stat(path, parent, '', create, rev).then(f => {return f.write(data);});
		}
		if(rev < 0) {
			return db.transaction('rw', [db.files,db.transactions],
				tx => {
					if(fs._debug)
						console.log("transaction: ", tx);
					return db.transactions.add({modtime: new Date()}).then(newrev => write(newrev));
				});
		} else {
			return write(rev);
		}

	}
	
	static patch(data, diff) {
		return "patch(" + data + ", "+ diff + ")";
	}

	static diff(from, to) {
		return "diff("+ from + ", " + to + ")";
	}

}

const fileSystem = new FileSystem(db);


export { fileSystem, FileSystem };

