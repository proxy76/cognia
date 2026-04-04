package com.cognia.app.routes

import com.cognia.app.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import kotlin.test.*

class ChatRoutesTest {

    private fun uniqueEmail(prefix: String) = "$prefix-${UUID.randomUUID()}@example.com"

    @Test
    fun listConversationsWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/chat/conversations")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun createConversationWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/chat/conversations") {
            contentType(ContentType.Application.Json)
            setBody("""{"participantId":"some-id"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun getMessagesWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/chat/conversations/some-id/messages")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun sendMessageWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/chat/conversations/some-id/messages") {
            contentType(ContentType.Application.Json)
            setBody("""{"messageType":"TEXT","textContent":"hello"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun chatBetweenFriends() = testApplication {
        application { module() }

        val emailA = uniqueEmail("chat-a")
        val emailB = uniqueEmail("chat-b")

        // Register user A
        val registerA = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailA","password":"password123","displayName":"Chat User A"}""")
        }
        assertEquals(HttpStatusCode.Created, registerA.status)
        val bodyA = Json.parseToJsonElement(registerA.bodyAsText()).jsonObject
        val tokenA = bodyA["token"]!!.jsonPrimitive.content
        val userAId = bodyA["userId"]!!.jsonPrimitive.content

        // Register user B
        val registerB = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailB","password":"password123","displayName":"Chat User B"}""")
        }
        assertEquals(HttpStatusCode.Created, registerB.status)
        val bodyB = Json.parseToJsonElement(registerB.bodyAsText()).jsonObject
        val tokenB = bodyB["token"]!!.jsonPrimitive.content
        val userBId = bodyB["userId"]!!.jsonPrimitive.content

        // Cannot create conversation without being friends
        val nonFriendConv = client.post("/api/v1/chat/conversations") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"participantId":"$userBId"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, nonFriendConv.status)

        // Make them friends: A sends request, B accepts
        val friendRequest = client.post("/api/v1/friends/request/$userBId") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.Created, friendRequest.status)
        val requestId = Json.parseToJsonElement(friendRequest.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val accept = client.post("/api/v1/friends/accept/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.OK, accept.status)

        // Now create conversation
        val createConv = client.post("/api/v1/chat/conversations") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"participantId":"$userBId"}""")
        }
        assertEquals(HttpStatusCode.Created, createConv.status)
        val convBody = Json.parseToJsonElement(createConv.bodyAsText()).jsonObject
        val conversationId = convBody["id"]!!.jsonPrimitive.content

        // Send a text message
        val sendMsg = client.post("/api/v1/chat/conversations/$conversationId/messages") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"messageType":"TEXT","textContent":"Hello friend!"}""")
        }
        assertEquals(HttpStatusCode.Created, sendMsg.status)
        val msgBody = Json.parseToJsonElement(sendMsg.bodyAsText()).jsonObject
        assertEquals("TEXT", msgBody["messageType"]!!.jsonPrimitive.content)
        assertEquals("Hello friend!", msgBody["textContent"]!!.jsonPrimitive.content)
        assertEquals(userAId, msgBody["senderId"]!!.jsonPrimitive.content)

        // Get messages from B's perspective
        val getMessages = client.get("/api/v1/chat/conversations/$conversationId/messages") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.OK, getMessages.status)
        val messagesBody = Json.parseToJsonElement(getMessages.bodyAsText()).jsonObject
        val messages = messagesBody["messages"]!!.jsonArray
        assertEquals(1, messages.size)
        assertEquals("Hello friend!", messages[0].jsonObject["textContent"]!!.jsonPrimitive.content)

        // List conversations for A
        val listConversations = client.get("/api/v1/chat/conversations") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.OK, listConversations.status)
        val conversationsBody = Json.parseToJsonElement(listConversations.bodyAsText()).jsonObject
        val conversations = conversationsBody["conversations"]!!.jsonArray
        assertTrue(conversations.size >= 1)

        // Send a shared content message
        val sendShared = client.post("/api/v1/chat/conversations/$conversationId/messages") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"messageType":"SHARED_POST","sharedContentId":"video-123","sharedContentType":"VIDEO"}""")
        }
        assertEquals(HttpStatusCode.Created, sendShared.status)
        val sharedBody = Json.parseToJsonElement(sendShared.bodyAsText()).jsonObject
        assertEquals("SHARED_POST", sharedBody["messageType"]!!.jsonPrimitive.content)
        assertEquals("video-123", sharedBody["sharedContentId"]!!.jsonPrimitive.content)
        assertEquals("VIDEO", sharedBody["sharedContentType"]!!.jsonPrimitive.content)
    }

    @Test
    fun nonParticipantCannotAccessConversation() = testApplication {
        application { module() }

        val emailA = uniqueEmail("chat-access-a")
        val emailB = uniqueEmail("chat-access-b")
        val emailC = uniqueEmail("chat-access-c")

        // Register users A, B, and C
        val registerA = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailA","password":"password123","displayName":"Access A"}""")
        }
        assertEquals(HttpStatusCode.Created, registerA.status)
        val tokenA = Json.parseToJsonElement(registerA.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        val registerB = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailB","password":"password123","displayName":"Access B"}""")
        }
        assertEquals(HttpStatusCode.Created, registerB.status)
        val bodyB = Json.parseToJsonElement(registerB.bodyAsText()).jsonObject
        val tokenB = bodyB["token"]!!.jsonPrimitive.content
        val userBId = bodyB["userId"]!!.jsonPrimitive.content

        val registerC = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailC","password":"password123","displayName":"Access C"}""")
        }
        assertEquals(HttpStatusCode.Created, registerC.status)
        val tokenC = Json.parseToJsonElement(registerC.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        // Make A and B friends, create conversation
        val friendRequest = client.post("/api/v1/friends/request/$userBId") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.Created, friendRequest.status)
        val requestId = Json.parseToJsonElement(friendRequest.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content
        client.post("/api/v1/friends/accept/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }

        val createConv = client.post("/api/v1/chat/conversations") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"participantId":"$userBId"}""")
        }
        assertEquals(HttpStatusCode.Created, createConv.status)
        val conversationId = Json.parseToJsonElement(createConv.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        // C cannot access the conversation
        val getMessages = client.get("/api/v1/chat/conversations/$conversationId/messages") {
            header(HttpHeaders.Authorization, "Bearer $tokenC")
        }
        assertEquals(HttpStatusCode.Forbidden, getMessages.status)
    }
}
