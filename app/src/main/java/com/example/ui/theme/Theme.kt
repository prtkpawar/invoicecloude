package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.ui.theme.pro.AuroraTheme

@Composable
fun SwamiSolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AuroraTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun BizOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AuroraTheme(darkTheme = darkTheme, content = content)
}
