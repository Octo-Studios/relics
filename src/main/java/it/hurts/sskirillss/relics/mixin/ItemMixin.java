package it.hurts.sskirillss.relics.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsRelicStyles;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

        var relicData = relic.getRelicData(livingEntity, stack);

        if (livingEntity.tickCount % 20 == 0)
            relicData.getStatisticData().getMetricData("retention_time").addValue(1);
    }

    // TODO: I think there should be less nulls :/
    @Inject(method = "verifyComponentsAfterLoad", at = @At("HEAD"))
    public void onVerifyComponentsAfterLoad(ItemStack stack, CallbackInfo ci) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var relicData = relic.getRelicData(null, stack);
        var abilitiesData = relicData.getAbilitiesData();

        for (AbilityTemplate abilityData : relic.getDefaultAbilitiesTemplate().getAbilities().values()) {
            String abilityId = abilityData.getId();
            var ability = abilitiesData.getAbilityData(abilityId);

            if (ability == null || ability.getComponent() == null) {
                if (ability != null)
                    ability.randomizeStats();

                continue;
            } else {
                for (AbilityStatTemplate statData : relic.getDefaultAbilityTemplate(abilityId).getStats().values()) {
                    String statId = statData.getId();

                    if (ability.getStatData(statId).getComponent() == null)
                        ability.randomizeStat(statId);
                }
            }
        }
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component getName(Component original, ItemStack stack) {
        var item = stack.getItem();

        if (!(item instanceof IRelicItem) || original == null)
            return original;

        var text = original.getString();

        if (text.isEmpty())
            return original;

        var optional = RelicsRelicStyles.getStyle(item);

        if (optional.isEmpty())
            return original;

        var colors = optional.get().getItemNameColors(null, stack);

        if (colors.isEmpty())
            return original;

        var result = Component.empty();
        var length = text.length();

        var spread = Math.max(1F, length * 1.25F);
        var speed = 1F;
        var time = System.nanoTime() * 1e-9F;
        var colorCount = colors.size();

        for (var i = 0; i < length; i++) {
            var x = i / spread - time * speed;
            var tri = 1F - Math.abs((x % 2F) - 1F);
            var w = 0.5F - 0.5F * (float) Math.cos(tri * Math.PI);

            var scaled = w * (colorCount - 1);
            var idx1 = (int) Math.floor(scaled);
            var idx2 = (idx1 + 1) % colorCount;
            var localT = scaled - idx1;

            var color = colors.get(idx1).lerp(colors.get(idx2), localT);
            var rgb = color.getARGB() & 0xFFFFFF;

            result.append(
                    Component.literal(String.valueOf(text.charAt(i)))
                            .setStyle(original.getStyle().withColor(TextColor.fromRgb(rgb)))
            );
        }

        return result;
    }
}
