import { ApiError, makeAuthorization, request } from './api.mjs';
import { element, button, bookCover, bookCard, borrowingButton, savingButton, loanRow, skeletons } from './views.mjs';

const $ = id => document.getElementById(id);
const state = {
  authorization: null, username: '', view: 'catalog', query: '', catalogPage: 0,
  catalogPages: 0, books: [], loanPage: 0, loanPages: 0, loans: [], loanTotal: 0,
  owned: new Set(), borrowing: new Set(), returning: new Set(),
  category: '', availableOnly: false, sort: 'catalog', categories: [], categoriesLoaded: false,
  featured: [], featuredIndex: 0, libraryTab: 'active', favorites: [],
  saved: new Set(), saving: new Set(), summary: {}, detailBook: null,
};
let catalogRevision = 0;
let loanRevision = 0;
let sessionRevision = 0;
let detailRevision = 0;
let loginRevision = 0;
let pendingBook = null;
let toastTimer;

function toast(message, isError = false) {
  clearTimeout(toastTimer);
  $('toast').textContent = message;
  $('toast').classList.toggle('is-error', isError);
  $('toast').hidden = false;
  toastTimer = setTimeout(() => { $('toast').hidden = true; }, 5500);
}

function feedback(id, message, retry) {
  const region = $(id);
  region.replaceChildren();
  region.hidden = !message;
  if (message) {
    region.append(document.createTextNode(message));
    if (retry) region.append(button('重新加载', 'text-button', retry));
  }
}

function pageControls(prefix, page, pages) {
  $(`${prefix}-pagination`).hidden = pages < 2;
  $(`${prefix}-previous`).disabled = page === 0;
  $(`${prefix}-next`).disabled = page + 1 >= pages;
  $(`${prefix}-page`).textContent = `第 ${page + 1} 页 / 共 ${pages} 页`;
}

function renderCatalog() {
  $('book-grid').replaceChildren(...state.books.map(book => bookCard(book, bookOptions(book))));
}

function bookOptions(book) {
  return { owned: state.owned.has(book.id), busy: state.borrowing.has(book.id),
    saved: state.saved.has(book.id), saveBusy: state.saving.has(book.id),
    onDetails: openDetails, onBorrow: borrow, onFavorite: toggleFavorite };
}

function updateAccount() {
  const authenticated = Boolean(state.authorization);
  $('sign-in').hidden = authenticated;
  $('signed-in').hidden = !authenticated;
  $('reader-name').textContent = state.username;
  $('avatar').textContent = state.username.slice(0, 1).toLocaleUpperCase();
  $('loan-count').hidden = !authenticated;
  $('loan-count').textContent = state.summary.activeLoans === undefined ? '' : String(state.summary.activeLoans);
  for (const [tab, key] of [['active', 'activeLoans'], ['returned', 'returnedLoans'], ['favorites', 'favoriteBooks']]) {
    $(`count-${tab}`).textContent = state.summary[key] === undefined ? '' : String(state.summary[key]);
  }
}

async function loadCatalog({ quiet = false, adjustPage = true } = {}) {
  const revision = ++catalogRevision;
  $('book-grid').setAttribute('aria-busy', 'true');
  $('catalog-empty').hidden = true;
  feedback('catalog-feedback', '');
  if (!quiet || !state.books.length) $('book-grid').replaceChildren(...skeletons());
  try {
    const query = new URLSearchParams({ q: state.query, category: state.category,
      availableOnly: state.availableOnly, sort: state.sort, page: state.catalogPage, size: 12 });
    const data = await request(`/api/books?${query}`);
    if (revision !== catalogRevision) return;
    if (!Array.isArray(data.items)) throw new ApiError(502, 'INVALID_RESPONSE');
    if (adjustPage && data.items.length === 0 && data.totalElements > 0 && state.catalogPage > 0) {
      state.catalogPage = Math.max(0, data.totalPages - 1);
      return loadCatalog({ quiet, adjustPage: false });
    }
    state.books = data.items;
    state.catalogPages = data.totalPages;
    renderCatalog();
    $('catalog-count').textContent = `${data.totalElements} 本书`;
    $('shelf-heading').textContent = state.query ? '搜索结果' : state.category || '全部书籍';
    $('reset-filters').hidden = !state.query && !state.category && !state.availableOnly && state.sort === 'catalog';
    $('catalog-empty').hidden = data.items.length !== 0;
    pageControls('catalog', state.catalogPage, state.catalogPages);
  } catch (error) {
    if (revision !== catalogRevision) return;
    if (state.books.length) renderCatalog(); else $('book-grid').replaceChildren();
    feedback('catalog-feedback', error.message, () => loadCatalog());
  } finally {
    if (revision === catalogRevision) $('book-grid').setAttribute('aria-busy', 'false');
  }
}

