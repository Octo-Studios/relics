package it.hurts.sskirillss.relics.client.screen.description.base;

import it.hurts.sskirillss.relics.api.relics.IDocsEntry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategories;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.PageWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicExperienceWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.TabWidget;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class DescriptionScreen extends SimpleDescriptionScreen {
    public final int backgroundHeight = 256;
    public final int backgroundWidth = 418;

    public int x;
    public int y;

    protected DescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.initSidebar();
        this.initTopScroll();
        this.initBottomScroll();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderSpaceBackground(guiGraphics);

        renderTopScroll(guiGraphics);
        renderBottomScroll(guiGraphics);
    }

    protected void initSidebar() {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        if (LogoWidget.getRemainingClicks() != 0)
            this.addRenderableWidget(new LogoWidget(this.x + 313, this.y + 53, this));

        if (relic.getRelicData(minecraft.player, stack).getLevelingData().isPointsMismatch())
            this.addRenderableWidget(new PointsFixWidget(x + 330, y + 33, this));

        this.addRenderableWidget(new RelicProgressPlateWidget(this.x + 313, this.y + 77, this));
        this.addRenderableWidget(new RankPlateWidget(this.x + 313, this.y + 102, this));
        this.addRenderableWidget(new LevelingPointsPlateWidget(this.x + 313, this.y + 127, this));
        this.addRenderableWidget(new PlayerExperiencePlateWidget(this.x + 313, this.y + 152, this));

        if (relic instanceof IDocsEntry entry && entry.getURI(minecraft.player, stack) != null)
            this.addRenderableWidget(new DocumentationWidget(this.x + 321, this.y + 179, this));

        this.addRenderableWidget(new DiscordWidget(this.x + 345, this.y + 179, this));
    }

    protected void initTopScroll() {
        var player = minecraft.player;

        int xOff = 0;

        var subcategories = DescriptionSubcategories.getSubcategories().values().stream()
                .filter(subcategory -> subcategory.shouldAppear(this, player, stack))
                .sorted(Comparator.comparingInt(subcategory -> subcategory.getOrder(player, stack)))
                .collect(Collectors.toList());

        if (subcategories.size() > 1) {
            Collections.reverse(subcategories);

            for (var subcategory : subcategories) {
                this.addRenderableWidget(new PageWidget(x + 261 + xOff, y + 35, this, subcategory));

                xOff -= 19;
            }
        }

        this.addRenderableWidget(new RelicExperienceWidget(x + 142, y + 133, this));
    }

    protected void initBottomScroll() {
        var player = minecraft.player;

        int xOff = 0;

        for (var category : DescriptionCategories.getCategories().values().stream()
                .filter(category -> category.shouldAppear(player, stack))
                .sorted(Comparator.comparingInt(category -> category.getOrder(player, stack))).toList()) {
            this.addRenderableWidget(new TabWidget(x + 81 + xOff, y + 134, this, category));

            xOff += 19;
        }
    }

    protected void renderSpaceBackground(GuiGraphics guiGraphics) {
        var poseStack = guiGraphics.pose();

        GUIRenderer.begin(DescriptionTextures.SPACE_BACKGROUND, poseStack)
                .texSize(418, 4096)
                .patternSize(backgroundWidth, backgroundHeight)
                .pos(x + (backgroundWidth / 2F), y + (backgroundHeight / 2F))
                .animation(AnimationData.construct(4096, backgroundHeight, 2))
                .end();
    }

    protected void renderTopScroll(GuiGraphics guiGraphics) {
        var poseStack = guiGraphics.pose();

        GUIRenderer.begin(DescriptionTextures.TOP_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 105, y + 45)
                .end();
    }

    protected void renderBottomScroll(GuiGraphics guiGraphics) {
        var poseStack = guiGraphics.pose();

        GUIRenderer.begin(DescriptionTextures.BOTTOM_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 59, y + 144)
                .end();
    }
}
