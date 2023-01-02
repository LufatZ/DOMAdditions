package de.dayofmind.additions;

import de.dayofmind.additions.ItemGroups.ItemGroup;
import de.dayofmind.additions.block.DOMBlocksRegistry;
import de.dayofmind.additions.config.DOMConfig;
import de.dayofmind.additions.item.DOMItemsRegistry;
import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ModInitializer;


public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";
    @Override
    public void onInitialize() {
        System.out.println("DOM | DayOfMind is loading");
        System.out.println("DOM | Thanks for playing DayOfMind");
        System.out.println("DOM | Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("DOM | Please report bugs to LufatZ or MysticBanana");
        //TODO register config | only if complete config is installed
        DOMConfig config = new DOMConfig();
        config.load();
        //crowdin
        if (DOMConfig.ModSettings.EnabledTranslation) {
            System.out.println("DOM | you have enabled automatic download of translation files in the configs");
            System.out.println("DOM | download translation from crowdin");
            CrowdinTranslate.downloadTranslations("dayofmind-additions", "additions", true);
        }
        //Register DoM blocks
            DOMBlocksRegistry.registerBlocks();
        //Register DoM items
            DOMItemsRegistry.registerItems();
        //TODO Register DoM entities
        // registerAttributes();
        //Register DOM ItemGroups
            ItemGroup.DayOfMindItems();
            ItemGroup.DayOfMindBlocks();
    }
}
