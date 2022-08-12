package de.dayofmind.additions.config;

import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.data.ConfigOptions;
import me.lortseam.completeconfig.gui.ConfigScreenBuilder;
import me.lortseam.completeconfig.gui.cloth.ClothConfigScreenBuilder;

import static de.dayofmind.additions.Additions.MOD_ID;

public class ModConfig extends Config {

    public ModConfig() {
        // Replace modid with your mod's ID
        super(ConfigOptions.mod(MOD_ID).branch(new String[]{"config"}).fileHeader("Please edit this config in-game"));
    }
    @Transitive
    @ConfigEntries(includeAll = true)
    public static class ExperimentalSettings implements ConfigGroup {
        @ConfigEntry(requiresRestart = true)
        public static boolean ExperimentalEntities;
        @ConfigEntry(requiresRestart = true)
        public static  boolean ExperimentalBlocks;
    }

}
