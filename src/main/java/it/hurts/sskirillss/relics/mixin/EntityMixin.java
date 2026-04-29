package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.utility.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.utility.FluidCollisionEvent;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.misc.mixin.FluidWalkGraceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public class EntityMixin implements FluidWalkGraceAccessor {
    @Unique
    private int relics$fluidWalkGraceTicks;

    @Redirect(method = "collectColliders", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;"))
    private static Iterable<VoxelShape> relics$getBlockCollisions(Level level, Entity entity, AABB box) {
        var collisions = new ArrayList<VoxelShape>();

        for (var shape : level.getBlockCollisions(entity, box))
            collisions.add(shape);

        if (!(entity instanceof LivingEntity livingEntity))
            return collisions;

        collisions.addAll(relics$getFluidCollisions(level, livingEntity, box));

        return collisions;
    }

    @Unique
    private static List<VoxelShape> relics$getFluidCollisions(Level level, LivingEntity entity, AABB box) {
        var result = new ArrayList<VoxelShape>();

        var minX = Mth.floor(box.minX);
        var maxX = Mth.floor(box.maxX - 1.0E-7D);
        var minY = Mth.floor(box.minY);
        var maxY = Mth.floor(box.maxY - 1.0E-7D);
        var minZ = Mth.floor(box.minZ);
        var maxZ = Mth.floor(box.maxZ - 1.0E-7D);

        var mutablePos = new BlockPos.MutableBlockPos();

        for (var x = minX; x <= maxX; x++) {
            for (var y = minY; y <= maxY; y++) {
                for (var z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);

                    var fluidState = level.getFluidState(mutablePos);

                    if (fluidState.isEmpty())
                        continue;

                    var event = new FluidCollisionEvent(entity, fluidState);

                    NeoForge.EVENT_BUS.post(event);

                    if (!event.isCanceled())
                        continue;

                    result.add(Shapes.box(0D, 0D, 0D, 1D, fluidState.getOwnHeight(), 1D).move(x, y, z));
                }
            }
        }

        return result;
    }

    @Inject(method = "baseTick()V", at = @At("TAIL"))
    private void relics$tickFluidWalkGrace(CallbackInfo ci) {
        if (relics$fluidWalkGraceTicks > 0)
            relics$fluidWalkGraceTicks--;
    }

    @Override
    public boolean relics$hasFluidWalkGrace() {
        return relics$fluidWalkGraceTicks > 0;
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    public void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        Entity entity = (Entity) (Object) this;

        EntityBlockSpeedFactorEvent event = new EntityBlockSpeedFactorEvent(entity, entity.level().getBlockState(entity.getOnPos()), cir.getReturnValue());

        NeoForge.EVENT_BUS.post(event);

        cir.setReturnValue(event.getSpeedFactor());
    }

    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void canUsePortal(boolean allowPassengers, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player))
            return;

        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ClotOfTimeItem)
            cir.setReturnValue(false);
    }
}
