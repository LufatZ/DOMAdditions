package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocksRegister;
import de.dayofmind.additions.item.DOMItemsRegister;
import net.fabricmc.api.ModInitializer;

public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        System.out.println("DayOfMind is loading");
        System.out.println("Thanks for playing DayOfMind");
        System.out.println("Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("Please report bugs to LufatZ or MysticBanana");
        //Register DoM blocks
        DOMBlocksRegister.registerBlocks();
        //Register DoM items
        DOMItemsRegister.registerItems();
    }
    }
