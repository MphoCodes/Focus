package com.mphocodes.focuslauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.FocusInput
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun DisguiseRow(app: InstalledApp, value: String, onValueChange: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    val isModified = draft != value
    val isDuplicate = draft.trim().equals(app.label.trim(), ignoreCase = true)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        FocusText(
            text = app.label,
            color = FocusSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            singleLine = true,
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(FocusInput)
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        if (draft.isBlank()) {
                            FocusText(
                                text = stringResource(R.string.disguises_placeholder),
                                color = FocusMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                    if (isModified) {
                        FocusText(
                            text = "SAVE",
                            color = if (isDuplicate) FocusMuted else LocalFocusPrimary.current,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = !isDuplicate) { onValueChange(draft) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    } else if (value.isNotBlank()) {
                        FocusText(
                            text = "CLEAR",
                            color = FocusMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onValueChange("") }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        )
        if (isModified && isDuplicate) {
            FocusText(
                text = "Alias matches original name",
                color = FocusSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
internal fun QuotaRow(label: String, minutes: Int, usedMillis: Long = 0L, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        val usageText = if (minutes <= 0) {
            stringResource(R.string.quotas_off)
        } else {
            val usedMinutes = formatUsageMinutes(usedMillis)
            "$usedMinutes / ${stringResource(R.string.quotas_minutes, minutes)}"
        }
        FocusText(
            text = usageText,
            color = if (minutes <= 0) FocusMuted else LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}
@Composable
internal fun ProfileWifiField(label: String, value: String, onValueChange: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    val isModified = draft != value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        FocusText(
            text = label,
            color = FocusSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            singleLine = true,
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(FocusInput)
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        if (draft.isBlank()) {
                            FocusText(
                                text = stringResource(R.string.profiles_wifi_placeholder),
                                color = FocusMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                    if (isModified) {
                        FocusText(
                            text = "SAVE",
                            color = LocalFocusPrimary.current,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onValueChange(draft) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    } else if (value.isNotBlank()) {
                        FocusText(
                            text = "CLEAR",
                            color = FocusMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onValueChange("") }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
internal fun PrivacyTextBlock(heading: String, body: String) {
    SettingsSection(heading)
    FocusText(
        text = body,
        color = FocusSecondary,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(22.dp))
}
