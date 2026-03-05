package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyVariable(method = "move", ordinal = 1, index = 3, name = "vec32", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 fluidCollision(Vec3 original) {
        if (!((Entity) (Object) this instanceof LivingEntity entity))
            return original;

        if (original.y > 0)
            return original;

        Level level = entity.getCommandSenderWorld();

        int[][] offsets = {
                {1, 0, 1}, {1, 0, 0}, {1, -1, 0}, {1, 0, -1},
                {0, 0, 1}, {0, 0, 0}, {0, -1, 0}, {0, 0, -1},
                {-1, 0, 1}, {-1, 0, 0}, {-1, -1, 0}, {-1, 0, -1}
        };

        double highestValue = original.y;
        FluidState highestFluid = null;

        for (int[] offset : offsets) {
            BlockPos sourcePos = entity.blockPosition();
            BlockPos pos = new BlockPos(sourcePos.getX() + offset[0], sourcePos.getY() + offset[1], sourcePos.getZ() + offset[2]);

            FluidState fluidState = level.getFluidState(pos);

            if (fluidState.isEmpty())
                continue;

            VoxelShape shape = Shapes.block().move(pos.getX(), pos.getY() + fluidState.getOwnHeight(), pos.getZ());

            if (Shapes.joinIsNotEmpty(shape, Shapes.create(entity.getBoundingBox().inflate(0.5)), BooleanOp.AND)) {
                double height = shape.max(Direction.Axis.Y) - entity.getY() - 1;

                if (highestValue < height) {
                    highestValue = height;
                    highestFluid = fluidState;
                }
            }
        }

        if (highestFluid == null)
            return original;

        FluidCollisionEvent event = new FluidCollisionEvent(entity, highestFluid);

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            entity.fallDistance = 0F;
            entity.setOnGround(true);

            return new Vec3(original.x, highestValue, original.z);
        }

        return original;
    }

//    @Inject(at = @At(value = "RETURN"), method = "isInWaterOrRain", cancellable = true)
//    public void setWet(CallbackInfoReturnable<Boolean> info) {
//        Entity entity = (Entity) (Object) this;
//
//        if (!(entity instanceof LivingEntity))
//            return;
//
//        if (!EntityUtils.findEquippedCurio(entity, RelicsItems.DROWNED_BELT.get()).isEmpty())
//            info.setReturnValue(true);
//    }

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