function renderLoans() {
  const authenticated = Boolean(state.authorization);
  const favorites = state.libraryTab === 'favorites';
  const history = state.libraryTab === 'returned';
  const items = favorites ? state.favorites : state.loans;
  $('library-tabs').hidden = !authenticated;
  for (const tab of ['active', 'returned', 'favorites']) {
    $(`tab-${tab}`).setAttribute('aria-pressed', String(state.libraryTab === tab));
  }
  $('loans-guest').hidden = authenticated;
  $('loans-empty').hidden = !authenticated || items.length !== 0;
  $('loan-list').hidden = !authenticated || favorites || items.length === 0;
  $('favorite-grid').hidden = !authenticated || !favorites || items.length === 0;
  $('loan-list').replaceChildren(...state.loans.map(loan => loanRow(loan, {
    busy: state.returning.has(loan.id), onReturn: returnLoan, history, onDetails: openDetails,
  })));
  $('favorite-grid').replaceChildren(...state.favorites.map(book => bookCard(book, bookOptions(book))));
  const empty = favorites ? ['给想读的好书，留个位置', '点击书籍旁的收藏按钮，下一次就能在这里找到它。'] :
    history ? ['每一次借还，都会留下记录', '归还后的书籍会留在这里，方便回顾与再次查找。'] :
      ['书架空着，可能性满满', '挑一本感兴趣的好书，你的借阅会出现在这里。'];
  $('library-empty-title').textContent = empty[0];
  $('library-empty-description').textContent = empty[1];
  $('loans-subtitle').textContent = authenticated ? `${state.username} 的阅读书架，想读与正在读都在这里。` : '想读的、正在读的，以及每一次借还的记录。';
  pageControls('loans', state.loanPage, state.loanPages);
}

async function loadLoans({ adjustPage = true } = {}) {
  const revision = ++loanRevision;
  const session = sessionRevision;
  if (!state.authorization) { renderLoans(); return; }
  $('loans-empty').hidden = true;
  feedback('loans-feedback', '正在整理你的阅读书架…');
  try {
    const favorites = state.libraryTab === 'favorites';
    const path = favorites ? `/api/me/favorites?page=${state.loanPage}&size=12` :
      `/api/me/loans?status=${state.libraryTab}&page=${state.loanPage}&size=8`;
    const data = await request(path, { authorization: state.authorization });
    if (revision !== loanRevision || session !== sessionRevision) return;
    if (!Array.isArray(data.items)) throw new ApiError(502, 'INVALID_RESPONSE');
    if (adjustPage && data.items.length === 0 && data.totalElements > 0 && state.loanPage > 0) {
      state.loanPage = Math.max(0, data.totalPages - 1);
      return loadLoans({ adjustPage: false });
    }
    if (favorites) state.favorites = data.items; else state.loans = data.items;
    state.loanTotal = data.totalElements;
    state.loanPages = data.totalPages;
    if (state.libraryTab === 'active') data.items.forEach(loan => state.owned.add(loan.book.id));
    if (favorites) data.items.forEach(book => state.saved.add(book.id));
    state.summary[{ active: 'activeLoans', returned: 'returnedLoans', favorites: 'favoriteBooks' }[state.libraryTab]] = data.totalElements;
    feedback('loans-feedback', '');
    updateAccount(); renderLoans(); renderCatalog();
  } catch (error) {
    if (revision !== loanRevision || session !== sessionRevision) return;
    if (error.status === 401) { clearSession(); toast('登录已失效，请重新登录。', true); return; }
    renderLoans();
    $('loans-empty').hidden = true;
    feedback('loans-feedback', error.message, loadLoans);
  }
}

