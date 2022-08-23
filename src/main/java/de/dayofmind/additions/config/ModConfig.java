package de.dayofmind.additions.config;

import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.data.ConfigOptions;

import static de.dayofmind.additions.Additions.MOD_ID;

public class ModConfig extends Config {

    public ModConfig() {
        // Replace modid with your mod's ID
        super(ConfigOptions.mod(MOD_ID).branch(new String[]{"config"}).fileHeader("Please edit this config in-game \n Settings must be the same as on the server you are playing on"));
    }

    @Transitive
    public static class ExperimentalSettings implements ConfigGroup {

        @ConfigEntry(requiresRestart = true, comment = "Adds experimental entities like the musican to the game")
        public static boolean ExperimentalEntities;
        @ConfigEntry(requiresRestart = true, comment = "Adds experimental blocks like the guitar to the game")
        public static  boolean ExperimentalBlocks;
    }

}
