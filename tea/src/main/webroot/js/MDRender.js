import { Remarkable, utils } from 'remarkable';
import hljs from 'highlight.js/lib/core';
import javascript from 'highlight.js/lib/languages/javascript';
import json from 'highlight.js/lib/languages/json';
import python from 'highlight.js/lib/languages/python';
import java from 'highlight.js/lib/languages/java';
import jquery from 'jquery';
import slugify from 'slugify';
hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('java', java);
hljs.registerLanguage('json', json);
hljs.registerLanguage('python', python);


/** Convert a flat document to one with a tree-like section structure; i.e.,
    each heading and associated text will be placed in a DIV, with subsections
    nested within. Nodes are removed from `rootElt` and added to `destElt` */
function doc2tree(rootElt, destElt) {
	destElt.innerHTML = '';
	var current = destElt;
	var count = 0;
	var title = '';
	const stack = [destElt];
	const path = [];

	function newSection(elt, level) {
		const newSection = document.createElement('div');
		if(elt.id) {
			newSection.id = elt.id;
			elt.id = undefined;
		}
		var parent = null;
		for(var i = stack.length-1; parent == null && i >= 0; i--) {
			parent = stack[i];
		}
		parent.appendChild(newSection);
		stack.push(newSection);
		path.push(count+1);
		newSection.dataset.level = path.length;
		newSection.dataset.path =  path.join('.');
		return newSection;
	}

	Array.from(rootElt.childNodes).forEach(elt => {
		//console.log('doc2tree:', 'current', current, 'stack', stack, 'looking at', elt.innerHTML || elt.textContent);
		var m;
		if((m = elt.nodeName.match(/^H([123456])$/)) != null) {
			const level = parseInt(m[1]);
			if(!title && level === 1) {
				title = elt.innerText;
				elt.classList.add('title');
			}
			//console.log('looking at ', level, stack.length, stack, elt.innerHTML);
			while(level < stack.length) { // unwind to level-1
				current = stack.pop();
				count = path.pop();
				//console.log('doc2tree <=  ', path.join('.'), 'current', current, 'stack', JSON.stringify(stack), 'popping at', elt.innerHTML || elt.textContent)
			}
			while(level > stack.length) {
				stack.push(null);
				path.push(0);
				count = 0;
			}
			current = newSection(elt);
			//console.log('doc2tree ==  ', path.join('.'), 'current', current, 'stack', stack, 'pushing at', elt.innerHTML || elt.textContent)
		} 
		current.appendChild(elt);
	});
}
function makeScrollToListener(elt, scroller) {
	if(!scroller) {
		console.warn('no doc-display parent found', elt);
		return function(e) {
			e.stopPropagation();
			e.preventDefault();				
		};
	} else {
		return function(e) {
			e.stopPropagation();
			e.preventDefault();
			const target = e.currentTarget;
			console.log("scrollTo", target, e);
			if(target.href) {
				const section = elt.querySelector(target.getAttribute('href'));
				const fontSize = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
				if(section) {
					scroller.scrollTop = section.offsetTop - fontSize;
				}
			}
		}
	}
}
function makeToc(doc, elt, scroller) {
	if(!scroller)
		return [null,null,null];
	const headers = [];
	const sections = {};
	var i = 1;
	const toc = document.createElement('ol');
	toc.className = "table-of-contents";
	const listener = makeScrollToListener(elt, scroller);
	doc.querySelectorAll('h1, h2').forEach(head => {
		headers.push(head);
		const a = head.querySelector('slug');
		var title = head.textContent;
		//console.log('title', title);
		if(a) {
			title = a.textContent;
			a.remove();
			//console.log("title'", title);
		}
		const slug = `_${i}_` + slugify(title);
		//console.log('slug', slug);
		head.id = slug;
		const item = document.createElement('li');
		const link = document.createElement('a');
		sections[head.id] = {head: head, link: link};
		link.id = '_link_' + slug;
		link.href = '#' + slug;
		link.textContent = title;
		link.addEventListener('click', listener);
		link.type = 'button';
		item.appendChild(link);
		toc.appendChild(item);
	});


	if(toc.childElementCount > 0) {
		elt.appendChild(toc);
		scroller.classList.add('has-contents-bar');
	}
	return [toc, headers, sections];
}

