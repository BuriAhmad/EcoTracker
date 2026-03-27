# EcoTrack — UI & Design Guide

---

## 1. Design System

### 1.1 Color Palette

#### `res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ==================== BACKGROUNDS ==================== -->
    <color name="bg_void">#FF050A07</color>           <!-- Deepest background, splash/auth screens -->
    <color name="bg_deep">#FF0A120E</color>            <!-- Primary app background -->
    <color name="bg_surface">#FF0F1A14</color>         <!-- Card surfaces, bottom nav -->
    <color name="bg_card">#FF121F18</color>            <!-- Elevated cards, dialogs -->
    <color name="bg_input">#FF0A1510</color>           <!-- Input fields, search bars -->
    <color name="bg_elevated">#FF152A1F</color>        <!-- Hover states, selected items -->
    <color name="bg_glass">>#1A8BFFA3</color>          <!-- Glass-morphism overlays (10% green) -->

    <!-- ==================== ACCENT COLORS ==================== -->
    <color name="accent_green">#FFA3F77B</color>       <!-- Primary accent — CTAs, highlights, eco-score ring -->
    <color name="accent_green_dim">#33A3F77B</color>   <!-- 20% green — subtle backgrounds, card borders -->
    <color name="accent_green_glow">#4DA3F77B</color>  <!-- 30% green — glow effects -->
    <color name="accent_green_dark">#FF1A3A12</color>  <!-- Dark green — selected card bg -->

    <color name="accent_cyan">#FF5CE0D6</color>        <!-- Secondary accent — info, links, water metric -->
    <color name="accent_cyan_dim">#335CE0D6</color>    <!-- 20% cyan -->
    <color name="accent_cyan_dark">#FF0D2A28</color>   <!-- Dark cyan background -->

    <color name="accent_amber">#FFF5C842</color>       <!-- Tertiary accent — streak, points, warnings -->
    <color name="accent_amber_dim">#33F5C842</color>   <!-- 20% amber -->

    <color name="accent_violet">#FF8B6FD4</color>      <!-- Quaternary accent — badges, special events -->
    <color name="accent_violet_dim">#338B6FD4</color>  <!-- 20% violet -->

    <!-- ==================== TEXT COLORS ==================== -->
    <color name="text_primary">#FFFFFFFF</color>       <!-- Headings, primary content -->
    <color name="text_secondary">#B3FFFFFF</color>     <!-- 70% white — body text, labels -->
    <color name="text_muted">#66FFFFFF</color>         <!-- 40% white — hints, captions, inactive -->
    <color name="text_on_accent">#FF050A07</color>     <!-- Dark text on green buttons -->
    <color name="text_accent">#FFA3F77B</color>        <!-- Green text for emphasis -->

    <!-- ==================== SEMANTIC COLORS ==================== -->
    <color name="color_biking">#FFA3F77B</color>       <!-- Green -->
    <color name="color_walking">#FF5CE0D6</color>      <!-- Cyan -->
    <color name="color_recycling">#FFF5C842</color>    <!-- Amber -->
    <color name="color_water_save">#FF42B4F5</color>   <!-- Blue -->
    <color name="color_energy">#FF8B6FD4</color>       <!-- Violet -->
    <color name="color_plastic_free">#FFFF7B93</color> <!-- Pink -->

    <color name="color_success">#FFA3F77B</color>
    <color name="color_warning">#FFF5C842</color>
    <color name="color_error">#FFFF5252</color>
    <color name="color_info">#FF5CE0D6</color>

    <!-- ==================== COMPONENT COLORS ==================== -->
    <color name="border_card">#1AFFFFFF</color>        <!-- 10% white borders -->
    <color name="border_active">#FFA3F77B</color>      <!-- Active/selected border -->
    <color name="divider">#0DFFFFFF</color>            <!-- 5% white dividers -->
    <color name="ripple_dark">#1AFFFFFF</color>        <!-- Ripple on dark surfaces -->
    <color name="ripple_light">#33000000</color>       <!-- Ripple on light surfaces -->
    <color name="bottom_nav_bg">#FF0F1A14</color>      <!-- Bottom nav background -->
    <color name="scrim_overlay">#B3000000</color>      <!-- 70% black scrim -->

    <!-- ==================== CHART COLORS ==================== -->
    <color name="chart_green">#FFA3F77B</color>
    <color name="chart_cyan">#FF5CE0D6</color>
    <color name="chart_amber">#FFF5C842</color>
    <color name="chart_violet">#FF8B6FD4</color>
    <color name="chart_grid_line">#1AFFFFFF</color>    <!-- 10% white grid -->
</resources>
```

### 1.2 Typography

#### Font Families
- **Display/Heading font:** `Syne` (Google Fonts) — Bold, extra bold.  Used for screen titles, hero numbers, section headers.
- **Body font:** `Plus Jakarta Sans` (Google Fonts) — Regular, Medium, SemiBold, Bold.  Used for body text, labels, button text, input text.

#### `res/font/` Directory
```
res/font/
  syne_bold.ttf
  syne_extrabold.ttf
  plus_jakarta_sans_regular.ttf
  plus_jakarta_sans_medium.ttf
  plus_jakarta_sans_semibold.ttf
  plus_jakarta_sans_bold.ttf
```

#### Font Family XML Definitions
```xml
<!-- res/font/font_syne.xml -->
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="700" android:font="@font/syne_bold" />
    <font android:fontStyle="normal" android:fontWeight="800" android:font="@font/syne_extrabold" />
</font-family>

<!-- res/font/font_jakarta.xml -->
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="400" android:font="@font/plus_jakarta_sans_regular" />
    <font android:fontStyle="normal" android:fontWeight="500" android:font="@font/plus_jakarta_sans_medium" />
    <font android:fontStyle="normal" android:fontWeight="600" android:font="@font/plus_jakarta_sans_semibold" />
    <font android:fontStyle="normal" android:fontWeight="700" android:font="@font/plus_jakarta_sans_bold" />
