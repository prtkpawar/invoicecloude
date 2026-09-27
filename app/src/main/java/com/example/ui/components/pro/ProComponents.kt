package com.example.ui.components.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.pro.*

// ============================================================
// Shared professional components — build once, reuse everywhere
// ============================================================

enum class PayStatus { PAID, PARTIAL, UNPAID, DRAFT }

/** Small status pill — icon + word, never color alone */
@Composable
fun StatusPill(status: PayStatus) {
    val (bg, fg, icon, label) = when (status) {
        PayStatus.PAID    -> Quad(Success50, Success600, Icons.Default.CheckCircle, "Paid")
        PayStatus.PARTIAL -> Quad(Warning50, Warning600, Icons.Default.Timelapse, "Partial")
        PayStatus.UNPAID  -> Quad(Danger50,  Danger600,  Icons.Default.Schedule,  "Unpaid")
        PayStatus.DRAFT   -> Quad(SurfaceSoft, Ink600,   Icons.Default.Description, "Estimate")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

private data class Quad(val bg: Color, val fg: Color, val icon: ImageVector, val label: String)

/** Section header row: "Recent" ———————— "View all" */
@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, style = MaterialTheme.typography.labelMedium, color = Brand600)
            }
        }
    }
}

/** Primary full-width button — the ONLY loud element on screen */
@Composable
fun PrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/** Quiet secondary button */
@Composable
fun GhostButton(text: String, icon: ImageVector? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink900)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Standard card — 16dp radius, hairline border, NO drop shadow */
@Composable
fun ProCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
            .background(SurfaceCard)
            .border(1.dp, Line, MaterialTheme.shapes.large)
            .padding(20.dp),
        content = content
    )
}

/** Avatar circle with initial */
@Composable
fun Avatar(name: String, size: Int = 40, bg: Color = Brand100, fg: Color = Brand700) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(name.firstOrNull()?.uppercase() ?: "?", color = fg, style = MaterialTheme.typography.titleMedium)
    }
}
