package com.cognia.app.ui.social

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isFollowing) {
        OutlinedButton(
            onClick = onUnfollowClick,
            enabled = !isLoading,
            modifier = modifier
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text("Following")
            }
        }
    } else {
        Button(
            onClick = onFollowClick,
            enabled = !isLoading,
            modifier = modifier
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Follow")
            }
        }
    }
}