</font-family>
```

#### Text Appearance Styles

| Style Name | Font | Weight | Size (sp) | Color | Usage |
|-----------|------|--------|-----------|-------|-------|
| `TextAppearance.Eco.HeadlineLarge` | Syne | ExtraBold (800) | 28 | `text_primary` | Screen titles |
| `TextAppearance.Eco.HeadlineMedium` | Syne | Bold (700) | 22 | `text_primary` | Section headers |
| `TextAppearance.Eco.HeadlineSmall` | Syne | Bold (700) | 18 | `text_primary` | Card titles |
| `TextAppearance.Eco.HeroNumber` | Syne | ExtraBold (800) | 44 | `accent_green` | Big stat numbers |
| `TextAppearance.Eco.TitleLarge` | Jakarta | SemiBold (600) | 18 | `text_primary` | List titles |
| `TextAppearance.Eco.TitleMedium` | Jakarta | SemiBold (600) | 16 | `text_primary` | Card headings |
| `TextAppearance.Eco.Body` | Jakarta | Regular (400) | 14 | `text_secondary` | Body text |
| `TextAppearance.Eco.BodyMedium` | Jakarta | Medium (500) | 14 | `text_secondary` | Emphasized body |
| `TextAppearance.Eco.Label` | Jakarta | SemiBold (600) | 12 | `text_muted` | Category labels, captions |
| `TextAppearance.Eco.LabelSmall` | Jakarta | Medium (500) | 10 | `text_muted` | Timestamps, micro labels |
| `TextAppearance.Eco.Button` | Jakarta | Bold (700) | 14 | `text_on_accent` | CTA buttons |
| `TextAppearance.Eco.ButtonSmall` | Jakarta | SemiBold (600) | 12 | `text_on_accent` | Small action buttons |
| `TextAppearance.Eco.Chip` | Jakarta | Medium (500) | 12 | `text_secondary` | Chip/pill text |
| `TextAppearance.Eco.StatValue` | Syne | Bold (700) | 20 | `text_primary` | Stat numbers |
| `TextAppearance.Eco.StatUnit` | Jakarta | Regular (400) | 12 | `text_muted` | Stat units/labels |

### 1.3 Spacing & Dimensions

#### `res/values/dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ==================== BASE SPACING ==================== -->
    <dimen name="spacing_xxs">2dp</dimen>
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_sm">8dp</dimen>
    <dimen name="spacing_md">12dp</dimen>
    <dimen name="spacing_base">16dp</dimen>
    <dimen name="spacing_lg">20dp</dimen>
    <dimen name="spacing_xl">24dp</dimen>
    <dimen name="spacing_xxl">32dp</dimen>
    <dimen name="spacing_xxxl">48dp</dimen>

    <!-- ==================== CORNER RADII ==================== -->
    <dimen name="radius_sm">8dp</dimen>
    <dimen name="radius_md">12dp</dimen>
    <dimen name="radius_lg">16dp</dimen>
    <dimen name="radius_xl">20dp</dimen>
    <dimen name="radius_xxl">24dp</dimen>
    <dimen name="radius_round">50dp</dimen>     <!-- Fully round (pills, avatars) -->

    <!-- ==================== CARD DIMENSIONS ==================== -->
    <dimen name="card_padding">16dp</dimen>
    <dimen name="card_margin">16dp</dimen>
    <dimen name="card_radius">16dp</dimen>
    <dimen name="card_elevation">0dp</dimen>     <!-- We use borders, not elevation -->
    <dimen name="card_border_width">1dp</dimen>

    <!-- ==================== COMPONENT SIZES ==================== -->
    <dimen name="avatar_small">32dp</dimen>
    <dimen name="avatar_medium">48dp</dimen>
    <dimen name="avatar_large">80dp</dimen>
    <dimen name="avatar_hero">100dp</dimen>

    <dimen name="icon_small">16dp</dimen>
    <dimen name="icon_medium">24dp</dimen>
    <dimen name="icon_large">36dp</dimen>
    <dimen name="icon_xl">48dp</dimen>

    <dimen name="button_height">52dp</dimen>
    <dimen name="button_height_small">40dp</dimen>
    <dimen name="button_radius">12dp</dimen>

    <dimen name="input_height">52dp</dimen>
    <dimen name="input_radius">12dp</dimen>

    <dimen name="bottom_nav_height">64dp</dimen>
    <dimen name="bottom_nav_icon">24dp</dimen>

    <dimen name="eco_score_ring_size">140dp</dimen>
    <dimen name="eco_score_ring_stroke">10dp</dimen>

    <dimen name="heatmap_cell_size">18dp</dimen>
    <dimen name="heatmap_cell_gap">3dp</dimen>

    <dimen name="podium_avatar_first">64dp</dimen>
    <dimen name="podium_avatar_second">52dp</dimen>
    <dimen name="podium_avatar_third">52dp</dimen>

    <dimen name="activity_card_size">100dp</dimen>  <!-- Activity type grid cell -->
    <dimen name="activity_icon_size">32dp</dimen>

    <dimen name="badge_grid_item">80dp</dimen>
    <dimen name="badge_icon_large">64dp</dimen>

    <!-- ==================== SCREEN PADDING ==================== -->
    <dimen name="screen_padding_horizontal">20dp</dimen>
    <dimen name="screen_padding_top">16dp</dimen>
    <dimen name="screen_padding_bottom">24dp</dimen>
    <dimen name="content_max_width">420dp</dimen>

    <!-- ==================== CHART DIMENSIONS ==================== -->
    <dimen name="chart_height">200dp</dimen>
    <dimen name="chart_bar_width">28dp</dimen>
    <dimen name="chart_bar_radius">6dp</dimen>
</resources>
```

### 1.4 App Theme

#### `res/values/themes.xml`
```xml
<resources>
    <style name="Theme.EcoTrack" parent="Theme.Material3.Dark.NoActionBar">
        <!-- Primary brand color = green accent -->
        <item name="colorPrimary">@color/accent_green</item>
        <item name="colorOnPrimary">@color/text_on_accent</item>
        <item name="colorPrimaryContainer">@color/accent_green_dark</item>
        <item name="colorOnPrimaryContainer">@color/accent_green</item>

        <!-- Secondary = cyan -->
        <item name="colorSecondary">@color/accent_cyan</item>
        <item name="colorOnSecondary">@color/text_on_accent</item>

        <!-- Tertiary = amber -->
        <item name="colorTertiary">@color/accent_amber</item>
        <item name="colorOnTertiary">@color/text_on_accent</item>

        <!-- Background & surface -->
        <item name="android:colorBackground">@color/bg_deep</item>
        <item name="colorSurface">@color/bg_surface</item>
        <item name="colorSurfaceVariant">@color/bg_card</item>
        <item name="colorOnSurface">@color/text_primary</item>
        <item name="colorOnSurfaceVariant">@color/text_secondary</item>

        <!-- Error -->
        <item name="colorError">@color/color_error</item>

        <!-- Status bar and nav bar -->
        <item name="android:statusBarColor">@color/bg_void</item>
        <item name="android:navigationBarColor">@color/bg_surface</item>
        <item name="android:windowLightStatusBar">false</item>

        <!-- Window -->
        <item name="android:windowBackground">@color/bg_deep</item>

        <!-- Default text -->
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColorSecondary">@color/text_secondary</item>
        <item name="android:textColorHint">@color/text_muted</item>

        <!-- Font -->
        <item name="android:fontFamily">@font/font_jakarta</item>
    </style>

    <!-- Splash / Auth screen theme with void background -->
    <style name="Theme.EcoTrack.Auth" parent="Theme.EcoTrack">
        <item name="android:windowBackground">@color/bg_void</item>
        <item name="android:statusBarColor">@color/bg_void</item>
    </style>
