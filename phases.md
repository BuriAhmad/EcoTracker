# EcoTrack — Phased Implementation Plan

---

## Overview

This document breaks the EcoTrack Android application into **8 sequential phases**. Each phase is self-contained, testable, and builds on the previous one. A developer should complete each phase fully — including tests and verification — before moving to the next.

**Estimated total effort:** ~12 sprints (6 weeks at 2 sprints/week)

---

## Phase 1: Project Scaffold & Firebase Setup

### Goal
Create the Android project structure, configure all Gradle dependencies, set up Firebase, implement the single-Activity architecture with Navigation Component, and establish the design system resources.

### Scope
- Android project with package `com.ecotrack.app`
- All sub-packages created (empty classes can be stubs)
- Gradle files with all dependencies
- Firebase project connected (`google-services.json`)
- `MainActivity` with `NavHostFragment` and `BottomNavigationView`
- Navigation graph (`nav_graph.xml`) with all destinations declared (fragments can be empty)
- Full resource files: `colors.xml`, `dimens.xml`, `themes.xml`, `styles.xml`, font files
- ViewBinding enabled

### Implementation Steps
1. Create new Android project (Empty Activity template) with package `com.ecotrack.app`
2. Create all sub-packages under `com.ecotrack.app`:
   - `model/`, `controller/`, `repository/`, `viewmodel/`
   - `view/auth/`, `view/home/`, `view/logging/`, `view/dashboard/`, `view/leaderboard/`, `view/challenges/`, `view/social/`, `view/teams/`, `view/profile/`, `view/admin/`, `view/search/`
   - `util/`
3. Configure project-level `build.gradle` (plugins: android, google-services, safe-args)
4. Configure app-level `build.gradle` with ALL dependencies listed in `implementation_plan.md` §7
5. Add `settings.gradle` with JitPack repository
6. Create Firebase project → download `google-services.json` → place in `app/`
7. Enable Firebase Auth (email/password), Firestore, Storage, Cloud Messaging in Firebase Console
8. Copy `colors.xml` from `ui_guide.md` §1.1 into `res/values/`
9. Copy `dimens.xml` from `ui_guide.md` §1.3
10. Create `themes.xml` from `ui_guide.md` §1.4
11. Create widget styles from `ui_guide.md` §1.5
12. Download and place font files in `res/font/`, create font family XMLs
13. Download all Material icons listed in `ui_guide.md` §1.7 into `res/drawable/`
14. Create all shape drawables listed in `ui_guide.md` §1.6
15. Implement `MainActivity`:
    - `activity_main.xml`: `ConstraintLayout` with `FragmentContainerView` (NavHostFragment) + `BottomNavigationView`
    - Wire BottomNav to NavController using `NavigationUI.setupWithNavController()`
    - Bottom nav menu: Home, Log, Board, Profile (4 items)
16. Create `nav_graph.xml` with all fragment destinations (use placeholder empty fragments)
17. Create all Fragment classes as empty stubs extending `Fragment` with inflated empty layouts
18. Create `Constants.java` in `util/` with all collection names and SharedPreferences keys
19. Build and verify: App compiles, launches, bottom nav switches between 4 empty fragments

### Tests
- **Build test:** Project compiles without errors
- **Navigation test (manual):** All 4 bottom nav tabs switch correctly
- **Resource test:** `ContextCompat.getColor(context, R.color.accent_green)` returns correct value

### Acceptance Criteria
- [ ] App launches on emulator/device without crash
- [ ] Bottom navigation shows 4 tabs and switches between fragments
- [ ] All resource files are in place and compile
- [ ] Firebase is connected (no crash on Firestore access attempt)
- [ ] All 20+ fragment stubs exist and are reachable via nav graph

---

## Phase 2: Authentication (Login + Register + User Model)

### Goal
Implement complete user authentication flow: registration, login, logout, and user profile creation in Firestore.

### Scope
- `User` model class
- `UserRepository` (Firebase Auth + Firestore user document)
- `UserController` (register, login, logout, profile creation)
- `AuthViewModel` (UI state management)
- `LoginFragment` (full UI)
- `RegisterFragment` (full UI)
- `ValidationUtils` utility
- Auth state observation in `MainActivity` (redirect to login if not authenticated)

### Dependencies
- Phase 1 complete (project structure, Firebase setup)

### Implementation Steps
1. Implement `User.java` model — POJO with all fields from `implementation_plan.md` §4.1, Firestore annotations (`@DocumentId`, `@ServerTimestamp` where applicable), no-arg constructor for Firestore
2. Implement `ValidationUtils.java`:
   - `isValidEmail(String)` — regex check
   - `isValidPassword(String)` — min 6 chars
   - `isNotEmpty(String)` — null/blank check
