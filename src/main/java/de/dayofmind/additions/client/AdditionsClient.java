package de.dayofmind.additions.client;

import de.dayofmind.additions.client.block.TextureCutOut;
import de.dayofmind.additions.client.block.TintBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AdditionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //texture stuff
        System.out.println("DOM | Colors are added to blocks");
        TintBlocks.tintblocks();
        System.out.println("DOM | blocks become transparent");
        TextureCutOut.cutout();
    }
}
