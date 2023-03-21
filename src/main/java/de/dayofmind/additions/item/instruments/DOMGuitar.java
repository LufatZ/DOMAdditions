package de.dayofmind.additions.item.instruments;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DOMGuitar extends BlockItem {
    public DOMGuitar(Block block, Settings settings) {
        super(block, settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        float f = 16.0F;
        world.playSoundFromEntity(player, player,SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, SoundCategory.RECORDS, f, 1.0F);
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
        return TypedActionResult.consume(itemStack);
    }
}
