

class Component {
	constructor(name, element, tdstate) {
		console.log("new Component(%o,%o,%o)", name, element, tdstate);
		this.name = name;
		this.element = element;
		element.tabIndex = -1;
		element.addEventListener("focusin", e => {
			element.classList.add("focused");
			const last = tdstate.currentFocus;
			console.log("focusin", name, "last=", last, "event=", e);
			if(last !== this) {
				tdstate.currentFocus = this;
				tdstate.lastFocus = last;
				if(last)
					last.element.classList.remove("focused");
			}
			e.stopPropagation();
			return false;
		},false);
		element.addEventListener("blur", e => {
			console.log("focusout", name, e);
			//element.classList.remove("focused");
			//tdstate.lastFocus = this;
		},false);
	}
	
	element() {
		this.element;
	}
	
	focus() {
		this.element.focus();
	}
}


export { Component };
