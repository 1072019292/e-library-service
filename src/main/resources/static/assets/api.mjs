export class ApiError extends Error {
  constructor(status, code, ambiguous = false) {
    super(errorMessage(code));
    this.status = status;
    this.code = code;
    this.ambiguous = ambiguous;
  }
}

const messages = new Map(Object.entries({
    UNAUTHORIZED: '账号或密码不正确，请重新登录。',
    ALREADY_BORROWED: '这本书已经在你的借阅中，可以前往我的借阅查看。',
    NO_LICENSE_AVAILABLE: '这本书暂时借完了，归还后就会有新的借阅名额。',
    BOOK_NOT_FOUND: '这本书暂时无法找到，请刷新书架。',
    LOAN_NOT_FOUND: '未找到这条借阅记录，请刷新后核对。',
    BOOK_BUSY: '这本书正在办理借还，请稍后再试。',
    INVALID_REQUEST: '请检查输入内容后再试。',
    CONNECTION_ERROR: '暂时无法连接书库，请确认服务已启动后重试。',
    INVALID_RESPONSE: '书库返回的数据暂时无法读取，请稍后重试。',
    INTERNAL_ERROR: '书库暂时遇到问题，请稍后再试。',
}));

export function errorMessage(code) {
  return messages.get(code) || '操作暂时未完成，请刷新后核对。';
}

export function makeAuthorization(username, password) {
  const bytes = new TextEncoder().encode(`${username}:${password}`);
  return `Basic ${btoa(String.fromCharCode(...bytes))}`;
}

export async function request(path, { method = 'GET', authorization, body } = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  const headers = { Accept: 'application/json, application/problem+json' };
  if (authorization) headers.Authorization = authorization;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  try {
    const response = await fetch(path, {
      method, headers, credentials: 'omit', cache: 'no-store', signal: controller.signal,
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    });
    if (response.ok && response.status === 204) return null;
    let data;
    try { data = await response.json(); }
    catch { throw new ApiError(response.status, 'INVALID_RESPONSE', !['GET', 'HEAD'].includes(method)); }
    if (!response.ok) throw new ApiError(response.status, data.code || 'INTERNAL_ERROR');
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(0, 'CONNECTION_ERROR', !['GET', 'HEAD'].includes(method));
  } finally { clearTimeout(timeout); }
}
