package com.cognia.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Step indicator
        StepIndicator(
            currentStep = state.currentStep,
            totalSteps = 3,
            modifier = Modifier
                .padding(top = 20.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Step content
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> SelfDescriptionScreen(
                    selfDescription = state.selfDescription,
                    onDescriptionChanged = viewModel::updateSelfDescription,
                    onContinue = viewModel::nextStep
                )
                1 -> CategoryRecommendationScreen(
                    availableCategories = state.availableCategories,
                    selectedCategoryIds = state.selectedCategoryIds,
                    isLoading = state.isLoadingRecommendations,
                    onToggleCategory = viewModel::toggleCategory,
                    onContinue = viewModel::nextStep,
                    onBack = viewModel::previousStep
                )
                2 -> {
                    val selectedCategories = state.availableCategories.filter {
                        it.id in state.selectedCategoryIds
                    }
                    ConfirmationScreen(
                        selfDescription = state.selfDescription,
                        selectedCategories = selectedCategories,
                        isSaving = state.isSaving,
                        onConfirm = viewModel::savePreferences,
                        onBack = viewModel::previousStep
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            Box(
                modifier = Modifier
                    .then(
                        if (index == currentStep) Modifier.width(24.dp).height(10.dp)
                        else Modifier.size(10.dp)
                    )
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isActive) NeonPurple
                        else NeonPurple.copy(alpha = 0.2f)
                    )
            )
        }
    }
}
