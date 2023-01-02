package de.dayofmind.additions.client;

import de.dayofmind.additions.client.block.TextureCutOut;
import de.dayofmind.additions.client.block.TintBlocks;
import me.lortseam.completeconfig.gui.ConfigScreenBuilder;
import me.lortseam.completeconfig.gui.yacl.YaclScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static de.dayofmind.additions.Additions.MOD_ID;

@Environment(EnvType.CLIENT)
public class AdditionsClient implements ClientModInitializer {
 @Override
    public void onInitializeClient() {
        //Config Screen(must be on client side)
        System.out.println("DOM | creating the config screen on the client");
        ConfigScreenBuilder.setMain(MOD_ID, new YaclScreenBuilder());
        //texture stuff
        System.out.println("DOM | Colors are added to blocks");
        TintBlocks.TintGrassBlocks();
        System.out.println("DOM | blocks become transparent");
        TextureCutOut.CutOut();
        //ENTITY
    }
}
