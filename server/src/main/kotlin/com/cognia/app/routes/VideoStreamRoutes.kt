package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.database.VideosTable
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Route.videoStreamRoutes() {
    route("/api/v1/videos") {
        get("/{id}/stream") {
            val videoId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

            val video = transaction {
                VideosTable.selectAll().where { VideosTable.id eq videoId }
                    .singleOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not found"))

            val videoUrl = video[VideosTable.videoUrl]
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not yet processed"))

            val status = video[VideosTable.status]
            if (status != "PUBLISHED") {
                val principal = call.principal<UserPrincipal>()
                if (principal == null || principal.userId != video[VideosTable.creatorId]) {
                    return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not available"))
                }
            }

            val file = File(videoUrl)
            if (!file.exists()) {
                return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video file not found"))
            }

            // respondFile handles Range requests, Content-Length, and Content-Type automatically
            call.respondFile(file)
        }

        get("/{id}/thumbnail") {
            val videoId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

            val video = transaction {
                VideosTable.selectAll().where { VideosTable.id eq videoId }
                    .singleOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not found"))

            val thumbnailUrl = video[VideosTable.thumbnailUrl]
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Thumbnail not available"))

            val file = File(thumbnailUrl)
            if (!file.exists()) {
                return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Thumbnail file not found"))
            }

            call.respondFile(file)
        }
    }
}
