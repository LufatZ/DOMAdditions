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
        @ConfigEntry(comment = "This enables the Guitar Block and item", requiresRestart = true)
        public static boolean EnabledGuitar;

    }
    @Transitive
    public static class Features implements ConfigGroup {
        @ConfigEntry(comment = "This adds more lantern variants", requiresRestart = true)
        public static boolean EnabledLantern = true;
        @ConfigEntry(comment = "This adds redstone lanterns and redstone chain", requiresRestart = true)
        public static boolean EnabledRedstoneLantern = true;
        @ConfigEntry(comment = "This adds more dirt variants", requiresRestart = true)
        public static boolean EnabledDirt = true;
        @ConfigEntry(comment = "This adds more grass variants", requiresRestart = true)
        public static boolean EnabledGrass = true;
        @ConfigEntry(comment = "This adds more dirt path variants", requiresRestart = true)
        public static boolean EnabledDirtPath = true;
        @ConfigEntry(comment = "This adds more gold variants", requiresRestart = true)
        public static boolean EnabledGold = true;
        @ConfigEntry(comment = "This adds more iron variants", requiresRestart = true)
        public static boolean EnabledIron = true;
        @ConfigEntry(comment = "This adds more diamond variants", requiresRestart = true)
        public static boolean EnabledDia = true;
        @ConfigEntry(comment = "This adds more basalt variants", requiresRestart = true)
        public static boolean EnabledBasalt = true;
        @ConfigEntry(comment = "This adds more magma variants", requiresRestart = true)
        public static boolean EnabledMagma = true;
        @ConfigEntry(comment = "This adds more obsidian variants", requiresRestart = true)
        public static boolean EnabledObsidian = true;
        @ConfigEntry(comment = "This adds decorative trapdoor variants", requiresRestart = true)
        public static boolean EnabledDecorativeTrapdoor = true;
    }
    @Transitive
    public static class ModSettings implements ConfigGroup {
        @ConfigEntry(comment = "This enables the automatic download of additional or updated language files (crowdin)", requiresRestart = true)
        public static boolean EnabledTranslation = true;
    }
}