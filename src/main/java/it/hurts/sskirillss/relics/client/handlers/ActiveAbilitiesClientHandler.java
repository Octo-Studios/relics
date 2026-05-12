package it.hurts.sskirillss.relics.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relic_containers.AbilityReference;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.abilities.C2SActivateAbility;
import it.hurts.sskirillss.relics.relic_containers.CuriosRelicStackReference;
import it.hurts.sskirillss.relics.relic_containers.InventoryRelicStackReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class ActiveAbilitiesClientHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ResourceLocation CARD_FRAME_ACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_frame_active.png");
    private static final ResourceLocation CARD_FRAME_INACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_frame_inactive.png");
    private static final ResourceLocation CARD_FRAME_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_frame_outline.png");
    private static final ResourceLocation ARROW_LEFT = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_left.png");
    private static final ResourceLocation ARROW_RIGHT = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_right.png");
    private static final ResourceLocation ARROW_LEFT_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_left_outline.png");
    private static final ResourceLocation ARROW_RIGHT_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_right_outline.png");
    private static final ResourceLocation CURSOR = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/cursor.png");

    private static final int CARD_WIDTH = 40;
    private static final int CARD_HEIGHT = 48;
    private static final int CARD_OUTLINE_WIDTH = 42;
    private static final int CARD_OUTLINE_HEIGHT = 50;
    private static final int ICON_WIDTH = 22;
    private static final int ICON_HEIGHT = 31;
    private static final int ICON_X = (CARD_WIDTH - ICON_WIDTH) / 2;
    private static final int ICON_Y = (CARD_HEIGHT - ICON_HEIGHT) / 2;
    private static final int ARROW_WIDTH = 18;
    private static final int ARROW_HEIGHT = 48;
    private static final int ARROW_OUTLINE_WIDTH = 20;
    private static final int ARROW_OUTLINE_HEIGHT = 50;
    private static final int CURSOR_SIZE = 11;
    private static final int ARROW_GAP = 32;
    private static final int PAGE_SIZE = 8;
    private static final double CURSOR_SENSITIVITY = 1D;
    private static final double ROTATION_SPEED = 0.3D;
    private static final double ROTATION_SLOWDOWN_DURATION = 0.65D;
    private static final double ROTATION_RESUME_DURATION = 0.35D;
    private static final double OPEN_ANIMATION_DURATION = 0.35D;
    private static final double CLOSE_ANIMATION_DURATION = 0.35D;
    private static final double PAGE_SPIN_ANIMATION_DURATION = 0.16D;
    private static final double OPEN_SELECTION_THRESHOLD = 0.85D;
    private static final double OPEN_SPIRAL_ROTATION = Math.PI * 0.675D;
    private static final double PAGE_SPIN_ROTATION = Math.PI / 6D;

    private static double cursorX = 0D;
    private static double cursorY = 0D;
    private static double lastMouseX = 0D;
    private static double lastMouseY = 0D;
    private static boolean wasOpen = false;
    private static int page = 0;
    private static double rotation = 0D;
    private static double rotationSpeedMultiplier = 1D;
    private static long lastRotationNanos = 0L;
    private static long openStartedNanos = 0L;
    private static long closeStartedNanos = 0L;
    private static long pageSpinStartedNanos = 0L;
    private static double closeStartedProgress = 1D;
    private static int pageSpinDirection = 1;
    private static boolean slowingRotation = false;
    private static boolean closing = false;
    private static boolean wasLeftMouseDown = false;
    private static Tween rotationTween = null;
    private static AbilityReference castingReference = null;
    private static final Map<String, CardAnimation> CARD_ANIMATIONS = new HashMap<>();

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (MC.player == null || MC.screen != null)
            return;

        var keyDown = isAbilityListKeyDown();

        if (!keyDown && !closing) {
            if (!wasOpen)
                return;

            close();
            wasOpen = false;
        }

        if (keyDown && !wasOpen && !closing) {
            open();
            wasOpen = true;
        }

        if (!closing)
            updateVirtualCursor();

        var references = gatherReferences();
        var centerX = MC.getWindow().getGuiScaledWidth() / 2;
        var centerY = MC.getWindow().getGuiScaledHeight() / 2;
        if (references.isEmpty()) {
            if (closing)
                finishClosing();

            return;
        }

        page = clampPage(page, references.size());

        var visibleReferences = getVisibleReferences(references);
        var radius = getRadius(visibleReferences.size());
        var interfaceProgress = getInterfaceProgress();
        var interfaceAlpha = getInterfaceAlpha(interfaceProgress);
        var visualRotation = getVisualRotation();
        var acceptsInput = !closing && interfaceProgress >= OPEN_SELECTION_THRESHOLD;
        var selectedIndex = acceptsInput ? getSelectedIndex(visibleReferences, centerX, centerY, radius, visualRotation) : -1;

        updateRotation(selectedIndex >= 0);
        visualRotation = getVisualRotation();
        selectedIndex = acceptsInput ? getSelectedIndex(visibleReferences, centerX, centerY, radius, visualRotation) : -1;
        updateHoverAnimations(visibleReferences, selectedIndex);

        var hasMultiplePages = getPageCount(references.size()) > 1;

        renderArrows(guiGraphics, centerX, centerY, radius, hasMultiplePages, hasMultiplePages, interfaceProgress, interfaceAlpha);

        for (int index = 0; index < visibleReferences.size(); index++) {
            var point = getOpeningPoint(index, visibleReferences.size(), centerX, centerY, radius, visualRotation, interfaceProgress);
            var openScale = getOpenScale(interfaceProgress);

            renderAbility(guiGraphics, visibleReferences.get(index), point.x() - CARD_WIDTH / 2, point.y() - CARD_HEIGHT / 2, index == selectedIndex, openScale, interfaceAlpha);
        }

        if (keyDown && !closing)
            renderCursorMarker(guiGraphics, centerX + (int) Math.round(cursorX), centerY + (int) Math.round(cursorY));

        if (selectedIndex >= 0)
            renderSelectedTitle(guiGraphics, visibleReferences.get(selectedIndex), centerX, (int) Math.round(centerY + radius + 30));

        if (closing && getCloseProgress() >= 1D)
            finishClosing();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var open = MC.player != null && MC.screen == null && isAbilityListKeyDown();

        if (open && !wasOpen)
            open();

        if (!open) {
            if (wasOpen)
                close();

            wasOpen = false;

            return;
        }

        wasOpen = true;

        handleCasting();
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (!wasOpen || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!wasOpen)
            return;

        var references = gatherReferences();
        var nextPage = page + (event.getScrollDeltaY() < 0 ? 1 : -1);

        setPage(nextPage, references.size());

        event.setCanceled(true);
    }

    private static void open() {
        cursorX = -1D;
        cursorY = 0D;
        lastMouseX = MC.mouseHandler.xpos();
        lastMouseY = MC.mouseHandler.ypos();
        openStartedNanos = System.nanoTime();
        closeStartedNanos = 0L;
        pageSpinStartedNanos = 0L;
        closeStartedProgress = 1D;
        closing = false;
        wasLeftMouseDown = false;
        castingReference = null;
        resetAnimations();
    }

    private static void close() {
        if (castingReference != null) {
            sendActivation(castingReference, AbilityActivationStage.END);

            castingReference = null;
        }

        closeStartedProgress = getInterfaceProgress();
        closeStartedNanos = System.nanoTime();
        pageSpinStartedNanos = 0L;
        closing = true;
        wasLeftMouseDown = false;
        rotationSpeedMultiplier = 1D;
        slowingRotation = false;
        killRotationTween();
    }

    private static void handleCasting() {
        if (getOpenProgress() < OPEN_SELECTION_THRESHOLD)
            return;

        var references = gatherReferences();
        page = clampPage(page, references.size());

        var visibleReferences = getVisibleReferences(references);
        var centerX = MC.getWindow().getGuiScaledWidth() / 2;
        var centerY = MC.getWindow().getGuiScaledHeight() / 2;
        var radius = getRadius(visibleReferences.size());
        var selectedIndex = getSelectedIndex(visibleReferences, centerX, centerY, radius, getVisualRotation());
        var leftDown = isLeftMouseDown();
        var leftPressed = leftDown && !wasLeftMouseDown;

        if (!leftDown) {
            if (castingReference != null) {
                sendActivation(castingReference, AbilityActivationStage.END);

                castingReference = null;
            }

            wasLeftMouseDown = false;

            return;
        }

        if (leftPressed) {
            var arrow = getHoveredArrow(centerX, centerY, radius, getInterfaceProgress(), getPageCount(references.size()) > 1);

            if (arrow != null) {
                if (castingReference != null) {
                    sendActivation(castingReference, AbilityActivationStage.END);

                    castingReference = null;
                }

                setPage(page + (arrow == ArrowDirection.NEXT ? 1 : -1), references.size());
                wasLeftMouseDown = true;

                return;
            }
        }

        if (selectedIndex < 0 || selectedIndex >= visibleReferences.size()) {
            wasLeftMouseDown = true;

            return;
        }

        var selected = visibleReferences.get(selectedIndex);

        if (castingReference == null) {
            castingReference = selected;

            pulseCard(castingReference);
            playAbilityCastSound();
            sendActivation(castingReference, AbilityActivationStage.START);
            wasLeftMouseDown = true;

            return;
        }

        if (!castingReference.equals(selected)) {
            sendActivation(castingReference, AbilityActivationStage.END);

            castingReference = selected;

            pulseCard(castingReference);
            playAbilityCastSound();
            sendActivation(castingReference, AbilityActivationStage.START);
            wasLeftMouseDown = true;

            return;
        }

        sendActivation(castingReference, AbilityActivationStage.TICK);
        wasLeftMouseDown = true;
    }

    private static void updateVirtualCursor() {
        var mouseX = MC.mouseHandler.xpos();
        var mouseY = MC.mouseHandler.ypos();

        if (isLeftMouseDown()) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;

            return;
        }

        var guiScale = MC.getWindow().getGuiScale();

        cursorX += (mouseX - lastMouseX) / guiScale * CURSOR_SENSITIVITY;
        cursorY += (mouseY - lastMouseY) / guiScale * CURSOR_SENSITIVITY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        var length = Math.sqrt(cursorX * cursorX + cursorY * cursorY);
        var max = getRadius(getVisibleReferences(gatherReferences()).size()) + 50D;

        if (length > max) {
            cursorX = cursorX / length * max;
            cursorY = cursorY / length * max;
        }
    }

    public static List<AbilityReference> gatherReferences() {
        var player = MC.player;

        if (player == null)
            return List.of();

        List<AbilityReference> references = new ArrayList<>();

        for (var container : RelicsRegistries.RELIC_CONTAINER_REGISTRY)
            references.addAll(container.gatherAbilityReferences().apply(player));

        return references.stream()
                .filter(ActiveAbilitiesClientHandler::isUnlockedAbility)
                .toList();
    }

    private static boolean isUnlockedAbility(AbilityReference reference) {
        var player = MC.player;

        if (player == null)
            return false;

        var stack = reference.stackReference().getStack(player);

        if (!(stack.getItem() instanceof IRelicItem relic))
            return false;

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(reference.ability());

        return abilityData != null && abilityData.isUnlocked();
    }

    public static void sendActivation(AbilityReference reference, AbilityActivationStage stage) {
        var stackReference = reference.stackReference();

        if (stackReference instanceof InventoryRelicStackReference inventoryReference) {
            NetworkHandler.sendToServer(new C2SActivateAbility("inventory", inventoryReference.slot(), "", reference.ability(), stage));

            return;
        }

        if (stackReference instanceof CuriosRelicStackReference curiosReference)
            NetworkHandler.sendToServer(new C2SActivateAbility("curios", curiosReference.index(), curiosReference.identifier(), reference.ability(), stage));
    }

    private static List<AbilityReference> getVisibleReferences(List<AbilityReference> references) {
        return getVisibleReferences(references, page);
    }

    private static List<AbilityReference> getVisibleReferences(List<AbilityReference> references, int page) {
        if (references.isEmpty())
            return List.of();

        var currentPage = clampPage(page, references.size());
        var from = currentPage * PAGE_SIZE;
        var to = Math.min(from + PAGE_SIZE, references.size());

        return references.subList(from, to);
    }

    private static void setPage(int nextPage, int references) {
        var pageCount = getPageCount(references);
        var clampedPage = Math.floorMod(nextPage, pageCount);

        if (clampedPage == page)
            return;

        if (castingReference != null) {
            sendActivation(castingReference, AbilityActivationStage.END);

            castingReference = null;
        }

        pageSpinDirection = nextPage > page ? 1 : -1;
        pageSpinStartedNanos = System.nanoTime();
        page = clampedPage;
        resetAnimations();
    }

    private static int clampPage(int page, int references) {
        return Math.max(0, Math.min(page, getPageCount(references) - 1));
    }

    private static int getPageCount(int references) {
        return Math.max(1, (int) Math.ceil(references / (double) PAGE_SIZE));
    }

    public static boolean isAbilityListKeyDown() {
        var key = RelicsHotkeys.ACTIVE_ABILITIES_LIST.getKey();
        var window = MC.getWindow().getWindow();

        if (key.getType() == InputConstants.Type.MOUSE)
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;

        if (key.getType() == InputConstants.Type.KEYSYM)
            return GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;

        return RelicsHotkeys.ACTIVE_ABILITIES_LIST.isDown();
    }

    private static boolean isLeftMouseDown() {
        return GLFW.glfwGetMouseButton(MC.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    private static void renderAbility(GuiGraphics guiGraphics, AbilityReference reference, double x, double y, boolean selected, float openScale, float alpha) {
        var player = MC.player;
        var stack = reference.stackReference().getStack(player);

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(reference.ability());

        if (abilityData == null)
            return;

        var abilityTexture = DescriptionTextures.getAbilityCardTexture(stack, abilityData.getId());
        var canUse = abilityData.canPlayerActivate(player);
        var animation = getAnimation(reference);
        var scale = animation.getScale() * openScale;
        var pose = guiGraphics.pose();

        pose.pushPose();
        pose.translate(x + CARD_WIDTH / 2D, y + CARD_HEIGHT / 2D, 0);
        pose.mulPose(Axis.ZP.rotation(animation.clickZRotation));
        pose.scale(scale, scale, 1F);
        pose.translate(-CARD_WIDTH / 2D, -CARD_HEIGHT / 2D, 0);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
        guiGraphics.blit(abilityTexture, ICON_X, ICON_Y, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        guiGraphics.blit(canUse ? CARD_FRAME_ACTIVE : CARD_FRAME_INACTIVE, 0, 0, 0, 0, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT);

        if (selected)
            guiGraphics.blit(CARD_FRAME_OUTLINE, -1, -1, 0, 0, CARD_OUTLINE_WIDTH, CARD_OUTLINE_HEIGHT, CARD_OUTLINE_WIDTH, CARD_OUTLINE_HEIGHT);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();

        if (!canUse) {
            var overlayAlpha = Math.round(0x88 * alpha);

            guiGraphics.fill(0, 0, CARD_WIDTH, CARD_HEIGHT, overlayAlpha << 24);
        }

        pose.popPose();
    }

    private static void updateHoverAnimations(List<AbilityReference> references, int selectedIndex) {
        for (int index = 0; index < references.size(); index++)
            getAnimation(references.get(index)).setHovered(index == selectedIndex);
    }

    private static void pulseCard(AbilityReference reference) {
        getAnimation(reference).pulse();
    }

    private static void playAbilityCastSound() {
        MC.getSoundManager().play(SimpleSoundInstance.forUI(RelicsSounds.ABILITY_CAST.get(), 1F, 1F));
    }

    private static CardAnimation getAnimation(AbilityReference reference) {
        return CARD_ANIMATIONS.computeIfAbsent(getAnimationKey(reference), key -> new CardAnimation());
    }

    private static String getAnimationKey(AbilityReference reference) {
        return reference.stackReference() + ":" + reference.ability();
    }

    private static void resetAnimations() {
        CARD_ANIMATIONS.values().forEach(CardAnimation::kill);
        CARD_ANIMATIONS.clear();
    }

    private static void renderSelectedTitle(GuiGraphics guiGraphics, AbilityReference reference, int centerX, int y) {
        var player = MC.player;
        var stack = reference.stackReference().getStack(player);

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        var ability = reference.ability();
        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);

        if (abilityData == null)
            return;

        var title = Component.translatableWithFallback("relics.description." + itemId + ".ability." + ability, ability);

        if (!abilityData.getTemplate().getModes().isEmpty())
            title = title.copy()
                    .append(Component.literal(" ["))
                    .append(Component.translatable("relics.description." + itemId + ".ability." + ability + ".mode." + abilityData.getMode()))
                    .append(Component.literal("]"));

        guiGraphics.drawString(MC.font, title, centerX - MC.font.width(title) / 2, y, 0xFFFFFF, true);
    }

    private static void renderCursorMarker(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(CURSOR, x - CURSOR_SIZE / 2, y - CURSOR_SIZE / 2, 0, 0, CURSOR_SIZE, CURSOR_SIZE, CURSOR_SIZE, CURSOR_SIZE);
    }

    private static void renderArrows(GuiGraphics guiGraphics, int centerX, int centerY, double radius, boolean hasPrevious, boolean hasNext, double progress, float alpha) {
        var easedProgress = TransitionType.CUBIC.apply(EaseType.EASE_OUT, progress);
        var leftX = (int) Math.round(centerX + (centerX - radius - ARROW_GAP - ARROW_WIDTH - centerX) * easedProgress);
        var rightX = (int) Math.round(centerX + (centerX + radius + ARROW_GAP - centerX) * easedProgress);
        var y = centerY - ARROW_HEIGHT / 2;
        var hoveredArrow = getHoveredArrow(centerX, centerY, radius, progress, hasPrevious || hasNext);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);

        if (hasPrevious) {
            guiGraphics.blit(ARROW_LEFT, leftX, y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);

            if (hoveredArrow == ArrowDirection.PREVIOUS)
                guiGraphics.blit(ARROW_LEFT_OUTLINE, leftX - 1, y - 1, 0, 0, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT);
        }

        if (hasNext) {
            guiGraphics.blit(ARROW_RIGHT, rightX, y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);

            if (hoveredArrow == ArrowDirection.NEXT)
                guiGraphics.blit(ARROW_RIGHT_OUTLINE, rightX - 1, y - 1, 0, 0, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT);
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
    }

    private static ArrowDirection getHoveredArrow(int centerX, int centerY, double radius, double progress, boolean hasMultiplePages) {
        if (!hasMultiplePages)
            return null;

        var cursorScreenX = centerX + cursorX;
        var cursorScreenY = centerY + cursorY;
        var easedProgress = TransitionType.CUBIC.apply(EaseType.EASE_OUT, progress);
        var leftX = centerX + (centerX - radius - ARROW_GAP - ARROW_WIDTH - centerX) * easedProgress - 1D;
        var rightX = centerX + (centerX + radius + ARROW_GAP - centerX) * easedProgress - 1D;
        var y = centerY - ARROW_OUTLINE_HEIGHT / 2D;

        if (isInside(cursorScreenX, cursorScreenY, leftX, y, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT))
            return ArrowDirection.PREVIOUS;

        if (isInside(cursorScreenX, cursorScreenY, rightX, y, ARROW_OUTLINE_WIDTH, ARROW_OUTLINE_HEIGHT))
            return ArrowDirection.NEXT;

        return null;
    }

    private static boolean isInside(double x, double y, double left, double top, double width, double height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }

    private static int getSelectedIndex(List<AbilityReference> references, int centerX, int centerY, double radius, double rotation) {
        if (references.isEmpty())
            return -1;

        var cursorScreenX = centerX + cursorX;
        var cursorScreenY = centerY + cursorY;

        for (int index = references.size() - 1; index >= 0; index--) {
            var point = getPoint(index, references.size(), centerX, centerY, radius, rotation);
            var scale = 1.35D;
            var width = CARD_WIDTH * scale;
            var height = CARD_HEIGHT * scale;
            var left = point.x() - width / 2D;
            var top = point.y() - height / 2D;

            if (cursorScreenX >= left && cursorScreenX <= left + width && cursorScreenY >= top && cursorScreenY <= top + height)
                return index;
        }

        return -1;
    }

    private static Point getPoint(int index, int size, int centerX, int centerY, double radius, double rotation) {
        var angle = getAngle(index, size, rotation);

        return new Point(
                centerX + Math.cos(angle) * radius,
                centerY + Math.sin(angle) * radius
        );
    }

    private static Point getOpeningPoint(int index, int size, int centerX, int centerY, double radius, double rotation, double progress) {
        if (progress >= 1D)
            return getPoint(index, size, centerX, centerY, radius, rotation);

        var radiusProgress = TransitionType.CUBIC.apply(EaseType.EASE_OUT, progress);
        var angleProgress = TransitionType.SINE.apply(EaseType.EASE_OUT, progress);
        var angle = getAngle(index, size, rotation) - (1D - angleProgress) * OPEN_SPIRAL_ROTATION;
        var animatedRadius = radius * radiusProgress;

        return new Point(
                centerX + Math.cos(angle) * animatedRadius,
                centerY + Math.sin(angle) * animatedRadius
        );
    }

    private static double getAngle(int index, int size, double rotation) {
        return -Math.PI / 2D + ((Math.PI * 2D) * index / size) + rotation;
    }

    private static float getOpenScale(double progress) {
        if (progress >= 1D)
            return 1F;

        return (float) (0.25D + TransitionType.BACK.apply(EaseType.EASE_OUT, progress) * 0.75D);
    }

    private static float getInterfaceAlpha(double progress) {
        if (progress >= 1D)
            return 1F;

        return (float) TransitionType.SINE.apply(EaseType.EASE_OUT, progress);
    }

    private static double getInterfaceProgress() {
        if (!closing)
            return getOpenProgress();

        return closeStartedProgress * (1D - TransitionType.SINE.apply(EaseType.EASE_IN_OUT, getCloseProgress()));
    }

    private static double getOpenProgress() {
        if (openStartedNanos == 0L)
            return 1D;

        return Math.min(1D, (System.nanoTime() - openStartedNanos) / 1_000_000_000D / OPEN_ANIMATION_DURATION);
    }

    private static double getCloseProgress() {
        if (closeStartedNanos == 0L)
            return 1D;

        return Math.min(1D, (System.nanoTime() - closeStartedNanos) / 1_000_000_000D / getCloseDuration());
    }

    private static double getCloseDuration() {
        return CLOSE_ANIMATION_DURATION * Math.max(0.05D, closeStartedProgress);
    }

    private static double getVisualRotation() {
        if (pageSpinStartedNanos == 0L)
            return rotation;

        var progress = getPageSpinProgress();

        if (progress >= 1D) {
            pageSpinStartedNanos = 0L;

            return rotation;
        }

        var offset = pageSpinDirection * (1D - TransitionType.CUBIC.apply(EaseType.EASE_OUT, progress)) * PAGE_SPIN_ROTATION;

        return rotation - offset;
    }

    private static double getPageSpinProgress() {
        if (pageSpinStartedNanos == 0L)
            return 1D;

        return Math.min(1D, (System.nanoTime() - pageSpinStartedNanos) / 1_000_000_000D / PAGE_SPIN_ANIMATION_DURATION);
    }

    private static void finishClosing() {
        closing = false;
        closeStartedNanos = 0L;
        closeStartedProgress = 1D;
        resetAnimations();
    }

    private static void updateRotation(boolean hoveringCard) {
        var now = System.nanoTime();

        if (lastRotationNanos == 0L) {
            lastRotationNanos = now;

            return;
        }

        updateRotationSlowdown(hoveringCard);

        rotation += (now - lastRotationNanos) / 1_000_000_000D * ROTATION_SPEED * rotationSpeedMultiplier;

        lastRotationNanos = now;
    }

    private static void updateRotationSlowdown(boolean hoveringCard) {
        if (slowingRotation == hoveringCard)
            return;

        slowingRotation = hoveringCard;

        killRotationTween();

        rotationTween = Tween.create().setParallel(true);

        rotationTween.tweenMethod(ActiveAbilitiesClientHandler::setRotationSpeedMultiplier, rotationSpeedMultiplier, hoveringCard ? 0D : 1D, hoveringCard ? ROTATION_SLOWDOWN_DURATION : ROTATION_RESUME_DURATION)
                .setEaseType(EaseType.EASE_IN_OUT)
                .setTransitionType(TransitionType.SINE);

        rotationTween.start();
    }

    private static void killRotationTween() {
        if (rotationTween != null)
            rotationTween.kill();

        rotationTween = null;
    }

    private static void setRotationSpeedMultiplier(double rotationSpeedMultiplier) {
        ActiveAbilitiesClientHandler.rotationSpeedMultiplier = rotationSpeedMultiplier;
    }

    private static double getRadius(int cards) {
        return PAGE_SIZE * 64D / (Math.PI * 2D);
    }

    private record Point(double x, double y) {
    }

    private enum ArrowDirection {
        PREVIOUS,
        NEXT
    }

    private static class CardAnimation {
        private boolean hovered = false;
        private float hoverScale = 1F;
        private float pulseScale = 1F;
        private float clickZRotation = 0F;
        private Tween hoverTween = null;
        private Tween pulseTween = null;

        public float getScale() {
            return hoverScale * pulseScale;
        }

        public void setHovered(boolean hovered) {
            if (this.hovered == hovered)
                return;

            this.hovered = hovered;

            if (hoverTween != null)
                hoverTween.kill();

            var target = hovered ? 1.15F : 1F;

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenMethod(this::setHoverScale, hoverScale, target, 0.18D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(hovered ? TransitionType.BACK : TransitionType.CUBIC);

            hoverTween.start();
        }

        public void pulse() {
            if (pulseTween != null)
                pulseTween.kill();

            var targetRotation = ThreadLocalRandom.current().nextBoolean() ? 0.12F : -0.12F;

            pulseTween = Tween.create().setParallel(true);

            pulseTween.tweenMethod(this::setPulseScale, pulseScale, 1.12F, 0.06D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.EXPO);
            pulseTween.tweenMethod(this::setPulseScale, 1.12F, 1F, 0.14D)
                    .setDelay(0.04D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.CUBIC);
            pulseTween.tweenMethod(this::setClickZRotation, clickZRotation, targetRotation, 0.06D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.BACK);
            pulseTween.tweenMethod(this::setClickZRotation, targetRotation, 0F, 0.16D)
                    .setDelay(0.04D)
                    .setEaseType(EaseType.EASE_IN_OUT)
                    .setTransitionType(TransitionType.CUBIC);

            pulseTween.start();
        }

        public void kill() {
            if (hoverTween != null)
                hoverTween.kill();

            if (pulseTween != null)
                pulseTween.kill();
        }

        private void setHoverScale(float hoverScale) {
            this.hoverScale = hoverScale;
        }

        private void setPulseScale(float pulseScale) {
            this.pulseScale = pulseScale;
        }

        private void setClickZRotation(float clickZRotation) {
            this.clickZRotation = clickZRotation;
        }
    }
}
