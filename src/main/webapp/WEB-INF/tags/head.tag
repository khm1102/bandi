<%@ tag description="공통 head — 메타/폰트/토큰/Tailwind 매핑 (공유 자원, 22.5)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
<title><c:out value="${title}"/> - bandi</title>
<%-- TODO 브랜드 로고 확정 시 실제 파비콘 에셋으로 교체 --%>
<link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🎭</text></svg>">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;800;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<c:url value='/css/tokens.css'/>">
<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4.3.2/dist/index.global.js"
        integrity="sha384-yzJGD2ZXURu6RaDFjaXOC6IrnZq1aEeldwvXw7Pec43Q2BnmHkys/fGPzvwEtojO"
        crossorigin="anonymous"></script>
<%-- shadcn 토큰 → Tailwind 유틸리티 매핑 (공유 자원 — 값은 tokens.css에서만 수정) --%>
<style type="text/tailwindcss">
    @theme inline {
        --font-sans: "Noto Sans KR", "Pretendard", "Apple SD Gothic Neo", sans-serif;
        --color-background: var(--background);
        --color-foreground: var(--foreground);
        --color-card: var(--card);
        --color-card-foreground: var(--card-foreground);
        --color-popover: var(--popover);
        --color-popover-foreground: var(--popover-foreground);
        --color-primary: var(--primary);
        --color-primary-foreground: var(--primary-foreground);
        --color-primary-strong: var(--primary-strong);
        --color-secondary: var(--secondary);
        --color-secondary-foreground: var(--secondary-foreground);
        --color-muted: var(--muted);
        --color-muted-foreground: var(--muted-foreground);
        --color-accent: var(--accent);
        --color-accent-foreground: var(--accent-foreground);
        --color-destructive: var(--destructive);
        --color-destructive-foreground: var(--destructive-foreground);
        --color-destructive-soft: var(--destructive-soft);
        --color-success: var(--success);
        --color-success-soft: var(--success-soft);
        --color-warning: var(--warning);
        --color-warning-soft: var(--warning-soft);
        --color-info: var(--info);
        --color-info-soft: var(--info-soft);
        --color-border: var(--border);
        --color-input: var(--input);
        --color-ring: var(--ring);
        --color-sidebar: var(--sidebar);
        --color-sidebar-foreground: var(--sidebar-foreground);
        --color-sidebar-accent: var(--sidebar-accent);
        --color-sidebar-accent-foreground: var(--sidebar-accent-foreground);
        --color-sidebar-muted: var(--sidebar-muted);
        --color-sidebar-border: var(--sidebar-border);
        --radius-sm: calc(var(--radius) - 4px);
        --radius-md: calc(var(--radius) - 2px);
        --radius-lg: var(--radius);
        --radius-xl: calc(var(--radius) + 4px);
    }
    @layer base {
        * {
            @apply border-border outline-ring/50;
        }
        body {
            @apply bg-background text-foreground font-sans antialiased;
        }
    }
</style>
