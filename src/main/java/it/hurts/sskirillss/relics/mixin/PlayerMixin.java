package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
import it.hurts.sskirillss.relics.items.relics.RiderFluteItem;
import it.hurts.sskirillss.relics.misc.mixin.FluidWalkGraceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", shift = At.Shift.BEFORE), cancellable = true)
    private void relics$interceptRiderFluteCapture(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(entityToInteractOn instanceof LivingEntity livingEntity))
            return;

        var player = (Player) (Object) this;
        var stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof RiderFluteItem item))
            return;

        var result = item.interactLivingEntity(stack, player, livingEntity, hand);

        if (result.consumesAction())
            cir.setReturnValue(result);
    }

    @Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
    private void relics$skipBackOffOnFluid(Vec3 vec, MoverType mover, CallbackInfoReturnable<Vec3> cir) {
        var player = (Player) (Object) this;

        if (((FluidWalkGraceAccessor) (Object) this).relics$hasFluidWalkGrace()) {
            cir.setReturnValue(vec);

            return;
        }

        if (!player.isShiftKeyDown()
                || player.getAbilities().flying
                || vec.y > 0.0D
                || (mover != MoverType.SELF && mover != MoverType.PLAYER)
                || vec.horizontalDistanceSqr() < 1.0E-7D) {
            return;
        }

        if (relics$canStepOnFluidSurface(player, vec))
            cir.setReturnValue(vec);
    }

    @Unique
    private boolean relics$canStepOnFluidSurface(Player player, Vec3 vec) {
        var nextBox = player.getBoundingBox().move(vec.x, 0.0D, vec.z);
        var level = player.level();

        int minX = Mth.floor(nextBox.minX + 1.0E-6D);
        int maxX = Mth.floor(nextBox.maxX - 1.0E-6D);
        int minZ = Mth.floor(nextBox.minZ + 1.0E-6D);
        int maxZ = Mth.floor(nextBox.maxZ - 1.0E-6D);

        int y = Mth.floor(nextBox.minY - 0.2D);

        var pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, y, z);

                var fluid = level.getFluidState(pos);

                if (fluid.isEmpty())
                    continue;

                var surfaceY = pos.getY() + fluid.getOwnHeight();
                var feetY = nextBox.minY;

                if (feetY < surfaceY - 1.25D || feetY > surfaceY + 0.5D)
                    continue;

                var event = new FluidCollisionEvent(player, fluid);

                NeoForge.EVENT_BUS.post(event);

                if (event.isCanceled())
                    return true;
            }
        }

        return false;
    }
}