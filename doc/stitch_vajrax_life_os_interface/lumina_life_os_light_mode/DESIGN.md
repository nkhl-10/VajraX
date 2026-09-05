---
name: Lumina Life OS
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#464554'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#777586'
  outline-variant: '#c7c4d7'
  surface-tint: '#5148d7'
  primary: '#2a14b4'
  on-primary: '#ffffff'
  primary-container: '#4338ca'
  on-primary-container: '#c1beff'
  inverse-primary: '#c3c0ff'
  secondary: '#4b41e1'
  on-secondary: '#ffffff'
  secondary-container: '#645efb'
  on-secondary-container: '#fffbff'
  tertiary: '#353a44'
  on-tertiary: '#ffffff'
  tertiary-container: '#4c515c'
  on-tertiary-container: '#c0c4d0'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e3dfff'
  primary-fixed-dim: '#c3c0ff'
  on-primary-fixed: '#100069'
  on-primary-fixed-variant: '#372abf'
  secondary-fixed: '#e2dfff'
  secondary-fixed-dim: '#c3c0ff'
  on-secondary-fixed: '#0f0069'
  on-secondary-fixed-variant: '#3323cc'
  tertiary-fixed: '#dee2ef'
  tertiary-fixed-dim: '#c2c6d3'
  on-tertiary-fixed: '#171c25'
  on-tertiary-fixed-variant: '#424751'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 38px
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '800'
    lineHeight: 32px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
  display-stat:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '800'
    lineHeight: 24px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 1.5rem
  space-2xl: 2rem
  gutter-grid: 1rem
  margin-screen: 1.25rem
---

## Brand & Style

This design system embodies a modern, focused, and calming personal productivity experience. Built around the concept of mindful daily momentum, it delivers an uncluttered "Life OS" interface where visual noise is systematically eliminated in favor of clean space, soft card architecture, and crisp, intentional typography.

The target audience comprises high-output professionals, self-directed builders, and students seeking habit mastery and structured scheduling without cognitive overwhelm. The visual aesthetic fuses crisp contemporary minimalism with refined micro-surfaces: pure white elevated cards float effortlessly over an airy ice-tinted backdrop, complemented by high-contrast periwinkle-indigo accents and tranquil tinted containers. The emotional tone is deliberate, encouraging, and impeccably organized.

## Colors

The color palette is engineered for optical clarity, high text contrast, and serene ambient hierarchy.

- **Primary Accent (`#4338CA` / `#4F46E5`):** A deep, electric royal periwinkle used for primary focal points, active state highlights, completed state rings, and selected indicators.
- **Secondary Accent (`#6366F1`):** Lighter vibrant indigo used for graphical data rings, secondary status markers, and focus states.
- **Tertiary Tint (`#EEF2FF` / `#E0E7FF`):** Soft pastel lavender-blue washes that delineate active row containers, icon housing badges, and subtle data groupings without introducing hard divider lines.
- **Neutral Stack:**
  - `Surface Base`: `#F8F9FD` provides an airy, ultra-light canvas.
  - `Card Background`: Pure `#FFFFFF` delivers crisp contrast against the base canvas.
  - `Text Primary`: `#0F172A` offers razor-sharp legibility for titles and metrics.
  - `Text Muted`: `#64748B` and `#94A3B8` handle timestamps, category subtexts, and auxiliary indicators.
  - `Borders & Separators`: `#E2E8F0` and `#F1F5F9` frame structural boundaries with near-invisible subtlety.
  - `Floating Dock`: `#EAECEF` with 80% opacity and backdrop blur creates a distinctive floating navigation bar.

## Typography

The type system is powered entirely by **Plus Jakarta Sans**, imparting a geometric yet human touch with wide apertures and clean horizontal rhythms.

- **Headlines (`headline-xl`, `headline-lg`):** Strong, tight tracking (-0.02em) with dense ink weights (700/800) create assertive entry points such as "Good Morning." and "Tasks".
- **Metric Displays (`display-stat`):** Bold, grounded weights for numbers and percentages (`8/10`, `85%`, `92%`), ensuring immediate cognitive capture of completion progress.
- **Body & Captions:** Tuned with slightly relaxed letter spacing for glanceability in task lists, time ranges, and secondary meta tags.

