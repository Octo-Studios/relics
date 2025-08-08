package it.hurts.sskirillss.relics.client.layer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;

public class FeetLayer<T extends LivingEntity, M extends EntityModel<T>> extends EquippableRelicLayer<T, M> {
    public FeetLayer(RenderLayerParent<T, M> renderer) {
        super(renderer, "feet");
    }
}