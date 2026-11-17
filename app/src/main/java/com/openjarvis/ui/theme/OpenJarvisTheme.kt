package com.openjarvis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object VoidColor {
    val Void950 = Color(0xFF08080F)
    val Void900 = Color(0xFF10101A)
    val Void800 = Color(0xFF191925)
    val Void700 = Color(0xFF242433)
    val Void600 = Color(0xFF343447)
    val Violet = Color(0xFF9B7BFF)
    val VioletDim = Color(0xFF5C4A91)
    val Cyan = Color(0xFF63D8FF)
    val Green = Color(0xFF54D6A0)
    val Amber = Color(0xFFFFC46B)
    val Red = Color(0xFFFF6B7A)
    val TextPrimary = Color(0xFFF4F1FF)
    val TextSecondary = Color(0xFFA8A4B8)
    val TextDisabled = Color(0xFF6D6A7A)
    val BorderSubtle = Color(0xFF333344)
    val BorderGlow = Color(0xFF8064D8)
}

@Composable
fun OpenJarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = VoidColor.Violet,
            secondary = VoidColor.Cyan,
            background = VoidColor.Void950,
            surface = VoidColor.Void900,
            error = VoidColor.Red,
            onPrimary = VoidColor.Void950,
            onSecondary = VoidColor.Void950,
            onBackground = VoidColor.TextPrimary,
            onSurface = VoidColor.TextPrimary,
            onError = VoidColor.Void950
        ),
        content = content
    )
}