3. Implement `UserRepository.java`:
   - `createUserWithEmailAndPassword(email, password)` → `Task<AuthResult>`
   - `signInWithEmailAndPassword(email, password)` → `Task<AuthResult>`
   - `signOut()`
   - `getCurrentUser()` → `FirebaseUser`
   - `isLoggedIn()` → boolean
   - `saveUserDocument(User)` → `Task<Void>` (writes to `users/{uid}`)
   - `getUserDocument(userId)` → `Task<DocumentSnapshot>`
4. Implement `UserController.java`:
   - `register(name, email, department, password, callback)` — validate → create auth → create Firestore doc → callback
   - `login(email, password, callback)` — validate → sign in → callback
   - `logout()` — sign out
   - `getCurrentUserId()` → String
   - `isLoggedIn()` → boolean
   - Interface: `AuthCallback { onSuccess(User); onError(String); }`
5. Implement `AuthViewModel.java`:
   - `MutableLiveData<ViewState<User>> loginState`
   - `MutableLiveData<ViewState<User>> registerState`
   - `MutableLiveData<String> errorMessage`
   - `login(email, password)` — calls UserController, posts to loginState
   - `register(name, email, department, password)` — calls UserController, posts to registerState
6. Build `fragment_login.xml` per `ui_guide.md` §2.1
7. Implement `LoginFragment.java`:
   - Bind views with ViewBinding
   - Observe `loginState` LiveData
   - Handle loading/error/success states
   - On success: navigate to HomeFragment
   - On error: show error on TextInputLayout
8. Build `fragment_register.xml` per `ui_guide.md` §2.2
9. Implement `RegisterFragment.java`:
   - Department dropdown with ArrayAdapter
   - Observe `registerState`
   - On success: navigate to HomeFragment
10. Update `MainActivity`:
    - Check `FirebaseAuth.getCurrentUser()` on launch
    - If null → navigate to LoginFragment, hide bottom nav
    - If authenticated → show bottom nav, navigate to HomeFragment
    - Listen to auth state changes with `FirebaseAuth.AuthStateListener`
11. Wire navigation: Login ↔ Register transitions in nav_graph

### Tests
- **Unit tests:**
  - `ValidationUtilsTest`: email validation (valid, invalid, empty), password validation (short, valid)
  - `UserControllerTest` (with mocked `UserRepository`): register success, register with invalid email, login success, login with wrong password
- **Integration test (manual/Espresso):**
  - Register new user → verify Firestore document created
  - Login with registered user → lands on Home
  - Logout → redirected to Login
  - Login with wrong password → shows error

### Acceptance Criteria
- [ ] New user can register with name, email, department, password
- [ ] Registered user can log in
- [ ] Invalid inputs show appropriate error messages
- [ ] User document created in Firestore with correct fields
- [ ] App redirects to login when not authenticated
- [ ] Logout works and returns to login screen
- [ ] Bottom nav is hidden on auth screens, visible on main screens

---

## Phase 3: Activity Logging + Impact Calculation + Conversion Factors

### Goal
Implement the core activity logging workflow: select activity type, enter quantity, preview impact, submit log. This is the heart of the app.

### Scope
- `ActivityLog` model, `ConversionFactor` model
- `ActivityRepository` (CRUD for logs, read conversion factors, batch writes, campus stats increment)
- `ImpactCalculator` utility
- `ActivityController` (orchestrate full logging flow)
- `ActivityViewModel` (UI state for log screen)
- `LogActivityFragment` (full UI with grid, stepper, impact preview)
- Seed data: conversion factors in Firestore
- Update `CampusStats` model + aggregate document

### Dependencies
- Phase 2 complete (auth + user model)

### Implementation Steps
1. Implement `ConversionFactor.java` model — all fields per schema
2. Implement `ActivityLog.java` model — all fields per schema
3. Implement `CampusStats.java` model — all fields per schema
4. Seed Firestore with conversion factors from `implementation_plan.md` §8 (use a one-time seeder script or manual Firebase Console entry)
5. Create `campusStats/aggregate` document with all zeros
6. Implement `ActivityRepository.java`:
   - `getConversionFactor(activityType)` → `Task<ConversionFactor>`
   - `getAllConversionFactors()` → `Task<List<ConversionFactor>>`
   - `saveActivityLog(userId, ActivityLog)` → `Task<Void>` (subcollection write)
   - `getActivityLogs(userId, startDate, endDate)` → `Task<List<ActivityLog>>`
   - `getDailyLogCount(userId, date)` → `Task<Integer>`
   - `incrementCampusStats(co2, water, waste)` → uses `FieldValue.increment()`
   - `updateUserTotals(userId, points, co2, water, waste)` → uses `FieldValue.increment()`
7. Implement `ImpactCalculator.java`:
   - `calculateImpact(double quantity, ConversionFactor factor)` → `ImpactResult`
   - Inner class `ImpactResult { double co2Saved, waterSaved, wasteDiverted; int pointsEarned; }`
