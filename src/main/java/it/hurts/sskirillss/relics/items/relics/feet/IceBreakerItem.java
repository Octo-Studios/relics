package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.events.common.LivingSlippingEvent;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.entities.ShockwaveEntity;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastPredicate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

public class IceBreakerItem extends RelicItem {
    public static final String TAG_FALLING_POINT = "point";

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("sustainability")
                                .stat(StatData.builder("modifier")
                                        .initialValue(0.75, 0.5D)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.05D)
                                        .formatValue(value -> (int) (MathUtils.round(1 - value, 1) * 100))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("impact")
                                .maxLevel(10)
                                .active(CastType.INSTANTANEOUS, CastPredicate.builder()
                                        .predicate("falling", data -> {
                                            Player player = data.getPlayer();

                                            return !(player.isOnGround() || player.isSpectator());
                                        })
                                        .build())
                                .stat(StatData.builder("size")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.3D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(StyleData.builder()
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.COLD)
                        .build())
                .build();
    }

    @Override
    public RelicAttributeModifier getAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(Attributes.KNOCKBACK_RESISTANCE, (float) getAbilityValue(stack, "sustainability", "modifier")))
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("impact")) {
            NBTUtils.setString(stack, TAG_FALLING_POINT, NBTUtils.writePosition(player.position()));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        Vec3 position = NBTUtils.parsePosition(NBTUtils.getString(stack, TAG_FALLING_POINT, ""));

        if (position == null)
            return;

        if (!player.isOnGround()) {
            Vec3 motion = player.getDeltaMovement();

            if (player.isOnGround() || player.isSpectator()) {
                NBTUtils.clearTag(stack, TAG_FALLING_POINT);

                return;
            }

            player.stopFallFlying();
            player.getAbilities().flying = false;

            player.fallDistance = 0F;

            player.setDeltaMovement(motion.x(), Math.min(-0.01F, motion.y() * 1.075F), motion.z());
        } else {
            Level level = player.getCommandSenderWorld();

            double distance = (position.y() + Math.abs(level.getMinBuildHeight())) - (player.getY() + Math.abs(level.getMinBuildHeight()));

            NBTUtils.clearTag(stack, TAG_FALLING_POINT);

            if (distance <= 0)
                return;

            addExperience(player, stack, (int) Math.min(10, Math.round(distance / 3F)));

            ShockwaveEntity shockwave = new ShockwaveEntity(level,
                    (int) Math.round(Math.min(getAbilityValue(stack, "impact", "size"), distance * 0.25D)),
                    (float) getAbilityValue(stack, "impact", "damage"));

            BlockPos pos = player.getOnPos();

            shockwave.setOwner(player);
            shockwave.setPos(pos.getX(), pos.getY(), pos.getZ());

            level.addFreshEntity(shockwave);

            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.MASTER, 0.75F, 1.0F);
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (stack.getItem() == newStack.getItem())
            return;

        NBTUtils.clearTag(stack, TAG_FALLING_POINT);
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingSlipping(LivingSlippingEvent event) {
            if (event.getFriction() <= 0.6F || !(event.getEntity() instanceof Player player)
                    || player.isInWater() || player.isInLava())
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ICE_BREAKER.get());

            if (stack.isEmpty())
                return;

            event.setFriction(0.6F);
        }
    }
}