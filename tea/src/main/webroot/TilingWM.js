
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
class TilingWM {
	constructor(element, xsize, ysize) {
		this.element = element;
		this.windows = {};
		this.xsize = xsize;
		this.ysize = ysize;
		element.style.display = 'grid';
		element.style.gridTemplateColumns = `repeat(${xsize}, calc(100% / ${xsize}))`;
		element.style.gridTemplateRows = `repeat(${ysize}, calc(100% / ${ysize}))`;
	}
	
	addChild(win) {
		this.windows[win.id] = win;
	}
	
	removeChild(win) {
		delete this.windows[win.id];
	}
	
	layout(spec) {
		this._layout = layout(spec, new Area(1, this.xsize+1, 1, this.ysize+1),
			(id, area) => {
			const elt = document.getElementById(id);
			if(elt) {
				console.log(`#${id}`, area);
				elt.style.gridColumnStart = area.startX;
				elt.style.gridColumnEnd = area.endX;
				elt.style.gridRowStart = area.startY;
				elt.style.gridRowEnd = area.endY;
			}
		});
		return this._layout;
	}
	
	setupResizing() {
		this.element.querySelectorAll('.ew-resizer, ns-resizer').forEach(e => e.remove());
		this._setupResizing(this._layout);
	}
	_setupResizing(spec) {
		const dir = spec.dir;
		const items = spec.items;
		items.forEach((current,i) => {
			console.log("looking at", i, i+1, current, items[i+1]);
			if(items[i+1]) {
				const next = items[i+1];
				if(dir === 'H') {
					console.log("H", current.area, next.area);
					const elt = document.createElement('div');
					elt.style.gridColumnStart = current.area.endX-1;
					elt.style.gridColumnEnd = current.area.endX+1;
					elt.style.gridRowStart = current.area.startY;
					elt.style.gridRowEnd = current.area.endY;
					elt.className = 'ew-resizer';
					this.element.appendChild(elt);			
				} else if(dir === 'V') {
					console.log("V", current.area, next.area);
					const elt = document.createElement('div');
					elt.style.gridColumnStart = current.area.startX;
					elt.style.gridColumnEnd = current.area.endX;
					elt.style.gridRowStart = current.area.endY-1;
					elt.style.gridRowEnd = current.area.endY+1;
					elt.className = 'ns-resizer';
					this.element.appendChild(elt);			
				}
			}
			if(current.item.dir) {
				this._setupResizing(current.item);
			}
		});
	}
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
}


function layout(item, area, fun) {
	const dir = item.dir;
	const items = item.items;
	var total = Math.max(1,items.reduce((x,y) => x + y.size, 0));
	var avail = area.end(dir) - area.start(dir);
	var nextStart = area.start(dir);
	const result = {
		dir: item.dir,
		items: items.map(it => {
			const start = nextStart;
			const alloc = Math.round(it.size*avail/total);
			nextStart += alloc;
			total -= it.size;
			avail -= alloc;
			console.log(alloc, dir, start, nextStart);
			const allocArea = area.startEndAt(dir, start, nextStart);
			if(it.item.dir) { // layout recursively
				return {size: alloc, item: layout(it.item, allocArea, fun), area: allocArea};
			} else {
				if(fun)
					fun(it.item, allocArea);
				return {size: alloc, item: it.item, area: allocArea};
			}
		})};
	return result;
}

export { TilingWM, TilingWindow };
