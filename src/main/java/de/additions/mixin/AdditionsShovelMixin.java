package de.additions.mixin;

import de.additions.blocks.BlockRegistry;
import de.additions.config.AdditionsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.block.Blocks.DIRT;
import static net.minecraft.block.Blocks.DIRT_PATH;

/**
 * Mixin for the ShovelItem class.
 * Purpose: Extends the shovel's "useOnBlock" functionality (creating path blocks)
 * to include custom dirt-based stairs and slabs added by this mod.
 * It allows players to turn custom dirt stairs/slabs into their corresponding
 * path versions and potentially back, using a shovel, similar to how vanilla
 * dirt blocks are handled.
 * This behavior is controlled by the 'EnabledShovelMixin' config option.
 */
@Mixin(ShovelItem.class)
public abstract class AdditionsShovelMixin {

	@Unique
	private static final Logger logger = LoggerFactory.getLogger("DayOfMind Additions - Shovel Mixin");

	/**
	 * Stores the custom mappings between dirt-like blocks (including custom stairs/slabs)
	 * and their corresponding path block states. Also includes reverse mappings (path -> dirt)
	 * to allow toggling back if desired.
	 * This map is lazily initialized when first needed via fillCustomPathStatesMap().
	 * Marked as @Unique because this map is specific to this mixin's functionality.
	 */
	@Unique
	private static final Map<Block, BlockState> CUSTOM_PATH_STATES = new HashMap<>();

	/**
	 * Flag to track if the map has been initialized.
	 * Used to prevent redundant initialization attempts.
	 */
	@Unique
	private static boolean customMapInitialized = false;

	/**
	 * Populates the {@link #CUSTOM_PATH_STATES} map.
	 * This method is called lazily the first time the shovel interaction check occurs
	 * and the feature is enabled.
	 * <p>
	 * Implementation Details:
	 * - Checks the {@link AdditionsConfig#EnabledShovelMixin} config flag before proceeding.
	 * - Retrieves custom dirt path/dirt stairs and slabs from the mod's {@link BlockRegistry}.
	 * This relies on the mod registering these specific block types.
	 * - Iterates through all blocks tagged with {@link BlockTags#DIRT} in the block registry.
	 * - For each block in the tag:
	 * - Skips blocks that are already dirt path variants (based on translation key).
	 * - Determines the corresponding path block type (stair, slab, or default DIRT_PATH)
	 * based on the original block's translation key containing "stair" or "slab".
	 * - Adds a mapping from the dirt-like block to its path version's default state.
	 * - Determines the corresponding base dirt block type (stair, slab, or default DIRT).
	 * - Adds a reverse mapping from the path block to its base dirt version's default state.
	 * This allows using the shovel on a path stair/slab to potentially turn it back into a dirt stair/slab.
	 * - Uses Optional and logging for safer handling if custom blocks are not found,
	 * avoiding potential NullPointerExceptions from .orElse(null).
	 */
	@Unique
	private static void fillCustomPathStatesMap() {
		if (!AdditionsConfig.EnabledShovelMixin) {
			logger.debug("Shovel Mixin feature disabled via config, skipping map population.");
			return; // Do nothing if the feature is disabled
		}

		logger.info("Initializing custom path states map for Shovel Mixin...");

		// Find custom blocks safely using Optional
		Optional<Block> pathStairOpt = BlockRegistry.getRegisteredStairs().stream()
				.filter(stair -> stair.getTranslationKey().toLowerCase().contains(".dirt_path_stair"))
				.findFirst();
		Optional<Block> pathSlabOpt = BlockRegistry.getRegisteredSlabs().stream()
				.filter(slab -> slab.getTranslationKey().toLowerCase().contains(".dirt_path_slab"))
				.findFirst();
		Optional<Block> dirtStairOpt = BlockRegistry.getRegisteredStairs().stream()
				.filter(stair -> stair.getTranslationKey().toLowerCase().contains(".dirt_stair"))
				.findFirst();
		Optional<Block> dirtSlabOpt = BlockRegistry.getRegisteredSlabs().stream()
				.filter(slab -> slab.getTranslationKey().toLowerCase().contains(".dirt_slab"))
				.findFirst();

		// Log warnings if essential custom blocks are missing
		if (pathStairOpt.isEmpty()) logger.warn("Custom Dirt Path Stair block not found in registry!");
		if (pathSlabOpt.isEmpty()) logger.warn("Custom Dirt Path Slab block not found in registry!");
		if (dirtStairOpt.isEmpty()) logger.warn("Custom Dirt Stair block not found in registry!");
		if (dirtSlabOpt.isEmpty()) logger.warn("Custom Dirt Slab block not found in registry!");


		// Use Util.make for cleaner map initialization
		Map<Block, BlockState> pathStates = Util.make(new HashMap<>(), map -> {
			for (RegistryEntry<Block> entry : Registries.BLOCK.iterateEntries(BlockTags.DIRT)) {
				Block block = entry.value();
				String key = block.getTranslationKey().toLowerCase();

				// Skip if it's already some kind of path block found in the DIRT tag
				if (key.contains("dirt_path")) {
					continue;
				}

				// Determine target path block and source dirt block based on type
				Block targetPathBlock;
				Block sourceDirtBlock;

				if (key.contains("stair")) {
					targetPathBlock = pathStairOpt.orElse(null);
					sourceDirtBlock = dirtStairOpt.orElse(null);
				} else if (key.contains("slab")) {
					targetPathBlock = pathSlabOpt.orElse(null);
					sourceDirtBlock = dirtSlabOpt.orElse(null);
				} else {
					// Assume it's a full block if not stair or slab
					targetPathBlock = DIRT_PATH;
					sourceDirtBlock = DIRT; // Standard dirt block
				}

				// Add forward mapping (Dirt-like -> Path)
				if (targetPathBlock != null && !map.containsKey(block)) {
					map.put(block, targetPathBlock.getDefaultState());
					logger.debug("Mapping dirt block {} -> path block {}", Registries.BLOCK.getId(block), Registries.BLOCK.getId(targetPathBlock));
				} else if (targetPathBlock == null) {
					logger.trace("Skipping forward mapping for {} due to missing target path block.", Registries.BLOCK.getId(block));
				}


				// Add reverse mapping (Path -> Dirt)
				// Ensure we have valid blocks for the reverse mapping
				if (targetPathBlock != null && targetPathBlock != DIRT_PATH && sourceDirtBlock != null && !map.containsKey(targetPathBlock)) {
					// Check if sourceDirtBlock is the same as the original block if it was a stair/slab
					Block reverseTarget = (key.contains("stair") || key.contains("slab")) ? block : sourceDirtBlock;
					map.put(targetPathBlock, reverseTarget.getDefaultState());
					logger.debug("Mapping path block {} -> dirt block {}", Registries.BLOCK.getId(targetPathBlock), Registries.BLOCK.getId(reverseTarget));

				} else if (targetPathBlock != null && (sourceDirtBlock == null && (key.contains("stair") || key.contains("slab")))) {
					logger.trace("Skipping reverse mapping for {} due to missing source dirt block.", Registries.BLOCK.getId(targetPathBlock));
				}
			}
		});

		CUSTOM_PATH_STATES.putAll(pathStates);
		if (!CUSTOM_PATH_STATES.containsKey(DIRT_PATH)) {
			CUSTOM_PATH_STATES.put(DIRT_PATH, DIRT.getDefaultState());
			logger.debug("Mapping path block {} -> dirt block {}", Registries.BLOCK.getId(DIRT_PATH), Registries.BLOCK.getId(DIRT));
		}
		customMapInitialized = true; // Mark as initialized
		logger.info("Custom path states map initialized with {} entries.", pathStates.size());
	}

