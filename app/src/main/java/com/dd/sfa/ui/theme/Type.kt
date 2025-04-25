package com.dd.sfa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We don't have the actual Montserrat TTF/OTF files here,
// so we use a fallback to a built-in sans-serif style.
val MontserratFamily = FontFamily.SansSerif

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
    // Add more styles as needed
)
