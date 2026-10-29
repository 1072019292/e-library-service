---
name: 阅间 · E-Library
description: A contemporary reading room for browsing books and managing a private shelf.
colors:
  ground: "#f7f6f2"
  surface: "#ffffff"
  soft: "#efefe8"
  ink: "#23372f"
  muted: "#657166"
  accent: "#245c45"
  accent-hover: "#184c36"
  accent-ink: "#f9fbf5"
  line: "#d9dfd5"
  focus: "#337657"
  error: "#a43c34"
  error-ground: "#faefeb"
typography:
  display:
    fontFamily: '"Reading Serif", serif'
    fontSize: "46px"
    fontWeight: 500
    lineHeight: 1.34
    letterSpacing: "-.025em"
  body:
    fontFamily: '"Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif'
    fontSize: "16px"
    lineHeight: 1.65
  book-title:
    fontSize: "14px"
    fontWeight: 600
    lineHeight: 1.5
rounded:
  cover: "3px"
  category: "8px"
  control: "10px"
  toast: "12px"
  featured: "14px"
  dialog: "20px"
spacing:
  action-gap: "8px"
  section: "24px"
  shelf-column: "30px"
  shelf-row: "36px"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.accent-ink}"
    rounded: "{rounded.control}"
    padding: "10px 18px"
  button-primary-hover:
    backgroundColor: "{colors.accent-hover}"
---

# Design System: 阅间 · E-Library

## Overview

**Creative North Star: "The Contemporary Reading Room"**

Paper-like space, forest-green actions, Chinese serif headlines and typeset book covers define the implemented interface. Familiar search, filters and lending controls carry the tasks; the wordmark, reading invitation and book art carry its editorial character. Core UI, the display font and Phosphor SVG icons ship locally with the Java application.

This is the built system in `src/main/resources/static/index.html` and `assets/{app.css,app.js,views.mjs}`. Rendered references are the desktop, mobile, user-width and dark images in `.impeccable/review/v2-final/`.

## Colors

The frontmatter records the light CSS custom properties. Dark mode follows `prefers-color-scheme`; there is no theme switch. Cover palettes retain their own colors in both themes.

| Token | Use | Dark override |
|---|---|---|
| ground | Page canvas | `#18241e` |
| surface | Search, login and detail surfaces | `#213129` |
| soft | Featured panel, selected saved state, quiet feedback | `#293a30` |
| ink | Headings and body text | `#e8eddf` |
| muted | Supporting text, unselected navigation and metadata | `#a8b6a6` |
| accent | Primary actions, selected category, availability and active navigation | `#a8ceb1` |
| accent-hover | Primary action hover | `#c3dec7` |
| accent-ink | Text on the accent fill | `#183222` |
| line | Dividers and outlined controls | `#3a4c3f` |
| focus | Keyboard focus outlines | `#b4dbbc` |
| error | Form errors and light-theme error toast | `#efb5a8` |
| error-ground | JavaScript-disabled notice | `#432f2a` |

Fallback covers cycle by book ID through sage (`#cbd8ba`), ochre (`#e4bd8f`), blue-grey (`#b8cdcf`), forest (`#334e43`) and stone (`#d5d0c3`), each with its own ink and spine line. These colors identify cover variants, not categories or availability. The dark error toast uses a separate solid fill (`#914c41`) with pale text.

## Typography

The local Noto Serif SC font is registered as **Reading Serif** at weight 500 with `font-display: swap`. It appears in the wordmark, reading invitation, shelf heading and login heading. Body and controls use the platform sans stack in the frontmatter. Fallback covers and the featured book title use Georgia / Times New Roman / serif.

| Role | Implemented hierarchy |
|---|---|
| Catalog invitation | Display token; 38px at widths up to 1100px, 33px up to 767px, 29px up to 370px. Two complete Chinese phrases stay on separate unbroken lines. |
| Brand / shelf heading | Brand 27px, 24px on mobile; shelf heading 42px, 32px on mobile. |
| Book and detail titles | Catalog title uses the book-title token and a two-line, 42px minimum block; detail title 25px / 1.35, weight 600. Full catalog title remains available through its title attribute and detail view. |
| Supporting copy | Intro 15px on desktop, 12px on mobile; catalog excerpts 12px / 1.65; detail paragraphs and highlights 13px / 1.9. |
| Catalog metadata and actions | Desktop category/author/availability 11px and borrow action 12px; mobile metadata 12px and borrow action 13px. Catalog titles remain 14px. |

Counts and timestamps use tabular numerals. Catalog authors clamp to one line and excerpts to two; details retain the full copy. Small cover imprints are decorative duplicates, with book identity repeated in the accessible card text. English cover titles scale with cover-container width using `clamp(16px, 11cqw, 26px)` in catalog cards.

## Layout

Header and main share a centered 1400px outer shell, including horizontal padding; the widest content area is 1280px. Desktop header places brand, navigation and account in three columns. The introduction pairs a flexible text column with a 460px featured panel, then search, category facets, availability/sort controls and a paged shelf follow in normal document flow.

