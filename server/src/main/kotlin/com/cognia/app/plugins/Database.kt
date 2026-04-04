package com.cognia.app.plugins

import com.cognia.app.config.DatabaseConfig
import com.cognia.app.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    val config = DatabaseConfig.fromEnvironment()
    DatabaseFactory.init(config)
}
