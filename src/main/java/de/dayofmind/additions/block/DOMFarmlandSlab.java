package de.dayofmind.additions.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMFarmlandSlab extends DOMShortSlab {

    public static final EnumProperty<SlabType> TYPE;
    public static final BooleanProperty WATERLOGGED;
    public static final IntProperty MOISTURE; //add moisture (de: Feuchtigkeit)
    public static final int MAX_MOISTURE = 7;


    static {//load Block properties
        TYPE = Properties.SLAB_TYPE;
        WATERLOGGED = Properties.WATERLOGGED;
        MOISTURE = Properties.MOISTURE;}

    public DOMFarmlandSlab(Settings settings) {
        super(settings);setDefaultState(getStateManager().getDefaultState().with(WATERLOGGED, false).with(TYPE, SlabType.BOTTOM).with(MOISTURE, 0));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) { //add Block properties
        stateManager.add(TYPE, WATERLOGGED, MOISTURE);
    }

}
