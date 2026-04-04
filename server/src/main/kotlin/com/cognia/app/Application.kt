package com.cognia.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.cognia.app.config.AppConfig
import com.cognia.app.plugins.*
import com.cognia.app.service.BadgeService
import com.cognia.app.service.CategoryService
import org.koin.ktor.ext.inject

fun main() {
    val config = AppConfig.fromEnvironment()
    embeddedServer(
        Netty,
        port = config.server.port,
        host = config.server.host,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureDI()
    configureSerialization()
    configureCors()
    configureStatusPages()
    configureWebSockets()
    configureCallLogging()
    configureAuth()
    configureRouting()
    seedData()
}

private fun Application.seedData() {
    val categoryService by inject<CategoryService>()
    categoryService.seedCategories()

    val badgeService by inject<BadgeService>()
    badgeService.seedBadges()
}
