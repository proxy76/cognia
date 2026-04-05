package com.cognia.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive breakpoints for the Cognia app.
 */
enum class WindowWidthClass {
    /** Phone-like: < 600dp */
    COMPACT,
    /** Tablet / small browser: 600–840dp */
    MEDIUM,
    /** Desktop / wide browser: > 840dp */
    EXPANDED,
}

fun windowWidthClassOf(widthDp: Dp): WindowWidthClass = when {
    widthDp < 600.dp -> WindowWidthClass.COMPACT
    widthDp < 840.dp -> WindowWidthClass.MEDIUM
    else -> WindowWidthClass.EXPANDED
}

/** Maximum width for mobile-like screens rendered on a wide viewport. */
val MobileContentMaxWidth = 480.dp

/** Maximum width for dashboard / admin screens on a wide viewport. */
val DashboardContentMaxWidth = 1100.dp

/** Maximum width for the video player column on desktop. */
val VideoPlayerDesktopWidth = 420.dp

/** Side panel width for contextual info on desktop video view. */
val VideoSidePanelWidth = 320.dp

val LocalWindowWidthClass = compositionLocalOf { WindowWidthClass.COMPACT }

/** Routes that should use full-width immersive layout (no mobile constraint). */
val immersiveRoutes = setOf("reel/{videoId}", "home")
