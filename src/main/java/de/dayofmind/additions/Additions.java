package de.dayofmind.additions;

import de.dayofmind.additions.block.DOMBlocks;
import de.dayofmind.additions.block.TraverseBlocks;
import net.fabricmc.api.ModInitializer;

/*
TODO:
 -add all blocks                |COMPLETE BUT MORE IN FUTURE
 -add all BlockItems            |Automatic
 -give all Blocks Visual        |COMPLETE BUT PARTICLE LOOK STRANGE (GRASS_SLAB)
 -Block mineable
 -give all Blocks LootTable
 -look for tags
 -Block functions               |Missing (dirt slab or stair after time to grass slab or stair)
 -add connected Textures

IDEAS:
 -Glass Slabs                   |not needed as long MoGlass recive updates
 -snowy for grass stairs and slabs
 -stone path layer (decoration)
*/
public class Additions implements ModInitializer {

    public static String MOD_ID = "additions";

    @Override
    public void onInitialize() {
        System.out.println("DayOfMind is loading");
        System.out.println("Thanks for playing DayOfMind");
        System.out.println("Please take a look on my Discord: https://discord.com/invite/9EuPx2fJ4F");
        System.out.println("Please report bugs to LufatZ or MysticBanana");
        //Register DoM blocks
        DOMBlocks.registerBlocks();
        //Register Blocks from Traverse (unofficial port of a few Traverse blocks with the use of some original assets and names of the blocks). No biomes are added and blocks are only used to keep old worlds alive.
        System.out.println("Thanks to TerraformersMC for the permission to port some Blocks to 1.18.2");
        TraverseBlocks.registerBlocks();

    }
}
