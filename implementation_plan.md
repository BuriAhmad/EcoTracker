# EcoTrack — Comprehensive Implementation Plan

---

## 1. App Overview

**EcoTrack** is a gamified Android application for campus sustainability tracking. It enables university students to log eco-friendly activities (biking, recycling, water saving, etc.), see their environmental impact quantified in real metrics (kg CO₂ saved, litres of water conserved, kg of waste diverted), compete on leaderboards, earn achievement badges, participate in challenges, and view their progress on personal and campus-wide dashboards.

**Target users:** Students, campus sustainability offices, environmental clubs, and university administration.

**Problem solved:** Universities lack a persistent feedback loop between individual sustainable actions and their measurable environmental impact. EcoTrack closes that loop through gamification, social features, and data-driven dashboards.

**Language:** Java  
**Min SDK:** 26 (Android 8.0)  
**Target/Compile SDK:** 34  
**Backend:** Firebase (Firestore, Auth, Storage, Cloud Messaging)

---

## 2. Architecture

### Pattern: MVVM + Controller + Repository

The project uses a **layered MVVM architecture** with an explicit **Controller layer** between ViewModels and Repositories, as mandated by the team's UML and meeting guidelines.

**Why this fits:**
- **MVVM** provides lifecycle-aware UI state management via LiveData, surviving configuration changes.
- **Controller layer** centralizes all business logic and CRUD orchestration in dedicated classes, keeping models as pure POJOs (team requirement).
- **Repository layer** abstracts Firebase SDK calls behind clean interfaces, enabling testability and potential future data-source swaps.

### Layer Structure

```
┌─────────────────────────────────────────────────┐
│  UI LAYER (Fragments, Adapters, Custom Views)   │
│  - Observes LiveData from ViewModels            │
│  - Handles user interactions                     │
│  - Zero business logic                          │
├─────────────────────────────────────────────────┤
│  VIEWMODEL LAYER                                │
│  - Holds UI state as LiveData/MutableLiveData   │
│  - Performs lightweight input validation         │
│  - Delegates to Controllers                     │
│  - No Android framework imports (except ViewModel│
│    and LiveData)                                │
├─────────────────────────────────────────────────┤
│  CONTROLLER LAYER                               │
│  - All CRUD operations and business logic       │
│  - Orchestrates multi-step workflows            │
│  - Coordinates between Repositories and Utils   │
│  - Enforces business rules                      │
├─────────────────────────────────────────────────┤
│  REPOSITORY LAYER                               │
│  - Firestore/Firebase SDK abstraction           │
│  - Query construction, pagination               │
│  - Photo upload to Firebase Storage             │
│  - Real-time snapshot listeners                 │
├─────────────────────────────────────────────────┤
│  MODEL LAYER (POJOs)                            │
│  - Plain data holders, no logic                 │
│  - Firestore serialization annotations          │
├─────────────────────────────────────────────────┤
│  UTILITY LAYER                                  │
│  - Stateless helper classes                     │
│  - Impact calculation, badge evaluation, etc.   │
└─────────────────────────────────────────────────┘
```

---

## 3. Full Feature List

### F1: User Authentication & Profiles
- **What:** Email/password registration and login via Firebase Auth. User profile with display name, department, avatar.
- **Trigger:** App launch (if not authenticated), Register/Login button taps.
- **Data:** `users/{userId}` Firestore document.
- **Validation:** Email format validation, password minimum 6 characters, department required on registration.
- **Edge cases:** Duplicate email, network errors, empty fields, session expiry.

### F2: Activity Logging
- **What:** Students select an activity type from a visual grid (Biking, Walking, Recycling, Water Save, Energy Saving, Plastic-Free) and enter a quantity. Impact is calculated in real-time and displayed before submission.
- **Trigger:** Tap "Log" tab → select activity card → set quantity → tap "Log Activity".
- **Data:** `activityLogs/{logId}` subcollection under user, `conversionFactors/{activityType}`, `campusStats/aggregate`.
- **Validation:** Quantity must be positive and non-zero. Max 20 logs/day per user.
- **Edge cases:** Zero quantity, extremely large values (anomaly flag at >100 km biking/day), offline logging.

### F3: Impact Calculation Engine
- **What:** Converts logged activities into CO₂ saved (kg), water saved (L), waste diverted (kg), and points using server-side conversion factors.
- **Trigger:** On every activity log submission and in real-time preview.
- **Data:** `conversionFactors/{activityType}` — co2PerUnit, waterPerUnit, wastePerUnit, pointsPerUnit.
- **Formula:** `metric = quantity × factorPerUnit`.

### F4: Personal Dashboard
- **What:** Visual summary with Eco-Score ring, weekly bar chart, impact stats (CO₂, water, waste), carbon equivalencies (trees planted, miles not driven), activity heatmap calendar, and recent activity log list.
- **Trigger:** Navigate to Dashboard/Home screen.
- **Data:** Aggregated from user's `activityLogs`, user profile fields (ecoScore, totalPoints, currentStreak).

