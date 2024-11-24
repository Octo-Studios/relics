package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        if (!(entity instanceof Player player))
            return;

        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPRINGY_BOOT.get());

        if (!(stack.getItem() instanceof SpringyBootItem relic) || !relic.isAbilityTicking(stack, "bounce"))
            return;

        var motion = player.getKnownMovement();
        var speed = motion.multiply(0F, 1F, 0F).y();

        if (speed > -0.5D)
            return;

        player.causeFallDamage(fallDistance, 0F, level.damageSources().fall());

        ci.cancel();
    }

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    public void onEntityFall(BlockGetter getter, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Player player))
            return;

        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPRINGY_BOOT.get());

        if (!(stack.getItem() instanceof SpringyBootItem relic) || !relic.isAbilityTicking(stack, "bounce"))
            return;

        var motion = player.getKnownMovement();
        var speed = motion.multiply(0F, 1F, 0F).y();

        if (speed > -0.5D)
            return;

        var level = player.getCommandSenderWorld();

        if (!level.isClientSide())
            relic.spreadRelicExperience(player, stack, 1);

        speed = Math.abs(speed);

        var power = relic.getStatValue(stack, "bounce", "power");

        player.setDeltaMovement(motion.multiply(1D, -power, 1D));

        var random = level.getRandom();

        level.playSound(player, player.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.min(2F, 0.25F + speed * 0.5F), (float) Math.max(0.1F, 2F - speed * 0.75F));

        for (float i = 0; i < speed * 3F; i += 0.1F)
            level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), MathUtils.randomFloat(random) * speed * 0.15F, 0F, MathUtils.randomFloat(random) * speed * 0.15F);

        ci.cancel();
    }
}