</resources>
```

### 1.5 Common Widget Styles

```xml
<!-- Primary CTA button (green, full width) -->
<style name="Widget.Eco.Button.Primary" parent="Widget.Material3.Button">
    <item name="android:layout_width">match_parent</item>
    <item name="android:layout_height">@dimen/button_height</item>
    <item name="cornerRadius">@dimen/button_radius</item>
    <item name="backgroundTint">@color/accent_green</item>
    <item name="android:textColor">@color/text_on_accent</item>
    <item name="android:textAppearance">@style/TextAppearance.Eco.Button</item>
    <item name="android:textAllCaps">false</item>
</style>

<!-- Secondary / outline button -->
<style name="Widget.Eco.Button.Outlined" parent="Widget.Material3.Button.OutlinedButton">
    <item name="android:layout_width">match_parent</item>
    <item name="android:layout_height">@dimen/button_height</item>
    <item name="cornerRadius">@dimen/button_radius</item>
    <item name="strokeColor">@color/accent_green</item>
    <item name="strokeWidth">1dp</item>
    <item name="android:textColor">@color/accent_green</item>
    <item name="android:textAppearance">@style/TextAppearance.Eco.Button</item>
    <item name="android:textAllCaps">false</item>
</style>

<!-- Small chip/pill button -->
<style name="Widget.Eco.Button.Pill" parent="Widget.Material3.Button">
    <item name="android:layout_height">@dimen/button_height_small</item>
    <item name="cornerRadius">@dimen/radius_round</item>
    <item name="android:textAppearance">@style/TextAppearance.Eco.Chip</item>
    <item name="android:textAllCaps">false</item>
    <item name="android:paddingStart">16dp</item>
    <item name="android:paddingEnd">16dp</item>
</style>

<!-- Standard card container -->
<style name="Widget.Eco.Card" parent="Widget.Material3.CardView.Outlined">
    <item name="cardBackgroundColor">@color/bg_card</item>
    <item name="cardCornerRadius">@dimen/card_radius</item>
    <item name="cardElevation">0dp</item>
    <item name="strokeWidth">@dimen/card_border_width</item>
    <item name="strokeColor">@color/border_card</item>
    <item name="contentPadding">@dimen/card_padding</item>
</style>

<!-- Text input -->
<style name="Widget.Eco.TextInput" parent="Widget.Material3.TextInputLayout.OutlinedBox">
    <item name="boxBackgroundColor">@color/bg_input</item>
    <item name="boxStrokeColor">@color/border_card</item>
    <item name="boxStrokeWidth">1dp</item>
    <item name="boxCornerRadiusTopStart">@dimen/input_radius</item>
    <item name="boxCornerRadiusTopEnd">@dimen/input_radius</item>
    <item name="boxCornerRadiusBottomStart">@dimen/input_radius</item>
    <item name="boxCornerRadiusBottomEnd">@dimen/input_radius</item>
    <item name="hintTextColor">@color/text_muted</item>
</style>

<!-- Bottom Navigation View -->
<style name="Widget.Eco.BottomNav" parent="Widget.Material3.BottomNavigationView">
    <item name="android:background">@color/bottom_nav_bg</item>
    <item name="itemIconTint">@color/selector_bottom_nav</item>
    <item name="itemTextColor">@color/selector_bottom_nav</item>
    <item name="labelVisibilityMode">labeled</item>
    <item name="itemIconSize">@dimen/bottom_nav_icon</item>
