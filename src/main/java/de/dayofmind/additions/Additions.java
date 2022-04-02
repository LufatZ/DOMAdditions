package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.*;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/*
TODO:
 -add all blocks                |COMPLETE BUT MORE IN FUTURE
 -add all BlockItems            |Automatic
 -give all Blocks Visual        |COMPLETE BUT PARTICLE LOOK STRANGE (GRASS_SLAB)
 -Block mineable
 -give all Blocks LootTable
 -look for tags
 -Block functions               |WORKING (SNOWY; )
 -add connected Textures
*/
public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        System.out.println("DayOfMind is loading");
        System.out.println("Thanks for playing DayOfMind");
        System.out.println("Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("Please report bugs to LufatZ or MysticBanana");
        System.out.println("DayOfMind is just adding blocks to minecraft");
        DOMBlocks.registerBlocks();
        System.out.println("DayOfMind has successfully added blocks to minecraft");

    }
        //System.out.println("loading DayOfMind ItemGroup"); TODO Mystic fragen
        public static final ItemGroup DayOfMind = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"), () -> new ItemStack(DOMBlocks.GRASS_STAIR));

}
