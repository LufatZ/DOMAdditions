package de.dayofmind.additions.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

public class DOMConfigBuilder {
    @Config(name = "additions")
    public static class ModConfig extends PartitioningSerializer.GlobalData {
        @ConfigEntry.Category("experimental")
        @ConfigEntry.Gui.TransitiveObject
        ModuleAConfig ExperimentalFeatures = new ModuleAConfig();
        @ConfigEntry.Category("about_us")
        @ConfigEntry.Gui.TransitiveObject
        ModuleBConfig AboutUs = new ModuleBConfig();
    }

    @Config(name = "experimental")
    static
    class ModuleAConfig implements ConfigData {
        @Tooltip( count = 2)
        boolean Entity = false;
        @Tooltip( count = 2)
        boolean Blocks = false;
    }
    @Config(name = "about_us")
    static
    class ModuleBConfig implements ConfigData {
    }
}
