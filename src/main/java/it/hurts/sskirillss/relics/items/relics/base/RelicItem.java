package it.hurts.sskirillss.relics.items.relics.base;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.hurts.sskirillss.relics.items.ItemBase;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public abstract class RelicItem extends ItemBase implements ICurioItem, IRelicItem {
    public RelicItem(Item.Properties properties) {
        super(properties);
    }

    public RelicItem() {
        super(new Item.Properties()
                .tab(RelicsTab.RELICS_TAB)
                .rarity(Rarity.RARE)
                .stacksTo(1));
    }

    @Override
    @Deprecated(forRemoval = true)
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        if (DurabilityUtils.isBroken(stack))
            return modifiers;

        RelicAttributeModifier attributes = getAttributeModifiers(stack);
        RelicSlotModifier slots = getSlotModifiers(stack);

        if (attributes != null)
            attributes.getAttributes().forEach(attribute ->
                    modifiers.put(attribute.getAttribute(), new AttributeModifier(uuid,
                            ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + "_" + ForgeRegistries.ATTRIBUTES.getKey(attribute.getAttribute()).getPath(),
                            attribute.getMultiplier(), attribute.getOperation())));

        if (slots != null)
            slots.getModifiers().forEach(slot ->
                    CuriosApi.getCuriosHelper().addSlotModifier(modifiers, slot.getLeft(), uuid, slot.getRight(), AttributeModifier.Operation.ADDITION));

        return modifiers;
    }

    @Override
    public boolean showAttributesTooltip(String identifier, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEquip(String identifier, LivingEntity livingEntity, ItemStack stack) {
        return !DurabilityUtils.isBroken(stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return !DurabilityUtils.isBroken(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return switch (stack.getRarity()) {
            case COMMON -> 100;
            case UNCOMMON -> 150;
            case RARE -> 200;
            case EPIC -> 250;
        };
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return stack.getMaxDamage() > 0;
    }

    // Mojank moment

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) getMaxDamage(stack));
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
//        Triple<String, String, String> color = getStyle(stack).getDurability();
//
//        if (color == null)
        return Mth.hsvToRgb(Math.max(0F, ((float) getMaxDamage(stack) - (float) stack.getDamageValue()) / (float) getMaxDamage(stack)) / 3F, 1F, 1F);
//
//        float percentage = stack.getDamageValue() * 100F / getMaxDamage(stack);
//
//        return Color.decode(percentage < 33.3F ? color.getLeft() : percentage < 66.6F ? color.getMiddle() : color.getRight()).getRGB();
    }
}