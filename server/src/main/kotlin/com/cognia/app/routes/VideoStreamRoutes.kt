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

            streamFile(call, File(videoUrl))
        }

        get("/{id}/eli5-stream") {
            val videoId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

            val video = transaction {
                VideosTable.selectAll().where { VideosTable.id eq videoId }
                    .singleOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not found"))

            val eli5Url = video[VideosTable.eli5VideoUrl]
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorBody("ELI5 version not available"))

            val status = video[VideosTable.status]
            if (status != "PUBLISHED") {
                val principal = call.principal<UserPrincipal>()
                if (principal == null || principal.userId != video[VideosTable.creatorId]) {
                    return@get call.respond(HttpStatusCode.NotFound, ErrorBody("Video not available"))
                }
            }

            streamFile(call, File(eli5Url))
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

private suspend fun streamFile(call: io.ktor.server.application.ApplicationCall, file: File) {
    if (!file.exists()) {
        call.respond(HttpStatusCode.NotFound, ErrorBody("Video file not found"))
        return
    }

    val rangeHeader = call.request.headers[HttpHeaders.Range]
    val fileLength = file.length()

    try {
        if (rangeHeader != null) {
            val range = rangeHeader.removePrefix("bytes=")
            val parts = range.split("-")
            val start = parts[0].toLongOrNull() ?: 0L
            val end = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].toLong() else fileLength - 1
            val contentLength = end - start + 1

            call.response.header(HttpHeaders.ContentRange, "bytes $start-$end/$fileLength")
            call.response.header(HttpHeaders.AcceptRanges, "bytes")
            call.response.header(HttpHeaders.ContentLength, contentLength.toString())
            call.response.header(HttpHeaders.ContentType, "video/mp4")
            call.response.status(HttpStatusCode.PartialContent)

            call.respondOutputStream {
                file.inputStream().use { input ->
                    input.skip(start)
                    val buffer = ByteArray(8192)
                    var remaining = contentLength
                    while (remaining > 0) {
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                        if (read <= 0) break
                        write(buffer, 0, read)
                        remaining -= read
                    }
                }
            }
        } else {
            call.response.header(HttpHeaders.AcceptRanges, "bytes")
            call.respondFile(file)
        }
    } catch (_: java.io.IOException) {
        // Client disconnected (broken pipe) — safe to ignore
    }
}
