package it.hurts.sskirillss.relics.init;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailRegistry;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.client.gui.layers.*;
import it.hurts.sskirillss.relics.client.models.items.*;
import it.hurts.sskirillss.relics.client.models.items.base.CurioModel;
import it.hurts.sskirillss.relics.client.models.layers.WingsLayer;
import it.hurts.sskirillss.relics.client.renderer.entities.*;
import it.hurts.sskirillss.relics.client.renderer.items.*;
import it.hurts.sskirillss.relics.client.renderer.items.items.CurioRenderer;
import it.hurts.sskirillss.relics.description_categories.AbilityDescriptionCategory;
import it.hurts.sskirillss.relics.description_categories.RelicDescriptionCategory;
import it.hurts.sskirillss.relics.description_categories.SynergyDescriptionCategory;
import it.hurts.sskirillss.relics.entities.*;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = Relics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
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

        CuriosRendererRegistry.register(RelicsItems.REFLECTIVE_NECKLACE.get(), ReflectiveNecklaceRenderer::new);
        CuriosRendererRegistry.register(RelicsItems.JELLYFISH_NECKLACE.get(), JellyfishNecklaceRenderer::new);
        CuriosRendererRegistry.register(RelicsItems.KINETIC_BELT.get(), KineticBeltRenderer::new);
        CuriosRendererRegistry.register(RelicsItems.SPRINGY_BOOT.get(), SpringyBootRenderer::new);
        CuriosRendererRegistry.register(RelicsItems.MIDNIGHT_MANTLE.get(), MidnightMantleRenderer::new);

        for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
            if (!(item instanceof IRenderableCurio))
                continue;

            CuriosRendererRegistry.register(item, CurioRenderer::new);
        }

        EntityTrailRegistry.registerProvider(RelicsEntities.SHADOW_GLAIVE.get(), ShadowGlaiveEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.ELECTRIC_SPARK.get(), ElectricSparkEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.REFLECTIVE_ORB.get(), ReflectiveOrbEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.SPORE.get(), SporeEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.DEATH_ESSENCE.get(), DeathEssenceEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.LIFE_ESSENCE.get(), LifeEssenceEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.LEAVES_BLOCK.get(), LeavesBlockEntity.TrailProvider::new);
        EntityTrailRegistry.registerProvider(RelicsEntities.ROLLER_SPARK.get(), RollerSparkEntity.TrailProvider::new);

        DescriptionCategories.registerCategory(RelicDescriptionCategory::new);
        DescriptionCategories.registerCategory(AbilityDescriptionCategory::new);
        DescriptionCategories.registerCategory(SynergyDescriptionCategory::new);
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ReflectiveNecklaceModel.LAYER, ReflectiveNecklaceModel::constructLayerDefinition);
        event.registerLayerDefinition(JellyfishNecklaceModel.LAYER, JellyfishNecklaceModel::constructLayerDefinition);
        event.registerLayerDefinition(KineticBeltModel.LAYER, KineticBeltModel::constructLayerDefinition);
        event.registerLayerDefinition(SpringyBootModel.LAYER, SpringyBootModel::constructLayerDefinition);
        event.registerLayerDefinition(MidnightMantleModel.LAYER, MidnightMantleModel::constructLayerDefinition);

        for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
            if (!(item instanceof IRenderableCurio renderable))
                continue;

            event.registerLayerDefinition(CurioModel.getLayerLocation(item), renderable::constructLayerDefinition);
        }
    }

    @SubscribeEvent
    public static void onPlayerRendererRegister(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skinType : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(skinType);

            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new WingsLayer<>(playerRenderer));
            }
        }
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
    }

    @SubscribeEvent
    public static void onTooltipRegistry(RegisterClientTooltipComponentFactoriesEvent event) {
//        event.register(InfiniteHamItem.InfiniteHamTooltip.class, InfiniteHamItem.ClientInfiniteHamTooltip::new);
    }

    @SubscribeEvent
    public static void onOverlayRegistry(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "info_tile"), new InfoTileLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "active_abilities"), new ActiveAbilitiesLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "leafy_ring_hide"), new LeafyMantleHideLayer());
//        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "phantom_boot_bridge"), new PhantomBootBridgeLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "stun_effect"), new StunEffectLayer());
    }
}