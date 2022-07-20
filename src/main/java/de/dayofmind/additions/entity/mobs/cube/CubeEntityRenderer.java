package de.dayofmind.additions.entity.mobs.cube;

import de.dayofmind.additions.client.AdditionsClient;
import de.dayofmind.additions.entity.mobs.cube.CubeEntity;
import de.dayofmind.additions.entity.mobs.cube.CubeEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CubeEntityRenderer extends MobEntityRenderer<CubeEntity, CubeEntityModel> {

    public CubeEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new CubeEntityModel(context.getPart(AdditionsClient.MODEL_CUBE_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(CubeEntity entity) {
        return new Identifier("entitytesting", "textures/entity/cube/cube.png");
    }
}