function route() {
  const previous = state.view;
  state.view = location.hash === '#loans' ? 'loans' : 'catalog';
  const catalog = state.view === 'catalog';
  $('catalog-view').hidden = !catalog;
  $('loans-view').hidden = catalog;
  $('catalog-nav').toggleAttribute('aria-current', catalog);
  $('loans-nav').toggleAttribute('aria-current', !catalog);
  if (catalog) $('catalog-nav').setAttribute('aria-current', 'page');
  else $('loans-nav').setAttribute('aria-current', 'page');
  document.title = `${catalog ? '阅间' : '我的书架'} · 电子图书馆`;
  if (previous !== state.view || location.hash === '#catalog') window.scrollTo({ top: 0, behavior: 'auto' });
  if (!catalog) loadLoans();
}

function renderCategories() {
  const names = [{ name: '', bookCount: state.categoriesLoaded ? state.categories.reduce((total, item) => total + item.bookCount, 0) : undefined }, ...state.categories];
  $('category-filters').replaceChildren(...names.map(category => {
    const control = button('', 'category-button', () => {
      state.category = category.name; state.catalogPage = 0; renderCategories(); loadCatalog();
    });
    control.append(document.createTextNode(category.name || '全部主题'));
    if (category.bookCount !== undefined) control.append(element('span', '', String(category.bookCount)));
    control.setAttribute('aria-pressed', String(state.category === category.name));
    return control;
  }));
}

async function loadCategories() {
  try {
    const categories = await request('/api/books/categories');
    if (Array.isArray(categories)) { state.categories = categories; state.categoriesLoaded = true; renderCategories(); }
  } catch { $('category-filters').replaceChildren(button('全部主题', 'category-button', resetFilters)); }
}

function renderFeatured() {
  if (!state.featured.length) { $('featured-panel').hidden = true; return; }
  $('featured-panel').hidden = false;
  const book = state.featured[state.featuredIndex % state.featured.length];
  const header = element('div', 'featured-header');
  const next = button('换一本', 'text-button', () => { state.featuredIndex++; renderFeatured(); });
  next.setAttribute('aria-label', '换一本精选书');
  header.append(element('h2', '', '书库精选'), next);
  const copy = element('div', 'featured-copy');
  copy.append(element('h3', '', book.title), element('p', 'featured-description', book.description),
    element('span', 'book-category', book.category), button('查看详情', 'text-button', () => openDetails(book.id)));
  const cover = button('', 'featured-cover', () => openDetails(book.id));
  cover.setAttribute('aria-label', `查看精选书籍《${book.title}》`);
  cover.append(bookCover(book, { priority: true }));
  $('featured-panel').replaceChildren(header, copy, cover);
}

async function loadFeatured() {
  try {
    const data = await request('/api/books?featuredOnly=true&size=6');
    state.featured = data.items || []; renderFeatured();
  } catch { $('featured-panel').hidden = true; }
}

async function syncMembership() {
  if (!state.authorization) return;
  const session = sessionRevision;
  const authorization = state.authorization;
  const results = await Promise.allSettled([
    request('/api/me/library', { authorization }),
    request('/api/me/loans?size=100', { authorization }),
    request('/api/me/favorites?size=100', { authorization }),
  ]);
  if (session !== sessionRevision) return;
  if (results[0].status === 'fulfilled') state.summary = results[0].value;
  if (results[1].status === 'fulfilled') state.owned = new Set(results[1].value.items.map(loan => loan.book.id));
  if (results[2].status === 'fulfilled') state.saved = new Set(results[2].value.items.map(book => book.id));
  updateAccount(); renderCatalog();
}

function resetFilters() {
  $('search').value = ''; $('available-only').checked = false; $('sort').value = 'catalog';
  state.query = ''; state.category = ''; state.availableOnly = false; state.sort = 'catalog'; state.catalogPage = 0;
  renderCategories(); loadCatalog();
}

