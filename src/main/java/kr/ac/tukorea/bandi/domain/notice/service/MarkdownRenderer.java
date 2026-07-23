package kr.ac.tukorea.bandi.domain.notice.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownRenderer {

    private static final Pattern ATTACHMENT_IMAGE_PATTERN =
            Pattern.compile("attachment://([1-9][0-9]*)");
    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "h1", "h2", "h3", "h4", "h5", "h6",
                    "strong", "em", "del", "blockquote", "ul", "ol", "li",
                    "pre", "code", "hr", "table", "thead", "tbody", "tr", "th", "td",
                    "img")
            .allowElements((elementName, attributes) -> {
                attributes.add("rel");
                attributes.add("noopener noreferrer");
                attributes.add("target");
                attributes.add("_blank");
                return "a";
            }, "a")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "alt", "title").onElements("img")
            .allowUrlProtocols("http", "https", "mailto")
            .toFactory();

    private final Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .escapeHtml(true)
            .build();

    public SafeMarkdownHtml render(String markdown) {
        return render(markdown, Map.of());
    }

    public SafeMarkdownHtml render(String markdown, Map<Long, String> imageUrls) {
        return render(markdown, imageUrls, true);
    }

    public SafeMarkdownHtml renderInternalImagesOnly(String markdown,
                                                     Map<Long, String> imageUrls) {
        return render(markdown, imageUrls, false);
    }

    private SafeMarkdownHtml render(String markdown, Map<Long, String> imageUrls,
                                    boolean allowExternalImages) {
        Node document = parser.parse(removeRawHtml(markdown));
        replaceUnsafeImages(document, imageUrls, allowExternalImages);
        return new SafeMarkdownHtml(POLICY.sanitize(renderer.render(document)));
    }

    public Set<String> extractAttachmentImageReferences(String markdown) {
        Node document = parser.parse(removeRawHtml(markdown));
        Set<String> references = new LinkedHashSet<>();
        visitImages(document, image -> {
            if (image.getDestination().startsWith("attachment://")) {
                references.add(image.getDestination());
            }
        });
        return Set.copyOf(references);
    }

    private void replaceUnsafeImages(Node document, Map<Long, String> imageUrls,
                                     boolean allowExternalImages) {
        visitImages(document, image -> {
            Long storedFileId = extractStoredFileId(image.getDestination());
            if (storedFileId == null) {
                if (!allowExternalImages || !isSafeExternalImageUrl(image.getDestination())) {
                    image.unlink();
                }
                return;
            }
            String imageUrl = imageUrls.get(storedFileId);
            if (!isSafeInlineImageUrl(imageUrl)) {
                image.unlink();
                return;
            }
            image.setDestination(imageUrl);
        });
    }

    private Long extractStoredFileId(String destination) {
        Matcher matcher = ATTACHMENT_IMAGE_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }
        return Long.valueOf(matcher.group(1));
    }

    private boolean isSafeInlineImageUrl(String imageUrl) {
        return imageUrl != null && (imageUrl.startsWith("/api/internal-notices/")
                || imageUrl.startsWith("/api/internal-notice-management/")
                || imageUrl.startsWith("/api/resources/"));
    }

    private boolean isSafeExternalImageUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && uri.getUserInfo() == null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void visitImages(Node node, java.util.function.Consumer<Image> visitor) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            if (child instanceof Image image) {
                visitor.accept(image);
            }
            visitImages(child, visitor);
            child = next;
        }
    }

    private String removeRawHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        return markdown.replaceAll("(?is)<[!/a-z][^>]*>", "");
    }
}
