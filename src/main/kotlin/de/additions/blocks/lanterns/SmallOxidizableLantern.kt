package de.additions.blocks.lanterns

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.Oxidizable
import net.minecraft.block.Oxidizable.OxidationLevel
import net.minecraft.block.OxidizableLanternBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import java.util.function.Function

class SmallOxidizableLantern (private var oxidationLevel: OxidationLevel, settings: Settings?) :  SmallLantern (settings), Oxidizable {
    override fun getCodec(): MapCodec<OxidizableLanternBlock?> {
        return CODEC
    }

    override fun randomTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        this.tickDegradation(state, world, pos, random)
    }

    override fun hasRandomTicks(state: BlockState): Boolean {
        return Oxidizable.getIncreasedOxidationBlock(state.block).isPresent
    }

    override fun getDegradationLevel(): OxidationLevel? {
        return this.oxidationLevel
    }

    companion object {
        val CODEC: MapCodec<OxidizableLanternBlock?> =
            RecordCodecBuilder.mapCodec<OxidizableLanternBlock?>(
                Function { instance: RecordCodecBuilder.Instance<OxidizableLanternBlock?>? ->
                    instance!!.group<OxidationLevel?, Settings?>(
                        OxidationLevel.CODEC.fieldOf("weathering_state")
                            .forGetter<OxidizableLanternBlock?> { obj: OxidizableLanternBlock? -> obj!!.degradationLevel },
                        createSettingsCodec<OxidizableLanternBlock?>()
                    ).apply<OxidizableLanternBlock?>(
                        instance
                    ) { oxidationLevel: OxidationLevel?, settings: Settings? ->
                        OxidizableLanternBlock(
                            oxidationLevel,
                            settings!!
                        )
                    }
                })
    }
}
