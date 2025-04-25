package it.hurts.sskirillss.relics.items.relics.back;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;
import static it.hurts.sskirillss.relics.init.DataComponentRegistry.SPEED;

@EventBusSubscriber
public class ElytraBoosterItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("boost")
                                .maxLevel(10)
                                .active(CastData.builder()
                                        .type(CastType.CYCLICAL)
                                        .predicate("fuel", PredicateType.CAST, (player, stack) -> stack.getOrDefault(CHARGE, 0) > 0)
                                        .predicate("elytra", PredicateType.CAST, (player, stack) -> player.isFallFlying())
                                        .build())
                                .stat(StatTemplate.builder("capacity")
                                        .initialValue(50, 100)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(1.1D, 1.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 16, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingTemplate(100, 10, 100))
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
    }

    public int getBreathCapacity(ItemStack stack) {
        return (int) Math.round(getStatValue(entity, stack, "boost", "capacity"));
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("boost")) {
            if (stage == CastStage.TICK) {
                int fuel = stack.getOrDefault(CHARGE, 0);

                if (fuel > 0 && player.tickCount % 20 == 0)
                    stack.set(CHARGE, --fuel);

                double speed = stack.getOrDefault(SPEED, 0D);

                if (player.tickCount % 3 == 0) {
                    double maxSpeed = getStatValue(entity, stack, "boost", "speed");

                    if (speed < maxSpeed) {
                        speed = Math.min(maxSpeed, speed + ((maxSpeed - 1D) / 100D));

                        stack.set(SPEED, speed);
                    } else {
                        player.startAutoSpinAttack(5, 0F, ItemStack.EMPTY);
                    }
                }

                Vec3 look = player.getLookAngle();
                Vec3 motion = player.getDeltaMovement();
                Level world = player.getCommandSenderWorld();
                RandomSource random = world.getRandom();

                player.setDeltaMovement(motion.add(look.x * 0.1D + (look.x * speed - motion.x) * 0.5D,
                        look.y * 0.1D + (look.y * speed - motion.y) * 0.5D,
                        look.z * 0.1D + (look.z * speed - motion.z) * 0.5D));

                for (int i = 0; i < speed * 3; i++)
                    world.addParticle(ParticleTypes.SMOKE,
                            player.getX() + (MathUtils.randomFloat(random) * 0.4F),
                            player.getY() + (MathUtils.randomFloat(random) * 0.4F),
                            player.getZ() + (MathUtils.randomFloat(random) * 0.4F),
                            0, 0, 0);

                if (player.tickCount % Math.max(1, (int) Math.round((10 - speed * 2) / (player.isInWaterOrRain() ? 2 : 1))) == 0)
                    stack.set(CHARGE, --fuel);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

        if (!(entity instanceof Player player))
            return;

        double speed = stack.getOrDefault(SPEED, 0D);
        int fuel = stack.getOrDefault(CHARGE, 0);

        if (speed > 1 && (fuel <= 0 || !player.isFallFlying()))
            stack.set(SPEED, 1D);
    }

    @Override
    public ResourceLocation getTexture(ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/item/model/elytra_booster_" + (stack.getOrDefault(CHARGE, 0) > 0 ? 1 : 0) + ".png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, model);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, -0.075F, 1.25F, 10.0F, 6.0F, 4.0F, new CubeDeformation(0.025F)).mirror(false)
                .texOffs(0, 23).addBox(-2.0F, -2.0F, 3.75F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.25F, 0.0F, 0.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 0).addBox(-3.75F, -11.0F, -1.001F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).mirror().addBox(-3.75F, -8.0F, -1.0F, 6.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 11.25F, 2.25F, 0.0F, 0.0F, 0.1309F));
        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(2.75F, -11.0F, -1.001F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 10).addBox(-2.25F, -8.0F, -1.0F, 6.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.25F, 2.25F, 0.0F, 0.0F, -0.1309F));
        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 7).mirror().addBox(-0.15F, -2.975F, -1.8F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-8.7786F, 3.4288F, 4.25F, 0.0F, 0.0F, 0.5236F));
        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(28, 7).addBox(-3.875F, -2.975F, -1.8F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7786F, 3.4288F, 4.25F, 0.0F, 0.0F, -0.5236F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }

    @SubscribeEvent
    public static void onSlotClick(ContainerSlotClickEvent event) {
        if (event.getAction() != ClickAction.PRIMARY)
            return;

        Player player = event.getEntity();

        ItemStack heldStack = event.getHeldStack();
        ItemStack slotStack = event.getSlotStack();

        if (!(slotStack.getItem() instanceof ElytraBoosterItem booster))
            return;

        int time = heldStack.getBurnTime(RecipeType.SMELTING) / 20;
        int amount = slotStack.getOrDefault(CHARGE, 0);
        int capacity = booster.getBreathCapacity(slotStack);
        int sum = amount + time;

        if (time <= 0)
            return;

        slotStack.set(CHARGE, Math.min(capacity, sum));

        int left = sum > capacity ? time - (sum - capacity) : time;

        booster.spreadRelicExperience(player, slotStack, (int) Math.floor(left / 10F));

        ItemStack result = heldStack.getCraftingRemainingItem();

        heldStack.shrink(1);

        if (!result.isEmpty()) {
            if (heldStack.isEmpty())
                player.containerMenu.setCarried(result);
            else
                player.getInventory().add(result);
        }

        player.playSound(SoundEvents.BLAZE_SHOOT, 0.75F, 2F / (time * 0.025F));

        event.setCanceled(true);
    }
}