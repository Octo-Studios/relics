package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    public void onEntityFall(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity livingEntity))
            return;

        var stack = EntityUtils.findEquippedCurio(livingEntity, RelicsItems.SPRINGY_BOOT.get());

        if (!(stack.getItem() instanceof SpringyBootItem relic))
            return;

        var isOnShift = entity.isShiftKeyDown();
        var leaped = relic.isLeaped(stack);

        if (((!leaped || !isOnShift) && livingEntity.getKnownMovement().y() > -0.75D) || isOnShift)
            relic.setLeaped(stack, false);

        livingEntity.causeFallDamage(fallDistance, 0F, level.damageSources().fall());

        ci.cancel();
    }

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    public void onEntityFall(BlockGetter getter, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity livingEntity))
            return;

        var stack = EntityUtils.findEquippedCurio(livingEntity, RelicsItems.SPRINGY_BOOT.get());

        if (!(stack.getItem() instanceof SpringyBootItem relic) || !relic.isLeaped(stack))
            return;

        if (entity.isShiftKeyDown()) {
            ci.cancel();

            return;
        }

        var motion = livingEntity.getKnownMovement();
        var speed = motion.multiply(0F, 1F, 0F).y();

        if (speed > -0.75D)
            return;

        var level = livingEntity.getCommandSenderWorld();
        var random = level.getRandom();

        speed = Math.abs(speed);

        livingEntity.setDeltaMovement(motion.multiply(1D, -1D, 1D));

        level.playSound(livingEntity, livingEntity.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.min(2F, 0.25F + speed * 0.5F), (float) Math.max(0.1F, 2F - speed * 0.75F));

        for (float i = 0; i < speed * 3F; i += 0.1F)
            level.addParticle(ParticleTypes.CLOUD, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), MathUtils.randomFloat(random) * speed * 0.15F, 0F, MathUtils.randomFloat(random) * speed * 0.15F);

        ci.cancel();
    }
}