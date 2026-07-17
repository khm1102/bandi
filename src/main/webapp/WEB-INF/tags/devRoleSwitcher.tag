<%@ tag description="개발 역할 전환 — 현재 화면에서 허용하는 역할만 노출" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="active" %>
<%@ attribute name="role" %>
<%@ attribute name="allowedRoles" %>
<c:if test="${not empty role and not empty active}">
    <nav class="ml-auto inline-flex rounded-lg border bg-secondary p-0.5" aria-label="개발 역할 미리보기">
        <c:if test="${empty allowedRoles or allowedRoles.contains('member')}">
            <a href="<c:url value='/${active}'/>?role=member" class="inline-flex min-h-9 items-center rounded-md px-3 text-xs font-bold transition-colors ${role == 'member' ? 'border bg-card text-foreground' : 'text-muted-foreground'}" aria-current="${role == 'member' ? 'page' : 'false'}">일반 부원</a>
        </c:if>
        <c:if test="${empty allowedRoles or allowedRoles.contains('leader')}">
            <a href="<c:url value='/${active}'/>?role=leader" class="inline-flex min-h-9 items-center rounded-md px-3 text-xs font-bold transition-colors ${role == 'leader' ? 'border bg-card text-foreground' : 'text-muted-foreground'}" aria-current="${role == 'leader' ? 'page' : 'false'}">팀장</a>
        </c:if>
        <c:if test="${empty allowedRoles or allowedRoles.contains('admin')}">
            <a href="<c:url value='/${active}'/>?role=admin" class="inline-flex min-h-9 items-center rounded-md px-3 text-xs font-bold transition-colors ${role == 'admin' ? 'border bg-card text-foreground' : 'text-muted-foreground'}" aria-current="${role == 'admin' ? 'page' : 'false'}">운영진</a>
        </c:if>
    </nav>
</c:if>
