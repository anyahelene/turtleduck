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

class FileHandle {
	constructor(fs, inode, name, kind, file) {
		this.fs = fs;
		this.name = name;
		this.inode = inode;
		this.kind = kind;
		this.file = file;
	}
	
	get() {
		return this.file;
	}
	
	read() {
		return Promise.resolve(this.file.data);
	}
	
	write(newData) {
		return this.file.write(newData);
	}
}
class VirtFile extends FileHandle {
	constructor(info, fs) {
		super(fs, info.inode, info.name, info.kind || 'f');
		this.data = info.data || null;
		this.parent = info.parent || 0;
		this.modtime = info.modtime || new Date();
		this.ver = info.ver || 0;
		this.next = info.next || 0;
		this.files = info.files;
	}
	
	get() {
		return Promise.resolve(this);
	}
	
	log() {
		console.log("log file:", JSON.stringify(this, (key,value) => key.startsWith('_') ? undefined : value));			
	}
	save() {
		
	}
	
	read() {
		return Promise.resolve(this.data);
	}
	
	write(newData) {
		this.data = newData;
		return Promise.resolve(this.inode);
	}
	
	toString() {
		if(this.kind === 'd' || this.kind === 'm')
			return this.name + '/';
		else
			return this.name;		
	}
	list(filter) {
		return this._fs._list_impl(this, filter);
	}
}
const DBFile = db.files.defineClass({
		name: String,
		parent: Number,
		kind: String,
		data: String,
		next: Number,
		ver: Number,
		modtime: Date
});

DBFile.prototype.log = function() {
		console.log("log file:", JSON.stringify(this));	
}
DBFile.prototype.save = function() {
		console.log("save path:", JSON.stringify(this));
		return db.paths.put(this);
}
DBFile.prototype.read = function() {
		console.log("read path:", JSON.stringify(this));
		if(this.next > 0) {
			return db.files.get(this.next)
				.then(nextFile => FileSystem.patch(nextFile.read(), this.data));
		} else {
			return Promise.resolve(this.data);
		}
}

