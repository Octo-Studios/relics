package it.hurts.sskirillss.relics.client.screen.description.relic;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RelicBadgeWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RankupRelicActionWidget;
import it.hurts.sskirillss.relics.init.RelicsBadges;
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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class RelicDescriptionScreen extends DescriptionScreen implements ITabbedDescriptionScreen, IPagedDescriptionScreen {
    @Getter
    @Setter
    private DescriptionSubcategory subcategory = DescriptionSubcategories.getSubcategory("relic_description");

    public RelicDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);
    }

    @Override
    protected void init() {
        super.init();

        if (this.stack == null || !(this.stack.getItem() instanceof IRelicItem relic))
            return;

        int xOff = 0;

        this.addRenderableWidget(new BigRelicCardWidget(x + 59, y + 43, this));

        for (RelicBadge badge : RelicsBadges.BADGES.getEntries().stream().map(DeferredHolder::get).filter(entry -> entry instanceof RelicBadge).map(entry -> (RelicBadge) entry).toList()) {
            if (!badge.isVisible(this.minecraft.player, stack))
                continue;

            this.addRenderableWidget(new RelicBadgeWidget(x + 260 - xOff, y + 54, this, badge));

            xOff += 15;
        }

        var container = subcategory.getContainerWidget(this);

        this.addRenderableWidget(container);
        this.addRenderableWidget(new ScrollbarWidget(x + 279, y + 74, container));

        this.addRenderableWidget(new RankupRelicActionWidget(x + 289, y + 84, this));
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

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/top_background_delimiter.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 107, y + 70)
                .end();

        poseStack.popPose();
    }

    public static List<FormattedCharSequence> justifyStyledText(Component text, int maxWidth) {
        var font = Minecraft.getInstance().font;
        var splitter = font.getSplitter();
        var words = new ArrayList<FormattedText>();
        text.visit((style, str) -> {
            for (var token : str.split(" ", -1)) {
                if (token.isEmpty())
                    words.add(FormattedText.of(" ", style));
                else
                    words.add(FormattedText.of(token, style));
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
                    result.add(Language.getInstance().getVisualOrder(line.getFirst()));
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
    public it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory getCategory() {
        return DescriptionCategories.getCategory("relic");
    }
}