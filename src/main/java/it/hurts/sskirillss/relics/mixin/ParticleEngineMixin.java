package it.hurts.sskirillss.relics.mixin;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.client.particles.BasicColoredParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyRenderOrder(CallbackInfo ci) {
        var order = new ArrayList<>(RENDER_ORDER);

        order.add(4, BasicColoredParticle.RENDERER_NO_DEPTH);

        RENDER_ORDER = ImmutableList.copyOf(order);
    }
}