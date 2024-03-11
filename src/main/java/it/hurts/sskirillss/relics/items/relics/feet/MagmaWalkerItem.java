package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class MagmaWalkerItem extends RelicItem {
    public static final String TAG_HEAT = "heat";

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("heat_resistance")
                                .maxLevel(0)
                                .build())
                        .ability(AbilityData.builder("pace")
                                .stat(StatData.builder("time")
                                        .initialValue(20D, 50D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(RelicStyleData.builder()
                        .borders("#dc41ff", "#832698")
                        .build())
                .build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        int heat = NBTUtils.getInt(stack, TAG_HEAT, 0);

        if (!(entity instanceof Player player) || player.tickCount % 20 != 0)
            return;

        if (heat > 0) {
            if (heat > getAbilityValue(stack, "pace", "time"))
                player.hurt(DamageSource.HOT_FLOOR, (float) (1F + ((heat - getAbilityValue(stack, "pace", "time")) / 10F)));

            if (!level.getFluidState(player.blockPosition().below()).is(FluidTags.LAVA)
                    && !level.getFluidState(player.blockPosition()).is(FluidTags.LAVA))
                NBTUtils.setInt(stack, TAG_HEAT, --heat);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        ItemStack stack = EntityUtils.findEquippedCurio(event.getEntity(), ItemRegistry.MAGMA_WALKER.get());

        if (stack.getItem() instanceof IRelicItem relic && event.getSource() == event.getEntity().level().damageSources().hotFloor()
                && NBTUtils.getInt(stack, TAG_HEAT, 0) <= relic.getAbilityValue(stack, "pace", "time")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFluidCollide(FluidCollisionEvent event) {
        ItemStack stack = EntityUtils.findEquippedCurio(event.getEntity(), ItemRegistry.MAGMA_WALKER.get());

        if (!(event.getEntity() instanceof Player player) || !(stack.getItem() instanceof IRelicItem relic)
                || !event.getFluid().is(FluidTags.LAVA) || player.isShiftKeyDown())
            return;

        if (player.tickCount % 20 == 0) {
            int heat = NBTUtils.getInt(stack, TAG_HEAT, 0);

            NBTUtils.setInt(stack, TAG_HEAT, ++heat);

            if (heat % 5 == 0)
                relic.addExperience(player, stack, 1);
        }

        event.setCanceled(true);
    }
}