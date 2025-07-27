package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At(value = "TAIL"), method = "<init>")
    protected void init(Item.Properties properties, CallbackInfo ci) {
        Item item = (Item) (Object) this;

        if (item instanceof IRelicItem relic)
            RelicStorage.RELIC_TEMPLATES.put(relic, relic.getDefaultRelicTemplate()); // TODO: Use dynamic template?
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected, CallbackInfo ci) {
        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity) || !(stack.getItem() instanceof IRelicItem relic))
            return;

        for (var entry : relic.getAbilitiesTemplate(livingEntity, stack).getAbilities().entrySet()) {
            String ability = entry.getKey();

            if (relic.getAbilityCooldown(livingEntity, stack, ability) > 0)
                relic.addAbilityCooldown(livingEntity, stack, ability, -1);
        }

        if (livingEntity.tickCount % 20 == 0)
            relic.addRelicMetricValue(livingEntity, stack, "retention_time", 1);
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
            tooltip.add(Component.translatable("tooltip.relics.researching.info", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" "));
    }

    // TODO: I think there should be less nulls :/
    @Inject(method = "verifyComponentsAfterLoad", at = @At("HEAD"))
    public void onVerifyComponentsAfterLoad(ItemStack stack, CallbackInfo ci) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        for (AbilityTemplate abilityData : relic.getDefaultAbilitiesTemplate().getAbilities().values()) {
            String abilityId = abilityData.getId();

            if (relic.getAbilityComponent(null, stack, abilityId) == null)
                relic.randomizeAbilityStats(null, stack, abilityId);
            else {
                for (StatTemplate statData : relic.getDefaultAbilityTemplate(abilityId).getStats().values()) {
                    String statId = statData.getId();

                    if (relic.getStatComponent(null, stack, abilityId, statId) == null)
                        relic.randomizeStat(null, stack, abilityId, statId);
                }
            }
        }
    }
}