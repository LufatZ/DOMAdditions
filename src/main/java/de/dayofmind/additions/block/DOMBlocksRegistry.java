package de.dayofmind.additions.block;

import de.dayofmind.additions.block.instruments.DOMGuitarBlock;
import de.dayofmind.additions.block.lanterns.DOMCopperLantern;
import de.dayofmind.additions.block.lanterns.DOMNetheriteLantern;
import de.dayofmind.additions.block.lanterns.DOMRedstoneChain;
import de.dayofmind.additions.block.lanterns.DOMRedstoneLantern;
import de.dayofmind.additions.block.slabs.DOMCryingObsidianSlab;
import de.dayofmind.additions.block.slabs.DOMGrassSlab;
import de.dayofmind.additions.block.slabs.DOMMagmaSlab;
import de.dayofmind.additions.block.slabs.DOMShortSlab;
import de.dayofmind.additions.block.stairs.DOMDirtPathStair;
import de.dayofmind.additions.block.stairs.DOMGrassStair;
import de.dayofmind.additions.block.stairs.DOMMagmaStair;
import de.dayofmind.additions.block.stairs.DOMStairs;
import de.dayofmind.additions.block.trapdoors.DOMDecorativeIronTrapdoor;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.function.ToIntFunction;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.item.DOMItemsRegistry.GUITAR_ITEM;

public class DOMBlocksRegistry {
    //helpers
    private static ToIntFunction<BlockState> createLightLevelFromLitBlockState() {
        return (state) -> (Boolean)state.get(Properties.LIT) ? 15 : 0;
    }
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
        public static final Block CRYING_OBSIDIAN_SLAB = new DOMCryingObsidianSlab(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN));
    //stairs
        public static final Block DIRT_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().strength(0.5F).sounds(BlockSoundGroup.GRAVEL).mapColor(MapColor.DIRT_BROWN));
        public static final Block GRASS_STAIR = new DOMGrassStair(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS).mapColor(MapColor.GREEN));
        public static final Block DIRT_PATH_STAIR = new DOMDirtPathStair(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().strength(0.65F).sounds(BlockSoundGroup.GRASS).mapColor(MapColor.DIRT_BROWN));
        public static final Block GOLD_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL).mapColor(MapColor.GOLD));
        public static final Block IRON_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).mapColor(MapColor.IRON_GRAY));
        public static final Block DIAMOND_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).mapColor(MapColor.DIAMOND_BLUE));
        public static final Block SMOOTH_BASALT_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(1.25F, 4.2F).sounds(BlockSoundGroup.BASALT).mapColor(MapColor.BLACK));
        public static final Block POLISHED_BASALT_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(1.25F, 4.2F).sounds(BlockSoundGroup.BASALT).mapColor(MapColor.BLACK));
        public static final Block MAGMA_STAIR = new DOMMagmaStair(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.copyOf(Blocks.MAGMA_BLOCK));
        public static final Block OBSIDIAN_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.create().requiresTool().strength(50.0F, 1200.0F).mapColor(MapColor.BLACK));
        public static final Block CRYING_OBSIDIAN_STAIR = new DOMStairs(Blocks.ACACIA_STAIRS.getDefaultState(),FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN));

    //lanterns
        public static final Block NETHERITE_LANTERN = new DOMNetheriteLantern(FabricBlockSettings.copyOf(Blocks.LANTERN));
        public static final Block COPPER_LANTERN = new DOMCopperLantern(FabricBlockSettings.copyOf(Blocks.LANTERN));

        public static final Block NETHERITE_REDSTONE_LANTERN = new DOMRedstoneLantern(FabricBlockSettings.copyOf(NETHERITE_LANTERN).luminance(createLightLevelFromLitBlockState()));
        public static final Block COPPER_REDSTONE_LANTERN = new DOMRedstoneLantern(FabricBlockSettings.copyOf(COPPER_LANTERN).luminance(createLightLevelFromLitBlockState()));
        public static final Block REDSTONE_LANTERN = new DOMRedstoneLantern(FabricBlockSettings.copyOf(Blocks.LANTERN).luminance(createLightLevelFromLitBlockState()));

    //trapdoors
        public static final Block Decorative_Iron_Trapdoor = new DOMDecorativeIronTrapdoor(FabricBlockSettings.create().requiresTool().strength(5.0F).sounds(BlockSoundGroup.METAL).mapColor(MapColor.IRON_GRAY).nonOpaque(), BlockSetType.IRON);

    //instruments
        public static final Block GUITAR = new DOMGuitarBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.BROWN));
        //public static final Block DRUM = new DOMDrumBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.BROWN));
        //public static final Block FLUTE = new DOMFluteBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.GRAY));

    //chain
        public static final Block REDSTONE_CHAIN = new DOMRedstoneChain(FabricBlockSettings.copyOf(Blocks.CHAIN));

    public static void registerBlocks(){
        System.out.println("DOM | Adding blocks");
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
            registerBlock("netherite_redstone_lantern", NETHERITE_REDSTONE_LANTERN);
            registerBlock("copper_redstone_lantern", COPPER_REDSTONE_LANTERN);
            registerBlock("redstone_lantern", REDSTONE_LANTERN);
        //instruments
            registerBlock("guitar", GUITAR, (BlockItem) GUITAR_ITEM);
            //registerBlock("flute", FLUTE, (BlockItem) FLUTE_ITEM);
            //registerBlock("drum", DRUM, (BlockItem) DRUM_ITEM);
        //trapdoor
            registerBlock("decorative_iron_trapdoor", Decorative_Iron_Trapdoor);
        //chain
            registerBlock("redstone_chain", REDSTONE_CHAIN);
        System.out.println("DOM | DayOfMind successful added blocks to minecraft");
    }

    //for BlockItem registration
    private static void registerBlock(String name, Block block) {
        registerBlock(name, block, null);
    }
    private static void registerBlock(String name, Block block, BlockItem blockItem) {
        if (blockItem == null) {
            blockItem = new BlockItem(block, new Item.Settings());
        }
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), blockItem);
    }
}
