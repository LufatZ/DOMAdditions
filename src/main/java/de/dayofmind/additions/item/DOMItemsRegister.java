package de.dayofmind.additions.item;

import de.dayofmind.additions.config.ModConfig;
import de.dayofmind.additions.entity.EntityRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.block.DOMBlocksRegister.DayOfMind;

public class DOMItemsRegister {

    //SpawnEgg of experimental Entities
    public static Item WANDERING_MUSICAN_SPAWN_EGG(){
        if (ModConfig.ExperimentalSettings.ExperimentalEntities){
            return new SpawnEggItem(EntityRegister.WANDERING_MUSICAN, 0x1F1F1F, 0xE1C288,(new Item.Settings().group(DayOfMind).maxCount(1)));
        }
        return null;
    }
    //material nuggets
    public static Item NETHERITE_NUGGET(){
        return new Item((new Item.Settings().group(ItemGroup.MATERIALS).group(DayOfMind)));
    }
    public static Item COPPER_NUGGET(){
        return new Item((new Item.Settings().group(ItemGroup.MATERIALS).group(DayOfMind)));
    }
    public static void registerItems(){
        System.out.println("DOM | Adding items");

        //SpawnEgg of experimental Entities
        if (ModConfig.ExperimentalSettings.ExperimentalEntities) {
            registerItem("wandering_musican_spawn_egg", WANDERING_MUSICAN_SPAWN_EGG());
        }
        //material nuggets
        registerItem("netherite_nugget", NETHERITE_NUGGET());
        registerItem("copper_nugget", COPPER_NUGGET());

        System.out.println("DOM | DayOfMind successful added items to minecraft");
    }
    private static void registerItem(String name, Item item){
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, name), item);
    }
}
