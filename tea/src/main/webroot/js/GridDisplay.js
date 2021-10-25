import { Component } from './Component';
import { StyleModule } from 'style-mod';

class GridDisplayServer {
	constructor() {
		this.grids = {};
	}
	
	create(msg) {
		const content = msg.content;
		const id = content.id;
		const grid = new GridDisplay(content);
		this.grids[id] = grid;
		return Promise.resolve({header:{
			to: msg.header.from,
			msg_type: msg.header.msg_type+'_reply', 
			ref_id: msg.header.msg_id,
			msg_id: 'r' + msg.header.msg_id},
			content: {status:'ok', created:id}});
	}
	
	update(msg) {
		const id = msg.content.id;
		if(id && this.grids[id]) {
			return this.grids[id].update(msg);
		} else {
			throw new Error("not found: " + id);
		}
	}
	style(msg) {
		const id = msg.content.id;
		if(id && this.grids[id]) {
			return this.grids[id].style(msg);
		} else {
			throw new Error("not found: " + id);
		}
	}
		
	dispose(msg) {
		const id = msg.content.id;
		if(id && this.grids[id]) {
			this.grids[id].dispose();
			return Promise.resolve({header:{
				to: msg.header.from,
				msg_type: msg.header.msg_type+'_reply', 
				ref_id: msg.header.msg_id,
				msg_id: 'r' + msg.header.msg_id},
				content: {status:'ok', disposed:id}});
		} else {
			throw new Error("not found: " + id);
		}		
	}
	
}

class GridDisplay {
	constructor(args) {
		this.width = parseInt(args.width);
		this.height = parseInt(args.height);
		this.id = args.id;
		this.title = args.title;
		this.element = document.createElement('main');
		this.element.className = 'grid';
		this.elements = [];
		this.styleElt = document.createElement("style")
		this.styleRules = {};
		this.styleRules[`#grid${this.id} div`] = {
			border: 'thin dotted black'
		}
		document.head.insertBefore(this.styleElt, document.head.firstChild);


		this.component = new Component(this.id, this.element, turtleduck, turtleduck.wm.windows.screen);
		this.component.setTitle(args.title);
		this.component.register();
		this.component.select();
		const parent = document.createElement('div');
		parent.style.gridTemplateColumns = `repeat(${this.width}, 1fr)`;
		parent.style.gridTemplateRows = `repeat(${this.height}, 1fr)`;
		parent.id = 'grid' + args.id;
		this.element.appendChild(parent);
		const initial = args.initial || '';
		for(var y = 0; y < this.height; y++) {
			//const row = document.createElement('tr');
			//row.dataset.y = y;
			//this.element.appendChild(row);
			for(var x = 0; x < this.width; x++) {
				const col = document.createElement('div');
				var dir = '';
				if(y === 0)
					dir += 'grid-edge-N';
				else if(y === this.height-1)
					dir += 'grid-edge-S';
				if(x === 0)
					dir += ' grid-edge-W';
				else if(x === this.width-1)
					dir += ' grid-edge-E';
				
				col.dataset.y = y;
				col.dataset.x = x;
				col.dataset.dir = dir;
				col.style.gridColumn = x+1;
				col.style.gridRow = y+1;
				this._setCell(col, initial || '');
				parent.appendChild(col);
				this.elements.push(col);
			}
		}
	}
	
	_setCell(cell, val) {
		cell.className = cell.dataset.dir;
		val.split('').forEach(c => {
			const style = this._styleName(c);
			if(style.startsWith('grid-bg')) {
				cell.classList.add(style);
			} else {
				const elt = document.createElement('span');
				elt.classList.add(style);
				cell.appendChild(elt);
			}
		});
	}

	update(msg) {
		const content = msg.content;
		const updates = msg.content.updates;
		var error;
		if(updates) {
			if(!this.styled) {
				this._setStyle();
			}
			updates.forEach(u => {
				const x = u.x, y = u.y;
				if(x >= 0 && x < this.width && y >= 0 && y < this.height) {
					const i = y * this.width + x;
					const cell = this.elements[i];
					cell.replaceChildren();
					this._setCell(cell, u.text);
				} else {
					error = 'out of bounds: ' + JSON.stringify(u);
				}
			});
		}
		return Promise.resolve();
		/*{header:{
			to: msg.header.from,
			msg_type: msg.header.msg_type+'_reply', 
			ref_id: msg.header.msg_id,
			msg_id: 'r' + msg.header.msg_id},
			content: {status:'ok', updated:this.id}});*/
	}
	
	style(msg) {
		const content = msg.content;
		var selector = content.selector;
		if(selector === 'grid') {
			selector = `#grid${this.id}`;
		} else if(selector === 'cell') {
			selector = `#grid${this.id} div`;
		} else {
			selector = this._styleSelector(`#grid${this.id}`, selector);
		}
		var styleSet = content.styleset;
		if(!styleSet) {
			styleSet = {};
			styleSet[content.property] = content.value;
		}
		const rules = this.styleRules[selector] || {};
		console.log('styleSet', styleSet);
		for(var prop in styleSet) {
			rules[prop] = styleSet[prop];
		}
		this.styleRules[selector] = rules;
		this._setStyle();
		return Promise.resolve();
	}

	_styleSelector(id, s) {
		const spec = s.split(':');
		const name = this._styleName(spec[0]);
		const edgeSpec = spec.slice(1).map(arg => {
			var dirClass = `.grid-edge-${arg[0]}`;
			if(arg[1])
				dirClass += `.grid-edge-${arg[1]}`;
			return `${id} .${name}${dirClass}, ${id} ${dirClass} .${name}`;
			
		});
		if(edgeSpec.length > 0) {
			return edgeSpec.join(', ');
		} else {
			return `${id} .${name}`;
		}
	}
	_styleName(s) {
		if(s.match(/^[a-z]$/)) {
			return `grid-style-${s}`;		
		} else if(s.match(/^[A-Z]$/)) {
			return `grid-bg-${s.toLowerCase()}`;
		} else {
			return `grid-style-${s.charCodeAt(0)}`;
		}
	}
	

	
	_setStyle() {
		var ruleText = '';
		for(var key in this.styleRules) {
			const rules = this.styleRules[key];
			var rule = key + ' {\n';
			for(var prop in rules) {
				rule += '  ' + prop + ': ' + rules[prop] + ';\n';
			}
			rule += '}\n\n';
			ruleText += rule;
		}
		console.log(ruleText);
		this.styleElt.textContent = ruleText;
		this._styled = true;
	}
	
	dispose(msg) {
		if(this.element)
			this.element.remove();
		if(this.styleElt)
			this.styleElt.remove();
	}
}

export { GridDisplayServer, GridDisplay };
