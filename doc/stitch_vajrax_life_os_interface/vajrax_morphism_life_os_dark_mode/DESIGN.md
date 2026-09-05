---
name: VAJRAX Morphism Life OS
colors:
  surface: '#111316'
  surface-dim: '#111316'
  surface-bright: '#37393d'
  surface-container-lowest: '#0c0e11'
  surface-container-low: '#1a1c1f'
  surface-container: '#1e2023'
  surface-container-high: '#282a2d'
  surface-container-highest: '#333538'
  on-surface: '#e2e2e6'
  on-surface-variant: '#d0c5af'
  inverse-surface: '#e2e2e6'
  inverse-on-surface: '#2f3034'
  outline: '#99907c'
  outline-variant: '#4d4635'
  surface-tint: '#e9c349'
  primary: '#f2ca50'
  on-primary: '#3c2f00'
  primary-container: '#d4af37'
  on-primary-container: '#554300'
  inverse-primary: '#735c00'
  secondary: '#b8c8da'
  on-secondary: '#223240'
  secondary-container: '#394857'
  on-secondary-container: '#a7b7c8'
  tertiary: '#c4cfe1'
  on-tertiary: '#26313f'
  tertiary-container: '#a9b4c5'
  on-tertiary-container: '#3b4654'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffe088'
  primary-fixed-dim: '#e9c349'
  on-primary-fixed: '#241a00'
  on-primary-fixed-variant: '#574500'
  secondary-fixed: '#d4e4f6'
  secondary-fixed-dim: '#b8c8da'
  on-secondary-fixed: '#0d1d2a'
  on-secondary-fixed-variant: '#394857'
  tertiary-fixed: '#d8e3f5'
  tertiary-fixed-dim: '#bcc7d9'
  on-tertiary-fixed: '#111c29'
  on-tertiary-fixed-variant: '#3d4856'
  background: '#111316'
  on-background: '#e2e2e6'
  surface-variant: '#333538'
typography:
  display:
    fontFamily: sora
    fontSize: 36px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: sora
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: sora
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: sora
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: sora
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  body-lg:
    fontFamily: manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: 0em
  body-md:
    fontFamily: manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0em
  body-sm:
    fontFamily: manrope
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-md:
    fontFamily: sora
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: sora
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
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
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
  gutter-sm: 1rem
  gutter-md: 1.5rem
  gutter-lg: 2rem
---

## Brand & Style

This design system crafts an intentional, meditative environment for high-agency individuals managing life, productivity, habits, and knowledge. Moving away from standard flat interfaces, the system delivers a tactile, sculpted experience inspired by soft neumorphic modeling combined with modern dark luxury. 

The aesthetic centers around dark obsidian and deep charcoal tones, creating quiet spatial presence while minimizing cognitive load. Surfaces feel extruded from a unified dark medium rather than layered like loose cards. Tactile depressions convey active or selected states, while soft extruded planes represent interactable surfaces. Delicate gold accents supply disciplined punctuation for critical milestones, active highlights, and state completions, while muted slate provides unobtrusive secondary framing. 

The emotional response is calm, authoritative, sovereign, and disciplined—resembling a bespoke physical executive instrument crafted from matte titanium, sculpted obsidian glass, and subtle metallic inlay.

## Colors

The system operates strictly within a sophisticated dark landscape composed of carefully calibrated obsidian depths and refined metallic highlights:

- **Primary (`#D4AF37`)**: A tailored champagne gold. Reserved for high-value states, active tab markers, radial progress arcs, and task completion affirmations. It is used with extreme restraint to preserve its executive status.
- **Secondary (`#708090`)**: Muted slate. Handles secondary metadata, inactive icon states, progress track grooves, and supporting typography.
- **Tertiary (`#3A4553`)**: Deep slate steel. Supplies inner-shadow boundaries, sunken card base plates, and neutral progress tracks.
- **Neutral (`#121417`)**: Base obsidian charcoal. Serves as the canvas substrate from which all extruded elements rise (`#16191D`) and into which debossed wells sink (`#0E1012`).

### Functional Color Applications
- **Surface Elevation Extruded**: `#181B20` with top-left specular illumination and bottom-right shadow absorption.
- **Surface Inset / Pressed**: `#0F1114` with concave rim lighting.
- **Text & Content**: Primary text runs on `#EDEDEF` (92% opacity crisp chalk); secondary text uses `#8E95A2` (slate grey); disabled states fall back to `#484F5B`.

## Typography

The type system blends the geometric authority of `sora` for display headers, numeric metrics, and action badges with the ergonomic readability of `manrope` for structural descriptions and task flows.

- **Headlines & Metrics**: `sora` communicates forward-thinking precision. Generous tracking on uppercase labels (`0.04em` to `0.06em`) produces an architectural poise reminiscent of luxury horology and automotive cockpits.
- **Body & Data Listings**: `manrope` maintains pristine clarity across dark UI surfaces, mitigating light halation around white letterforms on dark backgrounds.
- **Numbers & Metrics**: Primary metrics (`8/10`, `85%`, `92%`) rely on `sora` with semi-bold weights to anchor visual interest cleanly inside recessed stat bays.

