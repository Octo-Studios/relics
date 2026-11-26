package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.handlers.RankHandler;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.C2SRankupRelic;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class RankupRelicActionWidget extends AbstractRelicActionWidget {
    public RankupRelicActionWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, "rankup", screen);
    }

    @Override
    public boolean isLocked() {
        var stack = getScreen().getStack();

        return DescriptionUtils.getEditableRelicSlot(minecraft.player, getScreen().getContainer(), getScreen().getSlot()) == null
                || !(stack.getItem() instanceof IRelicItem relic) || !relic.getRelicData(minecraft.player, stack).getLevelingData().mayPlayerRankup();
    }

    @Override
    public void onPress() {
        var stack = getScreen().getStack();

        if (!isLocked() && stack.getItem() instanceof IRelicItem relic)
            NetworkHandler.sendToServer(new C2SRankupRelic(getScreen().getContainer(), getScreen().getSlot(), stack.getItem(), relic.getRelicData(minecraft.player, stack).getLevelingData().getRank(), isQuickAction()));
    }

    @Override
    protected boolean isQuickAction() {
        return Screen.hasShiftDown();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            handler.play(SimpleSoundInstance.forUI(RelicsSounds.TABLE_REROLL.get(), 1F));
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var description = new ArrayList<MutableComponent>();

        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();

        var relicData = relic.getRelicData(player, stack);
        var levelingData = relicData.getLevelingData();

        var currentExperience = EntityUtils.getPlayerTotalExperience(player);
        var requiredExperience = levelingData.getRankupPlayerExperienceCost();
        var hasExperience = requiredExperience <= currentExperience;

        var newLine = Component.literal(" ");

        description.add(Component.translatable("relics.description.relic.rankup.title")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        description.add(newLine);

        if (relicData.isMaxRank()) {
            description.add(Component.translatable("relics.description.relic.rankup.max_rank").withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
        } else {
            description.add(Component.translatable("relics.description.general.cost.title")
                    .append(Component.literal(":"))
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.UNDERLINE));

            var relativeLevelCost = hasExperience ? EntityUtils.calculateExperienceLevelLoss(player, requiredExperience) : EntityUtils.getLevelFromTotalExperience(requiredExperience);
            var cost = levelingData.getRankupRankPointCost();
            var hasPoints = RankHandler.getRankPoints(player, stack.getItem()) >= cost;
            var quick = isQuickAction() && hasExperience && hasPoints;

            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.relic.rankup.cost.entry_1", quick ? requiredExperience + "+" : requiredExperience, quick ? relativeLevelCost + "+" : relativeLevelCost)
                            .withColor(hasExperience ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));

            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.relic.rankup.cost.entry_2", quick ? cost + "+" : cost)
                            .withColor(hasPoints ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));
            description.add(newLine);
            description.add(Component.translatable("relics.description.relic.rankup.auto").withColor(DescriptionUtils.NEUTRAL_COLOR(true)));
        }

        description.add(newLine);

        if (RelicsHotkeys.isHeld(RelicsHotkeys.RESEARCH_RELIC))
            description.add(Component.translatable("relics.description.relic.rankup.description")
                    .withStyle(ChatFormatting.ITALIC));
        else
            description.add(Component.translatable("relics.general.hold_shift", RelicsHotkeys.RESEARCH_RELIC.getTranslatedKeyMessage()));

        return description;
    }
}
