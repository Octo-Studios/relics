package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ChainParticleData;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.SparkParticleData;
import it.hurts.sskirillss.relics.client.screen.description.research.AbilityResearchScreen;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.lock.PacketAbilityUnlock;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AbilityCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final AbilityDescriptionScreen screen;
    private final String ability;

    private float scale = 1F;
    private float scaleOld = 1F;

    private int shakeDelta = 0;
    private int colorDelta = 0;

    public AbilityCardWidget(int x, int y, AbilityDescriptionScreen screen, String ability) {
        super(x, y, 32, 47);

        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public void onPress() {
        if (!(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        ItemStack stack = screen.getStack();

        boolean isEnoughLevel = relic.isEnoughLevel(stack, ability);
        boolean isLockUnlocked = relic.isLockUnlocked(stack, ability);
        boolean isAbilityResearched = relic.isAbilityResearched(stack, ability);

        SoundManager soundManager = minecraft.getSoundManager();

        if (isEnoughLevel) {
            if (isLockUnlocked) {
                if (isAbilityResearched)
                    screen.setSelectedAbility(ability);
                else
                    minecraft.setScreen(new AbilityResearchScreen(minecraft.player, screen.container, screen.slot, screen, ability));
            } else {
                int unlocks = relic.getLockUnlocks(stack, ability) + 1;

                RandomSource random = minecraft.player.getRandom();

                for (int i = 0; i < unlocks * 50; i++) {
                    var center = new Vec2(width / 2F, height / 2F);
                    var margin = new Vec2(center.x + MathUtils.randomFloat(random) * 7F, center.y + MathUtils.randomFloat(random) * 8.5F);

                    var motion = new Vec2(margin.x - center.x, margin.y - center.y).normalized().scale(5F + unlocks);

                    ParticleStorage.addParticle(screen, new SparkParticleData(new Color(150 + random.nextInt(100), 100 + random.nextInt(50), 0),
                            getX() + margin.x, getY() + margin.y, 1F + (random.nextFloat() * 0.5F), 20 + random.nextInt(100))
                            .setDeltaX(random.nextFloat() * motion.x)
                            .setDeltaY(random.nextFloat() * motion.y)
                    );
                }

                NetworkHandler.sendToServer(new PacketAbilityUnlock(screen.container, screen.slot, ability, unlocks));

                shakeDelta = Math.min(20, shakeDelta + 5 + random.nextInt(5));
                scale += 0.05F;

                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1F));

                if (unlocks >= relic.getMaxLockUnlocks()) {
                    for (int i = 0; i < 25; i++) {
                        var center = new Vec2(width / 2F, height / 2F);
                        var margin = new Vec2(center.x + MathUtils.randomFloat(random) * 7F, center.y + MathUtils.randomFloat(random) * 8.5F);

                        var motion = new Vec2(margin.x - center.x, margin.y - center.y).normalized().scale(7.5F);

                        ParticleStorage.addParticle(screen, new ChainParticleData(new Color(255, 255, 255),
                                getX() + margin.x, getY() + margin.y, 1F + (random.nextFloat() * 0.5F), 50 + random.nextInt(20))
                                .setDeltaX(random.nextFloat() * motion.x)
                                .setDeltaY(random.nextFloat() * motion.y)
                        );
                    }

                    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.WITHER_BREAK_BLOCK, 1F));
                    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, 1F));
                }
            }
        } else {
            shakeDelta = Math.min(20, shakeDelta + 10);
            colorDelta = Math.min(20, colorDelta + 10);

            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_BREAK, 1F));
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        var stack = screen.getStack();

        var manager = minecraft.getTextureManager();
        var poseStack = guiGraphics.pose();

        var unlocks = relic.getLockUnlocks(stack, ability);

        var isEnoughLevel = relic.isEnoughLevel(stack, ability);
        var isLockUnlocked = relic.isLockUnlocked(stack, ability);
        var isAbilityResearched = relic.isAbilityResearched(stack, ability);

        var canUse = isEnoughLevel && isLockUnlocked && isAbilityResearched;

        var canUpgrade = relic.mayPlayerUpgrade(minecraft.player, stack, ability);
        var canResearch = relic.mayResearch(stack, ability);

        var canBeUpgraded = relic.canBeUpgraded(ability);

        var hasAction = canUpgrade || canResearch;

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.enableBlend();

        poseStack.pushPose();

        var partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        var lerpedScale = Mth.lerp(partialTicks, scaleOld, scale);

        poseStack.scale(lerpedScale, lerpedScale, lerpedScale);

        poseStack.translate((getX() + (width / 2F)) / lerpedScale, (getY() + (height / 2F)) / lerpedScale, 0);

        var color = (float) ((canUpgrade ? 0.75F : 1.05F) + (Math.sin((player.tickCount + (ability.length() * 10)) * 0.2F) * 0.1F));

        if (isLockUnlocked)
            GUIRenderer.begin(DescriptionTextures.getAbilityCardTexture(stack, ability), poseStack)
                    .color(color, color, color, 1F)
                    .texSize(22, 31)
                    .scale(1.01F)
                    .end();

        if (!canUse)
            GUIRenderer.begin(isLockUnlocked ? DescriptionTextures.SMALL_CARD_RESEARCH_BACKGROUND : DescriptionTextures.SMALL_CARD_LOCK_BACKGROUND, poseStack)
                    .scale(1.01F)
                    .end();

        GUIRenderer.begin(canBeUpgraded ? canUse ? DescriptionTextures.SMALL_CARD_FRAME_UNLOCKED_ACTIVE : DescriptionTextures.SMALL_CARD_FRAME_UNLOCKED_INACTIVE : canUse ? DescriptionTextures.SMALL_CARD_FRAME_LOCKED_ACTIVE : DescriptionTextures.SMALL_CARD_FRAME_UNLOCKED_ACTIVE, poseStack).end();

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.SMALL_CARD_FRAME_OUTLINE, poseStack)
                    .pos(0, 0.5F)
                    .end();

        if (isLockUnlocked) {
            if (!isAbilityResearched) {
                var time = minecraft.player.tickCount + (ability.length() * 10F) + partialTick;

                GUIRenderer.begin(DescriptionTextures.RESEARCH, poseStack)
                        .pos((float) Math.sin(time * 0.25F), (float) Math.cos(time * 0.25F) + 0.5F)
                        .patternSize(16, 16)
                        .animation(AnimationData.builder()
                                .frame(0, 2).frame(1, 2)
                                .frame(2, 2).frame(3, 2)
                                .frame(4, 2).frame(5, 2)
                                .frame(6, 2).frame(7, 2)
                                .frame(8, 2).frame(9, 2)
                                .frame(10, 2).frame(11, 40))
                        .end();
            }
        } else {
            GUIRenderer.begin(isEnoughLevel ? ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/chains_active_" + unlocks + ".png") : DescriptionTextures.CHAINS_INACTIVE, poseStack)
                    .pos(0, -1)
                    .end();

            MutableComponent level = Component.literal(String.valueOf(relic.getAbilityData(ability).getRequiredLevel())).withStyle(ChatFormatting.BOLD);

            color = Math.min(0.75F, colorDelta * 0.04F);

            RenderSystem.setShaderColor(1, 1 - color, 1 - color, 1);

            poseStack.pushPose();

            if (shakeDelta > 0)
                poseStack.mulPose(Axis.ZP.rotation((float) Math.sin((player.tickCount + partialTick) * 0.75F) * ((shakeDelta / 30F) * 0.75F)));

            GUIRenderer.begin(isEnoughLevel ? ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/icons/lock_active_" + unlocks + ".png") : DescriptionTextures.LOCK_INACTIVE, poseStack).end();

            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(minecraft.font, level, (-(width / 2) + 16) * 2 - minecraft.font.width(level) / 2, (-(height / 2) + 24) * 2, isEnoughLevel ? 0xFFE278 : 0xB7AED9, true);

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            poseStack.popPose();
        }

        {
            if (canUse) {
                if (canUpgrade) {
                    RenderSystem.setShaderTexture(0, DescriptionTextures.UPGRADE);

                    manager.bindForSetup(DescriptionTextures.UPGRADE);

                    RenderSystem.enableBlend();

                    RenderUtils.renderAnimatedTextureFromCenter(poseStack, 0, -1, 20, 400, 20, 20, 0.9F + ((float) (Math.sin((player.tickCount + partialTick) * 0.25F) * 0.025F)), AnimationData.builder()
                            .frame(0, 2).frame(1, 2)
                            .frame(2, 2).frame(3, 2)
                            .frame(4, 2).frame(5, 2)
                            .frame(6, 2).frame(7, 2)
                            .frame(8, 2).frame(9, 2)
                            .frame(10, 2).frame(11, 2)
                            .frame(12, 2).frame(13, 2)
                            .frame(14, 2).frame(15, 2)
                            .frame(16, 2).frame(17, 2)
                            .frame(18, 2).frame(19, 2)
                    );

                    RenderSystem.disableBlend();
                }
            }
        }

        {
            if (canBeUpgraded && canUse) {
                int xOff = 0;

                for (int i = 0; i < 5; i++) {
                    guiGraphics.blit(DescriptionTextures.SMALL_STAR_HOLE, -(width / 2) + xOff + 4, -(height / 2) + 40, 0, 0, 4, 4, 4, 4);

                    xOff += 5;
                }

                xOff = 0;

                int quality = relic.getAbilityQuality(screen.stack, ability);
                boolean isAliquot = quality % 2 == 1;

                for (int i = 0; i < Math.floor(quality / 2D); i++) {
                    guiGraphics.blit(DescriptionTextures.SMALL_STAR_ACTIVE, -(width / 2) + xOff + 4, -(height / 2) + 40, 0, 0, 4, 4, 4, 4);

                    xOff += 5;
                }

                if (isAliquot)
                    guiGraphics.blit(DescriptionTextures.SMALL_STAR_ACTIVE, -(width / 2) + xOff + 4, -(height / 2) + 40, 0, 0, 2, 4, 4, 4);
            }
        }

        {
            if (canBeUpgraded) {
                MutableComponent title = Component.literal(canUse ? String.valueOf(relic.getAbilityLevel(screen.stack, ability)) : "?").withStyle(ChatFormatting.BOLD);

                float textScale = 0.5F;

                poseStack.scale(textScale, textScale, textScale);

                guiGraphics.drawString(minecraft.font, title, -((width + 1) / 2) - (minecraft.font.width(title) / 2) + 16, (-(height / 2) - 19), canUse ? 0xFFE278 : 0xB7AED9, true);
            }
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        float maxScale = 1.15F;
        float minScale = 1F;

        RandomSource random = minecraft.player.getRandom();

        boolean canUpgrade = relic.mayPlayerUpgrade(minecraft.player, screen.stack, ability);
        boolean canResearch = relic.mayResearch(screen.stack, ability);

        if (canUpgrade || canResearch) {
            if (minecraft.player.tickCount % 7 == 0)
                ParticleStorage.addParticle(screen, new ExperienceParticleData(new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                        getX() + 5 + random.nextInt(18), getY() + 18, 1F + (random.nextFloat() * 0.5F), 100 + random.nextInt(50)));
        }

        scaleOld = scale;

        if (scale > maxScale)
            scale = Math.max(minScale, scale - 0.01F);

        if (isHovered()) {
            if (minecraft.player.tickCount % 3 == 0)
                ParticleStorage.addParticle(screen, new ExperienceParticleData(
                        new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                        getX() + random.nextInt(width), getY() - 1, 1F + (random.nextFloat() * 0.5F), 100 + random.nextInt(50)));

            if (scale < maxScale)
                scale = Math.min(maxScale, scale + 0.04F);
        } else {
            if (scale > minScale)
                scale = Math.max(minScale, scale - 0.03F);
        }

        if (shakeDelta > 0)
            shakeDelta--;

        if (colorDelta > 0)
            colorDelta--;
    }

    @Override
    public boolean isLocked() {
        return screen.getSelectedAbility().equals(ability);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var stack = screen.stack;

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        AbilityData data = relic.getAbilityData(ability);

        if (data == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability, ability);

        int maxWidth = 110;
        int renderWidth = Math.min((minecraft.font.width(title.withStyle(ChatFormatting.BOLD)) / 2) + 4, maxWidth);

        List<MutableComponent> entries = new ArrayList<>();

        entries.add(Component.literal(" "));

        int level = relic.getRelicLevel(screen.stack);
        int requiredLevel = data.getRequiredLevel();

        if (level < requiredLevel) {
            entries.add(Component.literal(" "));

            entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.low_level", Component.literal(String.valueOf(requiredLevel)).withStyle(ChatFormatting.BOLD))));
        } else {
            if (!relic.isLockUnlocked(screen.stack, ability)) {
                entries.add(Component.literal(" "));

                entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_unlock", Component.literal(String.valueOf(relic.getMaxLockUnlocks() - relic.getLockUnlocks(screen.stack, ability))).withStyle(ChatFormatting.BOLD))));
            } else {
                if (!relic.isAbilityResearched(screen.stack, ability)) {
                    entries.add(Component.literal(" "));

                    entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.unresearched")));
                } else if (relic.mayPlayerUpgrade(minecraft.player, screen.stack, ability)) {
                    entries.add(Component.literal(" "));

                    entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_upgrade")));
                }
            }
        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry)) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 4, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        int height = tooltip.size() * 5;

        int y = getHeight() / 2;

        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        float lerpedScale = Mth.lerp(partialTicks, scaleOld, scale);

        poseStack.scale(lerpedScale, lerpedScale, lerpedScale);

        poseStack.translate((getX() + (getWidth() / 2F)) / lerpedScale, (getY() + (getHeight() / 2F)) / lerpedScale, 0);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, -((renderWidth + 19) / 2), y);

        int yOff = 0;

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        if (!relic.isAbilityUnlocked(stack, ability)) {
            title = ScreenUtils.stylizeWidthReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), ability.length());

            var random = minecraft.player.getRandom();

            var shakeX = MathUtils.randomFloat(random) * 0.5F;
            var shakeY = MathUtils.randomFloat(random) * 0.5F;

            poseStack.translate(shakeX, shakeY, 0F);

        } else
            title.withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, title, -(minecraft.font.width(title) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        for (FormattedCharSequence entry : tooltip) {
            poseStack.pushPose();

            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(minecraft.font, entry, -(minecraft.font.width(entry) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;

            poseStack.popPose();
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked() && screen.getStack().getItem() instanceof IRelicItem relic && relic.isAbilityUnlocked(screen.stack, ability))
            super.playDownSound(handler);
    }
}