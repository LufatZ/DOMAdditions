package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.LanternBlock;

public class DOMLanterns extends LanternBlock {
    public DOMLanterns(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(HANGING, false).with(WATERLOGGED, false));
    }
}
