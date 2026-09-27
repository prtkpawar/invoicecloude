package com.example.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centralized Snackbar manager to replace Toast.makeText throughout the app.
 *
 * Benefits over Toast:
 * - Consistent positioning (bottom of Scaffold)
 * - Supports "Undo" actions for destructive operations
 * - Dismissible with swipe
 * - Theme-aware (works in dark mode)
 * - Won't overlap with other UI elements
 *
 * Usage:
 * ```
 * val snackbarManager = rememberSnackbarManager()
 *
 * // In your Scaffold:
 * Scaffold(snackbarHost = { SnackbarHost(snackbarManager.hostState) }) { ... }
 *
 * // To show messages:
 * snackbarManager.show(scope, "Invoice saved successfully")
 * snackbarManager.showWithUndo(scope, "Payment deleted") { restorePayment() }
 * ```
 */
class SnackbarManager(val hostState: SnackbarHostState) {

    /**
     * Show a simple informational message.
     */
    fun show(
        scope: CoroutineScope,
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message, duration = duration)
        }
    }

    /**
     * Show a message with an "Undo" action button.
     * Perfect for destructive operations (delete, cancel, etc.)
     *
     * @param onUndo Called when the user taps "Undo"
     */
    fun showWithUndo(
        scope: CoroutineScope,
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
        onUndo: () -> Unit
    ) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = duration,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndo()
            }
        }
    }

    /**
     * Show a success message (e.g., "Invoice created").
     */
    fun showSuccess(scope: CoroutineScope, message: String) {
        show(scope, "✓ $message")
    }

    /**
     * Show an error message (e.g., "Failed to save").
     */
    fun showError(scope: CoroutineScope, message: String) {
        show(scope, "✗ $message", SnackbarDuration.Long)
    }
}

/**
 * Remember a [SnackbarManager] instance for the current composition.
 */
@Composable
fun rememberSnackbarManager(): SnackbarManager {
    val hostState = remember { SnackbarHostState() }
    return remember { SnackbarManager(hostState) }
}
