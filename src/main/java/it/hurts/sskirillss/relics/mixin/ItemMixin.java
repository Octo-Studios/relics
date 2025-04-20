package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At(value = "TAIL"), method = "<init>")
    protected void init(Item.Properties properties, CallbackInfo ci) {
        Item item = (Item) (Object) this;

        if (item instanceof IRelicItem relic)
            RelicStorage.RELIC_TEMPLATES.put(relic, relic.getRelicTemplate());
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected, CallbackInfo ci) {
        if (level.isClientSide() || !(stack.getItem() instanceof IRelicItem relic))
            return;

        for (Map.Entry<String, AbilityTemplate> entry : relic.getRelicTemplate().getAbilities().getAbilities().entrySet()) {
            String ability = entry.getKey();

            if (relic.getAbilityCooldown(stack, ability) > 0)
                relic.addAbilityCooldown(stack, ability, -1);
        }
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        relics$processTooltip(stack, context, tooltip);
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private void relics$processTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip) {
        Item item = stack.getItem();

        if (!(item instanceof IRelicItem))
            return;

        tooltip.add(Component.literal(" "));

        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu>)
            tooltip.add(Component.translatable("tooltip.relics.researching.info", HotkeyRegistry.RESEARCH_RELIC.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" "));
    }

    @Inject(method = "verifyComponentsAfterLoad", at = @At("HEAD"))
    public void onVerifyComponentsAfterLoad(ItemStack stack, CallbackInfo ci) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        for (AbilityTemplate abilityData : relic.getAbilitiesData().getAbilities().values()) {
            String abilityId = abilityData.getId();

            if (relic.getAbilityComponent(stack, abilityId) == null)
                relic.randomizeAbilityStats(stack, abilityId, 0);
            else {
                for (StatTemplate statData : relic.getAbilityData(abilityId).getStats().values()) {
                    String statId = statData.getId();

                    if (relic.getStatComponent(stack, abilityId, statId) == null)
                        relic.randomizeStat(stack, abilityId, statId);
                }
            }
        }
    }
}