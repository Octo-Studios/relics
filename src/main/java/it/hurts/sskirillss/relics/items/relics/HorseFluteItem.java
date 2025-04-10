//package it.hurts.sskirillss.relics.items.relics;
//
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
//import it.hurts.sskirillss.relics.utils.MathUtils;
//import it.hurts.sskirillss.relics.utils.NBTUtils;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.animal.horse.AbstractHorse;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.context.UseOnContext;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.Vec3;
//import top.theillusivec4.curios.api.SlotContext;
//
//import javax.annotation.Nullable;
//import java.util.Optional;
//import java.util.UUID;
//
//public class HorseFluteItem extends RelicItem {
//    public static final String TAG_ENTITY = "entity";
//    private static final String TAG_UUID = "uuid";
//
//    @Override
//    public RelicData constructDefaultRelicData() {
//        return RelicData.builder()
//                .abilities(AbilitiesData.builder()
//                        .ability(AbilityData.builder("paddock")
//                                .maxLevel(0)
//                                .build())
//                        .ability(AbilityData.builder("heal")
//                                .requiredLevel(5)
//                                .stat(StatData.builder("amount")
//                                        .initialValue(0.01D, 0.1D)
//                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 1D)
//                                        .formatValue(value -> MathUtils.round(value, 2))
//                                        .build())
//                                .build())
//                        .build())
//                .leveling(new LevelingData(100, 10, 100))
//                .style(StyleData.builder()
//                        .background(Backgrounds.PLAINS)
//                        .build())
//                .loot(LootData.builder()
//                        .entry(LootCollections.VILLAGE)
//                        .entry(LootCollections.ANTHROPOGENIC)
//                        .build())
//                .build();
//    }
//
//    @Override
//    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
//        if (!(entity instanceof AbstractHorse horse))
//            return InteractionResult.FAIL;
//
//        CompoundTag nbt = NBTUtils.getCompound(stack, TAG_ENTITY, new CompoundTag());
//
//        if (!nbt.isEmpty()) {
//            releaseHorse(stack, player);
//            catchHorse(horse, player, stack);
//
//            return InteractionResult.SUCCESS;
//        }
//
//        catchHorse(horse, player, stack);
//
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        Player player = context.getPlayer();
//        ItemStack stack = context.getItemInHand();
//        CompoundTag nbt = NBTUtils.getCompound(stack, TAG_ENTITY, new CompoundTag());
//
//        if (player == null)
//            return InteractionResult.FAIL;
//
//        if (nbt.isEmpty()) {
//            if (player.isShiftKeyDown())
//                NBTUtils.clearTag(stack, TAG_UUID);
//            else
//                catchHorse(stack, player);
//
//            return InteractionResult.SUCCESS;
//        }
//
//        releaseHorse(stack, player);
//
//        if (player.isShiftKeyDown())
//            NBTUtils.clearTag(stack, TAG_UUID);
//
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
//        ItemStack stack = player.getItemInHand(hand);
//        CompoundTag nbt = NBTUtils.getCompound(stack, TAG_ENTITY, new CompoundTag());
//
//        if (nbt.isEmpty()) {
//            if (player.isShiftKeyDown())
//                NBTUtils.clearTag(stack, TAG_UUID);
//            else
//                catchHorse(stack, player);
//
//            return InteractionResultHolder.success(stack);
//        }
//
//        releaseHorse(stack, player);
//
//        if (player.isShiftKeyDown())
//            NBTUtils.clearTag(stack, TAG_UUID);
//
//        return InteractionResultHolder.success(stack);
//    }
//
//    @Override
//    public void inventoryTick(ItemStack stack, Level level, Entity entityIn, int itemSlot, boolean isSelected) {
//        if (entityIn.tickCount % 20 == 0) {
//            AbstractHorse horse = decodeHorseData(stack, level);
//            if (horse != null) {
//                if (canUseAbility(stack, "heal")) {
//                    horse.heal((float) getAbilityValue(stack, "heal", "amount"));
//
//                    CompoundTag nbt = new CompoundTag();
//
//                    horse.save(nbt);
//
//                    NBTUtils.getOrCreateTag(stack).put(TAG_ENTITY, nbt);
//                }
//            }
//        }
//
//        AbstractHorse horse = findHorse(level, stack);
//
//        if (horse != null && entityIn.distanceTo(horse) > 16)
//            catchHorse(horse, entityIn, stack);
//    }
//
//    @Nullable
//    private AbstractHorse findHorse(Level world, ItemStack stack) {
//        if (world.isClientSide())
//            return null;
//
//        ServerLevel serverLevel = (ServerLevel) world;
//        String uuid = NBTUtils.getString(stack, TAG_UUID, "");
//
//        if (uuid.isEmpty())
//            return null;
//
//        Entity entity = serverLevel.getEntity(UUID.fromString(uuid));
//
//        return entity instanceof AbstractHorse ? (AbstractHorse) entity : null;
//    }
//
//    public void catchHorse(AbstractHorse horse, Entity entity, ItemStack stack) {
//        if (horse.isDeadOrDying() || (horse.getOwnerUUID() != null && !horse.getOwnerUUID().equals(entity.getUUID()))
//                || (horse.getOwnerUUID() == null && !horse.isTamed()))
//            return;
//
//        Level world = horse.getCommandSenderWorld();
//        CompoundTag nbt = new CompoundTag();
//        Vec3 pos = horse.position();
//
//        if (NBTUtils.getString(stack, TAG_UUID, "").equals(horse.getUUID().toString()) && horse.walkDist > 25) {
//            if (entity instanceof LivingEntity living)
//                spreadExperience(living, stack, Math.round(horse.walkDist / 25));
//
//            horse.walkDist = 0;
//        }
//
//        horse.save(nbt);
//
//        NBTUtils.setCompound(stack, TAG_ENTITY, nbt);
//        NBTUtils.clearTag(stack, TAG_UUID);
//
//        horse.remove(Entity.RemovalReason.KILLED);
//
//        if (!world.isClientSide())
//            ((ServerLevel) world).sendParticles(ParticleTypes.EXPLOSION, pos.x(), pos.y(), pos.z(), 1, 0F, 0F, 0F, 0F);
//
//        world.playSound(null, horse.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.0F);
//    }
//
//    private void catchHorse(ItemStack stack, Entity player) {
//        Level world = player.getCommandSenderWorld();
//
//        AbstractHorse horse = findHorse(world, stack);
//
//        if (horse != null)
//            catchHorse(horse, player, stack);
//    }
//
//    public void releaseHorse(ItemStack stack, Entity player) {
//        Level world = player.getCommandSenderWorld();
//        Vec3 pos = player.position();
//
//        AbstractHorse horse = decodeHorseData(stack, world);
//
//        if (horse == null)
//            return;
//
//        horse.setPos(pos.x(), pos.y(), pos.z());
//        world.addFreshEntity(horse);
//        horse.setDeltaMovement(player.getViewVector(1.0F).normalize());
//        horse.fallDistance = 0F;
//
//        NBTUtils.clearTag(stack, TAG_ENTITY);
//        NBTUtils.setString(stack, TAG_UUID, horse.getUUID().toString());
//
//        if (!world.isClientSide())
//            ((ServerLevel) world).sendParticles(ParticleTypes.EXPLOSION, pos.x(), pos.y(), pos.z(), 1, 0F, 0F, 0F, 0F);
//
//        world.playSound(null, horse.blockPosition(), SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 1.0F, 1.0F);
//    }
//
//    @Nullable
//    private AbstractHorse decodeHorseData(ItemStack stack, Level level) {
//        CompoundTag data = NBTUtils.getOrCreateTag(stack);
//
//        if (data == null)
//            return null;
//
//        Optional<EntityType<?>> type = EntityType.by(data.getCompound(TAG_ENTITY));
//
//        if (type.isEmpty())
//            return null;
//
//        Entity entity = type.get().create(level);
//
//        if (!(entity instanceof AbstractHorse horse))
//            return null;
//
//        horse.load(data.getCompound(TAG_ENTITY));
//
//        return horse;
//    }
//
//    @Override
//    public boolean isFoil(ItemStack stack) {
//        return !NBTUtils.getOrCreateTag(stack).getCompound(TAG_ENTITY).isEmpty();
//    }
//
//    @Override
//    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
//        return false;
//    }
//}