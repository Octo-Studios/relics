package it.hurts.sskirillss.relics.client.renderer.items;

import it.hurts.sskirillss.relics.client.models.items.JellyfishNecklaceModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.AbstractNecklaceRenderer;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class JellyfishNecklaceRenderer extends AbstractNecklaceRenderer<LivingEntity, JellyfishNecklaceModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/item/model/jellyfish_necklace.png");

    public JellyfishNecklaceRenderer() {
        super(() -> new JellyfishNecklaceModel(Minecraft.getInstance().getEntityModels().bakeLayer(JellyfishNecklaceModel.LAYER)), TEXTURE);
    }
}