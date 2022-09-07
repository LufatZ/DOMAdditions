package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocksRegister;
import de.dayofmind.additions.config.ModConfig;
import de.dayofmind.additions.item.DOMItemsRegister;
import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import static de.dayofmind.additions.entity.EntityRegister.registerAttributes;

public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";
    @Override
    public void onInitialize() {
        System.out.println("DOM | DayOfMind is loading");
        System.out.println("DOM | Thanks for playing DayOfMind");
        System.out.println("DOM | Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("DOM | Please report bugs to LufatZ or MysticBanana");
        //register config | only if complete config is installed
        if (FabricLoader.getInstance().isModLoaded("completeconfig-base")) {
            System.out.println("DOM | Adding configs");
            ModConfig config = new ModConfig();
            config.load();
        }
        //check configs
        if(ModConfig.ExperimentalSettings.ExperimentalEntities || ModConfig.ExperimentalSettings.ExperimentalBlocks) {
            System.out.println("DOM | You are using experimental settings. you may experience crashes and abnormal behavior.");
            System.out.println("DOM | If you want to play with experimental settings on servers, they must be enabled on the server and client.");
        }
        //crowdin TODO lang file will not be downloaded
        System.out.println("DOM | download translation from crowdin");
        CrowdinTranslate.downloadTranslations("dayofmind-additions", "additions", true);
        //Register DoM blocks
        DOMBlocksRegister.registerBlocks();
        //Register DoM items
        DOMItemsRegister.registerItems();
        //Register DoM entities
        registerAttributes();
    }
}
