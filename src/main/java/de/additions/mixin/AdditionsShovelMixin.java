package de.additions.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

import static de.additions.config.AdditionsConfig.EnabledShovelMixin;
import static net.minecraft.block.Blocks.*;

@Mixin(ShovelItem.class)
public class AdditionsShovelMixin {
	// Get access to PATH_STATES with Accessor Mixin
	@Accessor("PATH_STATES")
	static Map<Block, BlockState> getPathStates() {
		throw new AssertionError();
	}
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onStaticInit(CallbackInfo info) {
		if (EnabledShovelMixin) {
			// Get access to PATH_STATES with Accessor Mixin
			Map<Block, BlockState> pathStates = getPathStates();

			// create a copy of PATH_STATES and add entries
			Map<Block, BlockState> additionalPathStates = Map.of(
					DIRT_PATH, DIRT.getDefaultState()
					//ACACIA_STAIRS, OAK_STAIRS.getDefaultState(),
					//ACACIA_SLAB, OAK_SLAB.getDefaultState()
					//TODO: add own slabs and stairs
			);

			// Add new entries to original PATH_STATES Map
			pathStates.putAll(additionalPathStates);
		}
	}
	@Inject(method = "useOnBlock",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true)
	private void onSetBlockState(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity, BlockState blockState2, BlockState blockState3) {
		if (EnabledShovelMixin) {
			if (blockState2 != null) {
				// keep important blockstates
				if (blockState.getBlock() instanceof StairsBlock && blockState2.getBlock() instanceof StairsBlock) {
					blockState2 = blockState2.with(StairsBlock.FACING, blockState.get(StairsBlock.FACING))
							.with(StairsBlock.HALF, blockState.get(StairsBlock.HALF))
							.with(StairsBlock.SHAPE, blockState.get(StairsBlock.SHAPE));
				}
				if (blockState.getBlock() instanceof SlabBlock && blockState2.getBlock() instanceof SlabBlock) {
					blockState2 = blockState2.with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE));
				}
				// Set modified blockstates
				world.setBlockState(blockPos, blockState2, 11);
				cir.setReturnValue(ActionResult.success(world.isClient));
			}
		}
	}
}