const thresholds = [0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1,0];
class MDRender {
	constructor(opts = {}) {
		this.md = new Remarkable(jquery.extend({
			typographer: true,
	  		quotes: '“”‘’',
			highlight: this._remarkableHighlight()
		}, opts));
		
		this.Remarkable = Remarkable;
		this.utils = utils;
		this.hljs = hljs;
		this.javascript = javascript;
		this.json = json;
		this.python = python;
		this.java = java;
		
		const rules = this.md.renderer.rules;
		rules.link_open = this._render_link_open(rules.link_open);
		rules.image = this._render_image(rules.image);
		rules.fence = this._render_fence(rules.fence);
		rules.paragraph_open = this._render_paragraph_open(rules.paragraph_open);
		rules.paragraph_close = this._render_paragraph_close(rules.paragraph_close);
	}
	

	render_unsafe(elt, code) {
		this._snippets = [];
		const doc = document.createElement('div');
		doc.innerHTML = this.md.render(code);
		doc.querySelectorAll('a[data-action]').forEach(link => {
			const action = link.dataset.action;
			//var m = link.href.match(/^([a-z]*):\/\/(.*)/);
			//if(!m)
			//	m = ['unknown',''];
			link.type = 'button';
			link.addEventListener('click', e => {
				e.preventDefault();
				e.stopPropagation();
				turtleduck.handleKey(link.href, link, e);
			});

		});

		var scroller = elt;
		while(scroller && !scroller.classList.contains('doc-display')) {
			scroller = scroller.parentElement;
		}
		
		const [toc, headers, sections] = makeToc(doc, elt, scroller);
			
		doc2tree(doc, elt);
		
		if(toc) {
			console.log('sections', sections);
			const obs = new IntersectionObserver(entries => {
				for(const entry of entries) {
					const section = elt.querySelector('#'+entry.target.id);
					const link = elt.querySelector('#_link_'+entry.target.id);
					//console.log(section, link);
					if(section) {
						if(entry.intersectionRatio > 0) {
							link.classList.add('active');
						} else {
							link.classList.remove('active');
						}
					}
				}
			},  {root: scroller, rootMargin: "-25% 0px", threshold: thresholds});
			elt.querySelectorAll('div[data-level="2"], div[data-level="1"]').forEach(head => obs.observe(head));
			elt.appendChild(toc);
		}
		elt.querySelectorAll('button[data-snippet]').forEach(btn => {
			const snip = this._snippets[parseInt(btn.dataset.snippet)];
			var code = snip.code;
			if(snip.language === 'python') {
				if(snip.code.startsWith('>>> ')) {
					code = '';
					var mode = '';
					snip.code.split('\n').forEach(line => {
						if(line.startsWith('>>> ') || (line.startsWith('... ') && mode === '.')) {
							code = code + line.substring(4) + '\n';
							mode = '.';
						} else  {
							mode = '';
						}
					});
				}
				btn.innerHTML = '<span class="icon">📋</span><span>→ PyShell</span>';	
				btn.title = 'Paste Code in Python Shell';	
				btn.addEventListener('click', e => {
					e.preventDefault();
					e.stopPropagation();
					turtleduck.pyshell.paste(code);
					turtleduck.pyshell.focus();
				});
			} else {
				btn.innerHTML = '<span class="icon">📋</span><span>Copy</span>';
				btn.title = 'Copy Code to Clipboard';
				btn.addEventListener('click', e => {
					e.preventDefault();
					e.stopPropagation();
					navigator.clipboard.writeText(code)
							.then(() => {
								turtleduck.userlog("Copied!");
								},
								err => {
									turtleduck.userlog("Copy failed :(");
									console.error("Copy failed:", err);
								});
				});				
			}
		});
	}
	
