package it.hurts.sskirillss.relics.client.screen.description.relic;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.PageWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicExperienceWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.init.BadgeRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class ExperienceDescriptionScreen extends Screen implements IAutoScaledScreen, IRelicScreenProvider, ITabbedDescriptionScreen {
    public final Screen screen;

    @Getter
    public final int container;
    @Getter
    public final int slot;
    @Getter
    public ItemStack stack;

    private final int backgroundHeight = 256;
    private final int backgroundWidth = 418;

    public ExperienceDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(Component.empty());

        this.container = container;
        this.slot = slot;
        this.screen = screen;

        stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    @Override
    protected void init() {
        if (stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        this.addRenderableWidget(new PageWidget(x + 81, y + 123, this, DescriptionPage.RELIC, new RelicDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));
        this.addRenderableWidget(new PageWidget(x + 100, y + 123, this, DescriptionPage.ABILITY, new AbilityDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));
        this.addRenderableWidget(new PageWidget(x + 119, y + 123, this, DescriptionPage.EXPERIENCE, new ExperienceDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));

        //this.addRenderableWidget(new BigRelicCardWidget(x + 60, y + 47, this));

        this.addRenderableWidget(new LogoWidget(x + 313, y + 57, this));

        if (relic.isSomethingWrongWithLevelingPoints(stack))
            this.addRenderableWidget(new PointsFixWidget(x + 330, y + 33, this));

        this.addRenderableWidget(new PointsPlateWidget(x + 313, y + 77, this));
        this.addRenderableWidget(new PlayerExperiencePlateWidget(x + 313, y + 102, this));
        this.addRenderableWidget(new LuckPlateWidget(x + 313, y + 127, this));

        int xOff = 0;

        for (RelicBadge badge : BadgeRegistry.BADGES.getEntries().stream().map(DeferredHolder::get).filter(entry -> entry instanceof RelicBadge).map(entry -> (RelicBadge) entry).toList()) {
            if (!badge.isVisible(stack))
                continue;

            this.addRenderableWidget(new RelicBadgeWidget(x + 270 - xOff, y + 63, this, badge));

            xOff += 15;
        }

        this.addRenderableWidget(new RelicExperienceWidget(x + 142, y + 121, this));
    }

    @Override
    public void tick() {
        super.tick();

        stack = DescriptionUtils.gatherRelicStack(minecraft.player, slot);

        LocalPlayer player = minecraft.player;

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem))
            return;

        RandomSource random = player.getRandom();

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

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

        LocalPlayer player = minecraft.player;

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        RelicData relicData = relic.getRelicData();

        if (relicData == null)
            return;

        int level = relic.getRelicLevel(stack);

        PoseStack poseStack = guiGraphics.pose();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, DescriptionTextures.SPACE_BACKGROUND);

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        int yOff = 0;
        int xOff = 0;

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

        GUIRenderer.begin(DescriptionTextures.BIG_CARD_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 67, y + 57)
                .end();

        GUIRenderer.begin(DescriptionTextures.TOP_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 107, y + 47)
                .end();

        GUIRenderer.begin(DescriptionTextures.BOTTOM_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 60, y + 133)
                .end();

        int quality = relic.getRelicQuality(stack);
        boolean isAliquot = quality % 2 == 1;

        for (int i = 0; i < Math.floor(quality / 2D); i++) {
            GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(x + xOff + 64, y + 110)
                    .end();

            xOff += 8;
        }

        if (isAliquot)
            GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(x + xOff + 64, y + 110)
                    .patternSize(4, 7)
                    .texSize(8, 7)
                    .end();

        poseStack.pushPose();

        float scale = 1.75F;

        poseStack.translate(x + 70 + 8 * scale, y + 69 + Math.sin((player.tickCount + pPartialTick) * 0.1F) * 2F + 8 * scale, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.05F) * 5F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.075F) * 25F));

        poseStack.translate(-8 * scale, -8 * scale, -150 * scale);

        poseStack.scale(scale, scale, scale);

        guiGraphics.renderItem(stack, 0, 0);

        poseStack.popPose();

        poseStack.pushPose();

        MutableComponent levelComponent = Component.literal(String.valueOf(level)).withStyle(ChatFormatting.BOLD);

        poseStack.scale(0.75F, 0.75F, 1F);

        guiGraphics.drawString(minecraft.font, levelComponent, (int) (((x + 85.5F) * 1.33F) - (minecraft.font.width(levelComponent) / 2F)), (int) ((y + 51) * 1.33F), 0xFFE278, true);

        guiGraphics.drawString(minecraft.font, Component.literal(stack.getDisplayName().getString()
                        .replace("[", "").replace("]", ""))
                .withStyle(ChatFormatting.BOLD), (int) ((x + 113) * 1.33F), (int) ((y + 67) * 1.33F), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        yOff = 9;

        for (FormattedCharSequence line : minecraft.font.split(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".description"), 340)) {
            guiGraphics.drawString(minecraft.font, line, (x + 112) * 2, (y + 74) * 2 + yOff, DescriptionUtils.TEXT_COLOR, false);

            yOff += 9;
        }

        poseStack.popPose();
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
    public void rebuildWidgets() {
        super.rebuildWidgets();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public int getAutoScale() {
        return 0;
    }

    @Override
    public DescriptionPage getPage() {
        return DescriptionPage.EXPERIENCE;
    }
}