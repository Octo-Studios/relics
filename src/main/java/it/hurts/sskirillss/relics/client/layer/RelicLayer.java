package it.hurts.sskirillss.relics.client.layer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public abstract class RelicLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public final RenderLayerParent<T, M> renderLayerParent;

    public RelicLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);

        this.renderLayerParent = renderer;
    }
}
