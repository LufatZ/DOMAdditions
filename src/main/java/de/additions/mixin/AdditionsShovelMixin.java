package de.additions.mixin;

import de.additions.helper.PathConversionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A mixin for {@link ShovelItem} that extends shovel interaction logic to support
 * path creation and reversal mechanics. This implementation allows players to
 * transform soil-like blocks into path variants or revert existing paths
 * back to their original dirt state when crouching. It also manages entity
 * positioning during these transformations to prevent entities from falling
 * through the block.
 */
@Mixin(ShovelItem.class)
public abstract class AdditionsShovelMixin {

    @ModifyVariable(
            method = "useOn",
            at = @At(value = "STORE", ordinal = 0),
            name = "newState"
    )
    private BlockState injectCustomPathLogic(BlockState newState, UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState clickedState = level.getBlockState(clickedPos);
        Player player = context.getPlayer();

        if (player != null && player.isShiftKeyDown()) {
            BlockState reverted = PathConversionHelper.revertToDirtState(clickedState);
            if (!reverted.equals(clickedState)) {
                PathConversionHelper.applyEntityFix(level, clickedPos, reverted);
                return reverted;
            }
        }

        if (newState == null) {
            if (PathConversionHelper.isPathable(clickedState)) {
                BlockState pathState = PathConversionHelper.getPathTargetState(clickedState);
                PathConversionHelper.applyEntityFix(level, clickedPos, pathState);
                return pathState;
            }
        } else {
            PathConversionHelper.applyEntityFix(level, clickedPos, newState);
        }

        return newState;
    }
}