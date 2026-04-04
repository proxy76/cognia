# UI Flows

This document describes user flows, not visual design. Each flow maps the sequence of screens and user actions.

---

## 1. Authentication Flow

### Registration
1. User opens app → **Welcome Screen** with options: Sign Up, Log In
2. User taps Sign Up → **Registration Screen**
   - Options: Email/Password form, Google sign-in button, Apple sign-in button
3. Email path: user enters email, password, display name → Submit
4. OAuth path: user taps Google/Apple → OS auth sheet → returns token
5. On success → redirect to **Onboarding Flow**
6. On error → show inline error message

### Login
1. User taps Log In → **Login Screen**
   - Email/password form, Google button, Apple button
2. On success → redirect to **Home (For You feed)**
3. On error → show inline error

---

## 2. Onboarding Flow

1. After first registration → **Onboarding Welcome** (brief explanation)
2. **AI Question Screen** — AI asks preference questions, one at a time
3. **Self-Description Screen** — free-text input: "Tell us about yourself"
4. **Category Recommendation Screen**
   - Shows AI-recommended categories as selectable chips
   - User can deselect any
   - User can search/add more categories manually
5. User confirms → preferences saved → redirect to **Home (For You feed)**

**Later access:** User can edit categories from Profile Settings.

---

## 3. Home / Feed Navigation

### Feed Switching
- **Top transparent switch bar** (TikTok-style) with two tabs:
  - **For You** (default)
  - **Deep Dive**
- Tapping a tab loads that feed
- Vertical swipe scrolls through content items (reels)

### For You Feed
- Personalized content based on: selected categories, AI recommendations, behavior
- Each item shows: video thumbnail/player, creator info, title, category tag
- Tap → full-screen reel playback

### Deep Dive Feed
- Exploration content outside usual preferences
- Same item layout as For You
- Repeated engagement may promote topics to For You

---

## 4. Search Flow

1. User taps **Search** in bottom navigation → **Search Screen**
2. **Search bar** at top with text input
3. Below search bar (before typing): **Recent searches** list
   - Tap recent search → execute that query
   - "Clear all" option
4. User types query → **Live results** appear grouped by type:
   - **Categories** (top row of chips)
   - **Creators** (horizontal scroll of avatars + names)
   - **Videos** (vertical list with thumbnails)
   - **Quizzes** (vertical list)
5. Tap any result → navigate to that item (reel, quiz, creator profile, category feed)
6. Optional: filter tabs above results (All, Videos, Quizzes, Creators, Categories)

---

## 5. Reel Playback

1. Full-screen vertical video player
2. Overlay UI:
   - Creator avatar + name (tap → creator profile)
   - Title and category
   - Like/share/quiz action buttons
3. If video has an attached quiz → **Quiz Button** visible
4. Tap quiz button → **Quiz Screen** (overlay or new screen)
5. Swipe up → next reel
6. Swipe down → previous reel

---

## 5. Quiz Flow

### Taking a Quiz
1. User enters quiz (from reel button or standalone quiz in feed)
2. **Quiz Screen** shows:
   - Question text
   - Answer options (multiple choice buttons or true/false toggle)
3. User selects answer → immediate feedback (correct/incorrect)
4. Next question (if multi-question)
5. **Results Screen**:
   - Score: X/Y correct
   - Points awarded
   - Difficulty badge if applicable

---

## 6. Creator Upload Flow

### Video Upload
1. Creator taps Create/Upload button → **Upload Screen**
2. Select video file from device
3. **Metadata Form**:
   - Title (required)
   - Description (optional)
   - Category (required, dropdown/search)
   - Difficulty (only visible for licensed creators): easy/medium/hard
4. Save as draft → status: DRAFT
5. **Regular creator path:** Tap "Submit for Review" → status: PENDING_REVIEW
6. **Licensed creator path:** Tap "Publish" → status: PUBLISHED

