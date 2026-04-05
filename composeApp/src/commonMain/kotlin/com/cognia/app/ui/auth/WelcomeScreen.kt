package com.cognia.app.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognia.app.ui.theme.BackgroundDark
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonViolet
import com.cognia.app.ui.theme.SurfaceDarkCard

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Floating ambient animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambientFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Ambient gradient blobs (background decoration) ──
        // Top-right blob
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset { IntOffset(200, (-100 + floatOffset.toInt())) }
                .align(Alignment.TopEnd)
                .alpha(glowPulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonViolet.copy(alpha = 0.4f),
                            NeonPurpleDark.copy(alpha = 0.1f),
                            Color.Transparent,
                        )
                    )
                )
        )
        // Bottom-left blob
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset { IntOffset(-150, (100 - floatOffset.toInt())) }
                .align(Alignment.BottomStart)
                .alpha(glowPulse * 0.8f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.3f),
                            NeonCyan.copy(alpha = 0.05f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // ── Subtle grid pattern overlay ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonViolet.copy(alpha = 0.03f),
                            NeonPurpleDark.copy(alpha = 0.02f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // ── Main content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cognia branding
            Text(
                text = "Cognia",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    letterSpacing = (-2).sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                color = NeonPurpleBright,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Learn. Play. Grow.",
                style = MaterialTheme.typography.titleLarge,
                color = NeonPurple.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bite-sized educational videos\npowered by community and curiosity",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Buttons — constrained width for desktop
            Column(
                modifier = Modifier.widthIn(max = 380.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(
                            listOf(
                                NeonPurple.copy(alpha = 0.5f),
                                NeonPurpleDark.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        "I Already Have an Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonPurpleBright,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Feature highlights
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeatureHighlight(emoji = "🎓", text = "Learn")
                FeatureHighlight(emoji = "🧠", text = "Quiz")
                FeatureHighlight(emoji = "🚀", text = "Grow")
            }
        }
    }
}

@Composable
private fun FeatureHighlight(emoji: String, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = NeonPurple.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
        )
    }
}
