package com.naveenapps.expensemanager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette

internal data class JiangJiPaletteTokens(
    val primary: Color,
    val deepPrimary: Color,
    val lightPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val border: Color,
    val mutedGold: Color,
    val income: Color = Color(0xFF688261),
)

private fun JiangJiColorPalette.tokens(isDarkTheme: Boolean): JiangJiPaletteTokens = when (this) {
    JiangJiColorPalette.BURGUNDY -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFD49AA4),
            deepPrimary = Color(0xFF4A101B),
            lightPrimary = Color(0xFFE8B8C0),
            background = Color(0xFF21191A),
            surface = Color(0xFF2A2021),
            surfaceVariant = Color(0xFF403133),
            primaryText = Color(0xFFF7ECE6),
            secondaryText = Color(0xFFD7C3BF),
            border = Color(0xFF594446),
            mutedGold = Color(0xFFD4B982),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF6F1D2C),
            deepPrimary = Color(0xFF4A101B),
            lightPrimary = Color(0xFF9A5862),
            background = Color(0xFFF7F0E5),
            surface = Color(0xFFFFF9F1),
            surfaceVariant = Color(0xFFEFE3D4),
            primaryText = Color(0xFF29211E),
            secondaryText = Color(0xFF786B63),
            border = Color(0xFFDDCEC0),
            mutedGold = Color(0xFFB89A68),
        )
    }

    JiangJiColorPalette.MORANDI_SAGE -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFB7C9BD),
            deepPrimary = Color(0xFF26362E),
            lightPrimary = Color(0xFFD2DED5),
            background = Color(0xFF1D2420),
            surface = Color(0xFF27302B),
            surfaceVariant = Color(0xFF39453E),
            primaryText = Color(0xFFEEF4EF),
            secondaryText = Color(0xFFC4D0C8),
            border = Color(0xFF536259),
            mutedGold = Color(0xFFD0BD8C),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF53665B),
            deepPrimary = Color(0xFF36483F),
            lightPrimary = Color(0xFF7A9182),
            background = Color(0xFFF1F2ED),
            surface = Color(0xFFFBFCF8),
            surfaceVariant = Color(0xFFE1E7E0),
            primaryText = Color(0xFF27302B),
            secondaryText = Color(0xFF69746D),
            border = Color(0xFFCDD5CE),
            mutedGold = Color(0xFFA4936A),
        )
    }

    JiangJiColorPalette.LAVENDER_MIST -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFD2C2DA),
            deepPrimary = Color(0xFF382D40),
            lightPrimary = Color(0xFFE4D9E9),
            background = Color(0xFF241F26),
            surface = Color(0xFF302932),
            surfaceVariant = Color(0xFF473D4A),
            primaryText = Color(0xFFF6EFF8),
            secondaryText = Color(0xFFD8CBDD),
            border = Color(0xFF5F5163),
            mutedGold = Color(0xFFD2B783),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF756A82),
            deepPrimary = Color(0xFF50455D),
            lightPrimary = Color(0xFFA69AAF),
            background = Color(0xFFF2EEF5),
            surface = Color(0xFFFCF9FE),
            surfaceVariant = Color(0xFFE8E1EC),
            primaryText = Color(0xFF302B33),
            secondaryText = Color(0xFF746B77),
            border = Color(0xFFD8CEDD),
            mutedGold = Color(0xFFA89472),
        )
    }

    JiangJiColorPalette.MACARON_MIST -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFD2B0B0),
            deepPrimary = Color(0xFF513A3C),
            lightPrimary = Color(0xFFE3C9C5),
            background = Color(0xFF282122),
            surface = Color(0xFF352B2C),
            surfaceVariant = Color(0xFF4A3A3B),
            primaryText = Color(0xFFF8EFED),
            secondaryText = Color(0xFFD8C5C1),
            border = Color(0xFF685153),
            mutedGold = Color(0xFFD0B48C),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF96737A),
            deepPrimary = Color(0xFF684D55),
            lightPrimary = Color(0xFFB9969B),
            background = Color(0xFFF8F1ED),
            surface = Color(0xFFFFFAF6),
            surfaceVariant = Color(0xFFF0E1DE),
            primaryText = Color(0xFF352B2B),
            secondaryText = Color(0xFF7A6D6B),
            border = Color(0xFFE2D1CE),
            mutedGold = Color(0xFFB69A78),
        )
    }

    JiangJiColorPalette.OAT_CREAM -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFD4AA91),
            deepPrimary = Color(0xFF4A3025),
            lightPrimary = Color(0xFFE4C6B5),
            background = Color(0xFF27221F),
            surface = Color(0xFF332B27),
            surfaceVariant = Color(0xFF4A3D35),
            primaryText = Color(0xFFF7EEE8),
            secondaryText = Color(0xFFD8C6B9),
            border = Color(0xFF68564B),
            mutedGold = Color(0xFFD2B681),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF8A6652),
            deepPrimary = Color(0xFF5C4032),
            lightPrimary = Color(0xFFB28D77),
            background = Color(0xFFF5F0E8),
            surface = Color(0xFFFFFBF5),
            surfaceVariant = Color(0xFFEAE0D2),
            primaryText = Color(0xFF322923),
            secondaryText = Color(0xFF76675D),
            border = Color(0xFFDCD0C1),
            mutedGold = Color(0xFFA68D67),
        )
    }

    JiangJiColorPalette.DUSTY_ROSE -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFD2A0AA),
            deepPrimary = Color(0xFF4D2F38),
            lightPrimary = Color(0xFFE3BCC3),
            background = Color(0xFF281F21),
            surface = Color(0xFF34282B),
            surfaceVariant = Color(0xFF49383C),
            primaryText = Color(0xFFF8EEF0),
            secondaryText = Color(0xFFD9C5C9),
            border = Color(0xFF674E54),
            mutedGold = Color(0xFFD1B188),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF925C67),
            deepPrimary = Color(0xFF633B46),
            lightPrimary = Color(0xFFBA8791),
            background = Color(0xFFF6EFEE),
            surface = Color(0xFFFFF9F8),
            surfaceVariant = Color(0xFFEBDDDC),
            primaryText = Color(0xFF332A2B),
            secondaryText = Color(0xFF78696B),
            border = Color(0xFFDCCBCB),
            mutedGold = Color(0xFFA88D75),
        )
    }

    JiangJiColorPalette.MISTY_SLATE -> if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = Color(0xFFAFC3D0),
            deepPrimary = Color(0xFF2B3D49),
            lightPrimary = Color(0xFFC9D7E0),
            background = Color(0xFF1D2327),
            surface = Color(0xFF273036),
            surfaceVariant = Color(0xFF39454D),
            primaryText = Color(0xFFEEF4F7),
            secondaryText = Color(0xFFC4D0D6),
            border = Color(0xFF53636D),
            mutedGold = Color(0xFFD0B98B),
        )
    } else {
        JiangJiPaletteTokens(
            primary = Color(0xFF516879),
            deepPrimary = Color(0xFF354957),
            lightPrimary = Color(0xFF8298A7),
            background = Color(0xFFEEF1F3),
            surface = Color(0xFFFAFCFD),
            surfaceVariant = Color(0xFFDFE6EA),
            primaryText = Color(0xFF273239),
            secondaryText = Color(0xFF67737B),
            border = Color(0xFFCBD5DB),
            mutedGold = Color(0xFFA18F70),
        )
    }

    JiangJiColorPalette.CUSTOM -> customTokens("#6F1D2C", isDarkTheme)
}

