package de.dayofmind.additions.block;

import de.dayofmind.additions.block.instruments.DOMGuitarBlock;
import de.dayofmind.additions.block.lanterns.DOMCopperLantern;
import de.dayofmind.additions.block.lanterns.DOMNetheriteLantern;
import de.dayofmind.additions.block.slabs.DOMGrassSlab;
import de.dayofmind.additions.block.slabs.DOMMagmaSlab;
import de.dayofmind.additions.block.slabs.DOMShortSlab;
import de.dayofmind.additions.block.stairs.DOMGrassStair;
import de.dayofmind.additions.block.stairs.DOMMagmaStair;
import de.dayofmind.additions.block.stairs.DOMShortStairs;
import de.dayofmind.additions.block.stairs.DOMStairs;
import de.dayofmind.additions.item.DOMGuitar;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static de.dayofmind.additions.Additions.MOD_ID;

public class DOMBlocksRegister {

    //adding DayOfMind Item Group
    public static final ItemGroup DayOfMind = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"), () -> new ItemStack(DOMBlocksRegister.GRASS_STAIR));

    //slabs
        public static final Block DIRT_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_SLAB = new DOMGrassSlab(FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK));
        public static final Block DIRT_PATH_SLAB = new DOMShortSlab(FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        public static final Block DIAMOND_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK));
        public static final Block SMOOTH_BASALT_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.SMOOTH_BASALT));
        public static final Block POLISHED_BASALT_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.POLISHED_BASALT));
        public static final Block MAGMA_SLAB = new DOMMagmaSlab(FabricBlockSettings.copyOf(Blocks.MAGMA_BLOCK));
        public static final Block OBSIDIAN_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN));
        public static final Block CRYING_OBSIDIAN_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN));
    //stairs
        public static final Block DIRT_STAIR = new DOMStairs(Blocks.DIRT.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT));
        public static final Block GRASS_STAIR = new DOMGrassStair(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK)); //TODO Temporary Default state as acacia stair, better fix later
        public static final Block DIRT_PATH_STAIR = new DOMShortStairs(Blocks.DIRT_PATH.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIRT_PATH));
        public static final Block GOLD_STAIR = new DOMStairs(Blocks.GOLD_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
        public static final Block IRON_STAIR = new DOMStairs(Blocks.IRON_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        public static final Block DIAMOND_STAIR = new DOMStairs(Blocks.DIAMOND_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK));
        public static final Block SMOOTH_BASALT_STAIR = new DOMStairs(Blocks.BASALT.getDefaultState(),FabricBlockSettings.copyOf(Blocks.BASALT));
        public static final Block POLISHED_BASALT_STAIR = new DOMStairs(Blocks.POLISHED_BASALT.getDefaultState(),FabricBlockSettings.copyOf(Blocks.POLISHED_BASALT));
        public static final Block MAGMA_STAIR = new DOMMagmaStair(Blocks.MAGMA_BLOCK.getDefaultState(),FabricBlockSettings.copyOf(Blocks.MAGMA_BLOCK));
        public static final Block OBSIDIAN_STAIR = new DOMStairs(Blocks.OBSIDIAN.getDefaultState(),FabricBlockSettings.copyOf(Blocks.OBSIDIAN));
        public static final Block CRYING_OBSIDIAN_STAIR = new DOMStairs(Blocks.CRYING_OBSIDIAN.getDefaultState(),FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN));

    //lanterns
        public static final Block NETHERITE_LANTERN = new DOMNetheriteLantern(FabricBlockSettings.copyOf(Blocks.LANTERN));
        public static final Block COPPER_LANTERN = new DOMCopperLantern(FabricBlockSettings.copyOf(Blocks.LANTERN));

    //blocks
    //instruments
        public static final Block GUITAR = new DOMGuitarBlock(FabricBlockSettings.of(Material.WOOD, MapColor.BROWN).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD));

    //items
        public static final Item GUITAR_ITEM = new DOMGuitar(DOMBlocksRegister.GUITAR,(new Item.Settings().group(DayOfMind)));


    public static void registerBlocks(){

        System.out.println("DayOfMind is just adding blocks to minecraft");

        //slabs
            registerBlock("dirt_slab", DIRT_SLAB);
            registerBlock("grass_slab", GRASS_SLAB);
            registerBlock("dirt_path_slab", DIRT_PATH_SLAB);
            registerBlock("gold_slab", GOLD_SLAB);
            registerBlock("iron_slab", IRON_SLAB);
            registerBlock("diamond_slab", DIAMOND_SLAB);
            registerBlock("smooth_basalt_slab", SMOOTH_BASALT_SLAB);
            registerBlock("polished_basalt_slab", POLISHED_BASALT_SLAB);
            registerBlock("magma_slab", MAGMA_SLAB);
            registerBlock("obsidian_slab", OBSIDIAN_SLAB);
            registerBlock("crying_obsidian_slab", CRYING_OBSIDIAN_SLAB);
        //stairs
            registerBlock("dirt_stair", DIRT_STAIR);
            registerBlock("grass_stair", GRASS_STAIR);
            registerBlock("dirt_path_stair", DIRT_PATH_STAIR);
            registerBlock("gold_stair", GOLD_STAIR);
            registerBlock("iron_stair", IRON_STAIR);
            registerBlock("diamond_stair", DIAMOND_STAIR);
            registerBlock("smooth_basalt_stair", SMOOTH_BASALT_STAIR);
            registerBlock("polished_basalt_stair", POLISHED_BASALT_STAIR);
            registerBlock("magma_stair", MAGMA_STAIR);
            registerBlock("obsidian_stair", OBSIDIAN_STAIR);
            registerBlock("crying_obsidian_stair", CRYING_OBSIDIAN_STAIR);
        //lanterns
            registerBlock("netherite_lantern", NETHERITE_LANTERN);
            registerBlock("copper_lantern", COPPER_LANTERN);
        //blocks
        //instruments
            registerBlock("guitar", GUITAR, (BlockItem) GUITAR_ITEM);
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
