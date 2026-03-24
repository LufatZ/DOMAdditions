package de.additions.blocks.lanterns

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.world.level.block.WeatheringLanternBlock
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import java.util.function.Function

class SmallOxidizableRedstoneLantern (private var oxidationLevel: WeatherState, settings: Properties) :  SmallRedstoneLantern (settings),
    WeatheringCopper {
    override fun codec(): MapCodec<WeatheringLanternBlock> {
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

    companion object {
        val CODEC: MapCodec<WeatheringLanternBlock> =
            RecordCodecBuilder.mapCodec(
                Function { instance: RecordCodecBuilder.Instance<WeatheringLanternBlock>->
                    instance.group(
                        WeatherState.CODEC.fieldOf("weathering_state")
                            .forGetter { obj: WeatheringLanternBlock -> obj.age },
                        propertiesCodec<WeatheringLanternBlock>()
                    ).apply(
                        instance
                    ) { oxidationLevel: WeatherState, settings: Properties ->
                        WeatheringLanternBlock(
                            oxidationLevel,
                            settings
                        )
                    }
                })
    }
}
