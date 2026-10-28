package it.hurts.sskirillss.relics.client.renderer.sky;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.research.StarData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@EventBusSubscriber(value = Dist.CLIENT)
public class MidnightMantleSkyRenderer {
    private static final List<ResourceLocation> STAR_TEXTURES = IntStream.range(0, 6)
            .mapToObj(index -> ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_" + index + ".png"))
            .toList();
    private static final ResourceLocation CONSTELLATION_STAR_TEXTURE = STAR_TEXTURES.get(5);

    private static final Minecraft MC = Minecraft.getInstance();

    private static final Tone NEW_MOON_TINT = new Tone(0.10F, 0.42F, 0.92F);
    private static final Tone FULL_MOON_TINT = new Tone(0.42F, 0.12F, 0.82F);
    private static final StarColor NEW_MOON_STARS = new StarColor(new Tone(0.30F, 0.78F, 1F), new Tone(0.62F, 0.90F, 1F));
    private static final StarColor FULL_MOON_STARS = new StarColor(new Tone(0.82F, 0.42F, 1F), new Tone(1F, 0.62F, 1F));

    private static final float SKY_RADIUS = 100F;
    private static final float FADE_RATE_PER_TICK = 0.21F;
    private static final float TINT_ALPHA = 0.11F;

    private static final int SKY_DOME_STACKS = 16;
    private static final int SKY_DOME_SLICES = 36;
    private static final float SKY_DOME_MIN_ELEVATION = -0.35F;

    private static final int CONSTELLATION_SLOT_COUNT = 10;
    private static final int CONSTELLATION_SLOT_POSITIONS = 4;
    private static final int CONSTELLATION_SLOT_POOL_SIZE = CONSTELLATION_SLOT_COUNT * CONSTELLATION_SLOT_POSITIONS;
    private static final float CONSTELLATION_SLOT_GOLDEN_ANGLE = 2.3999631F;
    private static final double CONSTELLATION_SWITCH_TICKS = 200D;
    private static final double CONSTELLATION_FADE_TICKS = 60D;
    private static final double CONSTELLATION_ERASE_TICKS = 100D;
    private static final float CONSTELLATION_DRAW_TICKS = 160F;
    private static final float CONSTELLATION_REVEAL_SPAN = 0.35F;
    private static final float CONSTELLATION_SKY_SCALE = 0.15F;
    private static final float CONSTELLATION_STAR_SPIN = 0.006F;
    private static final int CONSTELLATION_SHAPE_CACHE_LIMIT = 64;

    private static final MidnightMantleSkyRenderer INSTANCE = new MidnightMantleSkyRenderer();

    private final Map<Long, ConstellationShape> constellationShapes = new HashMap<>();

    private List<StarLayer> starLayers;
    private List<Constellation> constellations;
    private int constellationStride;

    private Tone currentTint = new Tone(0F, 0F, 0F);
    private float currentTintAlpha;
    private float currentStarAlpha;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY || MC.player == null || MC.level == null)
            return;

        var stacks = EntityUtils.findEquippedCurios(MC.player, RelicsItems.MIDNIGHT_MANTLE.get());

