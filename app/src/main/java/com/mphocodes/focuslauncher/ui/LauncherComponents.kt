@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.mphocodes.focuslauncher.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.Workspace
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.combinedClickable
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.FocusDivider
import com.mphocodes.focuslauncher.ui.theme.FocusInput
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusBackground
import com.mphocodes.focuslauncher.ui.theme.LocalTextSizeMultiplier

@Composable
internal fun FocusShell(
    selected: LauncherScreen,
    selectedScreen: LauncherScreen,
    showBack: Boolean = false,
    backTarget: LauncherScreen = LauncherScreen.Settings,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Header(
            selectedScreen = selectedScreen,
            showBack = showBack,
            onBack = { onNavigate(backTarget) },
            onMenuClick = onMenuClick
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            content()
        }
        BottomNav(selected = selected, onNavigate = onNavigate)
    }
}

@Composable
private fun Header(
    selectedScreen: LauncherScreen,
    showBack: Boolean,
    onBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    val systemTime = remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))) }
    if (selectedScreen == LauncherScreen.Apps) {
        LaunchedEffect(Unit) {
            while (true) {
                systemTime.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
                delay(30_000)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedScreen == LauncherScreen.DefaultHome) {
            FocusText(
                text = stringResource(R.string.app_logo),
                color = LocalFocusPrimary.current,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = stringResource(R.string.cd_menu),
                tint = LocalFocusPrimary.current,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onMenuClick() }
            )
        } else {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (showBack) {
                    FocusText(
                        text = stringResource(R.string.header_back),
                        color = LocalFocusPrimary.current,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onBack() }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.cd_menu),
                        tint = LocalFocusPrimary.current,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onMenuClick() }
                    )
                }
            }

            FocusText(
                text = stringResource(R.string.app_logo),
                color = LocalFocusPrimary.current,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (selectedScreen == LauncherScreen.Apps) {
                    FocusText(
                        text = systemTime.value.uppercase(),
                        color = FocusSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceTileGrid(
    workspaces: List<Workspace>,
    onOpenWorkspace: (Workspace) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        gridItems(workspaces, key = { it.id }) { workspace ->
            WorkspaceTileButton(
                workspace = workspace,
                onClick = { onOpenWorkspace(workspace) }
            )
        }
    }
}

@Composable
private fun WorkspaceTileButton(workspace: Workspace, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkspaceTile(
            workspace = workspace,
            modifier = Modifier
                .size(72.dp)
        )
        Spacer(Modifier.height(8.dp))
        FocusText(
            text = workspace.name,
            color = LocalFocusPrimary.current,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun WorkspaceTile(workspace: Workspace, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, FocusDivider), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        FocusText(
            text = workspace.letter,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun TopMark(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.cd_menu),
            tint = LocalFocusPrimary.current,
            modifier = Modifier
                .size(18.dp)
                .clickable { onMenuClick() }
        )
        FocusText(
            text = stringResource(R.string.app_logo),
            color = LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(18.dp)) // Equal width to balance the hamburger icon
    }
}

@Composable
private fun BottomNav(selected: LauncherScreen, onNavigate: (LauncherScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(LocalFocusBackground.current)
            .border(BorderStroke(0.5.dp, FocusDivider))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            label = stringResource(R.string.nav_home),
            icon = Icons.Outlined.Home,
            selected = selected == LauncherScreen.Home
        ) { onNavigate(LauncherScreen.Home) }

        NavItem(
            label = stringResource(R.string.nav_apps),
            icon = Icons.Outlined.Apps,
            selected = selected == LauncherScreen.Apps
        ) { onNavigate(LauncherScreen.Apps) }

        NavItem(
            label = stringResource(R.string.nav_settings),
            icon = Icons.Outlined.Settings,
            selected = selected == LauncherScreen.Settings
        ) { onNavigate(LauncherScreen.Settings) }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) LocalFocusPrimary.current else FocusMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        FocusText(
            text = label,
            color = if (selected) LocalFocusPrimary.current else FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun SearchInput(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val multiplier = LocalTextSizeMultiplier.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = (14 * multiplier).sp, fontFamily = FontFamily.Monospace),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(FocusInput)
            .padding(horizontal = 16.dp),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        FocusText(
                            text = placeholder,
                            color = FocusMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
internal fun AppRow(label: String, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun CheckRow(label: String, selected: Boolean, muted: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(
            text = label,
            color = if (muted) FocusMuted else LocalFocusPrimary.current,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (selected) LocalFocusPrimary.current else Color.Transparent)
                .border(BorderStroke(1.dp, if (selected) LocalFocusPrimary.current else FocusMuted))
        )
    }
}
@Composable
internal fun SettingRow(label: String, value: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        FocusText(
            text = value ?: if (onClick != null) ">" else "",
            color = FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
internal fun ToggleRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(text = label, color = LocalFocusPrimary.current, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(14.dp)
                .border(BorderStroke(1.dp, FocusMuted))
                .padding(2.dp),
            contentAlignment = if (selected) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (selected) LocalFocusPrimary.current else FocusMuted)
            )
        }
    }
}

@Composable
internal fun StepRow(number: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        FocusText(text = number, color = FocusMuted, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        FocusText(text = text, color = LocalFocusPrimary.current, fontSize = 16.sp)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StaticReasonRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusText(text = text, color = FocusSecondary, fontSize = 14.sp)
    }
}

@Composable
internal fun FocusButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(if (enabled) LocalFocusPrimary.current else FocusMuted)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FocusText(
            text = text,
            color = if (enabled) Color.Black else FocusSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
internal fun GhostButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(BorderStroke(1.dp, FocusDivider))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FocusText(
            text = text,
            color = LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
internal fun SettingsSection(text: String) {
    FocusText(
        text = text,
        color = FocusMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
internal fun Eyebrow(text: String) {
    FocusText(
        text = text,
        color = FocusMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
}

@Composable
internal fun MutedEmptyText(text: String) {
    FocusText(
        text = text,
        color = FocusMuted,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
internal fun FocusText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    val multiplier = LocalTextSizeMultiplier.current
    val scaledFontSize = if (fontSize.isSp) {
        (fontSize.value * multiplier).sp
    } else {
        fontSize
    }
    val scaledLineHeight = if (lineHeight.isSp) {
        (lineHeight.value * multiplier).sp
    } else {
        lineHeight
    }
    val scaledStyle = if (style.fontSize.isSp) {
        style.copy(
            fontSize = (style.fontSize.value * multiplier).sp,
            lineHeight = if (style.lineHeight.isSp) (style.lineHeight.value * multiplier).sp else style.lineHeight
        )
    } else {
        style
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = scaledFontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = scaledLineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = scaledStyle
    )
}
