package it.hurts.sskirillss.relics.mixin;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Random;

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

                            relic.addAbilityMetricValue(livingEntity, stack, "bounce", "secondary_bounces", 1);

                            speed = Math.abs(speed);

                            level.playSound(null, livingEntity.blockPosition(), RelicsSounds.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.clamp(0.5F + speed * 0.5F, 0.5F, 2F), (float) Math.max(0.1F, 2F - speed * 0.75F));

                            if (!livingEntity.isShiftKeyDown())
                                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CBounceFromSurface(livingEntity.getId(), motion.multiply(1F, -1F, 1F).toVector3f()), livingEntity);
                            else {
                                if (relic.isAbilityRankModifierUnlocked(livingEntity, stack, "bounce", "shockwave")) {
                                    var delayTicks = waveIndex * 20;

                                    var center0 = livingEntity.blockPosition();
                                    var verticalSpeed0 = Math.abs(livingEntity.getKnownMovement().y());
                                    var radius0 = (int) Math.min(25, Math.round((1 + relic.getStatValue(livingEntity, stack, "bounce", "radius")) * verticalSpeed0));
                                    var damage0 = (float) relic.getStatValue(livingEntity, stack, "bounce", "damage");
                                    var stun0 = (int) relic.getStatValue(livingEntity, stack, "bounce", "stun") * 20;
                                    var stack0 = stack.copy();

                                    ServerScheduler.schedule(delayTicks, () -> {
                                        relic.addAbilityMetricValue(livingEntity, stack0, "bounce", "shockwaves_amount", 1);

                                        var poses = new ArrayList<BlockPos>();

                                        for (var i = -radius0; i <= radius0; i++) {
                                            var r1 = (int) Mth.sqrt(radius0 * radius0 - i * i);

                                            for (var j = -r1; j <= r1; j++)
                                                poses.add(center0.offset(i, 0, j));
                                        }

                                        for (var step = 0; step <= radius0; step++) {
                                            var finalStep = step;

                                            ServerScheduler.schedule(finalStep, () -> {
                                                var height = 0.25F;
                                                var localRandom = new Random();

                                                poses.stream().filter(pos -> {
                                                    var dx = pos.getX() - center0.getX();
                                                    var dz = pos.getZ() - center0.getZ();

                                                    var dist = Math.hypot(dx, dz);

                                                    return dist >= finalStep && dist < finalStep + 1;
                                                }).forEach(entryPos -> {
                                                    var centerY = center0.getY();
                                                    var maxOffset = 8;

                                                    var groundY = WorldUtils.findSurfaceY(level, entryPos.getX(), entryPos.getZ(), centerY, maxOffset);

                                                    var minAllowedY = Math.max(level.getMinBuildHeight(), centerY - maxOffset);
                                                    var maxAllowedY = Math.min(level.getMaxBuildHeight(), centerY + maxOffset);

                                                    if (groundY < minAllowedY || groundY > maxAllowedY)
                                                        return;

                                                    var surfacePos = new BlockPos(entryPos.getX(), groundY, entryPos.getZ());
                                                    var shockwave = new SpringyBootShockwaveBlockEntity(RelicsEntities.SHOCKWAVE_BLOCK.get(), level);

                                                    shockwave.setDamage(damage0);
                                                    shockwave.setStun(stun0);
                                                    shockwave.setPos(surfacePos.getX() + 0.5F, surfacePos.getY(), surfacePos.getZ() + 0.5F);
                                                    shockwave.setBlockState(level.getBlockState(surfacePos));
                                                    shockwave.setDeltaMovement(0, height, 0);
                                                    shockwave.setOwner(livingEntity);
                                                    shockwave.setCenter(surfacePos);
                                                    shockwave.setKnockback(1F);
                                                    shockwave.setStack(stack0);

                                                    level.addFreshEntity(shockwave);

                                                    var angle = localRandom.nextFloat() * Math.PI * 2;
                                                    var rad = (finalStep + 0.5F + (localRandom.nextFloat() - 0.5F) * 0.3F);

                                                    var px = (float) (surfacePos.getX() + Math.cos(angle) * rad + 0.5F);
                                                    var py = surfacePos.getY() + 1.5F + localRandom.nextFloat() * 0.2F;
                                                    var pz = (float) (surfacePos.getZ() + Math.sin(angle) * rad + 0.5F);

                                                    var vx = (float) Math.cos(angle) * 0.2F;
                                                    var vy = 0.025F + localRandom.nextFloat() * 0.05F;
                                                    var vz = (float) Math.sin(angle) * 0.2F;

                                                    NetworkHandler.sendToClientsTrackingEntity(new S2CSpawnParticle(ParticleTypes.CLOUD, new Vector3f(px, py, pz), new Vector3f(vx, vy, vz)), shockwave);
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