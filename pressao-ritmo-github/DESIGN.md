---
name: Sleek Health Pulse
colors:
  surface: '#FFFFFF'
  surface-dim: '#ded8e0'
  surface-bright: '#fef7ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f8f1f9'
  surface-container: '#f2ecf4'
  surface-container-high: '#ede6ee'
  surface-container-highest: '#e7e0e8'
  on-surface: '#1d1b20'
  on-surface-variant: '#494551'
  inverse-surface: '#322f35'
  inverse-on-surface: '#f5eff6'
  outline: '#7a7582'
  outline-variant: '#cbc4d2'
  surface-tint: '#6750a4'
  primary: '#4f378a'
  on-primary: '#ffffff'
  primary-container: '#6750a4'
  on-primary-container: '#e0d2ff'
  inverse-primary: '#cfbcff'
  secondary: '#625b71'
  on-secondary: '#ffffff'
  secondary-container: '#e8def9'
  on-secondary-container: '#686177'
  tertiary: '#633b48'
  on-tertiary: '#ffffff'
  tertiary-container: '#7d5260'
  on-tertiary-container: '#ffcbda'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#cfbcff'
  on-primary-fixed: '#22005d'
  on-primary-fixed-variant: '#4f378a'
  secondary-fixed: '#e8def9'
  secondary-fixed-dim: '#ccc2dc'
  on-secondary-fixed: '#1e192b'
  on-secondary-fixed-variant: '#4a4358'
  tertiary-fixed: '#ffd9e3'
  tertiary-fixed-dim: '#eeb8c8'
  on-tertiary-fixed: '#31111d'
  on-tertiary-fixed-variant: '#633b48'
  background: '#fef7ff'
  on-background: '#1d1b20'
  surface-variant: '#e7e0e8'
  status-normal: '#2E7D32'
  status-pre: '#F9A825'
  status-stage1: '#EF6C00'
  status-stage2: '#C62828'
  status-stage3: '#880E4F'
typography:
  display-hero:
    fontFamily: Hanken Grotesk
    fontSize: 38px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -1px
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 42px
  headline-sm:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-caps:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.8px
  display-hero-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 38px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  margin-main: 1.5rem
  gutter-grid: 1rem
  card-padding: 1.25rem
  stack-sm: 0.5rem
  stack-md: 1rem
  stack-lg: 2rem
---

## Brand & Style

The design system is engineered for "Pressão & Ritmo," a health-monitoring application that balances clinical precision with an approachable, modern aesthetic. It follows a **Sleek Interface** philosophy, rooted in **Material Design 3 (M3)** principles, ensuring a native feel for mobile users while elevating the experience through high-end polish.

The brand personality is **reliable, calm, and sophisticated**. It aims to reduce the anxiety often associated with medical monitoring by using a "Soft-Surgical" aesthetic: clean, spacious, and highly organized. By utilizing high-quality whitespace and a structured "Hero Card" hierarchy, the system ensures that critical health data remains the focal point without overwhelming the user.

Key style characteristics include:
- **Material Design 3 Evolution**: Adaptive surfaces and tonal palettes.
- **Modern Professionalism**: A mix of vibrant primary accents and muted slate secondary tones.
- **High Information Clarity**: Strong typographic scale for immediate data recognition.

## Colors

The color palette is divided into functional brand colors and critical clinical indicators. 

- **Primary & Secondary**: The vibrant purple (#6750A4) serves as the core interactive color, used for primary actions and brand presence. The slate grey (#625B71) provides a professional foundation for secondary UI elements.
- **Clinical Spectrum**: Health states are communicated via a strict semantic scale. These colors should be used for background tints in status chips, data visualization points, and the vertical "classification bars" in history lists.
- **Surface Strategy**: The background uses a "Perl" white (#FEF7FF) to soften the overall contrast, while interaction cards utilize pure white (#FFFFFF) to create a distinct layer of depth through elevation and subtle shadows.

## Typography

This design system utilizes a dual-font strategy to maximize both readability and character.

- **Headlines & Numbers**: **Hanken Grotesk** is chosen for its sharp, contemporary feel. The "Extra Bold" weight is reserved specifically for measurement numbers (38sp) to ensure they are the first thing a user sees.
- **Body & Labels**: **Inter** provides a neutral, highly legible foundation for technical labels and form fields. 
- **Hierarchy**: A strong vertical rhythm is maintained by contrasting the tight, uppercase labels with expansive, large-scale data displays. Mobile overrides are applied to the Hero displays to prevent layout breaking on smaller devices.

## Layout & Spacing

The layout follows a **Fluid Grid** model optimized for mobile-first interaction. 

- **The Grid**: A 4-column system for mobile and an 8-column system for tablets.
- **Dashboard Structure**: Uses a vertical stacking pattern. The "Hero Card" always occupies the full width of the container minus margins. Below, a 2x2 grid is used for secondary metrics.
- **Safe Zones**: A standard 24px (1.5rem) margin is applied to the left and right of the screen to ensure elements do not feel cramped.
- **Rhythm**: Spacing is based on an 8px increment system. Use `stack-lg` (32px) between major functional blocks and `stack-sm` (8px) between labels and their corresponding data points.

## Elevation & Depth

Hierarchy is achieved through **Ambient Shadows** and **Tonal Layering**, moving away from flat design into a more tactile, Material 3-inspired depth.

- **Level 0 (Background)**: `#FEF7FF` - Flat.
- **Level 1 (Main Cards)**: Pure white `#FFFFFF` with a soft, diffused shadow (0px 4px 12px rgba(0,0,0,0.05)).
- **Level 2 (Interactive Elements/Buttons)**: Primary color surfaces with a slightly more pronounced shadow to indicate "pressability."
- **Dialogs & Pop-ups**: Use the highest elevation tier with a 28dp corner radius and a 15% backdrop dimming to focus the user’s attention.

## Shapes

The shape language is overtly **Rounded**, conveying a sense of friendliness and safety appropriate for a healthcare application.

- **Primary Containers**: Cards and Hero sections use a 24dp radius (`rounded-xl`).
- **Small Components**: Buttons, input fields, and selection chips use a 12dp radius (`rounded-lg`).
- **Status Indicators**: Vertical bars on the left side of history items should have the top-left and bottom-left corners rounded to match the card, creating a seamless visual tab.
- **Avatars**: Circular (100% radius) for profile images and user selection icons.

## Components

### Buttons
- **Primary**: 12dp rounded corners, primary purple background, white text. Full-width for main actions like "Save."
- **Secondary**: Outlined purple with a 12dp radius for less critical actions like "Filter."

### Cards (The "Hero" & "Metric")
- **Hero Card**: 24dp rounded corners, pure white background, subtle shadow. Contains the 38sp bold measurement.
- **Metric Grid Cards**: 24dp rounded corners. Icons should be placed in the top left, with the metric value centered.

### Input Fields
- Underlined or outlined M3 style with 12dp rounded corners. Include a colored icon on the left (e.g., a heart icon for pulse) to provide a visual cue for the field type.

### Clinical History List
- **Vertical Indicator**: A 6px wide vertical bar on the extreme left of each list item. The bar color must change based on the clinical status (Green, Amber, etc.).
- **Typography**: The measurement value should be semi-bold (16sp) while the date is body-small.

### Classification Chips
- Dynamic chips that appear during data entry. These should use a "Container" color (a 20% opacity version of the clinical status color) with high-contrast text for accessibility.