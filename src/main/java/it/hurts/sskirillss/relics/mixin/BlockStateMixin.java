package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.LeafyMantleItem;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateMixin {
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;

        if (!state.is(BlockTags.LEAVES) || !(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof LivingEntity entity))
            return;

        var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.LEAFY_MANTLE.get());

        if (stack.isEmpty())
            return;

        var relic = (LeafyMantleItem) stack.getItem();

        if (!relic.canPlayerUseAbility(entity, stack, "camouflage"))
            return;

        if (entity.isShiftKeyDown()) {
            cir.setReturnValue(Shapes.empty());

            return;
        }

        cir.setReturnValue(entityContext.isAbove(Shapes.block(), pos, true) ? Shapes.block() : Shapes.empty());
    }
}