package it.hurts.sskirillss.relics.client.screen.description.experience;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.experience.widgets.BigExperienceCardWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.widgets.ExperienceGemWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.widgets.ResetExperienceActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.widgets.UpgradeExperienceActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionCache;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicExperienceWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.init.BadgeRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
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
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public class ExperienceDescriptionScreen extends Screen implements IAutoScaledScreen, IRelicScreenProvider, IPagedDescriptionScreen {
    public final Screen screen;

    @Getter
    public final int container;
    @Getter
    public final int slot;
    @Getter
    public ItemStack stack;

    private final int backgroundHeight = 256;
    private final int backgroundWidth = 418;

    public UpgradeExperienceActionWidget upgradeButton;
    public ResetExperienceActionWidget resetButton;

    public ExperienceDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(Component.empty());

        this.container = container;
        this.slot = slot;
        this.screen = screen;

        stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    public String getSelectedSource() {
        return DescriptionCache.getSelectedExperienceSource((IRelicItem) stack.getItem());
    }

    public void setSelectedSource(String source) {
        DescriptionCache.setSelectedExperienceSource((IRelicItem) stack.getItem(), source);
    }

    @Override
    protected void init() {
        if (stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        updateCache(relic);

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        var sources = relic.getLevelingSourcesData().getSources();
        var abilities = relic.getAbilitiesData().getAbilities();

        this.addRenderableWidget(new PageWidget(x + 81, y + 123, this, DescriptionPage.RELIC, new RelicDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));

        int xOff = 19;

        if (!abilities.isEmpty()) {
            this.addRenderableWidget(new PageWidget(x + 81 + xOff, y + 123, this, DescriptionPage.ABILITY, new AbilityDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));

            xOff += 19;
        }

        if (!sources.isEmpty())
            this.addRenderableWidget(new PageWidget(x + 81 + xOff, y + 123, this, DescriptionPage.EXPERIENCE, new ExperienceDescriptionScreen(minecraft.player, this.container, this.slot, this.screen)));

        this.addRenderableWidget(new BigExperienceCardWidget(x + 60, y + 47, this));

        this.addRenderableWidget(new LogoWidget(x + 313, y + 57, this));

        if (relic.isSomethingWrongWithLevelingPoints(stack))
            this.addRenderableWidget(new PointsFixWidget(x + 330, y + 33, this));

        this.addRenderableWidget(new PointsPlateWidget(x + 313, y + 77, this));
        this.addRenderableWidget(new PlayerExperiencePlateWidget(x + 313, y + 102, this));
        this.addRenderableWidget(new LuckPlateWidget(x + 313, y + 127, this));

        xOff = 0;

        for (RelicBadge badge : BadgeRegistry.BADGES.getEntries().stream().map(DeferredHolder::get).filter(entry -> entry instanceof RelicBadge).map(entry -> (RelicBadge) entry).toList()) {
            if (!badge.isVisible(stack))
                continue;

            this.addRenderableWidget(new RelicBadgeWidget(x + 270 - xOff, y + 63, this, badge));

            xOff += 15;
        }

        if (!sources.isEmpty()) {
            int gemWidth = 34;
            int containerWidth = 209;

            int count = Math.min(5, sources.size());

            int spacing = gemWidth + 8 + (3 * (5 - count));

            xOff = (containerWidth / 2) - (((gemWidth * count) + ((spacing - gemWidth) * Math.max(count - 1, 0))) / 2);

            for (String entry : sources.keySet()) {
                this.addRenderableWidget(new ExperienceGemWidget(x + 77 + xOff, y + 153, this, entry));

                xOff += spacing;
            }
        }

        this.addRenderableWidget(new RelicExperienceWidget(x + 142, y + 121, this));

        var source = getSelectedSource();

        if (relic.isLevelingSourceUnlocked(stack, source)) {
            this.upgradeButton = this.addRenderableWidget(new UpgradeExperienceActionWidget(x + 288, y + 70, this, source));
            this.resetButton = this.addRenderableWidget(new ResetExperienceActionWidget(x + 288, y + 90, this, source));
        }
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
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        var player = minecraft.player;

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        var source = getSelectedSource();
        var sourceData = relic.getLevelingSourceData(source);

        if (sourceData == null)
            return;

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

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".leveling_source." + source + ".title", source);

        if (!relic.isLevelingSourceUnlocked(stack, source)) {
            title = ScreenUtils.stylizeWidthReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), source.length());

            var random = player.getRandom();

            var shakeX = MathUtils.randomFloat(random) * 0.5F;
            var shakeY = MathUtils.randomFloat(random) * 0.5F;

            poseStack.translate(shakeX, shakeY, 0F);
        } else
            title.withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, title, (int) ((x + 113) * 1.33F), (int) ((y + 67) * 1.33F), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        yOff = 9;

        var ability = sourceData.getRequiredAbility();

        title = Component.literal("«").append(ability.isEmpty() ? title : Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability, ability)).append("»").withStyle(ChatFormatting.BOLD);

        if (relic.isLevelingSourceUnlocked(stack, source)) {
            List<MutableComponent> components = new ArrayList<>();

            var wantsUpgrade = upgradeButton != null && upgradeButton.isHovered();
            var wantsReset = resetButton != null && resetButton.isHovered();

            int color = DescriptionUtils.TEXT_COLOR;

            var component = Component.literal(String.valueOf(relic.getLevelingSourceData(source).getInitialValue())).withStyle(ChatFormatting.BOLD);

            if (wantsUpgrade)
                color = 0x228B22;

            if (wantsReset)
                color = 0xB22222;

            if (color != DescriptionUtils.TEXT_COLOR) {
                var brightness = (float) (0.75F + 0.1F * Math.sin(2 * Math.PI * 0.75F * player.tickCount / 20F));

                var hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

                hsb[2] = Mth.clamp(brightness, 0F, 1F);

                color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            }

            components.add(component.withColor(color));

            var pattern = Pattern.compile("([^ .,!?;:]*%(1)\\$s[^ .,!?;:]*)");

            for (var line : font.getSplitter().splitLines(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".leveling_source." + source + ".description", "%1$s", title), 350, Style.EMPTY)) {
                String unformattedLine = line.getString().replace("%%", "%");

                int currentX = (x + 112) * 2;
                int currentY = (y + 74) * 2 + yOff;

                var matcher = pattern.matcher(unformattedLine);

                int lastEnd = 0;

                while (matcher.find()) {
                    String dynamicSegment = matcher.group(1);

                    int index = Integer.parseInt(matcher.group(2)) - 1;

                    String staticText = unformattedLine.substring(lastEnd, matcher.start());

                    if (!staticText.isEmpty()) {
                        guiGraphics.drawString(font, staticText, currentX, currentY, DescriptionUtils.TEXT_COLOR, false);

                        currentX += font.width(staticText);
                    }

                    if (index >= 0 && index < components.size()) {
                        MutableComponent dynamicComponent = components.get(index);

                        var dynamicValue = Component.literal(dynamicSegment.substring(0, dynamicSegment.indexOf('%')) + dynamicComponent.getString() + dynamicSegment.substring(dynamicSegment.lastIndexOf('s') + 1)).withStyle(dynamicComponent.getStyle());

                        guiGraphics.drawString(font, dynamicValue, currentX + 2, currentY + 1, color, false);

                        int frameStartX = currentX - 1;
                        int frameStartY = currentY - 1;
                        int frameEndX = currentX + font.width(dynamicValue) + 4;
                        int frameEndY = currentY + font.lineHeight + 1;

                        guiGraphics.fill(frameStartX, frameStartY, frameEndX, frameStartY + 1, 0xFF000000 + color);
                        guiGraphics.fill(frameStartX, frameEndY - 1, frameEndX, frameEndY, 0xFF000000 + color);
                        guiGraphics.fill(frameStartX, frameStartY, frameStartX + 1, frameEndY, 0xFF000000 + color);
                        guiGraphics.fill(frameEndX - 1, frameStartY, frameEndX, frameEndY, 0xFF000000 + color);

                        currentX += font.width(dynamicValue) + 3;

                        lastEnd = matcher.end();
                    }
                }

                if (lastEnd < unformattedLine.length())
                    guiGraphics.drawString(font, unformattedLine.substring(lastEnd), currentX, currentY, DescriptionUtils.TEXT_COLOR, false);

                yOff += 10;
            }
        } else {
            List<Object> placeholders = Arrays.asList(
                    relic.getLevelingSourceData(source).getInitialValue(),
                    title
            );

            var component = ScreenUtils.stylizeWidthReplacement(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".leveling_source." + source + ".description", placeholders.toArray()), 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT), source.length());

            for (FormattedCharSequence line : font.split(component, 350)) {
                guiGraphics.drawString(font, line, (x + 112) * 2, (y + 74) * 2 + yOff, 0x662f13, false);

                yOff += 10;
            }
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