### F5: Campus-Wide Aggregated Dashboard
- **What:** Real-time totals of CO₂ saved, water saved, waste diverted, total activities logged, total users across all campus.
- **Trigger:** Displayed on Home screen hero card and Leaderboard campus banner.
- **Data:** `campusStats/aggregate` document.

### F6: Leaderboard
- **What:** Ranked list of top contributors by points with time-period tabs (This Week, This Month, All Time). Top 3 displayed as podium with avatars. Current user's rank highlighted. Paginated ranked list below.
- **Trigger:** Navigate to Board tab.
- **Data:** `users` collection ordered by `totalPoints` descending.
- **Edge cases:** Ties in points, user not yet ranked.

### F7: Achievement Badges
- **What:** Unlockable badges for milestones (e.g., "100 km Biked", "30-Day Streak", "Recycling Rookie"). Badge detail page shows progress, rarity, date earned, description.
- **Trigger:** Evaluated after each activity log. Badge detail opened from profile badge grid.
- **Data:** `badgeDefinitions/{badgeType}`, `users/{userId}/badges/{badgeId}`.

### F8: Streak & Habit Engine
- **What:** Tracks consecutive days of logging. Streaks multiply point earnings (1.5× after 7 days, 2× after 30 days). Displayed on profile with progress bar to next reward.
- **Trigger:** Evaluated on each activity log based on `lastLogDate`.
- **Data:** `users/{userId}.currentStreak`, `users/{userId}.lastLogDate`.

### F9: Weekly Green Challenges
- **What:** Time-boxed challenges (e.g., "Zero-Waste Week") created by admins with start/end dates, target activity type, and point rewards. Students can join, track progress, and complete challenges.
- **Trigger:** View challenges list, join challenge, log qualifying activities.
- **Data:** `challenges/{challengeId}`, `challenges/{challengeId}/participants/{userId}`.

### F10: Team Competitions
- **What:** Students create or join teams (club/department type). Teams have aggregated scores. Team detail shows member leaderboard and activity feed.
- **Trigger:** Teams tab, create team form, join team button, team detail page.
- **Data:** `teams/{teamId}` with memberIds array, totalPoints.

### F11: Social Feed & Reactions
- **What:** Campus-wide feed of recent activities with optional anonymity, emoji reactions, and 1-level reply depth. Live banner showing today's activity count.
- **Trigger:** Navigate to Feed screen.
- **Data:** `socialFeed/{feedItemId}`.

### F12: Activity Verification (Photo Proof)
- **What:** Optional photo upload for high-value activities. Admin review queue with approve/reject.
- **Trigger:** During activity logging (optional photo attachment). Admin analytics screen.
- **Data:** Firebase Storage `proofs/{userId}/{logId}.jpg`, `activityLog.photoProofUrl`, `activityLog.verified`.

### F13: QR Code Challenge Check-In
- **What:** Scan physical QR codes at campus stations to auto-log activities. QR encodes: location ID, activity type, success/failure, non-negative values.
- **Trigger:** Tap QR scanner from Log screen.
- **Data:** Decoded QR data → activity log creation.
- **Implementation:** CameraX + ML Kit Barcode Scanning.

### F14: Carbon Equivalency Translator
- **What:** Translates raw metrics into relatable analogies ("planted 2.4 trees", "45 miles not driven").
- **Trigger:** Displayed on Dashboard.
- **Data:** Mapping table in `EquivalencyTranslator` utility.

### F15: Eco-Score™ (Composite Index)
- **What:** Single 0–100 score: `0.4×CO₂_norm + 0.25×waste_norm + 0.2×water_norm + 0.15×streak_norm`.
- **Trigger:** Calculated after each activity log, displayed on Profile and Dashboard.
- **Data:** Derived from user's cumulative metrics.

### F16: Impact Heatmap Calendar
- **What:** GitHub-style contribution grid showing daily activity intensity with 4 levels (none, low, medium, high) over ~5 weeks.
- **Trigger:** Displayed on Dashboard.
- **Data:** Daily aggregation of user's activity logs.

### F17: Push Notification Reminders
- **What:** Daily nudge at user-configured time, challenge updates, streak alerts, campus milestones, badge unlocks. Configurable via Notification Settings screen.
- **Trigger:** Firebase Cloud Messaging scheduled topics.
- **Data:** User notification preferences in SharedPreferences.

### F18: Admin Panel
- **What:** Admin-only analytics dashboard showing campus-wide stats, weekly activity chart, photo proof review queue, conversion factor management, top students, and quick actions (create challenge, export CSV, sync factors, user audit).
- **Trigger:** Admin role users see Admin tab.
- **Data:** Aggregated campus data, `conversionFactors` collection, photo proof queue.

### F19: Search
- **What:** Search for activities and challenges with filter pills (All, Activities, Challenges, Teams). Results grouped by type with relevant details.
- **Trigger:** Search icon tap on various screens.
- **Data:** Queries across `conversionFactors`, `challenges`, `teams` collections.

