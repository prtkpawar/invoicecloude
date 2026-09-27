package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BizNavy
import com.example.ui.theme.BizBorder
import com.example.ui.theme.BizGray
import com.example.ui.theme.BizGreen
import com.example.ui.theme.BizPrimary

enum class AccordionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    OPTIONAL
}

/**
 * GO-LIVE EDITION — compact accordion card.
 * Fixes vs old version:
 *  - ~25% less vertical space (tighter paddings/fonts)
 *  - accents unified to black/white theme
 */
@Composable
fun AccordionFormSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    stepNumber: Int? = null,
    subtitle: String? = null,
    summary: String? = null,
    status: AccordionStatus = AccordionStatus.NOT_STARTED,
    badgeText: String? = null,
    headerTrailing: (@Composable () -> Unit)? = null,
    nextActionLabel: String? = null,
    onNextAction: (() -> Unit)? = null,
    containerColor: Color = Color.White,
    accentColor: Color = BizNavy,
    content: @Composable ColumnScope.() -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "accordion_rotation"
    )

    val isCompleted = status == AccordionStatus.COMPLETED ||
            (!summary.isNullOrBlank() && status != AccordionStatus.NOT_STARTED)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) containerColor else Color.White
        ),
        border = BorderStroke(
            width = if (isExpanded) 1.4.dp else 1.dp,
            color = if (isExpanded) BizNavy.copy(alpha = 0.6f) else BizBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 1.dp else 0.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Compact clickable header
            Surface(
                color = if (isExpanded) BizNavy.copy(alpha = 0.04f) else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        if (isExpanded) RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                        else RoundedCornerShape(14.dp)
                    )
                    .clickable(onClick = onToggle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Step / icon badge (smaller)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> Color(0xFFDCFCE7)
                                        isExpanded -> BizNavy.copy(alpha = 0.1f)
                                        else -> Color(0xFFF4F4F5)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted && !isExpanded) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = BizGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (stepNumber != null) {
                                Text(
                                    text = stepNumber.toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isExpanded) BizNavy else Color(0xFF18181B)
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isExpanded) BizNavy else BizGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111114),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (badgeText != null) {
                                    Surface(
                                        color = BizNavy.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(5.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BizNavy,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }
                            }
                            val stateText = when {
                                !isExpanded && !summary.isNullOrBlank() -> summary
                                !subtitle.isNullOrBlank() -> subtitle
                                else -> null
                            }
                            if (stateText != null) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = stateText,
                                    fontSize = 11.sp,
                                    fontWeight = if (!isExpanded && !summary.isNullOrBlank()) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isCompleted && !isExpanded) Color(0xFF15803D) else BizGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        headerTrailing?.invoke()
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isExpanded) BizNavy.copy(alpha = 0.1f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = if (isExpanded) BizNavy else BizGray,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(rotationState)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(240)) + fadeIn(animationSpec = tween(240)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    content()

                    if (nextActionLabel != null && onNextAction != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BizBorder, thickness = 0.8.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onNextAction,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = nextActionLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BizNavy,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * FIXED control header — the old one squeezed "Expand All" into a
 * vertical letter-stack. Title now takes one non-wrapping line and the
 * button always has room.
 */
@Composable
fun AccordionFormControlHeader(
    totalSections: Int,
    completedSections: Int,
    allExpanded: Boolean,
    onToggleExpandAll: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Form Sections"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BizBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title block — constrained to one line, shares row with button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF111114),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$completedSections/$totalSections",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (completedSections == totalSections) BizGreen else BizPrimary,
                    maxLines = 1
                )
            }

            TextButton(
                onClick = onToggleExpandAll,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = BizNavy,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (allExpanded) "Collapse" else "Expand All",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BizNavy,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (totalSections > 0) {
            val progress = (completedSections.toFloat() / totalSections.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (completedSections == totalSections) BizGreen else BizNavy,
                trackColor = Color(0xFFF1F5F9),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
