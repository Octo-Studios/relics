package it.hurts.sskirillss.relics.client.renderer.items;

import it.hurts.sskirillss.relics.client.models.items.ReflectiveNecklaceModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.AbstractNecklaceRenderer;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class ReflectiveNecklaceRenderer extends AbstractNecklaceRenderer<LivingEntity, ReflectiveNecklaceModel> {
    public ReflectiveNecklaceRenderer() {
        super(() -> new ReflectiveNecklaceModel(Minecraft.getInstance().getEntityModels().bakeLayer(ReflectiveNecklaceModel.LAYER)));
    }

    @Override
    public ResourceLocation getDefaultTexture(ItemStack stack, SlotContext slotContext) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/item/model/reflective_necklace.png");
    }
}