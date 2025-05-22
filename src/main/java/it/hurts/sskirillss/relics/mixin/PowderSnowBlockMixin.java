package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    public void boilSnow(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!EntityUtils.findEquippedCurio(entity, RelicsItems.WOOL_MITTEN.get()).isEmpty())
            ci.cancel();
    }
}