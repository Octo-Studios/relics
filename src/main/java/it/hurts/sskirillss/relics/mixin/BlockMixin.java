package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.client.particles.GhostlyFogParticle;
import it.hurts.sskirillss.relics.entities.SpringyBootShockwaveBlockEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.network.packets.item.springy_boot.S2CBounceFromSurface;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    public void onEntityFall(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity livingEntity))
            return;

        var mayContinue = false;

        for (var stack : EntityUtils.findEquippedCurios(livingEntity, RelicsItems.SPRINGY_BOOT.get())) {
            if (!(stack.getItem() instanceof SpringyBootItem relic))
                continue;

            if (relic.isLeaped(stack)) {
                if (!livingEntity.level().isClientSide()) {
                    if (livingEntity.getKnownMovement().y() > -0.75D) {
                        relic.setLeaped(stack, false);
                        relic.setLeaps(stack, 0);
                    } else {
                        if (livingEntity.isShiftKeyDown()) {
                            relic.setLeaped(stack, false);
                            relic.setLeaps(stack, 0);
                        }
                    }
                }

                mayContinue = true;
            }
        }

        if (mayContinue) {
            livingEntity.causeFallDamage(fallDistance, 0F, level.damageSources().fall());

            ci.cancel();
        }
    }

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    public void onEntityFall(BlockGetter getter, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity livingEntity))
            return;

        var level = livingEntity.level();

        var waveIndex = 0;

        for (var stack : EntityUtils.findEquippedCurios(livingEntity, RelicsItems.SPRINGY_BOOT.get())) {
            if (!(stack.getItem() instanceof SpringyBootItem relic))
                continue;

            if (relic.isLeaped(stack)) {
                if (relic.getBounceCooldown(stack) <= 0) {
                    if (!livingEntity.level().isClientSide()) {
                        var motion = livingEntity.getKnownMovement();
                        var speed = motion.y();

                        if (speed <= -0.75D) {
                            relic.addBounceCooldown(stack, 5);
                            relic.addLeaps(stack, 1);

                            speed = Math.abs(speed);

                            if (!livingEntity.isShiftKeyDown()) {
                                if (!level.isClientSide()) {
                                    relic.getRelicData(livingEntity, stack).getLevelingData().addExperience("bounce", "bounce", 1);

                                    relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("secondary_bounces").addValue(1);
                                }

                                level.playSound(null, livingEntity.blockPosition(), RelicsSounds.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.clamp(0.5F + speed * 0.5F, 0.5F, 2F), (float) Math.max(0.1F, 2F - speed * 0.75F));

                                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CBounceFromSurface(livingEntity.getId(), motion.multiply(1F, -1F, 1F).toVector3f()), livingEntity);

                                SpringyBootItem.spawnBounceFog(level, livingEntity, Math.max(1.1D, speed * 0.85D), 12 + Mth.ceil(speed * 2.5D));
                            } else {
                                if (relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getRankModifierData("shockwave").isEnabled()) {
                                    var delayTicks = waveIndex * 20;

                                    var center = livingEntity.blockPosition();
                                    var verticalSpeed = Math.abs(livingEntity.getKnownMovement().y());
                                    var radius = (int) Math.min(25, Math.round((1 + relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatData("radius").getValue()) * verticalSpeed));
                                    var damage = (float) relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatData("damage").getValue();
                                    var stun = (int) relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatData("stun").getValue() * 20;
                                    var serverLevel = (ServerLevel) level;

                                    relic.getRelicData(livingEntity, stack).getLevelingData().addExperience("bounce", "create_shockwave", radius);

                                    ServerScheduler.schedule(delayTicks, () -> {
                                        relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("shockwaves_amount").addValue(1);

                                        var poses = new ArrayList<BlockPos>();

                                        for (var i = -radius; i <= radius; i++) {
                                            var r1 = (int) Mth.sqrt(radius * radius - i * i);

                                            for (var j = -r1; j <= r1; j++)
                                                poses.add(center.offset(i, 0, j));
                                        }

                                        for (var step = 0; step <= radius; step++) {
                                            var finalStep = step;

                                            ServerScheduler.schedule(finalStep, () -> {
                                                var height = 0.25F;

                                                poses.stream().filter(pos -> {
                                                    var dx = pos.getX() - center.getX();
                                                    var dz = pos.getZ() - center.getZ();

                                                    var dist = Math.hypot(dx, dz);

                                                    return dist >= finalStep && dist < finalStep + 1;
                                                }).forEach(entryPos -> {
                                                    var centerY = center.getY();
                                                    var maxOffset = 8;

                                                    var groundY = WorldUtils.findSurfaceY(level, entryPos.getX(), entryPos.getZ(), centerY, maxOffset);

                                                    var minAllowedY = Math.max(level.getMinBuildHeight(), centerY - maxOffset);
                                                    var maxAllowedY = Math.min(level.getMaxBuildHeight(), centerY + maxOffset);

                                                    if (groundY < minAllowedY || groundY > maxAllowedY)
                                                        return;

                                                    var surfacePos = new BlockPos(entryPos.getX(), groundY, entryPos.getZ());
                                                    var shockwave = new SpringyBootShockwaveBlockEntity(RelicsEntities.SHOCKWAVE_BLOCK.get(), level);

                                                    shockwave.setDamage(damage);
                                                    shockwave.setStun(stun);
                                                    shockwave.setPos(surfacePos.getX() + 0.5F, surfacePos.getY(), surfacePos.getZ() + 0.5F);
                                                    shockwave.setBlockState(level.getBlockState(surfacePos));
                                                    shockwave.setDeltaMovement(0, height, 0);
                                                    shockwave.setOwner(livingEntity);
                                                    shockwave.setCenter(surfacePos);
                                                    shockwave.setKnockback(1F);
                                                    shockwave.setStack(stack);

                                                    level.addFreshEntity(shockwave);

                                                    var px = surfacePos.getX() + 0.15D + level.random.nextDouble() * 0.7D;
                                                    var pz = surfacePos.getZ() + 0.15D + level.random.nextDouble() * 0.7D;

                                                    var direction = new Vec3(px - (center.getX() + 0.5D), 0D, pz - (center.getZ() + 0.5D));

                                                    if (direction.lengthSqr() < 0.001D)
                                                        direction = new Vec3(level.random.nextDouble() - 0.5D, 0D, level.random.nextDouble() - 0.5D);

                                                    direction = direction.normalize();

                                                    var particleSpeed = 0.008D + level.random.nextDouble() * 0.012D;
                                                    var lifetime = 10 + level.random.nextInt(25);
                                                    var fogPosition = new Vector3f((float) px, surfacePos.getY() + 0.04F, (float) pz);
                                                    var fogMotion = new Vector3f((float) (direction.x * particleSpeed), height / 2F, (float) (direction.z * particleSpeed));

                                                    NetworkHandler.sendToClientsTrackingChunk(new S2CSpawnParticle(new GhostlyFogParticle.Options(lifetime), fogPosition, fogMotion), serverLevel, new ChunkPos(surfacePos));
                                                });
                                            });
                                        }
                                    });

                                    waveIndex++;
                                }
                            }
                        }
                    }
                }

                ci.cancel();
            }
        }
    }
}
