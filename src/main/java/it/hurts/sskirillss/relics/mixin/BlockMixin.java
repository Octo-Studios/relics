package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.entities.ShockwaveBlockEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSetEntityMotion;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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

        var stack = EntityUtils.findEquippedCurio(livingEntity, RelicsItems.SPRINGY_BOOT.get());

        if (!(stack.getItem() instanceof SpringyBootItem relic))
            return;

        if (relic.isLeaped(stack)) {
            var motion = livingEntity.getKnownMovement();
            var speed = motion.y();

            motion = motion.multiply(1D, -1D, 1D);

            if (speed > -0.75D) {
                relic.setLeaped(stack, false);
                relic.setLeaps(stack, 0);
            } else {
                var random = level.getRandom();

                relic.addLeaps(stack, 1);

                speed = Math.abs(speed);

                if (!livingEntity.isShiftKeyDown()) {
                    if (!level.isClientSide())
                        NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSetEntityMotion(livingEntity.getId(), motion.toVector3f()), livingEntity);
                }

                level.playSound(livingEntity, livingEntity.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.clamp(0.5F + speed * 0.5F, 0.5F, 2F), (float) Math.max(0.1F, 2F - speed * 0.75F));

                for (float i = 0; i < speed * 5F; i += 0.1F) {
                    var angle = random.nextFloat() * Math.PI * 2;
                    var radius = Math.sqrt(random.nextFloat()) * speed * 0.15F;

                    var dx = Math.cos(angle) * radius;
                    var dz = Math.sin(angle) * radius;

                    level.addParticle(ParticleTypes.CLOUD, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), dx, random.nextFloat() * 0.15F, dz);
                }

                if (!livingEntity.isShiftKeyDown()) {
                    for (int i = 0; i < 50 * speed; i += 1) {
                        var particleMotion = motion.normalize().scale(random.nextFloat());

                        level.addParticle(ParticleTypes.CLOUD, entity.getX() + MathUtils.randomFloat(random) * 0.5F, entity.getY(), entity.getZ() + MathUtils.randomFloat(random) * 0.5F, particleMotion.x(), particleMotion.y(), particleMotion.z());
                    }
                }

                if (relic.isAbilityRankModifierUnlocked(livingEntity, stack, "bounce", "shockwave") && livingEntity.isShiftKeyDown()) {
                    var verticalSpeed = Math.abs(livingEntity.getKnownMovement().y());

                    var center = livingEntity.blockPosition();
                    var radius = (int) Math.min(25, Math.round((1 + relic.getStatValue(livingEntity, stack, "bounce", "radius")) * verticalSpeed));

                    var poses = new ArrayList<BlockPos>();

                    for (var i = -radius; i <= radius; i++) {
                        var r1 = (int) Mth.sqrt(radius * radius - i * i);

                        for (var j = -r1; j <= r1; j++)
                            poses.add(center.offset(i, 0, j));
                    }

                    for (var step = 0; step <= radius; step++) {
                        int finalStep = step;

                        Scheduler.schedule(finalStep, () -> {
                            var height = 0.15F;

                            var localRandom = new Random();

                            poses.stream().filter(p -> {
                                var dx = p.getX() - center.getX();
                                var dz = p.getZ() - center.getZ();
                                var dist = Math.hypot(dx, dz);

                                return dist >= finalStep && dist < finalStep + 1;
                            }).forEach(entryPos -> {
                                var shockwave = new ShockwaveBlockEntity(RelicsEntities.SHOCKWAVE_BLOCK.get(), level);

                                shockwave.setDamage((float) relic.getStatValue(livingEntity, stack, "bounce", "damage"));
                                shockwave.setStun((int) relic.getStatValue(livingEntity, stack, "bounce", "stun") * 20);
                                shockwave.setPos(entryPos.getX() + 0.5F, entryPos.getY(), entryPos.getZ() + 0.5F);
                                shockwave.setBlockState(level.getBlockState(entryPos.below()));
                                shockwave.setDeltaMovement(0, height, 0);
                                shockwave.setCenter(entity.blockPosition());
                                shockwave.setOwner(livingEntity);

                                level.addFreshEntity(shockwave);

                                var angle = localRandom.nextFloat() * Math.PI * 2;

                                var rad = (finalStep + 0.5F + (localRandom.nextFloat() - 0.5F) * 0.3F);

                                var px = center.getX() + Math.cos(angle) * rad + 0.5F;
                                var py = entryPos.getY() + 0.5F + localRandom.nextFloat() * 0.2F;
                                var pz = center.getZ() + Math.sin(angle) * rad + 0.5F;

                                var vx = Math.cos(angle) * 0.1F;
                                var vy = 0.025F + localRandom.nextFloat() * 0.05F;
                                var vz = Math.sin(angle) * 0.1F;

                                level.addParticle(ParticleTypes.CLOUD, px, py, pz, vx, vy, vz);
                            });
                        });
                    }
                }
            }

            livingEntity.fallDistance = 0F;

            livingEntity.causeFallDamage(fallDistance, 0F, level.damageSources().fall());

            ci.cancel();
        }
    }
}