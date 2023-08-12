package de.dayofmind.additions.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class DOMConfig extends MidnightConfig {

    @MidnightConfig.Comment(category = "about")
    public static MidnightConfig.Comment DayOfMind;
    @MidnightConfig.Comment(category = "about")
    public static MidnightConfig.Comment aboutDayOfMind;

    @MidnightConfig.Comment(category = "features")
    public static MidnightConfig.Comment features;
    @MidnightConfig.Entry(category = "features")
    public static boolean EnabledBlockVariants = true;
    @MidnightConfig.Entry(category = "features")
    public static boolean EnabledLantern = true;
    @MidnightConfig.Entry(category = "features")
    public static boolean EnabledRedstoneLantern = true;
    @MidnightConfig.Entry(category = "features")
    public static boolean EnabledTranslation = true;
    @MidnightConfig.Entry(category = "features")
    public static boolean EnabledDecorativeTrapdoor = true;

    @MidnightConfig.Comment(category = "experimentalSettings")
    public static MidnightConfig.Comment experimental;
    @MidnightConfig.Entry(category = "experimentalSettings")
    public static boolean EnabledInstruments = false;

}
