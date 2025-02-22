package de.additions.blocks

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.POWER
import net.minecraft.state.property.Properties.POWERED
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RedstoneChainBlock(settings: Settings) : ChainBlock(settings) {
    init {
        this.defaultState = super.defaultState.with(POWERED, false).with(POWER, 0)
    }
    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>?) {
        builder?.add(POWERED, POWER, WATERLOGGED, AXIS)
    }
    /**
     * Ermittelt die maximale Redstone-Power und die entsprechende Richtung entlang der gegebenen Achse.
     *
     * @param world Die Welt, in der sich der Block befindet.
     * @param pos Die Position des Blocks.
     * @param axis Die zu prüfende Achse (X, Y oder Z).
     * @return Ein Pair, das den maximalen Power-Wert und die Richtung enthält, aus der dieser stammt.
     */
    fun getPowerFromAxis(world: World, pos: BlockPos, axis: Direction.Axis): Pair<Int, Direction> {
        var power = when (axis) {
            Direction.Axis.X -> {
                // Prüfe die benachbarten Blöcke im Osten und Westen
                val powerEast = world.getEmittedRedstonePower(pos.offset(Direction.EAST), Direction.EAST)
                val powerWest = world.getEmittedRedstonePower(pos.offset(Direction.WEST), Direction.WEST)
                if (powerEast >= powerWest) {
                    Pair(powerEast, Direction.EAST)
                } else {
                    Pair(powerWest, Direction.WEST)
                }
            }
            Direction.Axis.Y -> {
                // Prüfe die benachbarten Blöcke oben und unten
                val powerUp = world.getEmittedRedstonePower(pos.offset(Direction.UP), Direction.UP)
                val powerDown = world.getEmittedRedstonePower(pos.offset(Direction.DOWN), Direction.DOWN)
                if (powerUp >= powerDown) {
                    Pair(powerUp, Direction.UP)
                } else {
                    Pair(powerDown, Direction.DOWN)
                }
            }
            Direction.Axis.Z -> {
                // Prüfe die benachbarten Blöcke im Norden und Süden
                val powerNorth = world.getEmittedRedstonePower(pos.offset(Direction.NORTH), Direction.NORTH)
                val powerSouth = world.getEmittedRedstonePower(pos.offset(Direction.SOUTH), Direction.SOUTH)
                if (powerNorth >= powerSouth) {
                    Pair(powerNorth, Direction.NORTH)
                } else {
                    Pair(powerSouth, Direction.SOUTH)
                }
            }
        }
        if (world.getBlockState(pos.offset(power.second)).isOf(this) && world.getBlockState(pos.offset(power.second)).get(AXIS) != axis) {
            power = 0 to power.second
        }
        return power
    }
    /**
     * Determines the block state when the block is placed.
     * It checks if the placement location is waterlogged.
     * Also, it determines the power level and direction of the block based on the surrounding blocks.
     *
     *
     * @param ctx The item placement context.
     * @return The block state with the appropriate waterlogged property.
     */
    @Nullable
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val powered = getPowerFromAxis(ctx.world, ctx.blockPos, ctx.side.axis)
        return super.getPlacementState(ctx)?.with(POWER, powered.first)?.with(POWERED, powered.first > 0)
    }
    /**
     * Gibt an, ob dieser Block ein Redstonesignal abstrahlt.
     *
     * @param state Der aktuelle Blockzustand.
     * @return true, wenn das Signal ausgesendet werden soll.
     */
    override fun emitsRedstonePower(state: BlockState): Boolean {
        return state.get(POWERED)
    }

    /**
     * Liefert die schwache Redstone-Power, die von diesem Block in eine bestimmte Richtung abgegeben wird.
     *
     * @param state Der aktuelle Blockzustand.
     * @param world Der BlockView-Kontext.
     * @param pos Die Position des Blocks.
     * @param direction Die Richtung, in die das Signal abgegeben wird.
     * @return Der Power-Wert, üblicherweise aus dem POWER-Property.
     */
    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return  state.get(POWER) - 1
    }
}