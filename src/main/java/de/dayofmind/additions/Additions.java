package de.dayofmind.additions;

import de.dayofmind.additions.ItemGroups.ItemGroup;
import de.dayofmind.additions.block.DOMBlocksRegistry;
import de.dayofmind.additions.item.DOMItemsRegistry;
import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ModInitializer;


public class Additions implements ModInitializer {
    public static String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        System.out.println("DOM | DayOfMind is loading \n Thanks for playing DayOfMind \n Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F \n Please report bugs to LufatZ or MysticBanana");
        //config

        //crowdin
        //if (DOMConfig.ModSettings.EnabledTranslation) {
            System.out.println("DOM | you have enabled automatic download of translation \n download translation from crowdin");
            CrowdinTranslate.downloadTranslations("dayofmind-additions", "additions", true);
        //}
        //Register DoM blocks
            DOMBlocksRegistry.registerBlocks();
        //Register DoM items
            DOMItemsRegistry.registerItems();
        //TODO Register DoM entities
        // registerAttributes();
        //Register DOM ItemGroups
            ItemGroup.ItemGroupRegister();
    }
}