function closeLogin() {
  loginRevision++;
  $('login-dialog').close();
  $('login-form').reset();
  $('login-submit').disabled = false;
  $('login-submit').textContent = '登录';
  pendingBook = null;
}

function showLogin(book = null, kind = 'borrow') {
  pendingBook = book ? { book, kind } : null;
  $('login-error').hidden = true;
  if ($('book-dialog').open) closeDetails();
  $('login-dialog').showModal();
  $('username').focus();
}

async function signIn(event) {
  event.preventDefault();
  if (!$('login-form').reportValidity() || $('login-submit').disabled) return;
  const attempt = ++loginRevision;
  const username = $('username').value.trim();
  const authorization = makeAuthorization(username, $('password').value);
  $('login-submit').disabled = true;
  $('login-submit').textContent = '正在登录…';
  $('login-error').hidden = true;
  try {
    const data = await request('/api/me/loans?page=0&size=8', { authorization });
    if (attempt !== loginRevision) return;
    sessionRevision++;
    const authenticatedSession = sessionRevision;
    state.authorization = authorization;
    state.username = username;
    state.loans = data.items;
    state.loanTotal = data.totalElements;
    state.loanPages = data.totalPages;
    state.loanPage = 0;
    state.owned = new Set(data.items.map(loan => loan.book.id));
    const selectedBook = pendingBook;
    state.libraryTab = 'active';
    state.summary = { activeLoans: data.totalElements };
    closeLogin(); updateAccount(); renderCatalog(); renderLoans();
    await syncMembership();
    if (authenticatedSession !== sessionRevision) return;
    toast(`欢迎回来，${username}。`);
    if (selectedBook?.kind === 'favorite') {
      if (!state.saved.has(selectedBook.book.id)) await toggleFavorite(selectedBook.book);
      else toast('这本书已经在你的收藏书架中。');
    } else if (selectedBook) {
      if (!state.owned.has(selectedBook.book.id)) await borrow(selectedBook.book);
      else toast('这本书已经在你的当前借阅中。');
    }
  } catch (error) {
    if (attempt !== loginRevision) return;
    $('login-error').textContent = error.message;
    $('login-error').hidden = false;
    $('password').value = '';
    $('password').focus();
  } finally {
    if (attempt === loginRevision) {
      $('login-submit').disabled = false;
      $('login-submit').textContent = '登录';
    }
  }
}

function clearSession() {
  sessionRevision++; loanRevision++;
  state.authorization = null;
  state.username = '';
  state.loans = []; state.owned.clear();
  state.favorites = []; state.saved.clear(); state.summary = {}; state.libraryTab = 'active';
  state.loanTotal = 0; state.loanPages = 0; state.loanPage = 0;
  feedback('loans-feedback', '');
  updateAccount(); renderCatalog(); renderLoans();
}

async function refresh() {
  await syncMembership();
  await Promise.allSettled([loadCatalog({ quiet: true }), loadLoans()]);
  renderDetailActions();
}

async function toggleFavorite(book) {
  if (!state.authorization) { showLogin(book, 'favorite'); return; }
  if (state.saving.has(book.id)) return;
  const session = sessionRevision;
  const remove = state.saved.has(book.id);
  state.saving.add(book.id); renderCatalog(); renderDetailActions();
  if (state.view === 'loans') renderLoans();
  try {
    await request(`/api/me/favorites/${encodeURIComponent(book.id)}`, {
      method: remove ? 'DELETE' : 'PUT', authorization: state.authorization,
    });
    if (session !== sessionRevision) return;
    if (remove) state.saved.delete(book.id); else state.saved.add(book.id);
    if ($('detail-feedback') && state.detailBook?.id === book.id) {
      $('detail-feedback').textContent = remove ? '已移出收藏书架。' : '已收藏到你的书架。';
      $('detail-feedback').hidden = false;
    }
    toast(remove ? `已将《${book.title}》移出收藏。` : `已将《${book.title}》收藏到书架。`);
  } catch (error) {
    if (session !== sessionRevision) return;
    if (error.status === 401) clearSession();
    const message = error.ambiguous ? '收藏结果暂未确认，请刷新书架后核对。' : error.message;
    toast(message, true);
    if ($('detail-feedback') && state.detailBook?.id === book.id) {
      $('detail-feedback').textContent = message; $('detail-feedback').hidden = false;
    }
  } finally {
    state.saving.delete(book.id);
    if (session === sessionRevision) await refresh();
  }
}

