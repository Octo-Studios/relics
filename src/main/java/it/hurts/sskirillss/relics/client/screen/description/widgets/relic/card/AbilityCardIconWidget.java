package it.hurts.sskirillss.relics.client.screen.description.widgets.relic.card;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import it.hurts.sskirillss.relics.items.relics.base.utils.QualityUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class AbilityCardIconWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    private final RelicDescriptionScreen screen;
    private final String ability;

    public AbilityCardIconWidget(int x, int y, RelicDescriptionScreen screen, String ability) {
        super(x, y, 28, 37);

        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public void onPress() {
        MC.setScreen(new AbilityDescriptionScreen(screen.pos, screen.stack, ability));
    }

    @Override
    public void renderButton(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        TextureManager manager = MC.getTextureManager();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, RelicDescriptionScreen.TEXTURE);

        manager.bindForSetup(RelicDescriptionScreen.TEXTURE);

        if (AbilityUtils.canUseAbility(screen.stack, ability)) {
            blit(poseStack, x, y, 258, 0, 28, 37, 512, 512);

            if (isHovered)
                blit(poseStack, x - 1, y - 1, 318, 0, 30, 39, 512, 512);
        } else {
            blit(poseStack, x, y, 258, 39, 28, 37, 512, 512);

            if (isHovered)
                blit(poseStack, x - 1, y - 1, 318, 39, 30, 39, 512, 512);
        }

        ResourceLocation card = new ResourceLocation(Reference.MODID, "textures/gui/description/cards/" + ForgeRegistries.ITEMS.getKey(screen.stack.getItem()).getPath() + "/" + ability + ".png");

        RenderSystem.setShaderTexture(0, card);

        manager.bindForSetup(card);

        if (GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT) == 29) {
            if (!AbilityUtils.canUseAbility(screen.stack, ability))
                RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1F);

            blit(poseStack, x + 3, y + 3, 0, 0, 20, 29, 20, 29);
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, RelicDescriptionScreen.TEXTURE);

        manager.bindForSetup(RelicDescriptionScreen.TEXTURE);

        if (AbilityUtils.canUseAbility(screen.stack, ability))
            blit(poseStack, x, y, 288, 0, 28, 38, 512, 512);
        else
            blit(poseStack, x, y, 288, 39, 28, 38, 512, 512);
    }

    @Override
    public void onHovered(PoseStack poseStack, int mouseX, int mouseY) {
        if (!(screen.stack.getItem() instanceof RelicItem relic))
            return;

        RelicData relicData = relic.getRelicData();

        if (relicData == null)
            return;

        RelicAbilityEntry abilityData = AbilityUtils.getRelicAbilityEntry(relic, ability);

        if (abilityData == null)
            return;

        int points = AbilityUtils.getAbilityPoints(screen.stack, ability);
        int maxPoints = abilityData.getMaxLevel() == -1 ? (relicData.getLevelingData().getMaxLevel() / abilityData.getRequiredPoints()) : abilityData.getMaxLevel();

        int level = LevelingUtils.getLevel(screen.stack);
        int requiredLevel = abilityData.getRequiredLevel();

        MutableComponent name = Component.translatable("tooltip.relics." + ForgeRegistries.ITEMS.getKey(screen.stack.getItem()).getPath() + ".ability." + ability).withStyle(ChatFormatting.BOLD);

        int width = Mth.clamp(MC.font.width(name) / 2 + 12,60, 100);
        int height = this.height / 2 + 1;

        List<MutableComponent> description = new ArrayList<>();

        MutableComponent rarity = Component.translatable("tooltip.relics.relic.ability.tooltip.rarity");

        int yOff = 0;

        if (level < requiredLevel) {
            width = 105;

            description.add(rarity);

            description.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.ability.tooltip.low_level", requiredLevel, Component.translatable("tooltip.relics.relic.status.negative").withStyle(ChatFormatting.RED))));
        } else if (abilityData.getMaxLevel() == 0) {
            width = 105;

            description.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.ability.tooltip.no_stats")));
        } else {
            yOff += 4;

            description.add(Component.translatable("tooltip.relics.relic.ability.tooltip.level", points, maxPoints == -1 ? "∞" : maxPoints));
            description.add(rarity);
        }

        int renderX = x + 29;
        int renderY = y; // FIXME 1.19.2 :: Height too high?

        ScreenUtils.drawTexturedTooltipBorder(poseStack, RelicDescriptionScreen.BORDER_PAPER, width, height, renderX, renderY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        MC.font.draw(poseStack, name, (renderX + 9) * 2 + width - MC.font.width(name) / 2F, (renderY + 7) * 2, 0x412708);

        for (MutableComponent entry : description) {
            if (entry == rarity) {
                int texWidth = 512;
                int texHeight = 512;

                int xOff = 0;

                TextureManager manager = MC.getTextureManager();

                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, AbilityDescriptionScreen.TEXTURE);

                manager.bindForSetup(AbilityDescriptionScreen.TEXTURE);

                blit(poseStack, (x + 39) * 2 + MC.font.width(rarity), (renderY + 14) * 2 - 1 + yOff, 302, 44, 36, 8, texWidth, texHeight);

                for (int i = 1; i < QualityUtils.getAbilityQuality(screen.stack, ability) + 1; i++) {
                    boolean isAliquot = i % 2 == 1;

                    blit(poseStack, (x + 39) * 2 + MC.font.width(rarity) + xOff + 1, (renderY + 14) * 2 - 1 + yOff + 1, (AbilityUtils.canUseAbility(screen.stack, ability) ? 303 : 312) + (isAliquot ? 0 : 4), 54, isAliquot ? 4 : 3, 7, texWidth, texHeight);

                    xOff += isAliquot ? 4 : 3;
                }
            }

            List<FormattedCharSequence> lines = MC.font.split(entry, 210);

            for (FormattedCharSequence line : lines) {
                MC.font.draw(poseStack, line, (renderX + 9) * 2, (renderY + 14) * 2 + yOff, 0x412708);

                yOff += 9;
            }

            if (entry == rarity)
                yOff += 4;
        }

        poseStack.scale(1F, 1F, 1F);
    }
}