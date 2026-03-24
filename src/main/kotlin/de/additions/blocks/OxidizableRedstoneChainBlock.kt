package de.additions.blocks

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.world.level.block.WeatheringCopperChainBlock
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import java.util.function.Function

class OxidizableRedstoneChainBlock (private var oxidationLevel: WeatherState, settings: Properties): RedstoneChainBlock(settings),
    WeatheringCopper {
    val CODEC: MapCodec<WeatheringCopperChainBlock> =
        RecordCodecBuilder.mapCodec(Function { instance: RecordCodecBuilder.Instance<WeatheringCopperChainBlock>->
            instance.group(
                WeatherState.CODEC.fieldOf("weathering_state")
                    .forGetter { obj: WeatheringCopperChainBlock -> obj.age },
                propertiesCodec<WeatheringCopperChainBlock>()
            ).apply(
                instance
            ) { oxidationLevel: WeatherState, settings: Properties->
                WeatheringCopperChainBlock(
                    oxidationLevel,
                    settings
                )
            }
        })

    override fun codec(): MapCodec<WeatheringCopperChainBlock> {
        return CODEC
    }

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        this.changeOverTime(state, world, pos, random)
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return WeatheringCopper.getNext(state.block).isPresent
    }

    override fun getAge(): WeatherState {
        return this.oxidationLevel
    }
}