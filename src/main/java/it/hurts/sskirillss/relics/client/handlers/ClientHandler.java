package it.hurts.sskirillss.relics.client.handlers;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailRegistry;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategories;
import it.hurts.sskirillss.relics.client.gui.layers.*;
import it.hurts.sskirillss.relics.client.layer.*;
import it.hurts.sskirillss.relics.client.models.items.*;
import it.hurts.sskirillss.relics.client.models.layers.WingsLayer;
import it.hurts.sskirillss.relics.client.postEffects.LensPostEffect;
import it.hurts.sskirillss.relics.client.renderer.entities.*;
import it.hurts.sskirillss.relics.client.renderer.items.*;
import it.hurts.sskirillss.relics.client.style.*;
import it.hurts.sskirillss.relics.description_categories.AbilityDescriptionCategory;
import it.hurts.sskirillss.relics.description_categories.RelicDescriptionCategory;
import it.hurts.sskirillss.relics.description_categories.SynergyDescriptionCategory;
import it.hurts.sskirillss.relics.description_subcategories.*;
import it.hurts.sskirillss.relics.entities.*;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.feet.CutGlassBootItem;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = Relics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
//            ItemProperties.register(RelicsItems.INFINITY_HAM.get(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "pieces"),
//                    (stack, world, entity, id) -> ((InfiniteHamItem) stack.getItem()).getPieces(stack));
//            ItemProperties.register(RelicsItems.MAGMA_WALKER.get(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "heat"),
//                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) >= ((IRelicItem) stack.getItem()).getStatValue(entity, stack, "pace", "time") ? 1 : 0);
//            ItemProperties.register(RelicsItems.AQUA_WALKER.get(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "drench"),
//                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) >= ((IRelicItem) stack.getItem()).getStatValue(entity, stack, "walking", "time") ? 1 : 0);
//            ItemProperties.register(RelicsItems.HOLY_LOCKET.get(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "mode"),
//                    (stack, world, entity, id) -> ((HolyLocketItem) stack.getItem()).getMode(stack).getIndex());
            ItemProperties.register(RelicsItems.MIDNIGHT_MANTLE.get(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "phase"),
                    (stack, world, entity, id) -> {
                        var relic = (MidnightMantleItem) stack.getItem();
                        var mode = relic.getAbilityMode(entity, stack, "phase");

                        return mode.equals("full_moon") ? 1 : 0;
                    });

            for (var item : BuiltInRegistries.ITEM.stream().toList()) {
                if (!(item instanceof IRelicItem relic))
                    continue;

                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "flawless"),
                        (stack, level, entity, id) -> relic.isRelicFlawless(entity, stack) ? 1 : 0);
            }
        });

        RelicsRelicRenderers.register(RelicsItems.ROLLER_SKATE.get(), RollerSkateRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.SPRINGY_BOOT.get(), SpringyBootRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.KINETIC_BELT.get(), KineticBeltRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.REFLECTIVE_NECKLACE.get(), ReflectiveNecklaceRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.JELLYFISH_NECKLACE.get(), JellyfishNecklaceRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.MIDNIGHT_MANTLE.get(), MidnightMantleRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.LEAFY_MANTLE.get(), LeafyMantleRenderer::new);
        RelicsRelicRenderers.register(RelicsItems.PIGLIN_MASK.get(), PiglinMaskRenderer::new);

        EntityTrailRegistry.registerProvider(RelicsEntities.SHADOW_GLAIVE.get(), ShadowGlaiveEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.ELECTRIC_SPARK.get(), ElectricSparkEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.REFLECTIVE_ORB.get(), ReflectiveOrbEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.SPORE.get(), SporeEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.DEATH_ESSENCE.get(), DeathEssenceEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.LIFE_ESSENCE.get(), LifeEssenceEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.LEAVES_BLOCK.get(), LeavesBlockEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.ROLLER_SPARK.get(), RollerSparkEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.GOLDEN_TOOTH.get(), GoldenToothEntity.TrailProvider::new);

        DescriptionCategories.registerCategory(RelicDescriptionCategory::new);
        DescriptionCategories.registerCategory(AbilityDescriptionCategory::new);
        DescriptionCategories.registerCategory(SynergyDescriptionCategory::new);

        DescriptionSubcategories.registerSubcategory(AbilityDescriptionDescriptionSubcategory::new);
        DescriptionSubcategories.registerSubcategory(AbilityExperienceDescriptionSubcategory::new);
        DescriptionSubcategories.registerSubcategory(AbilityStatisticDescriptionSubcategory::new);
        DescriptionSubcategories.registerSubcategory(RelicDescriptionDescriptionSubcategory::new);
        DescriptionSubcategories.registerSubcategory(RelicStatisticDescriptionSubcategory::new);

        RelicsRelicStyles.register(RelicsItems.REFLECTIVE_NECKLACE.get(), ReflectiveNecklaceStyle::new);
        RelicsRelicStyles.register(RelicsItems.JELLYFISH_NECKLACE.get(), JellyfishNecklaceStyle::new);
        RelicsRelicStyles.register(RelicsItems.MIDNIGHT_MANTLE.get(), MidnightMantleStyle::new);
        RelicsRelicStyles.register(RelicsItems.CHORUS_STAFF.get(), ChorusStaffStyle::new);
        RelicsRelicStyles.register(RelicsItems.LEAFY_MANTLE.get(), LeafyMantleStyle::new);
        RelicsRelicStyles.register(RelicsItems.SPRINGY_BOOT.get(), SpringyBootStyle::new);
        RelicsRelicStyles.register(RelicsItems.KINETIC_BELT.get(), KineticBeltStyle::new);
        RelicsRelicStyles.register(RelicsItems.ROLLER_SKATE.get(), RollerSkateStyle::new);
        RelicsRelicStyles.register(RelicsItems.PIGLIN_MASK.get(), PiglinMaskStyle::new);

        RelicsPostEffects.register(LensPostEffect::new);

        RelicsRelicStyles.init();
        RelicsPostEffects.init();
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ReflectiveNecklaceModel.LAYER, ReflectiveNecklaceModel::constructLayerDefinition);
        event.registerLayerDefinition(JellyfishNecklaceModel.LAYER, JellyfishNecklaceModel::constructLayerDefinition);
        event.registerLayerDefinition(KineticBeltModel.LAYER, KineticBeltModel::constructLayerDefinition);
        event.registerLayerDefinition(SpringyBootModel.LAYER, SpringyBootModel::constructLayerDefinition);
        event.registerLayerDefinition(MidnightMantleModel.LAYER, MidnightMantleModel::constructLayerDefinition);
        event.registerLayerDefinition(LeafyMantleModel.LAYER, LeafyMantleModel::constructLayerDefinition);
        event.registerLayerDefinition(RollerSkateModel.LAYER, RollerSkateModel::constructLayerDefinition);
        event.registerLayerDefinition(PiglinMaskModel.LAYER, PiglinMaskModel::constructLayerDefinition);
    }

    @SubscribeEvent
    public static void onPlayerRendererRegister(EntityRenderersEvent.AddLayers event) {
        for (var skinType : event.getSkins()) {
            if (event.getSkin(skinType) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new WingsLayer<>(renderer));

                renderer.addLayer(new NecklaceLayer<>(renderer));
                renderer.addLayer(new BeltLayer<>(renderer));
                renderer.addLayer(new BackLayer<>(renderer));
                renderer.addLayer(new FeetLayer<>(renderer));
                renderer.addLayer(new HeadLayer<>(renderer));
            }
        }

        RelicsRelicRenderers.init();
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RelicsEntities.SHADOW_GLAIVE.get(), ShadowGlaiveRenderer::new);
        event.registerEntityRenderer(RelicsEntities.CONSTELLATION_STAR.get(), ConstellationStarRenderer::new);
        event.registerEntityRenderer(RelicsEntities.FALLING_STAR.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.ELECTRIC_SPARK.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.ROLLER_SPARK.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.SHOCKWAVE_BLOCK.get(), ShockwaveBlockRenderer::new);
        event.registerEntityRenderer(RelicsEntities.LIFE_ESSENCE.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.DEATH_ESSENCE.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.REFLECTIVE_ORB.get(), ReflectiveOrbRenderer::new);
        event.registerEntityRenderer(RelicsEntities.SPORE.get(), SporeRenderer::new);
        event.registerEntityRenderer(RelicsEntities.LEAVES_BLOCK.get(), LeavesBlockRenderer::new);
        event.registerEntityRenderer(RelicsEntities.RELIC_EXPERIENCE_ORB.get(), RelicExperienceOrbRenderer::new);
        event.registerEntityRenderer(RelicsEntities.THROWN_RELIC_EXPERIENCE_BOTTLE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(RelicsEntities.GOLDEN_TOOTH.get(), GoldenToothRenderer::new);
    }

    @SubscribeEvent
    public static void onTooltipRegistry(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CutGlassBootItem.CutGlassBootTooltip.class, CutGlassBootItem.ClientCutGlassBootTooltip::new);
    }

    @SubscribeEvent
    public static void onOverlayRegistry(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "info_tile"), new InfoTileLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "active_abilities"), new ActiveAbilitiesLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "leafy_ring_hide"), new LeafyMantleHideLayer());
//        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "phantom_boot_bridge"), new PhantomBootBridgeLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "stun_effect"), new StunEffectLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "piglin_mask/teeth"), new PiglinMaskTeethLayer());
    }
}