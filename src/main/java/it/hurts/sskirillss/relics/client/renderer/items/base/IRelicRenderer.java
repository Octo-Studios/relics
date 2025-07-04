package it.hurts.sskirillss.relics.client.renderer.items.base;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IRelicRenderer {
    default ResourceLocation getFlawlessOrDefaultTexture(LivingEntity entity, ItemStack stack, ResourceLocation source) {
        var isFlawless = ((IRelicItem) stack.getItem()).isRelicFlawless(entity, stack);

        return isFlawless ? this.constructFlawlessTexture(source) : source;
    }

    default ResourceLocation constructFlawlessTexture(ResourceLocation source) {
        return ResourceLocation.parse(source.toString().replaceFirst("\\.png$", "_flawless.png"));
    }
}