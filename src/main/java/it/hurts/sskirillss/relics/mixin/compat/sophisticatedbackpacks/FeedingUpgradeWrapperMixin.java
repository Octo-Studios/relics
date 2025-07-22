package it.hurts.sskirillss.relics.mixin.compat.sophisticatedbackpacks;

import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FeedingUpgradeWrapper.class)
public class FeedingUpgradeWrapperMixin {
//    @Inject(method = "tick", at = @At("HEAD"))
//    public void tick(Entity entity, Level level, BlockPos pos, CallbackInfo ci) {
//        if (!(entity instanceof Player))
//            return;
//
//        InventoryHelper.iterate(((UpgradeWrapperBaseAccessor) this).getStorageWrapper().getInventoryForUpgradeProcessing(), (slot, stack) -> {
//            if (stack.getItem() instanceof InfiniteHamItem relic)
//                relic.inventoryTick(stack, level, entity, slot, false);
//
//            return true;
//        }, () -> false, (result) -> false);
//    }
}