</style>
```

### 1.6 Drawable Resources Needed

| Drawable | Type | Description |
|----------|------|-------------|
| `bg_card_border.xml` | Shape | Rounded rect, `bg_card` fill, 1dp `border_card` stroke, 16dp corners |
| `bg_card_active.xml` | Shape | Rounded rect, `accent_green_dark` fill, 1dp `accent_green` stroke |
| `bg_input.xml` | Shape | Rounded rect, `bg_input` fill, 1dp `border_card` stroke, 12dp corners |
| `bg_button_primary.xml` | Shape | Rounded rect, `accent_green` fill, 12dp corners |
| `bg_button_outlined.xml` | Shape | Rounded rect, transparent fill, 1dp `accent_green` stroke, 12dp corners |
| `bg_pill_selected.xml` | Shape | Fully rounded, `accent_green` fill |
| `bg_pill_unselected.xml` | Shape | Fully rounded, `bg_card` fill, 1dp `border_card` stroke |
| `bg_eco_score_track.xml` | Ring shape | For eco-score background track |
| `bg_bottom_nav.xml` | Shape | Top border 1dp `border_card`, `bg_surface` fill |
| `bg_glass_card.xml` | Shape | Rounded rect, 10% `accent_green` fill, 1dp `accent_green_dim` stroke |
| `selector_bottom_nav.xml` | Selector | Selected = `accent_green`, Default = `text_muted` |
| `bg_heatmap_none.xml` | Shape | 4dp corners, `bg_card` fill |
| `bg_heatmap_low.xml` | Shape | 4dp corners, 30% `accent_green` fill |
| `bg_heatmap_medium.xml` | Shape | 4dp corners, 60% `accent_green` fill |
| `bg_heatmap_high.xml` | Shape | 4dp corners, 100% `accent_green` fill |
| `bg_avatar_circle.xml` | Oval shape | `accent_green_dark` fill for placeholder |
| `ic_eco_leaf.xml` | Vector | App logo — leaf/seedling shape |

### 1.7 Material Icons Needed (via Material Design Icons)

| Icon | Usage | MDI Name |
|------|-------|----------|
| Home tab | Bottom nav | `ic_home_24` |
| Log tab | Bottom nav | `ic_add_circle_24` |
| Board tab | Bottom nav | `ic_leaderboard_24` |
| Profile tab | Bottom nav | `ic_person_24` |
| Biking | Activity type | `ic_directions_bike_24` |
| Walking | Activity type | `ic_directions_walk_24` |
| Recycling | Activity type | `ic_recycling_24` |
| Water | Activity type | `ic_water_drop_24` |
| Energy | Activity type | `ic_bolt_24` |
| Plastic-Free | Activity type | `ic_eco_24` |
| Points/Star | Points display | `ic_star_24` |
| Streak/Fire | Streak display | `ic_local_fire_department_24` |
| Trophy | Achievements | `ic_emoji_events_24` |
| Search | Search bar | `ic_search_24` |
| Bell | Notifications | `ic_notifications_24` |
| Settings/Gear | Settings | `ic_settings_24` |
| Camera | Photo proof | `ic_camera_alt_24` |
| QR Code | QR scanner | `ic_qr_code_scanner_24` |
| Back arrow | Navigation | `ic_arrow_back_24` |
| Close | Dialogs/Sheets | `ic_close_24` |
| Check | Verification | `ic_check_circle_24` |
| Share | Badge share | `ic_share_24` |
| Edit | Edit profile | `ic_edit_24` |
| Delete | Delete account | `ic_delete_24` |
| Plus | Add/Create | `ic_add_24` |
| Minus | Quantity stepper | `ic_remove_24` |
| Calendar | Date display | `ic_calendar_today_24` |
| Team/Group | Teams | `ic_group_24` |
| Flag | Challenges | `ic_flag_24` |
| CO₂/Cloud | Carbon metric | `ic_cloud_24` |
| Timer | Time display | `ic_schedule_24` |

---

## 2. Screen-by-Screen Layout Specifications

### 2.1 Login Fragment (`fragment_login.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_void` (#050A07)

| Element | View Type | Positioning | Styles/Notes |
|---------|-----------|-------------|-------------|
| App logo | `ImageView` (leaf icon) | Center-horizontal, top 80dp | 64dp size, tint `accent_green`, subtle glow via elevation shadow |
| App title "EcoTrack" | `TextView` | Below logo, 16dp margin | `HeadlineLarge`, `text_primary` |
| Subtitle "Track your impact" | `TextView` | Below title, 4dp margin | `Body`, `text_muted` |
| Email input | `TextInputLayout` + `TextInputEditText` | Below subtitle, 48dp margin-top, 20dp horizontal padding | `Widget.Eco.TextInput`, icon start `ic_email`, hint "Email address" |
| Password input | `TextInputLayout` + `TextInputEditText` | Below email, 16dp margin | Same style, icon start `ic_lock`, `inputType="textPassword"`, password toggle end icon |
| Login button | `MaterialButton` | Below password, 24dp margin | `Widget.Eco.Button.Primary`, text "Sign In" |
| Register link | `TextView` | Below button, 16dp margin | "Don't have an account? **Create one**", `text_muted` with `text_accent` span for "Create one" |
| Loading indicator | `ProgressBar` | Center, GONE by default | Circular, `accent_green` tint, shown during auth |

**State handling:**
- Loading: Disable button, show progress indicator, hide button text
- Error: Show `TextInputLayout.error` on relevant field
- Success: Navigate to `HomeFragment`

---

### 2.2 Register Fragment (`fragment_register.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_void`

| Element | View Type | Notes |
|---------|-----------|-------|
| Title "Create Account" | `TextView` | `HeadlineLarge` |
| Subtitle | `TextView` | "Join the green movement", `text_muted` |
| Full Name input | `TextInputLayout` | Icon `ic_person`, hint "Full Name" |
| Email input | `TextInputLayout` | Icon `ic_email`, hint "University Email" |
| Department spinner | `TextInputLayout` with `AutoCompleteTextView` | Exposed dropdown menu, hint "Department" |
| Password input | `TextInputLayout` | Password toggle, hint "Password" |
| Register button | `MaterialButton` | `Widget.Eco.Button.Primary`, text "Create Account" |
| Login link | `TextView` | "Already have an account? **Sign In**" |

**Department dropdown items:** Computer Science, Environmental Sciences, Engineering, Business, Arts & Humanities, Social Sciences, Natural Sciences, Other

---

### 2.3 Home Fragment (`fragment_home.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

This is the most complex screen — the main landing page.

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | Greeting "Hello, {name} 👋" | `TextView` | `HeadlineMedium`, top of page |
| | Notification bell | `ImageButton` | Top-right, `ic_notifications`, badge count dot |
| **Eco Score Hero** | Eco-score ring | `EcoScoreRingView` (Custom) | 140dp, animated ring, score number center |
| | "Your Eco Score" label | `TextView` | `Label`, below ring |
| | Level text | `TextView` | "Level: Eco Warrior", `text_accent` |
| **Quick Stats Row** | Stats container | `LinearLayout` horizontal | 4 items, equally weighted |
| | Points stat | `TextView` pair | Icon + "2,450" + "points" |
| | Streak stat | `TextView` pair | 🔥 + "12" + "day streak" |
| | CO₂ stat | `TextView` pair | ☁️ + "8.4" + "kg saved" |
| | Activities stat | `TextView` pair | ✓ + "47" + "logged" |
| **Campus Impact Card** | Card container | `MaterialCardView` | `Widget.Eco.Card`, gradient left border (green to cyan) |
| | Title "Campus Impact" | `TextView` | `TitleMedium`, with leaf icon |
| | Stats grid | 2×2 `GridLayout` | CO₂ saved, Water saved, Waste diverted, Active students |
| | Each stat cell | `TextView` pair | Big number (`StatValue`) + label (`StatUnit`) |
| **Weekly Activity Chart** | Chart container | `BarChart` (MPAndroidChart) | 200dp height, 7 bars (Mon–Sun), `accent_green` bars |
| | Title "This Week" | `TextView` | `TitleMedium` with period selector |
| **Recent Activity List** | Section header | `TextView` | "Recent Activity", with "View All" link |
| | Activity items | `RecyclerView` | Vertical, 3 items visible, `RecentLogAdapter` |
| | Each item | `ConstraintLayout` | Activity icon (colored circle) + title + time + points |
| **Quick Actions** | Floating action strip | `LinearLayout` | "Log Activity" pill + "Scan QR" pill, bottom-anchored |

