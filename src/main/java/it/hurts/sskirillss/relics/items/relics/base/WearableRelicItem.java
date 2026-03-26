package it.hurts.sskirillss.relics.items.relics.base;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

public class WearableRelicItem extends RelicItem implements ICurioItem {
    @Override
    @Deprecated(forRemoval = true)
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();

        RelicAttributeModifier attributes = getRelicAttributeModifiers(slotContext.entity(), stack);
        RelicSlotModifier slots = getSlotModifiers(slotContext.entity(), stack);

        if (attributes != null)
            attributes.getAttributes().forEach(attribute ->
                    modifiers.put(attribute.getAttribute(), new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(Relics.MODID, BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "_" + BuiltInRegistries.ATTRIBUTE.getKey(attribute.getAttribute().value()).getPath() + "_" + slotContext.identifier() + "_" + slotContext.index()),
                            attribute.getMultiplier(), attribute.getOperation())));

        if (slots != null)
            slots.getModifiers().forEach((slot, count) -> CuriosApi.addSlotModifier(modifiers, slot, id, count, AttributeModifier.Operation.ADD_VALUE));

        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        return new ArrayList<>();
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}