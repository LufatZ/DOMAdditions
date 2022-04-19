package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/*
TODO:
 -add all blocks                |COMPLETE BUT MORE IN FUTURE
 -add all BlockItems            |Automatic
 -give all Blocks Visual        |COMPLETE BUT PARTICLE LOOK STRANGE (GRASS_SLAB)
 -Block mineable
 -give all Blocks LootTable
 -look for tags
 -Block functions               |WORKING (SNOWY_Grass_Slab; ...))
 -add connected Textures

IDEAS:
 -Glass Slabs                   |not needed as long MoGlass recive updates
 -snowy for grass stairs and slabs
 -stony path layer (decoration)
*/
public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        System.out.println("DayOfMind is loading");
        System.out.println("Thanks for playing DayOfMind");
        System.out.println("Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("Please report bugs to LufatZ or MysticBanana");
        DOMBlocks.registerBlocks();

    }
        //System.out.println("loading DayOfMind ItemGroup"); TODO Mystic fragen
        public static final ItemGroup DayOfMind = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"), () -> new ItemStack(DOMBlocks.GRASS_STAIR));

}
