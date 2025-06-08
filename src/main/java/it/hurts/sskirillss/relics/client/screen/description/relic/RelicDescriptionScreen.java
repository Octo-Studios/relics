package it.hurts.sskirillss.relics.client.screen.description.relic;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RelicBadgeWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.init.BadgeRegistry;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class RelicDescriptionScreen extends DescriptionScreen implements ITabbedDescriptionScreen, IPagedDescriptionScreen {
    @Getter
    @Setter
    private DescriptionPage page = DescriptionPage.DESCRIPTION;

    public RelicDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);
    }

    @Override
    protected void init() {
        super.init();

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        this.updateCache(relic);

        this.addRenderableWidget(new PageWidget(x + 242, y + 35, this, DescriptionPage.DESCRIPTION));
        this.addRenderableWidget(new PageWidget(x + 261, y + 35, this, DescriptionPage.STATISTIC));

        int xOff = 0;

        this.addRenderableWidget(new BigRelicCardWidget(x + 59, y + 43, this));

        for (RelicBadge badge : BadgeRegistry.BADGES.getEntries().stream().map(DeferredHolder::get).filter(entry -> entry instanceof RelicBadge).map(entry -> (RelicBadge) entry).toList()) {
//            if (!badge.isVisible(minecraft.player, stack))
//                continue;

            this.addRenderableWidget(new RelicBadgeWidget(x + 260 - xOff, y + 54, this, badge));

            xOff += 15;
        }

        this.addRenderableWidget(new RelicExperienceWidget(x + 142, y + 133, this));

        DescriptionContainerWidget container = null;

        switch (this.getPage()) {
            case DESCRIPTION -> container = new RelicDescriptionContainerWidget(x + 107, y + 77, this);
            case STATISTIC -> container = new RelicStatisticContainerWidget(x + 107, y + 77, this);
        }

        if (container != null) {
            this.addRenderableWidget(container);
            this.addRenderableWidget(new ScrollbarWidget(x + 279, y + 74, container));
        }
    }

    @Override
    public void rebuildWidgets() {
        stack = DescriptionUtils.gatherRelicStack(minecraft.player, slot);

        super.rebuildWidgets();
    }

    @Override
    public void tick() {
        super.tick();

        stack = DescriptionUtils.gatherRelicStack(minecraft.player, slot);

        LocalPlayer player = minecraft.player;

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem))
            return;

        RandomSource random = player.getRandom();

        if (player.tickCount % 3 == 0) {
            ParticleStorage.addParticle(this, new ExperienceParticleData(
                    new Color(140, random.nextInt(50), 255),
                    x + 73 + random.nextInt(20), y + 73 + random.nextInt(20),
                    1.5F + (random.nextFloat() * 0.5F), 100 + random.nextInt(50)));
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        guiGraphics.drawString(minecraft.font, Component.literal(stack.getDisplayName().getString().replace("[", "").replace("]", ""))
                .withStyle(ChatFormatting.BOLD), (int) ((x + 114) * 1.33F), (int) ((y + 62) * 1.33F), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        poseStack.pushPose();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/top_background_delimiter.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 107, y + 70)
                .end();

        poseStack.popPose();
    }

    public static List<FormattedCharSequence> justifyStyledText(Component text, int maxWidth, Font font) {
        var splitter = font.getSplitter();
        var words = new ArrayList<FormattedText>();
        text.visit((style, str) -> {
            for (var word : str.split(" ")) {
                if (!word.isEmpty()) words.add(FormattedText.of(word, style));
            }
            return Optional.empty();
        }, Style.EMPTY);

        var result = new ArrayList<FormattedCharSequence>();
        var line = new ArrayList<FormattedText>();
        float lineWidth = 0;

        for (var word : words) {
            float wordWidth = splitter.stringWidth(word) + font.width(" ");
            if (lineWidth + wordWidth > maxWidth && !line.isEmpty()) {
                if (line.size() == 1) {
                    result.add(Language.getInstance().getVisualOrder(line.get(0)));
                } else {
                    float totalWordsWidth = line.stream().map(splitter::stringWidth).reduce(0f, Float::sum);
                    int gaps = line.size() - 1;
                    float totalSpacing = maxWidth - totalWordsWidth;
                    int baseSpaces = (int) (totalSpacing / font.width(" "));
                    int extra = (int) (totalSpacing % font.width(" "));

                    var parts = new ArrayList<FormattedText>();
                    for (int i = 0; i < line.size(); i++) {
                        parts.add(line.get(i));
                        if (i < gaps) {
                            int count = baseSpaces / gaps + (i < baseSpaces % gaps ? 1 : 0);
                            parts.add(FormattedText.of(" ".repeat(Math.max(1, count))));
                        }
                    }
                    result.add(Language.getInstance().getVisualOrder(FormattedText.composite(parts)));
                }
                line.clear();
                lineWidth = 0;
            }
            line.add(word);
            lineWidth += wordWidth;
        }

        if (!line.isEmpty()) {
            var parts = new ArrayList<FormattedText>();
            for (int i = 0; i < line.size(); i++) {
                parts.add(line.get(i));
                if (i < line.size() - 1) parts.add(FormattedText.of(" "));
            }
            result.add(Language.getInstance().getVisualOrder(FormattedText.composite(parts)));
        }

        return result;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        for (GuiEventListener listener : this.children()) {
            if (listener instanceof AbstractButton button && button.isHovered()
                    && button instanceof IHoverableWidget widget) {
                guiGraphics.pose().translate(0, 0, 100);

                widget.onHovered(guiGraphics, pMouseX, pMouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();

            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        screen.rebuildWidgets();

        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public DescriptionTab getTab() {
        return DescriptionTab.RELIC;
    }
}