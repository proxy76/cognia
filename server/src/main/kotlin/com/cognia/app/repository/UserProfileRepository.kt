package com.cognia.app.repository

import com.cognia.app.database.*
import com.cognia.app.dto.profile.BadgeSummary
import com.cognia.app.dto.profile.UpdateProfileRequest
import com.cognia.app.dto.profile.UserProfileResponse
import com.cognia.app.dto.profile.PublicProfileResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserProfileRepository {

    fun getFullProfile(userId: String): UserProfileResponse? = transaction {
        val row = UsersTable.join(UserProfilesTable, JoinType.LEFT, UsersTable.id, UserProfilesTable.userId)
            .selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull() ?: return@transaction null

        val followerCount = FollowsTable.selectAll()
            .where { FollowsTable.followedId eq userId }
            .count().toInt()

        val followingCount = FollowsTable.selectAll()
            .where { FollowsTable.followerId eq userId }
            .count().toInt()

        val friendCount = FriendshipsTable.selectAll()
            .where {
                (FriendshipsTable.status eq "ACCEPTED") and
                    ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.receiverId eq userId))
            }
            .count().toInt()

        val badgeCount = UserBadgesTable.selectAll()
            .where { UserBadgesTable.userId eq userId }
            .count().toInt()

        val categories = UserCategoriesTable
            .join(CategoriesTable, JoinType.INNER, UserCategoriesTable.categoryId, CategoriesTable.id)
            .select(CategoriesTable.name)
            .where { UserCategoriesTable.userId eq userId }
            .map { it[CategoriesTable.name] }

        UserProfileResponse(
            id = row[UsersTable.id],
            email = row[UsersTable.email],
            displayName = row[UsersTable.displayName],
            avatarUrl = row[UsersTable.avatarUrl],
            role = row[UsersTable.role],
            selfDescription = row.getOrNull(UserProfilesTable.selfDescription),
            level = row.getOrNull(UserProfilesTable.level) ?: 1,
            totalPoints = row.getOrNull(UserProfilesTable.totalPoints) ?: 0,
            categories = categories,
            badgeCount = badgeCount,
            followerCount = followerCount,
            followingCount = followingCount,
            friendCount = friendCount
        )
    }

    fun getPublicProfile(userId: String): PublicProfileResponse? = transaction {
        val row = UsersTable.join(UserProfilesTable, JoinType.LEFT, UsersTable.id, UserProfilesTable.userId)
            .selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull() ?: return@transaction null

        val followerCount = FollowsTable.selectAll()
            .where { FollowsTable.followedId eq userId }
            .count().toInt()

        val videoCount = VideosTable.selectAll()
            .where { (VideosTable.creatorId eq userId) and (VideosTable.status eq "PUBLISHED") }
            .count().toInt()

        val badges = UserBadgesTable
            .join(BadgesTable, JoinType.INNER, UserBadgesTable.badgeId, BadgesTable.id)
            .select(BadgesTable.id, BadgesTable.name, BadgesTable.iconUrl)
            .where { UserBadgesTable.userId eq userId }
            .map { BadgeSummary(it[BadgesTable.id], it[BadgesTable.name], it[BadgesTable.iconUrl]) }

        PublicProfileResponse(
            id = row[UsersTable.id],
            displayName = row[UsersTable.displayName],
            avatarUrl = row[UsersTable.avatarUrl],
            role = row[UsersTable.role],
            level = row.getOrNull(UserProfilesTable.level) ?: 1,
            followerCount = followerCount,
            videoCount = videoCount,
            badges = badges
        )
    }

    fun updateProfile(userId: String, request: UpdateProfileRequest): Boolean = transaction {
        // Check user exists
        val exists = UsersTable.selectAll()
            .where { UsersTable.id eq userId }
            .count() > 0
        if (!exists) return@transaction false

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        // Update UsersTable fields
        if (request.displayName != null || request.avatarUrl != null) {
            UsersTable.update({ UsersTable.id eq userId }) {
                request.displayName?.let { name -> it[displayName] = name }
                request.avatarUrl?.let { url -> it[avatarUrl] = url }
                it[updatedAt] = now
            }
        }

        // Update UserProfilesTable fields
        request.selfDescription?.let { desc ->
            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[selfDescription] = desc
            }
        }

        true
    }
}
