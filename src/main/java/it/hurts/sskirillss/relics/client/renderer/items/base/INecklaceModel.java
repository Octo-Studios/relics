package it.hurts.sskirillss.relics.client.renderer.items.base;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface INecklaceModel<T extends LivingEntity> {
    void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks);

    void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    ModelPart getBodyPart();
}