# Ludo UI Kit — Front-end Modernisation

This document describes the shared design tokens, reusable components, and front-end conventions introduced for the African Ludo Android app and web admin panel.

## Design Tokens

The following tokens are available across platforms:

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `ludo_primary` | `#1E2148` | `#BEC2FF` | Primary brand indigo |
| `ludo_secondary` | `#F59E0B` | `#FFB648` | Accent gold/orange |
| `ludo_surface` | `#F7F7FB` | `#161836` | Surfaces/backgrounds |
| `ludo_on_surface` | `#111426` | `#E4E7FF` | Primary text |
| `ludo_surface_variant` | `#E1E2EC` | `#3A3D57` | Cards, chips |
| Corner radius | 8dp, 16dp, 24dp | 8dp, 16dp, 24dp | Shape system |
| Spacing scale | 4dp base (4/8/12/16/20/24/32/36) | Same | Layout rhythm |
| Font | Inter | Inter | Typography across headings, body, labels |

### Android
- Tokens live in `app/src/main/res/values/colors.xml`, `typography.xml`, `dimens.xml`, with night variants in `values-night`.
- `Theme.Ludo` in `themes.xml` binds Material 3 colors, typography, ripple/outline, and component defaults.
- The `ui-components` module exposes `LudoButton`, `LudoCard`, `LudoTextField`, `LudoDialog`, and `LudoSnackbar` for consistent styling.

### Web
- Tailwind configuration (`web-admin/tailwind.config.cjs`) mirrors the token set.
- Global styles (fonts, antialiasing) are handled in `web-admin/src/styles/index.css`.

## Android UI Modernisation

- Material 3 theme applied to all activities via `Theme.Ludo` and AppCompat DayNight support.
- Single-activity navigation uses `navigation/main_nav_graph.xml` with destinations for Home, Wallet, Play, Profile, and Leaderboard.
- `MainActivity` now hosts a `MaterialToolbar`, adaptive bottom navigation, quick-play FAB, theme toggling, and FCM notification preferences dialog.
- New fragments provide modular screens:
  - `HomeFragment`: wallet snapshot, quick actions.
  - `WalletFragment`: deposit/withdraw shortcuts.
  - `ProfileFragment`: user profile summary with logout dialog.
  - `LeaderboardFragment`: launch leaderboard experiences.
- `activity_splash.xml` features a branded Lottie dice animation (`res/raw/lottie_dice_roll.json`).
- Layouts use `ConstraintLayout`, `NestedScrollView`, and `ui-components` widgets to maintain spacing and accessibility (TalkBack labels, `sp` text sizing).

## Web Admin Panel

A new React + Tailwind admin shell (`web-admin/`) provides:

- Responsive sidebar/top-bar layout with dark mode toggle.
- `StatsCard`, `DataTable`, `LudoModal`, `ToggleSwitch`, `FormField` components for reusable cards, tables, dialogs, and inputs.
- `AppLayout` screen demonstrates stats, player table with search/pagination, payment gateway toggles, and modal form for tournaments.
- Tailwind dark mode uses the `class` strategy, matching Android theming tokens.

## Component Usage

### Android `LudoButton`
```xml
<com.tomtomkenya.ludouikit.components.LudoButton
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Play Now" />
```

### Web `StatsCard`
```tsx
<StatsCard
  title="Players Online"
  value="1,248"
  trend="▲ 6% vs yesterday"
  trendDirection="up"
/>
```

## Accessibility & Motion

- Contrast ratios follow ≥4.5:1 by default palette.
- Motion: Splash screen Lottie animation, Match tabs celebration card (MotionLayout + dice Lottie), quick actions Motion-friendly transitions, snackbar fade animations.
- All interactive elements expose descriptive text or labels.

## Build & Testing

- **Android**: `./gradlew assembleDebug` (Material 3, Navigation Component dependencies included).
- **Web**: `pnpm|npm install` then `npm run dev` / `npm run build` in `web-admin/`.

## Future Enhancements

- Extend `ui-components` with list, badge, and chip widgets.
- Hook Android nav destinations to backend data (wallet history, leaderboard API).
- Connect web DataTable to live REST endpoints and auth guards.
