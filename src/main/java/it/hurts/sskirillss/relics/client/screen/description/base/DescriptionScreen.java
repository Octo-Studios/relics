package it.hurts.sskirillss.relics.client.screen.description.base;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BookmarkWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.TabWidget;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO: Get rid of IRelicScreenProvider, use DescriptionScreen instead
@OnlyIn(Dist.CLIENT)
public class DescriptionScreen extends Screen implements IRelicScreenProvider, IAutoScaledScreen {
    public final Screen screen;

    @Getter
    public final int container;
    @Getter
    public final int slot;
    @Getter
    public ItemStack stack;

    public final int backgroundHeight = 256;
    public final int backgroundWidth = 418;

    public int x;
    public int y;

    protected DescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(Component.empty());

        this.container = container;
        this.slot = slot;
        this.screen = screen;

        stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        initSidebar();
        initTabs();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderSpaceBackground(guiGraphics);

        renderTopScroll(guiGraphics);
        renderBottomScroll(guiGraphics);
    }

    protected void initSidebar() {
        if (LogoWidget.getRemainingClicks() != 0)
            this.addRenderableWidget(new LogoWidget(this.x + 313, this.y + 53, this));

//        FIXME: Somebody do something :'\
//        if (relic.isSomethingWrongWithLevelingPoints(minecraft.player, stack))
//            this.addRenderableWidget(new PointsFixWidget(x + 330, y + 33, this));

        this.addRenderableWidget(new RankPlateWidget(this.x + 313, this.y + 77, this));
        this.addRenderableWidget(new PointsPlateWidget(this.x + 313, this.y + 102, this));
        this.addRenderableWidget(new PlayerExperiencePlateWidget(this.x + 313, this.y + 127, this));
        this.addRenderableWidget(new LuckPlateWidget(this.x + 313, this.y + 152, this));
    }

    protected void initTabs() {
        var player = this.minecraft.player;
        var stack = this.getStack();
        var relic = ((IRelicItem) stack.getItem());

        int xOff = 19;

        this.addRenderableWidget(new TabWidget(x + 81, y + 134, this, DescriptionTab.RELIC, new RelicDescriptionScreen(player, this.container, this.slot, this.screen)));

        if (!relic.getAbilitiesTemplate(player, stack).getAbilities().isEmpty()) {
            this.addRenderableWidget(new TabWidget(x + 81 + xOff, y + 134, this, DescriptionTab.ABILITY, new AbilityDescriptionScreen(player, this.container, this.slot, this.screen)));

            xOff += 19;
        }

        // FIXME: Should not be empty, don't blame me
        if (relic.getAbilitiesTemplate(player, stack).getSynergies().isEmpty())
            this.addRenderableWidget(new TabWidget(x + 81 + xOff, y + 134, this, DescriptionTab.SYNERGY, new ExperienceDescriptionScreen(player, this.container, this.slot, this.screen)));
    }

    protected void renderSpaceBackground(GuiGraphics guiGraphics) {
        var poseStack = guiGraphics.pose();

        GUIRenderer.begin(DescriptionTextures.SPACE_BACKGROUND, poseStack)
                .texSize(418, 4096)
                .patternSize(backgroundWidth, backgroundHeight)
                .pos(x + (backgroundWidth / 2F), y + (backgroundHeight / 2F))
                .animation(AnimationData.builder()
                        .frame(0, 2).frame(1, 2).frame(2, 2)
                        .frame(3, 2).frame(4, 2).frame(5, 2)
                        .frame(6, 2).frame(7, 2).frame(8, 2)
                        .frame(9, 2).frame(10, 2).frame(11, 2)
                        .frame(12, 2).frame(13, 2).frame(14, 2)
                        .frame(15, 2))
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

    @Override
    public int getAutoScale() {
        return 4;
    }
}