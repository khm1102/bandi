const JSON_CONTENT_TYPE = 'application/json';
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

export class ApiError extends Error {
    constructor(status, code, message, fieldErrors = []) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors;
    }
}

function lookupMetaContent(name) {
    return document.querySelector(`meta[name="${name}"]`)?.content || '';
}

function appendQuery(url, query) {
    if (!query) {
        return url;
    }
    const searchParams = new URLSearchParams();
    Object.entries(query).forEach(([key, rawValue]) => {
        if (rawValue === undefined || rawValue === null || rawValue === '') {
            return;
        }
        const values = Array.isArray(rawValue) ? rawValue : [rawValue];
        values.forEach((value) => searchParams.append(key, String(value)));
    });
    const queryString = searchParams.toString();
    if (!queryString) {
        return url;
    }
    return `${url}${url.includes('?') ? '&' : '?'}${queryString}`;
}

function buildHeaders(method, body, customHeaders) {
    const headers = new Headers(customHeaders);
    headers.set('Accept', JSON_CONTENT_TYPE);
    if (body !== undefined && !(body instanceof FormData)) {
        headers.set('Content-Type', JSON_CONTENT_TYPE);
    }
    if (!SAFE_METHODS.has(method)) {
        const csrfToken = lookupMetaContent('_csrf');
        const csrfHeader = lookupMetaContent('_csrf_header');
        if (csrfToken && csrfHeader) {
            headers.set(csrfHeader, csrfToken);
        }
    }
    return headers;
}

async function parseResponse(response) {
    if (response.status === 204 || response.status === 205) {
        return null;
    }
    const text = await response.text();
    if (!text) {
        return null;
    }
    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes(JSON_CONTENT_TYPE)) {
        return text;
    }
    try {
        return JSON.parse(text);
    } catch (error) {
        throw new ApiError(response.status, 'INVALID_RESPONSE',
                '서버 응답을 읽을 수 없습니다.');
    }
}

function toRequestBody(body) {
    if (body === undefined || body instanceof FormData) {
        return body;
    }
    return JSON.stringify(body);
}

async function request(method, url, options = {}) {
    const normalizedMethod = method.toUpperCase();
    const response = await fetch(appendQuery(url, options.query), {
        method: normalizedMethod,
        credentials: 'same-origin',
        headers: buildHeaders(normalizedMethod, options.body,
                options.headers),
        body: toRequestBody(options.body),
        signal: options.signal,
    });
    const payload = await parseResponse(response);
    if (!response.ok) {
        if (response.status === 401) {
            window.dispatchEvent(new CustomEvent('bandi:session-expired'));
        }
        const message = payload?.message
                || `요청을 처리하지 못했습니다. (${response.status})`;
        throw new ApiError(response.status, payload?.code || 'HTTP_ERROR',
                message, payload?.fieldErrors || []);
    }
    return payload;
}

export const get = (url, query, options = {}) => request('GET', url, {
    ...options,
    query,
});
export const post = (url, body, options = {}) => request('POST', url, {
    ...options,
    body,
});
export const put = (url, body, options = {}) => request('PUT', url, {
    ...options,
    body,
});
export const patch = (url, body, options = {}) => request('PATCH', url, {
    ...options,
    body,
});
export const del = (url, body, options = {}) => request('DELETE', url, {
    ...options,
    body,
});
