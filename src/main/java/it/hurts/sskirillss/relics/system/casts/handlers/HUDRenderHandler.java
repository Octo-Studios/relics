package it.hurts.sskirillss.relics.system.casts.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.abilities.SpellCastPacket;
import it.hurts.sskirillss.relics.system.casts.abilities.AbilityCache;
import it.hurts.sskirillss.relics.system.casts.abilities.AbilityReference;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static it.hurts.sskirillss.relics.system.casts.handlers.CacheHandler.REFERENCES;

@OnlyIn(value = Dist.CLIENT)
public class HUDRenderHandler {
    public static final ResourceLocation CARD_FRAME_ACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_frame_active.png");
    public static final ResourceLocation CARD_FRAME_INACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_frame_inactive.png");

    public static final ResourceLocation CARD_POINTER_ACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_pointer_active.png");
    public static final ResourceLocation CARD_POINTER_INACTIVE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/card_pointer_inactive.png");

    public static final ResourceLocation ARROW_RIGHT = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_right.png");
    public static final ResourceLocation ARROW_LEFT = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_left.png");

    public static final ResourceLocation ARROW_RIGHT_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_right_outline.png");
    public static final ResourceLocation ARROW_LEFT_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/arrow_left_outline.png");

    public static final ResourceLocation STATE_TOGGLEABLE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/widgets/toggleable.png");
    public static final ResourceLocation STATE_CYCLICAL = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/widgets/cyclical.png");

    private static final Minecraft MC = Minecraft.getInstance();

    private static int selectedIndex = 0;

    private static boolean animationDown = false;
    private static int animationDelta = 0;

    private static int castShakeDelta = 0;

    private static int mouseDelta = 0;

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        if (animationDelta == 0)
            return;

        PoseStack poseStack = guiGraphics.pose();
        Window window = MC.getWindow();
        LocalPlayer player = MC.player;

        if (player == null || REFERENCES.isEmpty())
            return;

        int x = (window.getGuiScaledWidth()) / 2;
        int y = -38;

        poseStack.pushPose();

        poseStack.translate(0, (animationDelta - (animationDelta != 5 ? partialTicks * (animationDown ? -1 : 1) : 0)) * 16, 0);

        float shakeOffset = castShakeDelta > 0 ? ((castShakeDelta - partialTicks) * 0.25F) : 0;

        drawAbility(guiGraphics, player, -2, x - 74 - shakeOffset, y, partialTicks);
        drawAbility(guiGraphics, player, -1, x - 39 - shakeOffset, y, partialTicks);
        drawAbility(guiGraphics, player, 0, x, y, partialTicks);
        drawAbility(guiGraphics, player, 1, x + 39 + shakeOffset, y, partialTicks);
        drawAbility(guiGraphics, player, 2, x + 74 + shakeOffset, y, partialTicks);

        RenderSystem.setShaderTexture(0, ARROW_LEFT);
        RenderUtils.renderTextureFromCenter(poseStack, x - 100 - shakeOffset, y + 2, 0, 0, 11, 30, 11, 30, 1F + (mouseDelta < 0 ? Math.abs(mouseDelta) * 0.01F : 0));

        if (mouseDelta < 0) {
            RenderSystem.setShaderTexture(0, ARROW_LEFT_OUTLINE);

            RenderUtils.renderTextureFromCenter(poseStack, x - 100 - shakeOffset, y + 2, 0, 0, 13, 32, 13, 32, 1F + Math.abs(mouseDelta) * 0.01F);
        }

        RenderSystem.setShaderTexture(0, ARROW_RIGHT);
        RenderUtils.renderTextureFromCenter(poseStack, x + 100 + shakeOffset, y + 2, 0, 0, 11, 30, 11, 30, 1F + (mouseDelta > 0 ? Math.abs(mouseDelta) * 0.01F : 0));

        if (mouseDelta > 0) {
            RenderSystem.setShaderTexture(0, ARROW_RIGHT_OUTLINE);

            RenderUtils.renderTextureFromCenter(poseStack, x + 100 + shakeOffset, y + 2, 0, 0, 13, 32, 13, 32, 1F + Math.abs(mouseDelta) * 0.01F);
        }

