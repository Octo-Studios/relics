package it.hurts.sskirillss.relics.init;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailRegistry;
import it.hurts.octostudios.octolib.module.particle.trail.TestArrowTrail;
import it.hurts.sskirillss.relics.client.gui.layers.ActiveAbilitiesLayer;
import it.hurts.sskirillss.relics.client.gui.layers.InfoTileLayer;
import it.hurts.sskirillss.relics.client.gui.layers.LeafyRingHideLayer;
import it.hurts.sskirillss.relics.client.gui.layers.PhantomBootBridgeLayer;
import it.hurts.sskirillss.relics.client.models.items.ReflectiveNecklaceModel;
import it.hurts.sskirillss.relics.client.models.items.base.CurioModel;
import it.hurts.sskirillss.relics.client.models.layers.WingsLayer;
import it.hurts.sskirillss.relics.client.renderer.entities.*;
import it.hurts.sskirillss.relics.client.renderer.items.ReflectiveNecklaceRenderer;
import it.hurts.sskirillss.relics.client.renderer.items.items.CurioRenderer;
import it.hurts.sskirillss.relics.client.renderer.tiles.ResearchingTableRenderer;
import it.hurts.sskirillss.relics.entities.*;
import it.hurts.sskirillss.relics.items.relics.InfiniteHamItem;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.necklace.HolyLocketItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;
import static it.hurts.sskirillss.relics.init.DataComponentRegistry.WORLD_POSITION;

@EventBusSubscriber(modid = Reference.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.RESEARCHING_TABLE.get(), RenderType.cutout());

        event.enqueueWork(() -> {
            ItemProperties.register(RelicsItems.INFINITY_HAM.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "pieces"),
                    (stack, world, entity, id) -> ((InfiniteHamItem) stack.getItem()).getPieces(stack));
            ItemProperties.register(RelicsItems.SHADOW_GLAIVE.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "charges"),
                    (stack, world, entity, id) -> Math.min(8, stack.getOrDefault(CHARGE, 0)));
            ItemProperties.register(RelicsItems.MAGIC_MIRROR.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "world"),
                    (stack, world, entity, id) -> {
                        Entity e = Minecraft.getInstance().getCameraEntity();

                        if (e == null)
                            return 0;

                        return switch (e.getCommandSenderWorld().dimension().location().getPath()) {
                            case "overworld" -> 1;
                            case "the_nether" -> 2;
                            case "the_end" -> 3;
                            default -> 0;
                        };
                    });
            ItemProperties.register(RelicsItems.SHADOW_GLAIVE.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "charges"),
                    (stack, world, entity, id) -> Math.min(8, stack.getOrDefault(CHARGE, 0)));
            ItemProperties.register(RelicsItems.MAGMA_WALKER.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "heat"),
                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) >= ((IRelicItem) stack.getItem()).getStatValue(entity, stack, "pace", "time") ? 1 : 0);
            ItemProperties.register(RelicsItems.AQUA_WALKER.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "drench"),
                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) >= ((IRelicItem) stack.getItem()).getStatValue(entity, stack, "walking", "time") ? 1 : 0);
//            ItemProperties.register(ItemRegistry.ARROW_QUIVER.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "fullness"),
//                    (stack, world, entity, id) -> {
//                        int maxAmount = ((ArrowQuiverItem) stack.getItem()).getSlotsAmount(stack);
//                        int amount = getArrows(world.registryAccess(), stack).size();
//
//                        return amount > 0 ? (int) Math.floor(amount / (maxAmount / 2F)) + 1 : 0;
//                    });
            ItemProperties.register(RelicsItems.ELYTRA_BOOSTER.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "fuel"),
                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) > 0 ? 1 : 0);
            ItemProperties.register(RelicsItems.SOLID_SNOWBALL.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "snow"),
                    (stack, world, entity, id) -> {
                        ItemStack relic = EntityUtils.findEquippedCurio(entity, RelicsItems.WOOL_MITTEN.get());

                        if (relic.isEmpty())
                            return 3;

                        return (int) Math.floor(stack.getOrDefault(CHARGE, 0) / (((IRelicItem) relic.getItem()).getStatValue(entity, relic, "mold", "size") / 3F));
                    });
            ItemProperties.register(RelicsItems.ROLLER_SKATES.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "active"),
                    (stack, world, entity, id) -> stack.getOrDefault(CHARGE, 0) > 0 ? 1 : 0);

            ItemProperties.register(RelicsItems.BLAZING_FLASK.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "active"),
                    (stack, world, entity, id) -> stack.get(WORLD_POSITION) == null ? 0 : 1);
            ItemProperties.register(RelicsItems.HOLY_LOCKET.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "mode"),
                    (stack, world, entity, id) -> ((HolyLocketItem) stack.getItem()).getMode(stack).getIndex());
            ItemProperties.register(RelicsItems.MIDNIGHT_MANTLE.get(), ResourceLocation.fromNamespaceAndPath(Reference.MODID, "mode"),
                    (stack, world, entity, id) -> {
                        var relic = (MidnightMantleItem) stack.getItem();
                        var mode = relic.getAbilityMode(entity, stack, "phase");

                        return mode.equals("full_moon") ? 1 : 0;
                    });
        });

        CuriosRendererRegistry.register(RelicsItems.REFLECTIVE_NECKLACE.get(), ReflectiveNecklaceRenderer::new);

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
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ReflectiveNecklaceModel.LAYER, ReflectiveNecklaceModel::constructLayerDefinition);

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
        event.registerEntityRenderer(RelicsEntities.ELECTRIC_SPARK.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.BLOCK_SIMULATION.get(), BlockSimulationRenderer::new);
        event.registerEntityRenderer(RelicsEntities.SHOCKWAVE.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.LIFE_ESSENCE.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.DEATH_ESSENCE.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.REFLECTIVE_ORB.get(), ReflectiveOrbRenderer::new);
        event.registerEntityRenderer(RelicsEntities.DISSECTION.get(), DissectionRenderer::new);
        event.registerEntityRenderer(RelicsEntities.SPORE.get(), SporeRenderer::new);
        event.registerEntityRenderer(RelicsEntities.SOLID_SNOWBALL.get(), SolidSnowballRenderer::new);
//        event.registerEntityRenderer(EntityRegistry.ARROW_RAIN.get(), NullRenderer::new);
        event.registerEntityRenderer(RelicsEntities.RELIC_EXPERIENCE_ORB.get(), RelicExperienceOrbRenderer::new);
        event.registerEntityRenderer(RelicsEntities.THROWN_RELIC_EXPERIENCE_BOTTLE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(RelicsEntities.CHAIR.get(), NullRenderer::new);

        event.registerBlockEntityRenderer(TileRegistry.RESEARCHING_TABLE.get(), ResearchingTableRenderer::new);
    }

    @SubscribeEvent
    public static void onTooltipRegistry(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(InfiniteHamItem.InfiniteHamTooltip.class, InfiniteHamItem.ClientInfiniteHamTooltip::new);
    }

    @SubscribeEvent
    public static void onOverlayRegistry(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "info_tile"), new InfoTileLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "active_abilities"), new ActiveAbilitiesLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "leafy_ring_hide"), new LeafyRingHideLayer());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "phantom_boot_bridge"), new PhantomBootBridgeLayer());
    }
}