	_remarkableHighlight() {
		const md = this;
		return function(str, lang) {
			if(lang && hljs.getLanguage(lang)) {
				try {
					md._snippets.push({language: lang, code: str});
					const r = hljs.highlight(str, {language:lang}).value;
					//console.log("hljs.highlight:", r);
					return r;
				} catch(err) {
					console.error(err);
				}
			}
			try { // fallback to auto 
		   		const r = hljs.highlightAuto(str).value;
		 		//console.log("hljs.highlightAuto:", r);
				return r;
			} catch (err) {
				console.error(err);
			}
		 	return ''; // fallback to default
		};
	}

	_render_fence(old) {
		const md = this;
		return function(tokens, idx, options, env, instance) {
			const l = md._snippets.length;
			var r = old(tokens, idx, options, env, instance);
			//console.log('fence:', tokens[idx].params, r);
			if(l != md._snippets.length) {
				const btn = `<nav class="toolbar"><button class="insert" type="button" data-action="insert" data-snippet="${l}"></button></nav>`;
				const m = r.match(/^([\s\S]*?)(<\/pre>|)(\s*)$/);
				if(m != null) {
					r = m[1] + btn + m[2] + m[3];
				} else {
					r = r + btn; 					
				}
			}
			return r;
		}

	}
	_render_link_open(old) {
		return function(tokens, idx, options /* env */) {
			const link = tokens[idx];
			const title = link.title ? (' title="' + utils.escapeHtml(utils.replaceEntities(link.title)) + '"') : '';
			var target = options.linkTarget ? (' target="' + options.linkTarget + '"') : '';
			var href = link.href;
			//console.log("link_open", link);
			const m = href.match(/^(insert|focus|open|run|save|snap|qrscan):/);
			if(m != null) {
				target = ' data-action="'+ m[1]+'"'; 
			} else if(href.match(/^\w[\w\d+.-]*:/) != null) {
				target = ' target="_blank"';
			} else if(options.hrefPrefix) {
				href = options.hrefPrefix + href;
			}
			return '<a href="' + utils.escapeHtml(href) + '"' + title + target + '>';
		};
	}
	
	_render_image(old) {
		return function(tokens, idx, options /*, env */) {
			var src = tokens[idx].src;
			if(src.match(/^\w[\w\d+.-]*:/) == null) {
				src = options.hrefPrefix + src;
			}
			//console.log("image", tokens[idx]);
			src = ' src="' + utils.escapeHtml(src) + '"';
			var alt = ' alt="' + (tokens[idx].alt ? utils.escapeHtml(utils.replaceEntities(utils.unescapeMd(tokens[idx].alt))) : '') + '"';
			var style = tokens[idx].style ? (' class="' + tokens[idx].style + '"') : '';
			var suffix = options.xhtmlOut ? ' /' : '';
			if(tokens[idx].figure) {
					var title = '';
					if(tokens[idx].title === '<') {
						return '<aside class="left small"><figure>' + '<img' + src + alt + suffix + '></figure></aside>';
					} else if(tokens[idx].title) {
						title = '<figcaption>' + (options.html ? tokens[idx].title : utils.escapeHtml(utils.replaceEntities(tokens[idx].title))) + '</figcaption>';						
					}
					return '<aside' + style + '><figure>' + '<img' + src + alt + suffix + '>' + title + '</figure></aside>';
			} else {
				var title = tokens[idx].title ? (' title="' + utils.escapeHtml(utils.replaceEntities(tokens[idx].title)) + '"') : '';
				return '<img' + src + alt + title + style + suffix + '>';
			}
		};
	}
	
	_render_paragraph_open(old) {
		return function(tokens, idx, options, env) {
			try {
				if(tokens[idx+2].type === 'paragraph_close') {
					const next_token = tokens[idx+1];
					if(next_token.children.length === 1 && next_token.children[0].type === 'image') {
						tokens[idx+2].omit = true;
						next_token.children[0].figure = true;
						next_token.children[0].style = 'right';
						return '';
					}
				}
			} catch(e) {
				console.error(e);
			}
			return old(tokens, idx, options, env);
		};
	}
	
	_render_paragraph_close(old) {
		return function(tokens, idx, options, env) {
			if(tokens[idx].omit)
				return '';
			else
				return old(tokens, idx, options, env);
		};
	}
}

export { MDRender };

