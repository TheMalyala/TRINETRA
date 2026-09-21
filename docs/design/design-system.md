# Monochrome Design System

Trinetra implements a calm, high-contrast monochrome design language ensuring maximum readability, accessibility, and zero ambiguity.

## Color Palette Tokens

| Token | Light Theme | Dark Theme | Purpose |
|---|---|---|---|
| `bg` | `#FFFFFF` | `#000000` | Canvas background |
| `surface` | `#F6F6F6` | `#0D0D0D` | Primary card & panel container |
| `surface-2` | `#EDEDED` | `#1A1A1A` | Secondary containers, inputs |
| `ink` | `#000000` | `#FFFFFF` | Primary headings, values, icons |
| `ink-muted` | `#5C5C5C` | `#A3A3A3` | Subtitles, labels, timestamps |
| `line` | `#D9D9D9` | `#2E2E2E` | 1dp borders, dividers |
| `inverse-bg` | `#000000` | `#FFFFFF` | Inverted callouts, critical alerts |
| `inverse-ink` | `#FFFFFF` | `#000000` | Text on inverted backgrounds |

## Semantics Without Color
- **Abnormal / Out of Range**: Bold tabular numerals + ▲ / ▼ glyphs + filled chart markers + hatched reference band edge.
- **Urgent / Emergency**: Inverted block with Star of Life icon and the word "Urgent".
- **Verification Status**: Checkmark in shield (Verified) vs. outlined shield (Unverified).
- **Origin of Advice**: Solid card border (Doctor-signed) vs. dashed border (Self-reported).
- **Chart Series**: Solid, dashed, or dotted lines; distinct marker shapes (circle, square, triangle).

## Typography
- **UI & Content**: Inter (400 Regular, 500 Medium, 600 SemiBold).
- **Lab Values & Data**: JetBrains Mono (tabular figures, fixed-width numerals).
- **Indic Scripts**: Noto Sans (Devanagari, Telugu, Tamil).
- **Scale**: 12 (caption), 14 (body small), 16 (body), 20 (title), 28 (headline), 40 (display).
