package com.cognia.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationTest {

    private val allScreens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Create,
        Screen.Chat,
        Screen.Profile,
        Screen.Welcome,
        Screen.Login,
        Screen.Register,
        Screen.Onboarding,
        Screen.ReelPlayer,
        Screen.QuizScreen,
        Screen.UserProfile,
        Screen.ChatConversation,
        Screen.Notifications,
        Screen.Leaderboard,
        Screen.CreatorAnalytics,
        Screen.Settings,
    )

    @Test
    fun allScreenRoutesAreUnique() {
        val routes = allScreens.map { it.route }
        assertEquals(routes.size, routes.toSet().size, "Screen routes must be unique")
    }

    @Test
    fun bottomNavItemsContainsExactlyFiveItems() {
        assertEquals(5, bottomNavItems.size, "Bottom nav should have exactly 5 items")
    }

    @Test
    fun bottomNavItemsReferenceValidScreenRoutes() {
        val allRoutes = allScreens.map { it.route }.toSet()
        bottomNavItems.forEach { item ->
            assertTrue(
                item.screen.route in allRoutes,
                "Bottom nav item '${item.label}' references unknown route '${item.screen.route}'",
            )
        }
    }

    @Test
    fun detailScreensGenerateCorrectRoutes() {
        assertEquals("reel/abc123", Screen.ReelPlayer.createRoute("abc123"))
        assertEquals("quiz/q1", Screen.QuizScreen.createRoute("q1"))
        assertEquals("user/u42", Screen.UserProfile.createRoute("u42"))
        assertEquals("chat/conv7", Screen.ChatConversation.createRoute("conv7"))
    }
}
