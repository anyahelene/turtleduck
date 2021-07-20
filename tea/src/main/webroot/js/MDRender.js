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
			highlight: this._remarkableHighlight
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
		rules.paragraph_open = this._render_paragraph_open(rules.paragraph_open);
		rules.paragraph_close = this._render_paragraph_close(rules.paragraph_close);
	}
	
	render_unsafe(elt, code) {
		elt.innerHTML = this.md.render(code);
	}
	
	_remarkableHighlight(str, lang) {
		if(lang && hljs.getLanguage(lang)) {
			try {
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
	}

	_render_link_open(old) {
		return function(tokens, idx, options /* env */) {
			const link = tokens[idx];
			const title = link.title ? (' title="' + utils.escapeHtml(utils.replaceEntities(link.title)) + '"') : '';
			var target = options.linkTarget ? (' target="' + options.linkTarget + '"') : '';
			var href = link.href;
			//console.log("link_open", link);
			if(href.match(/^\w[\w\d+.-]*:/) != null) {
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
			var title = tokens[idx].title ? (' title="' + utils.escapeHtml(utils.replaceEntities(tokens[idx].title)) + '"') : '';
			var alt = ' alt="' + (tokens[idx].alt ? utils.escapeHtml(utils.replaceEntities(utils.unescapeMd(tokens[idx].alt))) : '') + '"';
			var style = tokens[idx].style ? (' class="' + tokens[idx].style + '"') : '';
			var suffix = options.xhtmlOut ? ' /' : '';
			return '<img' + src + alt + title + style + suffix + '>';
		};
	}
	
	_render_paragraph_open(old) {
		return function(tokens, idx, options, env) {
			try {
				if(tokens[idx+2].type === 'paragraph_close') {
					const next_token = tokens[idx+1];
					if(next_token.children.length === 1 && next_token.children[0].type === 'image') {
						tokens[idx+2].omit = true;
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

