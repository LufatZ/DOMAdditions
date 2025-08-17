package de.additions.config

import eu.midnightdust.lib.config.MidnightConfig

class AdditionsConfig : MidnightConfig() {
    companion object {

        @JvmField
        @Comment(category = "about", centered = true)
        var DayOfMind: Comment? = null

        @JvmField
        @Comment(category = "about")
        var aboutDayOfMind: Comment? = null

        @JvmField
        @Comment(category = "features", centered = true)
        var features: Comment? = null

        @JvmField
        @Entry(category = "features")
        var EnabledShovelMixin: Boolean = true

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
        var TranslationCounter: Int = 3

        @JvmField
        @Entry(category = "features")
        var DetailedLogging: Boolean = false

        @JvmField
        @Entry(category = "features")
        var EnabledTrapdoor: Boolean = true
    }
}