### F20: Profile & Settings
- **What:** View profile with stats (points, eco-score, logs), streak card, impact breakdown, badges grid. Edit profile (name, department, avatar). Notification settings. Privacy toggles. Delete account.
- **Trigger:** Profile tab, settings gear button.
- **Data:** User document, badges subcollection, activity logs.

---

## 4. Complete Class/Component List

### 4.1 Models (`com.ecotrack.app.model`)

| Class | Type | Responsibility | Key Fields | Notes |
|-------|------|---------------|------------|-------|
| `User` | POJO | Represents a user profile | `userId`, `displayName`, `email`, `department`, `avatarUrl`, `role` (student/admin), `totalPoints`, `ecoScore`, `currentStreak`, `lastLogDate`, `createdAt` | Firestore serializable |
| `ActivityLog` | POJO | Represents a logged activity | `logId`, `userId`, `activityType`, `quantity`, `unit`, `co2Saved`, `waterSaved`, `wasteDiverted`, `pointsEarned`, `photoProofUrl`, `verified`, `timestamp` | Subcollection under user |
| `ConversionFactor` | POJO | Per-activity conversion rates | `activityType`, `co2PerUnit`, `waterPerUnit`, `wastePerUnit`, `pointsPerUnit`, `unit`, `source`, `lastUpdated` | Read from Firestore |
| `Challenge` | POJO | A time-boxed challenge | `challengeId`, `title`, `description`, `activityType`, `targetQuantity`, `startDate`, `endDate`, `createdBy`, `teamBased`, `pointsReward` | Top-level collection |
| `ChallengeParticipant` | POJO | User's progress in a challenge | `userId`, `progress`, `joinedAt` | Subcollection under challenge |
| `Team` | POJO | A team (club/department) | `teamId`, `name`, `type` (club/department/other), `description`, `memberIds`, `totalPoints`, `createdAt`, `createdBy`, `isPublic` | Top-level collection |
| `Badge` | POJO | An earned badge instance | `badgeType`, `unlockedAt`, `displayed` | Subcollection under user |
| `BadgeDefinition` | POJO | Badge template/definition | `badgeType`, `name`, `description`, `iconUrl`, `metric`, `threshold`, `multiplierBonus`, `rarity` | Top-level collection |
| `CampusStats` | POJO | Campus-wide aggregates | `totalCo2Saved`, `totalWaterSaved`, `totalWasteDiverted`, `totalActivitiesLogged`, `totalUsers`, `lastUpdated` | Single document |
| `FeedItem` | POJO | Social feed entry | `feedItemId`, `userId`, `displayName`, `activityType`, `quantity`, `department`, `anonymous`, `reactions` (Map<String,Integer>), `timestamp`, `co2Saved`, `description` | Top-level collection |
| `QrScanResult` | POJO | Decoded QR data | `locationId`, `locationName`, `activityType`, `quantity`, `success` | Local model, not persisted directly |
| `NotificationPreferences` | POJO | User notification settings | `dailyReminderEnabled`, `reminderHour`, `challengeUpdates`, `streakAlerts`, `campusMilestones`, `badgeUnlocks` | Stored in SharedPreferences |

### 4.2 Controllers (`com.ecotrack.app.controller`)

| Class | Responsibility | Key Methods | Collaborators |
|-------|---------------|-------------|---------------|
| `ActivityController` | Orchestrate activity logging workflow: validate → compute impact → batch write (log + user totals + campus stats + feed) → evaluate badges → update streak. Enforce max 20 logs/day. | `logActivity(type, qty, photoUri)`, `getActivityHistory(userId, page)`, `getDailyLogCount(userId)` | `ActivityRepository`, `UserRepository`, `ImpactCalculator`, `StreakManager`, `BadgeEvaluator`, `FeedController` |
| `UserController` | Manage auth flows (register, login, logout), profile CRUD, role checking. | `register(email, pass, name, dept)`, `login(email, pass)`, `logout()`, `updateProfile(user)`, `deleteAccount()`, `isAdmin()` | `UserRepository`, `FirebaseAuth` |
| `ChallengeController` | CRUD for challenges, join/leave logic, progress tracking, validation. | `createChallenge(challenge)`, `joinChallenge(challengeId)`, `updateProgress(challengeId, qty)`, `getActiveChallenges()`, `getChallengeDetail(id)` | `ChallengeRepository`, `UserRepository` |
| `TeamController` | Team CRUD, join/leave, aggregate score computation. | `createTeam(team)`, `joinTeam(teamId)`, `leaveTeam(teamId)`, `getTeamDetail(id)`, `getTeamList()`, `getTeamMembers(teamId)` | `TeamRepository`, `UserRepository` |
| `LeaderboardController` | Query and rank users/teams by points with time filters. | `getLeaderboard(timePeriod, page)`, `getUserRank(userId)`, `getTeamLeaderboard()` | `LeaderboardRepository`, `UserRepository` |
| `FeedController` | Construct feed items, enforce anonymity, handle reactions, 1-level replies. | `postToFeed(activityLog, anonymous)`, `getFeed(page)`, `addReaction(feedItemId, emoji)`, `addReply(feedItemId, text)` | `FeedRepository`, `UserRepository` |
| `SearchController` | Search activities, challenges, and teams by query string. | `search(query, filter)`, `searchActivities(query)`, `searchChallenges(query)`, `searchTeams(query)` | `ActivityRepository`, `ChallengeRepository`, `TeamRepository` |
| `AdminController` | Admin-specific operations: analytics, photo proof review, conversion factor management. | `getCampusAnalytics()`, `getPhotoProofQueue()`, `approveProof(logId)`, `rejectProof(logId)`, `updateConversionFactor(factor)` | `ActivityRepository`, `UserRepository` |