8. Implement `ActivityController.java`:
   - `logActivity(String activityType, double quantity, @Nullable Uri photoUri, LogCallback callback)`:
     1. Check daily log count < 20
     2. Fetch ConversionFactor for type
     3. Calculate impact via ImpactCalculator
     4. Build ActivityLog object
     5. Execute Firestore batch write: save log + increment user totals + increment campus stats
     6. Post to social feed via FeedController (or skip for now, do in Phase 6)
     7. Evaluate streak (Phase 4)
     8. Evaluate badges (Phase 5)
     9. Callback success with ActivityLog
   - `getConversionFactors(callback)` — fetch all factors for display
   - `getImpactPreview(activityType, quantity, callback)` — real-time preview without saving
9. Implement `ActivityViewModel.java`:
   - `MutableLiveData<String> selectedActivityType`
   - `MutableLiveData<Double> quantity`
   - `MutableLiveData<ImpactResult> impactPreview`
   - `MutableLiveData<ViewState<ActivityLog>> logResult`
   - `MutableLiveData<Integer> todayLogCount`
   - `MutableLiveData<List<ConversionFactor>> conversionFactors`
   - `selectActivity(type)` — sets type, triggers preview update
   - `setQuantity(qty)` — triggers preview update (debounced)
   - `submitLog()` — calls ActivityController
   - `loadConversionFactors()` — fetches from controller
10. Build `item_activity_category.xml` per `ui_guide.md` §3.1
11. Implement `ActivityCategoryAdapter.java` with single-selection logic
12. Build `fragment_log_activity.xml` per `ui_guide.md` §2.4
13. Implement `LogActivityFragment.java`:
    - Setup RecyclerView grid (3 columns)
    - Handle card selection → show quantity section
    - Quantity stepper: ±1 on tap, ±5 on long-press
    - Observe impactPreview → update preview card
    - Submit button → observe logResult → success snackbar
    - Show daily count "X of 20"
14. Handle empty/loading states

### Tests
- **Unit tests:**
  - `ImpactCalculatorTest`: calculate with known factors, zero quantity, edge cases
  - `ActivityControllerTest` (mocked repo): successful log, daily limit exceeded, invalid quantity
- **Integration test:**
  - Log a biking activity → verify ActivityLog in Firestore subcollection
  - Verify user points incremented
  - Verify campusStats incremented
  - Verify daily count enforcement at 20

### Acceptance Criteria
- [ ] 6 activity types displayed in a 3×2 grid
- [ ] Selecting a type highlights it and shows quantity stepper
- [ ] Impact preview updates in real-time as quantity changes
- [ ] Submitting creates ActivityLog in Firestore
- [ ] User totalPoints and metric fields are incremented
- [ ] campusStats aggregate is incremented
- [ ] Max 20 logs/day enforced with appropriate message
- [ ] Success feedback shown after log

---

## Phase 4: Streak Engine + Personal Dashboard + Eco-Score

### Goal
Implement the streak tracking system, eco-score calculation, personal dashboard with charts and heatmap, and carbon equivalencies.

### Scope
- `StreakManager` utility
- `EcoScoreCalculator` utility
- `EquivalencyTranslator` utility
- `DateUtils` utility
- `DashboardViewModel`
- `DashboardFragment` (full UI with charts and heatmap)
- `EcoScoreRingView` custom view
- `HeatmapCalendarView` custom view
- Integrate streak evaluation into `ActivityController.logActivity()`
- Update `HomeFragment` with live data (eco-score, stats, streak)
- `HomeViewModel`

### Dependencies
- Phase 3 complete (activity logging provides data to display)

### Implementation Steps
1. Implement `DateUtils.java`:
   - `isConsecutiveDay(Date d1, Date d2)` → boolean
   - `isToday(Date d)` → boolean
   - `formatRelativeTime(Date timestamp)` → "2h ago", "Yesterday", etc.
   - `getStartOfWeek()` → Date (Monday)
   - `getStartOfMonth()` → Date
   - `getDaysBetween(Date start, Date end)` → int
2. Implement `StreakManager.java`:
   - `evaluateStreak(Date lastLogDate, Date currentDate, int currentStreak)` → `StreakResult`
   - `StreakResult { int newStreak; double multiplier; boolean streakBroken; }`
   - Logic: If lastLogDate is yesterday → streak++; if today → no change; otherwise → reset to 1
   - Multiplier: streak ≥ 30 → 2.0×; streak ≥ 7 → 1.5×; else → 1.0×
3. Integrate streak into `ActivityController.logActivity()`:
   - After saving log, call `StreakManager.evaluateStreak()`
   - Update user document: `currentStreak`, `lastLogDate`
   - Apply multiplier to points earned