## Layout & Spacing

The layout model implements a fluid adaptive container that respects spatial rhythm through 4px and 8px step increments:

- **Mobile Viewports (<768px)**: Single-column flow with `1.25rem` screen edge margins. Interactive components preserve minimum 48px touch heights. Sticky navigation anchors to the bottom with floating pill geometry.
- **Tablet / Desktop Viewports (>=768px)**: Split-pane dashboard architecture. Left/primary panel hosts overview summaries, circadian task rings, and top-level metrics; secondary side panel hosts modular day/week agendas, matrix check-ins, and deep work intervals.
- **Internal Density**: Content rhythm uses `space-xs` (8px) for tightly coupled metadata, `space-md` (16px) for item gaps within lists, and `space-xl` (32px) to isolate distinct interactive modules.

## Elevation & Depth

Visual hierarchy is communicated via dual-light soft neumorphic sculpting on deep charcoal backgrounds. Hard dropshadows and harsh stroke borders are eliminated.

### Extrusion Mechanics
Surfaces assume light falling from the top-left at a 315-degree angle:
- **Raised Planes (Cards, Floating Pills, Stat Containers)**:
  - Top-left highlight: `-6px -6px 16px rgba(255, 255, 255, 0.04)`
  - Bottom-right shadow: `8px 8px 20px rgba(0, 0, 0, 0.65)`
  - Soft perimeter ring: subtle 1px stroke set to `rgba(255, 255, 255, 0.05)` to define boundaries without cutting into the background.

- **Debossed / Sunken Wells (Completed Checkbox Wells, Active Row States, Progress Tracks)**:
  - Top-left deep shadow: `inset 4px 4px 10px rgba(0, 0, 0, 0.75)`
  - Bottom-right lip highlight: `inset -3px -3px 8px rgba(255, 255, 255, 0.03)`
  - Background surface shifts 3% darker than the canvas floor (`#0E1012`).

- **Floating Command Anchors (Bottom Nav, Modal Overlays)**:
  - Higher altitude softness: `0px 14px 32px rgba(0, 0, 0, 0.7)`, topped with `0px 0px 1px rgba(255, 255, 255, 0.12)`.

## Shapes

The design system uses a balanced rounded geometry (`roundedness: 2`) to ensure that raised surfaces feel organic, touch-friendly, and naturally milled from solid material:

- **Micro Controls & Checkboxes**: 10px to 14px radius (soft organic squircle).
- **Stat Modules & Inner Cards**: 16px radius (`rounded-lg`).
- **Primary Cards & Containers**: 24px radius (`rounded-xl`).
- **Navigation Dock & Pill Badges**: Fully rounded continuous curvature (`rounded-full`).
- **Inner Debossed Insets**: Always scaled 4px to 6px smaller in corner radius than their parent containers to maintain concentric harmony.

## Components

### Buttons
- **Elevated Button**: Extruded obsidian base, `label-md` uppercase text in crisp chalk. On hover, the top-left highlight intensifies. On active click, the element transitions to an inset inner shadow state, depressing visually into the canvas.
- **Gold Accent Action**: Extruded metallic gradient from `#DFBA44` to `#C59D28` with dark obsidian typography (`#121417`), reserved for core daily actions (e.g., "Complete Day", "Deep Session").

### Checkboxes & Habit Matrix Rings
- **Unchecked**: A debossed dark circular well (`#0F1114`) with a soft internal shadow and faint slate boundary.
- **Checked**: Filled with a warm gold core (`#D4AF37`) or elevated obsidian medallion holding a glowing gold check glyph.
- **Streak Cells**: Circular cells arranged horizontally across calendar dates. Inactive days appear as shallow slate craters; successful days illuminate as gold discs.

### Cards & Habit Blocks
- Crafted as extruded obsidian tiles. The interior arranges the activity icon inside an embossed circular crest, followed by title, timing metadata, and an inset trailing indicator.
- Selected or in-progress rows transform from an elevated plane to a smooth debossed trench with a 1.5px gold accent accent along the leading left lip.

### Chips & Badges
- Compact pill-shaped modules. Soft recessed background with `label-sm` tracking. When signaling active progress, a miniature 6px circular amber-gold LED indicator pulses softly at the leading edge.

### Stat Containers
- Modular metric trios (e.g., Today `8/10`, Week `85%`, Consistency `92%`). Contained in extruded soft-cornered bays with centered vertical typography: `label-sm` slate grey header above a large `headline-md` value in pure chalk white.

### Bottom Floating Navigation Bar
- A suspended obsidian pill floating 24px above the viewport base. Backdropped with `rgba(18, 20, 23, 0.85)` surface blur (16px).
- Navigation icons remain muted slate (`#708090`); active items illuminate with a subtle gold glow underneath and bright champagne gold iconography (`#D4AF37`).