### 4.3 Repositories (`com.ecotrack.app.repository`)

| Class | Responsibility | Key Methods |
|-------|---------------|-------------|
| `ActivityRepository` | Firestore CRUD for activityLogs, conversionFactors, campusStats. Photo upload to Firebase Storage. | `saveActivityLog(userId, log)`, `getConversionFactor(type)`, `getAllConversionFactors()`, `getActivityLogs(userId, startDate, endDate)`, `uploadPhoto(userId, logId, uri)`, `executeBatchWrite(batch)`, `incrementCampusStats(co2, water, waste)` |
| `UserRepository` | Firebase Auth + Firestore user document CRUD. | `createUser(user)`, `getUser(userId)`, `updateUser(user)`, `getUsersByPoints(limit, offset)`, `getUserBadges(userId)`, `saveBadge(userId, badge)`, `deleteUser(userId)` |
| `ChallengeRepository` | Firestore CRUD for challenges and participants. | `createChallenge(challenge)`, `getActiveChallenge()`, `getChallenge(id)`, `addParticipant(challengeId, participant)`, `updateParticipantProgress(challengeId, userId, progress)`, `getChallengeParticipants(challengeId)` |
| `TeamRepository` | Firestore CRUD for teams. | `createTeam(team)`, `getTeam(teamId)`, `getTeams(filter)`, `updateTeam(team)`, `addMember(teamId, userId)`, `removeMember(teamId, userId)` |
| `LeaderboardRepository` | Firestore queries for ranked user/team lists with pagination. | `getUsersRankedByPoints(timePeriod, limit, startAfter)`, `getTeamsRankedByPoints(limit)`, `getUserRank(userId, timePeriod)` |
| `FeedRepository` | Firestore CRUD for socialFeed, real-time listeners. | `postFeedItem(item)`, `getFeedItems(limit, startAfter)`, `updateReactions(feedItemId, reactions)`, `addFeedListener(listener)` |

### 4.4 ViewModels (`com.ecotrack.app.viewmodel`)

| Class | Responsibility | Key LiveData |
|-------|---------------|-------------|
| `AuthViewModel` | UI state for login/register flows. | `loginState`, `registerState`, `currentUser`, `errorMessage` |
| `ActivityViewModel` | UI state for activity logging. Selected activity, quantity, live preview, submission. | `selectedActivity`, `quantity`, `impactPreview`, `logResult`, `isLoading`, `todayLogCount` |
| `DashboardViewModel` | UI state for personal dashboard. Eco-score, charts, stats, heatmap, recent logs. | `ecoScore`, `weeklyData`, `totalCo2`, `totalWater`, `totalWaste`, `heatmapData`, `recentLogs`, `equivalencies` |
| `HomeViewModel` | UI state for home screen. Campus stats, streak, points. | `campusStats`, `userStreak`, `weeklyPoints`, `ecoScore`, `quickActions` |
| `LeaderboardViewModel` | UI state for leaderboard. Time period, ranked list, user rank. | `timePeriod`, `leaderboardEntries`, `userRank`, `campusStats` |
| `ChallengeViewModel` | UI state for challenges list and detail. | `activeChallenges`, `availableChallenges`, `completedChallenges`, `challengeDetail`, `userProgress` |
| `TeamViewModel` | UI state for teams list and detail. | `teams`, `teamDetail`, `teamMembers`, `teamActivity` |
| `ProfileViewModel` | UI state for profile display and editing. | `user`, `badges`, `streakInfo`, `impactBreakdown`, `editState` |
| `FeedViewModel` | UI state for social feed. | `feedItems`, `isLoading`, `reactionResult` |
| `SearchViewModel` | UI state for search results. | `query`, `activityResults`, `challengeResults`, `teamResults` |
| `AdminViewModel` | UI state for admin analytics. | `campusStats`, `weeklyChart`, `proofQueue`, `conversionFactors`, `topStudents` |
| `QrScannerViewModel` | UI state for QR scanning. | `scanResult`, `isScanning`, `detectedActivity` |
| `NotificationViewModel` | UI state for notification preferences. | `preferences`, `saveResult` |

