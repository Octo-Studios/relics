package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.misc.DismantleEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class DismantleRelicActionWidget extends AbstractRelicActionWidget {
    public DismantleRelicActionWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, "dismantle", screen);
    }

    @Override
    public boolean isLocked() {
        return getScreen().getDismantleEffect().isCommitted()
                || DescriptionUtils.getRemovableRelicSlot(minecraft.player, getScreen().getContainer(), getScreen().getSlot()) == null;
    }

    @Override
    public void onPress() {
        var stack = getScreen().getStack();

        if (!isLocked() && stack.getItem() instanceof IRelicItem relic)
            getScreen().getDismantleEffect().strike(stack, relic.getRelicData(minecraft.player, stack).getLevelingData().getRank());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return CommonInputs.selected(keyCode);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var stack = getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var reward = relic.getRelicData(minecraft.player, stack).getLevelingData().getDismantleRankPointReward();
        var newLine = Component.literal(" ");

        return List.of(
                Component.translatable("relics.description.relic.dismantle.title", getScreen().getDismantleEffect().hits(), DismantleEffect.REQUIRED_HITS)
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                newLine,
                Component.translatable("relics.description.general.cost.title").append(":").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                Component.literal("   ● ").append(Component.translatable("relics.description.relic.dismantle.cost.entry_1")
                        .withColor(isLocked() ? DescriptionUtils.NEGATIVE_COLOR(true) : DescriptionUtils.POSITIVE_COLOR(true))),
                newLine,
                Component.translatable("relics.description.general.reward.title").append(":").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                Component.literal("   ● ").append(Component.translatable("relics.description.relic.dismantle.reward.entry_1", reward)
                        .withColor(DescriptionUtils.POSITIVE_COLOR(true))),
                newLine,
                Component.translatable("relics.description.relic.dismantle.description", DismantleEffect.REQUIRED_HITS).withColor(DescriptionUtils.NEGATIVE_COLOR(false))
        );
    }
}
