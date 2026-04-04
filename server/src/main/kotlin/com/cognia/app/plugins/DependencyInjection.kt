package com.cognia.app.plugins

import com.cognia.app.di.allModules
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(allModules)
    }
}