### 4.5 Views — Fragments (`com.ecotrack.app.view`)

| Package | Fragment | Screen | Layout XML |
|---------|----------|--------|------------|
| `auth/` | `LoginFragment` | Login screen | `fragment_login.xml` |
| `auth/` | `RegisterFragment` | Registration screen | `fragment_register.xml` |
| `home/` | `HomeFragment` | Home/landing screen with eco-score, stats, recent activity | `fragment_home.xml` |
| `logging/` | `LogActivityFragment` | Activity logging with card grid and quantity stepper | `fragment_log_activity.xml` |
| `logging/` | `QrScannerFragment` | Camera QR scanner with result overlay | `fragment_qr_scanner.xml` |
| `dashboard/` | `DashboardFragment` | Personal dashboard with charts, heatmap, equivalencies | `fragment_dashboard.xml` |
| `leaderboard/` | `LeaderboardFragment` | Leaderboard with podium and ranked list | `fragment_leaderboard.xml` |
| `challenges/` | `ChallengesFragment` | Challenges list (active, available, completed) | `fragment_challenges.xml` |
| `challenges/` | `ChallengeDetailFragment` | Single challenge detail with progress ring | `fragment_challenge_detail.xml` |
| `challenges/` | `CreateChallengeFragment` | Admin form to create a challenge | `fragment_create_challenge.xml` |
| `social/` | `FeedFragment` | Social feed with activity cards and reactions | `fragment_feed.xml` |
| `teams/` | `TeamListFragment` | List of teams with filter pills | `fragment_team_list.xml` |
| `teams/` | `TeamDetailFragment` | Team detail with member leaderboard and activity feed | `fragment_team_detail.xml` |
| `teams/` | `CreateTeamFragment` | Form to create a new team | `fragment_create_team.xml` |
| `profile/` | `ProfileFragment` | User profile with stats, streak, badges, impact breakdown | `fragment_profile.xml` |
| `profile/` | `EditProfileFragment` | Edit profile form with privacy toggles | `fragment_edit_profile.xml` |
| `profile/` | `BadgeDetailFragment` | Badge detail view | `fragment_badge_detail.xml` |
| `profile/` | `NotificationSettingsFragment` | Notification preferences | `fragment_notification_settings.xml` |
| `admin/` | `AdminAnalyticsFragment` | Admin analytics dashboard | `fragment_admin_analytics.xml` |
| `search/` | `SearchFragment` | Search with filter tabs and results | `fragment_search.xml` |

### 4.6 Views — Adapters (`com.ecotrack.app.view`)

| Adapter | Used By | Item Layout | Binds To |
|---------|---------|-------------|----------|
| `ActivityCategoryAdapter` | `LogActivityFragment` | `item_activity_category.xml` | Activity type cards (icon, label, selected state) |
| `LeaderboardAdapter` | `LeaderboardFragment` | `item_leaderboard_entry.xml` | Rank, avatar, name, points, activity count |
| `FeedItemAdapter` | `FeedFragment` | `item_feed_activity.xml` | Feed cards with avatar, name, activity, metrics, reactions |
| `ChallengeAdapter` | `ChallengesFragment` | `item_challenge.xml` | Challenge cards (title, participants, points, join button) |
| `TeamAdapter` | `TeamListFragment` | `item_team.xml` | Team cards (initials, name, members, rank, points) |
| `TeamMemberAdapter` | `TeamDetailFragment` | `item_team_member.xml` | Member rows (rank, avatar, name, points) |
| `TeamActivityAdapter` | `TeamDetailFragment` | `item_team_activity.xml` | Team activity timeline items |
| `BadgeGridAdapter` | `ProfileFragment` | `item_badge.xml` | Badge grid items (icon, name, locked/unlocked state) |
| `RecentLogAdapter` | `DashboardFragment` | `item_recent_log.xml` | Recent log rows (icon, title, time, points) |
| `SearchResultAdapter` | `SearchFragment` | `item_search_result.xml` | Search result rows (icon, title, description, action) |
| `ProofQueueAdapter` | `AdminAnalyticsFragment` | `item_proof_queue.xml` | Proof review items (user, activity, approve/reject buttons) |
| `ConversionFactorAdapter` | `AdminAnalyticsFragment` | `item_conversion_factor.xml` | Conversion factor table rows |

### 4.7 Custom Views (`com.ecotrack.app.view`)

| View | Package | Responsibility |
|------|---------|---------------|
| `HeatmapCalendarView` | `dashboard/` | Custom View rendering a 7-column grid of colored squares showing daily activity intensity over weeks |
| `ImpactChartView` | `dashboard/` | Wrapper/helper for MPAndroidChart bar/scatter chart configuration |
| `EcoScoreRingView` | `home/` | Custom View rendering animated circular progress ring for eco-score |
| `CampusStatsCard` | `home/` | Reusable card component for campus aggregate display |