**Custom View — `EcoScoreRingView`:**
- Extends `View`
- Draws circular arc with `Paint` using `accent_green` color
- Background track: 10% white ring
- Animated sweep angle from 0 to target angle on data change
- Center text: score number (Syne Bold, 44sp) + "/ 100" (Jakarta, 12sp, muted)
- `setScore(int score)` method triggers `ValueAnimator`

---

### 2.4 Log Activity Fragment (`fragment_log_activity.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | Title "Log Activity" | `TextView` | `HeadlineLarge` |
| | Subtitle "What did you do?" | `TextView` | `Body`, `text_muted` |
| | QR scan button | `ImageButton` | Top-right, `ic_qr_code_scanner`, `accent_cyan` tint |
| **Activity Grid** | Grid container | `RecyclerView` | `GridLayoutManager(3)`, `ActivityCategoryAdapter` |
| | Each card | `MaterialCardView` | 100dp square, centered icon + label, colored icon per type |
| | Selected state | — | `bg_card_active` background, `accent_green` border, icon brightened |
| **Quantity Stepper** | Section (appears after selection) | `ConstraintLayout` | Animated slide-in via `TransitionManager` |
| | Title "How much?" | `TextView` | `TitleMedium` |
| | Minus button | `MaterialButton` icon-only | Circle, `ic_remove` |
| | Quantity display | `TextView` | `HeroNumber`, editable on tap |
| | Unit label | `TextView` | "km", "items", "L" — dynamic per type |
| | Plus button | `MaterialButton` icon-only | Circle, `ic_add` |
| **Impact Preview** | Preview card | `MaterialCardView` | `Widget.Eco.Card`, glass-morphism style |
| | "Impact Preview" header | `TextView` | `TitleMedium` |
| | CO₂ row | `TextView` pair | "🌿 X.XX kg CO₂ saved" |
| | Water row | `TextView` pair | "💧 X.X L water saved" |
| | Waste row | `TextView` pair | "♻️ X.XX kg waste diverted" |
| | Points row | `TextView` pair | "⭐ +XX points", `accent_amber` |
| **Photo Proof** | Optional section | `ConstraintLayout` | "Add photo proof (optional)" with camera icon |
| | Photo preview | `ImageView` | 100dp square, rounded, shown after capture |
| **Submit Button** | CTA | `MaterialButton` | `Widget.Eco.Button.Primary`, text "Log Activity" |
| | Daily count | `TextView` | "X of 20 daily logs used", `text_muted` |

**Interactions:**
- Tapping an activity card selects it (single selection from grid)
- Quantity stepper: ± buttons increment/decrement by 1, long-press for fast change
- Impact preview updates in real-time as quantity changes (debounced 300ms)
- Photo: Opens camera intent, shows preview thumbnail
- Submit: Shows success snackbar with confetti animation, navigates back or resets

---

### 2.5 QR Scanner Fragment (`fragment_qr_scanner.xml`)

**Root:** `FrameLayout`  
**Background:** Black (camera feed)

| Element | View Type | Notes |
|---------|-----------|-------|
| Camera preview | `PreviewView` (CameraX) | Full-screen |
| Viewfinder overlay | Custom `View` | 4 corner bracket lines (green), semi-transparent scrim outside |
| Scanning animation | `View` with anim | Horizontal green line moving up and down inside viewfinder |
| "Point at a QR code" text | `TextView` | Bottom of viewfinder, `text_secondary` |
| Close button | `ImageButton` | Top-left, `ic_close` |
| **Result overlay** (GONE → VISIBLE) | `MaterialCardView` | Slides up from bottom, `bg_card` |
| | Location name | `TextView` | `TitleMedium` |
| | Activity type + icon | `Chip` | Colored per type |
| | Impact summary | `TextView` | CO₂ saved preview |
| | Confirm button | `MaterialButton` | `Widget.Eco.Button.Primary`, text "Confirm Activity" |

---

### 2.6 Dashboard Fragment (`fragment_dashboard.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "My Dashboard" | `TextView` | `HeadlineLarge` |
| **Eco Score Ring** | Ring view | `EcoScoreRingView` | Same as Home, centered |
| **Impact Stats Row** | 3-column layout | `LinearLayout` horizontal | CO₂ saved, Water saved, Waste diverted |
| | Each stat | `ConstraintLayout` | Big number + label + icon |
| **Carbon Equivalencies** | Equivalency card | `MaterialCardView` | `Widget.Eco.Card` |
| | "You've..." header | `TextView` | `TitleMedium` |
| | Tree icon + "Planted X trees" | Row | `accent_green` icon |
| | Car icon + "X miles not driven" | Row | `accent_cyan` icon |
| | Lightbulb + "Powered X homes for a day" | Row | `accent_amber` icon |
| **Weekly Chart** | Bar chart | `BarChart` (MPAndroidChart) | 200dp, green bars, ghost bars for incomplete days |
| **Impact Heatmap** | Heatmap view | `HeatmapCalendarView` (Custom) | 5 rows × 7 columns of colored cells |
| | Legend | `LinearLayout` horizontal | "Less" → 4 color swatches → "More" |
| **Recent Logs** | Section header | `TextView` + "See All" | `TitleMedium` |
| | Log list | `RecyclerView` | 5 items, `RecentLogAdapter` |

**Custom View — `HeatmapCalendarView`:**
- Extends `View`
- Input: `Map<LocalDate, Integer>` (date → activity count)
- Renders 7-column × 5-row grid of rounded squares
- Day labels (M, T, W, ...) along top
- Color mapping: 0 → `bg_card`, 1–2 → low green, 3–5 → medium green, 6+ → bright green
- Cell size: 18dp with 3dp gap

---

### 2.7 Leaderboard Fragment (`fragment_leaderboard.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "Leaderboard" | `TextView` | `HeadlineLarge` |
| **Time Filter Tabs** | Chip group | `ChipGroup` | 3 chips: "This Week", "This Month", "All Time" — single-selection |
| **Campus Banner** | Stats strip | `LinearLayout` horizontal | Total CO₂, Active users, Activities — compact |
| **Podium (Top 3)** | Podium container | `ConstraintLayout` | Centered layout |
| | 2nd place (left) | `ConstraintLayout` | Avatar (52dp) + crown icon (silver) + name + points, shorter column |
| | 1st place (center) | `ConstraintLayout` | Avatar (64dp) + crown icon (gold) + name + points, tallest column |
| | 3rd place (right) | `ConstraintLayout` | Avatar (52dp) + crown icon (bronze) + name + points, shortest column |
| | Each podium column | Gradient bg | Subtle green gradient fading down |
| **Ranked List** | Section header | `TextView` | "Rankings" |
| | List | `RecyclerView` | `LeaderboardAdapter` |
| | Each row | `ConstraintLayout` | Rank # + avatar (32dp) + name + department tag + points + activity count |
| | Current user highlight | — | `accent_green_dim` background row for current user |
| **Your Rank Card** | Sticky card | `MaterialCardView` | Bottom of list or floating, shows "Your Rank: #X" with points |

