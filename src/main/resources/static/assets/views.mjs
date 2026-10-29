export function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

export function icon(name) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
  svg.setAttribute('aria-hidden', 'true');
  use.setAttribute('href', `/assets/icons.svg#${name}`);
  svg.append(use);
  return svg;
}

export function button(text, className, action) {
  const node = element('button', className, text);
  node.type = 'button';
  node.addEventListener('click', action);
  return node;
}

export function bookCover(book, { priority = false } = {}) {
  const cover = element('div', `book-cover cover-${Number(book.id) % 5}`);
  cover.setAttribute('aria-hidden', 'true');
  cover.append(element('span', 'cover-imprint', '阅间书库 / READING ROOM'),
    element('span', 'cover-title', book.title), element('span', 'cover-ornament'),
    element('span', 'cover-author', book.author));
  if (typeof book.coverUrl === 'string' && /^https:\/\/covers\.openlibrary\.org\/b\/id\/\d+-M\.jpg\?default=false$/.test(book.coverUrl)) {
    const image = element('img', 'cover-image');
    image.alt = ''; image.loading = 'eager'; image.decoding = 'async';
    image.fetchPriority = priority ? 'high' : 'auto';
    image.referrerPolicy = 'no-referrer';
    cover.dataset.coverState = 'pending';
    image.addEventListener('load', () => {
      if (!cover.isConnected) return;
      if (image.naturalWidth < 300 || image.naturalHeight < 380) {
        cover.dataset.coverState = 'low-resolution'; return;
      }
      cover.classList.add('real-cover'); cover.dataset.coverState = 'loaded'; cover.replaceChildren(image);
    }, { once: true });
    image.addEventListener('error', () => { cover.dataset.coverState = 'unavailable'; }, { once: true });
    image.src = book.coverUrl.replace('-M.jpg', '-L.jpg');
  }
  return cover;
}

export function savingButton(book, { saved, saveBusy, onFavorite }, { compact = false } = {}) {
  const label = saveBusy ? '正在更新收藏' : saved ? '取消收藏' : '收藏';
  const node = button('', `button save-button${saved ? ' is-saved' : ''}${compact ? ' compact' : ''}`, () => onFavorite?.(book));
  node.disabled = Boolean(saveBusy);
  node.setAttribute('aria-label', `${label}：${book.title}`);
  node.setAttribute('aria-pressed', String(Boolean(saved)));
  node.append(icon('bookmark-simple'));
  if (!compact) node.append(document.createTextNode(saved ? '已收藏' : '收藏到书架'));
  return node;
}

export function borrowingButton(book, { owned, busy, onBorrow }) {
  const text = busy ? '正在借阅…' : owned ? '已在借阅' : book.availableLicenses > 0 ? '借阅这本书' : '暂时借完';
  const node = button(text, `button borrow-button${owned ? ' is-owned' : ''}`, () => onBorrow(book));
  node.disabled = busy || owned || book.availableLicenses < 1;
  node.dataset.bookId = book.id;
  node.setAttribute('aria-label', `${text}：${book.title}`);
  if (!node.disabled) node.append(icon('arrow-up-right'));
  return node;
}

export function bookCard(book, options) {
  const card = element('article', 'book-card');
  const cover = button('', 'cover-button', () => options.onDetails(book.id));
  cover.setAttribute('aria-label', `查看《${book.title}》详情`);
  cover.append(bookCover(book));
  const meta = element('div', 'book-meta');
  const title = element('h3', 'book-title');
  title.append(button(book.title, '', () => options.onDetails(book.id)));
  title.title = book.title;
  const bottom = element('div', 'book-bottom');
  const availability = element('span', 'availability');
  availability.append(element('strong', '', `${book.availableLicenses} 份可借`),
    document.createTextNode(` / 共 ${book.totalLicenses} 份`));
  const actions = element('div', 'book-actions');
  actions.append(borrowingButton(book, options), savingButton(book, options, { compact: true }));
  bottom.append(availability, actions);
  const author = element('p', 'book-author', book.author); author.title = book.author;
  meta.append(element('span', 'book-category', book.category || '书库藏书'), title, author,
    element('p', 'book-excerpt', book.description), bottom);
  card.append(cover, meta);
  return card;
}

export function loanRow(loan, { busy, onReturn, history = false, onDetails }) {
  const row = element('article', 'loan-row');
  const copy = element('div', 'loan-copy');
  const date = new Date(loan.borrowedAt);
  const time = element('time', '', Number.isNaN(date.getTime()) ? '借阅时间暂不可用' :
    `借阅于 ${new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(date)}`);
  time.dateTime = loan.borrowedAt;
  copy.append(element('h2', '', loan.book.title), element('p', '', loan.book.author), time);
  if (history) {
    const returned = new Date(loan.returnedAt);
    const returnedTime = element('time', 'returned-time', Number.isNaN(returned.getTime()) ? '归还时间暂不可用' :
      `归还于 ${new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(returned)}`);
    returnedTime.dateTime = loan.returnedAt;
    copy.append(returnedTime);
  }
  const action = button(history ? '查看书籍' : busy ? '正在归还…' : '归还书籍', 'button button-secondary',
    () => history ? onDetails(loan.book.id) : onReturn(loan));
  action.disabled = busy;
  action.setAttribute('aria-label', `${history ? '查看' : '归还'}《${loan.book.title}》`);
  row.append(bookCover(loan.book), copy, action);
  return row;
}

export function skeletons(count = 5) {
  return Array.from({ length: count }, () => {
    const card = element('div', 'skeleton');
    card.setAttribute('aria-hidden', 'true');
    card.append(element('div', 'skeleton-cover'), element('div', 'skeleton-text'), element('div', 'skeleton-text'));
    return card;
  });
}
