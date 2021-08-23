import { Remarkable, utils } from 'remarkable';
import hljs from 'highlight.js/lib/core';
import javascript from 'highlight.js/lib/languages/javascript';
import json from 'highlight.js/lib/languages/json';
import python from 'highlight.js/lib/languages/python';
import java from 'highlight.js/lib/languages/java';
import jquery from 'jquery';
hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('java', java);
hljs.registerLanguage('json', json);
hljs.registerLanguage('python', python);

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
		elt.innerHTML = this.md.render(code);
		elt.querySelectorAll('a[data-action]').forEach(link => {
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
		})
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
			console.log('fence:', tokens[idx].params, r);
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