private fun customTokens(hex: String, isDarkTheme: Boolean): JiangJiPaletteTokens {
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF6F1D2C))
    return if (isDarkTheme) {
        JiangJiPaletteTokens(
            primary = baseColor.mixWith(Color.White, 0.35f),
            deepPrimary = baseColor.mixWith(Color.Black, 0.4f),
            lightPrimary = baseColor.mixWith(Color.White, 0.55f),
            background = Color(0xFF1E1E1E),
            surface = Color(0xFF262626),
            surfaceVariant = Color(0xFF383838),
            primaryText = Color(0xFFF7ECE6),
            secondaryText = Color(0xFFD7C3BF),
            border = Color(0xFF4C4C4C),
            mutedGold = Color(0xFFD4B982),
        )
    } else {
        JiangJiPaletteTokens(
            primary = baseColor,
            deepPrimary = baseColor.mixWith(Color.Black, 0.3f),
            lightPrimary = baseColor.mixWith(Color.White, 0.3f),
            background = Color(0xFFF7F5F2),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFECE8),
            primaryText = Color(0xFF24201E),
            secondaryText = Color(0xFF756D67),
            border = Color(0xFFDDD7D0),
            mutedGold = Color(0xFFB89A68),
        )
    }
}

/** Brand tokens backed by the currently selected curated palette. */
object JiangJiColors {
    private var activeTokens = JiangJiColorPalette.BURGUNDY.tokens(isDarkTheme = false)