        INSTANCE.render(event, stacks);
    }

    public static boolean shouldSuppressVanillaStars() {
        if (MC.player == null || MC.level == null || !hasNormalSky())
            return false;

        return !EntityUtils.findEquippedCurios(MC.player, RelicsItems.MIDNIGHT_MANTLE.get()).isEmpty()
                || INSTANCE.currentStarAlpha > 0.003F;
    }

    private static boolean hasNormalSky() {
        return MC.level != null
                && MC.level.dimensionType().hasSkyLight()
                && MC.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL;
    }

    private void initialise() {
        if (starLayers != null)
            return;

        starLayers = List.of(
                new StarLayer(makeStars(0.5F, 1.3F, 90, 77221), STAR_TEXTURES.get(0), 0.024F, true, 0.95F),
                new StarLayer(makeStars(0.5F, 1.5F, 900, 41315), STAR_TEXTURES.get(1), 0.020F, false, 1F),
                new StarLayer(makeStars(0.5F, 1.6F, 760, 35151), STAR_TEXTURES.get(2), -0.017F, true, 0.90F),
                new StarLayer(makeStars(0.7F, 2.0F, 540, 61354), STAR_TEXTURES.get(3), 0.013F, false, 0.78F),
                new StarLayer(makeStars(0.9F, 2.3F, 1240, 61355), STAR_TEXTURES.get(4), -0.011F, true, 0.42F),
                new StarLayer(makeStars(0.9F, 2.3F, 90, 61356), STAR_TEXTURES.get(5), 0.009F, false, 0.42F)
        );

        constellations = collectConstellations();
        constellationStride = findStride(constellations.size());
    }

    public void render(RenderLevelStageEvent event, List<ItemStack> stacks) {
        initialise();

        var matrices = new PoseStack();

        matrices.mulPose(event.getModelViewMatrix());

        var deltaTracker = event.getPartialTick();
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var renderTime = MC.level.getGameTime() + (double) partialTick;
        var skyAngle = MC.level.getTimeOfDay(partialTick) * 360F;
        var fadeSpeed = 1F - (float) Math.pow(1F - FADE_RATE_PER_TICK, deltaTracker.getRealtimeDeltaTicks());

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        var skyOcclusion = hasNormalSky() ? getSkyOcclusion(event.getCamera()) : 0F;
        var visibility = getVisibility(partialTick) * skyOcclusion;
        var equipped = !stacks.isEmpty();
        var newMoon = equipped && isNewMoon(stacks.getFirst());
        var targetTint = !equipped ? currentTint : newMoon ? NEW_MOON_TINT : FULL_MOON_TINT;

        updateTint(targetTint, equipped ? visibility * TINT_ALPHA : 0F, fadeSpeed);
        updateStarAlpha(equipped ? skyOcclusion : 0F, fadeSpeed);

        if (currentTintAlpha > 0.003F)
            renderSkyDome(matrices, renderTime, currentTint, currentTintAlpha);

        if (currentStarAlpha > 0.003F && equipped && visibility > 0)
            renderStars(matrices, renderTime, skyAngle, newMoon ? NEW_MOON_STARS : FULL_MOON_STARS, visibility * currentStarAlpha);

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    private void updateTint(Tone target, float targetAlpha, float speed) {
        currentTint = currentTint.lerp(target, speed);
        currentTintAlpha = Mth.lerp(speed, currentTintAlpha, targetAlpha);
    }

    private void updateStarAlpha(float targetAlpha, float speed) {
        currentStarAlpha = Mth.lerp(speed, currentStarAlpha, targetAlpha);

        if (targetAlpha == 0F && currentStarAlpha < 0.003F)
            currentStarAlpha = 0F;
        else if (targetAlpha == 1F && currentStarAlpha > 0.997F)
            currentStarAlpha = 1F;
    }

    private float getVisibility(float partialTick) {
        var night = Mth.clamp((1F - MC.level.getSkyDarken(partialTick)) * 1.65F, 0F, 1F);
        var weather = 1F - MC.level.getRainLevel(partialTick) * 0.65F;

        return Mth.clamp((MC.level.dimensionType().hasSkyLight() ? night : 1F) * weather, 0F, 1F);
    }

    private float getSkyOcclusion(Camera camera) {
        var fogType = camera.getFluidInCamera();

        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW || fogType == FogType.WATER)
            return 0F;

        if (camera.getEntity() instanceof LivingEntity entity
                && (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS)))
            return 0F;

        return 1F;
    }

    private boolean isNewMoon(ItemStack stack) {
        return "new_moon".equals(((MidnightMantleItem) stack.getItem())
                .getRelicData(MC.player, stack)
                .getAbilitiesData()
                .getAbilityData("phase")
                .getMode());
    }

    private void renderSkyDome(PoseStack matrices, double renderTime, Tone tone, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        var matrix = matrices.last().pose();
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var stack = 0; stack < SKY_DOME_STACKS; stack++) {
            var lowerElevation = getDomeElevation(stack);
            var upperElevation = getDomeElevation(stack + 1);

            for (var slice = 0; slice < SKY_DOME_SLICES; slice++) {
                var leftAzimuth = slice * Mth.TWO_PI / SKY_DOME_SLICES;
                var rightAzimuth = (slice + 1) * Mth.TWO_PI / SKY_DOME_SLICES;

                addDomeVertex(buffer, matrix, lowerElevation, leftAzimuth, renderTime, tone, alpha);
                addDomeVertex(buffer, matrix, lowerElevation, rightAzimuth, renderTime, tone, alpha);
                addDomeVertex(buffer, matrix, upperElevation, rightAzimuth, renderTime, tone, alpha);
                addDomeVertex(buffer, matrix, upperElevation, leftAzimuth, renderTime, tone, alpha);
            }
        }

        draw(buffer);

        RenderSystem.enableCull();
    }

    private float getDomeElevation(int stack) {
        return Mth.lerp(stack / (float) SKY_DOME_STACKS, SKY_DOME_MIN_ELEVATION, Mth.HALF_PI);
    }

    private void addDomeVertex(BufferBuilder buffer, Matrix4f matrix, float elevation, float azimuth, double renderTime, Tone tone, float alpha) {
        var height = Mth.sin(elevation);
        var horizontal = Mth.cos(elevation);
        var glow = (float) Math.exp(-Math.max(height, 0F) * 3.2F);
        var drift = 0.85F + 0.15F * wave(renderTime, 0.01F, azimuth * 3F);
        var vertexAlpha = Mth.clamp(alpha * (0.55F + 2.1F * glow) * drift, 0F, 0.6F);

        buffer.addVertex(matrix, horizontal * Mth.cos(azimuth) * SKY_RADIUS, height * SKY_RADIUS, horizontal * Mth.sin(azimuth) * SKY_RADIUS)
                .setColor(
                        Mth.lerp(glow, tone.red() * 0.6F, tone.red() + (1F - tone.red()) * 0.3F),
                        Mth.lerp(glow, tone.green() * 0.6F, tone.green() + (1F - tone.green()) * 0.3F),
                        Mth.lerp(glow, tone.blue() * 0.6F, tone.blue() + (1F - tone.blue()) * 0.3F),
                        vertexAlpha);
    }

    private void renderStars(PoseStack matrices, double renderTime, float skyAngle, StarColor color, float alpha) {
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(-90F));
        matrices.mulPose(Axis.XP.rotationDegrees(skyAngle));

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        var matrix = matrices.last().pose();

        for (var layer : starLayers) {
            var tone = layer.secondary() ? color.secondary() : color.primary();
            var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            RenderSystem.setShaderTexture(0, layer.texture());

            for (var star : layer.stars())
                addStar(buffer, matrix, star, renderTime, layer.spin(), tone, alpha * layer.alpha());

            draw(buffer);
        }

        renderConstellations(matrices, renderTime, color, alpha);

        matrices.popPose();
    }

    private void renderConstellations(PoseStack matrices, double renderTime, StarColor color, float alpha) {
        if (constellations.isEmpty())
            return;

        var switchStep = (long) Math.floor(renderTime / CONSTELLATION_SWITCH_TICKS);
        var activeSlot = (int) (switchStep % CONSTELLATION_SLOT_COUNT);
        var round = switchStep / CONSTELLATION_SLOT_COUNT;
        var switchProgress = renderTime - switchStep * CONSTELLATION_SWITCH_TICKS;
        var fadeInStart = CONSTELLATION_SWITCH_TICKS - CONSTELLATION_FADE_TICKS;

        for (var slotIndex = 0; slotIndex < CONSTELLATION_SLOT_COUNT; slotIndex++) {
            var generation = round + (slotIndex < activeSlot ? 1L : 0L);
            var constellationAlpha = alpha;
            var erase = 0F;
            var age = Float.MAX_VALUE;

            if (slotIndex != activeSlot) {
                var lastStep = switchStep - Math.floorMod(switchStep - slotIndex, (long) CONSTELLATION_SLOT_COUNT);

                age = (float) (renderTime - (lastStep * CONSTELLATION_SWITCH_TICKS + fadeInStart));
            } else if (switchProgress < CONSTELLATION_ERASE_TICKS) {
                erase = (float) (switchProgress / CONSTELLATION_ERASE_TICKS);
            } else if (switchProgress > fadeInStart) {
                generation++;
                age = (float) (switchProgress - fadeInStart);
                constellationAlpha = alpha * (float) ((switchProgress - fadeInStart) / CONSTELLATION_FADE_TICKS);
            } else
                continue;

            if (constellationAlpha > 0.003F)
                renderConstellation(matrices, getConstellationShape(slotIndex, generation), renderTime, age, erase, color, constellationAlpha);
        }
    }

    private void renderConstellation(PoseStack matrices, ConstellationShape shape, double renderTime, float age, float erase, StarColor color, float alpha) {
        var matrix = matrices.last().pose();
        var lineCount = shape.lines().size();
        var drawProgress = Mth.clamp(age / CONSTELLATION_DRAW_TICKS, 0F, 1F) * lineCount;
        var eraseProgress = erase * lineCount;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        var lineBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var i = 0; i < lineCount; i++) {
            var revealed = Mth.clamp(drawProgress - i, 0F, 1F);
            var erased = Mth.clamp(eraseProgress - i, 0F, 1F);

            if (revealed > 0F && erased < 1F)
                addConstellationLine(lineBuffer, matrix, shape.lines().get(i), erased, revealed, renderTime, color, alpha * 0.95F);
        }

        draw(lineBuffer);

        RenderSystem.enableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CONSTELLATION_STAR_TEXTURE);

        var starBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (var i = 0; i < shape.stars().size(); i++) {
            var appeared = Mth.clamp((drawProgress - shape.arrivals()[i]) / CONSTELLATION_REVEAL_SPAN + 1F, 0F, 1F);
            var remaining = Mth.clamp((shape.departures()[i] - eraseProgress) / CONSTELLATION_REVEAL_SPAN, 0F, 1F);
            var reveal = Math.min(appeared, remaining);

            if (reveal > 0F)
                addStar(starBuffer, matrix, shape.stars().get(i), renderTime, CONSTELLATION_STAR_SPIN, color.secondary(), alpha * reveal);
        }

        draw(starBuffer);
    }

    private ConstellationShape getConstellationShape(int slotIndex, long generation) {
        var key = generation * CONSTELLATION_SLOT_COUNT + slotIndex;
        var shape = constellationShapes.get(key);

        if (shape == null) {
            if (constellationShapes.size() >= CONSTELLATION_SHAPE_CACHE_LIMIT)
                constellationShapes.clear();

            shape = buildConstellationShape(constellations.get((int) Math.floorMod(key * constellationStride, (long) constellations.size())), slotIndex, generation);

            constellationShapes.put(key, shape);
        }

        return shape;
    }

    private ConstellationShape buildConstellationShape(Constellation constellation, int slotIndex, long generation) {
        var poolIndex = slotIndex * CONSTELLATION_SLOT_POSITIONS + (int) Math.floorMod(generation, (long) CONSTELLATION_SLOT_POSITIONS);
        var normalY = Mth.lerp((poolIndex + 0.5F) / CONSTELLATION_SLOT_POOL_SIZE, -0.85F, 0.85F);
        var horizontal = Mth.sqrt(1F - normalY * normalY);
        var angle = poolIndex * CONSTELLATION_SLOT_GOLDEN_ANGLE;
        var normalX = Mth.cos(angle) * horizontal;
        var normalZ = Mth.sin(angle) * horizontal;

        var patternOffset = new LegacyRandomSource(53791L + slotIndex * 104729L + generation * 130363L).nextInt(10000);
        var random = new LegacyRandomSource(constellation.seed() * 31L + patternOffset);

        var tangentLength = Mth.sqrt(normalX * normalX + normalZ * normalZ);
        var tangentX = tangentLength < 0.001F ? 1F : normalZ / tangentLength;
        var tangentZ = tangentLength < 0.001F ? 0F : -normalX / tangentLength;

        var bitangentX = normalY * tangentZ;
        var bitangentY = normalZ * tangentX - normalX * tangentZ;
        var bitangentZ = -normalY * tangentX;

        var stars = new ArrayList<Star>();

        for (var point : constellation.points()) {
            var pointX = normalX + (tangentX * point.x() + bitangentX * point.y()) * CONSTELLATION_SKY_SCALE;
            var pointY = normalY + bitangentY * point.y() * CONSTELLATION_SKY_SCALE;
            var pointZ = normalZ + (tangentZ * point.x() + bitangentZ * point.y()) * CONSTELLATION_SKY_SCALE;
            var scale = SKY_RADIUS / Mth.sqrt(pointX * pointX + pointY * pointY + pointZ * pointZ);

            stars.add(createStar(pointX * scale, pointY * scale, pointZ * scale,
                    1.2F + random.nextFloat() * 0.6F, random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, 0.035F + random.nextFloat() * 0.04F));
        }

        var lines = new ArrayList<ConstellationLine>();
        var arrivals = new float[stars.size()];
        var departures = new float[stars.size()];

        Arrays.fill(arrivals, constellation.links().size());

        for (var i = 0; i < constellation.links().size(); i++) {
            var link = constellation.links().get(i);

            lines.add(new ConstellationLine(stars.get(link.from()), stars.get(link.to()), random.nextFloat() * Mth.TWO_PI));

            arrivals[link.from()] = Math.min(arrivals[link.from()], i);
            arrivals[link.to()] = Math.min(arrivals[link.to()], i + 1);
            departures[link.from()] = Math.max(departures[link.from()], i + CONSTELLATION_REVEAL_SPAN);
            departures[link.to()] = Math.max(departures[link.to()], i + 1);
        }

        return new ConstellationShape(stars, lines, arrivals, departures);
    }

    private List<Constellation> collectConstellations() {
        var result = new ArrayList<Constellation>();

        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof IRelicItem relic))
                continue;

            var itemKey = BuiltInRegistries.ITEM.getKey(item).toString();

            for (var abilityEntry : relic.getDefaultRelicTemplate().getAbilities().getAbilities().entrySet()) {
                var research = abilityEntry.getValue().getResearchTemplate();

                if (research.getStars().size() < 3 || research.getLinks().size() < 2)
                    continue;

                var constellation = makeConstellation((itemKey + "/" + abilityEntry.getKey()).hashCode(), research);

                if (constellation != null)
                    result.add(constellation);
            }
        }

        return result;
    }

    private Constellation makeConstellation(int seed, ResearchTemplate research) {
        var sourceStars = research.getStars().values().stream()
                .sorted(Comparator.comparingInt(StarData::getIndex))
                .toList();

        var minX = sourceStars.stream().mapToInt(StarData::getX).min().orElse(0);
        var maxX = sourceStars.stream().mapToInt(StarData::getX).max().orElse(0);
        var minY = sourceStars.stream().mapToInt(StarData::getY).min().orElse(0);
        var maxY = sourceStars.stream().mapToInt(StarData::getY).max().orElse(0);
        var span = Math.max(maxX - minX, maxY - minY);

        if (span <= 0)
            return null;

        var centerX = (minX + maxX) * 0.5F;
        var centerY = (minY + maxY) * 0.5F;
        var indexMap = new HashMap<Integer, Integer>();
        var points = new ArrayList<ConstellationPoint>();

        for (var i = 0; i < sourceStars.size(); i++) {
            var star = sourceStars.get(i);

            indexMap.put(star.getIndex(), i);
            points.add(new ConstellationPoint((star.getX() - centerX) / span * 2F, (centerY - star.getY()) / span * 2F));
        }

        var links = new ArrayList<ConstellationLink>();

        for (var entry : research.getLinks().entries()) {
            var from = indexMap.get(entry.getKey());
            var to = indexMap.get(entry.getValue());

            if (from != null && to != null && !from.equals(to))
                links.add(new ConstellationLink(from, to));
        }

        return links.size() < 2 ? null : new Constellation(seed, points, links);
    }

    private static int findStride(int size) {
        var stride = 7;

        while (gcd(stride, size) != 1)
            stride++;

        return stride;
    }

    private static int gcd(int first, int second) {
        return second == 0 ? first : gcd(second, first % second);
    }

    private List<Star> makeStars(float minSize, float maxSize, int count, long seed) {
        var random = new LegacyRandomSource(seed);
        var stars = new ArrayList<Star>();

        for (var i = 0; i < count; ++i) {
            var posX = random.nextFloat() * 2F - 1F;
            var posY = random.nextFloat() * 2F - 1F;
            var posZ = random.nextFloat() * 2F - 1F;
            var size = Mth.lerp(random.nextFloat(), minSize, maxSize);
            var lengthSquared = posX * posX + posY * posY + posZ * posZ;

            if (lengthSquared < 1F && lengthSquared > 0.001F) {
                var scale = SKY_RADIUS / Mth.sqrt(lengthSquared);

                stars.add(createStar(posX * scale, posY * scale, posZ * scale, size,
                        random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, 0.045F + random.nextFloat() * 0.045F));
            }
        }

        return stars;
    }

    private static Star createStar(float x, float y, float z, float size, float angle, float pulseOffset, float pulseSpeed) {
        var length = Mth.sqrt(x * x + y * y + z * z);
        var unitX = x / length;
        var unitZ = z / length;
        var horizontal = Mth.sqrt(unitX * unitX + unitZ * unitZ);
        var flat = horizontal < 1.0E-5F;

        return new Star(x, y, z, size, angle, pulseOffset, pulseSpeed, flat ? 0F : unitX / horizontal, flat ? 1F : unitZ / horizontal, horizontal, y / length);
    }

    private static float wave(double time, float speed, float offset) {
        return (float) Math.sin(time * speed + offset);
    }

    private static void draw(BufferBuilder buffer) {
        var mesh = buffer.build();

        if (mesh != null)
            BufferUploader.drawWithShader(mesh);
    }

    private void addStar(BufferBuilder buffer, Matrix4f matrix, Star star, double renderTime, float spinSpeed, Tone tone, float alpha) {
        var pulse = 0.5F + 0.5F * wave(renderTime, star.pulseSpeed(), star.pulseOffset());
        var alphaPulse = 0.78F + 0.22F * wave(renderTime, star.pulseSpeed() * 1.17F, star.pulseOffset() + 1.4F);

        var red = Mth.clamp(Mth.lerp(pulse, 1F, tone.red()) * 1.25F, 0F, 1F);
        var green = Mth.clamp(Mth.lerp(pulse, 1F, tone.green()) * 1.25F, 0F, 1F);
        var blue = Mth.clamp(Mth.lerp(pulse, 1F, tone.blue()) * 1.25F, 0F, 1F);
        var vertexAlpha = Mth.clamp(alpha * alphaPulse, 0F, 1F);

        var spin = star.angle() + renderTime * spinSpeed;
        var sinSpin = (float) Math.sin(spin);
        var cosSpin = (float) Math.cos(spin);

        for (var index = 0; index < 4; ++index) {
            var x = ((index & 2) - 1) * star.size();
            var y = ((index + 1 & 2) - 1) * star.size();

            var rotatedX = x * cosSpin - y * sinSpin;
            var rotatedY = y * cosSpin + x * sinSpin;
            var lifted = rotatedX * star.sinPitch();
            var depth = -rotatedX * star.cosPitch();

            buffer.addVertex(matrix,
                            star.x() + depth * star.sinYaw() - rotatedY * star.cosYaw(),
                            star.y() + lifted,
                            star.z() + rotatedY * star.sinYaw() + depth * star.cosYaw())
                    .setUv((index >> 1) & 1, ((index + 1) >> 1) & 1)
                    .setColor(red, green, blue, vertexAlpha);
        }
    }

    private void addConstellationLine(BufferBuilder buffer, Matrix4f matrix, ConstellationLine line, float startDelta, float endDelta, double renderTime, StarColor color, float alpha) {
        var fromX = Mth.lerp(startDelta, line.from().x(), line.to().x());
        var fromY = Mth.lerp(startDelta, line.from().y(), line.to().y());
        var fromZ = Mth.lerp(startDelta, line.from().z(), line.to().z());
        var toX = Mth.lerp(endDelta, line.from().x(), line.to().x());
        var toY = Mth.lerp(endDelta, line.from().y(), line.to().y());
        var toZ = Mth.lerp(endDelta, line.from().z(), line.to().z());

        var dx = toX - fromX;
        var dy = toY - fromY;
        var dz = toZ - fromZ;

        var midX = fromX + toX;
        var midY = fromY + toY;
        var midZ = fromZ + toZ;

        var sideX = dy * midZ - dz * midY;
        var sideY = dz * midX - dx * midZ;
        var sideZ = dx * midY - dy * midX;
        var sideLength = Mth.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);

        if (sideLength < 0.001F)
            return;

        var width = Mth.lerp(0.5F + 0.5F * wave(renderTime, 0.025F, line.pulseOffset()), 0.1F, 0.2F) / sideLength;
        var alphaPulse = 0.5F + 0.5F * wave(renderTime, 0.04F, line.pulseOffset() + 1.2F);
        var lineAlpha = Mth.clamp(alpha * Mth.lerp(alphaPulse, 0.12F, 0.42F), 0F, 0.42F);

        var red = Mth.clamp((color.primary().red() + color.secondary().red()) * 0.58F, 0F, 1F);
        var green = Mth.clamp((color.primary().green() + color.secondary().green()) * 0.58F, 0F, 1F);
        var blue = Mth.clamp((color.primary().blue() + color.secondary().blue()) * 0.58F, 0F, 1F);

        var tailAlpha = lineAlpha * (1F - startDelta);
        var tipAlpha = lineAlpha * endDelta;

        for (var side = 1F; side >= -1F; side -= 2F) {
            var offsetX = sideX * width * side;
            var offsetY = sideY * width * side;
            var offsetZ = sideZ * width * side;

            buffer.addVertex(matrix, fromX + offsetX, fromY + offsetY, fromZ + offsetZ).setColor(red, green, blue, tailAlpha);
            buffer.addVertex(matrix, toX + offsetX, toY + offsetY, toZ + offsetZ).setColor(red, green, blue, tipAlpha);
            buffer.addVertex(matrix, toX - offsetX, toY - offsetY, toZ - offsetZ).setColor(red, green, blue, tipAlpha);
            buffer.addVertex(matrix, fromX - offsetX, fromY - offsetY, fromZ - offsetZ).setColor(red, green, blue, tailAlpha);
        }
    }

    private record Tone(float red, float green, float blue) {
        private Tone lerp(Tone target, float delta) {
            return new Tone(Mth.lerp(delta, red, target.red), Mth.lerp(delta, green, target.green), Mth.lerp(delta, blue, target.blue));
        }
    }

    private record StarColor(Tone primary, Tone secondary) {

    }

    private record Star(float x, float y, float z, float size, float angle, float pulseOffset, float pulseSpeed, float sinYaw, float cosYaw, float sinPitch, float cosPitch) {

    }

    private record StarLayer(List<Star> stars, ResourceLocation texture, float spin, boolean secondary, float alpha) {

    }

    private record ConstellationPoint(float x, float y) {

    }

    private record ConstellationLink(int from, int to) {

    }

    private record Constellation(int seed, List<ConstellationPoint> points, List<ConstellationLink> links) {

    }

    private record ConstellationLine(Star from, Star to, float pulseOffset) {

    }

    private record ConstellationShape(List<Star> stars, List<ConstellationLine> lines, float[] arrivals, float[] departures) {

    }
}
