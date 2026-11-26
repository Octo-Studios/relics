package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.general.particles.FlyingParticleData;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RankPointsPlateWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.handlers.RankHandler;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.C2SDismantleRelic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DismantleEffect {
    public static final int REQUIRED_HITS = 5;
    private static final int BURST_TICKS = 15;
    private static final int POINT_LAUNCH_TICK = 2;
    private static final int POINT_ARRIVAL_TICK = 16;
    private static final int MAX_TRAILS = 20;
    private static int nextRequestId;

    private final RelicDescriptionScreen screen;
    private final FracturedRelicRenderer renderer = new FracturedRelicRenderer();
    private final List<FlyingParticleData> pointTrails = new ArrayList<>();
    private int pendingRequestId;
    private ItemStack snapshot = ItemStack.EMPTY;
    private int rank;
    private long pointsBeforeStrike;
    private int hits;
    private int age;
    private int finishAge = -1;
    private float stress;
    private float previousStress;

    public DismantleEffect(RelicDescriptionScreen screen) {
        this.screen = screen;
    }

    public int hits() {
        return isCommitted() ? hits : StrikeProgress.remainingHits(hits, age);
    }

    public boolean isFinishing() {
        return finishAge >= 0;
    }

    public boolean isFinished() {
        return finishAge >= POINT_ARRIVAL_TICK + FloatingTexts.DURATION;
    }

    public boolean isCommitted() {
        return pendingRequestId != 0 || isFinishing();
    }

    public void strike(ItemStack stack, int rank) {
        snapshot = stack.copy();
        this.rank = rank;

        var nextHit = hits() + 1;

        if (nextHit == REQUIRED_HITS) {
            pointsBeforeStrike = RankHandler.getRankPoints(Minecraft.getInstance().player, stack.getItem());
            pendingRequestId = ++nextRequestId;

            NetworkHandler.sendToServer(new C2SDismantleRelic(screen.getContainer(), screen.getSlot(), stack.getItem(), stack.getCount(), rank, pendingRequestId));
        } else
            playStrike(nextHit);
    }

    public long displayedPoints(long points) {
        return isCommitted() && finishAge < POINT_ARRIVAL_TICK ? pointsBeforeStrike : points;
    }

    public void confirm(int requestId, boolean dismantled) {
        if (pendingRequestId != requestId)
            return;

        pendingRequestId = 0;

        if (dismantled)
            playStrike(REQUIRED_HITS);
        else
            reset();
    }

    private void playStrike(int hits) {
        this.hits = hits;
        age = 0;

        var minecraft = Minecraft.getInstance();
        var sounds = minecraft.getSoundManager();
        var random = minecraft.player.getRandom();
        var tint = BigRelicCardWidget.getItemColor(snapshot);
        var x = screen.x + 84F;
        var y = screen.y + 88F;
        var shattered = hits == REQUIRED_HITS;

        screen.getRankupEffect().start(1.25F * hits / REQUIRED_HITS);
        LevelUpEffect.crumbleAt(screen, random, x, y, tint, 2 * hits);
        LevelUpEffect.burstAt(screen, random, x, y, tint, shattered ? 4 * hits : hits);

        if (shattered) {
            finishAge = 0;
            startPointTrails(x, y, random);
            sounds.play(SimpleSoundInstance.forUI(SoundEvents.DEEPSLATE_BREAK, 0.65F, 0.5F));
            sounds.play(SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, 0.82F, 0.8F));
            sounds.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.3F, 0.8F));
            LevelUpEffect.burstAt(screen, random, x, y, 0xFFE6BC, 16);
        } else
            sounds.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 0.7F + 0.16F * hits, 0.4F + 0.08F * hits));
    }

    public void tick() {
        if (snapshot.isEmpty())
            return;

        age++;
        previousStress = stress;
        stress = Mth.approach(stress, (float) hits() * FracturedRelicRenderer.CRACK_COUNT / REQUIRED_HITS, 0.32F);

        if (isFinishing()) {
            finishAge++;

            if (finishAge == POINT_ARRIVAL_TICK)
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.25F, 0.65F));
        } else if (previousStress == 0F && stress == 0F && hits() == 0)
            reset();
    }

    public ItemStack displayedStack(ItemStack live) {
        if (isCommitted())
            return snapshot;

        if (!snapshot.isEmpty() && (!live.is(snapshot.getItem()) || live.getCount() != snapshot.getCount()
                || ((IRelicItem) live.getItem()).getRelicData(Minecraft.getInstance().player, live).getLevelingData().getRank() != rank))
            reset();

        return live;
    }

    private float impact(float partialTick) {
        return Math.max(0F, 1F - (age + partialTick) / 6F) * hits / REQUIRED_HITS;
    }

    public float backgroundFlash(float partialTick) {
        return Minecraft.getInstance().options.hideLightningFlash().get() ? 0F : impact(partialTick);
    }

    public boolean renderItem(GuiGraphics graphics, ItemStack stack, float partialTick) {
        if (snapshot.isEmpty() || !isFinishing() && previousStress == 0F && stress == 0F && hits == 0)
            return false;

        var time = isFinishing() ? finishAge + partialTick : 0F;
        if (time >= BURST_TICKS)
            return true;

        var pressure = Mth.lerp(partialTick, previousStress, stress);
        return renderer.render(graphics, stack, pressure, impact(partialTick), time / BURST_TICKS) || finishAge >= POINT_LAUNCH_TICK;
    }

    private void startPointTrails(float x, float y, RandomSource random) {
        for (var child : screen.children()) {
            if (!(child instanceof RankPointsPlateWidget plate))
                continue;

            var targetX = plate.getX() + 11F;
            var targetY = plate.getY() + 11F;
            var points = RankHandler.getRankPoints(Minecraft.getInstance().player, snapshot.getItem()) - pointsBeforeStrike;
            var color = new Color(UIParticles.GOLD);

            for (var i = 0; i < Math.min(points, MAX_TRAILS); i++) {
                var controlX = (x + targetX) / 2F + (random.nextFloat() - 0.5F) * 70F;
                var controlY = Math.min(y, targetY) - 10F - random.nextFloat() * 40F;
                var trail = new FlyingParticleData(color, x, y, targetX, targetY, controlX, controlY,
                        POINT_LAUNCH_TICK, POINT_ARRIVAL_TICK - POINT_LAUNCH_TICK, 1.6F, random.nextInt());

                pointTrails.add(trail);
                ParticleStorage.addParticle(screen, trail);
            }
            break;
        }
    }

    public void reset() {
        if (!pointTrails.isEmpty()) {
            ParticleStorage.getParticles(screen).removeAll(pointTrails);
            pointTrails.clear();
        }

        pendingRequestId = 0;
        snapshot = ItemStack.EMPTY;
        hits = 0;
        previousStress = stress = 0F;
        finishAge = -1;
        renderer.close();
    }
}
