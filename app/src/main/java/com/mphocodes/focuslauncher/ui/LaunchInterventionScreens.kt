@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.mphocodes.focuslauncher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import kotlinx.coroutines.delay
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusBackground

@Composable
internal fun IntentPromptScreen(
    uiState: LauncherUiState,
    app: InstalledApp,
    onContinue: (String) -> Unit,
    onCancel: () -> Unit,
    onMenuClick: () -> Unit
) {
    var remaining by remember(app.id, uiState.settings.focusPauseSeconds) {
        mutableIntStateOf(uiState.settings.focusPauseSeconds)
    }
    LaunchedEffect(app.id, uiState.settings.focusPauseSeconds) {
        remaining = uiState.settings.focusPauseSeconds
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var typedIntent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current.copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current, CircleShape)
            )
        }

        Spacer(Modifier.height(16.dp))
        FocusText(
            text = stringResource(R.string.intent_prompt_title, appDisplayLabel(app, uiState.settings)),
            color = LocalFocusPrimary.current,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 25.sp
        )
        Spacer(Modifier.height(6.dp))
        FocusText(
            text = stringResource(R.string.intent_prompt_type_unlock),
            color = FocusMuted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(22.dp))

        BasicTextField(
            value = typedIntent,
            onValueChange = { typedIntent = it },
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusSecondary)
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (typedIntent.isEmpty()) {
                    FocusText(stringResource(R.string.intent_prompt_placeholder), color = FocusSecondary, fontSize = 16.sp)
                }
                innerTextField()
            }
        )
        Spacer(Modifier.height(16.dp))
        
        FocusText(text = stringResource(R.string.intent_prompt_recent), color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth()) {
            uiState.settings.intentLogs.take(3).forEach { log ->
                AppRow(label = log, onClick = { typedIntent = log })
            }
        }
        Spacer(Modifier.height(32.dp))

        FocusButton(
            text = if (remaining > 0) stringResource(R.string.intent_prompt_btn_wait, remaining) else stringResource(R.string.intent_prompt_btn_continue),
            enabled = remaining <= 0 && typedIntent.isNotBlank(),
            onClick = { onContinue(typedIntent) }
        )
        Spacer(Modifier.height(10.dp))
        GhostButton(text = stringResource(R.string.intent_prompt_btn_cancel), onClick = onCancel)
    }
}

@Composable
internal fun QuotaBlockScreen(
    uiState: LauncherUiState,
    quotaBlock: QuotaBlock,
    onOpenUsageAccess: () -> Unit,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(42.dp))
        Eyebrow(stringResource(R.string.quota_block_eyebrow))
        Spacer(Modifier.height(10.dp))
        FocusText(
            text = if (quotaBlock.requiresUsageAccess) {
                stringResource(R.string.quota_block_usage_title)
            } else {
                stringResource(R.string.quota_block_title, appDisplayLabel(quotaBlock.app, uiState.settings))
            },
            color = LocalFocusPrimary.current,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 29.sp
        )
        Spacer(Modifier.height(12.dp))
        FocusText(
            text = if (quotaBlock.requiresUsageAccess) {
                stringResource(R.string.quota_block_usage_desc, quotaBlock.quotaMinutes)
            } else {
                stringResource(
                    R.string.quota_block_desc,
                    formatUsageMinutes(quotaBlock.usedMillis),
                    quotaBlock.quotaMinutes
                )
            },
            color = FocusSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(36.dp))
        if (quotaBlock.requiresUsageAccess) {
            FocusButton(text = stringResource(R.string.quota_block_open_usage_access), onClick = onOpenUsageAccess)
            Spacer(Modifier.height(10.dp))
        }
        GhostButton(text = stringResource(R.string.quota_block_back), onClick = onDismiss)
    }
}
