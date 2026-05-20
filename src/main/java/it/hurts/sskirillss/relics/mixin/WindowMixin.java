package it.hurts.sskirillss.relics.mixin;

import com.mojang.blaze3d.platform.Window;
import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.init.RelicsConfigs;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
    public void getGuiScale(CallbackInfoReturnable<Double> cir) {
        Minecraft MC = Minecraft.getInstance();
        Window window = (Window) (Object) (this);

        if (!RelicsConfigs.RELICS_CONFIG.isEnabledAutoScale() || !(MC.screen instanceof IAutoScaledScreen screen))
            return;

        cir.setReturnValue((double) this.relics$getAutoScale(window, screen, MC.isEnforceUnicode()));
    }

    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
    public void getGuiScaledHeight(CallbackInfoReturnable<Integer> cir) {
        Minecraft MC = Minecraft.getInstance();
        Window window = (Window) (Object) (this);

        if (!RelicsConfigs.RELICS_CONFIG.isEnabledAutoScale() || !(MC.screen instanceof IAutoScaledScreen screen))
            return;

        double scale = this.relics$getAutoScale(window, screen, MC.isEnforceUnicode());
        int height = (int) (window.getHeight() / scale);

        cir.setReturnValue((window.getHeight() / scale > height ? height + 1 : height));
    }

    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    public void getGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        Minecraft MC = Minecraft.getInstance();
        Window window = (Window) (Object) (this);

        if (!RelicsConfigs.RELICS_CONFIG.isEnabledAutoScale() || !(MC.screen instanceof IAutoScaledScreen screen))
            return;

        double scale = this.relics$getAutoScale(window, screen, MC.isEnforceUnicode());
        int width = (int) (window.getWidth() / scale);

        cir.setReturnValue((window.getWidth() / scale > width ? width + 1 : width));
    }

    @Unique
    private int relics$getAutoScale(Window window, IAutoScaledScreen screen, boolean enforceUnicode) {
        var scale = window.calculateScale(screen.getAutoScale(), enforceUnicode);

        if (scale > 1 && scale % 2 != 0)
            scale--;

        return scale;
    }
}
