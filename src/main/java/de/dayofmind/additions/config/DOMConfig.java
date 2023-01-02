package de.dayofmind.additions.config;

import de.dayofmind.additions.Additions;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.data.ConfigOptions;

public class DOMConfig extends Config {
    public DOMConfig() {
        super(ConfigOptions
                .mod(Additions.MOD_ID)
                .fileHeader("Please edit this settings in Minecraft")
                .branch(new String[]{"dayofmind"})
        );
    }
    @Transitive
    public static class ExperimentalSettings implements ConfigGroup {

        //@ConfigEntry(comment = "This enables the wandering Musican")
        //boolean EnabledMusican;
        @ConfigEntry(comment = "This enables the Guitar Block and item")
        public static boolean EnabledGuitar;

    }
    @Transitive
    public static class Features implements ConfigGroup {
        @ConfigEntry(comment = "This adds more lantern variants")
        public static boolean EnabledLantern = true;
        @ConfigEntry(comment = "This adds more dirt variants")
        public static boolean EnabledDirt = true;
        @ConfigEntry(comment = "This adds more grass variants")
        public static boolean EnabledGrass = true;
        @ConfigEntry(comment = "This adds more dirt path variants")
        public static boolean EnabledDirtPath = true;
        @ConfigEntry(comment = "This adds more gold variants")
        public static boolean EnabledGold = true;
        @ConfigEntry(comment = "This adds more iron variants")
        public static boolean EnabledIron = true;
        @ConfigEntry(comment = "This adds more diamond variants")
        public static boolean EnabledDia = true;
        @ConfigEntry(comment = "This adds more basalt variants")
        public static boolean EnabledBasalt = true;
        @ConfigEntry(comment = "This adds more magma variants")
        public static boolean EnabledMagma = true;
        @ConfigEntry(comment = "This adds more obsidian variants")
        public static boolean EnabledObsidian = true;
    }
    @Transitive
    public static class ModSettings implements ConfigGroup {
        @ConfigEntry(comment = "This enables the automatic download of additional or updated language files (crowdin)", requiresRestart = true)
        public static boolean EnabledTranslation = true;
    }
}