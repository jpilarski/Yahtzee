package com.example.yahtzeegame.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val YahtzeeTypography = Typography(
    labelSmall = TextStyle( // small text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp
    ), bodyMedium = TextStyle( // normal text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp
    ), labelLarge = TextStyle( // button text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 16.sp
    ), titleLarge = TextStyle( // big text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 20.sp
    ), displayLarge = TextStyle( // header text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp
    )
)
