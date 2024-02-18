package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.client.hud.abilities.AbilitiesRenderHandler;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.client.renderer.entities.*;
import it.hurts.sskirillss.relics.client.renderer.items.items.CurioRenderer;
import it.hurts.sskirillss.relics.client.renderer.tiles.ResearchingTableRenderer;
import it.hurts.sskirillss.relics.items.SolidSnowballItem;
import it.hurts.sskirillss.relics.items.relics.BlazingFlaskItem;
import it.hurts.sskirillss.relics.items.relics.InfinityHamItem;
import it.hurts.sskirillss.relics.items.relics.ShadowGlaiveItem;
import it.hurts.sskirillss.relics.items.relics.back.ArrowQuiverItem;
import it.hurts.sskirillss.relics.items.relics.back.ElytraBoosterItem;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.feet.AquaWalkerItem;
import it.hurts.sskirillss.relics.items.relics.feet.MagmaWalkerItem;
import it.hurts.sskirillss.relics.items.relics.feet.RollerSkatesItem;
import it.hurts.sskirillss.relics.tiles.base.IHasHUDInfo;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static it.hurts.sskirillss.relics.items.relics.back.ArrowQuiverItem.*;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        ResourceLocation location = event.getAtlas().location();

        if (location.equals(TextureAtlas.LOCATION_BLOCKS)) {
            event.addSprite(new ResourceLocation(Reference.MODID, "gui/curios/empty_talisman_slot"));
            event.addSprite(new ResourceLocation(Reference.MODID, "gui/curios/empty_feet_slot"));
        }
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.RESEARCHING_TABLE.get(), RenderType.cutout());

        event.enqueueWork(() -> {
        ItemProperties.register(ItemRegistry.INFINITY_HAM.get(), new ResourceLocation(Reference.MODID, "pieces"),
                (stack, world, entity, id) -> Math.min(10, NBTUtils.getInt(stack, InfinityHamItem.TAG_PIECES, 0)));
        ItemProperties.register(ItemRegistry.SHADOW_GLAIVE.get(), new ResourceLocation(Reference.MODID, "charges"),
                (stack, world, entity, id) -> Math.min(8, NBTUtils.getInt(stack, ShadowGlaiveItem.TAG_CHARGES, 0)));
        ItemProperties.register(ItemRegistry.MAGIC_MIRROR.get(), new ResourceLocation(Reference.MODID, "world"),
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
            ItemProperties.register(ItemRegistry.SHADOW_GLAIVE.get(), new ResourceLocation(Reference.MODID, "charges"),
                    (stack, world, entity, id) -> Math.min(8, NBTUtils.getInt(stack, ShadowGlaiveItem.TAG_CHARGES, 0)));
            ItemProperties.register(ItemRegistry.MAGMA_WALKER.get(), new ResourceLocation(Reference.MODID, "heat"),
                    (stack, world, entity, id) -> {
                        int heat = NBTUtils.getInt(stack, MagmaWalkerItem.TAG_HEAT, 0);
                        int maxHeat = (int) Math.round(((IRelicItem) stack.getItem()).getAbilityValue(stack, "pace", "heat"));

                        return heat > maxHeat ? 4 : (int) Math.floor(heat / (maxHeat / 4F));
                    });
            ItemProperties.register(ItemRegistry.AQUA_WALKER.get(), new ResourceLocation(Reference.MODID, "drench"),
                    (stack, world, entity, id) -> NBTUtils.getInt(stack, AquaWalkerItem.TAG_DRENCH, 0) >= ((IRelicItem) stack.getItem()).getAbilityValue(stack, "walking", "time") ? 1 : 0);
            ItemProperties.register(ItemRegistry.ARROW_QUIVER.get(), new ResourceLocation(Reference.MODID, "fullness"),
                    (stack, world, entity, id) -> {
                        int maxAmount = ((ArrowQuiverItem) stack.getItem()).getSlotsAmount(stack);
                        int amount = getArrows(stack).size();

                        return amount > 0 ? (int) Math.floor(amount / (maxAmount / 2F)) + 1 : 0;
                    });
            ItemProperties.register(ItemRegistry.ELYTRA_BOOSTER.get(), new ResourceLocation(Reference.MODID, "fuel"),
                    (stack, world, entity, id) -> NBTUtils.getInt(stack, ElytraBoosterItem.TAG_FUEL, 0) > 0 ? 1 : 0);
            ItemProperties.register(ItemRegistry.SOLID_SNOWBALL.get(), new ResourceLocation(Reference.MODID, "snow"),
                    (stack, world, entity, id) -> {
                        ItemStack relic = EntityUtils.findEquippedCurio(entity, ItemRegistry.WOOL_MITTEN.get());

                        return (int) Math.floor(NBTUtils.getInt(stack, SolidSnowballItem.TAG_SNOW, 0) / (((IRelicItem) stack.getItem()).getAbilityValue(relic, "mold", "size") / 3F));
                    });
            ItemProperties.register(ItemRegistry.ROLLER_SKATES.get(), new ResourceLocation(Reference.MODID, "active"),
                    (stack, world, entity, id) -> NBTUtils.getInt(stack, RollerSkatesItem.TAG_SKATING_DURATION, 0) > 0 ? 1 : 0);

        ItemProperties.register(ItemRegistry.BLAZING_FLASK.get(), new ResourceLocation(Reference.MODID, "active"),
                (stack, world, entity, id) -> NBTUtils.getString(stack, BlazingFlaskItem.TAG_POSITION, "").isEmpty() ? 0 : 1);
        });

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!(item instanceof IRenderableCurio))
                continue;

            CuriosRendererRegistry.register(item, CurioRenderer::new);
        }
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!(item instanceof IRenderableCurio renderable))
                continue;

            event.registerLayerDefinition(CurioModel.getLayerLocation(item), renderable::constructLayerDefinition);
        }
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.SHADOW_GLAIVE.get(), new ShadowGlaiveRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.BLOCK_SIMULATION.get(), new BlockSimulationRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.SHOCKWAVE.get(), new NullRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.LIFE_ESSENCE.get(), new NullRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.STALACTITE.get(), new StalactiteRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.DISSECTION.get(), new DissectionRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.SPORE.get(), new SporeRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.SHADOW_SAW.get(), new ShadowSawRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.SOLID_SNOWBALL.get(), new SolidSnowballRenderer.RenderFactory());
        event.registerEntityRenderer(EntityRegistry.ARROW_RAIN.get(), new NullRenderer.RenderFactory());

        event.registerBlockEntityRenderer(TileRegistry.RESEARCHING_TABLE.get(), ResearchingTableRenderer::new);
    }

    @SubscribeEvent
    public static void onTooltipRegistry(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArrowQuiverTooltip.class, ClientArrowQuiverTooltip::new);
    }

    @SubscribeEvent
    public static void onOverlayRegistry(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("researching_hint", (ForgeGui, poseStack, partialTick, screenWidth, screenHeight) -> {
            Minecraft MC = Minecraft.getInstance();
            ClientLevel level = MC.level;

            if (level == null)
                return;

            HitResult hit = MC.hitResult;

            if (hit == null || hit.getType() != HitResult.Type.BLOCK)
                return;

            BlockPos pos = ((BlockHitResult) MC.hitResult).getBlockPos();
            BlockEntity tile = level.getBlockEntity(pos);

            if (!(tile instanceof IHasHUDInfo infoTile))
                return;

            infoTile.renderHUDInfo(poseStack, MC.getWindow());
        });

        event.registerBelowAll("active_abilities", (ForgeGui, poseStack, partialTick, screenWidth, screenHeight) -> {
            AbilitiesRenderHandler.render(poseStack, partialTick);
        });
    }
}