## Layout & Spacing

The spatial engine follows an 8pt baseline rhythm, using an adaptable columnar fluid system that flexes comfortably from single-column mobile views to multi-pane dashboard layouts.

- **Mobile Viewport (< 640px):** Single-column stacked stream. Side screen margins are locked at `1.25rem` (`20px`). Cards span full width, with interior paddings of `1.25rem` to `1.5rem`.
- **Tablet / Desktop Split View (≥ 768px):** Two-column modular grid with a `1.5rem` gutter separating analytical widgets from linear schedule/matrix tracking panels.
- **Rhythm & Grouping:** Related elements (e.g., metric label + value) sit within `0.25rem` to `0.5rem`. Interactive checklist items maintain a consistent vertical touch target height of `48px` to `56px` with `0.5rem` row gaps.

## Elevation & Depth

This system avoids heavy drop shadows and dramatic physical skeuomorphism, opting for diffused ambient buoyancy and multi-layer tonal stacking.

- **Level 0 (Canvas Base):** Plain `#F8F9FD` tint that anchors the viewport.
- **Level 1 (Card & Section Surfaces):** Pure `#FFFFFF` surface with an ultra-soft, low-opacity shadow tinted with slate-indigo: `box-shadow: 0 10px 25px -5px rgba(67, 56, 202, 0.04), 0 8px 10px -6px rgba(15, 23, 42, 0.02)`. An optional border of `1px solid #F1F5F9` gives edge precision on low-gamma displays.
- **Level 2 (Active Rows & Tonal Wells):** Inset surfaces using `#EEF2FF` with zero shadow or a delicate hairline outline of `rgba(79, 70, 229, 0.15)`.
- **Level 3 (Floating Overlays & Docks):** Bottom tab navigation bar utilizes a pill container with backdrop filter blur (`backdrop-filter: blur(12px)`) and a gentle boundary shadow: `box-shadow: 0 12px 32px -4px rgba(15, 23, 42, 0.08)`.

## Shapes

The geometry balances smooth organic comfort with functional discipline. 

- **Primary Cards:** Styled with `rounded-2xl` (`1.5rem` / `24px`), giving them a friendly, tactile slate feel.
- **Interior Stat Wells & Row Tiles:** Styled with `rounded-xl` (`0.75rem` / `12px` to `1rem` / `16px`) to nest naturally inside main cards.
- **Floating Controls & Checkboxes:** Circular icons and full-radius pills (`rounded-full` / `9999px`) are reserved for check targets, status chips, day-of-week selectors, and bottom navigation pill containers.

## Components

### Habit & Task Checkboxes
- **Completed State:** Filled circular button (`#4338CA` or `#4F46E5`), containing a crisp white vector checkmark.
- **Pending State:** Empty circular outline (`border: 1.5px solid #CBD5E1`), background transparent or tinted white.
- **Current Active State:** Outlined circle with primary accent border and a subtle internal progress accent or center minus dash indicator.

### Status Badges & Pills
- **Percentage Chip:** Lavender background (`#EEF2FF`), text and icon in `#4F46E5`, pill shape with `px-3 py-1`, font weight 600 (`label-md`).
- **Date Selector Pill:** Active day is highlighted in a solid primary-colored circle with white text; weekday abbreviations and dates stack vertically with muted slate colors.

### Schedule / Matrix Tracker Rows
- Composed of a two-part layout: an informational task group on the left (rounded square icon container in `#EEF2FF`, bold title, secondary time subtitle) and an aligned horizontal series of date check circles on the right.
- Active or focused rows receive an airy `#EEF2FF` fill with soft indigo borders to highlight the active workflow item.

### Metric Stat Pods
- Compact interior cards placed in a horizontal flex/grid row inside the overview card.
- Pale neutral gray-blue background (`#F8FAFC`), rounded with `1rem` radius, containing a muted uppercase or small-title label and bold tabular stat figures.

### Floating Bottom Navigation Dock
- Pill-shaped bar floating `16px` above the bottom screen edge, centered horizontally.
- Light neutral background (`#EAECEF` or `#F1F5F9` at 85% opacity with blur), housing clean icon-plus-label items. Active tab is highlighted with primary blue tint and icon weight.