| Viewport width | Shell gutter | Shelf | Structural changes |
|---|---|---|---|
| Above 1100px | 60px | Four columns; 30px column / 36px row gaps | Introduction gap 56px. |
| 768–1100px | 36px | Three columns; same gaps | Featured panel narrows to 390px; introduction gap 25px. |
| Up to 767px | 24px | Two columns; 22px column / 34px row gaps | Navigation moves below brand/account; introduction stacks; featured cover is 88px wide; search fills its own row; filter options wrap. |
| Up to 370px | 18px | Two columns; 16px column gap | Invitation shrinks and borrow-arrow icons are hidden. |

Category facets scroll horizontally. A narrow desktop panel uses the same viewport rules as a phone. Catalog and saved shelves show up to 12 books per page; current/history lists show up to eight records. Loan rows use an 80px cover, flexible copy and trailing action; on mobile a 66px cover spans the copy/action rows and the action moves below the copy.

The login dialog is centered at up to 440px wide with 16px minimum side clearance. Book details occupy a right-aligned sheet up to 560px wide and `100dvh` high; mobile removes its rounded corners and reduces horizontal padding to 28px.

## Elevation & Depth

The page and book cards sit directly on the canvas. Thin dividers and the soft featured panel establish grouping. Local covers use a subtle shadow (`0 8px 20px rgb(30 49 36 / 9%)`); cover-button hover lifts them by 5px and deepens that shadow. Publisher images use a contained image with a small drop shadow instead of a card shadow.

Dialogs and toasts use the shared shadow (`0 22px 72px rgb(28 49 36 / 18%)`), darkened to `rgb(10 24 15 / 32%)` in dark mode. Dialog backdrops use `rgb(20 35 28 / 43%)`, or `rgb(9 21 14 / 65%)` in dark mode. Control color changes last 180ms; cover movement lasts 220ms. Reduced-motion preference removes transitions and cover lift.

## Shapes

The frontmatter captures the recurring corner sizes: near-square printed covers, gently rounded controls, a softer featured panel and rounded dialogs. The desktop detail sheet rounds only its left corners; the mobile sheet is square. Avatars are circular. Catalog covers use a width/height ratio of 0.8, featured covers 0.68, and loan thumbnails 0.72. Cover spine lines and restrained circular ornaments belong to the local cover artwork.

## Components

- **Navigation and shelf tabs:** hash routes select catalog or private shelf. Active navigation and current/history/saved controls use an accent underline; categories use an accent fill. Current loan and shelf counts reflect API state. Selected states use `aria-current` or `aria-pressed`.
- **Search and filters:** a 350px desktop search field becomes full width on mobile. The container has a visible focus-within outline. Keyword, category, available-only and sort controls combine; reset clears them and returns to the first page. Sort offers catalog order, recent additions and title A–Z.
- **Book card and featured panel:** cover and title open details; category, author, description, available/total licenses, borrow and save follow. The featured panel shows one of six sourced choices and changes only through “换一本”. A failed or empty featured request hides that optional panel.
- **Cover loading:** local typography appears immediately. Recognized Open Library source URLs request the `-L` image; only a loaded image at least 300px wide and 380px high replaces the fallback. Missing, failed or smaller scans keep local art. Images use `object-fit: contain`; source attribution remains in the footer.
- **Actions:** primary buttons use the accent fill; secondary and borrow actions use quiet outlines. Base buttons have a 44px minimum height; catalog borrow/save controls are 39px on desktop and 44px on mobile. Borrow labels distinguish ready, busy, already borrowed and unavailable states, disabling the latter three. Saved buttons expose their pressed state and disable while updating.
- **Detail and login dialogs:** native modal dialogs provide focus handling. Both close with their close button, Escape or an outside click. Login focuses the username; errors clear and refocus the password. Details contain full description, reading highlights, audience guidance, live license counts, borrow/save actions and an optional bibliographic source link. Credentials stay in page memory; logout clears private UI state. A requested borrow/save action can continue after successful login.
- **Private shelves:** current loans offer return; history displays borrow/return timestamps and a detail action; saved books reuse catalog cards. Guest and each empty shelf have explicit copy and an appropriate next action.
- **Feedback:** catalog loading uses five static skeletons and `aria-busy`; private shelves use a loading message and details use a loading heading. Catalog, shelf and detail errors offer retry; empty search offers reset. Writes disable their related action, then refresh catalog/membership state. Success and error toasts announce through a status region for 5.5 seconds. Ambiguous write outcomes ask the reader to reconcile the shelf; they do not announce success or automatically repeat the write.

Keyboard users have a skip link, labeled fields/actions and a two-pixel focus outline. Buttons, inputs and links use a 5px outline offset; search uses its own container treatment. Dynamic copy is inserted as text, and decorative SVGs/covers are hidden from assistive technology while actionable controls retain book-specific labels.

## Do's and Don'ts

- **Do** preserve the paper/forest hierarchy, local display font, complete-phrase headline breaks and shared shelf components.
- **Do** derive book copy, facets, counts and membership from the API, and keep public browsing available before login.
- **Do** keep the interface usable with only local assets and retain a clean fallback until an external cover passes its resolution gate.
- **Don't** turn cover palette variants into category or status semantics, or replace text state labels with color alone.
- **Don't** add ratings, due dates, reading files or personalised recommendation claims to the implemented catalog and lending flow.