### 4.8 Utilities (`com.ecotrack.app.util`)

| Class | Responsibility | Key Methods |
|-------|---------------|-------------|
| `ImpactCalculator` | Compute environmental impact from activity + conversion factor. Stateless. | `calculateImpact(quantity, conversionFactor)` → returns `ImpactResult(co2, water, waste, points)` |
| `EcoScoreCalculator` | Compute composite 0–100 eco-score from normalized metrics. | `calculateEcoScore(co2Total, wasteTotal, waterTotal, streak)` |
| `StreakManager` | Determine streak continuation, increment, or reset. | `evaluateStreak(lastLogDate, currentDate, currentStreak)` → returns `StreakResult(newStreak, multiplier)` |
| `BadgeEvaluator` | Compare user metrics against badge thresholds, return newly earned badges. | `evaluateNewBadges(user, badgeDefinitions)` → returns `List<Badge>` |
| `EquivalencyTranslator` | Convert raw metrics into relatable analogies. | `translate(co2Kg)` → returns `List<Equivalency>` (e.g., "2.4 trees planted", "45 miles not driven") |
| `DateUtils` | Date formatting, day-of-week helpers, timestamp comparisons. | `isConsecutiveDay(date1, date2)`, `formatRelativeTime(timestamp)`, `getStartOfWeek()`, `getStartOfMonth()` |
| `Constants` | App-wide constants. | `MAX_DAILY_LOGS = 20`, `STREAK_MULTIPLIER_7 = 1.5`, `STREAK_MULTIPLIER_30 = 2.0`, collection names, SharedPreferences keys |
| `ValidationUtils` | Input validation helpers. | `isValidEmail(email)`, `isValidPassword(pass)`, `isPositiveQuantity(qty)` |
| `ImageUtils` | Bitmap compression for photo uploads. | `compressImage(uri, maxWidth)` → returns `byte[]` |

### 4.9 Main Activity & Navigation

| Class | Responsibility |
|-------|---------------|
| `MainActivity` | Single-Activity host. Sets up Navigation Component with NavHostFragment and BottomNavigationView. Handles auth state redirection. |

---

## 5. Data Layer — Firestore Schema

### 5.1 Firestore Collections & Documents

#### `users/{userId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `userId` | String | Document ID, matches Firebase Auth UID |
| `displayName` | String | Required, ≤50 chars |
| `email` | String | Required, valid email format |
| `department` | String | Required (e.g., "Computer Science", "Environmental Sciences") |
| `avatarUrl` | String | Nullable, URL to Firebase Storage |
| `role` | String | "student" or "admin", default "student" |
| `totalPoints` | Number | Default 0, non-negative |
| `ecoScore` | Number | 0–100, computed |
| `currentStreak` | Number | Default 0, non-negative |
| `lastLogDate` | Timestamp | Nullable |
| `createdAt` | Timestamp | Server timestamp on creation |
| `anonymousOnFeed` | Boolean | Default false |
| `showOnLeaderboard` | Boolean | Default true |

#### `users/{userId}/activityLogs/{logId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `logId` | String | Auto-generated document ID |
| `activityType` | String | Must match a conversionFactors document |
| `quantity` | Number | Positive, non-zero |
| `unit` | String | e.g., "km", "items", "L", "hrs", "day" |
| `co2Saved` | Number | Computed, ≥0 |
| `waterSaved` | Number | Computed, ≥0 |
| `wasteDiverted` | Number | Computed, ≥0 |
| `pointsEarned` | Number | Computed, ≥0 |
| `photoProofUrl` | String | Nullable |
| `verified` | Boolean | Default true (false if photo required for type) |
| `timestamp` | Timestamp | Server timestamp |

#### `users/{userId}/badges/{badgeId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `badgeType` | String | Matches a badgeDefinitions document |
| `unlockedAt` | Timestamp | Server timestamp |
| `displayed` | Boolean | Default true |

#### `conversionFactors/{activityType}`
| Field | Type | Constraints |
|-------|------|-------------|
| `activityType` | String | Document ID (e.g., "biking", "recycling") |
| `co2PerUnit` | Number | ≥0 |
| `waterPerUnit` | Number | ≥0 |
| `wastePerUnit` | Number | ≥0 |
| `pointsPerUnit` | Number | ≥0 |
| `unit` | String | e.g., "km", "items" |
| `source` | String | Citation URL |
| `lastUpdated` | Timestamp | |

#### `challenges/{challengeId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `challengeId` | String | Auto-generated |
| `title` | String | Required |
| `description` | String | Required |
| `activityType` | String | |
| `targetQuantity` | Number | Positive |
| `startDate` | Timestamp | |
| `endDate` | Timestamp | Must be > startDate |
| `createdBy` | String | userId of creator |
| `teamBased` | Boolean | Default false |
| `pointsReward` | Number | ≥0 |

#### `challenges/{challengeId}/participants/{userId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `progress` | Number | ≥0, ≤ targetQuantity |
| `joinedAt` | Timestamp | |
| `completed` | Boolean | Default false |

