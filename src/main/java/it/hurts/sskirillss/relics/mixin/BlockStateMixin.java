package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.utility.FluidCollisionEvent;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.GlitchyMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateMixin {
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getFluidCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;

        if (!(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return;

        var fluidState = state.getFluidState();

        if (fluidState.isEmpty())
            return;

        var event = new FluidCollisionEvent(entity, fluidState);

        NeoForge.EVENT_BUS.post(event);

        if (!event.isCanceled())
            return;

        var shape = Shapes.box(0D, 0D, 0D, 1D, fluidState.getOwnHeight(), 1D);

        cir.setReturnValue(shape);
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getLeavesCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;

        if (!(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return;

        if (!state.is(BlockTags.LEAVES))
            return;

        var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.LEAFY_MANTLE.get());

        if (stack.isEmpty())
            return;

        if (entity.isShiftKeyDown()) {
            cir.setReturnValue(Shapes.empty());

            return;
        }

        cir.setReturnValue(entityContext.isAbove(Shapes.block(), pos, true) ? Shapes.block() : Shapes.empty());
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getAirCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;

        if (!(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return;

        if (!state.isAir())
            return;

        var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.GLITCHY_MANTLE.get());

        if (stack.isEmpty())
            return;

        var relic = (GlitchyMantleItem) stack.getItem();

        if (relic.isForcedFall(stack))
            return;

        if (relic.canCollideWithAirLike(entity, stack))
            cir.setReturnValue(entityContext.isAbove(Shapes.block(), pos, true) ? Shapes.block() : Shapes.empty());
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getSolidCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;

        if (!(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return;

        if (state.isAir() || !state.getFluidState().isEmpty())
            return;

        if (state.getDestroySpeed(null, pos) < 0F)
            return;

        var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.GLITCHY_MANTLE.get());

        if (stack.isEmpty())
            return;

        var relic = (GlitchyMantleItem) stack.getItem();

        if (relic.isForcedFall(stack))
            return;

        if (relic.canPhaseThroughBlocks(entity, stack))
            cir.setReturnValue(entity.isShiftKeyDown() ? Shapes.empty() : entityContext.isAbove(Shapes.block(), pos, true) ? Shapes.block() : Shapes.empty());
    }
}
