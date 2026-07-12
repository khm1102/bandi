const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

async function request(method, url, body) {
    const response = await fetch(url, {
        method,
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken,
        },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`요청 실패: ${response.status}`);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

export const post = (url, body) => request('POST', url, body);
export const put = (url, body) => request('PUT', url, body);
export const patch = (url, body) => request('PATCH', url, body);
export const del = (url) => request('DELETE', url);
