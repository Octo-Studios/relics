package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.client.renderer.items.models.JellyfishNecklaceModel;
import it.hurts.sskirillss.relics.client.tooltip.base.AbilityTooltip;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicTooltip;
import it.hurts.sskirillss.relics.configs.data.relics.RelicConfigData;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStats;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class JellyfishNecklaceItem extends RelicItem<JellyfishNecklaceItem.Stats> {
    public static JellyfishNecklaceItem INSTANCE;

    public JellyfishNecklaceItem() {
        super(RelicData.builder()
                .rarity(Rarity.RARE)
                .hasAbility()
                .build());

        INSTANCE = this;
    }

    @Override
    public RelicTooltip getTooltip(ItemStack stack) {
        return RelicTooltip.builder()
                .borders("#53b2f8", "#0d4065")
                .ability(AbilityTooltip.builder()
                        .arg("+" + (int) (stats.healMultiplier * 100 - 100) + "%")
                        .build())
                .ability(AbilityTooltip.builder()
                        .arg("+" + (int) (stats.magicResistance * 100) + "%")
                        .build())
                .ability(AbilityTooltip.builder()
                        .arg("-" + (int) Math.abs(stats.swimSpeedModifier * 100) + "%")
                        .negative()
                        .build())
                .ability(AbilityTooltip.builder()
                        .active()
                        .build())
                .build();
    }

    @Override
    public RelicConfigData<Stats> getConfigData() {
        return RelicConfigData.<Stats>builder()
                .stats(new Stats())
                .build();
    }

    @Override
    public RelicAttributeModifier getAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(ForgeMod.SWIM_SPEED.get(), stats.swimSpeedModifier))
                .build();
    }

    @Override
    public void castAbility(PlayerEntity player, ItemStack stack) {
        if (!player.isInWaterOrRain() || player.getCooldowns().isOnCooldown(stack.getItem()))
            return;

        float rot = ((float) Math.PI / 180F);
        float f0 = MathHelper.cos(player.xRot * rot);
        float f1 = -MathHelper.sin(player.yRot * rot) * f0;
        float f2 = -MathHelper.sin(player.xRot * rot);
        float f3 = MathHelper.cos(player.yRot * rot) * f0;
        float f4 = MathHelper.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
        float f5 = stats.riptidePower;
        f1 *= (f5 / f4);
        f2 *= (f5 / f4);
        f3 *= (f5 / f4);

        player.push(f1, f2, f3);
        player.startAutoSpinAttack(20);

        player.getCooldowns().addCooldown(stack.getItem(), stats.riptideCooldown * 20);

        player.getCommandSenderWorld().playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_3, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel<LivingEntity> getModel() {
        return new JellyfishNecklaceModel();
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class JellyfishNecklaceEvents {
        @SubscribeEvent
        public static void onEntityHeal(LivingHealEvent event) {
            Stats stats = INSTANCE.stats;
            LivingEntity entity = event.getEntityLiving();

            if (EntityUtils.findEquippedCurio(entity, ItemRegistry.JELLYFISH_NECKLACE.get()).isEmpty()
                    || !entity.isInWater())
                return;

            event.setAmount(event.getAmount() * stats.healMultiplier);
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            Stats stats = INSTANCE.stats;

            if (EntityUtils.findEquippedCurio(event.getEntityLiving(), ItemRegistry.JELLYFISH_NECKLACE.get()).isEmpty()
                    || event.getSource() != DamageSource.MAGIC)
                return;

            event.setAmount(event.getAmount() * stats.magicResistance);
        }
    }

    public static class Stats extends RelicStats {
        public float swimSpeedModifier = -0.2F;
        public float magicResistance = 0.25F;
        public float healMultiplier = 3.0F;
        public int riptideCooldown = 5;
        public float riptidePower = 2.0F;
    }
}