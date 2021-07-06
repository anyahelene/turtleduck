package turtleduck.tea;

import static turtleduck.tea.HTMLUtil.*;

import org.slf4j.Logger;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;
import turtleduck.util.Logging;

public class DocDisplay {
	public static final Logger logger = Logging.getLogger(DocDisplay.class);
	private Component component;
	private Component parent;
	private HTMLElement mainElement;
	private HTMLElement textElement;
	private String name;
	private String title;
	private static int docNum = 0;

	public DocDisplay(Component parent) {
		this.parent = parent;
	}

	protected void setup(String name, String title, boolean closeable) {
		this.name = name;
		this.title = title;
		this.textElement = element("section", clazz("text"));
		this.mainElement = element("main", clazz("doc-display"), textElement);
		this.component = JSUtil.createComponent(this.name, mainElement);
		component.setParent(parent);
		if (closeable)
			component.onclose(this::onclose);
		component.setTitle(this.title);
		component.register();
	}

	protected void reopen() {
		if(textElement != null && mainElement != null && component == null) {
			this.component = JSUtil.createComponent(this.name, mainElement);
			component.setParent(parent);
			component.onclose(this::onclose);
			component.setTitle(this.title);
			component.register();	
		}
	}
	public void initFromUrl(String url, String title, boolean closeable) {
		if (title == null)
			title = url.replaceFirst("^.*/", "");
		setup("doc_" + docNum++, title, closeable);

		textElement.withText("loading ...");
		XMLHttpRequest req = XMLHttpRequest.create();
		req.onComplete(() -> {
			if (req.getReadyState() == 4 && req.getStatus() == 200) {
				JSUtil.renderSafeMarkdown(textElement, req.getResponseText());
			} else {
				textElement
						.withText(String.format("Error loading %s: %d %s", url, req.getStatus(), req.getStatusText()));
				logger.warn("Unexpected request result: {}", req);
			}
			component.select();
		});
		req.open("GET", url, true);
		req.setRequestHeader("Accept", "text/markdown, text/plain, text/*;q=0.9");
		req.send();
	}

	protected JSBoolean onclose(Component comp, Event ev) {
		logger.info("closing document: name={}, title={}", name, title); 
		component = null;
		return JSBoolean.valueOf(true);
	}
}