	/**
	 * Modifies the local variable 'blockState2' within the ShovelItem#useOnBlock method.
	 * <p>
	 * Injection Point (@At):
	 * - `value = "STORE"`: Injects the code *after* a value has been calculated and is about to be stored
	 * into a local variable.
	 * - `ordinal = 0`: Targets the first occurrence of storing a value into a variable of the target type
	 * (BlockState in this case) within the method's bytecode. This corresponds to the line where
	 * `PATH_STATES.get(blockState.getBlock())` result is assigned to `blockState2`.
	 * - `name = "blockState2"`: Explicitly targets the local variable named `blockState2`.
	 * - `require = 1`: Ensures that the injection point is found, otherwise Mixin processing will fail.
	 * <p>
	 * Purpose:
	 * - Intercept the result of the vanilla path block lookup (`PATH_STATES.get(...)`).
	 * - If vanilla did *not* find a corresponding path block (`originalBlockState2` is null)
	 * AND this mod's feature is enabled:
	 * - Lazily initialize the `CUSTOM_PATH_STATES` map if it hasn't been done yet.
	 * - Look up the actually clicked block in the `CUSTOM_PATH_STATES` map.
	 * - If a custom mapping is found:
	 * - Transfer relevant block properties (like facing, half, waterlogged) from the
	 * original clicked block state to the new target path state using `transferStateProperties`.
	 * This is crucial for preserving orientation and state.
	 * - Return the new, state-aware `customTargetState`. This value will then be stored
	 * in the `blockState2` local variable in `useOnBlock`, effectively overriding the null value.
	 * - If vanilla *did* find a block, or the feature is disabled, or no custom mapping exists,
	 * return the original result (`originalBlockState2`) unchanged, preserving vanilla behavior.
	 *
	 * @param originalBlockState2 The original value that would have been assigned to `blockState2`.
	 * This is the result from `ShovelItem.PATH_STATES.get()`. Can be null.
	 * @param context             The ItemUsageContext providing information about the interaction.
	 * @return The BlockState to actually store in `blockState2`, potentially modified by this mixin.
	 */
	@ModifyVariable(
			method = "useOnBlock",
			at = @At(value = "STORE", ordinal = 0), // Target assignment to blockState2 after PATH_STATES.get()
			name = "blockState2", // The name of the local variable in ShovelItem#useOnBlock
			require = 1 // Ensure the target is found
	)
	private BlockState injectCustomPathStates(BlockState originalBlockState2, ItemUsageContext context) {
		// Only intervene if vanilla lookup failed AND the feature is enabled
		if (originalBlockState2 == null && AdditionsConfig.EnabledShovelMixin) {

			// Lazily initialize the map on first use if needed
			if (!customMapInitialized) {
				fillCustomPathStatesMap();
			}

			Block clickedBlock = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
			BlockState customTargetState = CUSTOM_PATH_STATES.get(clickedBlock);

			if (customTargetState != null) {
				// Found a custom mapping (e.g., Dirt Stair -> Path Stair or Path Slab -> Dirt Slab)
				BlockState originalClickedState = context.getWorld().getBlockState(context.getBlockPos());

				// Attempt to transfer properties like facing, waterlogged, etc.
				BlockState finalState = transferStateProperties(originalClickedState, customTargetState);

				// Log the transformation
				logger.debug("Shovel Mixin: Transforming {} to {}",
						Registries.BLOCK.getId(originalClickedState.getBlock()),
						Registries.BLOCK.getId(finalState.getBlock()));


				return finalState; // Return our custom state
			}
		}
		// Otherwise, return the result from the vanilla lookup (could be null or a vanilla path state)
		return originalBlockState2;
	}

