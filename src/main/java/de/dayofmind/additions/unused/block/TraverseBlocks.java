package de.dayofmind.additions.unused.block;

import de.dayofmind.additions.block.DOMStairs;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

//traverse Blocks
//IMPORTANT this is only temporary, because traverse hasn´t updated yet.
//Using block-names and assets of the original traverse mod, with the permission of TerraformersMC
public class TraverseBlocks {

    //adding Traverse Item Group
    public static final ItemGroup Traverse = FabricItemGroupBuilder.build(new Identifier("traverse", "items"), () -> new ItemStack(TraverseBlocks.RED_AUTUMNAL_LEAVES));

    //leaves
    public static final Block RED_AUTUMNAL_LEAVES = new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES));
    public static final Block BROWN_AUTUMNAL_LEAVES = new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES));
    public static final Block ORANGE_AUTUMNAL_LEAVES = new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES));
    public static final Block YELLOW_AUTUMNAL_LEAVES = new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES));
    public static final Block FIR_LEAVES = new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES));
    //planks
    public static final Block FIR_PLANKS = new Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
    //logs
    public static final Block STRIPPED_FIR_LOG = createTraverseLogBlock(MapColor.BROWN, MapColor.OAK_TAN);
    public static final Block FIR_LOG = createTraverseLogBlock(MapColor.BROWN, MapColor.OAK_TAN);
    //wood
    public static final Block STRIPPED_FIR_WOOD = createTraverseLogBlock(MapColor.OAK_TAN, MapColor.OAK_TAN);
    public static final Block FIR_WOOD =  createTraverseLogBlock(MapColor.BROWN,MapColor.OAK_TAN);
    //slabs
    public static final Block FIR_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.OAK_SLAB));
    //stairs
    public static final Block FIR_STAIRS = new DOMStairs(FIR_PLANKS.getDefaultState(), FabricBlockSettings.copyOf(Blocks.OAK_STAIRS));
    //doors
    public static final Block FIR_DOOR = new DoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_DOOR));
    //buttons
    public static final Block FIR_BUTTON = new WoodenButtonBlock(FabricBlockSettings.copyOf(Blocks.OAK_BUTTON));
    //plates
    public static final Block FIR_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, FabricBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE));
    //fences
    public static final Block FIR_FENCE = new FenceBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE));
    public static final Block FIR_FENCE_GATE =new FenceGateBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE_GATE));
    //trapdoors
    public static final Block FIR_TRAPDOOR = new TrapdoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));

    public static void registerBlocks(){
        //traverse leaves
        registerTraverseBlock("red_autumnal_leaves", RED_AUTUMNAL_LEAVES);
        registerTraverseBlock("brown_autumnal_leaves", BROWN_AUTUMNAL_LEAVES);
        registerTraverseBlock("orange_autumnal_leaves", ORANGE_AUTUMNAL_LEAVES);
        registerTraverseBlock("yellow_autumnal_leaves", YELLOW_AUTUMNAL_LEAVES);
        registerTraverseBlock("fir_leaves", FIR_LEAVES);
        //planks
        registerTraverseBlock("fir_planks", FIR_PLANKS);
        //logs
        registerTraverseBlock("stripped_fir_log", STRIPPED_FIR_LOG);
        registerTraverseBlock("fir_log", FIR_LOG);
        //wood
        registerTraverseBlock("stripped_fir_wood", STRIPPED_FIR_WOOD);
        registerTraverseBlock("fir_wood", FIR_WOOD);
        //slab
        registerTraverseBlock("fir_slab", FIR_SLAB);
        //stairs
        registerTraverseBlock("fir_stairs", FIR_STAIRS);
        //door
        registerTraverseBlock("fir_door", FIR_DOOR);
        //buttons
        registerTraverseBlock("fir_button", FIR_BUTTON);
        //plates
        registerTraverseBlock("fir_pressure_plate", FIR_PRESSURE_PLATE);
        //fences
        registerTraverseBlock("fir_fence", FIR_FENCE);
        registerTraverseBlock("fir_fence_gate", FIR_FENCE_GATE);
        //trapdoors
        registerTraverseBlock("fir_trapdoor", FIR_TRAPDOOR);
    }
    //for BlockItem registration

    private static void registerTraverseBlock(String name, Block block) {
        registerTraverseBlock(name, block, null);
    }
    private static void registerTraverseBlock(String name, Block block, BlockItem blockItem) {
        if (blockItem == null) {
            blockItem = new BlockItem(block, new Item.Settings().group(Traverse));
        }
        Registry.register(Registry.BLOCK, new Identifier("traverse", name), block);
        Registry.register(Registry.ITEM, new Identifier("traverse", name), blockItem);
    }

    private static PillarBlock createTraverseLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return new PillarBlock(AbstractBlock.Settings.of(Material.WOOD, (state) -> {
            return state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor;
        }).strength(2.0F).sounds(BlockSoundGroup.WOOD));
    }
}
