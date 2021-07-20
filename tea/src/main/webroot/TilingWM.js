
import { Component } from './Component';


function addHeader(id, elt) {
	const head = document.createElement('header');
	head.id = elt.id + '-head';
	elt.insertBefore(head, elt.firstElementChild);
	const tabs = document.createElement('nav');
	tabs.id = id + '-tabs';
	tabs.className = 'tabs';
	head.appendChild(tabs);
	const tools = document.createElement('nav');
	tools.id = id + '-tools;'
	tools.className = 'toolbar';
	head.appendChild(tools);
}
class Area {
	constructor(startX, endX, startY, endY) {
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
	}
	start(dir) {
		return dir === 'H' ? this.startX : this.startY;
	}
	end(dir) {
		return dir === 'H' ? this.endX : this.endY;
	}	
	startAt(dir, value) {
		return new Area(dir === 'H' ? value : this.startX, this.endX,
					dir === 'V' ? value : this.startY, this.endY);		
	}
	endAt(dir, value) {
		return new Area(this.startX, dir === 'H' ? value : this.endX,
					this.startY, dir === 'V' ? value : this.endY);	
	}
	startEndAt(dir, start, end) {
		if(dir === 'H')
			return new Area(start, end, this.startY, this.endY);	
		else
			return new Area(this.startX, this.endX, start, end);
	}	
	
}


//	e.stopPropagation();
//	e.preventDefault();

class TilingWM {
	constructor(id, xsize, ysize) {
		this.name = id;
		this.windows = {};
		this.xsize = xsize;
		this.ysize = ysize;
	}
	initialize() {
		this.element = document.getElementById(this.name);
		this.element.style.display = 'grid';
		this.element.style.gridTemplateColumns = `repeat(${this.xsize}, calc(100% / ${this.xsize}))`;
		this.element.style.gridTemplateRows = `repeat(${this.ysize}, calc(100% / ${this.ysize}))`;
	}
	addChild(win) {
		this.windows[win.name] = win;
	}
	
	removeChild(win) {
		delete this.windows[win.name];
	}
	
	maximize(arg) {
		this._with_win(arg, (name, win) => {
			this._maximize_find(name, this._layout,(_name, layout) => {
				console.log("_iconify_except", name, layout);
				this._visit_windows(layout, (win,item) => {
					if(win.name === name) {
						console.log("maximizing", win);
						win.maximized(true);
						return true;
					} else {
						console.log("iconfying", win);
						win.iconified(true);
						return false;
					}
				});		
			});
			this.recomputeLayout();
		});		
	}
	
	unmaximize(arg) {
		this._with_win(arg, (name, win) => {
			this._maximize_find(name, this._layout, (_name, layout) => {
				console.log("_deiconify_all", name, layout);
				this._visit_windows(layout, (win,item) => {
					if(win.name === name) {
						console.log("unmaximizing", win);
						win.maximized(false);
					} else {
						console.log("deiconfying", win);
						win.iconified(false);
					}
					return true;						
				});		
			});
			this.recomputeLayout();
		});			
	}

	_with_win(arg, fun) {
		var name, win;
		if(typeof(arg) === 'string') {
			name = arg;
			win = this.windows[arg]
		} else {
			name = arg.name;
			win = arg;
		}
		
		if(!(win && this.windows.hasOwnProperty(name))) {
			console.warn("maximize: unknown window: ", name, win, arg);
			return;
		}	
		
		return fun(name, win);	
	}

	_maximize_find(name, layout, fun) {
		console.log("_maximize_find", name, layout);
		if(layout.items) {
			var maxi = false;
			layout.items.forEach(item => {
				maxi = maxi || this._maximize_find(name, item, fun);
			});
			if(maxi && layout.max_container) {
				fun(name, layout);
				return false;
			}
			return maxi;
		} else if(layout.item === name) {
			const win = this.windows[layout.item];
			console.log("maximizing", name, layout, win);
			win.maximized(true);
			return true;
		} else {
			return false;
		}
	}
	
	/** traverse layout node, visit each window. for each container, sets _iconified if fun returns false for all children */
	_visit_windows(layout, fun) {
		var displayed = false;
		layout.items.forEach(item => {
			if(item.items) {
				displayed = this._visit_windows(item, fun) || displayed;
			} else if(item.item) {
				const win = this.windows[item.item];
				if(win) {
					displayed = fun(win, item) || displayed;
				} else {
					console.warn("can't find component for window ", item.item, item);				
				}
			}
		});
		if(!displayed) {
			layout._iconified = true;
		}
		return displayed;
	}
	recomputeLayout() {
		if(this._layout) {
			this.layout(this._layout);
		}
	}
	
