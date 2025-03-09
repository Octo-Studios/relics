package it.hurts.sskirillss.relics.items.relics.ring;

import com.mojang.datafixers.util.Pair;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;
import java.util.Optional;

public class BastionRingItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("compass")
                                .maxLevel(0)
                                .build())
                        .ability(AbilityData.builder("trade")
                                .requiredLevel(5)
                                .requiredPoints(2)
                                .stat(StatData.builder("rolls")
                                        .initialValue(0D, 1D)
                                        .upgradeModifier(UpgradeOperation.ADD, 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .loot(LootData.builder()
                        .entry(LootEntries.BASTION)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        Level world = player.getCommandSenderWorld();

        if (world.isClientSide() || world.dimension() != Level.NETHER)
            return;

        Piglin piglin = world.getNearestEntity(Piglin.class, TargetingConditions.DEFAULT, player,
                player.getX(), player.getY(), player.getZ(), player.getBoundingBox().inflate(5));

        if (piglin == null || piglin.getTarget() == player)
            return;

        ServerLevel serverLevel = (ServerLevel) world;

        ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("bastion_remnant"));

        Optional<HolderSet<Structure>> optional = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getHolder(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("bastion_remnant")))
                .map(HolderSet::direct);

        if (optional.isEmpty())
            return;

        Pair<BlockPos, Holder<Structure>> bastion = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel,
                optional.get(), player.blockPosition(), 100, false);

        if (bastion == null)
            return;

        BlockPos bastionPos = bastion.getFirst();

        piglin.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, false, false));

        Vec3 currentVec = piglin.position();
        Vec3 finalVec = currentVec.add(new Vec3(bastionPos.getX(), piglin.getY(),
                bastionPos.getZ()).subtract(currentVec).normalize().multiply(2, 2, 2));
        int distance = (int) Math.round(currentVec.distanceTo(finalVec)) * 20;

        for (int i = 0; i < distance; i++) {
            float x = (float) (((finalVec.x - currentVec.x) * i / distance) + currentVec.x);
            float z = (float) (((finalVec.z - currentVec.z) * i / distance) + currentVec.z);

            serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(new Color(255, 240, 150), 0.2F - i * 0.00375F, 1, 0.99F),
                    x, piglin.getY() + (piglin.getBbHeight() / 1.75F), z, 1, 0F, 0F, 0F, 0);
        }

        for (int i = 0; i < 2; i++) {
            float angle = (0.02F * (piglin.tickCount * 3 + i * 160));
            double extraX = (double) (0.75F * Mth.sin((float) (Math.PI + angle))) + piglin.getX();
            double extraZ = (double) (0.75F * Mth.cos(angle)) + piglin.getZ();

            serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(new Color(255, 240, 150), 0.2F, 30, 0.95F),
                    extraX, piglin.getY() + (piglin.getBbHeight() / 1.75F), extraZ, 1, 0F, 0F, 0F, 0);
        }
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BASTION_RING.get());

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            LivingEntity entity = event.getEntity();

            if (entity instanceof ZombifiedPiglin)
                relic.spreadRelicExperience(player, stack, 1);

            if (entity instanceof Piglin)
                relic.spreadRelicExperience(player, stack, 5);

            if (entity instanceof PiglinBrute)
                relic.spreadRelicExperience(player, stack, 10);
        }
    }
}