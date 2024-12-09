package it.hurts.sskirillss.relics.client.screen.description.research;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.*;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionCache;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.research.misc.BurnPoint;
import it.hurts.sskirillss.relics.client.screen.description.research.particles.ResearchParticleData;
import it.hurts.sskirillss.relics.client.screen.description.research.widgets.HintWidget;
import it.hurts.sskirillss.relics.client.screen.description.research.widgets.StarWidget;
import it.hurts.sskirillss.relics.client.screen.description.research.widgets.TipWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchData;
import it.hurts.sskirillss.relics.items.relics.base.data.research.StarData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.research.PacketManageLink;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class AbilityResearchScreen extends Screen implements IAutoScaledScreen, IRelicScreenProvider {
    public final Screen screen;

    @Getter
    public final int container;
    @Getter
    public final int slot;
    @Getter
    public ItemStack stack;

    public final String ability;

    public int backgroundHeight = 256;
    public int backgroundWidth = 418;

    public int x;
    public int y;

    @Nullable
    public StarData selectedStar;

    private List<BurnPoint> points = new ArrayList<>();
    private List<StarWidget> stars = new ArrayList<>();

    private final int maxResearchProgress = 40;
    private int researchProgress = 0;

    public AbilityResearchScreen(Player player, int container, int slot, Screen screen, String ability) {
        super(Component.empty());

        this.container = container;
        this.slot = slot;
        this.screen = screen;

        this.ability = ability;

        stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    public int getTotalConnectionsCount(StarData star) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0;

        return relic.getAbilityData(ability).getResearchData().getConnectedStars(star).size();
    }

    public int getOccupiedConnectionsCount(StarData star) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0;

        int index = star.getIndex();

        return (int) relic.getResearchLinks(stack, ability).entries().stream()
                .filter(entry -> entry.getKey() == index || entry.getValue() == index)
                .map(entry -> entry.getKey() < entry.getValue()
                        ? entry.getKey() + "-" + entry.getValue()
                        : entry.getValue() + "-" + entry.getKey())
                .distinct()
                .count();
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - backgroundWidth) / 2;
        this.y = (this.height - backgroundHeight) / 2;

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        researchProgress = 0;

        stars.clear();
        points.clear();

        this.addRenderableWidget(new LogoWidget(x + 313, y + 57, this));

        if (relic.isSomethingWrongWithLevelingPoints(stack))
            this.addRenderableWidget(new PointsFixWidget(x + 330, y + 33, this));

        this.addRenderableWidget(new PointsPlateWidget(x + 313, y + 77, this));
        this.addRenderableWidget(new PlayerExperiencePlateWidget(x + 313, y + 102, this));
        this.addRenderableWidget(new LuckPlateWidget(x + 313, y + 127, this));

        this.addRenderableWidget(new TipWidget(x + 117, y + 207, this));

        this.addRenderableWidget(new HintWidget(x + 192, y + 198, this));

        int starSize = 17;

        for (var entry : relic.getAbilityData(ability).getResearchData().getStars().values())
            stars.add(this.addWidget(new StarWidget((int) (x + 67 + (entry.getX() * 5F) - starSize / 2F), (int) (y + 54 + (entry.getY() * 5F) - starSize / 2F), this, entry)));
    }

    @Override
    public void tick() {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        super.tick();

        stack = DescriptionUtils.gatherRelicStack(minecraft.player, slot);

        RandomSource random = minecraft.player.getRandom();

        if (relic.isAbilityResearched(stack, ability)) {
            if (researchProgress >= 0 && researchProgress < maxResearchProgress) {
                researchProgress++;

                if (researchProgress % 3 == 0) {
                    ResearchData researchData = relic.getResearchData(ability);

                    for (var link : relic.getResearchLinks(stack, ability).entries()) {
                        var start = researchData.getStars().get(link.getKey()).getPos();
                        var end = researchData.getStars().get(link.getValue()).getPos();

                        executeForConnection(start, end, 0.75F, point -> {
                            ParticleStorage.addParticle(this, new ResearchParticleData(new Color(100 + random.nextInt(150), random.nextInt(25), 200 + random.nextInt(50)),
                                    point.x + MathUtils.randomFloat(random), point.y + MathUtils.randomFloat(random), 1F + (random.nextFloat() * 0.25F), 20 + random.nextInt(40 + researchProgress), random.nextFloat() * 0.025F));
                        });
                    }
                }
            }
        }

        if (minecraft.player.tickCount % 3 == 0) {
            var links = relic.getResearchLinks(stack, ability);

            for (var pair : links.entries()) {
                var start = pair.getKey();
                var end = pair.getValue();

                var startStar = stars.stream().filter(entry -> entry.getStar().getIndex() == start).findFirst();
                var endStar = stars.stream().filter(entry -> entry.getStar().getIndex() == end).findFirst();

                if (startStar.isPresent() && endStar.isPresent())
                    executeForConnection(startStar.get().getStar().getPos(), endStar.get().getStar().getPos(), 1.1F, point -> {
                        addStaticPoint((int) point.x, (int) point.y, 0.06F, Pair.of(start, end));
                    });
            }

            for (var pair : points.stream().map(BurnPoint::getLink).filter(Objects::nonNull).toList()) {
                var start = pair.getKey();
                var end = pair.getValue();

                if (!links.containsEntry(start, end) && !links.containsEntry(end, start)) {
                    this.points.stream().filter(point -> point.getLink() != null && point.getLink().getKey().equals(start) && point.getLink().getValue().equals(end)).forEach(entry -> {
                        entry.setLink(null);
                        entry.setTicker(point -> {
                            int time = point.getLifeTime();

                            if (time <= 0)
                                return;

                            point.setLifeTime(--time);

                            float diff = Mth.clamp(point.getLifeTime(), 0.01F, point.getMaxLifeTime()) / point.getMaxLifeTime();

                            point.setScaleO(point.getScale());
                            point.setScale(point.getScale() * diff);
                        });
                    });
                }
            }
        }

        for (BurnPoint point : points)
            point.tick();
    }

    private void addAbstractPoint(BurnPoint point) {
        if (points.size() >= 256)
            return;

        var optional = points.stream().filter(entry -> entry.getX() == point.getX() && entry.getY() == point.getY()).findAny();

        if (optional.isPresent()) {
            if (optional.get().getLifeTime() > 0)
                return;
        } else
            optional = points.stream().filter(entry -> entry.getLifeTime() <= 0).findFirst();

        if (optional.isEmpty())
            points.add(point);
        else optional.get().set(point);
    }

    private void addLivingPoint(int x, int y, int lifeTime, float scale) {
        addAbstractPoint(BurnPoint.builder(x, y, scale)
                .lifeTime(lifeTime)
                .maxLifeTime(lifeTime)
                .ticker(point -> {
                    int time = point.getLifeTime();

                    if (time < 0)
                        return;

                    point.setLifeTime(--time);

                    float diff = Mth.clamp(point.getLifeTime(), 0.01F, point.getMaxLifeTime()) / point.getMaxLifeTime();

                    point.setScaleO(point.getScale());
                    point.setScale(point.getMaxScale() * diff);
                })
                .build());
    }

    private void addStaticPoint(int x, int y, float scale, Pair<Integer, Integer> link) {
        addAbstractPoint(BurnPoint.builder(x, y, scale)
                .scale(0F)
                .lifeTime(1)
                .maxLifeTime(8)
                .ticker(point -> {
                    point.setScaleO(point.getScale());

                    if (researchProgress <= 0) {
                        int time = point.getLifeTime();

                        if (time >= point.getMaxLifeTime())
                            return;

                        point.setLifeTime(++time);

                        float diff = Mth.clamp(point.getLifeTime(), 0.01F, point.getMaxLifeTime()) / point.getMaxLifeTime();

                        point.setScale(point.getMaxScale() * diff);
                    } else
                        point.setScale(point.getScale() + (researchProgress * 0.00075F));
                })
                .link(link)
                .build());
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

        PoseStack poseStack = guiGraphics.pose();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

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

        {
            float color = (float) (0.5F + (Math.sin((player.tickCount + pPartialTick) * 0.1F) * 0.1F));

            GUIRenderer.begin(DescriptionTextures.getAbilityCardTexture(stack, ability), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .color(color, color, color, 1F)
                    .pos(x + 67, y + 54)
                    .texSize(110, 155)
                    .end();
        }

        {
            ResearchData researchData = relic.getAbilityData(ability).getResearchData();

            for (var link : relic.getResearchLinks(stack, ability).entries()) {
                var start = researchData.getStars().get(link.getKey()).getPos();
                var end = researchData.getStars().get(link.getValue()).getPos();

                drawLink(poseStack, getScaledPos(start), getScaledPos(end), pMouseX, pMouseY, pPartialTick);
            }

            if (selectedStar != null) {
                var pos = selectedStar.getPos();

                drawLink(poseStack, getScaledPos(pos), new Vec2(pMouseX, pMouseY), pMouseX, pMouseY, pPartialTick);
            }
        }

        {
            for (StarWidget widget : stars) {
                widget.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }

        {
            poseStack.pushPose();

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            RenderSystem.setShaderTexture(0, DescriptionTextures.RESEARCH_FOG);

            List<Vector2f> positions = Lists.newArrayList(new Vector2f(pMouseX, pMouseY));
            List<Float> scales = Lists.newArrayList(0.15F);
            List<Float> noises = Lists.newArrayList(10F);

            addLivingPoint(pMouseX, pMouseY, 3, 0.1F);

            for (BurnPoint point : points) {
                boolean shouldRender = point.getLifeTime() > 0;

                positions.add(new Vector2f(shouldRender ? point.getX() : -100, shouldRender ? point.getY() : -100));
                scales.add(Mth.lerp(pPartialTick, point.getScaleO(), point.getScale()));
                noises.add(point.getScale() * 75);
            }

            poseStack.translate(0, 0, 5000);

            RenderUtils.renderRevealingPanel(poseStack, x + 67, y + 54, 110, 155, positions, scales, noises, (player.tickCount + pPartialTick) / 50F);

            GUIRenderer.begin(DescriptionTextures.RESEARCH_BACKGROUND, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(x + 60, y + 45)
                    .end();

            poseStack.popPose();
        }

        {
            poseStack.pushPose();

            var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability, ability);

            if (!relic.isAbilityUnlocked(stack, ability)) {
                title = ScreenUtils.stylizeWidthReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), ability.length());

                var random = player.getRandom();

                var shakeX = MathUtils.randomFloat(random) * 0.5F;
                var shakeY = MathUtils.randomFloat(random) * 0.5F;

                poseStack.translate(shakeX, shakeY, 0F);
            } else
                title.withStyle(ChatFormatting.BOLD);

            poseStack.translate((int) (x + 184 + (102 / 2F) - (minecraft.font.width(title) / 2F / 1.3F)), y + 67, 0F);

            poseStack.scale(0.75F, 0.75F, 1F);

            guiGraphics.drawString(minecraft.font, title, 0, 0, DescriptionUtils.TEXT_COLOR, false);

            poseStack.popPose();
        }

        {
            poseStack.pushPose();

            poseStack.translate(x + 184 + (102 / 2F), y + 100, 0F);

            poseStack.scale(0.5F, 0.5F, 1F);

            int yOff = 0;

            List<Number> placeholders = new ArrayList<>();

            for (var stat : relic.getAbilityData(ability).getStats().values())
                placeholders.add(stat.getFormatValue().apply(relic.getStatValue(stack, ability, stat.getId(), relic.getAbilityLevel(stack, ability))));

            var component = Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability + ".description", placeholders.toArray());

            var startColor = 0xE500FF;
            var endColor = DescriptionUtils.TEXT_COLOR;

            float progress = (float) researchProgress / maxResearchProgress;

            int startRed = (startColor >> 16) & 0xFF;
            int startGreen = (startColor >> 8) & 0xFF;
            int startBlue = startColor & 0xFF;

            int endRed = (endColor >> 16) & 0xFF;
            int endGreen = (endColor >> 8) & 0xFF;
            int endBlue = endColor & 0xFF;

            int red = (int) (startRed + (endRed - startRed) * progress);
            int green = (int) (startGreen + (endGreen - startGreen) * progress);
            int blue = (int) (startBlue + (endBlue - startBlue) * progress);

            int color = (red << 16) | (green << 8) | blue;

            if (researchProgress < maxResearchProgress)
                component.withColor(color);

            component = ScreenUtils.stylizeWidthReplacement(component, 1F - progress, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT), ability.length());

            for (FormattedCharSequence line : minecraft.font.split(component, 180)) {
                guiGraphics.drawString(minecraft.font, line, -(minecraft.font.width(line) / 2F), yOff, DescriptionUtils.TEXT_COLOR, false);

                yOff += 10;
            }

            poseStack.popPose();
        }
    }

    public Vec2 getScaledPos(Vec2 pos) {
        int scale = 5;

        return new Vec2(x + 67 + (pos.x * scale), y + 54 + (pos.y * scale));
    }

    private void drawLink(PoseStack poseStack, Vec2 start, Vec2 end, int mouseX, int mouseY, float partialTick) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        poseStack.pushPose();

        float offset = (float) (Math.sin(((minecraft.player.tickCount + partialTick + start.length()) * 0.2F)) * 0.1F);
        float color = 1.25F + offset;

        if (!relic.isAbilityResearched(stack, ability) && isHoveringConnection(start, end, mouseX, mouseY))
            RenderSystem.setShaderColor(color, 0.25F, 0.25F, 0.75F + offset);
        else
            RenderSystem.setShaderColor(color, color, color, 0.75F + offset);

        RenderSystem.enableBlend();

        int width = 6;
        int height = 4;

        int distance = (int) Math.sqrt(start.distanceToSqr(end));

        poseStack.translate(start.x, start.y, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees(getAngle(start, end)));

        poseStack.translate(-(width / 2F), -(height / 2F), 0);

        RenderSystem.setShaderTexture(0, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/line.png"));

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/line.png"), poseStack)
                .pos(0, 0)
                .texSize(width, height * 6)
                .patternSize(distance, height)
                .anchor(SpriteAnchor.TOP_LEFT)
                .animation(AnimationData.builder()
                        .frame(0, 2).frame(1, 2).frame(2, 2)
                        .frame(3, 2).frame(4, 2).frame(5, 2)
                )
                .end();

        poseStack.mulPose(Axis.ZP.rotationDegrees(-getAngle(start, end)));

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void executeForConnection(Vec2 start, Vec2 end, float step, Consumer<Vec2> task) {
        int steps = (int) (Math.sqrt(start.distanceToSqr(end)) / step);

        Vec2 direction = new Vec2(end.x - start.x, end.y - start.y).normalized();

        for (int i = 0; i <= steps; i++) {
            Vec2 point = new Vec2(direction.x * step * i, direction.y * step * i).add(start);

            task.accept(getScaledPos(point));
        }
    }

    private boolean isHoveringConnection(Vec2 start, Vec2 end, int mouseX, int mouseY) {
        float minDistance = 4F;
        float thickness = 4F;

        float x1 = start.x;
        float y1 = start.y;

        float x2 = end.x;
        float y2 = end.y;

        double distanceToStart = Math.hypot(mouseX - x1, mouseY - y1);
        double distanceToEnd = Math.hypot(mouseX - x2, mouseY - y2);

        if (distanceToStart < minDistance || distanceToEnd < minDistance)
            return false;

        double collinearity = (x2 - x1) * (mouseY - y1) - (y2 - y1) * (mouseX - x1);

        double lineLength = Math.hypot(x2 - x1, y2 - y1);
        double distanceFromLine = Math.abs(collinearity / lineLength);

        if (distanceFromLine > thickness / 2)
            return false;

        return Math.min(x1, x2) - thickness / 2 <= mouseX && mouseX <= Math.max(x1, x2) + thickness / 2
                && Math.min(y1, y2) - thickness / 2 <= mouseY && mouseY <= Math.max(y1, y2) + thickness / 2;
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
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (stack.getItem() instanceof IRelicItem relic && !relic.isAbilityResearched(stack, ability) && pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            ResearchData researchData = relic.getResearchData(ability);

            Pair<Integer, Integer> toRemove = null;

            for (var link : relic.getResearchLinks(stack, ability).entries())
                if (isHoveringConnection(getScaledPos(researchData.getStars().get(link.getKey()).getPos()), getScaledPos(researchData.getStars().get(link.getValue()).getPos()), (int) pMouseX, (int) pMouseY))
                    toRemove = Pair.of(link.getKey(), link.getValue());

            if (toRemove != null)
                removeLink(toRemove.getKey(), toRemove.getValue());
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public void addLink(int start, int end) {
        var startStar = stars.stream().filter(entry -> entry.getStar().getIndex() == start).findFirst();
        var endStar = stars.stream().filter(entry -> entry.getStar().getIndex() == end).findFirst();

        if (startStar.isEmpty() || endStar.isEmpty())
            return;

        var random = minecraft.player.getRandom();

        NetworkHandler.sendToServer(new PacketManageLink(container, slot, ability, PacketManageLink.Operation.ADD, start, end));

        executeForConnection(startStar.get().getStar().getPos(), endStar.get().getStar().getPos(), 0.25F, point -> {
            ParticleStorage.addParticle(this, new ResearchParticleData(new Color(100 + random.nextInt(150), random.nextInt(25), 200 + random.nextInt(50)),
                    point.x + MathUtils.randomFloat(random), point.y + MathUtils.randomFloat(random), 1F + (random.nextFloat() * 0.25F), 20 + random.nextInt(60), random.nextFloat() * 0.025F));
        });
    }

    public void removeLink(int start, int end) {
        var startStar = stars.stream().filter(entry -> entry.getStar().getIndex() == start).findFirst();
        var endStar = stars.stream().filter(entry -> entry.getStar().getIndex() == end).findFirst();

        if (startStar.isEmpty() || endStar.isEmpty())
            return;

        var random = minecraft.player.getRandom();

        NetworkHandler.sendToServer(new PacketManageLink(container, slot, ability, PacketManageLink.Operation.REMOVE, start, end));

        executeForConnection(startStar.get().getStar().getPos(), endStar.get().getStar().getPos(), 0.1F, point -> {
            ParticleStorage.addParticle(this, new ResearchParticleData(new Color(100 + random.nextInt(150), random.nextInt(25), 200 + random.nextInt(50)),
                    point.x + MathUtils.randomFloat(random), point.y + MathUtils.randomFloat(random), 1F + (random.nextFloat() * 0.25F), 10 + random.nextInt(50), random.nextFloat() * 0.01F));
        });

        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.DISCONNECT_STARS.get(), 0.75F + minecraft.player.getRandom().nextFloat() * 0.5F, 0.75F));
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (stack.getItem() instanceof IRelicItem relic && pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && selectedStar != null) {

            for (StarWidget widget : stars) {
                if (!widget.isHovered())
                    continue;

                Multimap<Integer, Integer> links = relic.getResearchLinks(stack, ability);

                int start = selectedStar.getIndex();
                int end = widget.getStar().getIndex();

                StarData star = widget.getStar();

                if (start == end || getOccupiedConnectionsCount(star) >= getTotalConnectionsCount(star)
                        || links.containsEntry(start, end) || links.containsEntry(end, start))
                    continue;

                addLink(start, end);

                break;
            }

            selectedStar = null;
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void onClose() {
        screen.rebuildWidgets();

        minecraft.setScreen(screen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public int getAutoScale() {
        return 0;
    }

    public float getAngle(Vec2 from, Vec2 to) {
        float angle = (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));

        if (angle < 0)
            angle += 360;

        return angle;
    }
}