DBFile.prototype.write = function(newData) {
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

DBFile.prototype.toString = function() {
		if(this.kind === 'd')
			return this.name + '/';
		else
			return this.name;
}


class FileSystem {
	
	/** */
	constructor(cwd = 0, project) {
		this.project = project;
		this.cwd = cwd;
		this._File = File;
		this._JSZip = JSZip;
		this._saveAs = saveAs;
		this._debug = false;
	}
	
	context(project, cwd = 0) {
		return new FileSystem(cwd, project);
	}
	list(dirname = '', filter = (e => true)) {
		const fs = this;
		const path = this._filenameToPath(dirname);
		const parent = dirname.startsWith('/') ? 0 : this.cwd;
		console.warn("list", dirname);
		return this._stat_nodev(path, parent, '').then(f => {
			console.warn("f=", f);
			if(f === undefined) {
				return Promise.reject(new Error("file not found: " + dirname));
			} else if(f.kind == 'f') {
				return Promise.resolve(f);
			} else if(f.kind == 'd') {
				return f.fs._list_impl(f, filter);
			} else if(f.kind == 'm') {
				return f.data.list('', filter);
			}
		});
	}
	
	_find(file, path = '', foreach, opts = {}) {
		if(file === undefined) {
			return Promise.resolve();
		} else {
			path = path + file.name;
			if(file.kind == 'f') {
				foreach(path, file);
				return Promise.resolve();
			} else if(file.kind == 'd') {
				path = path + '/'
				foreach(path, file);
				return this._list_impl(file).each(f => this._find(f, path, foreach, opts));
			} else if(file.kind == 'm') {
				if(opts.xdev) { // stay in one filesystem
					foreach(path, file);
					return Promise.resolve();
				} else {
					return file.data._get_impl(0).then(root => file.data._find(root, path, foreach, opts));
				}
			}
		}
	}
	
	_zip(file, zip) {
		if(file === undefined) {
			return Promise.resolve();
		} else {
			if(file.kind == 'f') {
				return file.read().then(data => zip.file(file.name, data));
			} else if(file.kind == 'd') {
				const dir = zip.folder(file.name);
				return file.list().each(f => this._zip(f, dir));
			} else if(file.kind == 'm') {
				// TODO
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
		function mkParentDirs(fs, name, path, parent, fullpath) {
			if(fs._debug)
				console.log("Creating missing directory", name, "in", fullpath);
			return fs._create_impl(name, parent, 'd');
		}
		function mkdir(rev) {
			return fs._stat(path, parent, '', mkParentDirs).then(f => {
					if(f === undefined) {
						return fs._create_impl(name, parent, 'd')
					} else {
						return Promise.resolve(f);
					}
				})
		}
		return this._transact_impl(rev,mkdir);

	}

	_transact_impl(rev, fun) {
		return fun(rev);
	}
	
	_create_impl(name, parent, kind, rev) {
		throw new Error("create not implemented");
	}

	/** */
	_stat_xdev(path, parent, fullpath, handler, rev = -1) {
		return this._stat(path, parent, fullpath, handler, rev, {xdev:true});
	}
	_stat_nodev(path, parent, fullpath, handler, rev = -1) {
		return this._stat(path, parent, fullpath, handler, rev, {nodev:true});
	}
	_stat(path, parent, fullpath, handler, rev = -1, opts = {}) {
		const fs = this;
		while(path[0] == "")
			path.shift();
		if(path.length == 0) {
			return this._get_impl(parent);
		} else {
			let n = path.shift();
			if(fs._debug)
				console.log("looking for", n, path, parent, "in", fullpath);
				
			function check_result(f) {
				console.log("result:", f);
				if(!f.kind) {
					if(fs._debug)
						console.log("not found");
					return Promise.reject(new Error("file not found: " + fullpath + "/" + n));
				} else if(path.length == 0) {
						return Promise.resolve(f);
				} else if(f.kind === 'm'){
					return f.data._stat(path, 0, fullpath + "/" + n, handler, rev, opts);
				} else {
					return f.fs._stat(path, f.inode, fullpath + "/" + n, handler, rev, opts);
				}	
			}
			return this._stat_impl(n, parent, f => {
				console.warn(f);
				if(!f.kind && handler) {
					if(fs._debug)
						console.log("handling: ", f, n, path, parent, fullpath);
					return handler(f.fs, n, path, parent, fullpath, rev).then(check_result);
				} else {
					return check_result(f);				
				}
			});		
		}
	}
	
	_stat_impl(name, parent, fun) {
		throw new Error("stat not implemented");
	}
	
	/** */
	stat(filename = '', opts = {}) {
		const parent = filename.startsWith('/') ? 0 : this.cwd;
		if(this._debug)
			console.log('stat', filename, parent);
		return this._stat(this._filenameToPath(filename), parent, "", undefined, -1, opts);
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
				return f.read();
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
		function create(fs, name, path, parent, fullpath, rev) {
			if(path.length == 0) {
				if(fs._debug)
					console.log("Creating missing file", name, "in", fullpath);
				return fs._create_impl(name, parent, 'f', rev);
			} else {
				if(fs._debug)
					console.log("Creating missing directory", name, "in", fullpath);
				return fs._create_impl(name, parent, 'd', rev);
			}
		}
		function write(rev) {
			return fs._stat(path, parent, '', create, rev).then(f => {console.log("f=>", f); return f.write(data);}); //.then(id => this.files.get(id));
		}
		return this._transact_impl(rev, write);
	}
	
	static patch(data, diff) {
		return "patch(" + data + ", "+ diff + ")";
	}

	static diff(from, to) {
		return "diff("+ from + ", " + to + ")";
	}

}

class DBFileSystem extends FileSystem {
	constructor(db, cwd = 0, project) {
		super(cwd, project);
		this.db = db;
		this.files = db.files;
	}
	context(project, cwd = 0) {
		return new DBFileSystem(this.db, cwd, project);
	}
	
	_create_impl(name, parent, kind, rev) {
		const fs = this;
		if(fs._debug)
			console.log("_create_impl:", name, parent, kind, rev);
		return this.files.add({name: name, data: null, parent: parent,
							kind: kind, next: 0, ver: rev, modtime: new Date()})
						.then(f => this.files.get(f))
						.then(f => this._filehandle(f));

	}
	
	_list_impl(file, filter) {
		if(filter)
			return this.files.where(['parent', 'next']).equals([file.inode, 0]).filter(filter).sortBy('name');
		else
			return this.files.where(['parent', 'next']).equals([file.inode, 0]).sortBy('name');
	}
	_get_impl(inode) {
		return this.files.get(inode).then(f => this._filehandle(f));
	}
	
	_filehandle(f) {
		if(f)
			return new FileHandle(this, f.inode, f.name, f.kind, f);
		else
			return new FileHandle(this, undefined, name, undefined, f);		
	}
	_stat_impl(name, parent, fun) {
		return this.files.where(['name','parent', 'next']).equals([name, parent, 0]) // 
			.first().then(f => fun(this._filehandle(f)));
	}
	_transact_impl(rev, fun) {
		const fs = this;
		if(rev < 0) {
			return db.transaction('rw', [db.files,db.transactions],
				tx => {
					if(fs._debug)
						console.log("transaction: ", tx);
					return db.transactions.add({modtime: new Date()}).then(newrev => fun(newrev));
				});
		} else {
			return fun(rev);
		}			
	}
}

class VirtFileSystem extends FileSystem {
	constructor(cwd = 0, project = undefined) {
		super(cwd, project);
		this.db = {};
		this.files = {0: 
			new VirtFile({inode: 0, name: '', data: null, parent: 0,
						  modtime: new Date(), kind: 'd', ver: 0, files: {}}, this)};
		this.nextInode = -1;
		
	}
	context(project, cwd = 0) {
		return new VirtFileSystem(this.db, cwd, project);
	}
	_list_impl(file, filter) {
		if(!file.files) {
			console.error(file);
			throw new Error(`list(${name}): not a directory: ${file}`);
		}
		return Promise.resolve(file.files);
	}
	
	_create_impl(name, parent, kind, rev) {
		const dir = this.files[parent];
		if(this._debug)
			console.log("_create_impl:", name, parent, kind, rev, dir);
		if(!dir) {
			throw new Error(`create(${name}): parent not found: ${parent}`);
		}
		const inode = this.nextInode--;
		const file = new VirtFile({inode: inode, name: name, data: null, parent: parent,
							kind: kind, next: 0, ver: rev, modtime: new Date()}, this);
		if(kind === 'd')
			file.files = {};
		this.files[inode] = file;
		dir.files[name] = file;
		return Promise.resolve(file);
	}
	_get_impl(inode) {
		return Promise.resolve(this.files[inode]);
	}
	_stat_impl(name, parent, fun) {
		console.log("_stat_impl", name, parent);
		const dir = this.files[parent] || {};
		if(!dir) {
			throw new Error(`stat(${name}): parent not found: ${parent}`);
		} else if(dir.kind === 'd') {
			const file = dir.files[name];
			console.log("_stat_impl", name, parent, '=>', dir, file);
			if(file)
				return fun(file);
			else
				return fun(new FileHandle(this, undefined, name, undefined, file));
		} else if(dir.kind == 'm') {
			console.log("_stat_impl", name, parent, '=> forward to', dir.data);
			return dir.data._stat_impl(name, 0, fun);
		} else {
			throw new Error(`stat(${name}): parent is not a directory: ${dir}`);			
		}
	}
	_transact_impl(rev, fun) {
		return fun(rev);
	}
	
	_mount(name, parent, fs) {
		if(this._debug)
			console.log("_mount:", name, parent, fs);
		this._create_impl(name, parent, 'm', -1).then(mp => {
			mp.data = fs;
		});
	}
}
const fileSystem = new DBFileSystem(db);
const vfs = new VirtFileSystem();
vfs._debug = true;
//await vfs._mount('usr', 0, fileSystem);

export { fileSystem, FileSystem, vfs };

