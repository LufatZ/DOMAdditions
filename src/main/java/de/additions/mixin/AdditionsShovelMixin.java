package de.additions.mixin;

import de.additions.blocks.BlockRegistry;
import de.additions.config.AdditionsConfig;
import de.additions.datagen.BlockTagGenerator;
import net.minecraft.block.*;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.minecraft.block.Blocks.DIRT;
import static net.minecraft.block.Blocks.DIRT_PATH;

/**
 * Mixin for the ShovelItem class.
 * Purpose: Extends the shovel's "useOnBlock" functionality (creating path blocks).
 * It enables the following transformations via right-clicking with a shovel on registered blocks:
 * 1. Toggling specific custom blocks: Dirt Stairs <-> Path Stairs, Dirt Slabs <-> Path Slabs.
 * 2. Transforming vanilla DIRT_PATH back to DIRT.
 * 3. FEATURE: Transforming other compatible soil variants found in relevant tags
 * (e.g., Podzol Stairs, Moss Slabs) into the generic Path Stairs or Path Slabs respectively.
 * This behavior is controlled by the 'EnabledShovelMixin' config option.
 * It relies on BlockRegistry providing either the actual custom block instance or a
 * type-compatible fallback block if the custom block is disabled.
 */
@Mixin(ShovelItem.class)
public abstract class AdditionsShovelMixin {

    @Unique private static final Logger logger = LoggerFactory.getLogger("DayOfMind Additions - Shovel Mixin");
    @Unique private static final Block FALLBACK_STAIR = Blocks.OAK_STAIRS; // ADJUST TO YOUR ACTUAL FALLBACK
    @Unique private static final Block FALLBACK_SLAB = Blocks.OAK_SLAB;   // ADJUST TO YOUR ACTUAL FALLBACK

    /**
     * Modifies the local variable 'blockState2' within the ShovelItem#useOnBlock method.
     * * Checks tags and determines if a custom transformation should apply via getTargetState.
     * <p>
     * Injection Point (@At):
     * - Targets the assignment to the local variable `blockState2` right after the vanilla
     * `PATH_STATES.get(blockState.getBlock())` lookup is performed.
     * <p>
     * Purpose:
     * - Intercepts the result of the vanilla path block lookup.
     * - If vanilla did *not* find a corresponding path block (`originalBlockState2` is null)
     * AND this mod's feature is enabled via config:
     * - Checks if the clicked block belongs to the vanilla DIRT tag or the custom
     * `additions:dirt_like` or `additions:dirt_path_variant` tags.
     * - If it belongs to any of these relevant tags, it calls {@link #getTargetState(BlockState)}
     * to determine the appropriate transformed state (e.g., Dirt Stair -> Path Stair, Path -> Dirt,
     * *or Podzol Stair -> Path Stair* based on the logic in getTargetState).
     * - If a valid target state (different from the original) is found, this new state
     * is returned and stored in the `blockState2` local variable, overriding the null value.
     * - Otherwise (if vanilla found a state, feature is disabled, no relevant tag matched,
     * or no specific transformation rule applied in getTargetState),
     * it returns the original result (`originalBlockState2`), preserving vanilla behavior.
     *
     * @param originalBlockState2 The original value that would have been assigned to `blockState2`.
     * @param context             The ItemUsageContext providing information about the interaction.
     * @return The BlockState to actually store in `blockState2`, potentially modified by this mixin.
     */
    @ModifyVariable(
            method = "useOnBlock",
            at = @At(value = "STORE", ordinal = 0),
            index = 6,
            require = 1
    )
    private BlockState injectCustomPathStates(BlockState originalBlockState2, ItemUsageContext context) {
       // Only intervene if vanilla lookup failed AND the feature is enabled
       if (originalBlockState2 == null && AdditionsConfig.EnabledShovelMixin) {
          BlockState clickedBlockState = context.getWorld().getBlockState(context.getBlockPos());

          // Check if the clicked block is relevant for transformation (Vanilla Dirt, or our custom variants)
          if (
                clickedBlockState.isIn(BlockTags.DIRT) ||
                // Check custom tag for dirt-like variants (e.g., Podzol Stairs, Moss Slabs)
                clickedBlockState.isIn(BlockTagGenerator.Companion.getDirtLikeBlockTag()) ||
                // Check custom tag for path variants (Path Stairs, Path Slabs, Path Block itself)
                clickedBlockState.isIn(BlockTagGenerator.Companion.getDirtPathVariantTag()) ||
                clickedBlockState.getBlock() == DIRT_PATH // Check if clicked block is a Path Block
          ) {
             // Determine the target state (e.g., Path Stair, Dirt Slab, Dirt) including property transfer
             BlockState targetState = getTargetState(clickedBlockState);

             // Ensure a valid transformation occurred and the state actually changed.
             // Also handles potential null return from getTargetState on critical errors.
             if (targetState != null && targetState != clickedBlockState) {
                // Log the successful transformation
                logger.debug("Shovel Mixin: Transforming {} to {}",
                      Registries.BLOCK.getId(clickedBlockState.getBlock()),
                      Registries.BLOCK.getId(targetState.getBlock()));

                return targetState; // Return our custom transformed state
             }
             // If getTargetState returned null or the original state, we implicitly fall through
             // and return originalBlockState2 (which is null here), preserving vanilla behavior
             // for blocks in tags that don't have a specific transformation rule.
          }
       }
       // Otherwise, return the result from the vanilla lookup (could be null or a vanilla path state)
       return originalBlockState2;
    }

