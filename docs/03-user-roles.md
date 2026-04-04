# User Roles

## Role Summary

| Role | Type | Description |
|------|------|-------------|
| Learner | Consumer | Default role for all registered users |
| Regular Creator | Creator | Can upload content, subject to moderation review |
| Licensed Creator | Creator | Trusted creator with instant-publish privileges |
| Moderator | Staff | Reviews content, reports, and license requests |
| Admin | Staff | Manages platform roles, moderation config, and system settings |

---

## Learner

**Who:** Any registered user consuming content on Cognia.

**Permissions:**
- Watch content (reels)
- Take quizzes
- Follow creators
- Add friends (mutual)
- Chat privately (1:1, text and shared posts)
- Share content
- Gain points from correct quiz answers
- Appear in leaderboards (friends and global)

**Constraints:**
- Cannot upload content
- Cannot set difficulty on quizzes
- Cannot access moderation tools

---

## Regular Creator

**Who:** A user who has opted into the creator role and uploads educational content.

**Permissions:**
- Upload videos
- Create quizzes (multiple choice, true/false; attached to video or standalone)
- Submit content for moderator review
- View basic analytics (views, quiz attempts, correct answer rate, shares, engagement)
- Request licensed creator status after 5 approved videos

**Constraints:**
- All content must be approved by a moderator before it is published
- Cannot set difficulty level on content
- Cannot publish instantly
- Subject to the strike system (3 strikes)

**Content workflow:**
`draft → submit → moderator_review → approved/rejected → published (if approved)`

---

## Licensed Creator

**Who:** A creator who has been granted licensed status by a moderator.

**Eligibility:**
- Must have at least 5 previously approved videos

**Approval flow:**
1. Creator requests licensed status
2. Moderator reviews creator's history
3. Moderator approves or rejects the request

**Permissions:**
- All Regular Creator permissions
- Publish content instantly (no pre-publication review)
- Set difficulty level (easy, medium, hard) on eligible content
- View analytics

**Constraints:**
- Content is still reviewed after publication
- If licensed content is rejected in post-publication review, it is deleted
- **3 rejected licensed videos → licensed role is revoked**
- After revocation, creator reverts to Regular Creator and must re-apply later

**Content workflow:**
`draft → publish_instantly → post_publication_review → deleted (if rejected)`

---

## Moderator

**Who:** Staff member responsible for content quality and safety.

**Permissions:**
- Review Regular Creator submissions (approve/reject)
- Review post-publication Licensed Creator content
- Review reported content from users
- Approve or reject creator license requests
- Issue strikes to creators
- Revoke licensed creator role (manually or via the 3-rejection rule)

**Constraints:**
- Cannot manage platform roles beyond creator licensing
- Cannot modify system settings
- Reviews based on accuracy and safety criteria

---

## Admin

**Who:** Platform administrator with full system access.

**Permissions:**
- Manage platform roles (assign/remove moderator, admin roles)
- Manage moderation configuration
- Manage system settings

**Constraints:**
- Admin is a management role; day-to-day content moderation is handled by Moderators

---

## Role Hierarchy

```
Admin
  └── Moderator
        └── Licensed Creator
              └── Regular Creator
                    └── Learner
```

- Every user is at minimum a Learner.
- Creator roles are additive — a Licensed Creator retains all Learner and Regular Creator permissions.
- Moderator and Admin are separate staff roles, not part of the creator progression.

## Strike System

- Applies to all creators (regular and licensed)
- Strikes are issued by moderators
- **3 strikes** is the limit

### Consequences per strike:
1. **1-week cooldown**: Creator cannot upload or publish any content for 7 days
2. **License revocation** (if licensed): Licensed creator role is revoked immediately. Creator must restart the full licensing process (earn 5 approved videos again) after cooldown ends.
3. These consequences apply on **every** strike, not just the 3rd

### Separate rule: Licensed content rejection
- 3 rejected licensed videos also revoke the licensed role (independent of the strike system)
- After revocation, creator reverts to Regular Creator and must re-apply
