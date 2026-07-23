<%@ tag description="서버에서 정화한 Markdown HTML만 출력" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="html" required="true" type="kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml" %>
${html.value}
