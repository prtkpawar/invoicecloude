package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Canonical data-entry scaffold: edge-to-edge safe, keyboard-aware.
 * Use this around every form screen in the app.
 *
 * Features:
 * - imePadding() ensures content resizes when software keyboard appears
 * - Bottom bar (e.g., Save button) always stays visible above the keyboard
 * - LazyColumn with proper weight so it scrolls independently
 */
@Composable
fun FormScaffold(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    bottomBar: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            content = content
        )
        // Primary action ALWAYS visible above the keyboard
        Surface(
            shadowElevation = 8.dp,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            bottomBar()
        }
    }
}
