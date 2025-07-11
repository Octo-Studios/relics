package it.hurts.sskirillss.relics.client.renderer.items;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.ReflectiveNecklaceModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.AbstractNecklaceRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ReflectiveNecklaceRenderer extends AbstractNecklaceRenderer<LivingEntity, ReflectiveNecklaceModel> {
    public ReflectiveNecklaceRenderer() {
        super(() -> new ReflectiveNecklaceModel(Minecraft.getInstance().getEntityModels().bakeLayer(ReflectiveNecklaceModel.LAYER)), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/reflective_necklace.png"));
    }
}