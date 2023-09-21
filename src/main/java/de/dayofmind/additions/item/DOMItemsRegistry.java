package de.dayofmind.additions.item;

import de.dayofmind.additions.item.instruments.DOMGuitarItem;
import de.dayofmind.additions.item.tools.DOMMultiBlockTool;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.block.DOMBlocksRegistry.GUITAR;

public class DOMItemsRegistry {

    //Tools
        public static final Item BIG_SHOVEL = new DOMMultiBlockTool(5.5F,1F, ToolMaterials.IRON, BlockTags.SHOVEL_MINEABLE, new FabricItemSettings());
        public static final Item HAMMER = new DOMMultiBlockTool(5F, 1.2F, ToolMaterials.IRON, BlockTags.PICKAXE_MINEABLE, new FabricItemSettings());

    //SpawnEgg of experimental Entities

    //material nuggets
        public static final Item NETHERITE_NUGGET = new Item(new FabricItemSettings());
        public static final Item COPPER_NUGGET = new Item(new FabricItemSettings());
    //instruments
        public static final Item GUITAR_ITEM = new DOMGuitarItem(GUITAR,(new Item.Settings()));
        //public static final Item DRUM_ITEM = new DOMDrumItem(DRUM,(new Item.Settings()));
        //public static final Item FLUTE_ITEM = new DOMFluteItem(FLUTE,(new Item.Settings()));


    public static void registerItems(){
        System.out.println("DOM | Adding items");
        //Tools
            registerItem("big_shovel", BIG_SHOVEL);
            registerItem("hammer", HAMMER);
        //SpawnEgg of experimental Entities

        //material nuggets
            registerItem("netherite_nugget", NETHERITE_NUGGET);
            registerItem("copper_nugget", COPPER_NUGGET);

        System.out.println("DOM | DayOfMind successful added items to minecraft");
    }
    private static void registerItem(String name, Item item){
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
    }
}
