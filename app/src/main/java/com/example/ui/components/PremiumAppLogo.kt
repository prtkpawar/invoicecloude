package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Premium BillBook Brand Logo component.
 * Organizes the official circular emblem with exact sizing, scaling, and full visibility.
 */
@Composable
fun PremiumAppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showText: Boolean = false,
    subtitle: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Full unclipped BillBook Logo emblem
        Image(
            painter = painterResource(id = R.drawable.ic_billbook_logo),
            contentDescription = "BillBook Official Logo",
            modifier = Modifier
                .size(size),
            contentScale = ContentScale.Fit
        )

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BillBook",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GST PRO",
                        fontWeight = FontWeight.Black,
                        fontSize = 9.5.sp,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = subtitle ?: "Smart Invoicing & Business OS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
