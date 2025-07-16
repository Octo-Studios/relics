package it.hurts.sskirillss.relics.items.relics.belt;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.kinetic_belt.C2SSetActive;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class KineticBeltItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("slots")
                                .requiredPoints(2)
                                .initialMaxLevel(5)
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gliding")
                                .rankModifier(1, "momentum")
                                .rankModifier(3, "strike")
                                .rankModifier(5, "resistance")
                                .modes("enabled", "disabled")
                                .stat(StatTemplate.builder("efficiency")
                                        .initialValue(0.25D, 0.35D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.05D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("resistance")
                                        .initialValue(0.05D, 0.15D)
                                        .thresholdValue(0D, 0.75D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xFFbaa8a8)
                                .borderBottom(0xFFbaa8a8)
                                .textured(true)
                                .build())
                        .build())
                .build();
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.set(DataComponentRegistry.KINETIC_BELT_ACTIVE, active);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.KINETIC_BELT_ACTIVE, false);
    }

    public void setLanded(ItemStack stack, boolean landed) {
        stack.set(DataComponentRegistry.KINETIC_BELT_LANDED, landed);
    }

    public boolean isLanded(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.KINETIC_BELT_LANDED, false);
    }

    @Override
    public RelicSlotModifier getSlotModifiers(LivingEntity entity, ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) Math.round(getStatValue(entity, stack, "slots", "amount")))
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        if (!this.canPlayerUseAbility(entity, stack, "gliding") || this.getAbilityMode(entity, stack, "gliding").equals("disabled"))
            return;

        var level = entity.level();
        var random = level.getRandom();

        var isActive = this.isActive(stack);
        var isLanded = this.isLanded(stack);

        var onGround = entity.onGround();

        var hasAttribute = EntityUtils.hasAttribute(entity, stack, Attributes.GRAVITY);

        if (level.isClientSide()) {
            if (entity instanceof LocalPlayer player) {
                if (player.input.jumping && !player.isFallFlying() && !player.getAbilities().flying && !player.isSwimming()) {
                    NetworkHandler.sendToServer(new C2SSetActive(slotContext.identifier(), slotContext.index(), true));
                } else if (isActive) {
                    NetworkHandler.sendToServer(new C2SSetActive(slotContext.identifier(), slotContext.index(), false));
                }
            }
        }

        if (isLanded) {
            if (this.isAbilityRankModifierUnlocked(entity, stack, "gliding", "momentum") && !onGround && isActive)
                this.setLanded(stack, false);
        } else {
            if (onGround && !isActive)
                this.setLanded(stack, true);
        }

        if (isActive) {
            if (!hasAttribute)
                EntityUtils.applyAttribute(entity, stack, Attributes.GRAVITY, (float) -Math.min(this.getStatValue(entity, stack, "gliding", "efficiency"), 0.9F), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            var motion = entity.getDeltaMovement();
            var verticalMotion = (float) -motion.y;

            var minVy = 0.25F;
            var maxVy = 7.5F;
            var baseSlowFactor = Math.clamp((maxVy - verticalMotion) / (maxVy - minVy), 0F, 1F);

            var slowFactor = (float) Math.sqrt(baseSlowFactor);

            var maxReductionPerTick = 1F;

            var reduction = maxReductionPerTick * slowFactor;

            entity.fallDistance = Math.max(0F, entity.fallDistance - reduction);

            var prevPosition = new Vec3(entity.xOld, entity.yOld, entity.zOld);
            var position = entity.getPosition(0.25F);
            var movementDelta = position.subtract(prevPosition);
            var distance = movementDelta.length();
            var spawnStep = 0.025D;
            int spawnCount = (int) (distance / spawnStep) + 1;

            var yawRadians = Math.toRadians(entity.yBodyRot);
            var forward = new Vec3(-Math.sin(yawRadians), 0, Math.cos(yawRadians)).normalize();
            var right = forward.cross(new Vec3(0, 1, 0)).normalize();
            var left = right.scale(-1);

            var sideOffset = 0.4D;
            var backOffset = 0.2D;

            for (var i = 0; i <= spawnCount; i++) {
                var t = spawnCount == 0 ? 0 : (double) i / spawnCount;
                var basePosition = prevPosition.add(movementDelta.scale(t)).subtract(forward.scale(backOffset));
                var centerX = basePosition.x;
                var centerY = basePosition.y + entity.getBbHeight() / 2F - 0.15F;
                var centerZ = basePosition.z;

                for (var side : new Vec3[]{right, left}) {
                    var x = centerX + side.x * sideOffset + (random.nextDouble() * 2 - 1) * 0.02;
                    var y = centerY + (random.nextDouble() * 2 - 1) * 0.02;
                    var z = centerZ + side.z * sideOffset + (random.nextDouble() * 2 - 1) * 0.02;

                    var vx = (random.nextFloat() * 2 - 1) * 0.01F;
                    var vy = (random.nextFloat() * 2 - 1) * 0.01F;
                    var vz = (random.nextFloat() * 2 - 1) * 0.01F;

                    level.addParticle(ParticleUtils.constructSimpleSpark(this.isRelicFlawless(entity, stack) ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(random.nextInt(50), 75 + random.nextInt(100), 255), 0.1F + random.nextFloat() * 0.15F, 100 + random.nextInt(20), 0.995F), x, y, z, vx, vy, vz);
                }
            }
        } else if (hasAttribute)
            EntityUtils.removeAttribute(entity, stack, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get())) {
                var relic = (KineticBeltItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "gliding") || relic.getAbilityMode(entity, stack, "gliding").equals("disabled")
                        || !relic.isAbilityRankModifierUnlocked(entity, stack, "gliding", "momentum") || !relic.isActive(stack))
                    continue;

                event.setDistance(0);

                break;
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            var original = event.getNewDamage();

            if (event.getSource().getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity source) {
                for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.KINETIC_BELT.get())) {
                    var relic = (KineticBeltItem) stack.getItem();

                    if (!relic.canPlayerUseAbility(source, stack, "gliding") || relic.getAbilityMode(entity, stack, "gliding").equals("disabled")
                            || !relic.isAbilityRankModifierUnlocked(source, stack, "gliding", "strike") || !relic.isActive(stack))
                        continue;

                    event.setNewDamage((float) (original + (original * relic.getStatValue(entity, stack, "gliding", "damage"))));
                }
            }

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get())) {
                var relic = (KineticBeltItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "gliding") || relic.getAbilityMode(entity, stack, "gliding").equals("disabled")
                        || !relic.isAbilityRankModifierUnlocked(entity, stack, "gliding", "resistance") || !relic.isActive(stack))
                    continue;

                event.setNewDamage((float) (original - (original * relic.getStatValue(entity, stack, "gliding", "resistance"))));
            }
        }
    }
}