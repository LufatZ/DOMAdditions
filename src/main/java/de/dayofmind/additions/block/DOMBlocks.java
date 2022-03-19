package de.dayofmind.additions.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static de.dayofmind.additions.Additions.DayOfMind;
import static de.dayofmind.additions.Additions.MOD_ID;

public class DOMBlocks {

    //slabs
        public static final Block DIRT_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_SLAB = new DOMGrassSlab(FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK));
        public static final Block FARMLAND_SLAB = new DOMFarmlandSlab(FabricBlockSettings.copyOf(Blocks.FARMLAND));
        public static final Block DIRT_PATH_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    //stairs
        public static final Block DIRT_STAIR = new DOMStairsBlock(Blocks.DIRT.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_STAIR = new DOMStairsBlock(Blocks.GRASS_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK));
        //public static final Block FARMLAND_STAIR = new DOMStairsBlock(Blocks.FARMLAND.getDefaultState(),FabricBlockSettings.copyOf(Blocks.FARMLAND));
        public static final Block DIRT_PATH_STAIR = new DOMStairsBlock(Blocks.DIRT_PATH.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_STAIR = new DOMStairsBlock(Blocks.GOLD_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_STAIR = new DOMStairsBlock(Blocks.IRON_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));

    public static void registerBlocks(){
        //slabs
            registerBlock("dirt_slab", DIRT_SLAB);
            registerBlock("grass_slab", GRASS_SLAB);
            registerBlock("farmland_slab", FARMLAND_SLAB);
            registerBlock("dirt_path_slab", DIRT_PATH_SLAB);
            registerBlock("gold_slab", GOLD_SLAB);
            registerBlock("iron_slab", IRON_SLAB);
        //stairs
            registerBlock("dirt_stair", DIRT_STAIR);
            registerBlock("grass_stair", GRASS_STAIR);
            //registerBlock("farmland_stair", FARMLAND_STAIR);
            registerBlock("dirt_path_stair", DIRT_PATH_STAIR);
            registerBlock("gold_stair", GOLD_STAIR);
            registerBlock("iron_stair", IRON_STAIR);
    }




    //for Block registration
    private static void registerBlock(String name, Block block) {
        registerBlock(name, block, null);
    }
    private static void registerBlock(String name, Block block, BlockItem blockItem) {
        if (blockItem == null) {
            blockItem = new BlockItem(block, new Item.Settings().group(DayOfMind));
        }
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, name), block);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, name), blockItem);
    }
}