4. Implement `EcoScoreCalculator.java`:
   - `calculateEcoScore(double co2Total, double wasteTotal, double waterTotal, int streak)` → int (0–100)
   - Normalization: co2_norm = min(co2Total / MAX_CO2, 1.0), similar for others
   - MAX values: CO₂ = 500kg, Waste = 200kg, Water = 5000L, Streak = 60 days
   - Formula: `score = (int)((0.4 * co2_norm + 0.25 * waste_norm + 0.2 * water_norm + 0.15 * streak_norm) * 100)`
5. Implement `EquivalencyTranslator.java`:
   - `translate(double co2Kg)` → `List<Equivalency>`
   - Equivalencies: 1 tree absorbs ~22kg CO₂/year, 1 gallon gas = 8.89kg CO₂, avg home = 30 kWh/day
   - `Equivalency { String icon; String description; double value; }` — e.g., "🌳 Planted 2.4 trees"
6. Implement `EcoScoreRingView.java`:
   - Custom `View` that draws animated circular progress arc
   - `setScore(int score)` — animates from current to target
   - Canvas drawing: background track (10% white ring) + foreground arc (`accent_green`)
   - Center text: score number + "/ 100"
7. Implement `HeatmapCalendarView.java`:
   - Custom `View` with 7×5 grid of rounded squares
   - `setData(Map<LocalDate, Integer> dailyCounts)`
   - Color mapping per `ui_guide.md` §2.6
   - Day-of-week labels along top
8. Implement `DashboardViewModel.java` — all LiveData fields per `implementation_plan.md` §4.4
9. Build `fragment_dashboard.xml` per `ui_guide.md` §2.6:
   - Eco-score ring at top
   - 3-column impact stats
   - Carbon equivalencies card
   - Weekly bar chart (MPAndroidChart)
   - Heatmap calendar
   - Recent logs RecyclerView
10. Implement `DashboardFragment.java`:
    - Configure MPAndroidChart: dark theme, green bars, rounded corners, no grid
    - Populate heatmap from activity log dates
    - Load recent logs list
11. Implement `HomeViewModel.java` — campus stats, user streak, weekly points, eco-score
12. Update `HomeFragment.java`:
    - Display eco-score ring
    - Quick stats row (points, streak, CO₂, activities)
    - Campus impact card (from campusStats document)
    - Weekly chart
    - Recent activity list
    - "Log Activity" and "Scan QR" quick action buttons
13. Implement `RecentLogAdapter` for dashboard/home recent log lists

### Tests
- **Unit tests:**
  - `StreakManagerTest`: consecutive day → increment, same day → no change, gap → reset, multiplier thresholds
  - `EcoScoreCalculatorTest`: zero inputs → 0, max inputs → 100, partial inputs → expected range
  - `EquivalencyTranslatorTest`: known CO₂ values produce correct equivalencies
  - `DateUtilsTest`: consecutive days, relative time formatting
- **UI test:**
  - Dashboard displays correct eco-score after logging activities
  - Heatmap shows colored cells for days with logs
  - Chart shows correct bar heights per day

### Acceptance Criteria
- [ ] Streak increments on consecutive daily logs
- [ ] Streak resets after a missed day
- [ ] Multiplier correctly applied at 7-day and 30-day thresholds
- [ ] Eco-score ring animates and shows correct value
- [ ] Dashboard shows CO₂, water, waste totals
- [ ] Equivalency card shows relatable analogies
- [ ] Weekly chart renders with MPAndroidChart
- [ ] Heatmap renders activity density
- [ ] Home screen shows campus-wide stats from aggregate
- [ ] Recent activity list populates from Firestore

---

## Phase 5: Badges + Leaderboard + Profile

### Goal
Implement the achievement badge system, the competitive leaderboard with podium, and the user profile page with stats, streak display, and badge grid.

### Scope
- `Badge` model, `BadgeDefinition` model
- `BadgeEvaluator` utility
- Badge evaluation integrated into `ActivityController.logActivity()`
- Seed badge definitions in Firestore
- `LeaderboardRepository`, `LeaderboardController`, `LeaderboardViewModel`
- `LeaderboardFragment` (full UI with podium + ranked list)
- `LeaderboardAdapter`
- `ProfileViewModel`
- `ProfileFragment` (full UI)
- `BadgeGridAdapter`
- `BadgeDetailFragment`

### Dependencies
- Phase 4 complete (streak, eco-score provide data for badges/profile)

### Implementation Steps
1. Implement `Badge.java` model
2. Implement `BadgeDefinition.java` model
3. Seed `badgeDefinitions` collection in Firestore per `implementation_plan.md` §8
4. Implement `BadgeEvaluator.java`:
   - `evaluateNewBadges(User user, List<BadgeDefinition> definitions, List<Badge> existingBadges)` → `List<Badge>`
   - Compare user metrics against each definition threshold
   - Return only newly earned badges (not already in existingBadges)
