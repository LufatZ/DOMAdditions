package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocksRegister;
import de.dayofmind.additions.config.ModConfig;
import de.dayofmind.additions.item.DOMItemsRegister;
import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ModInitializer;

import static de.dayofmind.additions.entity.EntityRegister.registerAttributes;

public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";
    @Override
    public void onInitialize() {
        System.out.println("DayOfMind is loading");
        System.out.println("Thanks for playing DayOfMind");
        System.out.println("Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("Please report bugs to LufatZ or MysticBanana");
        //register config
        System.out.println("DOM | adding configs");
        ModConfig config = new ModConfig();
        config.load();
        //crowdin
        CrowdinTranslate.downloadTranslations("dayofmind-additions", MOD_ID);
        //Register DoM blocks
        System.out.println("DOM | adding blocks");
        DOMBlocksRegister.registerBlocks();
        //Register DoM items
        System.out.println("DOM | adding items");
        DOMItemsRegister.registerItems();
        //Register DoM entities
        System.out.println("DOM | adding entities");
        registerAttributes();
    }
}
