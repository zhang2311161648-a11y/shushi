package com.naveenapps.expensemanager.core.model

/**
 * Curated, low-saturation color palettes for ShuShi.
 *
 * The palette is intentionally an enum instead of arbitrary user-entered colors so every
 * combination keeps sufficient contrast and preserves the meaning of income/outgo colors.
 */
enum class JiangJiColorPalette(val id: String) {
    BURGUNDY("burgundy"),
    MORANDI_SAGE("morandi_sage"),
    LAVENDER_MIST("lavender_mist"),
    MACARON_MIST("macaron_mist"),
    OAT_CREAM("oat_cream"),
    DUSTY_ROSE("dusty_rose"),
    MISTY_SLATE("misty_slate"),
    CUSTOM("custom"),
    ;

    companion object {
        fun fromId(id: String?): JiangJiColorPalette =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BURGUNDY
    }
}
