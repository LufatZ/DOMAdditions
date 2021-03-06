package de.dayofmind.additions.block;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static de.dayofmind.additions.Additions.MOD_ID;

public class DOMBlocks {

    //adding DayOfMind Item Group
    public static final ItemGroup DayOfMind = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"), () -> new ItemStack(DOMBlocks.GRASS_STAIR));

    //slabs
        public static final Block DIRT_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_SLAB = new DOMGrassSlab(FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK));
        public static final Block DIRT_PATH_SLAB = new DOMShortSlab(FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        public static final Block DIAMOND_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK));
    //stairs
        public static final Block DIRT_STAIR = new DOMStairs(Blocks.DIRT.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_STAIR = new DOMGrassStair(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK)); //TODO Temporary Default state as acacia stair, better fix later
        public static final Block DIRT_PATH_STAIR = new DOMShortStairs(Blocks.DIRT_PATH.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_STAIR = new DOMStairs(Blocks.GOLD_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_STAIR = new DOMStairs(Blocks.IRON_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        public static final Block DIAMOND_STAIR = new DOMStairs(Blocks.DIAMOND_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK));
    //blocks
    //items

    public static void registerBlocks(){

        System.out.println("DayOfMind is just adding blocks to minecraft");

        //slabs
            registerBlock("dirt_slab", DIRT_SLAB);
            registerBlock("grass_slab", GRASS_SLAB);
            registerBlock("dirt_path_slab", DIRT_PATH_SLAB);
            registerBlock("gold_slab", GOLD_SLAB);
            registerBlock("iron_slab", IRON_SLAB);
            registerBlock("diamond_slab", DIAMOND_SLAB);
        //stairs
            registerBlock("dirt_stair", DIRT_STAIR);
            registerBlock("grass_stair", GRASS_STAIR);
            registerBlock("dirt_path_stair", DIRT_PATH_STAIR);
            registerBlock("gold_stair", GOLD_STAIR);
            registerBlock("iron_stair", IRON_STAIR);
            registerBlock("diamond_stair", DIAMOND_STAIR);
        //blocks
    }


    //for BlockItem registration
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