	layout(spec) {
		spec.id = '.';
		this._layout = layout(spec, new Area(1, this.xsize+1, 1, this.ysize+1),
			(it, area) => {
			const elt = document.getElementById(it.item);
			const win = this.windows[it.item];
			if(elt) {
				console.log(`#${it.item}`, area);
				elt.style.gridColumnStart = area.startX;
				elt.style.gridColumnEnd = area.endX;
				elt.style.gridRowStart = area.startY;
				elt.style.gridRowEnd = area.endY;
			}
		});
		return this._layout;
	}
	
	setupResizing() {
		this.element.querySelectorAll('.ew-resizer, .ns-resizer').forEach(e => e.remove());
		this._setupResizing(this._layout);
	}
	
	itemById(id) {
		return this._itemById(this._layout, id);
	}
	
	_itemById(item, id) {
		//console.log("_itemById", item, id);
		if(item.id === id) {
			return item;
		} else {
			const path = id.split('.');
			path.forEach(idx => {
				if(idx !== '') {
					//console.log("item:", item, "idx: ", idx);
					item = item.items[parseInt(idx)];
				}
			});
			return item;
		}
	}
	
	_setupResizing(spec) {
		const dir = spec.dir;
		const items = spec.items;
		items.forEach((current,i) => {
			console.log("looking at", i, i+1, current, items[i+1]);
			if(items[i+1]) {
				const next = items[i+1];
				const elt = document.createElement('div');
				const curId = current.id;
				const nxtId = next.id;
				var k;
				if(dir === 'H') {
					console.log("H", current.area, next.area);
					elt.style.gridColumnStart = current.area.endX-1;
					elt.style.gridColumnEnd = current.area.endX+1;
					elt.style.gridRowStart = current.area.startY;
					elt.style.gridRowEnd = current.area.endY;
					elt.className = 'ew-resizer';
					k = 0;
					this.element.appendChild(elt);
				} else if(dir === 'V') {
					console.log("V", current.area, next.area);
					elt.style.gridColumnStart = current.area.startX;
					elt.style.gridColumnEnd = current.area.endX;
					elt.style.gridRowStart = current.area.endY;
					elt.style.gridRowEnd = current.area.endY+1;
					elt.className = 'ns-resizer';
					k = 1;
					this.element.appendChild(elt);			
				}
					elt.addEventListener("mousedown", e => {
						const pos = this._resizeMoveCalc(dir, e);
						console.log("resize starting at: ", pos, e);
						var currentPos = pos;
						const controller = new AbortController();
						const signal = controller.signal;
						this.element.addEventListener("mousemove", e2 => {
							const newPos = this._resizeMoveCalc(dir,e2);
							if(newPos != currentPos) {
								console.log("mousemove!", `dir=${dir}, pos=${pos}, currentPos=${currentPos}, newPos=${newPos}`);		
								const diff = newPos-currentPos;
								const curItem = this._itemById(this._layout, curId);
								const nxtItem = this._itemById(this._layout, nxtId);
								if(curItem.size + diff >= curItem.minSize[k] && nxtItem.size - diff >= nxtItem.minSize[k]) {
									console.log("resize!", curItem.minSize, nxtItem.minSize, `dir=${dir}, pos=${pos}, currentPos=${currentPos}, newPos=${newPos}`);		
										currentPos = newPos;
									if(dir === 'H') {
										elt.style.gridColumnStart = currentPos-1;
										elt.style.gridColumnEnd = currentPos+1;
									} else {
										elt.style.gridRowStart = currentPos;
										elt.style.gridRowEnd = currentPos+1;
									}
									console.log("old layout:", JSON.stringify(this._layout));

									curItem.size += diff;
									nxtItem.size -= diff;
									this.layout(this._layout);
									console.log("new layout:", JSON.stringify(this._layout));
								}
							}
							e.stopPropagation();
							e.preventDefault();	
						}, {
							capture: true, signal: signal
						});
						document.body.addEventListener("mouseup", e2 => {
							console.log("mouseup!");
							controller.abort();
						}, { capture: true, once: true, signal: signal });
						document.body.addEventListener("mouseleave", e2 => {
							console.log("mouseleave!");
							controller.abort();
						}, { capture: false, once: true, signal: signal });
						e.stopPropagation();
						e.preventDefault();			
					});
					
			}
			if(current.dir) {
				this._setupResizing(current);
			}
		});
	}
	