#### `teams/{teamId}`
| Field | Type | Constraints |
|-------|------|-------------|
| `teamId` | String | Auto-generated |
| `name` | String | Required, unique |
| `type` | String | "club", "department", or "other" |
| `description` | String | Optional |
| `memberIds` | Array<String> | User IDs |
| `totalPoints` | Number | Aggregated from members |
| `createdAt` | Timestamp | |
| `createdBy` | String | userId |
| `isPublic` | Boolean | Default true |

#### `campusStats/aggregate`
| Field | Type |
|-------|------|
| `totalCo2Saved` | Number |
| `totalWaterSaved` | Number |
| `totalWasteDiverted` | Number |
| `totalActivitiesLogged` | Number |
| `totalUsers` | Number |
| `lastUpdated` | Timestamp |

#### `socialFeed/{feedItemId}`
| Field | Type |
|-------|------|
| `feedItemId` | String |
| `userId` | String |
| `displayName` | String (denormalized) |
| `activityType` | String |
| `quantity` | Number |
| `unit` | String |
| `co2Saved` | Number |
| `department` | String (denormalized) |
| `anonymous` | Boolean |
| `description` | String |
| `reactions` | Map<String, Integer> (emoji → count) |
| `timestamp` | Timestamp |

#### `badgeDefinitions/{badgeType}`
| Field | Type |
|-------|------|
| `badgeType` | String |
| `name` | String |
| `description` | String |
| `iconUrl` | String |
| `metric` | String (e.g., "totalCo2", "currentStreak", "totalBikeKm") |
| `threshold` | Number |
| `rarity` | String ("Common", "Rare", "Epic", "Legendary") |
| `multiplierBonus` | Number (nullable) |

### 5.2 SharedPreferences Keys

| Key | Type | Purpose |
|-----|------|---------|
| `pref_daily_reminder_enabled` | Boolean | Whether daily reminder is on |
| `pref_reminder_hour` | Int | Hour for daily reminder (0–23) |
| `pref_challenge_updates` | Boolean | Challenge notification toggle |
| `pref_streak_alerts` | Boolean | Streak alert toggle |
| `pref_campus_milestones` | Boolean | Campus milestone toggle |
| `pref_badge_unlocks` | Boolean | Badge unlock notification toggle |
| `pref_user_role` | String | Cached user role for quick nav decisions |
| `pref_onboarding_complete` | Boolean | Whether onboarding has been shown |

---

## 6. Navigation Graph

### Single-Activity Architecture

`MainActivity` hosts a `NavHostFragment` with a navigation graph (`nav_graph.xml`).

### Bottom Navigation Destinations (4 tabs)
1. **Home** → `HomeFragment`
2. **Log** → `LogActivityFragment`
3. **Board** → `LeaderboardFragment`
4. **Profile** → `ProfileFragment`

### Full Navigation Map

```
LoginFragment
  ├── → RegisterFragment (tap "Create Account")
  ├── → HomeFragment (successful login)
  └── ← RegisterFragment (tap "Sign In" link)

RegisterFragment
  ├── → HomeFragment (successful registration)
  └── → LoginFragment (tap "Sign In" link)

HomeFragment (Bottom Nav: Home)
  ├── → DashboardFragment (tap eco-score card or "My Dashboard")
  ├── → NotificationSettingsFragment (tap bell icon)
  ├── → LogActivityFragment (via bottom nav)
  └── → SearchFragment (tap search icon)

LogActivityFragment (Bottom Nav: Log)
  ├── → QrScannerFragment (tap QR icon)
  └── → HomeFragment (after successful log, optional)

QrScannerFragment
  ├── → LogActivityFragment (confirmed QR activity)
  └── ← Back to LogActivityFragment

DashboardFragment
  ├── → HomeFragment (back)
  └── Shows: eco-score ring, weekly chart, stats, heatmap, equivalencies, recent logs

LeaderboardFragment (Bottom Nav: Board)
  ├── → TeamListFragment (tap teams tab or link)
  ├── → ProfileFragment (tap a user entry — future)
  └── → ChallengesFragment (tap challenges link)

ChallengesFragment
  ├── → ChallengeDetailFragment (tap a challenge card)
  ├── → CreateChallengeFragment (admin only, tap create button)
  └── ← Back to LeaderboardFragment

ChallengeDetailFragment
  ├── ← Back to ChallengesFragment
  └── Join/Leave actions within the fragment

CreateChallengeFragment
  └── ← Back to ChallengesFragment (after publish/save)

TeamListFragment
  ├── → TeamDetailFragment (tap a team card)
  ├── → CreateTeamFragment (tap + button)
  └── ← Back to LeaderboardFragment

TeamDetailFragment
  ├── ← Back to TeamListFragment
  └── Leave team action within fragment

CreateTeamFragment
  └── ← Back to TeamListFragment (after creation)

FeedFragment
  ├── → SearchFragment (tap search icon)
  └── Accessed via HomeFragment or navigation drawer

ProfileFragment (Bottom Nav: Profile)
  ├── → EditProfileFragment (tap settings gear)
  ├── → BadgeDetailFragment (tap a badge in grid)
  ├── → NotificationSettingsFragment (from settings)
  ├── → AdminAnalyticsFragment (admin only, visible in settings)
  └── → LoginFragment (after logout)

EditProfileFragment
  ├── ← Back to ProfileFragment
  └── → LoginFragment (after delete account)

BadgeDetailFragment
  └── ← Back to ProfileFragment

NotificationSettingsFragment
  └── ← Back to ProfileFragment or HomeFragment

AdminAnalyticsFragment (admin only)
  ├── → CreateChallengeFragment (quick action)
  └── ← Back to ProfileFragment

SearchFragment
  ├── → ChallengeDetailFragment (tap challenge result)
  ├── → TeamDetailFragment (tap team result)
  └── ← Back to calling fragment
```

