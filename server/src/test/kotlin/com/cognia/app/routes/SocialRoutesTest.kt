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

class SocialRoutesTest {

    private fun uniqueEmail(prefix: String) = "$prefix-${UUID.randomUUID()}@example.com"

    @Test
    fun followWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/users/some-user/follow")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun unfollowWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.delete("/api/v1/users/some-user/follow")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun getFollowersWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/users/some-user/followers")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun getFollowingWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/users/some-user/following")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun sendFriendRequestWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/friends/request/some-user")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun acceptFriendRequestWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/friends/accept/some-request")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun declineFriendRequestWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.post("/api/v1/friends/decline/some-request")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun listFriendsWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/friends")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun listFriendRequestsWithoutAuthReturns401() = testApplication {
        application { module() }
        val response = client.get("/api/v1/friends/requests")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun followAndUnfollowWithAuthToken() = testApplication {
        application { module() }

        val email1 = uniqueEmail("follow-test")
        val email2 = uniqueEmail("follow-target")

        // Register a user to get a valid token
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email1","password":"password123","displayName":"Follow Tester"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)

        val registerBody = Json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
        val token = registerBody["token"]!!.jsonPrimitive.content
        val userId = registerBody["userId"]!!.jsonPrimitive.content

        // Register a second user
        val register2Response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email2","password":"password123","displayName":"Target User"}""")
        }
        assertEquals(HttpStatusCode.Created, register2Response.status)
        val register2Body = Json.parseToJsonElement(register2Response.bodyAsText()).jsonObject
        val targetId = register2Body["userId"]!!.jsonPrimitive.content

        // Follow the target user
        val followResponse = client.post("/api/v1/users/$targetId/follow") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, followResponse.status)
        val followBody = Json.parseToJsonElement(followResponse.bodyAsText()).jsonObject
        assertTrue(followBody["following"]!!.jsonPrimitive.content.toBoolean())

        // Get followers of target
        val followersResponse = client.get("/api/v1/users/$targetId/followers") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, followersResponse.status)
        val followersBody = Json.parseToJsonElement(followersResponse.bodyAsText()).jsonObject
        val followers = followersBody["followers"]!!.jsonArray
        assertEquals(1, followers.size)
        assertEquals(userId, followers[0].jsonObject["id"]!!.jsonPrimitive.content)

        // Unfollow
        val unfollowResponse = client.delete("/api/v1/users/$targetId/follow") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, unfollowResponse.status)
        val unfollowBody = Json.parseToJsonElement(unfollowResponse.bodyAsText()).jsonObject
        assertFalse(unfollowBody["following"]!!.jsonPrimitive.content.toBoolean())

        // Verify no more followers
        val followersAfter = client.get("/api/v1/users/$targetId/followers") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val followersAfterBody = Json.parseToJsonElement(followersAfter.bodyAsText()).jsonObject
        assertEquals(0, followersAfterBody["followers"]!!.jsonArray.size)
    }

    @Test
    fun friendRequestFlow() = testApplication {
        application { module() }

        val emailA = uniqueEmail("friend-a")
        val emailB = uniqueEmail("friend-b")

        // Register user A
        val registerA = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailA","password":"password123","displayName":"User A"}""")
        }
        assertEquals(HttpStatusCode.Created, registerA.status)
        val bodyA = Json.parseToJsonElement(registerA.bodyAsText()).jsonObject
        val tokenA = bodyA["token"]!!.jsonPrimitive.content

        // Register user B
        val registerB = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$emailB","password":"password123","displayName":"User B"}""")
        }
        assertEquals(HttpStatusCode.Created, registerB.status)
        val bodyB = Json.parseToJsonElement(registerB.bodyAsText()).jsonObject
        val tokenB = bodyB["token"]!!.jsonPrimitive.content
        val userBId = bodyB["userId"]!!.jsonPrimitive.content

        // A sends friend request to B
        val requestResponse = client.post("/api/v1/friends/request/$userBId") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.Created, requestResponse.status)
        val requestBody = Json.parseToJsonElement(requestResponse.bodyAsText()).jsonObject
        val requestId = requestBody["id"]!!.jsonPrimitive.content
        assertEquals("PENDING", requestBody["status"]!!.jsonPrimitive.content)

        // B sees pending requests
        val pendingResponse = client.get("/api/v1/friends/requests") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.OK, pendingResponse.status)
        val pendingBody = Json.parseToJsonElement(pendingResponse.bodyAsText()).jsonObject
        val requests = pendingBody["requests"]!!.jsonArray
        assertTrue(requests.size >= 1)

        // B accepts the request
        val acceptResponse = client.post("/api/v1/friends/accept/$requestId") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.OK, acceptResponse.status)
        val acceptBody = Json.parseToJsonElement(acceptResponse.bodyAsText()).jsonObject
        assertEquals("ACCEPTED", acceptBody["status"]!!.jsonPrimitive.content)

        // Both users should now see each other as friends
        val friendsA = client.get("/api/v1/friends") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        val friendsABody = Json.parseToJsonElement(friendsA.bodyAsText()).jsonObject
        assertTrue(friendsABody["friends"]!!.jsonArray.size >= 1)
    }
}
