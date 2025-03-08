package it.hurts.sskirillss.relics.items.relics.base;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.items.ItemBase;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.misc.ICreativeTabContent;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

public abstract class RelicItem extends ItemBase implements ICurioItem, IRelicItem, ICreativeTabContent {
    public RelicItem(Item.Properties properties) {
        super(properties);
    }

    public RelicItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE)
                .stacksTo(1));
    }

    @Override
    @Deprecated(forRemoval = true)
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();

        RelicAttributeModifier attributes = getRelicAttributeModifiers(stack);
        RelicSlotModifier slots = getSlotModifiers(stack);

        if (attributes != null)
            attributes.getAttributes().forEach(attribute ->
                    modifiers.put(attribute.getAttribute(), new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(Reference.MODID, BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "_" + BuiltInRegistries.ATTRIBUTE.getKey(attribute.getAttribute().value()).getPath() + "_" + slotContext.identifier() + "_" + slotContext.index()),
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
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        constructor.entry(CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, this);
    }

    @Override
    public String getConfigRoute() {
        return Reference.MODID;
    }
}