        RenderSystem.disableBlend();

        AbilityReference selectedAbility = getAbilityByIndex(selectedIndex);
        AbilityCache selectedCache = getCacheByIndex(selectedIndex);

        if (selectedAbility == null || selectedCache == null)
            return;

        ItemStack stack = selectedAbility.getSlot().gatherStack(player);

        String registryName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        MutableComponent name = Component.translatable("relics.description." + registryName + ".ability." + selectedAbility.getId());

        guiGraphics.drawString(MC.font, name, x - MC.font.width(name) / 2, y - 38, 0xFFFFFF, true);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.translate((animationDelta - (animationDelta != 5 ? partialTicks * (animationDown ? -1 : 1) : 0)) * 16, 0, 0);

        int yOff = 0;

        x = -70;
        y = 25;

        for (Map.Entry<String, Boolean> entry : selectedCache.getPredicates().entrySet()) {
            String predicateName = entry.getKey();
            boolean isCompleted = entry.getValue();

            RenderSystem.setShaderTexture(0, isCompleted ? ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/icons/completed.png") : ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/icons/" + registryName + "/" + predicateName + ".png"));

            RenderUtils.renderTextureFromCenter(poseStack, x, y + yOff, 0, 0, 16, 16, 16, 16, 0.5F);

            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(MC.font, Component.translatable("relics.description." + registryName + ".ability." + selectedAbility.getId() + ".predicate." + predicateName).withStyle(isCompleted ? ChatFormatting.STRIKETHROUGH : ChatFormatting.RESET), (x + 7) * 2, (y - 2 + yOff) * 2, isCompleted ? 0xbeffb8 : 0xf17f9c, true);

            poseStack.scale(2F, 2F, 2F);

