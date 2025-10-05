package it.hurts.sskirillss.relics.items.relics.base;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsRelicStyles;
import it.hurts.sskirillss.relics.items.ItemBase;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

public abstract class RelicItem extends ItemBase implements ICurioItem, IRelicItem {
    public RelicItem(Item.Properties properties) {
        super(properties);
    }

    public RelicItem() {
        super(new Item.Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1));
    }

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

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public String getConfigRoute() {
        return Relics.MODID;
    }

    @Override
    public Component getName(ItemStack stack) {
        var base = super.getName(stack);
        var text = base.getString();

        if (text.isEmpty())
            return base;

        var optional = RelicsRelicStyles.getStyle(this);

        if (optional.isEmpty())
            return base;

        var colors = optional.get().getItemNameColors(null, stack);

        if (colors.isEmpty())
            return base;

        var result = Component.empty();

        var length = text.length();

        var spread = Math.max(1F, length * 1.25F);
        var speed = 1F;

        var time = (System.nanoTime() * 1e-9F);

        var colorCount = colors.size();

        for (int i = 0; i < length; i++) {
            var x = i / spread - time * speed;
            var tri = 1F - Math.abs((x % 2F) - 1F);
            var w = 0.5F - 0.5f * (float) Math.cos(tri * Math.PI);

            var scaled = w * (colorCount - 1);

            var idx1 = (int) Math.floor(scaled);
            var idx2 = (idx1 + 1) % colorCount;

            var localT = scaled - idx1;

            var color = colors.get(idx1).lerp(colors.get(idx2), localT);

            int rgb = color.getARGB() & 0xFFFFFF;

            result.append(Component.literal(String.valueOf(text.charAt(i))).setStyle(base.getStyle().withColor(TextColor.fromRgb(rgb))));
        }

        return result;
    }
}