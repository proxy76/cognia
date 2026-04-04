package com.cognia.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.cognia.app.config.AppConfig
import com.cognia.app.plugins.*

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
    configureRouting()
}