### Quiz Creation
1. Creator taps Create Quiz → **Quiz Builder**
2. Select quiz type: Multiple Choice or True/False
3. Optionally attach to an existing video
4. Add questions:
   - Question text
   - Answer options (2-4 for MC, auto for T/F)
   - Mark correct answer
5. Set metadata: title, category, difficulty (licensed only)
6. Save / Submit / Publish (same paths as video)

---

## 7. Profile Screen

1. **My Profile** (from bottom nav):
   - Avatar, display name, level, points
   - Badge display
   - Stats: followers, following, friends
   - My content (for creators): list of uploaded videos/quizzes with statuses
   - Settings button
2. **Other User Profile** (from tapping a creator/user):
   - Public info, follow/add friend buttons
   - Published content list

### Profile Settings
- Edit display name, avatar
- Edit category preferences (same UI as onboarding selection)
- Account settings (change password, linked accounts)

---

## 8. Social — Friends & Following

### Following Creators
1. On creator profile → tap "Follow"
2. Following list accessible from own profile

### Friend System
1. On another user's profile → tap "Add Friend"
2. Recipient gets notification → opens **Friend Requests** screen
3. Accept or Decline
4. Accepted → both users appear in each other's friends list

### Friends List
- Accessible from profile
- Shows mutual friends
- Tap friend → their profile

---

## 9. Chat Flow

1. **Conversations List** (from bottom nav or social tab):
   - List of 1:1 conversations, sorted by last message
   - Each row: avatar, name, last message preview, unread indicator
2. Tap conversation → **Chat Screen**:
   - Message list (scrollable, newest at bottom)
   - Text input field + send button
   - Share button → opens content picker to share a post
3. **New conversation:**
   - From a user's profile → tap "Message"
   - Or from friends list → tap "Message"
4. **Shared post in chat:**
   - Renders as a card with thumbnail, title, creator
   - Tap → opens the content (reel or quiz)

---

## 10. Leaderboard Flow

1. Accessible from bottom nav or gamification section
2. **Leaderboard Screen** with tab toggle:
   - **Friends** — ranks among mutual friends
   - **Global** — ranks among all users
3. Each entry: rank, avatar, display name, level, total points
4. Current user's position highlighted

---

## 11. Notifications Flow

1. **Notification Bell** (in header/nav) with unread badge count
2. Tap → **Notifications List**:
   - Grouped by recency
   - Types: friend request, chat message, content approved/rejected, badge earned, level up, new follower, strike, license decision
3. Tap notification → navigate to relevant screen (e.g., approved video, chat, profile)
4. Mark as read on tap; "Mark all as read" option

---

## 12. Moderation Flow (Web Only)

### Moderator Dashboard
1. **Login** → web moderation panel
2. **Review Queue**:
   - Tabs: Videos, Quizzes, Reports, License Requests
   - Each item: content preview, creator info, submission date
3. **Review Screen** (for content):
   - Video player or quiz preview
   - Creator history (past approvals, strikes)
   - Action buttons: Approve, Reject (with reason)
4. **Report Review**:
   - Reported content with report reason
   - Actions: Dismiss, Remove Content, Issue Strike
5. **License Request Review**:
   - Creator profile and history
   - Approved video count
   - Actions: Approve, Reject (with reason)
6. **Strike Management**:
   - View strikes per user
   - Issue new strike with reason

---

## 13. Badge & Level Display

- Badges shown on profile screen as icons with names
- Level shown prominently on profile (number + progress bar to next level)
- Level-up and badge-earned events trigger notifications
- Badge detail: tap badge → description of how it was earned

---

## Navigation Structure

```
Bottom Navigation:
├── Home (Feeds: For You / Deep Dive)
├── Search (keyword search across content, creators, categories)
├── Create (Upload — creators only)
├── Chat (Conversations)
└── Profile (My profile, settings)

Top Bar:
├── Feed switcher (For You / Deep Dive) — on Home
└── Notification bell — global
```
