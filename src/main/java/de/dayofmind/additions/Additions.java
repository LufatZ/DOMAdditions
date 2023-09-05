package de.dayofmind.additions;

import de.dayofmind.additions.ItemGroups.ItemGroup;
import de.dayofmind.additions.block.DOMBlocksRegistry;
import de.dayofmind.additions.config.DOMConfig;
import de.dayofmind.additions.item.DOMItemsRegistry;
import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import static de.dayofmind.additions.config.DOMConfig.EnabledTranslation;
import static java.lang.System.*;


public class Additions implements ModInitializer {
    public static final String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        out.println("DOM | DayOfMind is loading \n Thanks for playing DayOfMind \n Please take a look on my Discord: https://discord.com/invite/uMs57SHZYn \n Please report bugs to LufatZ or MysticBanana");
        //config
        MidnightConfig.init(MOD_ID, DOMConfig.class);
        //crowdin
        if (EnabledTranslation) {
            out.println("DOM | you have enabled automatic download of translation \n download translation from crowdin");
            CrowdinTranslate.downloadTranslations("dayofmind-additions", MOD_ID, true);
        }
        //Register DoM blocks
            DOMBlocksRegistry.registerBlocks();
        //Register DoM items
            DOMItemsRegistry.registerItems();
        //TODO Register DoM entities
        //Register DOM ItemGroups
            ItemGroup.itemGroupRegister();
    }
}
