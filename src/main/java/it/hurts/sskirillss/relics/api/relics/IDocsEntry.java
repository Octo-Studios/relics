package it.hurts.sskirillss.relics.api.relics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IDocsEntry {
    @Nullable
    default String getURI(LivingEntity entity, ItemStack stack) {
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());

        return "https://shatterbyte.com/docs/mods/" + key.getNamespace() + "/relics/" + key.getPath() + "/";
    }
}
