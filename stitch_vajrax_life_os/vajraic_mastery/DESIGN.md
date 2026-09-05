---
name: Vajraic Mastery
colors:
  surface: '#17130d'
  surface-dim: '#17130d'
  surface-bright: '#3d3831'
  surface-container-lowest: '#110e08'
  surface-container-low: '#1f1b15'
  surface-container: '#231f19'
  surface-container-high: '#2e2922'
  surface-container-highest: '#39342d'
  on-surface: '#eae1d6'
  on-surface-variant: '#d2c5b2'
  inverse-surface: '#eae1d6'
  inverse-on-surface: '#343029'
  outline: '#9b8f7e'
  outline-variant: '#4e4637'
  surface-tint: '#f0bf64'
  primary: '#f4c367'
  on-primary: '#412d00'
  primary-container: '#d6a84f'
  on-primary-container: '#583e00'
  inverse-primary: '#7c5800'
  secondary: '#c2c7cd'
  on-secondary: '#2c3136'
  secondary-container: '#42474d'
  on-secondary-container: '#b1b5bc'
  tertiary: '#afcbff'
  on-tertiary: '#003063'
  tertiary-container: '#8db0ed'
  on-tertiary-container: '#194278'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdea6'
  primary-fixed-dim: '#f0bf64'
  on-primary-fixed: '#271900'
  on-primary-fixed-variant: '#5e4200'
  secondary-fixed: '#dee3ea'
  secondary-fixed-dim: '#c2c7cd'
  on-secondary-fixed: '#171c21'
  on-secondary-fixed-variant: '#42474d'
  tertiary-fixed: '#d6e3ff'
  tertiary-fixed-dim: '#a9c7ff'
  on-tertiary-fixed: '#001b3d'
  on-tertiary-fixed-variant: '#1f477d'
  background: '#17130d'
  on-background: '#eae1d6'
  surface-variant: '#39342d'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-md:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-margin: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  stack-xl: 64px
---

## Brand & Style

This design system embodies a "Self-Directed Life OS," prioritizing mental clarity, discipline, and intentionality. The aesthetic is anchored in **VAJRAX Morphism**: a sophisticated blend of glassmorphism for informational transparency, soft neumorphic depth for tactile interaction, and high-end editorial layouts for focus.

The experience is "Dark-First," designed to reduce cognitive load and visual fatigue. It evokes a sense of calm authority and private mastery. Unlike typical gamified trackers, this system feels like a digital sanctuary—quiet, premium, and international.

**Design Principles:**
- **Clarity over Clutter:** Every element must earn its place on the screen.
- **The Golden Ratio of Intent:** Gold is reserved exclusively for moments of high-level achievement or "mastery" milestones.
- **Dimensional Quiet:** Depth is used to suggest hierarchy and focus without the use of loud colors.

## Colors

The palette is intentionally restrained to promote a focused, monastic environment.

- **Foundational Neutrals:** The background and surface colors use deep, obsidian tones (`#0B0D0F`, `#14181C`) to provide a high-contrast base for content.
- **Vajra Gold:** This is the "Mastery" accent. It must be used with extreme discipline—limited to progress rings, primary action buttons for core life-path decisions, or achievement badges.
- **Semantic States:** Success, Warning, and Error colors are muted and desaturated. They provide information without breaking the calm, premium mood of the interface.
- **Text Hierarchy:** `Text Primary` is a soft off-white to prevent "vibration" against the dark background, while `Text Muted` handles auxiliary metadata.

## Typography

Manrope is utilized for its geometric precision and modern, approachable character. The typographic system relies on a generous scale to create editorial "breathing room."

- **Editorial Headings:** Use `display-lg` and `headline-lg` for daily summaries and life-path titles to create a sense of importance and scale.
- **Micro-Copy:** `label-sm` is used for metadata and system status, always paired with a higher letter-spacing to maintain legibility in the dark theme.
- **Rhythm:** Maintain a consistent 4px or 8px baseline grid to ensure vertical rhythm across varied content blocks.

## Layout & Spacing

The design system employs a **Fluid Grid** logic with fixed side margins.

- **Margins:** A standard 24px margin on mobile ensures content feels centered and "held."
- **One Screen, One Decision:** Layouts should avoid split-focus. If a task is "In Progress," the layout should minimize all peripheral navigation to center the primary timer or evidence-capture tool.
- **Whitespace as a Feature:** Use `stack-xl` (64px) to separate major content sections (e.g., the transition from "Today's Progress" to "Upcoming Tasks"). This generous spacing reinforces the "Calm" brand pillar.

## Elevation & Depth

Hierarchy is established through a sophisticated layering system rather than heavy drop shadows.

1.  **Level 0 (Base):** `#0B0D0F`. The foundational void.
2.  **Level 1 (Surface):** `#14181C`. Used for the main content cards and list items. Features a subtle 1px border (`#1B2025`) to define edges.
3.  **Level 2 (Elevated):** `#1B2025`. Used for active states or floating menus. 
4.  **Glassmorphism (The Vajra Veil):** For overlays and bottom navigation, use a background blur (20px) with 60% opacity of the Surface color. This maintains context while providing a focused interactive layer.
5.  **Neumorphic Depth:** Subtle inner shadows are used on input fields and "depressed" button states to provide a tactile, physical feel to the digital OS.

## Shapes

The shape language is "Rounded," striking a balance between organic softness and professional structure.

- **Standard Radius:** 0.5rem (8px) for small components like tags and input fields.
- **Large Radius:** 1rem (16px) for primary containers and dashboard cards.
- **Interactive Elements:** Buttons follow a "Soft" to "Pill" transition depending on their importance. Primary Mastery actions often use a more pronounced pill shape to feel distinct from structural cards.

## Components

### Buttons
- **Primary Mastery:** Solid `Vajra Gold` background with dark text. Reserved for "Complete Task," "Begin Journey," or "Save Evidence."
- **Secondary/Ghost:** `Surface` background with a subtle border. Used for "Skip" or "Edit."
- **Tertiary/Minimum:** Text-only with an underline or muted color for "Minimum/Skip" options, honoring the user's freedom to adjust intensity.

### Cards
- **Focus Cards:** Utilize Level 1 Surface with a 1px border. On-tap, they may exhibit a soft neumorphic "lift" or a slight scale increase (1.02x).
- **Glass Cards:** Used for global status updates (e.g., a "Current Path" banner) that floats over the main scroll view.

### Progress Indicators
- **Vajra Rings:** Thin, elegant circular strokes using `Vajra Gold`. Avoid thick, heavy gauges to maintain the "Minimalist" aesthetic.

### Input Fields
- Integrated into the surface with a subtle inner shadow (neumorphic) to feel "carved" into the OS. Focus state is indicated by a thin gold border-bottom.

### Life Path Chips
- Rounded chips with `text-secondary` and a `surface-elevated` background. Active paths gain a small gold dot indicator rather than a full color change.