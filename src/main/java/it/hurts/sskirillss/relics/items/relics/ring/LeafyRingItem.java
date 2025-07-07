package it.hurts.sskirillss.relics.items.relics.ring;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;
import top.theillusivec4.curios.api.SlotContext;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.PROGRESS;
import static it.hurts.sskirillss.relics.init.DataComponentRegistry.TOGGLED;

public class LeafyRingItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("hide")
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.1D, 0.35D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff164f00)
                                .borderBottom(0xff164f00)
                                .textured(true)
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        var level = player.getCommandSenderWorld();

        {
            var progress = getCurrentProgress(stack);
            var hiding = isHiding(stack);

            var pos = player.getBoundingBox().getBottomCenter().add(0F, player.getBbHeight(), 0F);

            if (player.isShiftKeyDown() && level.getBlockState(new BlockPos((int) Math.floor(pos.x()), (int) Math.floor(pos.y()), (int) Math.floor(pos.z()))).getBlock() instanceof BushBlock) {
                if (!hiding)
                    setHiding(stack, true);

                if (progress < getMaxProgress())
                    addCurrentProgress(stack, 1);

                player.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));
            } else {
                if (hiding)
                    setHiding(stack, false);

                if (progress > 0)
                    addCurrentProgress(stack, -1);
            }
        }
    }

    public boolean isHiding(ItemStack stack) {
        return stack.getOrDefault(TOGGLED, false);
    }

    public void setHiding(ItemStack stack, boolean hiding) {
        stack.set(TOGGLED, hiding);
    }

    public int getCurrentProgress(ItemStack stack) {
        return stack.getOrDefault(PROGRESS, 0);
    }

    public void setCurrentProgress(ItemStack stack, int progress) {
        stack.set(PROGRESS, Math.clamp(progress, 0, getMaxProgress()));
    }

    public void addCurrentProgress(ItemStack stack, int progress) {
        setCurrentProgress(stack, getCurrentProgress(stack) + progress);
    }

    public int getMaxProgress() {
        return 10;
    }
}