    /**
     * Determines the target BlockState when a shovel is used on a relevant source state.
     * Handles Dirt<->Path cycles and transforms other soil variants into Path variants.
     * Retrieves required block instances (or fallbacks) from BlockRegistry.
     * Transfers state properties (facing, half, type, waterlogged).
     * Includes a safety check to prevent transforming *into* a fallback block.
     * <p>
     * Transformation Rules Implemented:
     * <ul>
     * <li>Dirt Stairs <-> Path Stairs</li>
     * <li>Dirt Slabs <-> Path Slabs</li>
     * <li>DIRT_PATH -> DIRT</li>
     * <li>Other Stairs (in relevant tags, e.g., Podzol Stairs) -> Path Stairs</li>
     * <li>Other Slabs (in relevant tags, e.g., Moss Slabs) -> Path Slabs</li>
     * </ul>
     * Note: The transformation for "Other" variants into Path variants is currently one-way into the Dirt/Path cycle
     * (e.g., Podzol Stair -> Path Stair -> Dirt Stair).
     * <p>
     * Relies on BlockRegistry providing non-null block instances (real or fallback).
     * Uses a try-catch for robustness during property transfer.
     *
     * @param source The original BlockState that was right-clicked (guaranteed to be registered).
     * @return The correctly transformed BlockState with properties transferred,
     * or the original source state if no transformation applies or would result in a fallback.
     * Returns null only on critical internal errors.
     */
    @Unique
    @Nullable
    private static BlockState getTargetState(BlockState source) {
       BlockState target = source; // Default to original state
       try {
           logger.info("Shovel Mixin: Attempting to transform {}", Registries.BLOCK.getId(source.getBlock()));
          // --- Retrieve required block instances (real or fallback) ---
          // These are guaranteed non-null by the BlockRegistry implementation.
          Block dirtPathStairsBlock = BlockRegistry.INSTANCE.getDIRT_PATH_STAIR();
          Block dirtPathSlabBlock = BlockRegistry.INSTANCE.getDIRT_PATH_SLAB();
          Block dirtStairsBlock = BlockRegistry.INSTANCE.getDIRT_STAIR();
          Block dirtSlabBlock = BlockRegistry.INSTANCE.getDIRT_SLAB();

          // Get default states (safe now, as blocks are non-null)
          BlockState stairPathState = dirtPathStairsBlock.getDefaultState();
          BlockState slabPathState = dirtPathSlabBlock.getDefaultState();
          BlockState stairDirtState = dirtStairsBlock.getDefaultState();
          BlockState slabDirtState = dirtSlabBlock.getDefaultState();

          // --- Transformation Logic ---
          Block sourceBlock = source.getBlock();
          BlockState potentialTargetState;

          if (sourceBlock instanceof StairsBlock) {
              // If source is PathStairs -> target DirtStairs, else target PathStairs
              potentialTargetState = (sourceBlock == dirtPathStairsBlock) ? stairDirtState : stairPathState;
              target = potentialTargetState
                      .with(StairsBlock.FACING, source.get(StairsBlock.FACING))
                      .with(StairsBlock.HALF, source.get(StairsBlock.HALF))
                      .with(StairsBlock.SHAPE, source.get(StairsBlock.SHAPE))
                      .with(StairsBlock.WATERLOGGED, source.get(StairsBlock.WATERLOGGED));
          } else if (sourceBlock instanceof SlabBlock) {
              // If source is PathSlab -> target DirtSlab, else target PathSlab
              potentialTargetState = (sourceBlock == dirtPathSlabBlock) ? slabDirtState : slabPathState;
              target = potentialTargetState
                      .with(SlabBlock.TYPE, source.get(SlabBlock.TYPE))
                      .with(SlabBlock.WATERLOGGED, source.get(SlabBlock.WATERLOGGED));
          } else if (sourceBlock == DIRT_PATH) {
              // Path -> Dirt
              target = DIRT.getDefaultState();
          }

          // --- Sanity Check: Prevent transforming INTO a known fallback block ---
          // This prevents Dirt_Stairs -> Oak_Stairs if Path_Stairs are disabled etc.
          Block targetBlock = target.getBlock();
          if (target != source && (targetBlock == FALLBACK_STAIR || targetBlock == FALLBACK_SLAB)) {
               logger.warn("Shovel Mixin: Prevented transformation of {} into fallback block {}. Target block likely not registered correctly.",
                            Registries.BLOCK.getId(sourceBlock), Registries.BLOCK.getId(targetBlock));
               target = source; // Abort transformation
          }

       } catch (IllegalArgumentException e) {
          logger.error("Failed to transfer block state properties from {}: {}",
                Registries.BLOCK.getId(source.getBlock()), e.getMessage());
          target = source; // Fallback safely to original state
       } catch (Exception e) {
            logger.error("Unexpected error during getTargetState for {}: {}", Registries.BLOCK.getId(source.getBlock()), e.getMessage(), e);
            return null; // Signal critical error
       }

       return target;
    }
}