            yOff += 10;
        }

        poseStack.popPose();
    }

    private static void drawAbility(GuiGraphics guiGraphics, LocalPlayer player, int realIndex, float x, float y, float partialTicks) {
        int relativeIndex = getRelativeIndex(realIndex);

        AbilityReference ability = getAbilityByIndex(relativeIndex);
        AbilityCache cache = getCacheByIndex(relativeIndex);

        if (ability == null || cache == null)
            return;

        ItemStack stack = ability.getSlot().gatherStack(player);

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability.getId());
        var template = abilityData.getTemplate();

        if (template == null)
            return;

        boolean isLocked = !abilityData.canPlayerUse(player);

        ResourceLocation card = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/abilities/" + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "/" + template.getIcon().apply(player, stack, ability.getId()) + ".png");

        RenderSystem.setShaderTexture(0, card);

        RenderSystem.enableBlend();

        poseStack.pushPose();

        int width = 20;
        int height = 29;

        float scale = (float) ((1F + Mth.clamp(Math.pow(15F, -Math.abs(realIndex)), 0F, 0.2F)) + (realIndex == 0 ? (Math.sin((player.tickCount + partialTicks) * 0.1F) * 0.05F + (castShakeDelta > 0 ? ((castShakeDelta - partialTicks) * 0.02F) : 0F)) : 0F));

        RenderUtils.renderTextureFromCenter(poseStack, x - scale + 1, y - scale + 2, width, height, scale + 0.025F);

        int cooldown = abilityData.getExtenderData().getCooldown();
        int cap = abilityData.getExtenderData().getCooldownCap();

        String iconDescription = "";

        if (cooldown > 0) {
            RenderSystem.setShaderTexture(0, card);

            RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1F);

            float percentage = cooldown / (cap / 100F) / 100F;

            RenderUtils.renderTextureFromCenter(poseStack, x - scale, ((y + 2) - scale + (height * scale) / 2F) - (height * scale / 2F) * percentage, 0, height - height * percentage, width, height, width, height * percentage, scale + 0.025F);

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        RenderSystem.setShaderTexture(0, isLocked ? CARD_FRAME_INACTIVE : CARD_FRAME_ACTIVE);

        RenderUtils.renderTextureFromCenter(poseStack, x, y + 1, 0, 0, 30, 41, 30, 41, scale);

        if (abilityData.isTicking()) {
            CastType type = abilityData.getCastData().getType();

            if (type == CastType.TOGGLEABLE) {
                RenderSystem.setShaderTexture(0, STATE_TOGGLEABLE);

                RenderSystem.enableBlend();

                RenderUtils.renderAnimatedTextureFromCenter(poseStack, x, y + 1, 34, 630, 34, 45, scale, AnimationData.builder()
                        .frame(0, 1).frame(1, 1).frame(2, 1)
                        .frame(3, 1).frame(4, 1).frame(5, 1)
                        .frame(6, 1).frame(7, 1).frame(8, 1)
                        .frame(9, 1).frame(10, 1).frame(11, 1)
                        .frame(12, 1).frame(13, 1));

                RenderSystem.disableBlend();
            } else {
                RenderSystem.setShaderColor(1F, 1F, 1F, (float) ((Math.sin(player.tickCount * 0.5F) * 0.25F) + 0.75F));

                RenderSystem.enableBlend();

                RenderSystem.setShaderTexture(0, STATE_CYCLICAL);

                RenderUtils.renderTextureFromCenter(poseStack, x - 0.5F, y + 1, 33, 45, scale);

                RenderSystem.disableBlend();

                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
        }

        if (realIndex == 0) {
            RenderSystem.setShaderTexture(0, isLocked ? CARD_POINTER_INACTIVE : CARD_POINTER_ACTIVE);

            RenderUtils.renderTextureFromCenter(poseStack, x, y - 20, 0, 0, 6, 11, 6, 11, scale);
        }

        if (cooldown > 0) {
            RenderSystem.setShaderTexture(0, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/widgets/icons/cooldown.png"));

            RenderSystem.enableBlend();

            drawAbilityStatusIcon(cache, guiGraphics, x - scale, y - scale, 22, 352, scale - 0.1F, AnimationData.builder()
                            .frame(0, 2).frame(1, 2).frame(2, 2)
                            .frame(3, 2).frame(4, 2).frame(5, 2)
                            .frame(6, 2).frame(7, 2).frame(8, 2)
                            .frame(9, 1).frame(10, 1).frame(11, 1)
                            .frame(12, 1).frame(13, 1).frame(14, 1)
                            .frame(15, 1),
                    cap - cooldown, partialTicks);

            RenderSystem.disableBlend();

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            iconDescription = String.valueOf(MathUtils.round(cooldown / 20D, 1));
        } else {
            Collection<Boolean> infoEntries = cache.getPredicates().values();

            int successPredicates = 0;

            for (boolean value : infoEntries) {
                if (value)
                    successPredicates++;
            }

            int failedPredicates = infoEntries.size() - successPredicates;

            if (failedPredicates > 0) {
                RenderSystem.setShaderTexture(0, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/abilities/widgets/icons/locked.png"));

                RenderSystem.enableBlend();

                drawAbilityStatusIcon(cache, guiGraphics, x - scale, y - scale, 20, 80, scale - 0.1F, AnimationData.builder()
                                .frame(0, 20).frame(1, 2).frame(2, 2)
                                .frame(3, 2).frame(2, 2).frame(1, 2),
                        player.tickCount, partialTicks);

                RenderSystem.disableBlend();

                iconDescription = successPredicates + "/" + infoEntries.size();
            }
        }

        if (!iconDescription.isEmpty()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);

            MutableComponent descriptionComponent = Component.literal(iconDescription).withStyle(ChatFormatting.BOLD);

            guiGraphics.drawString(MC.font, descriptionComponent, (int) (x * 2F - (MC.font.width(descriptionComponent) / 2F)), (int) ((y - 6 + scale * 15) * 2F), 0xFFFFFF, true);
        }

        poseStack.popPose();
    }

    private static void drawAbilityStatusIcon(AbilityCache cache, GuiGraphics guiGraphics, float x, float y, float texWidth, float texHeight, float scale, @Nullable AnimationData animation, long ticks, float partialTicks) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(x + 1, y, 0);

        if (cache.getIconShakeDelta() != 0) {
            float color = cache.getIconShakeDelta() * 0.04F;

            RenderSystem.setShaderColor(1, 1 - color, 1 - color, 1);

            poseStack.mulPose(Axis.ZP.rotation((float) Math.sin((ticks + partialTicks) * 0.75F) * 0.1F));

            scale += (cache.getIconShakeDelta() - partialTicks) * 0.025F;
        }

        if (animation != null)
            RenderUtils.renderAnimatedTextureFromCenter(poseStack, 0, 0, texWidth, texHeight, texWidth, texWidth, scale, animation, ticks);
        else
            RenderUtils.renderTextureFromCenter(poseStack, 0, 0, texWidth, texHeight, scale);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.popPose();
    }

    private static int getRelativeIndex(int offset) {
        int current = selectedIndex;
        int sum = current + offset;
        int max = REFERENCES.size() - 1;

        return sum > max ? Math.min(max, sum - (max + 1)) : sum < 0 ? Math.max(0, sum + (max + 1)) : sum;
    }

    @Nullable
    private static AbilityReference getAbilityByIndex(int index) {
        if (REFERENCES.isEmpty())
            return null;

        return (AbilityReference) REFERENCES.keySet().toArray()[Mth.clamp(index, 0, REFERENCES.size() - 1)];
    }

    @Nullable
    private static AbilityCache getCacheByIndex(int index) {
        if (REFERENCES.isEmpty())
            return null;

        return (AbilityCache) REFERENCES.values().toArray()[Mth.clamp(index, 0, REFERENCES.size() - 1)];
    }

    private static void applyDelta(int delta) {
        int current = selectedIndex;
        int sum = current + delta;
        int max = REFERENCES.size() - 1;

        selectedIndex = sum > max ? sum - max - 1 : sum < 0 ? max : sum;
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class GeneralEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (!RelicsHotkeys.ACTIVE_ABILITIES_LIST.isDown() || REFERENCES.isEmpty())
                return;

            int current = selectedIndex;

            applyDelta(event.getScrollDeltaY() > 0 ? -1 : 1);

            if (current != selectedIndex) {
                mouseDelta = event.getScrollDeltaY() > 0 ? -10 : 10;

                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null)
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.5F + player.getRandom().nextFloat() * 0.25F);
            }

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            if (!(player instanceof LocalPlayer))
                return;

            if (castShakeDelta > 0)
                castShakeDelta--;

            if (mouseDelta > 0)
                mouseDelta--;
            else if (mouseDelta < 0)
                mouseDelta++;

            if (RelicsHotkeys.ACTIVE_ABILITIES_LIST.isDown()) {
                AbilityReference ability = getAbilityByIndex(selectedIndex);

                if (ability != null) {
                    ItemStack stack = ability.getSlot().gatherStack(player);

                    if (stack.getItem() instanceof IRelicItem relic) {
                        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability.getId());

                        if (abilityData.getTemplate() != null && abilityData.canPlayerUse(player))
                            relic.tickActiveAbilitySelection(stack, player, ability.getId());
                    }
                }

                if (animationDelta < 5)
                    animationDelta++;

                animationDown = true;
            } else {
                if (animationDelta > 0)
                    animationDelta--;

                animationDown = false;
            }

            if (animationDelta == 0)
                return;

            if (selectedIndex > REFERENCES.size() || selectedIndex < 0)
                selectedIndex = 0;
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class CastEvents {
        @SubscribeEvent
        public static void onKeyPressed(InputEvent.MouseButton.Pre event) {
            if (animationDelta == 0 || event.getAction() != InputConstants.PRESS
                    || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_1)
                return;

            Minecraft MC = Minecraft.getInstance();

            if (MC.screen != null)
                return;

            Player player = MC.player;

            if (player == null)
                return;

            AbilityReference ability = getAbilityByIndex(selectedIndex);
            AbilityCache cache = getCacheByIndex(selectedIndex);

            if (ability == null || cache == null)
                return;

            ItemStack stack = ability.getSlot().gatherStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability.getId());

            if (!abilityData.canPlayerUse(player)) {
                int delta = cache.getIconShakeDelta();

                cache.setIconShakeDelta(Math.min(20, delta + (delta > 0 ? 5 : 15)));

                MC.getSoundManager().play(SimpleSoundInstance.forUI(abilityData.isOnCooldown()
                        ? RelicsSounds.ABILITY_COOLDOWN.get() : RelicsSounds.ABILITY_LOCKED.get(), 1F));

                event.setCanceled(true);

                return;
            }

            boolean isTicking = abilityData.isTicking();

            CastType type = abilityData.getCastData().getType();

            MC.getSoundManager().play(SimpleSoundInstance.forUI(RelicsSounds.ABILITY_CAST.get(), 1F));

            switch (type) {
                case INSTANTANEOUS -> {
                    NetworkHandler.sendToServer(new SpellCastPacket(CastType.INSTANTANEOUS, CastStage.END, ability.serializeNBT()));

                    relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.END);
                }
                case CYCLICAL -> {
                    NetworkHandler.sendToServer(new SpellCastPacket(CastType.CYCLICAL, CastStage.START, ability.serializeNBT()));

                    relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.START);
                }
                case INTERRUPTIBLE -> {
                    CastStage stage = isTicking ? CastStage.END : CastStage.START;

                    NetworkHandler.sendToServer(new SpellCastPacket(CastType.INTERRUPTIBLE, stage, ability.serializeNBT()));

                    relic.castActiveAbility(player, stack, ability.getId(), type, stage);
                }
                case TOGGLEABLE -> {
                    CastStage stage = isTicking ? CastStage.END : CastStage.START;

                    NetworkHandler.sendToServer(new SpellCastPacket(CastType.TOGGLEABLE, stage, ability.serializeNBT()));

                    relic.castActiveAbility(player, stack, ability.getId(), type, stage);
                }
            }

            castShakeDelta = 10;

            Minecraft.getInstance().mouseHandler.releaseMouse();
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            if (!(player instanceof LocalPlayer))
                return;

            AbilityReference ability = getAbilityByIndex(selectedIndex);

            if (ability == null)
                return;

            ItemStack stack = ability.getSlot().gatherStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic) || !relic.getRelicData(player, stack).getTemplate().getAbilities().getAbilities().containsKey(ability.getId()))
                return;

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability.getId());
            boolean isTicking = abilityData.isTicking();
            boolean isCasting = Minecraft.getInstance().mouseHandler.isLeftPressed();

            AbilityTemplate entry = abilityData.getTemplate();

            if (entry == null)
                return;

            CastType type = entry.getCastData().getType();

            switch (type) {
                case CYCLICAL -> {
                    if (isTicking) {
                        if (isCasting) {
                            NetworkHandler.sendToServer(new SpellCastPacket(CastType.CYCLICAL, CastStage.TICK, ability.serializeNBT()));

                            relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.TICK);
                        } else {
                            NetworkHandler.sendToServer(new SpellCastPacket(CastType.CYCLICAL, CastStage.END, ability.serializeNBT()));

                            relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.END);
                        }
                    }
                }
                case INTERRUPTIBLE -> {
                    if (isTicking) {
                        NetworkHandler.sendToServer(new SpellCastPacket(CastType.INTERRUPTIBLE, CastStage.TICK, ability.serializeNBT()));

                        relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.TICK);
                    }
                }
                case TOGGLEABLE -> {
                    if (isTicking) {
                        NetworkHandler.sendToServer(new SpellCastPacket(CastType.TOGGLEABLE, CastStage.TICK, ability.serializeNBT()));

                        relic.castActiveAbility(player, stack, ability.getId(), type, CastStage.TICK);
                    }
                }
            }
        }
    }
}
