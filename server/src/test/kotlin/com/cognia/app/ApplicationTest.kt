package com.cognia.app

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testModuleLoads() = testApplication {
        application {
            module()
        }
        // Module loads without error — if we get here, it works
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            module()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun testUnknownRouteReturns404Json() = testApplication {
        application {
            module()
        }
        val response = client.get("/nonexistent")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        val error = json["error"]!!.jsonObject
        assertEquals("NOT_FOUND", error["code"]!!.jsonPrimitive.content)
        assertEquals("Resource not found", error["message"]!!.jsonPrimitive.content)
    }
}
