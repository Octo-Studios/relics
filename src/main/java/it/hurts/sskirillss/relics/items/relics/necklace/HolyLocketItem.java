package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.DeathEssenceEntity;
import it.hurts.sskirillss.relics.entities.LifeEssenceEntity;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingSourceTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingSourcesTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Locale;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.MODE;

public class HolyLocketItem extends RelicItem {
    private static final int MAX_TARGETS = 10;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("faith")
                                .castData(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .icon((player, stack, ability) -> ability + "_" + getMode(stack).name().toLowerCase(Locale.ROOT))
                                .stat(StatTemplate.builder("health")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.75D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .researchTemplate(ResearchTemplate.builder()
                                        .star(0, 13, 5).star(1, 6, 8).star(2, 10, 12)
                                        .star(3, 4, 13).star(4, 18, 13).star(5, 8, 16)
                                        .star(6, 14, 16).star(7, 5, 20).star(8, 17, 20)
                                        .star(9, 11, 24).star(10, 8, 28).star(11, 14, 28)
                                        .link(0, 2).link(0, 4).link(1, 2).link(1, 3).link(3, 5).link(4, 6).link(5, 6).link(3, 7)
                                        .link(4, 8).link(7, 9).link(8, 9).link(9, 10).link(9, 11).link(10, 11)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("penitence")
                                .requiredLevel(5)
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.3D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .researchTemplate(ResearchTemplate.builder()
                                        .star(0, 7, 12).star(1, 15, 12).star(2, 6, 19)
                                        .star(3, 16, 19).star(4, 9, 26).star(5, 13, 26)
                                        .link(0, 1).link(0, 2).link(1, 3).link(2, 4).link(3, 5).link(4, 5)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("ascension")
                                .requiredLevel(10)
                                .requiredPoints(3)
                                .maxLevel(5)
                                .stat(StatTemplate.builder("max_duration")
                                        .initialValue(7.5D, 15D)
                                        .upgradeModifier(ScalingModelRegistry.EXPONENTIAL.get(), 0.3195D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .researchTemplate(ResearchTemplate.builder()
                                        .star(0, 11, 27).star(1, 3, 19).star(2, 3, 4)
                                        .star(3, 11, 17).star(4, 6, 13).star(5, 11, 13)
                                        .star(6, 16, 13).star(7, 19, 19).star(8, 19, 4)
                                        .link(0, 1).link(0, 3).link(0, 7).link(1, 2).link(2, 8).link(7, 8).link(3, 4).link(3, 5).link(3, 6)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(20)
                        .step(100)
                        .sources(LevelingSourcesTemplate.builder()
                                .source(LevelingSourceTemplate.abilityBuilder("faith")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .source(LevelingSourceTemplate.abilityBuilder("penitence")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .source(LevelingSourceTemplate.abilityBuilder("ascension")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip((player, stack) -> getMode(stack) == Mode.HOLINESS
                                ? TooltipData.builder()
                                .borderTop(0xFFcb4a0c)
                                .borderBottom(0xFF9c3309)
                                .textured(true)
                                .icon("holy_locket_holiness")
                                .build()
                                : TooltipData.builder()
                                .borderTop(0xFF484c51)
                                .borderBottom(0xFF484c51)
                                .textured(true)
                                .icon("holy_locket_wickedness")
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.DESERT)
                        .build())
                .build();
    }

    public Mode getMode(ItemStack stack) {
        return Mode.byIndex(stack.getOrDefault(MODE, Mode.HOLINESS.getIndex()));
    }

    public void setMode(ItemStack stack, Mode mode) {
        stack.set(MODE, mode.getIndex());
    }

    public void cycleMode(ItemStack stack, int steps) {
        setMode(stack, getMode(stack).cycle(steps));
    }

    @Override
    public void castActiveAbility(Player player, ItemStack stack, String ability, CastType type, CastStage stage) {
        if (ability.equals("faith") && stage == CastStage.END)
            cycleMode(stack, 1);
    }

    @Getter
    @AllArgsConstructor
    public enum Mode {
        HOLINESS(1),
        WICKEDNESS(2);

        private final int index;

        public static Mode byIndex(int index) {
            for (var mode : Mode.values())
                if (mode.getIndex() == index)
                    return mode;

            throw new IllegalArgumentException();
        }

        public Mode cycle(int steps) {
            var modes = Mode.values();
            int index = (this.ordinal() + steps) % modes.length;

            if (index < 0)
                index += modes.length;

            return modes[index];
        }
    }

    @EventBusSubscriber
    public static class HolyLocketEvents {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.HOLY_LOCKET.get())) {
                if (!(stack.getItem() instanceof IRelicItem relic) || !relic.canPlayerUseAbility(player, stack, "ascension"))
                    continue;

                var effect = player.getEffect(RelicsMobEffects.IMMORTALITY);
                var duration = effect == null ? 0 : effect.getDuration();
                var maxDuration = (int) (relic.getStatValue(player, stack, "ascension", "max_duration") * 20);

                if (duration >= maxDuration)
                    continue;

                player.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, (int) Math.min((relic.getStatValue(player, stack, "ascension", "duration") * 20) + duration, maxDuration)));

                relic.spreadRelicExperience(player, stack, 1);
            }
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var amount = event.getAmount();

            if (amount <= 0.5F)
                return;

            var maxDistance = 32;

            var entity = event.getEntity();
            var level = entity.getCommandSenderWorld();
            var random = level.getRandom();

            var targets = 0;

            for (var player : EntityUtils.gatherPotentialTargets(entity, Player.class, maxDistance).toList()) {
                if (player.getStringUUID().equals(entity.getStringUUID()))
                    continue;

                targets++;

                for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.HOLY_LOCKET.get())) {
                    if (!(stack.getItem() instanceof HolyLocketItem relic) || relic.getMode(stack) != Mode.HOLINESS || !relic.canPlayerUseAbility(player, stack, "faith")
                            || entity.position().distanceTo(player.position()) > relic.getStatValue(entity, stack, "faith", "radius"))
                        continue;

                    var heal = (float) (amount * relic.getStatValue(entity, stack, "faith", "health"));

                    var essence = new LifeEssenceEntity(RelicsEntities.LIFE_ESSENCE.get(), level);

                    essence.setHeal(heal);
                    essence.setOwner(player);
                    essence.setTarget(player);
                    essence.setPos(entity.getEyePosition());
                    essence.setDeltaMovement(MathUtils.randomFloat(random), random.nextFloat(), MathUtils.randomFloat(random));

                    level.addFreshEntity(essence);

                    event.setAmount(amount - heal);

                    relic.spreadRelicExperience(player, stack, 1);
                }

                if (targets >= MAX_TARGETS)
                    break;
            }

            if (entity instanceof Player player && player.getHealth() < player.getMaxHealth()) {
                for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.HOLY_LOCKET.get())) {
                    if (!(stack.getItem() instanceof HolyLocketItem relic) || relic.getMode(stack) != Mode.WICKEDNESS || !relic.canPlayerUseAbility(player, stack, "faith"))
                        continue;

                    targets = 0;

                    for (var target : EntityUtils.gatherPotentialTargets(player, LivingEntity.class, relic.getStatValue(entity, stack, "faith", "radius")).toList()) {
                        if (player.getStringUUID().equals(target.getStringUUID()))
                            continue;

                        targets++;

                        var essence = new DeathEssenceEntity(RelicsEntities.DEATH_ESSENCE.get(), level);

                        essence.setOwner(player);
                        essence.setTarget(target);
                        essence.setPos(player.getEyePosition());
                        essence.setDamage((float) (event.getAmount() * relic.getStatValue(entity, stack, "faith", "damage")));
                        essence.setDeltaMovement(MathUtils.randomFloat(random), random.nextFloat(), MathUtils.randomFloat(random));

                        level.addFreshEntity(essence);

                        relic.spreadRelicExperience(player, stack, 1);

                        if (targets >= MAX_TARGETS)
                            break;
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingIncomingDamageEvent event) {
            var entity = event.getEntity();

            if (!entity.isInvertedHealAndHarm() || !(event.getSource().getEntity() instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.HOLY_LOCKET.get())) {
                if (!(stack.getItem() instanceof HolyLocketItem relic) || !relic.canPlayerUseAbility(player, stack, "penitence"))
                    continue;

                var amount = event.getAmount();

                if (amount >= 1F && !entity.isOnFire())
                    relic.spreadRelicExperience(player, stack, 1);

                event.setAmount((float) (amount + (amount * relic.getStatValue(entity, stack, "penitence", "amount"))));

                entity.igniteForSeconds(10F);
            }
        }
    }
}