import Dexie from 'dexie';

var db;
var _db = new Dexie('tdhist');
_db.version(1).stores({
	historyIds: 'session,historyId',
});
const HistoryID = _db.historyIds.defineClass({
		session: String,
		historyId: Number,
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
		this._HistoryID = HistoryID;
		this._currentId = {};
		this._history = {};
	}
	get(session, id) {
		if(typeof id != 'number') {
			return this.currentId(session).then(id => this.get(session, id));
		} else {
			const path = this.getPathName(session, id);
			return this.fs.read(path).then(data => {
					console.log("get", session, id, data);
					if((data === null || data === undefined) && this._history[session]) {
						return Promise.resolve(this._history[session][id] || '');
					} else {
						return Promise.resolve(data);
					}
				}).catch(() => Promise.resolve(this._history[session][id] || ''));
		}
	}
	
	getPathName(session, id) {
		return histPath + session + '/' + id;
	}
	
	put(session, data, id) {
		if(!this._history[session])
			this._history[session] = {};
		
		return this.nextId(session, id).then(id =>	{
			this._history[session][id] = data;
			return this.fs.write(this.getPathName(session, id), data).then(() => Promise.resolve(id));
		});
	}
	rebuild(session) {
		console.log("rebuilding history for", session);
		return this.fs.list(histPath + session + '/').then(files => {
			var max = 1;
			files.forEach(file => {
				console.log("checking", file.name);
				const id = parseInt(file.name);
				if(isFinite(id)) {
					max = Math.max(max, id);
				}
			});
			this._currentId[session] = max;
			if(db) {
				return db.historyIds.put({session: session, historyId: max}).then(() => Promise.resolve(max));
			} else {
				return Promise.resolve(max);
			}
		}).catch(err => {
			console.warn(err);
				if(db) {
				return db.historyIds.put({session: session, historyId: 1}).then(() => Promise.resolve(1));
			} else {
				return Promise.resolve(1);
			}		
		});
	}
	currentId(session) {
		const hist = this;
		function fallback() {
			console.warn("Fallback activated!");
			var currentId = hist._currentId[session];
			if(!currentId)
				currentId = 0;
			hist._currentId[session] = currentId;
			return Promise.resolve(currentId);			
		}
		if(db) {
			return db.historyIds.get(session).then(data => {
				if(data) {
					//console.log("currentId", session, data.historyId);
					this._currentId[session] = data.historyId;
					return Promise.resolve(data.historyId);
				} else {
					return fallback();
				}
			}).catch(() => fallback());
		} else {
			return fallback();
		}
	}
	
	nextId(session, id) {
		if(id) {
			this._currentId[session] = id;
			return Promise.resolve(id);
		}
		if(db) {
			return db.transaction('rw', [db.historyIds],
				() => db.historyIds.get(session).then(data => {
					if(data) {
						data.historyId = data.historyId + 1;
						this._currentId[session] = data.historyId;
						return db.historyIds.put(data).then(() => Promise.resolve(data.historyId));
					} else {
						this._currentId[session] = 1;
						return db.historyIds.add({session: session, historyId: 1}).then(() => Promise.resolve(1));
					}
			}));
		} else {
			this._currentId[session] = (this._currentId[session] || 0) + 1;
			return Promise.resolve(this._currentId[session]);
		}
	}
}

export { History };

