package it.hurts.sskirillss.relics.mixin.compat.lithium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.GlitchyMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkAwareBlockCollisionSweeper.class, remap = false)
public class ChunkAwareBlockCollisionSweeperMixin {
    @Final
    @Shadow(remap = false)
    private CollisionContext context;

    @ModifyExpressionValue(method = "nextSection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private boolean relics$keepEmptySectionsForGlitchAirWalk(boolean original) {
        if (!original)
            return false;

        if (!(this.context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return true;

        var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.GLITCHY_MANTLE.get());

        if (stack.isEmpty())
            return true;

        var relic = (GlitchyMantleItem) stack.getItem();

        return !relic.canCollideWithAirLike(entity, stack);
    }
}