### Data Passed Between Destinations

| From → To | Args (via Safe Args Bundle) |
|-----------|---------------------------|
| `ChallengesFragment` → `ChallengeDetailFragment` | `challengeId: String` |
| `TeamListFragment` → `TeamDetailFragment` | `teamId: String` |
| `ProfileFragment` → `BadgeDetailFragment` | `badgeType: String`, `userId: String` |
| `QrScannerFragment` → `LogActivityFragment` | `activityType: String`, `quantity: int`, `locationName: String` |
| `SearchFragment` → `ChallengeDetailFragment` | `challengeId: String` |
| `SearchFragment` → `TeamDetailFragment` | `teamId: String` |

---

## 7. Dependencies (Gradle)

### Project-level `build.gradle`
```groovy
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'com.google.gms.google-services' version '4.4.0' apply false
    id 'androidx.navigation.safeargs' version '2.7.6' apply false
}
```

### App-level `build.gradle`
```groovy
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
    id 'androidx.navigation.safeargs'
}

android {
    namespace 'com.ecotrack.app'
    compileSdk 34
    defaultConfig {
        applicationId "com.ecotrack.app"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding true
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    // AndroidX Core
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // Lifecycle & ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime:2.7.0'

    // Navigation
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'

    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-messaging'

    // Charts
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

    // QR/Barcode Scanning
    implementation 'androidx.camera:camera-core:1.3.1'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    implementation 'androidx.camera:camera-view:1.3.1'
    implementation 'com.google.mlkit:barcode-scanning:17.2.0'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.8.0'
    testImplementation 'org.mockito:mockito-inline:5.2.0'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test:rules:1.5.0'
    androidTestImplementation 'androidx.navigation:navigation-testing:2.7.6'
    androidTestImplementation 'androidx.fragment:fragment-testing:1.6.2'
}
```

### `settings.gradle` — Repositories
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // For MPAndroidChart
    }
}
```

---

## 8. Seed Data

### Conversion Factors (to seed in Firestore)

| activityType | co2PerUnit | waterPerUnit | wastePerUnit | pointsPerUnit | unit |
|-------------|-----------|-------------|-------------|--------------|------|
| biking | 0.21 | 0 | 0 | 15 | km |
| walking | 0.25 | 0 | 0 | 10 | km |
| recycling | 0 | 0 | 0.4 | 20 | items |
| water_save | 0 | 1.0 | 0 | 5 | L |
| energy_saving | 0.12 | 0 | 0 | 12 | hrs |
| plastic_free | 0 | 0 | 0.8 | 50 | day |
| composting | 0.05 | 0 | 0.5 | 15 | kg |
| reuse_cup | 0.05 | 0.5 | 0 | 10 | use |
| meatless_meal | 1.50 | 500 | 0 | 50 | meal |
| public_transit | 0.15 | 0 | 0 | 12 | km |

### Badge Definitions (to seed in Firestore)

| badgeType | name | metric | threshold | rarity |
|-----------|------|--------|-----------|--------|
| first_log | First Step | totalActivities | 1 | Common |
| streak_7 | Week Warrior | currentStreak | 7 | Common |
| streak_30 | Monthly Master | currentStreak | 30 | Rare |
| bike_100km | Leaf Glider | totalBikeKm | 100 | Rare |
| recycle_50 | Recycling Rookie | totalRecycled | 50 | Common |
| recycle_200 | Recycling Pro | totalRecycled | 200 | Epic |
| co2_100kg | Carbon Crusher | totalCo2 | 100 | Epic |
| water_1000L | Water Guardian | totalWater | 1000 | Rare |
| points_5000 | Eco Champion | totalPoints | 5000 | Legendary |
| challenges_5 | Challenge Seeker | challengesCompleted | 5 | Rare |

---

*This document serves as the single source of architectural truth for the EcoTrack Android application. All class names, package structures, data schemas, and design decisions referenced here should be followed consistently throughout implementation.*
