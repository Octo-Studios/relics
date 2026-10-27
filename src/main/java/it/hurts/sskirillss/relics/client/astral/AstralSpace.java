package it.hurts.sskirillss.relics.client.astral;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberration;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSetAstralSatchelActive;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstralSpace {
    public static final float HIT_RADIUS = 0.75F;
    public static final float BIRTH_DURATION = 0.6F;
    public static final float FLY_DURATION = 0.5F;
    public static final float WAVE_DURATION = 2.4F;
    public static final float TRANSITION_DURATION = 1.6F;

    private static final Minecraft MC = Minecraft.getInstance();
    private static final long CLOCK_START = System.nanoTime();
    private static final Map<Vec3, Long> BIRTHS = new HashMap<>();
    private static final Map<Vec3, Vec3> BIRTH_STARTS = new HashMap<>();
    private static final List<Wave> WAVES = new ArrayList<>();
    private static final List<Flyer> FLYERS = new ArrayList<>();

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
    private static List<AstralSatchelItem.Entry> lastEntries = List.of();

    @Getter
    private static ItemStack labelStack = ItemStack.EMPTY;
    @Getter
    private static float labelX;
    @Getter
    private static float labelY;
    @Getter
    private static float labelStrength;

    private enum Phase {
        NONE, ENTER, EXIT
    }

    public record Wave(Vec3 origin, long born, int color) {
    }

    public record Flyer(ItemStack stack, Vec3 start, long born) {
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

    public static void enter() {
        var player = MC.player;

        if (player == null || isEngaged())
            return;

        beginScene();

        setPhase(Phase.ENTER);

        lastUseAt = phaseStart;

        echo();

        NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(true));
    }

    public static void exit() {
        if (!scene || phase != Phase.NONE)
            return;

        setPhase(Phase.EXIT);

        released = true;

        echo();

        NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(false));
    }

    private static void hardExit() {
        if (MC.player != null && isEngaged() && !released)
            NetworkHandler.sendToServer(new C2SSetAstralSatchelActive(false));

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
        camera = Vec3.ZERO;
        previousCamera = Vec3.ZERO;
        velocity = Vec3.ZERO;
        previousVelocity = Vec3.ZERO;
        sentCamera = Vec3.ZERO;
        lastEntries = List.of();
        labelStack = ItemStack.EMPTY;
        labelStrength = 0F;

        BIRTHS.clear();
        BIRTH_STARTS.clear();
        WAVES.clear();
        FLYERS.clear();

        for (var entry : getEntries())
            BIRTHS.put(entry.position(), -1L);

        AstralFX.init();
    }

    private static void endScene() {
        scene = false;

        BIRTHS.clear();
        BIRTH_STARTS.clear();
        WAVES.clear();
        FLYERS.clear();

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

    public static List<Wave> getWaves() {
        return WAVES;
    }

    public static List<Flyer> getFlyers() {
        return FLYERS;
    }

    public static void setLabel(ItemStack stack, float x, float y, float strength) {
        labelStack = stack;
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

        camera = AstralSatchelItem.clamp(camera.add(velocity));

        var entries = getEntries();
        var now = System.nanoTime();

        BIRTHS.keySet().removeIf(position -> entries.stream().noneMatch(entry -> entry.position().equals(position)));
        BIRTH_STARTS.keySet().removeIf(position -> !BIRTHS.containsKey(position));

        for (var entry : entries) {
            if (BIRTHS.putIfAbsent(entry.position(), now) == null) {
                var tint = AstralFX.getTint(entry.stack());

                BIRTH_STARTS.put(entry.position(), getCamera(1F).add(look.scale(0.8D)).add(0D, -0.45D, 0D));

                WAVES.add(new Wave(entry.position(), now, tint));
            }
        }

        for (var previous : lastEntries) {
            if (entries.stream().noneMatch(entry -> entry.position().equals(previous.position()))) {
                var tint = AstralFX.getTint(previous.stack());

                WAVES.add(new Wave(previous.position(), now, tint));
                FLYERS.add(new Flyer(previous.stack(), previous.position(), now));
            }
        }

        WAVES.removeIf(wave -> (now - wave.born()) / 1.0E9F > WAVE_DURATION);
        FLYERS.removeIf(flyer -> (now - flyer.born()) / 1.0E9F > FLY_DURATION);

        lastEntries = entries;

        AstralFX.tick();

        if (phase != Phase.EXIT && MC.player.tickCount % 2 == 0 && !camera.equals(sentCamera)) {
            sentCamera = camera;

            NetworkHandler.sendToServer(new C2SSyncAstralSatchelCamera(camera.toVector3f()));
        }
    }

    public static float getBirth(AstralSatchelItem.Entry entry) {
        var born = BIRTHS.get(entry.position());

        if (born == null || born < 0L)
            return 1F;

        var progress = Mth.clamp((System.nanoTime() - born) / 1.0E9F / BIRTH_DURATION, 0F, 1F);

        return 1F - (1F - progress) * (1F - progress) * (1F - progress);
    }

    public static Vec3 getPosition(AstralSatchelItem.Entry entry, float partialTick) {
        var position = entry.position();
        var phase = (float) (position.x * 12.9898D + position.y * 78.233D + position.z * 37.719D);
        var time = getTime();
        var drift = new Vec3(
                Mth.sin(time * 0.7F + phase) * 0.25D,
                Mth.sin(time * 0.9F + phase * 1.3F) * 0.2D,
                Mth.cos(time * 0.6F + phase * 0.7F) * 0.25D
        );

        var settled = position.add(drift);
        var birth = getBirth(entry);

        if (birth >= 1F)
            return settled;

        var start = BIRTH_STARTS.get(position);

        if (start == null)
            start = getCamera(partialTick).add(MC.player.getViewVector(1F).scale(1.2D));

        return start.lerp(settled, birth).add(0D, Math.sin(Math.PI * birth) * 0.55D, 0D);
    }

    public static float getFlyerProgress(Flyer flyer) {
        var progress = Mth.clamp((System.nanoTime() - flyer.born()) / 1.0E9F / FLY_DURATION, 0F, 1F);

        return progress * progress * (3F - 2F * progress);
    }

    public static Vec3 getFlyerPosition(Flyer flyer, float partialTick) {
        var player = MC.player;

        if (player == null)
            return flyer.start();

        var t = getFlyerProgress(flyer);
        var target = getCamera(partialTick).add(player.getViewVector(1F).scale(0.7D)).add(0D, -0.55D, 0D);
        var control = flyer.start().lerp(target, 0.5D).add(0D, 1.2D, 0D);
        var inverse = 1D - t;

        return flyer.start().scale(inverse * inverse).add(control.scale(2D * inverse * t)).add(target.scale((double) t * t));
    }

    public static int pick(List<AstralSatchelItem.Entry> entries, float partialTick) {
        var player = MC.player;

        if (player == null)
            return -1;

        var origin = getCamera(partialTick);
        var look = player.getViewVector(1F);
        var best = -1;
        var bestDistance = Double.MAX_VALUE;

        for (var i = 0; i < entries.size(); i++) {
            var offset = getPosition(entries.get(i), partialTick).subtract(origin);
            var along = offset.dot(look);

            if (along <= 0.2D || along > AstralSatchelItem.RADIUS * 2D)
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

            event.setSwingHand(false);
            event.setCanceled(true);

            if (!event.isUseItem() || !AstralSpace.isActive())
                return;

            var aimed = AstralSpace.pick(AstralSpace.getEntries(), 1F);

            if (aimed >= 0) {
                AstralSpace.markUse();

                NetworkHandler.sendToServer(new C2SWithdrawAstralSatchelItem(aimed));

                return;
            }

            var holdsSatchel = MC.player.getMainHandItem().getItem() instanceof AstralSatchelItem || MC.player.getOffhandItem().getItem() instanceof AstralSatchelItem;

            if (holdsSatchel && AstralSpace.canExit())
                AstralSpace.exit();
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
