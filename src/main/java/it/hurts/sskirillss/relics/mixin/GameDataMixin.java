package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RegistryRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(GameData.class)
public class GameDataMixin {
    @Inject(method = "getRegistrationOrder", at = @At("RETURN"), cancellable = true)
    private static void onGetRegistrationOrder(CallbackInfoReturnable<Set<ResourceLocation>> cir) {
        Set<ResourceLocation> order = new LinkedHashSet<>();

        order.add(RegistryRegistry.SCALING_MODEL_REGISTRY_KEY.location());
        order.add(RegistryRegistry.RELIC_CONTAINER_REGISTRY_KEY.location());
        order.add(RegistryRegistry.BADGE_REGISTRY_KEY.location());

        order.addAll(cir.getReturnValue());

        cir.setReturnValue(order);
    }
}