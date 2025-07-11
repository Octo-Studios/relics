package it.hurts.sskirillss.relics.client.screen.description.ability;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory;
import it.hurts.sskirillss.relics.badges.base.AbilityBadge;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.AbilityBadgeWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.AbilityDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.PageWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.init.BadgeRegistry;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class AbilityDescriptionScreen extends DescriptionScreen implements ITabbedDescriptionScreen, IPagedDescriptionScreen {
    @Getter
    @Setter
    private int pageOld;

    @Getter
    @Setter
    private String selectedAbility;

    @Getter
    @Setter
    private DescriptionPage page = DescriptionPage.DESCRIPTION;

    @Getter
    private UpgradeAbilityActionWidget upgradeButton;
    @Getter
    private RerollAbilityActionWidget rerollButton;
    @Getter
    private ResetAbilityActionWidget resetButton;

    public AbilityDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);

        if (stack.getItem() instanceof IRelicItem relic) {
            var abilities = relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().stream()
                    .filter(entry -> relic.isAbilityEnabled(player, stack, entry))
                    .toList();

            this.setPageOld(abilities.indexOf(getSelectedAbility()) / 5);
        }
    }

    @Override
    protected void init() {
        super.init();

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var player = minecraft.player;

        if (this.selectedAbility == null)
            this.setSelectedAbility(relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().stream().findFirst().get());

        this.addRenderableWidget(new PageWidget(x + 242, y + 35, this, DescriptionPage.DESCRIPTION));
        this.addRenderableWidget(new PageWidget(x + 261, y + 35, this, DescriptionPage.STATISTIC));

        var ability = getSelectedAbility();

        if (relic.getAbilityTemplate(player, stack, ability) == null)
            return;

        var abilities = relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().stream()
                .filter(entry -> relic.isAbilityEnabled(player, stack, entry))
                .toList();

        var maxEntries = 5;

        if (abilities.size() > maxEntries) {
            this.addRenderableWidget(new AbilityPageWidget(x + 289, y + 151, this, -1));
            this.addRenderableWidget(new AbilityPageWidget(x + 289, y + 186, this, 1));
        }

        int startIndex = pageOld * maxEntries;
        int endIndex = Math.min(startIndex + maxEntries, abilities.size());

        var paginatedAbilities = (startIndex < abilities.size() && startIndex >= 0) ? abilities.subList(startIndex, endIndex) : new ArrayList<String>();

        int xOff = 0;

        this.addRenderableWidget(new BigAbilityCardWidget(x + 59, y + 43, this));

        if (relic.isAbilityUnlocked(player, stack, ability)) {
            for (AbilityBadge badge : BadgeRegistry.BADGES.getEntries().stream().map(DeferredHolder::get).filter(entry -> entry instanceof AbilityBadge).map(entry -> (AbilityBadge) entry).toList()) {
                if (!badge.isVisible(player, stack, ability))
                    continue;

                this.addRenderableWidget(new AbilityBadgeWidget(x + 260 - xOff, y + 54, this, badge, ability));

                xOff += 15;
            }
        }

        if (!paginatedAbilities.isEmpty()) {
            int objectWidth = 38;
            int containerWidth = 209;

            int count = paginatedAbilities.size();

            int spacing = objectWidth + 8 + (3 * (maxEntries - count));

            xOff = (containerWidth / 2) - (((objectWidth * count) + ((spacing - objectWidth) * Math.max(count - 1, 0))) / 2);

            for (String entry : paginatedAbilities) {
                this.addRenderableWidget(new AbilityCardWidget(x + 77 + xOff, y + 160, this, entry));

                xOff += spacing;
            }
        }

        if (relic.isAbilityUpgradeEnabled(player, stack, ability))
            this.upgradeButton = this.addRenderableWidget(new UpgradeAbilityActionWidget(x + 289, y + 63, this));
        if (relic.isAbilityRerollEnabled(player, stack, ability))
            this.rerollButton = this.addRenderableWidget(new RerollAbilityActionWidget(x + 289, y + 84, this));
        if (relic.isAbilityResetEnabled(player, stack, ability))
            this.resetButton = this.addRenderableWidget(new ResetAbilityActionWidget(x + 289, y + 105, this));

        DescriptionContainerWidget container = null;

        switch (this.getPage()) {
            case DESCRIPTION -> container = new AbilityDescriptionContainerWidget(x + 107, y + 77, this);
            //case STATISTIC -> container = new RelicStatisticContainerWidget(x + 107, y + 77, this);
        }

        if (container != null) {
            this.addRenderableWidget(container);
            this.addRenderableWidget(new ScrollbarWidget(x + 279, y + 74, container));
        }
    }

    @Override
    public void rebuildWidgets() {
        stack = DescriptionUtils.gatherRelicStack(Minecraft.getInstance().player, slot);

        super.rebuildWidgets();
    }

    @Override
    public void tick() {
        super.tick();

        stack = DescriptionUtils.gatherRelicStack(Minecraft.getInstance().player, slot);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        var player = Minecraft.getInstance().player;

        if (this.stack == null || !(this.stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        var ability = this.getSelectedAbility();

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability, ability);

        if (!relic.isAbilityUnlocked(player, stack, ability)) {
            title = ScreenUtils.stylizeWithReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), ability.length());

            var random = player.getRandom();

            var shakeX = MathUtils.randomFloat(random) * 0.5F;
            var shakeY = MathUtils.randomFloat(random) * 0.5F;

            poseStack.translate(shakeX, shakeY, 0F);
        } else
            title.withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, title.withStyle(ChatFormatting.BOLD), (int) ((x + 114) * 1.33F), (int) ((y + 62) * 1.33F), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        poseStack.pushPose();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/top_background_delimiter.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 107, y + 70)
                .end();

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
        if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
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
    public DescriptionCategory getCategory() {
        return DescriptionCategories.getCategory("ability");
    }
}