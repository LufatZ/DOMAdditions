package de.additions.config

import eu.midnightdust.lib.config.MidnightConfig

class AdditionsConfig : MidnightConfig() {
    companion object {
        enum class TranslationVersion {
            CROWDIN,OXFATECH, CUSTOM
        }
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
        var TranslationUrl: TranslationVersion = TranslationVersion.OXFATECH

        @JvmField
        @Entry(category = "features")
        var TranslationUrlCustom: String = "https://example.com/translationFileName.zip"

        @JvmField
        @Entry(category = "features")
        var EnabledTrapdoor: Boolean = true

    }
}