package de.additions.config

import eu.midnightdust.lib.config.MidnightConfig

class AdditionsConfig : MidnightConfig() {
    companion object {
        @JvmField
        @Comment(category = "about")
        var DayOfMind: Comment? = null

        @JvmField
        @Comment(category = "about")
        var aboutDayOfMind: Comment? = null

        @JvmField
        @Comment(category = "features")
        var features: Comment? = null

        @JvmField
        @Entry(category = "features")
        var EnabledBlockVariants: Boolean = true

        @JvmField
        @Entry(category = "features")
        var EnabledLantern: Boolean = true

        @JvmField
        @Entry(category = "features")
        var EnabledRedstoneLantern: Boolean = true

        @JvmField
        @Entry(category = "features")
        var EnabledTranslation: Boolean = true

        @JvmField
        @Entry(category = "features")
        var EnabledDecorativeTrapdoor: Boolean = true

        @JvmField
        @Comment(category = "experimentalSettings")
        var experimental: Comment? = null

        @JvmField
        @Entry(category = "experimentalSettings")
        var EnabledInstruments: Boolean = false
/*
        // Beispiel für einen String-Eintrag
        @Entry(category = "about")
        var ModVersion: String = "1.0.0"
        // Beispiel für einen Int-Eintrag mit Grenzen
        @Entry(category = "features", min = 1.0, max = 100.0)
        var MaxBlockVariants: Int = 10
        // Beispiel für einen Enum-Eintrag
        @Entry(category = "features")
        var LanternType: LanternTypes = LanternTypes.NORMAL
        enum class LanternTypes {
            NORMAL, SOUL, REDSTONE
        }
        // Beispiel für einen Slider
        @Entry(category = "features", isSlider = true, min = 0.0, max = 1.0)
        var LanternBrightness: Float = 0.5f
        // Beispiel für einen Farb-Eintrag
        @JvmField
        @Entry(category = "features", isColor = true)
        var LanternColor: String = "#FFFFFF"
 */
    }
}