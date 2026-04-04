package com.cognia.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognia.app.ui.theme.BackgroundDark
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonViolet

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        NeonViolet.copy(alpha = 0.1f),
                        NeonPurpleDark.copy(alpha = 0.06f),
                        BackgroundDark,
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cognia",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 52.sp,
                    letterSpacing = (-1.5).sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                color = NeonPurpleBright,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Learn. Play. Grow.",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )

            Spacer(modifier = Modifier.height(72.dp))

            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "Sign Up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.5f), NeonPurpleDark.copy(alpha = 0.3f)))
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "Log In",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonPurpleBright,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
