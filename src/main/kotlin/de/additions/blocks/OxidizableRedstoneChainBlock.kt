package de.additions.blocks

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.Oxidizable
import net.minecraft.block.Oxidizable.OxidationLevel
import net.minecraft.block.OxidizableChainBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import java.util.function.Function

class OxidizableRedstoneChainBlock (private var oxidationLevel: OxidationLevel, settings: Settings): RedstoneChainBlock(settings), Oxidizable {
    val CODEC: MapCodec<OxidizableChainBlock?> =
        RecordCodecBuilder.mapCodec<OxidizableChainBlock?>(Function { instance: RecordCodecBuilder.Instance<OxidizableChainBlock?>? ->
            instance!!.group(
                OxidationLevel.CODEC.fieldOf("weathering_state")
                    .forGetter<OxidizableChainBlock?>(Function { obj: OxidizableChainBlock? -> obj!!.degradationLevel }),
                createSettingsCodec<OxidizableChainBlock?>()
            ).apply<OxidizableChainBlock?>(
                instance
            ) { oxidationLevel: OxidationLevel?, settings: Settings? ->
                OxidizableChainBlock(
                    oxidationLevel,
                    settings
                )
            }
        })

    override fun getCodec(): MapCodec<OxidizableChainBlock?> {
        return CODEC
    }

    override fun randomTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        this.tickDegradation(state, world, pos, random)
    }

    override fun hasRandomTicks(state: BlockState): Boolean {
        return Oxidizable.getIncreasedOxidationBlock(state.block).isPresent
    }

    override fun getDegradationLevel(): OxidationLevel {
        return this.oxidationLevel
    }
}