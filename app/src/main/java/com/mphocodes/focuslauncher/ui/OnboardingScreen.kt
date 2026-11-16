package com.mphocodes.focuslauncher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.FocusDivider
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusBackground

private data class WalkthroughStep(
    val visualRes: Int,
    val titleRes: Int,
    val bodyRes: Int,
    val bulletRes: List<Int>
)

@Composable
internal fun OnboardingScreen(onGetStarted: () -> Unit, onMenuClick: () -> Unit) {
    var showSplash by remember { mutableStateOf(true) }
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            WalkthroughStep(
                visualRes = R.drawable.onboarding_visual_home,
                titleRes = R.string.onboarding_home_title,
                bodyRes = R.string.onboarding_home_body,
                bulletRes = listOf(
                    R.string.onboarding_home_point_1,
                    R.string.onboarding_home_point_2,
                    R.string.onboarding_home_point_3
                )
            ),
            WalkthroughStep(
                visualRes = R.drawable.onboarding_visual_friction,
                titleRes = R.string.onboarding_friction_title,
                bodyRes = R.string.onboarding_friction_body,
                bulletRes = listOf(
                    R.string.onboarding_friction_point_1,
                    R.string.onboarding_friction_point_2,
                    R.string.onboarding_friction_point_3
                )
            ),
            WalkthroughStep(
                visualRes = R.drawable.onboarding_visual_home,
                titleRes = R.string.onboarding_profiles_title,
                bodyRes = R.string.onboarding_profiles_body,
                bulletRes = listOf(
                    R.string.onboarding_profiles_point_1,
                    R.string.onboarding_profiles_point_2,
                    R.string.onboarding_profiles_point_3
                )
            ),
            WalkthroughStep(
                visualRes = R.drawable.onboarding_visual_privacy,
                titleRes = R.string.onboarding_privacy_title,
                bodyRes = R.string.onboarding_privacy_body,
                bulletRes = listOf(
                    R.string.onboarding_privacy_point_1,
                    R.string.onboarding_privacy_point_2,
                    R.string.onboarding_privacy_point_3
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(1_350)
        showSplash = false
    }

    if (showSplash) {
        OnboardingSplash(onMenuClick = onMenuClick)
        return
    }

    val step = steps[stepIndex]
    val transition = rememberInfiniteTransition(label = "onboarding-visual")
    val visualScale by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding-visual-scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusText(
                text = stringResource(R.string.onboarding_step_count, stepIndex + 1, steps.size),
                color = FocusMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
            WalkthroughProgress(current = stepIndex, total = steps.size)
        }
        Spacer(Modifier.height(18.dp))
        Image(
            painter = painterResource(step.visualRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer(scaleX = visualScale, scaleY = visualScale)
                .clip(RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, FocusDivider), RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(24.dp))
        FocusText(
            text = stringResource(step.titleRes),
            color = LocalFocusPrimary.current,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(12.dp))
        FocusText(
            text = stringResource(step.bodyRes),
            color = FocusSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(18.dp))
        step.bulletRes.forEach { bullet ->
            WalkthroughBullet(text = stringResource(bullet))
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.weight(1f))
        if (stepIndex > 0) {
            GhostButton(
                text = stringResource(R.string.onboarding_back),
                onClick = { stepIndex -= 1 }
            )
            Spacer(Modifier.height(12.dp))
        }
        FocusButton(
            text = stringResource(if (stepIndex == steps.lastIndex) R.string.onboarding_finish else R.string.onboarding_next),
            onClick = {
                if (stepIndex == steps.lastIndex) {
                    onGetStarted()
                } else {
                    stepIndex += 1
                }
            }
        )
        if (stepIndex == 0) {
            Spacer(Modifier.height(12.dp))
            GhostButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = onGetStarted
            )
        }
    }
}

@Composable
private fun OnboardingSplash(onMenuClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "onboarding-splash")
    val ringScale by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding-ring-scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(ringScale)
                    .border(BorderStroke(1.dp, FocusDivider), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(LocalFocusPrimary.current, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FocusText(
                    text = "F",
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(34.dp))
        FocusText(
            text = stringResource(R.string.onboarding_splash_eyebrow),
            color = FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        FocusText(
            text = stringResource(R.string.onboarding_splash_title),
            color = LocalFocusPrimary.current,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        FocusText(
            text = stringResource(R.string.onboarding_splash_body),
            color = FocusSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1.2f))
    }
}

@Composable
private fun WalkthroughProgress(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .width(if (index == current) 22.dp else 7.dp)
                    .height(7.dp)
                    .background(
                        color = if (index == current) LocalFocusPrimary.current else FocusDivider,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun WalkthroughBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .background(LocalFocusPrimary.current, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        FocusText(
            text = text,
            color = FocusMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
