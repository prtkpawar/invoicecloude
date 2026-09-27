package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BizNavy

/**
 * GO-LIVE RULE: every primary (black) button in the app MUST use this
 * component (or copy its colors). It force-white text so nothing is
 * ever black-on-black again.
 */
@Composable
fun PrimaryBlackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = BizNavy,           // black
            contentColor = Color.White,         // ALWAYS white text/icons
            disabledContainerColor = Color(0xFFD4D4D8),
            disabledContentColor = Color(0xFF71717A)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            color = Color.White,                // hard-forced, theme can't override
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

/** Outlined (white) companion button with black border + black text. */
@Composable
fun SecondaryOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        border = BorderStroke(1.2.dp, if (enabled) BizNavy else Color(0xFFD4D4D8)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BizNavy,
            disabledContentColor = Color(0xFFA1A1AA)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) BizNavy else Color(0xFFA1A1AA),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

/** One text-field style for the whole app: white fill, black focus border. */
@Composable
fun PremiumTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF111114),
    unfocusedTextColor = Color(0xFF111114),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = BizNavy,
    unfocusedBorderColor = Color(0xFFE4E4E7),
    focusedLabelColor = BizNavy,
    unfocusedLabelColor = Color(0xFF71717A),
    cursorColor = BizNavy,
    errorBorderColor = Color(0xFFDC2626)
)

val PremiumTextFieldTextStyle: TextStyle
    @Composable get() = TextStyle(color = Color(0xFF111114), fontSize = 14.sp)

@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BizNavy,
            contentColor = Color.White
        ),
        enabled = enabled,
        content = content
    )
}

@Composable
fun CurrencyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum")
    )
}