---

### 2.8 Challenges Fragment (`fragment_challenges.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "Green Challenges" | `TextView` | `HeadlineLarge` |
| | Create button (admin) | `ImageButton` | `ic_add`, `accent_green`, visibility controlled by role |
| **Active Challenge Card** | Hero card | `MaterialCardView` | Larger card with gradient border |
| | Title | `TextView` | `HeadlineSmall` |
| | Progress ring | Custom progress | Circular or linear progress bar |
| | "X/Y completed" | `TextView` | Progress text |
| | Time remaining | `Chip` | "3 days left", `accent_amber` |
| **Available Challenges** | Section | `TextView` | "Available" |
| | Challenge list | `RecyclerView` | `ChallengeAdapter`, vertical |
| | Each card | `MaterialCardView` | Icon + title + participants count + points reward + "Join" button |
| **Completed Challenges** | Section | `TextView` | "Completed" |
| | Completed list | `RecyclerView` | Same adapter, "Completed ✓" badge instead of Join |

---

### 2.9 Challenge Detail Fragment (`fragment_challenge_detail.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Back button | `ImageButton` | Top-left, `ic_arrow_back` |
| Challenge icon | `ImageView` | Large, colored per activity type |
| Title | `TextView` | `HeadlineLarge` |
| Description | `TextView` | `Body`, multi-line |
| Stats row | `LinearLayout` horizontal | Participants count, Points reward, Days remaining |
| Progress section | `ConstraintLayout` | |
| Progress ring | Custom `ProgressBar` | Circular, `accent_green` |
| "X/Y" text | `TextView` | Center of ring |
| "Your Progress" label | `TextView` | Below ring |
| Join/Leave button | `MaterialButton` | Toggle between "Join Challenge" (primary) / "Leave" (outlined) |
| Participants list | `RecyclerView` | Avatar + name + progress bar, 5 visible |

---

### 2.10 Feed Fragment (`fragment_feed.xml`)

**Root:** `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "Campus Feed" | `TextView` | `HeadlineLarge` |
| | Search icon | `ImageButton` | Top-right |
| **Live Banner** | Activity counter | `TextView` | Sliding banner: "🌿 142 activities logged today", `bg_glass_card` |
| **Feed List** | `RecyclerView` | `SwipeRefreshLayout` wrapping `RecyclerView` | `FeedItemAdapter`, vertical |
| **Each Feed Item** | Card | `MaterialCardView` | `Widget.Eco.Card` |
| | Avatar | `ImageView` | 40dp circle, or generic leaf if anonymous |
| | Name / "Anonymous Hero" | `TextView` | `TitleMedium`, or "Anonymous Hero" if anonymous |
| | Department tag | `Chip` | Small, `text_muted` |
| | Timestamp | `TextView` | "2h ago", relative time, `text_muted` |
| | Activity description | `TextView` | "Biked 5.2 km to campus" |
| | Impact stat | `Chip` | "🌿 1.1 kg CO₂ saved", green tinted |
| | Points earned | `TextView` | "+78 pts", `accent_amber` |
| | Reaction row | `LinearLayout` horizontal | Emoji buttons: 🌱(count) 💚(count) 🎉(count) 👏(count) |
| | Reply section | Expandable | 1-level deep, shows reply input + existing replies |

---

### 2.11 Profile Fragment (`fragment_profile.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | Settings gear | `ImageButton` | Top-right, navigates to `EditProfileFragment` |
| **Profile Hero** | Avatar | `ImageView` | 80dp circle, Glide loaded |
| | Display name | `TextView` | `HeadlineMedium` |
| | Department | `Chip` | Below name, `text_muted` |
| | Joined date | `TextView` | "Member since Oct 2024", `text_muted` |
| **Stats Row** | 3-column | `LinearLayout` | Points, Eco-Score, Activities logged |
| **Streak Card** | Card | `MaterialCardView` | 🔥 streak count + "Day Streak" + progress to next milestone |
| | Progress bar | `LinearLayout` | Custom segmented bar: "7 days" / "30 days" markers |
| | Multiplier badge | `Chip` | "1.5× multiplier" if streak ≥7, `accent_amber` |
| **Impact Breakdown** | Card | `MaterialCardView` | |
| | Title "Environmental Impact" | `TextView` | `TitleMedium` |
| | CO₂ row | `ConstraintLayout` | Icon + label + value, `accent_green` |
| | Water row | `ConstraintLayout` | Icon + label + value, `accent_cyan` |
| | Waste row | `ConstraintLayout` | Icon + label + value, `accent_amber` |
| **Badges Grid** | Section title "Achievements" | `TextView` | `TitleMedium` |
| | Badge grid | `RecyclerView` | `GridLayoutManager(4)`, `BadgeGridAdapter` |
| | Each badge | `ConstraintLayout` | 80dp, icon + name, dim if locked, bright if earned |
| | Locked overlay | `ImageView` | Lock icon overlay for unearned badges |
| **Logout** | Button | `MaterialButton` | `Widget.Eco.Button.Outlined`, text "Sign Out", bottom |

---

### 2.12 Edit Profile Fragment (`fragment_edit_profile.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Back button | `ImageButton` | `ic_arrow_back` |
| Title "Edit Profile" | `TextView` | `HeadlineLarge` |
| Avatar with edit overlay | `FrameLayout` > `ImageView` + camera icon | 80dp circle, tap opens gallery/camera picker |
| Full Name | `TextInputLayout` | Pre-filled |
| Display Name | `TextInputLayout` | Pre-filled |
| Department | `TextInputLayout` dropdown | Pre-selected |
| **Privacy Section** | | |
| "Anonymous contributions" | `SwitchMaterial` | Toggle anonymous mode on feed |
| "Show on leaderboard" | `SwitchMaterial` | Toggle leaderboard visibility |
| Save button | `MaterialButton` | `Widget.Eco.Button.Primary` |
| **Danger Zone** | Bordered section | Red-tinted card |
| Delete account | `MaterialButton` | Red tint, "Delete Account" — shows confirmation dialog |

