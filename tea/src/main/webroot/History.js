import Dexie from 'dexie';

const db = new Dexie('tdhist');
db.version(1).stores({
	historyIds: 'session,historyId',
});
const HistoryID = db.historyIds.defineClass({
		session: String,
		historyId: Number,
});
db.open();


class History {
	constructor(fs) {
		this._db = db;
		this.fs = fs;
		this._HistoryID = HistoryID;
	}
	get(session, id) {
		if(typeof id != 'number') {
			return this.currentId(session).then(id => this.get(session, id));
		} else {
			//console.log("get", session, id);
			const path = '/history/' + session + '/' + id;
			return this.fs.read(path);
		}
	}
	
	put(session, data, id) {
		if(id) {
			return this.fs.write('/history/' + session + '/' + id, data)
				.then(() => Promise.resolve(id));
		} else {
			return this.nextId(session)
				.then(id =>	this.fs.write('/history/' + session + '/' + id, data)
					.then(() => Promise.resolve(id)));
		}
	}
		
	currentId(session) {
		return db.historyIds.get(session).then(data => {
			if(data) {
				//console.log("currentId", session, data.historyId);
				return Promise.resolve(data.historyId);
			} else {
				//console.log("currentId", session, 0);
				return Promise.resolve(0);
			}
		});
	}
	
	nextId(session) {
		return db.transaction('rw', [db.historyIds],
			() => db.historyIds.get(session).then(data => {
				if(data) {
					data.historyId = data.historyId + 1;
					return db.historyIds.put(data).then(() => Promise.resolve(data.historyId));
				} else {
					return db.historyIds.add({session: session, historyId: 1}).then(() => Promise.resolve(1));
				}
		}));
	}
}

export { History };

