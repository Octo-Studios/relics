package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.misc.mixin.FluidWalkGraceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements FluidWalkGraceAccessor {
    @Unique
    private int relics$fluidWalkGraceTicks;

    @Unique
    private static final int[][] FLUID_OFFSETS = {
            {1, 0, 1}, {1, 0, 0}, {1, -1, 0}, {1, 0, -1},
            {0, 0, 1}, {0, 0, 0}, {0, -1, 0}, {0, 0, -1},
            {-1, 0, 1}, {-1, 0, 0}, {-1, -1, 0}, {-1, 0, -1}
    };

    @ModifyVariable(method = "move", ordinal = 1, index = 3, name = "vec32", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 fluidCollision(Vec3 original) {
        if (!((Entity) (Object) this instanceof LivingEntity entity) || original.y > 0)
            return original;

        var level = entity.getCommandSenderWorld();
        var sourcePos = entity.blockPosition();
        var mutablePos = new BlockPos.MutableBlockPos();
        var entityShape = Shapes.create(entity.getBoundingBox().inflate(0.5));

        var highestY = original.y;

        FluidState highestFluid = null;

        for (var offset : FLUID_OFFSETS) {
            mutablePos.set(
                    sourcePos.getX() + offset[0],
                    sourcePos.getY() + offset[1],
                    sourcePos.getZ() + offset[2]
            );

            var fluidState = level.getFluidState(mutablePos);

            if (fluidState.isEmpty())
                continue;

            var shape = Shapes.block().move(
                    mutablePos.getX(),
                    mutablePos.getY() + fluidState.getOwnHeight(),
                    mutablePos.getZ()
            );

            if (!Shapes.joinIsNotEmpty(shape, entityShape, BooleanOp.AND))
                continue;

            var height = shape.max(Direction.Axis.Y) - entity.getY() - 1;

            if (height > highestY) {
                highestY = height;
                highestFluid = fluidState;
            }
        }

        if (highestFluid == null)
            return original;

        var event = new FluidCollisionEvent(entity, highestFluid);

        NeoForge.EVENT_BUS.post(event);

        if (!event.isCanceled())
            return original;

        entity.fallDistance = 0F;

        entity.setOnGround(true);

        relics$fluidWalkGraceTicks = 2;

        return new Vec3(original.x, highestY, original.z);
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
