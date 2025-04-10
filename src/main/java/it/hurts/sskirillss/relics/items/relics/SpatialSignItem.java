//package it.hurts.sskirillss.relics.items.relics;
//
//import it.hurts.sskirillss.relics.init.EffectRegistry;
//import it.hurts.sskirillss.relics.init.ItemRegistry;
//import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
//import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
//
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
//import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
//import it.hurts.sskirillss.relics.items.relics.base.data.style.misc.Backgrounds;
//import it.hurts.sskirillss.relics.utils.EntityUtils;
//import it.hurts.sskirillss.relics.utils.MathUtils;
//import it.hurts.sskirillss.relics.utils.NBTUtils;
//import it.hurts.sskirillss.relics.utils.ParticleUtils;
//import net.minecraft.network.chat.Component;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
//import top.theillusivec4.curios.api.SlotContext;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SpatialSignItem extends RelicItem {
//    public static final String TAG_POSITION = "pos";
//    public static final String TAG_TIME = "time";
//    public static final String TAG_WORLD = "world";
//
//    @Override
//    public RelicData constructDefaultRelicData() {
//        return RelicData.builder()
//                .abilities(AbilitiesData.builder()
//                        .ability(AbilityData.builder("seal")
//                                .stat(StatData.builder("time")
//                                        .initialValue(2D, 4D)
//                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
//                                        .formatValue(value -> MathUtils.round(value, 1))
//                                        .build())
//                                .build())
//                        .build())
//                .leveling(new LevelingData(100, 10, 200))
//                .style(StyleData.builder()
//                        .background(Backgrounds.CAVE)
//                        .build())
//                .loot(LootData.builder()
//                        .entry(LootCollections.ANTHROPOGENIC)
//                        .build())
//                .build();
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
//        ItemStack stack = playerIn.getItemInHand(handIn);
//
//        if (NBTUtils.getList(stack, TAG_POSITION, String.class).size() < 2 || worldIn.isClientSide())
//            return InteractionResultHolder.fail(stack);
//
//        if (NBTUtils.getInt(stack, TAG_TIME, 0) > 0) {
//            NBTUtils.setInt(stack, TAG_TIME, 0);
//        } else {
//            NBTUtils.setInt(stack, TAG_TIME, (int) Math.round(getAbilityValue(stack, "seal", "time")));
//
//            worldIn.playSound(null, playerIn.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1F, 2F);
//        }
//
//        return InteractionResultHolder.success(stack);
//    }
//
//    @Override
//    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
//        if (!(entityIn instanceof Player player))
//            return;
//
//        RandomSource random = worldIn.getRandom();
//
//        int time = NBTUtils.getInt(stack, TAG_TIME, -1);
//
//        if (player.level().isClientSide())
//            player.displayClientMessage(Component.literal(" " + time), true);
//
//        List<String> positions = NBTUtils.getList(stack, TAG_POSITION, String.class);
//
//        if (time >= 0) {
//            if (player.isPassenger())
//                player.stopRiding();
//
//            if (player.tickCount % 20 == 0 && !worldIn.isClientSide()) {
//                NBTUtils.setInt(stack, TAG_TIME, --time);
//
//                spreadExperience(player, stack, 1);
//            }
//
//            if (time <= 0) {
//                worldIn.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1F, 0.5F);
//
//                player.getCooldowns().addCooldown(this, (int) (Math.ceil(getAbilityValue(stack, "seal", "time")) * 20));
//
//                NBTUtils.setInt(stack, TAG_TIME, -1);
//
//                return;
//            }
//
//            if (!positions.isEmpty()) {
//                Vec3 target = NBTUtils.parsePosition(positions.get(positions.size() - 1));
//
//                double distance = player.position().distanceTo(target);
//
//                if (distance > 2) {
//                    if (distance > 64) {
//                        NBTUtils.setInt(stack, TAG_TIME, 0);
//                        NBTUtils.setList(stack, TAG_POSITION, new ArrayList<>());
//                    }
//
//                    for (int i = 0; i < 10; i++)
//                        worldIn.addParticle(ParticleUtils.constructSimpleSpark(new Color(255 - random.nextInt(100), 0, 255 - random.nextInt(50)),
//                                        0.1F + random.nextFloat() * 0.25F, 100, 0.96F), player.getX(),
//                                player.getY() + random.nextFloat() * player.getBbHeight(), player.getZ(), 0F, random.nextFloat() * 0.1F, 0F);
//
//                    player.addEffect(new MobEffectInstance(EffectRegistry.VANISHING, 5, 0, false, false));
//
//                    player.setDeltaMovement(target.add(0, 1, 0).subtract(player.position()).normalize().scale(0.75F));
//
//                    player.fallDistance = 0;
//                    player.noPhysics = true;
//                    player.clearFire();
//                } else {
//                    positions.remove(positions.size() - 1);
//
//                    NBTUtils.setList(stack, TAG_POSITION, positions);
//                }
//            } else {
//                NBTUtils.setInt(stack, TAG_TIME, 0);
//            }
//        }
//
//        if (player.tickCount % 20 == 0) {
//            if (positions.size() >= 5 * getAbilityValue(stack, "seal", "time"))
//                positions.remove(0);
//
//            if (positions.isEmpty() || player.position().distanceTo(NBTUtils.parsePosition(positions.get(positions.size() - 1))) >= 2) {
//                if (worldIn.dimension().location().toString().equals(NBTUtils.getString(stack, TAG_WORLD, ""))) {
//                    positions.add(NBTUtils.writePosition(player.position()));
//                } else {
//                    positions.clear();
//
//                    NBTUtils.setString(stack, TAG_WORLD, worldIn.dimension().location().toString());
//                }
//            }
//
//            NBTUtils.setList(stack, TAG_POSITION, positions);
//        }
//    }
//
//    @Override
//    public int getMaxDamage(ItemStack stack) {
//        return 1;
//    }
//
//    @Override
//    public boolean isFoil(ItemStack stack) {
//        return NBTUtils.getInt(stack, TAG_TIME, 0) > 0;
//    }
//
//    @Override
//    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
//        return false;
//    }
//
//    @EventBusSubscriber
//    public static class Events {
//        @SubscribeEvent
//        public static void onLivingAttack(LivingAttackEvent event) {
//            LivingEntity entity = event.getEntity();
//
//            if (!(entity instanceof Player player))
//                return;
//
//            for (int slot : EntityUtils.getSlotsWithItem(player, ItemRegistry.SPATIAL_SIGN.get()).stream()
//                    .filter(slot -> slot != -1)
//                    .toList()) {
//                ItemStack stack = player.getInventory().getItem(slot);
//
//                if (stack.getItem() instanceof SpatialSignItem && NBTUtils.getInt(stack, TAG_TIME, 0) > 0) {
//                    event.setCanceled(true);
//
//                    return;
//                }
//            }
//        }
//    }
//}