package com.cognia.app.plugins

import com.cognia.app.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    val dbPath = System.getenv("COGNIA_DB_PATH") ?: "./data/cognia-dev.db"
    DatabaseFactory.init(dbPath)
}
