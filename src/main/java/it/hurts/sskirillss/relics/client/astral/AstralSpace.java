package it.hurts.sskirillss.relics.client.astral;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberration;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SPushAstralSatchelItem;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSetAstralSatchelActive;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSetAstralSatchelAim;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSyncAstralSatchelCamera;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SWithdrawAstralSatchelItem;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AstralSpace {
    public static final float HIT_RADIUS = 0.65F;
    public static final float TRANSITION_DURATION = 1.6F;

    private static final Minecraft MC = Minecraft.getInstance();
    private static final long CLOCK_START = System.nanoTime();

    private static Phase phase = Phase.NONE;
    private static boolean scene;
    private static boolean released;
    private static long phaseStart;
    private static long lastUseAt;
    private static Vec3 camera = Vec3.ZERO;
    private static Vec3 previousCamera = Vec3.ZERO;
    private static Vec3 velocity = Vec3.ZERO;
    private static Vec3 previousVelocity = Vec3.ZERO;
    private static Vec3 sentCamera = Vec3.ZERO;
    private static UUID sentAim;

    @Getter
    private static double radius = AstralSatchelItem.DEFAULT_RADIUS;
    @Getter
    private static ItemStack labelStack = ItemStack.EMPTY;
    @Getter
    private static float labelRadius;
    @Getter
    private static float labelX;
    @Getter
    private static float labelY;
    @Getter
    private static float labelStrength;

    private enum Phase {
        NONE, ENTER, EXIT
    }

    public static boolean isRendering() {
        return scene && phase == Phase.NONE;
    }

    public static boolean isTransitioning() {
        return scene && phase != Phase.NONE;
    }

    public static boolean isActive() {
        return scene && phase != Phase.EXIT;
    }

    public static boolean isLocked() {
        return (scene || phase != Phase.NONE) && !released;
    }

    public static boolean isEngaged() {
        return scene || phase != Phase.NONE;
    }

    public static void enter(int slot) {
        var player = MC.player;

        if (player == null || isEngaged())
            return;

        AstralSatchelItem.setSlot(player, slot);

        beginScene();

        setPhase(Phase.ENTER);

        lastUseAt = phaseStart;

        echo();

        NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(true, slot));
    }

    public static void exit() {
        if (!scene || phase != Phase.NONE)
            return;

        setPhase(Phase.EXIT);

        released = true;

        echo();

        NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(false, -1));
    }

    private static void hardExit() {
        if (MC.player != null && isEngaged() && !released)
            NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(false, -1));

        endScene();

        phase = Phase.NONE;
    }

    private static void echo() {
        if (MC.player == null || MC.level == null)
            return;

        ChromaticAberrationManager.add(MC.level, ChromaticAberration.builder(MC.player)
                .radius(Integer.MAX_VALUE)
                .strength(0.045F)
                .duration(32)
                .fadeInTime(8)
                .fadeOutTime(20)
                .build());
    }

    private static void setPhase(Phase value) {
        phase = value;
        phaseStart = System.nanoTime();
    }

    private static void beginScene() {
        scene = true;
        released = false;
        radius = AstralSatchelItem.getRadius(MC.player);
        camera = Vec3.ZERO;
        previousCamera = Vec3.ZERO;
        velocity = Vec3.ZERO;
        previousVelocity = Vec3.ZERO;
        sentCamera = Vec3.ZERO;
        sentAim = null;
        labelStack = ItemStack.EMPTY;
        labelStrength = 0F;

        AstralMotion.begin(getEntries());
        AstralFX.init();
    }

    private static void endScene() {
        scene = false;

        if (MC.player != null)
            AstralSatchelItem.setSlot(MC.player, -1);

        AstralMotion.reset();
        AstralFX.reset();
        AstralSceneBuffer.release();
    }

    public static void markUse() {
        lastUseAt = System.nanoTime();
    }

    public static boolean canExit() {
        return System.nanoTime() - lastUseAt > 450_000_000L;
    }

    public static void reset() {
        phase = Phase.NONE;

        endScene();
    }

    public static float getTime() {
        return (System.nanoTime() - CLOCK_START) / 1.0E9F;
    }

    public static float getCoverage() {
        var progress = ease((System.nanoTime() - phaseStart) / 1.0E9F / TRANSITION_DURATION);

        return switch (phase) {
            case NONE -> 0F;
            case ENTER -> progress;
            case EXIT -> 1F - progress;
        };
    }

    private static float ease(float value) {
        var t = Mth.clamp(value, 0F, 1F);

        return t * 0.5F + (t * t * (3F - 2F * t)) * 0.5F;
    }

    public static float getSpeed(float partialTick) {
        return Mth.lerp(partialTick, (float) previousVelocity.length(), (float) velocity.length());
    }

    public static Vec3 getCamera(float partialTick) {
        return previousCamera.lerp(camera, partialTick);
    }

    public static void setLabel(ItemStack stack, float x, float y, float radius, float strength) {
        labelStack = stack;
        labelRadius = radius;
        labelX = x;
        labelY = y;
        labelStrength = strength;
    }

    public static void fadeLabel(float amount) {
        labelStrength = Math.max(0F, labelStrength - amount);
    }

    public static List<AstralSatchelItem.Entry> getEntries() {
        var player = MC.player;

        if (player == null)
            return List.of();

        return AstralSatchelItem.getEntries(AstralSatchelItem.find(player));
    }

    public static void tick() {
        var player = MC.player;

        if (player == null || AstralSatchelItem.find(player).isEmpty()) {
            hardExit();

            return;
        }

        radius = AstralSatchelItem.getRadius(player);

        var elapsed = (System.nanoTime() - phaseStart) / 1.0E9F;

        if (phase == Phase.ENTER && elapsed >= TRANSITION_DURATION) {
            setPhase(Phase.NONE);
        } else if (phase == Phase.EXIT && elapsed >= TRANSITION_DURATION) {
            endScene();

            setPhase(Phase.NONE);
        }

        if (!scene)
            return;

        previousCamera = camera;

        var look = player.getViewVector(1F);
        var yaw = Math.toRadians(player.getYRot());
        var right = new Vec3(-Math.cos(yaw), 0D, -Math.sin(yaw));
        var options = MC.options;
        var wish = Vec3.ZERO;

        if (MC.screen == null && phase != Phase.EXIT) {
            if (options.keyUp.isDown())
                wish = wish.add(look);

            if (options.keyDown.isDown())
                wish = wish.subtract(look);

            if (options.keyRight.isDown())
                wish = wish.add(right);

            if (options.keyLeft.isDown())
                wish = wish.subtract(right);

            if (options.keyJump.isDown())
                wish = wish.add(0D, 1D, 0D);

            if (options.keyShift.isDown())
                wish = wish.subtract(0D, 1D, 0D);
        }

        var speed = options.keySprint.isDown() ? 0.09D : 0.045D;

        previousVelocity = velocity;
        velocity = velocity.scale(0.82D);

        if (wish.lengthSqr() > 1.0E-6D)
            velocity = velocity.add(wish.normalize().scale(speed));

        camera = AstralSatchelItem.clamp(camera.add(velocity), radius);

        var entries = getEntries();

        AstralMotion.tick(entries);
        AstralFX.tick();

        if (phase == Phase.EXIT)
            return;

        if (player.tickCount % 2 == 0 && !camera.equals(sentCamera)) {
            sentCamera = camera;

            NetworkHandler.sendToServer(new C2SSyncAstralSatchelCamera(camera.toVector3f()));
        }

        var aimed = pick(entries, 1F);
        var aimedId = aimed < 0 ? null : entries.get(aimed).id();

        if (!Objects.equals(aimedId, sentAim)) {
            sentAim = aimedId;

            NetworkHandler.sendToServer(new C2SSetAstralSatchelAim(Optional.ofNullable(aimedId)));
        }
    }

    private static void push() {
        var player = MC.player;
        var entries = getEntries();
        var aimed = pick(entries, 1F);

        if (player == null || aimed < 0)
            return;

        var entry = entries.get(aimed);

        AstralMotion.pulse(entry.id(), AstralMotion.PULSE_SHRINK);

        markUse();

        NetworkHandler.sendToServer(new C2SPushAstralSatchelItem(entry.id(), player.getViewVector(1F).toVector3f()));
    }

    public static int pick(List<AstralSatchelItem.Entry> entries, float partialTick) {
        var player = MC.player;

        if (player == null)
            return -1;

        var origin = getCamera(partialTick);
        var look = player.getViewVector(1F);
        var reach = player.entityInteractionRange() * AstralSatchelItem.REACH_MULTIPLIER;
        var best = -1;
        var bestDistance = Double.MAX_VALUE;

        for (var i = 0; i < entries.size(); i++) {
            var offset = AstralMotion.getPosition(entries.get(i), partialTick).subtract(origin);
            var along = offset.dot(look);

            if (along <= 0.2D || along > reach)
                continue;

            var miss = offset.subtract(look.scale(along)).length();

            if (miss <= HIT_RADIUS && along < bestDistance) {
                best = i;
                bestDistance = along;
            }
        }

        return best;
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onClientTickPost(ClientTickEvent.Post event) {
            if (AstralSpace.isEngaged())
                AstralSpace.tick();
        }

        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            if (!AstralSpace.isLocked())
                return;

            var input = event.getInput();

            input.leftImpulse = 0F;
            input.forwardImpulse = 0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }

        @SubscribeEvent
        public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
            if (!AstralSpace.isLocked() || MC.player == null)
                return;

            if (AstralSpace.isActive() && event.isUseItem()) {
                var entries = AstralSpace.getEntries();
                var aimed = AstralSpace.pick(entries, 1F);
                var holdsSatchel = MC.player.getItemInHand(event.getHand()).getItem() instanceof AstralSatchelItem;

                if (aimed < 0 && !holdsSatchel)
                    return;

                event.setSwingHand(false);
                event.setCanceled(true);

                if (aimed >= 0) {
                    AstralSpace.markUse();

                    NetworkHandler.sendToServer(new C2SWithdrawAstralSatchelItem(entries.get(aimed).id()));
                } else if (AstralSpace.canExit() && AstralSatchelItem.isLocked(MC.player, AstralSatchelItem.getSlot(MC.player, event.getHand()))) {
                    AstralSpace.exit();
                }

                return;
            }

            var pushing = AstralSpace.isActive() && event.isAttack();

            event.setSwingHand(pushing);
            event.setCanceled(true);

            if (pushing)
                AstralSpace.push();
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            AstralSpace.reset();
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            AstralTooltip.render(event.getGuiGraphics());
        }
    }
}
