package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.badges.base.AbilityBadge;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractBadgeWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AbilityBadgeWidget extends AbstractBadgeWidget {
    private final AbilityBadge badge;
    private final String ability;

    public AbilityBadgeWidget(int x, int y, IRelicScreenProvider provider, AbilityBadge badge, String ability) {
        super(x, y, provider, badge);

        this.badge = badge;
        this.ability = ability;
    }

    @Override
    public AbilityBadge getBadge() {
        return badge;
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = getProvider().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 100;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                getBadge().getTitle(stack, ability).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE),
                Component.literal(" ")
        );

        entries.addAll(getBadge().getDescription(stack, ability));

        List<MutableComponent> hint = getBadge().getHint(stack, ability);

        if (!hint.isEmpty()) {
            entries.add(Component.literal(" "));

            if (Screen.hasShiftDown())
                entries.addAll(hint.stream().map(entry -> entry.withStyle(ChatFormatting.ITALIC)).toList());
            else
                entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));
        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 400);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }
}