async function borrow(book) {
  if (!state.authorization) { showLogin(book); return; }
  if (state.borrowing.has(book.id)) return;
  const session = sessionRevision;
  state.borrowing.add(book.id); renderCatalog();
  if ($('book-dialog').open) closeDetails();
  try {
    await request('/api/loans', { method: 'POST', authorization: state.authorization, body: { bookId: book.id } });
    if (session !== sessionRevision) return;
    state.owned.add(book.id);
    state.loanPage = 0;
    toast(`已借阅《${book.title}》，可以在我的书架中查看。`);
  } catch (error) {
    if (session !== sessionRevision) return;
    if (error.code === 'ALREADY_BORROWED') state.owned.add(book.id);
    if (error.status === 401) clearSession();
    toast(error.ambiguous ? '请求结果暂未确认，请查看我的借阅后再操作。' : error.message, true);
  } finally {
    state.borrowing.delete(book.id);
    if (session === sessionRevision) await refresh();
    else renderCatalog();
  }
}

async function returnLoan(loan) {
  if (!state.authorization || state.returning.has(loan.id)) return;
  const session = sessionRevision;
  state.returning.add(loan.id); renderLoans();
  try {
    await request(`/api/loans/${encodeURIComponent(loan.id)}/return`, { method: 'PUT', authorization: state.authorization });
    if (session !== sessionRevision) return;
    state.owned.delete(loan.book.id);
    state.loanPage = 0;
    toast(`已归还《${loan.book.title}》。期待下一次阅读。`);
  } catch (error) {
    if (session !== sessionRevision) return;
    if (error.status === 401) clearSession();
    toast(error.ambiguous ? '归还结果暂未确认，请刷新我的借阅后核对。' : error.message, true);
  } finally {
    state.returning.delete(loan.id);
    if (session === sessionRevision) await refresh();
  }
}

function closeDetails() { detailRevision++; state.detailBook = null; $('book-dialog').close(); }

function renderDetailActions() {
  if (!$('book-dialog').open || !state.detailBook || !$('detail-actions')) return;
  const book = state.detailBook;
  $('detail-actions').replaceChildren(borrowingButton(book, bookOptions(book)), savingButton(book, bookOptions(book)));
}

async function openDetails(bookId) {
  const revision = ++detailRevision;
  const heading = element('h2', 'detail-title', '正在载入书籍…');
  heading.id = 'detail-title';
  $('detail-content').replaceChildren(heading);
  if (!$('book-dialog').open) $('book-dialog').showModal();
  try {
    const book = await request(`/api/books/${encodeURIComponent(bookId)}`);
    if (revision !== detailRevision) return;
    state.detailBook = book;
    const cover = element('div', 'detail-cover'); cover.append(bookCover(book));
    const title = element('h2', 'detail-title', book.title); title.id = 'detail-title';
    const license = element('div', 'detail-license');
    license.append(element('span', '', '当前借阅名额'), element('strong', '', `${book.availableLicenses} / ${book.totalLicenses} 份可借`));
    const about = element('section', 'detail-section');
    about.append(element('h3', '', '关于本书'), element('p', '', book.description));
    const topics = element('section', 'detail-section');
    if (Array.isArray(book.highlights) && book.highlights.length) {
      const list = element('ul', 'detail-highlights');
      book.highlights.forEach(text => list.append(element('li', '', text)));
      topics.append(element('h3', '', '你会读到'), list);
    }
    const audience = element('section', 'detail-section');
    if (book.audience) audience.append(element('h3', '', '适合谁读'), element('p', '', book.audience));
    const actions = element('div', 'detail-actions'); actions.id = 'detail-actions';
    const detailFeedback = element('p', 'detail-feedback'); detailFeedback.id = 'detail-feedback';
    detailFeedback.setAttribute('role', 'status'); detailFeedback.hidden = true;
    const source = element('div', 'detail-source');
    try {
      const url = new URL(book.sourceUrl);
      if (url.protocol === 'https:' && ['openlibrary.org', 'pages.cs.wisc.edu'].includes(url.hostname)) {
        const link = element('a', '', '查看书目来源');
        link.href = url.href; link.target = '_blank'; link.rel = 'noopener noreferrer'; source.append(link);
      }
    } catch { /* Optional bibliographic source is omitted when absent. */ }
    $('detail-content').replaceChildren(cover, element('span', 'book-category', book.category || '书库藏书'),
      title, element('p', 'detail-author', book.author), about, topics, audience, license, actions, detailFeedback,
      element('p', 'detail-note', '归还后，阅读名额将留给下一位读者。'), source);
    renderDetailActions();
  } catch (error) {
    if (revision !== detailRevision) return;
    heading.textContent = '书籍详情暂时无法加载';
    $('detail-content').append(element('p', 'detail-description', error.message),
      button('重新加载', 'button button-secondary', () => openDetails(bookId)));
  }
}

