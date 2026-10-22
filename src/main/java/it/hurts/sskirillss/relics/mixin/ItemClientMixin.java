package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemClientMixin {
	@Inject(method = "appendHoverText", at = @At("HEAD"))
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
		Item item = stack.getItem();
		
		if (!(item instanceof IRelicItem))
			return;
		
		tooltip.add(Component.literal(" "));
		
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu>)
			tooltip.add(Component.translatable("relics.description.researching.info", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" "));
	}
}
