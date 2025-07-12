package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.common.LivingSlippingEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.roller_skate.C2SCreateSpark;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;

public class RollerSkateItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("skating")
                                .rankModifier(1, "step_height")
                                .rankModifier(1, "resistance")
                                .rankModifier(1, "sparkling")
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.05D, 0.1D)
                                        .upgradeModifier(ScalingModelRegistry.LOGARITHMIC.get(), 0.2511D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("step_height")
                                        .initialValue(0.6D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("resistance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("ignite")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .build();
    }

    public int getDuration(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.ROLLER_SKATE_DURATION, 0);
    }

    public void setDuration(ItemStack stack, int duration) {
        stack.set(DataComponentRegistry.ROLLER_SKATE_DURATION, Math.clamp(duration, 0, this.getMaxDuration()));
    }

    public void addDuration(ItemStack stack, int progress) {
        this.setDuration(stack, this.getDuration(stack) + progress);
    }

    public int getMaxDuration() {
        return 5 * 20;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity entity))
            return;

        var level = entity.level();
        var random = level.getRandom();

        var duration = this.getDuration(stack);

        if (entity.isSprinting() && entity.onGround() && !entity.isInLiquid() && !entity.isFallFlying()) {
            if (duration < this.getMaxDuration())
                this.addDuration(stack, 1);
        } else if (duration > 0) {
            this.addDuration(stack, -1);
        }

        if (duration > 0) {
            EntityUtils.resetAttribute(entity, stack, Attributes.MOVEMENT_SPEED, (float) (this.getStatValue(entity, stack, "skating", "speed") / this.getMaxDuration() * this.getDuration(stack)), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            if (this.isAbilityRankModifierUnlocked(entity, stack, "skating", "step_height"))
                EntityUtils.resetAttribute(entity, stack, Attributes.STEP_HEIGHT, (float) this.getStatValue(entity, stack, "skating", "step_height"), AttributeModifier.Operation.ADD_VALUE);
        }

        if (this.isAbilityRankModifierUnlocked(entity, stack, "skating", "sparkling")) {
            var motion = entity.getDeltaMovement();

            var xMotion = motion.x;
            var zMotion = motion.z;

            var speed = Mth.sqrt((float) (xMotion * xMotion + zMotion * zMotion));

            if (entity instanceof Player player && level.isClientSide)
                player.displayClientMessage(Component.literal("S: " + speed), true);

            if (speed > 0.25F && entity.onGround()) {
                var yawRad = entity.getYRot() * (float) Math.PI / 180F;

                var inputX = entity.xxa;
                var inputZ = entity.zza;

                var inLen = Mth.sqrt(inputX * inputX + inputZ * inputZ);

                if (inLen > 0.01F) {
                    inputX /= inLen;
                    inputZ /= inLen;

                    var directionX = inputZ * -Mth.sin(yawRad) + inputX * Mth.cos(yawRad);
                    var directionZ = inputZ * Mth.cos(yawRad) + inputX * Mth.sin(yawRad);

                    var dot = directionX * (xMotion / speed) + directionZ * (zMotion / speed);

                    if (dot < 0.5F) {
                        var count = Mth.clamp((int) (speed * 6), 1, 20);

                        for (int i = 0; i < count; i++) {
                            var force = ((0.25F + random.nextFloat() * 0.25F) * speed) * 2F;

                            var offset = (random.nextFloat() - 0.5F) * 0.15F;

                            var deltaX = -directionX * force + (-directionZ) * offset;
                            var deltaY = 0.2F + random.nextFloat() * 0.2F * speed;
                            var deltaZ = -directionZ * force + directionX * offset;

                            NetworkHandler.sendToServer(new C2SCreateSpark(entity.position().toVector3f(), new Vector3f(deltaX, deltaY, deltaZ), entity.getStringUUID(),
                                    (float) (this.getStatValue(entity, stack, "skating", "damage") * speed),
                                    (float) (this.getStatValue(entity, stack, "skating", "ignite") * speed),
                                    this.isRelicFlawless(entity, stack)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (stack.getItem() == newStack.getItem())
            return;

        LivingEntity entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        EntityUtils.removeAttribute(entity, stack, Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE);
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingSlipping(LivingSlippingEvent event) {
            var entity = event.getEntity();

            if (entity.isInLiquid() || entity.isFallFlying() || !entity.onGround())
                return;

            var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.ROLLER_SKATES.get());

            if (stack.isEmpty())
                return;

            var relic = (RollerSkateItem) stack.getItem();

            var base = 0.6F;
            var max = 1.075F;
            var diff = max - base;
            var modifier = diff / relic.getMaxDuration() * relic.getDuration(stack);

            event.setFriction(base + modifier);
        }

        @SubscribeEvent
        public static void onSpeedFactor(EntityBlockSpeedFactorEvent event) {
            if (!(event.getEntity() instanceof LivingEntity entity) || entity.isInLiquid() || entity.isFallFlying() || !entity.onGround())
                return;

            var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.ROLLER_SKATES.get());

            if (stack.isEmpty())
                return;

            event.setSpeedFactor(1F);
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();
            var original = event.getOriginalDamage();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.ROLLER_SKATES.get())) {
                var relic = (RollerSkateItem) stack.getItem();

                var duration = relic.getDuration(stack);

                if (!relic.canPlayerUseAbility(entity, stack, "skating") || !relic.isAbilityRankModifierUnlocked(entity, stack, "skating", "resistance") || duration <= 0)
                    continue;

                event.setNewDamage((float) (original * (relic.getStatValue(entity, stack, "skating", "resistance") / relic.getMaxDuration() * duration)));
            }
        }
    }
}