	/**
	 * Helper method to transfer relevant properties from a source BlockState to a target BlockState.
	 * <p>
	 * Why this is necessary: When replacing a block like a stair or slab, simply setting the new
	 * block type (e.g., Dirt Stair to Path Stair) is not enough. We need to preserve properties like
	 * its facing direction, whether it's an upper or lower slab/stair (`HALF`/`TYPE`),
	 * its shape (for stairs connected to others), and whether it's waterlogged. Without this,
	 * the new block would revert to its default state (e.g., always facing north, lower half).
	 * <p>
	 * Implementation:
	 * - Checks if the source and target blocks are of compatible types (e.g., both Stairs or both Slabs).
	 * - If compatible, it attempts to copy the relevant properties using `target.with(PROPERTY, source.get(PROPERTY))`.
	 * - Uses a try-catch block to handle potential `IllegalArgumentException`. This can occur if,
	 * unexpectedly, a source state doesn't have a property that the target state expects,
	 * or vice versa (though unlikely with proper type checking). This makes the transfer more robust.
	 * - Logs an error if property transfer fails.
	 *
	 * @param source The original BlockState of the block being replaced (e.g., Dirt Stair).
	 * @param target The initial default BlockState of the new block (e.g., Path Stair default state).
	 * @return The target BlockState, potentially updated with properties copied from the source state,
	 * or the original target state if transfer fails or is not applicable. Returns null if target is initially null.
	 */
	@Unique
	private static BlockState transferStateProperties(BlockState source, @Nullable BlockState target) {
		if (target == null) {
			// Should not happen if called correctly after a map lookup, but good safety check.
			return null;
		}
		try {
			// Transfer properties if both are Stairs
			if (source.getBlock() instanceof StairsBlock && target.getBlock() instanceof StairsBlock) {
				target = target.with(StairsBlock.FACING, source.get(StairsBlock.FACING))
						.with(StairsBlock.HALF, source.get(StairsBlock.HALF))
						.with(StairsBlock.SHAPE, source.get(StairsBlock.SHAPE))
						.with(StairsBlock.WATERLOGGED, source.get(StairsBlock.WATERLOGGED));
			}
			// Transfer properties if both are Slabs
			else if (source.getBlock() instanceof SlabBlock && target.getBlock() instanceof SlabBlock) {
				target = target.with(SlabBlock.TYPE, source.get(SlabBlock.TYPE))
						.with(SlabBlock.WATERLOGGED, source.get(SlabBlock.WATERLOGGED));
			}
			// Add more conditions here for other block types if needed (e.g., logs with AXIS)

		} catch (IllegalArgumentException e) {
			// Log an error if a property doesn't exist on either source or target state unexpectedly.
			logger.error("Failed to transfer block state properties from {} to {}: {}",
					Registries.BLOCK.getId(source.getBlock()),
					Registries.BLOCK.getId(target.getBlock()),
					e.getMessage());
			// Return the target state without the failed property transfer attempt
		}
		return target;
	}
}