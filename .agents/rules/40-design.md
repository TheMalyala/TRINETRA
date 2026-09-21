# Monochrome Design Rules

1. **Strict Palette**:
   - Light Theme: Background `#FFFFFF`, Surface `#F6F6F6`, Ink `#000000`, Ink Muted `#5C5C5C`, Line `#D9D9D9`.
   - Dark Theme: Background `#000000`, Surface `#0D0D0D`, Ink `#FFFFFF`, Ink Muted `#A3A3A3`, Line `#2E2E2E`.
   - No color tokens outside of black, white, and neutral grays.
2. **Semantics Without Color (WCAG AA)**:
   - Out of range values: Bold text + ▲ (high) / ▼ (low) glyphs + filled chart markers.
   - Critical / Emergency: Inverted high-contrast banner (black bg in light theme) with Star of Life icon.
   - Verification status: Checkmark in shield (Verified) vs. outlined shield (Unverified).
   - Advice origin: Solid border (Doctor-signed) vs. dashed border (Self-reported).
   - Chart lines: Solid, dashed, dotted line styles and distinct point shapes (circle, square, triangle).
3. **Typography**:
   - Primary: Inter for UI text.
   - Lab values & numbers: JetBrains Mono (tabular figures).
   - Indic scripts: Noto Sans (Devanagari, Telugu, Tamil).
4. **Touch Targets & Radii**:
   - Minimum 48dp touch targets.
   - Card border radius: 12–16dp. Border stroke: 1dp hairlines.
