package it.hurts.sskirillss.relics.items.relics.belt;

import com.google.common.collect.Lists;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class DrownedBeltItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("slots")
                                .requiredPoints(2)
                                .stat(StatTemplate.builder("charm")
                                        .initialValue(0D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("anchor")
                                .stat(StatTemplate.builder("slowness")
                                        .initialValue(0.5D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), -0.05D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 2) * 100))
                                        .build())
                                .stat(StatTemplate.builder("sinking")
                                        .initialValue(5D, 3D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), -0.1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 2) * 100))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("pressure")
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(1.25D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 2) * 100))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("riptide")
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(10D, 5D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), -0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingTemplate(100, 10, 100))
                .loot(LootTemplate.builder()
                        .entry(LootEntries.AQUATIC)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        if (player.isEyeInFluid(FluidTags.WATER) && !player.onGround())
            EntityUtils.applyAttribute(player, stack, Attributes.GRAVITY, (float) getStatValue(entity, stack, "anchor", "sinking"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        else
            EntityUtils.removeAttribute(player, stack, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        EntityUtils.removeAttribute(slotContext.entity(), stack, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) Math.round(getStatValue(entity, stack, "slots", "charm")))
                .build();
    }

    @Override
    public RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(NeoForgeMod.SWIM_SPEED, (float) -getStatValue(entity, stack, "anchor", "slowness")))
                .build();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition bone = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 7).addBox(-2.05F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, -2.5F, -0.1295F, -0.0378F, 0.0894F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }

    @EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onEntityHurt(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player)
                    || !player.isUnderWater() || !event.getEntity().isUnderWater())
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.DROWNED_BELT.get());

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            event.setAmount((float) (event.getAmount() * relic.getStatValue(entity, stack, "pressure", "damage")));
        }

        @SubscribeEvent
        public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
            ItemStack stack = event.getItem();

            if (!(event.getEntity() instanceof Player player) || stack.getItem() != Items.TRIDENT || !player.getCooldowns().isOnCooldown(stack.getItem()))
                return;

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onItemUseFinish(LivingEntityUseItemEvent.Stop event) {
            ItemStack trident = event.getItem();

            if (!(event.getEntity() instanceof Player player) || trident.getItem() != Items.TRIDENT)
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.DROWNED_BELT.get());

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;


            int duration = trident.getItem().getUseDuration(trident, player) - event.getDuration();
            int enchantment = trident.getEnchantmentLevel(player.level().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.RIPTIDE));

            if (duration < 10 || enchantment <= 0)
                return;

            relic.spreadRelicExperience(player, stack, enchantment);

            player.getCooldowns().addCooldown(trident.getItem(), (int) Math.round(relic.getStatValue(entity, stack, "riptide", "cooldown") * enchantment * 20));
        }
    }
}