    internal fun apply(palette: JiangJiColorPalette, isDarkTheme: Boolean) {
        activeTokens = palette.tokens(isDarkTheme)
    }

    internal fun applyTokens(tokens: JiangJiPaletteTokens) {
        activeTokens = tokens
    }

    val Burgundy: Color get() = activeTokens.primary
    val DeepBurgundy: Color get() = activeTokens.deepPrimary
    val LightBurgundy: Color get() = activeTokens.lightPrimary
    val Ivory: Color get() = activeTokens.background
    val WarmSurface: Color get() = activeTokens.surface
    val SecondarySurface: Color get() = activeTokens.surfaceVariant
    val PrimaryText: Color get() = activeTokens.primaryText
    val SecondaryText: Color get() = activeTokens.secondaryText
    val Border: Color get() = activeTokens.border
    val MutedGold: Color get() = activeTokens.mutedGold
    // Income remains green in every palette because it carries transaction semantics.
    val Income: Color get() = activeTokens.income
}

object JiangJiMotion {
    const val Fast = 120
    const val Normal = 220
    const val Slow = 360
}

private fun Color.mixWith(other: Color, amount: Float): Color = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha + (other.alpha - alpha) * amount,
)

private fun lightColorSchemeFor(tokens: JiangJiPaletteTokens) = lightColorScheme(
    primary = tokens.primary,
    onPrimary = if (tokens.primary.luminance() > 0.179f) Color.Black else Color.White,
    primaryContainer = tokens.background.mixWith(tokens.primary, amount = 0.13f),
    onPrimaryContainer = tokens.deepPrimary,
    secondary = tokens.mutedGold,
    onSecondary = Color.White,
    secondaryContainer = tokens.background.mixWith(tokens.mutedGold, amount = 0.18f),
    onSecondaryContainer = tokens.primaryText,
    background = tokens.background,
    onBackground = tokens.primaryText,
    surface = tokens.surface,
    onSurface = tokens.primaryText,
    surfaceVariant = tokens.surfaceVariant,
    onSurfaceVariant = tokens.secondaryText,
    outline = tokens.border,
    outlineVariant = tokens.border,
    error = tokens.deepPrimary,
    onError = Color.White,
)

private fun darkColorSchemeFor(tokens: JiangJiPaletteTokens) = darkColorScheme(
    primary = tokens.primary,
    onPrimary = tokens.deepPrimary,
    primaryContainer = tokens.deepPrimary,
    onPrimaryContainer = tokens.lightPrimary,
    secondary = tokens.mutedGold,
    onSecondary = tokens.deepPrimary,
    secondaryContainer = tokens.background.mixWith(tokens.mutedGold, amount = 0.24f),
    onSecondaryContainer = tokens.primaryText,
    background = tokens.background,
    onBackground = tokens.primaryText,
    surface = tokens.surface,
    onSurface = tokens.primaryText,
    surfaceVariant = tokens.surfaceVariant,
    onSurfaceVariant = tokens.secondaryText,
    outline = tokens.border,
    outlineVariant = tokens.border,
    error = tokens.lightPrimary,
    onError = tokens.deepPrimary,
)

private val JiangJiTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
        ),
        headlineSmall = headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
        ),
        titleLarge = titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
        ),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp),
        labelSmall = labelSmall.copy(fontSize = 12.sp),
    )
}

private val JiangJiShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun JiangJiTheme(
    isDarkTheme: Boolean,
    palette: JiangJiColorPalette = JiangJiColorPalette.BURGUNDY,
    customColorHex: String? = null,
    hasBackgroundImage: Boolean = false,
    content: @Composable () -> Unit,
) {
    val paletteTokens = if (palette == JiangJiColorPalette.CUSTOM) {
        customTokens(customColorHex ?: "#6F1D2C", isDarkTheme)
    } else {
        palette.tokens(isDarkTheme)
    }
    JiangJiColors.applyTokens(paletteTokens)
    MaterialTheme(
        colorScheme = if (isDarkTheme) {
            darkColorSchemeFor(paletteTokens)
        } else {
            lightColorSchemeFor(paletteTokens)
        }.let { scheme ->
            if (hasBackgroundImage) scheme.copy(background = Color.Transparent) else scheme
        },
        typography = JiangJiTypography,
        shapes = JiangJiShapes,
        content = content,
    )
}
