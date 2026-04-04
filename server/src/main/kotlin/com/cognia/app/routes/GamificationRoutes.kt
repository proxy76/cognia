package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.database.UserProfilesTable
import com.cognia.app.database.UsersTable
import com.cognia.app.dto.gamification.BadgeListResponse
import com.cognia.app.dto.gamification.LeaderboardEntryResponse
import com.cognia.app.dto.gamification.LeaderboardResponse
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.model.Leveling
import com.cognia.app.service.BadgeService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Route.gamificationRoutes() {
    val badgeService by application.inject<BadgeService>()

    route("/api/v1") {
        authenticate("auth-jwt") {
            get("/leaderboard") {
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 20
                val clampedLimit = limit.coerceIn(1, 100)

                val entries = transaction {
                    UsersTable
                        .join(UserProfilesTable, JoinType.LEFT, UsersTable.id, UserProfilesTable.userId)
                        .select(
                            UsersTable.id,
                            UsersTable.displayName,
                            UsersTable.avatarUrl,
                            UserProfilesTable.totalPoints,
                            UserProfilesTable.level
                        )
                        .orderBy(UserProfilesTable.totalPoints, SortOrder.DESC)
                        .limit(clampedLimit)
                        .mapIndexed { index, row ->
                            LeaderboardEntryResponse(
                                rank = index + 1,
                                user = UserSummary(
                                    id = row[UsersTable.id],
                                    displayName = row[UsersTable.displayName],
                                    avatarUrl = row[UsersTable.avatarUrl]
                                ),
                                totalPoints = (row.getOrNull(UserProfilesTable.totalPoints) ?: 0).toLong(),
                                level = row.getOrNull(UserProfilesTable.level) ?: 1
                            )
                        }
                }

                call.respond(HttpStatusCode.OK, LeaderboardResponse(entries = entries))
            }

            get("/users/me/badges") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val userBadges = badgeService.getUserBadges(principal.userId)
                call.respond(HttpStatusCode.OK, BadgeListResponse(badges = userBadges.map { it.badge }))
            }

            get("/badges") {
                val allBadges = badgeService.getAllBadges()
                call.respond(HttpStatusCode.OK, BadgeListResponse(badges = allBadges))
            }
        }
    }
}
