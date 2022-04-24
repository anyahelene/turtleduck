import Dexie from 'dexie';

function make_key(s,id) {
	const i = s.indexOf('/');
	const key = i > 0 ? [s.slice(0,i), s.slice(i+1)] : [s, ''];
	if(id !== undefined)
		key.push(id);
	return key;
}

var db;
var _db = new Dexie('tdhist');
_db.version(5).stores({
	historyIds: 'session,historyId',
	historyEntries: '[session+historyId],line'
});

_db.version(6).stores({
	historyIds: 'session,historyId',
	historyEntries: '[session+historyId],line',
	sessions: '[session+shell]',  // was 'session,historyId'
	entries: '[session+shell+id]' // was '[session+historyId],line'
}).upgrade(tx => {
	return tx.table("historyIds").toCollection().each(async entry => {
		console.log("upgrading", entry);
		const key = make_key(entry.session);
		console.log("=>", {session: key[0], shell: key[1], id: entry.historyId, date: new Date()});
		await _db.sessions.put({session: key[0], shell: key[1], id: entry.historyId, date: new Date()});
	})
	.then(tx.table("historyEntries").toCollection().each(async entry => {
		const key = make_key(entry.session);
		await _db.entries.put({session: key[0], shell: key[1], id: entry.historyId, data: entry.line});
	}));
});
_db.version(7).stores({
	historyIds: 'session,historyId',
	historyEntries: '[session+historyId],line',
	sessions: '[session+shell]',  // was 'session,historyId'
	entries: '[session+shell+id],[session+shell]' // was '[session+historyId],line'
})

const Session = _db.sessions.defineClass({
		session: String,
		shell: String,
		id: Number,
		date: Date
});
const Entry = _db.entries.defineClass({
		session: String,
		shell: String,
		id: Number,
		data: String
});

_db.open().then(dbobj => {
	db = dbobj;
}).catch(err => {
	db = null;
	console.warn("HistoryDB open failed", err);
});

const histPath = '/home/history/';
class History {
	constructor(fs) {
		this.fs = fs;
		this._db = _db;
		this._make_key = make_key;
		this._sessions = {};
		this._history = {};
	}
	get(session, id) {
		if(!this._history[session])
			this._history[session] = {};
		if(typeof id != 'number') {
			return this.currentId(session).then(id => this.get(session, id));
		} else if(db) {
			return db.entries.get(make_key(session, id))
				.then(entry => Promise.resolve(entry.data))
				.catch((e) => Promise.resolve(this._history[session][id] || ''));
		} else {
			return this._history[session][id] || '';
		}
	}
	
	getPathName(session, id) {
		return histPath + session + '/' + id;
	}
	
	put(session, data, id) {
		if(!this._history[session])
			this._history[session] = {};
		
		if(db) {
			const key = make_key(session);
			return db.transaction('rw', [db.sessions, db.entries],
				() => db.sessions.get(key).then(x => {
					const latest = x ? x : {session: key[0], shell: key[1], id: 0};
					const thisId = id ? id : latest.id + 1;
					console.log("latest:", latest, "thisId:", thisId);
					latest.id = Math.max(thisId, latest.id);
					latest.date = new Date();
					this._sessions[session] = latest.id;
					this._history[session][thisId] = data;
					return db.sessions.put(latest)
						.then(() => db.entries.put({session: key[0], shell: key[1], id: thisId, data: data}))
						.then(() =>	Promise.resolve(thisId));
			}));
		} else {
			const oldId = (this._sessions[session] || 0);
			const thisId = id ? id : oldId + 1;
			this._sessions[session] = Math.max(thisId, oldId);
			this._history[session][thisId] = data;
			return Promise.resolve(thisId);
		}
	}

	async rebuildAll() {
		const sessions = await this.fs.list(histPath);
		sessions.forEach(async sess => {
			const shells = await this.fs.list(histPath + "/" + sess.name);
			shells.forEach(async shell => {
				if(shell.kind == 'd') {
					var session = sess.name + '/' + shell.name;
					await this.rebuild(session);
					
				}
				
			});
		});
	}
	rebuild(session) {
		console.log("rebuilding history for", session);
		const key = make_key(session);
		return this.fs.list(histPath + session + '/').then(async files => {
			var max = 1;
		
			files.forEach(async file => {
				console.log("checking", file.name);
				const id = parseInt(file.name);
				if(isFinite(id)) {
					max = Math.max(max, id);
					await db.entries.put({session: key[0], shell: key[1], id: id, data: file.data});
				}
			});
			this._sessions[session] = max;
			if(db) {
				return db.sessions.put({session: key[0], shell: key[1], id: max, date: new Date()}).then(() => Promise.resolve(max));
			} else {
				return Promise.resolve(max);
			}
		}).catch(err => {
			console.warn(err);
				if(db) {
				return db.sessions.put({session: key[0], shell: key[1], id: 1, date: new Date()}).then(() => Promise.resolve(1));
			} else {
				return Promise.resolve(1);
			}		
		});
	}
	list(session) {
		const key = make_key(session);
		return db.entries.where({session: key[0], shell: key[1]}).toArray();
	}
	sessions() {
		return db.sessions.toArray();
	}
	currentId(session) {
		const hist = this;
		function fallback() {
			console.warn("Fallback activated!");
			var currentId = hist._sessions[session];
			if(!currentId)
				currentId = 0;
			hist._sessions[session] = currentId;
			return Promise.resolve(currentId);			
		}
		if(db) {
			return db.sessions.get(make_key(session)).then(data => {
				if(data) {
					console.log("currentId", session, data.id);
					this._sessions[session] = data.id;
					return Promise.resolve(data.id);
				} else {
					return fallback();
				}
			}).catch(() => fallback());
		} else {
			return fallback();
		}
	}
	

}

export { History };

