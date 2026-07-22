package kr.ac.tukorea.bandi.domain.notice.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarkdownRenderer {

    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "h1", "h2", "h3", "h4", "h5", "h6",
                    "strong", "em", "del", "blockquote", "ul", "ol", "li",
                    "pre", "code", "hr", "table", "thead", "tbody", "tr", "th", "td")
            .allowElements((elementName, attributes) -> {
                attributes.add("rel");
                attributes.add("noopener noreferrer");
                attributes.add("target");
                attributes.add("_blank");
                return "a";
            }, "a")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("http", "https", "mailto")
            .toFactory();

    private final Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .escapeHtml(true)
            .build();

    public SafeMarkdownHtml render(String markdown) {
        Node document = parser.parse(removeRawHtml(markdown));
        return new SafeMarkdownHtml(POLICY.sanitize(renderer.render(document)));
    }

    private String removeRawHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        return markdown.replaceAll("(?is)<[!/a-z][^>]*>", "");
    }
}
