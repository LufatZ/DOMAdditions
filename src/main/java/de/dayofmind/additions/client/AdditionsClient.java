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
        TintBlocks.TintGrassBlocks();
        TextureCutOut.CutOut();
    }
}
