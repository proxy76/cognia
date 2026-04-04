package com.cognia.app.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import kotlin.test.AfterTest
import kotlin.test.Test

class KoinModuleTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyAllModulesLoad() {
        startKoin {
            modules(allModules)
        }.checkModules()
    }
}