5. Integrate into `ActivityController.logActivity()`:
   - After updating user totals, fetch badge definitions and existing badges
   - Run `BadgeEvaluator.evaluateNewBadges()`
   - Save any new badges to `users/{userId}/badges/` subcollection
   - Return badge info in callback for UI notification
6. Implement `LeaderboardRepository.java`:
   - `getUsersRankedByPoints(String timePeriod, int limit, DocumentSnapshot startAfter)` → paginated query
   - Time filtering: "week" → `createdAt >= startOfWeek` (or use a separate weekly points field); "month" → startOfMonth; "all" → no filter
   - Note: Firestore doesn't support complex time-windowed aggregation natively. Options: (a) use `totalPoints` for "All Time", and maintain `weeklyPoints`/`monthlyPoints` fields updated via cloud functions or client-side; (b) for MVP, use `totalPoints` for all periods
   - `getUserRank(userId)` → query count of users with more points
7. Implement `LeaderboardController.java`:
   - `getLeaderboard(timePeriod, page, callback)`
   - `getUserRank(userId, callback)`
8. Implement `LeaderboardViewModel.java` — timePeriod, entries list, userRank, loading
9. Build `item_leaderboard_entry.xml` per `ui_guide.md` §3.2
10. Implement `LeaderboardAdapter.java` — highlight current user
11. Build `fragment_leaderboard.xml` per `ui_guide.md` §2.7:
    - Time filter ChipGroup
    - Podium layout (1st center/tall, 2nd left, 3rd right)
    - Rankings RecyclerView with pagination
12. Implement `LeaderboardFragment.java`:
    - Chip selection triggers new leaderboard load
    - Podium populated from first 3 entries
    - "Your Rank" card at bottom
13. Implement `ProfileViewModel.java` — user data, badges, streak, impact
14. Build `item_badge.xml` per `ui_guide.md` §3.5
15. Implement `BadgeGridAdapter.java` — earned vs locked states
16. Build `fragment_profile.xml` per `ui_guide.md` §2.11
17. Implement `ProfileFragment.java`:
    - Load user data, badges, calculate streak display
    - Badge grid (4 columns)
    - Impact breakdown card
    - Logout button
18. Build `fragment_badge_detail.xml` per `ui_guide.md` §2.14
19. Implement `BadgeDetailFragment.java`:
    - Receive badgeType via Safe Args
    - Load badge definition + user's badge (if earned)
    - Show progress bar if not earned, date if earned

### Tests
- **Unit tests:**
  - `BadgeEvaluatorTest`: user meets threshold → badge earned, user below → not earned, already earned → not duplicated
  - `LeaderboardControllerTest` (mocked repo): returns sorted list, user rank correct
- **Integration test:**
  - Log enough activities to trigger a badge → verify badge in Firestore subcollection
  - Leaderboard shows users in correct order
  - Profile displays correct stats and earned badges

### Acceptance Criteria
- [ ] Badges auto-evaluate after each activity log
- [ ] Newly earned badges saved to Firestore
- [ ] Leaderboard shows paginated list of users by points
- [ ] Top 3 displayed in podium layout
- [ ] Time period chips filter the leaderboard
- [ ] Current user's rank is highlighted
- [ ] Profile shows user stats, streak card, impact breakdown
- [ ] Badge grid shows earned (bright) and locked (dim) badges
- [ ] Badge detail page shows progress or earned date

---

## Phase 6: Social Feed + Challenges

### Goal
Implement the social activity feed with reactions, and the challenge system with join/progress/complete flows.

### Scope
- `FeedItem` model
- `FeedRepository`, `FeedController`, `FeedViewModel`
- `FeedFragment` + `FeedItemAdapter`
- Feed posting integrated into `ActivityController.logActivity()`
- `Challenge` model, `ChallengeParticipant` model
- `ChallengeRepository`, `ChallengeController`, `ChallengeViewModel`
- `ChallengesFragment` + `ChallengeAdapter`
- `ChallengeDetailFragment`

### Dependencies
- Phase 5 complete (user profiles for feed display, leaderboard nav for challenges)

### Implementation Steps
1. Implement `FeedItem.java` model
2. Implement `FeedRepository.java`:
   - `postFeedItem(FeedItem)` → `Task<Void>`
   - `getFeedItems(int limit, DocumentSnapshot startAfter)` → paginated query, ordered by timestamp desc
   - `updateReactions(feedItemId, reactions)` → `Task<Void>`
3. Implement `FeedController.java`:
   - `postToFeed(ActivityLog log, User user)` — construct FeedItem from log data, respect user's anonymity setting
   - `getFeed(page, callback)`
   - `addReaction(feedItemId, emoji, callback)` — increment reaction count atomically
4. Integrate into `ActivityController.logActivity()`:
   - After successful log, call `FeedController.postToFeed()` with the log and user
5. Implement `FeedViewModel.java` — feedItems, isLoading, pagination state
6. Build `item_feed_activity.xml` per `ui_guide.md` §3.3
7. Implement `FeedItemAdapter.java`:
   - Handle anonymous vs named display
   - Reaction emoji row with click listeners
   - Relative timestamp display