---

### 2.13 Notification Settings Fragment (`fragment_notification_settings.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Title "Notifications" | `TextView` | `HeadlineLarge` |
| Daily Reminder toggle | `SwitchMaterial` | With time picker row (hour ± buttons) |
| Reminder time | `TextView` + stepper | Shows "8:00 AM", ± buttons |
| Challenge Updates toggle | `SwitchMaterial` | |
| Streak Alerts toggle | `SwitchMaterial` | |
| Campus Milestones toggle | `SwitchMaterial` | |
| Badge Unlocks toggle | `SwitchMaterial` | |
| Save button | `MaterialButton` | `Widget.Eco.Button.Primary` |

---

### 2.14 Badge Detail Fragment (`fragment_badge_detail.xml`)

**Root:** `ConstraintLayout` (could be BottomSheetDialogFragment)  
**Background:** `bg_card`

| Element | View Type | Notes |
|---------|-----------|-------|
| Badge icon | `ImageView` | 64dp, colored per badge type |
| Badge name | `TextView` | `HeadlineMedium` |
| Rarity tag | `Chip` | "Rare", "Epic", "Legendary" etc., color-coded |
| Description | `TextView` | `Body`, multi-line |
| Progress bar | `LinearProgressIndicator` | If not yet unlocked, shows X/Y |
| Date earned | `TextView` | "Earned: Nov 15, 2024" or "Not yet earned" |
| Points value | `TextView` | "Worth 500 points" |
| Share button | `MaterialButton` | Outlined, `ic_share` |
| Close button | `MaterialButton` | Primary, "Close" |

---

### 2.15 Team List Fragment (`fragment_team_list.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "Teams" | `TextView` | `HeadlineLarge` |
| | Create team button | `FloatingActionButton` | `ic_add`, `accent_green` |
| **My Team Card** | Hero card | `MaterialCardView` | If user has a team — initials circle + name + rank + points |
| **Filter Pills** | `ChipGroup` | Horizontal scroll | "All", "Clubs", "Departments" — single selection |
| **Team List** | `RecyclerView` | `TeamAdapter` | Vertical list |
| | Each card | `MaterialCardView` | Colored left border, initials circle + name + type tag + member count + points |
| **Create CTA** | Bottom card | `MaterialCardView` | "Start Your Own Team" + description + button |

---

### 2.16 Team Detail Fragment (`fragment_team_detail.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Hero** | Team initials circle | `TextView` in circle bg | Large, colored gradient |
| | Team name | `TextView` | `HeadlineLarge` |
| | Type badge | `Chip` | "Club" / "Department" |
| **Stats Row** | 3 stats | `LinearLayout` | Rank, Total Points, Members |
| **Points Strip** | This week banner | `MaterialCardView` | "+1,250 pts this week", `accent_green` bg |
| **Leave Button** | `MaterialButton` | Outlined red, "Leave Team" |
| **Members** | Section title | `TextView` | "Members (X)" |
| | Member list | `RecyclerView` | `TeamMemberAdapter`, top 5 with rank, avatar, name, points |
| **Activity Feed** | Section title | `TextView` | "Team Activity" |
| | Activity list | `RecyclerView` | `TeamActivityAdapter`, timeline-style feed items |

---

### 2.17 Create Team Fragment (`fragment_create_team.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Title "Create Team" | `TextView` | `HeadlineLarge` |
| Avatar with initials | `TextView` in circle | Updates initials live from name input |
| Color picker | 4 `View` circles | Gradient colors, select one for team avatar bg |
| Team Name | `TextInputLayout` | Required |
| Type pills | `ChipGroup` | "Department", "Club", "Other" — single selection |
| Description | `TextInputLayout` | Multi-line, optional |
| Public toggle | `SwitchMaterial` | "Allow anyone to join" |
| Create button | `MaterialButton` | `Widget.Eco.Button.Primary`, "Create Team" |

---

### 2.18 Create Challenge Fragment (`fragment_create_challenge.xml`)

