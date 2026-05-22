package it.hurts.sskirillss.relics.client.renderer.sky;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class MidnightMantleSkyRenderer {
    private static final ResourceLocation STAR_0 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_0.png");
    private static final ResourceLocation STAR_1 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_1.png");
    private static final ResourceLocation STAR_2 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_2.png");
    private static final ResourceLocation STAR_3 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_3.png");
    private static final ResourceLocation STAR_4 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_4.png");
    private static final ResourceLocation STAR_5 = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_5.png");

    private static final Minecraft MC = Minecraft.getInstance();
    private static final MidnightMantleSkyRenderer INSTANCE = new MidnightMantleSkyRenderer();

    private List<Star> starLayer0;
    private List<Star> starLayer1;
    private List<Star> starLayer2;
    private List<Star> starLayer3;
    private List<Star> starLayer4;
    private List<Star> starLayer5;

    private float currentTintRed;
    private float currentTintGreen;
    private float currentTintBlue;
    private float currentTintAlpha;
    private float currentStarAlpha;

    private boolean initialised;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY || MC.player == null || MC.level == null)
            return;

        var stacks = EntityUtils.findEquippedCurios(MC.player, RelicsItems.MIDNIGHT_MANTLE.get());

        INSTANCE.render(event, stacks);
    }

    public static boolean shouldSuppressVanillaStars() {
        return MC.player != null && MC.level != null && hasNormalSky() && INSTANCE.currentStarAlpha > 0.985F;
    }

    private void initialise() {
        if (!initialised) {
            initStars();

            initialised = true;
        }
    }

    public void render(RenderLevelStageEvent event, List<ItemStack> stacks) {
        if (MC.level == null)
            return;

        initialise();

        var matrices = new PoseStack();

        matrices.mulPose(event.getModelViewMatrix());

        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var renderTime = MC.player.tickCount + partialTick;
        var skyAngle = MC.level.getTimeOfDay(partialTick) * 360F;

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        var skyOcclusion = hasNormalSky() ? getSkyOcclusion(event) : 0F;
        var visibility = getVisibility(event) * skyOcclusion;
        var equipped = !stacks.isEmpty();
        var tint = equipped ? getSkyTint(stacks) : new SkyTint(currentTintRed, currentTintGreen, currentTintBlue);
        var targetTintAlpha = equipped ? visibility * 0.075F : 0F;
        var targetStarAlpha = equipped ? skyOcclusion : 0F;

        updateTint(tint, targetTintAlpha, partialTick);
        updateStarAlpha(targetStarAlpha, partialTick);

        var starAlpha = visibility * currentStarAlpha;

        if (currentTintAlpha > 0.003F)
            renderSkyTint(matrices, currentTintRed, currentTintGreen, currentTintBlue, currentTintAlpha);

        if (currentStarAlpha > 0.003F && equipped && visibility > 0) {
            var color = getStarColor(stacks);

            matrices.pushPose();
            matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
            matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle));

            RenderSystem.setShaderTexture(0, STAR_0);
            renderStarLayer(matrices, starLayer0, renderTime, 0.024F, color.secondaryRed(), color.secondaryGreen(), color.secondaryBlue(), starAlpha * 0.95F);

            RenderSystem.setShaderTexture(0, STAR_1);
            renderStarLayer(matrices, starLayer1, renderTime, 0.020F, color.red(), color.green(), color.blue(), starAlpha);

            RenderSystem.setShaderTexture(0, STAR_2);
            renderStarLayer(matrices, starLayer2, renderTime, -0.017F, color.secondaryRed(), color.secondaryGreen(), color.secondaryBlue(), starAlpha * 0.90F);

            RenderSystem.setShaderTexture(0, STAR_3);
            renderStarLayer(matrices, starLayer3, renderTime, 0.013F, color.red(), color.green(), color.blue(), starAlpha * 0.78F);

            RenderSystem.setShaderTexture(0, STAR_4);
            renderStarLayer(matrices, starLayer4, renderTime, -0.011F, color.secondaryRed(), color.secondaryGreen(), color.secondaryBlue(), starAlpha * 0.42F);

            RenderSystem.setShaderTexture(0, STAR_5);
            renderStarLayer(matrices, starLayer5, renderTime, 0.009F, color.red(), color.green(), color.blue(), starAlpha * 0.42F);

            matrices.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void updateTint(SkyTint target, float targetAlpha, float partialTick) {
        var speed = 0.075F * Math.max(partialTick, 0.25F);

        currentTintRed = Mth.lerp(speed, currentTintRed, target.red());
        currentTintGreen = Mth.lerp(speed, currentTintGreen, target.green());
        currentTintBlue = Mth.lerp(speed, currentTintBlue, target.blue());
        currentTintAlpha = Mth.lerp(speed, currentTintAlpha, targetAlpha);
    }

    private void updateStarAlpha(float targetAlpha, float partialTick) {
        var speed = 0.075F * Math.max(partialTick, 0.25F);

        currentStarAlpha = Mth.lerp(speed, currentStarAlpha, targetAlpha);

        if (targetAlpha == 0F && currentStarAlpha < 0.003F)
            currentStarAlpha = 0F;
        else if (targetAlpha == 1F && currentStarAlpha > 0.997F)
            currentStarAlpha = 1F;
    }

    private float getVisibility(RenderLevelStageEvent event) {
        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var night = Mth.clamp((1F - MC.level.getSkyDarken(partialTick)) * 1.65F, 0F, 1F);
        var weather = 1F - MC.level.getRainLevel(partialTick) * 0.65F;

        return Mth.clamp((MC.level.dimensionType().hasSkyLight() ? night : 1F) * weather, 0F, 1F);
    }

    private float getSkyOcclusion(RenderLevelStageEvent event) {
        var camera = event.getCamera();
        var fogType = camera.getFluidInCamera();

        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW)
            return 0F;

        if (camera.getEntity() instanceof LivingEntity entity
                && (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS)))
            return 0F;

        if (fogType == FogType.WATER)
            return 0.18F;

        return 1F;
    }

    private static boolean hasNormalSky() {
        return MC.level != null
                && MC.level.dimensionType().hasSkyLight()
                && MC.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL;
    }

    private SkyTint getSkyTint(List<ItemStack> stacks) {
        var stack = stacks.getFirst();
        var mode = ((MidnightMantleItem) stack.getItem())
                .getRelicData(MC.player, stack)
                .getAbilitiesData()
                .getAbilityData("phase")
                .getMode();

        if ("new_moon".equals(mode))
            return new SkyTint(0.10F, 0.42F, 0.92F);

        return new SkyTint(0.42F, 0.12F, 0.82F);
    }

    private StarColor getStarColor(List<ItemStack> stacks) {
        var stack = stacks.getFirst();
        var mode = ((MidnightMantleItem) stack.getItem())
                .getRelicData(MC.player, stack)
                .getAbilitiesData()
                .getAbilityData("phase")
                .getMode();

        if ("new_moon".equals(mode))
            return new StarColor(0.30F, 0.78F, 1F, 0.62F, 0.90F, 1F);

        return new StarColor(0.82F, 0.42F, 1F, 1F, 0.62F, 1F);
    }

    private void renderSkyTint(PoseStack matrices, float red, float green, float blue, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        var matrix = matrices.last().pose();
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var radius = 100F;

        addTintFace(buffer, matrix, -radius, -radius, -radius, -radius, -radius, radius, radius, -radius, radius, radius, -radius, -radius, red, green, blue, alpha);
        addTintFace(buffer, matrix, -radius, radius, radius, -radius, radius, -radius, radius, radius, -radius, radius, radius, radius, red, green, blue, alpha);
        addTintFace(buffer, matrix, -radius, -radius, radius, -radius, radius, radius, radius, radius, radius, radius, -radius, radius, red, green, blue, alpha);
        addTintFace(buffer, matrix, radius, -radius, -radius, radius, radius, -radius, -radius, radius, -radius, -radius, -radius, -radius, red, green, blue, alpha);
        addTintFace(buffer, matrix, -radius, -radius, -radius, -radius, radius, -radius, -radius, radius, radius, -radius, -radius, radius, red, green, blue, alpha);
        addTintFace(buffer, matrix, radius, -radius, radius, radius, radius, radius, radius, radius, -radius, radius, -radius, -radius, red, green, blue, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void addTintFace(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float red, float green, float blue, float alpha) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x4, y4, z4).setColor(red, green, blue, alpha);
    }

    private void renderStarLayer(PoseStack matrices, List<Star> stars, float renderTime, float spinSpeed, float r, float g, float b, float a) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        var matrix = matrices.last().pose();
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (var star : stars)
            addStar(buffer, matrix, star, renderTime, spinSpeed, r, g, b, a);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void initStars() {
        starLayer0 = makeUVStars(0.5f, 1.3f, 90, 77221);
        starLayer1 = makeUVStars(0.5f, 1.5f, 900, 41315);
        starLayer2 = makeUVStars(0.5f, 1.6f, 760, 35151);
        starLayer3 = makeUVStars(0.7f, 2.0f, 540, 61354);
        starLayer4 = makeUVStars(0.9f, 2.3f, 1240, 61355);
        starLayer5 = makeUVStars(0.9f, 2.3f, 90, 61356);
    }

    private List<Star> makeUVStars(float minSize, float maxSize, int count, long seed) {
        var random = new LegacyRandomSource(seed);
        var stars = new ArrayList<Star>();

        for (int i = 0; i < count; ++i) {
            var posX = random.nextFloat() * 2.0f - 1.0f;
            var posY = random.nextFloat() * 2.0f - 1.0f;
            var posZ = random.nextFloat() * 2.0f - 1.0f;

            var size = randRange(minSize, maxSize, random);
            var length = posX * posX + posY * posY + posZ * posZ;

            if (length < 1.0f && length > 0.001f) {
                length = 1.0f / (float) Math.sqrt(length);
                posX *= length;
                posY *= length;
                posZ *= length;

                var px = posX * 100.0f;
                var py = posY * 100.0f;
                var pz = posZ * 100.0f;

                stars.add(new Star(px, py, pz, size, random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, 0.045F + random.nextFloat() * 0.045F));
            }
        }

        return stars;
    }

    private void addStar(BufferBuilder buffer, Matrix4f matrix, Star star, float renderTime, float spinSpeed, float r, float g, float b, float a) {
        var posX = star.x();
        var posY = star.y();
        var posZ = star.z();

        var size = star.size();
        var pulse = 0.5F + 0.5F * Mth.sin(renderTime * star.pulseSpeed() + star.pulseOffset());
        var alphaPulse = 0.78F + 0.22F * Mth.sin(renderTime * star.pulseSpeed() * 1.17F + star.pulseOffset() + 1.4F);

        var red = Mth.clamp(Mth.lerp(pulse, 1F, r) * 1.25F, 0F, 1F);
        var green = Mth.clamp(Mth.lerp(pulse, 1F, g) * 1.25F, 0F, 1F);
        var blue = Mth.clamp(Mth.lerp(pulse, 1F, b) * 1.25F, 0F, 1F);
        var alpha = Mth.clamp(a * alphaPulse, 0F, 1F);

        var unitX = posX / 100F;
        var unitY = posY / 100F;
        var unitZ = posZ / 100F;

        var angle = (float) Math.atan2(unitX, unitZ);
        var sin1 = (float) Math.sin(angle);
        var cos1 = (float) Math.cos(angle);

        angle = (float) Math.atan2(Math.sqrt(unitX * unitX + unitZ * unitZ), unitY);

        var sin2 = (float) Math.sin(angle);
        var cos2 = (float) Math.cos(angle);

        angle = star.angle() + renderTime * spinSpeed;

        var sin3 = (float) Math.sin(angle);
        var cos3 = (float) Math.cos(angle);

        for (int index = 0; index < 4; ++index) {
            var x = (float) ((index & 2) - 1) * size;
            var y = (float) ((index + 1 & 2) - 1) * size;

            var aa = x * cos3 - y * sin3;
            var ab = y * cos3 + x * sin3;
            var dy = aa * sin2 + 0.0f * cos2;
            var ae = 0.0f * sin2 - aa * cos2;

            var dx = ae * sin1 - ab * cos1;
            var dz = ab * sin1 + ae * cos1;

            var texU = (index >> 1) & 1;
            var texV = ((index + 1) >> 1) & 1;

            buffer.addVertex(matrix, posX + dx, posY + dy, posZ + dz).setUv(texU, texV).setColor(red, green, blue, alpha);
        }
    }

    private static float randRange(float min, float max, RandomSource random) {
        return min + random.nextFloat() * (max - min);
    }

    private record Star(float x, float y, float z, float size, float angle, float pulseOffset, float pulseSpeed) {

    }

    private record StarColor(float red, float green, float blue, float secondaryRed, float secondaryGreen, float secondaryBlue) {

    }

    private record SkyTint(float red, float green, float blue) {

    }
}