$('search-form').addEventListener('submit', event => {
  event.preventDefault(); state.query = $('search').value.trim(); state.catalogPage = 0; loadCatalog();
});
$('search').addEventListener('search', () => {
  if (!$('search').value) { state.query = ''; state.catalogPage = 0; loadCatalog(); }
});
$('clear-search').addEventListener('click', () => {
  resetFilters();
});
$('reset-filters').addEventListener('click', resetFilters);
$('available-only').addEventListener('change', () => {
  state.availableOnly = $('available-only').checked; state.catalogPage = 0; loadCatalog();
});
$('sort').addEventListener('change', () => { state.sort = $('sort').value; state.catalogPage = 0; loadCatalog(); });
for (const tab of ['active', 'returned', 'favorites']) {
  $(`tab-${tab}`).addEventListener('click', () => {
    state.libraryTab = tab; state.loanPage = 0; state.loans = []; state.favorites = [];
    renderLoans(); loadLoans();
  });
}
$('catalog-previous').addEventListener('click', () => { if (state.catalogPage > 0) { state.catalogPage--; loadCatalog(); } });
$('catalog-next').addEventListener('click', () => { if (state.catalogPage + 1 < state.catalogPages) { state.catalogPage++; loadCatalog(); } });
$('loans-previous').addEventListener('click', () => { if (state.loanPage > 0) { state.loanPage--; loadLoans(); } });
$('loans-next').addEventListener('click', () => { if (state.loanPage + 1 < state.loanPages) { state.loanPage++; loadLoans(); } });
$('sign-in').addEventListener('click', () => showLogin());
$('loans-sign-in').addEventListener('click', () => showLogin());
$('login-form').addEventListener('submit', signIn);
$('login-close').addEventListener('click', closeLogin);
$('login-dialog').addEventListener('cancel', event => { event.preventDefault(); closeLogin(); });
$('detail-close').addEventListener('click', closeDetails);
$('book-dialog').addEventListener('cancel', event => { event.preventDefault(); closeDetails(); });
for (const [id, close] of [['login-dialog', closeLogin], ['book-dialog', closeDetails]]) {
  $(id).addEventListener('click', event => {
    const bounds = $(id).getBoundingClientRect();
    if (event.target === $(id) && (event.clientX < bounds.left || event.clientX > bounds.right ||
      event.clientY < bounds.top || event.clientY > bounds.bottom)) close();
  });
}
for (const name of ['alice', 'bob']) {
  $(`fill-${name}`).addEventListener('click', () => {
    $('username').value = name; $('password').value = `${name}-demo-pass`; $('login-error').hidden = true;
    $('login-submit').focus();
  });
}
$('sign-out').addEventListener('click', () => { clearSession(); loadCatalog({ quiet: true }); toast('已退出登录。'); });
$('explore-loans').addEventListener('click', () => { location.hash = '#loans'; });
window.addEventListener('hashchange', route);
updateAccount(); route(); loadCatalog(); loadCategories(); loadFeatured();
