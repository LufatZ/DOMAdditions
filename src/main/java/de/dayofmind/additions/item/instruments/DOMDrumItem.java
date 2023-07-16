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

public class DOMDrumItem extends BlockItem {
    public DOMDrumItem(Block block, Settings settings) {
        super(block, settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        float volume = 1.0F;
        float pitch = 1.0F;
        world.playSoundFromEntity(player, player,SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), SoundCategory.RECORDS, volume, pitch);
        world.emitGameEvent(player, GameEvent.INSTRUMENT_PLAY, player.getBlockPos());
        return TypedActionResult.consume(itemStack);
    }
}