**Root:** `ScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Title "Create Challenge" | `TextView` | `HeadlineLarge` |
| Challenge Title | `TextInputLayout` | Required |
| Description | `TextInputLayout` | Multi-line, required |
| Activity Type grid | `RecyclerView` | `GridLayoutManager(3)`, 2 rows, same as Log screen |
| Goal quantity | Stepper | Label + minus + number + unit + plus |
| Start Date | `TextInputLayout` | Opens `MaterialDatePicker` on tap |
| End Date | `TextInputLayout` | Opens `MaterialDatePicker` on tap |
| Team-based toggle | `SwitchMaterial` | |
| Anonymous toggle | `SwitchMaterial` | |
| Reward preview | `MaterialCardView` | Shows computed point reward |
| Publish button | `MaterialButton` | Primary, "Publish Challenge" |
| Save Draft button | `MaterialButton` | Outlined, "Save as Draft" |

---

### 2.19 Search Fragment (`fragment_search.xml`)

**Root:** `ConstraintLayout`  
**Background:** `bg_deep`

| Element | View Type | Notes |
|---------|-----------|-------|
| Search bar | `TextInputLayout` | `Widget.Eco.TextInput`, search icon start, clear icon end |
| Filter chips | `ChipGroup` | "All", "Activities", "Challenges", "Teams" — single selection |
| Results list | `RecyclerView` | `SearchResultAdapter`, mixed item types |
| Empty state | `ConstraintLayout` | Shown when no results — illustration + "No results found" |
| Loading | `ProgressBar` | Circular, centered, shown during search |

---

### 2.20 Admin Analytics Fragment (`fragment_admin_analytics.xml`)

**Root:** `NestedScrollView` > `ConstraintLayout`  
**Background:** `bg_deep`

| Section | Element | View Type | Notes |
|---------|---------|-----------|-------|
| **Header** | "Admin Panel" + admin badge | `TextView` + `Chip` | `HeadlineLarge`, admin pill "Admin" violet |
| **Campus Stats Grid** | 2×2 `GridLayout` | `MaterialCardView` each | CO₂ saved, Water saved, Waste diverted, Total activities |
| **Weekly Chart** | `BarChart` | MPAndroidChart | 7 bars, green, titled "Weekly Activity" |
| **Photo Proof Queue** | Section title | `TextView` | "Pending Reviews" |
| | Queue list | `RecyclerView` | `ProofQueueAdapter` — user name + activity + thumbnail + approve/reject buttons |
| **Conversion Factors** | Section title | `TextView` | "Conversion Factors" |
| | Factor table | `RecyclerView` | `ConversionFactorAdapter` — type + CO₂ + water + waste + points per unit |
| **Top Students** | Section title | `TextView` | "Top Contributors" |
| | Student list | `RecyclerView` | Rank + avatar + name + points |
| **Quick Actions** | 2×2 grid | `GridLayout` | 4 buttons: New Challenge, Export CSV, Sync Factors, User Audit |

---

## 3. RecyclerView Adapter Specifications

### 3.1 `ActivityCategoryAdapter`
- **Item layout:** `item_activity_category.xml`
- **Layout:** `MaterialCardView` (square) > `LinearLayout` vertical (centered)
  - `ImageView` — 32dp icon, tinted per activity color
  - `TextView` — activity name, `Label` style
- **Selection:** Single-selection mode. Track `selectedPosition`. Selected item gets `bg_card_active` + green border. Previous selection cleared.
- **Data class:** `ActivityCategory(type: String, label: String, iconRes: Int, colorRes: Int)`

### 3.2 `LeaderboardAdapter`
- **Item layout:** `item_leaderboard_entry.xml`
- **Layout:** `ConstraintLayout`
  - `TextView` — rank number, `TitleMedium`, fixed 36dp width
  - `ImageView` — avatar circle, 32dp
  - `LinearLayout` vertical — name (`TitleMedium`) + department (`Label`, `text_muted`)
  - `LinearLayout` vertical (end) — points (`StatValue`) + activities count (`Label`)
- **Highlight:** If entry.userId == currentUserId, set bg to `accent_green_dim`
- **Pagination:** Implement `RecyclerView.OnScrollListener` for infinite scroll

### 3.3 `FeedItemAdapter`
- **Item layout:** `item_feed_activity.xml`
- **Layout:** `MaterialCardView` > `ConstraintLayout`
  - Header row: avatar (40dp) + name + department chip + timestamp
  - Body: activity description text
  - Impact: green chip "🌿 X.X kg CO₂" + points text
  - Reactions row: 4 emoji TextViews with counts, clickable
- **Anonymous handling:** If `feedItem.anonymous`, show leaf icon avatar + "Anonymous Hero"
- **Reaction click:** Callback to ViewModel to increment reaction count

### 3.4 `ChallengeAdapter`
- **Item layout:** `item_challenge.xml`
- **Layout:** `MaterialCardView` > `ConstraintLayout`
  - Icon (left) — activity type, colored circle
  - Title + description snippet
  - Bottom row: participants icon + count, points chip, "Join"/"Joined" button
- **State:** Active (green glow border), Available (default), Completed (checkmark overlay)

### 3.5 `BadgeGridAdapter`
- **Item layout:** `item_badge.xml`
- **Layout:** `ConstraintLayout` (80dp × 80dp)
  - `ImageView` — badge icon, 48dp
  - `TextView` — badge name, `LabelSmall`, centered
  - Lock overlay — `ImageView` with lock icon, visible if not earned
- **Interaction:** Tap navigates to `BadgeDetailFragment` with badgeType arg
- **State:** Earned = full color + glow. Locked = 40% opacity + lock icon.

---

## 4. Animation & Motion Specifications

### 4.1 Shared Transitions
- **Screen transitions:** Use Navigation Component default `FragmentNavigatorExtras` with `MaterialContainerTransform` for card → detail transitions (challenge card → challenge detail, team card → team detail).
- **Bottom nav transitions:** Fade + slide (direction based on tab index).

### 4.2 Micro-Animations

| Animation | Target | Type | Duration |
|-----------|--------|------|----------|
| Eco-score ring fill | `EcoScoreRingView` | `ValueAnimator` sweep angle | 1000ms, AccelerateDecelerateInterpolator |
| Heatmap cell appear | `HeatmapCalendarView` | Sequential alpha 0→1 per cell | 50ms stagger |
| Activity card selection | `ActivityCategoryAdapter` item | Scale 1.0→0.95→1.0 + border color | 200ms |
| Quantity change | Quantity `TextView` | Scale bounce 1.0→1.2→1.0 | 150ms |
| Feed item enter | `FeedItemAdapter` items | Slide up + fade in | 300ms, staggered 50ms |
| Badge unlock | `BadgeGridAdapter` item | Scale 0→1.1→1.0 + particle burst | 500ms |
| Points earned | Points text overlay | Float up + fade out | 800ms |
| Podium entrance | Leaderboard podium | Slide up from bottom, staggered | 3rd→2nd→1st, 200ms each |
| Progress bar fill | `LinearProgressIndicator` | Width animation | 600ms |
| Tab switch content | Screen content | CrossFade | 200ms |

### 4.3 Loading States

Every screen that fetches data should implement these states:
1. **Loading:** `ShimmerFrameLayout` (Facebook shimmer) or custom placeholder with pulsing `bg_card` rectangles
2. **Content:** Normal layout visible
3. **Empty:** Centered illustration + "No data yet" message + CTA
4. **Error:** Centered error icon + message + "Retry" button

Manage via a `sealed class ViewState<T> { Loading, Success(data), Empty, Error(message) }` pattern in each ViewModel.

---

## 5. Responsive Design Notes

- **Content max width:** 420dp — content centered on tablets
- **Grid columns:** Activity grid = 3 columns. Badge grid = 4 columns. Admin stats = 2 columns.
- **ScrollView:** All main screens use `NestedScrollView` to accommodate varying content heights
- **Keyboard handling:** `android:windowSoftInputMode="adjustResize"` on Activity; `ScrollView` ensures inputs remain visible
- **Orientation:** Lock to portrait (`android:screenOrientation="portrait"`)

---

## 6. Accessibility Checklist

- All `ImageView`s must have `contentDescription`
- All clickable elements must have `android:minHeight="48dp"` touch target
- Color contrast ratios: `accent_green` on `bg_deep` ≈ 8:1 ✓, `text_primary` on `bg_card` ≈ 15:1 ✓
- Dynamic type: Use `sp` units for all text sizes
- Screen reader: Announce score/rank changes with `announceForAccessibility()`
- All form inputs must have visible labels (not just hints)

---

*This UI guide is the authoritative reference for translating the HTML mockups into Android XML layouts and custom views. Every color value, dimension, style, and component pattern specified here should be followed exactly for visual consistency.*