8. Build `fragment_feed.xml` per `ui_guide.md` §2.10
9. Implement `FeedFragment.java`:
   - SwipeRefreshLayout for pull-to-refresh
   - Infinite scroll pagination
   - Live banner: "X activities today" (query with today's date filter)
10. Add Feed access point from HomeFragment or Navigation Drawer
11. Implement `Challenge.java` model + `ChallengeParticipant.java` model
12. Implement `ChallengeRepository.java`:
    - `createChallenge(Challenge)` → `Task<Void>` (admin)
    - `getActiveChallenges()` → query where endDate > now
    - `getChallenge(challengeId)` → `Task<Challenge>`
    - `addParticipant(challengeId, ChallengeParticipant)` → `Task<Void>`
    - `updateParticipantProgress(challengeId, userId, newProgress)` → `Task<Void>`
    - `getChallengeParticipants(challengeId)` → `Task<List<ChallengeParticipant>>`
13. Implement `ChallengeController.java`:
    - `getActiveChallenges(callback)`
    - `joinChallenge(challengeId, callback)` — add participant doc
    - `updateProgress(challengeId, quantity, callback)` — increment progress, check if completed
    - `getChallengeDetail(challengeId, callback)` — challenge + participant list
14. Integrate into `ActivityController.logActivity()`:
    - After log, check if user has active challenges matching the activity type
    - If yes, call `ChallengeController.updateProgress()` automatically
15. Implement `ChallengeViewModel.java`
16. Build `item_challenge.xml` per `ui_guide.md` §3.4
17. Implement `ChallengeAdapter.java` — active/available/completed states
18. Build `fragment_challenges.xml` per `ui_guide.md` §2.8
19. Implement `ChallengesFragment.java`:
    - Active challenge hero card at top
    - Available challenges list
    - Completed challenges list
20. Build `fragment_challenge_detail.xml` per `ui_guide.md` §2.9
21. Implement `ChallengeDetailFragment.java`:
    - Progress ring
    - Join/Leave button toggle
    - Participant list

### Tests
- **Unit tests:**
  - `FeedControllerTest`: post creates correct FeedItem, anonymous mode hides name
  - `ChallengeControllerTest`: join adds participant, progress increments, completion detected
- **Integration test:**
  - Log activity → feed item appears in social feed
  - Join challenge → log matching activity → challenge progress updates
  - Complete challenge → marked as completed

### Acceptance Criteria
- [ ] Activities auto-post to social feed on log
- [ ] Anonymous setting respected (shows "Anonymous Hero")
- [ ] Reactions can be added and counts update
- [ ] Feed supports pull-to-refresh and pagination
- [ ] Challenges list shows active/available/completed sections
- [ ] User can join a challenge
- [ ] Logging matching activities auto-updates challenge progress
- [ ] Challenge detail shows progress ring and participant list

---

## Phase 7: Teams + QR Scanner + Search + Admin

### Goal
Implement team functionality, QR code scanning for activity check-in, search across the app, and the admin analytics panel.

### Scope
- `Team` model
- `TeamRepository`, `TeamController`, `TeamViewModel`
- `TeamListFragment` + `TeamAdapter`
- `TeamDetailFragment` + `TeamMemberAdapter` + `TeamActivityAdapter`
- `CreateTeamFragment`
- `QrScanResult` model
- `QrScannerViewModel`
- `QrScannerFragment` (CameraX + ML Kit)
- `SearchController`, `SearchViewModel`
- `SearchFragment` + `SearchResultAdapter`
- `AdminController`, `AdminViewModel`
- `AdminAnalyticsFragment`
- `CreateChallengeFragment`

### Dependencies
- Phase 6 complete (challenges exist for admin to create, feed for team activity)

### Implementation Steps
1. Implement `Team.java` model
2. Implement `TeamRepository.java`:
   - `createTeam(Team)` → `Task<Void>`
   - `getTeam(teamId)` → `Task<Team>`
   - `getTeams(filter)` → `Task<List<Team>>` (filter: all, club, department)
   - `addMember(teamId, userId)` → add to memberIds array, increment count
   - `removeMember(teamId, userId)` → remove from array
   - `updateTeamPoints(teamId, points)` → `FieldValue.increment()`
3. Implement `TeamController.java`:
   - `createTeam(name, type, description, isPublic, callback)`
   - `joinTeam(teamId, callback)` — add current user to members
   - `leaveTeam(teamId, callback)` — remove current user
   - `getTeamDetail(teamId, callback)` — team + member user objects
   - `getTeamList(filter, callback)`
4. Integrate into `ActivityController.logActivity()`:
   - After log, if user is in a team, increment team's totalPoints
5. Implement `TeamViewModel.java`
6. Build team UI: `fragment_team_list.xml`, `item_team.xml`, `fragment_team_detail.xml`, `item_team_member.xml`, `fragment_create_team.xml` — all per `ui_guide.md` §2.15–§2.17
7. Implement `TeamListFragment`, `TeamDetailFragment`, `CreateTeamFragment`
8. Implement `TeamAdapter`, `TeamMemberAdapter`, `TeamActivityAdapter`
9. Implement `QrScanResult.java` model
10. Implement `QrScannerViewModel.java` — scanResult, isScanning
11. Build `fragment_qr_scanner.xml` per `ui_guide.md` §2.5
12. Implement `QrScannerFragment.java`:
    - Request camera permission (runtime)
    - Setup CameraX preview
    - Configure ML Kit barcode scanner (QR format)
    - On QR detected: parse JSON/data → `QrScanResult`
    - Show result overlay card with activity details
    - "Confirm" → navigate to LogActivityFragment with pre-filled data (via Safe Args)
13. QR code format: JSON `{"locationId":"lib-001","locationName":"Library","activityType":"recycling","quantity":1}`
14. Implement `SearchController.java`:
    - `search(query, filter, callback)` — dispatches to sub-searches based on filter
    - `searchChallenges(query)` — Firestore query with `whereGreaterThanOrEqualTo` on title
    - `searchTeams(query)` — similar prefix search
    - Note: Firestore doesn't support full-text search natively; use prefix matching or consider Algolia for production
15. Implement `SearchViewModel.java`
16. Build `fragment_search.xml` per `ui_guide.md` §2.19
17. Implement `SearchFragment.java` — search bar with debounce, filter chips, results RecyclerView
18. Implement `SearchResultAdapter.java` — heterogeneous results
19. Implement `AdminController.java`:
    - `getCampusAnalytics(callback)` — campus stats + weekly chart data
    - `getPhotoProofQueue(callback)` — query unverified activity logs with photos
    - `approveProof(userId, logId, callback)` — set verified = true
    - `rejectProof(userId, logId, callback)` — delete log, decrement stats
    - `updateConversionFactor(factor, callback)` — update Firestore document
20. Implement `AdminViewModel.java`
21. Build `fragment_admin_analytics.xml` per `ui_guide.md` §2.20
22. Implement `AdminAnalyticsFragment.java`:
    - Campus stats grid
    - Weekly activity chart
    - Photo proof review queue
    - Conversion factors table
    - Top students list
    - Quick actions (create challenge navigates to `CreateChallengeFragment`)
23. Build `fragment_create_challenge.xml` per `ui_guide.md` §2.18
24. Implement `CreateChallengeFragment.java` — admin-only, form with date pickers
25. Role-based visibility: In `ProfileFragment` settings, show "Admin Panel" only if `user.role == "admin"`

### Tests
- **Unit tests:**
  - `TeamControllerTest`: create team, join, leave, points aggregation
  - `SearchControllerTest`: search returns matching results
  - `AdminControllerTest`: approve/reject proof, update conversion factor
- **Integration test:**
  - Create team → join → log activity → team points increase
  - QR scan → confirms activity → log created
  - Search "biking" → returns relevant challenges/activities
  - Admin: approve photo proof → verified flag set

### Acceptance Criteria
- [ ] Users can create teams with name, type, and color
- [ ] Users can join/leave teams
- [ ] Team points aggregate from member activity
- [ ] Team detail shows member leaderboard and activity feed
- [ ] QR scanner opens camera, detects QR codes, parses data
- [ ] Confirmed QR scan creates an activity log
- [ ] Search returns challenges and teams matching query
- [ ] Admin panel shows campus analytics
- [ ] Admin can approve/reject photo proofs
- [ ] Admin can create challenges via form
- [ ] Admin features only visible to admin-role users

---

## Phase 8: Settings, Notifications, Polish & Testing

### Goal
Implement remaining settings screens, push notification infrastructure, comprehensive testing, performance optimization, and final polish.

### Scope
- `EditProfileFragment` (full UI + functionality)
- `NotificationSettingsFragment` (full UI + SharedPreferences)
- `NotificationPreferences` model
- `NotificationViewModel`
- `ImageUtils` utility (photo compression)
- Firebase Cloud Messaging setup
- Photo upload flow in activity logging
- Comprehensive test suite
- Performance optimization (pagination, caching)
- Error handling and offline states
- Animations and transitions per `ui_guide.md` §4

### Dependencies
- Phase 7 complete (all features exist, polish phase adds finishing touches)

### Implementation Steps
1. Build `fragment_edit_profile.xml` per `ui_guide.md` §2.12
2. Implement `EditProfileFragment.java`:
   - Pre-fill fields from current user
   - Avatar upload: gallery picker → `ImageUtils.compressImage()` → Firebase Storage upload → update user `avatarUrl`
   - Privacy toggles: `anonymousOnFeed`, `showOnLeaderboard`
   - Save: update user Firestore document
   - Delete account: confirmation dialog → `UserController.deleteAccount()` → navigate to Login
3. Implement `ImageUtils.java`:
   - `compressImage(Uri, maxWidth)` → `byte[]` — scale down, JPEG compress (80% quality)
4. Integrate photo upload into `LogActivityFragment`:
   - Camera intent or gallery picker
   - Compress and upload to `proofs/{userId}/{logId}.jpg`
   - Set `photoProofUrl` on ActivityLog
5. Build `fragment_notification_settings.xml` per `ui_guide.md` §2.13
6. Implement `NotificationPreferences.java` model (SharedPreferences backed)
7. Implement `NotificationViewModel.java`:
   - Load/save preferences from SharedPreferences
8. Implement `NotificationSettingsFragment.java`:
   - Toggle switches + time picker for daily reminder
   - Save to SharedPreferences
9. Firebase Cloud Messaging setup:
   - Register FCM token on app launch → store in user document
   - Subscribe to topics: `all_users`, `campus_milestones`, etc.
   - Handle incoming notifications in `FirebaseMessagingService`
10. Implement animations from `ui_guide.md` §4:
    - Eco-score ring animation
    - Heatmap cell stagger
    - Feed item slide-up
    - Podium entrance
    - Activity card selection bounce
    - Screen transitions via Navigation Component
11. Implement proper loading states across all screens:
    - Shimmer placeholders or progress indicators
    - Empty states with illustrations
    - Error states with retry buttons
12. Performance optimization:
    - Firestore query pagination on all lists (leaderboard, feed, logs)
    - Glide image caching configured
    - RecyclerView `setHasFixedSize(true)` where applicable
    - `DiffUtil.ItemCallback` in all adapters for efficient updates
13. Offline handling:
    - Firestore offline persistence is enabled by default
    - Show connectivity warning banner when offline
    - Queue actions for when connection returns
14. Write comprehensive test suite:
    - **Unit tests for all utilities:** ImpactCalculator, EcoScoreCalculator, StreakManager, BadgeEvaluator, EquivalencyTranslator, DateUtils, ValidationUtils
    - **Unit tests for all controllers** (with mocked repositories)
    - **ViewModel tests** (with mocked controllers, verify LiveData emissions)
    - **Espresso UI tests:**
      - Full auth flow: register → login → logout
      - Log activity flow: select → quantity → preview → submit → success
      - Navigation: all bottom tabs accessible
      - Leaderboard: tabs switch, list scrolls
15. Code cleanup:
    - Remove all TODO stubs
    - Add JavaDoc to public methods
    - ProGuard/R8 rules for Firebase, Glide, MPAndroidChart
    - Verify all `contentDescription` attributes
    - Test on multiple screen sizes (phone + tablet)

### Tests
- **Full test suite** as described in step 14
- **Manual QA checklist:**
  - [ ] Fresh install: register → full app tour
  - [ ] Log all 6 activity types
  - [ ] Verify dashboard updates after each log
  - [ ] Trigger badge unlock
  - [ ] Break and rebuild streak
  - [ ] Join and complete a challenge
  - [ ] Create and manage a team
  - [ ] Scan QR code
  - [ ] Search for challenge and team
  - [ ] Edit profile and change avatar
  - [ ] Toggle notification settings
  - [ ] Test as admin: review proofs, create challenge
  - [ ] Test offline mode
  - [ ] Test rotation (locked to portrait)
  - [ ] Test process death (ViewModel saved state)

### Acceptance Criteria
- [ ] Edit profile saves all changes to Firestore
- [ ] Avatar upload works via gallery picker
- [ ] Delete account removes auth and Firestore data
- [ ] Notification settings persist in SharedPreferences
- [ ] Photo proof upload works during activity logging
- [ ] All screens have proper loading/empty/error states
- [ ] Animations are smooth (60fps)
- [ ] Unit test coverage ≥ 80% on utilities and controllers
- [ ] No crashes on emulator testing
- [ ] App size < 15MB APK

---

## Phase Dependency Graph

```
Phase 1 (Scaffold)
    │
    ▼
Phase 2 (Auth)
    │
    ▼
Phase 3 (Activity Logging)
    │
    ▼
Phase 4 (Dashboard + Streak + Eco-Score)
    │
    ▼
Phase 5 (Badges + Leaderboard + Profile)
    │
    ▼
Phase 6 (Feed + Challenges)
    │
    ▼
Phase 7 (Teams + QR + Search + Admin)
    │
    ▼
Phase 8 (Settings + Notifications + Polish + Tests)
```

Each phase MUST be fully completed and tested before proceeding to the next. This ensures a stable foundation at every step.

---

*This phased plan ensures that the application is built incrementally with testable milestones. Follow each phase in order, completing all acceptance criteria before advancing.*