	_resizeMoveCalc(dir, e) {
		if(dir === 'H') {
			const c = 1 + Math.round(this.xsize * (e.clientX-this.element.offsetLeft) / (this.element.offsetWidth))
			//console.log("resizeMoveCalc", dir, e.clientX, this.element.clientWidth, c);
			//const c = Math.min(maxColumn, Math.max(minColumn, ));
			return c;
		} else {
			const r = 1 + Math.round(this.ysize * (e.clientY-this.element.offsetTop) / (this.element.offsetHeight))
			//console.log("resizeMoveCalc", dir, e.clientY, this.element.clientHeight, r);	
			return r;	
		}
	}
		
//		if(c !== column) {
//			column = c;
//			console.log("column: ", column)
//			leftElt.style.gridColumnEnd = column;
//			rightElts.forEach(re => {re.style.gridColumnStart = column;});
//		}
//	};
	static _l(elts) {
		const l = [];
		var size = 1;
		elts.forEach(elt => {
			if(typeof(elt) === 'number') {
				size = elt;
			} else {
				l.push({size: size, item: elt});
			}
		})
		return l;
	}
	static H(...elts) {
		return {dir: 'H', items: TilingWM._l(elts)};
	}
	static V(...elts) {
		return {dir: 'V', items: TilingWM._l(elts)};
	}
}

class TilingWindow extends Component {
	constructor(name, element, tdstate) {
		if(!name && !element) {
			throw new Error("name and element can't both be undefined");
		}
		if(!element) {
			element = TilingWindow.createElement(name);
		} else if(!element.querySelector('#' + name + ' header')) {
			addHeader(name, element);
		}
		super(name, element, tdstate);	
	}
	
	static createElement(id) {
		const elt = document.createElement('section');
		elt.id = id;
		elt.className = "box focusable";
		addHeader(id, elt);

		const foot = document.createElement('footer');
		foot.id = id + '-foot';
		const insertHere = document.createElement('div');
		insertHere.style.display = 'none';
		insertHere.dataset.insertHere = 'true';
		elt.appendChild(insertHere);
		elt.appendChild(foot);
		return elt;
	}
	

	maximize(enable) {
		
	}
}


function prealloc(items, avail) {
	if(items.length > avail) {
		console.warn("Not enough space for layout items: ", avail, items);
	}
	var total = Math.max(1,items.reduce((x,y) => x + y.size, 0));
	
	items.map((it,i) => {
		const elt = it.item ? document.getElementById(it.item) : undefined;
		const iconified = elt ? elt.classList.contains('iconified') : it._iconified;
		const alloc = Math.round(it.size*avail/total);
		total -= it.size;
		avail -= alloc;	
		it.size = alloc;
		it._iconified = iconified;
	});

}
function layout(item, area, fun) {
	console.log(item);
	const dir = item.dir;
	const k0 = dir === 'H' ? 0 : 1;
	const k1 = dir === 'H' ? 1 : 0;
	const items = item.items;
	var avail = area.end(dir) - area.start(dir);
	var nextStart = area.start(dir);
	item.minSize = [0,0];
	prealloc(items, avail);

	var total = Math.max(1,items.reduce((x,y) => x + (y._iconified ? 0 : y.size), 0));
	items.forEach((it,i) => {
			const start = nextStart;
			const alloc = Math.round(it.size*avail/total);
			const allocArea = area.startEndAt(dir, start, start + alloc);
			it.id = `${item.id}${i}.`;
			if(it._iconified) {
				it.alloc = 0;
				it.minSize = [0,0];
				if(it.items)
					layout(it, allocArea, fun);
				else if(fun)
					fun(it, allocArea);
				delete it._iconified;
				
				return it;
			}
			nextStart += alloc;
			total -= it.size;
			avail -= alloc;
			console.log(alloc, dir, start, nextStart);
			if(it.items) { // layout recursively
				layout(it, allocArea, fun);
				it.alloc = alloc;
				it.area = allocArea;
				item.minSize[k0] += it.minSize[k0];
				item.minSize[k1] = Math.max(item.minSize[k1], it.minSize[k1]);
			} else {
				if(fun)
					fun(it, allocArea);
				item.minSize[k0] += 1;
				item.minSize[k1] = Math.max(item.minSize[k1], 2);
				it.alloc = alloc;
				it.minSize = [1,2];
				it.area = allocArea;
			}
		});
	return item;
}

export { TilingWM, TilingWindow };
