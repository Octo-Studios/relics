package it.hurts.sskirillss.relics.utils;

import com.google.common.collect.Lists;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EntityUtils {
    public static void moveTowardsPosition(Entity entity, Vec3 targetPos, double speed) {
        Vec3 motion = targetPos.subtract(entity.position()).normalize().scale(speed);

        entity.setDeltaMovement(motion.x, motion.y, motion.z);
    }

    public static int getSlotWithItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i)
            if (player.getInventory().getItem(i).getItem() == item)
                return i;

        return -1;
    }

    public static List<Integer> getSlotsWithItem(Player player, Item item) {
        List<Integer> list = Lists.newArrayList();

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i)
            if (player.getInventory().getItem(i).getItem() == item) {
                list.add(i);

                if (i == player.getInventory().getContainerSize())
                    return list;
            }

        return list;
    }

    public static List<ItemStack> findItemsInInventory(Player player, Item item) {
        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            var stack = player.getInventory().getItem(i);

            if (stack.getItem() == item)
                list.add(stack);
        }

        return list;
    }

    public static void addItem(Player player, ItemStack stack) {
        if (player.addItem(stack))
            return;

        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);

        drop.setDeltaMovement(
                MathUtils.randomFloat(random) * 0.15F,
                0.1F + random.nextFloat() * 0.2F,
                MathUtils.randomFloat(random) * 0.15F
        );
        drop.setPickUpDelay(20);

        level.addFreshEntity(drop);
    }

    public static EntityHitResult rayTraceEntity(Entity shooter, Predicate<? super Entity> filter, double distance) {
        Level world = shooter.level();

        Vec3 startVec = shooter.getEyePosition(1.0F);
        Vec3 endVec = shooter.getEyePosition(1.0F).add(shooter.getViewVector(1.0F).scale(distance));

        double d0 = distance * distance;

        Entity entity = null;
        Vec3 vector3d = null;

        for (Entity entity1 : world.getEntities(shooter, shooter.getBoundingBox()
                .expandTowards(shooter.getViewVector(1.0F).scale(distance * distance)).inflate(1.0D), filter)) {
            AABB axisalignedbb = entity1.getBoundingBox().inflate(entity1.getPickRadius());
            Optional<Vec3> optional = axisalignedbb.clip(startVec, endVec);

            if (axisalignedbb.contains(startVec)) {
                if (d0 >= 0.0D) {
                    entity = entity1;
                    vector3d = optional.orElse(startVec);

                    d0 = 0.0D;
                }
            } else if (optional.isPresent()) {
                Vec3 vector3d1 = optional.get();

                double d1 = startVec.distanceToSqr(vector3d1);

                if (d1 < d0 || d0 == 0.0D) {
                    if (entity1.getRootVehicle() == shooter.getRootVehicle() && !entity1.canRiderInteract()) {
                        if (d0 == 0.0D) {
                            entity = entity1;
                            vector3d = vector3d1;
                        }
                    } else {
                        entity = entity1;
                        vector3d = vector3d1;

                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity, vector3d);
    }

    public static ResourceLocation getAttributeId(ItemStack stack, Attribute attribute) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "_" + BuiltInRegistries.ATTRIBUTE.getKey(attribute).getPath());
    }

    public static boolean hasAttribute(LivingEntity entity, Holder<Attribute> attributeHolder, ResourceLocation id) {
        var instance = entity.getAttribute(attributeHolder);

        return instance != null && instance.hasModifier(id);
    }

    public static boolean hasAttribute(LivingEntity entity, ItemStack stack, Holder<Attribute> attributeHolder) {
        return hasAttribute(entity, attributeHolder,  getAttributeId(stack, attributeHolder.value()));
    }

    public static void applyAttribute(LivingEntity entity, Holder<Attribute> attributeHolder, float value, AttributeModifier.Operation operation, ResourceLocation id) {
        var instance = entity.getAttribute(attributeHolder);

        if (hasAttribute(entity, attributeHolder, id))
            return;

        instance.addTransientModifier(new AttributeModifier(id, value, operation));
    }

    public static void applyAttribute(LivingEntity entity, ItemStack stack, Holder<Attribute> attributeHolder, float value, AttributeModifier.Operation operation) {
        applyAttribute(entity, attributeHolder, value, operation, getAttributeId(stack, attributeHolder.value()));
    }

    public static void removeAttribute(LivingEntity entity, Holder<Attribute> attributeHolder, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation, ResourceLocation id) {
        var instance = entity.getAttribute(attributeHolder);

        if (!hasAttribute(entity, attributeHolder, id))
            return;

        instance.removeModifier(new AttributeModifier(id, instance.getValue(), operation));
    }

    public static void removeAttribute(LivingEntity entity, ItemStack stack, Holder<Attribute> attributeHolder, AttributeModifier.Operation operation) {
        removeAttribute(entity, attributeHolder, operation, getAttributeId(stack, attributeHolder.value()));
    }

    public static void resetAttribute(LivingEntity entity, Holder<Attribute> attributeHolder, float value, AttributeModifier.Operation operation, ResourceLocation id) {
        var attribute = entity.getAttribute(attributeHolder);

        if (attribute == null || attribute.getValue() != value) {
            EntityUtils.removeAttribute(entity, attributeHolder, operation, id);
            EntityUtils.applyAttribute(entity, attributeHolder, value, operation, id);
        }
    }

    public static void resetAttribute(LivingEntity entity, ItemStack stack, Holder<Attribute> attributeHolder, float value, AttributeModifier.Operation operation) {
        resetAttribute(entity, attributeHolder, value, operation, getAttributeId(stack, attributeHolder.value()));
    }

    public static ItemStack findEquippedCurio(Entity entity, Item item) {
        if (!(entity instanceof Player player))
            return ItemStack.EMPTY;

        return CuriosApi.getCuriosInventory(player)
                .map(inventory -> {
                    var equipped = inventory.getEquippedCurios();

                    for (var i = 0; i < equipped.getSlots(); i++) {
                        var stack = equipped.getStackInSlot(i);

                        if (!stack.isEmpty() && stack.getItem() == item)
                            return stack;
                    }

                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    public static List<ItemStack> findEquippedCurios(Entity entity, Item item) {
        return EntityUtils.findEquippedCurios(entity, item, (stack) -> true);
    }

    public static List<ItemStack> findEquippedCurios(Entity entity, Item item, Predicate<ItemStack> predicate) {
        if (!(entity instanceof LivingEntity livingEntity))
            return List.of();

        return CuriosApi.getCuriosInventory(livingEntity)
                .map(inventory -> {
                    var equipped = inventory.getEquippedCurios();
                    List<ItemStack> result = new ArrayList<>();

                    for (var i = 0; i < equipped.getSlots(); i++) {
                        var stack = equipped.getStackInSlot(i);

                        if (!stack.isEmpty() && stack.getItem() == item && predicate.test(stack))
                            result.add(stack);
                    }

                    return result;
                })
                .orElse(List.of());
    }

    public static long getExperienceForLevel(int level) {
        return level >= 30 ? 112 + (level - 30) * 9L : level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2L;
    }

    public static long getTotalExperienceForLevel(int level) {
        long result = 0;

        for (int i = 0; i < level; i++)
            result += getExperienceForLevel(i);

        return result;
    }

    public static long getPlayerTotalExperience(Player player) {
        long totalExperience = 0;

        for (int level = 0; level < player.experienceLevel; level++)
            totalExperience += getExperienceForLevel(level);

        totalExperience += Math.round(player.experienceProgress * getExperienceForLevel(player.experienceLevel));

        return totalExperience;
    }

    // Blame Mojang, not me!!!
    public static double getLevelFromTotalExperience(long totalXP) {
        if (totalXP <= 0)
            return 0D;

        if (totalXP < 315) {
            double calculatedLevel = (-6 + Math.sqrt(36 + 4 * totalXP)) / 2.0;

            int floorLevel = (int) Math.floor(calculatedLevel);

            double xpAtLevel = floorLevel * floorLevel + 6 * floorLevel;
            double xpNeededForNextLevel = 7 + 2 * floorLevel;
            double progressWithinLevel = (totalXP - xpAtLevel) / xpNeededForNextLevel;

            return MathUtils.round(floorLevel + progressWithinLevel, 1);
        } else if (totalXP < 1395) {
            double xpOffset = totalXP - 315;
            double discriminant = 34.5 * 34.5 + 4 * 2.5 * xpOffset;
            double levelOffset = (-34.5 + Math.sqrt(discriminant)) / (2 * 2.5);
            double calculatedLevel = 15 + levelOffset;

            int floorLevel = (int) Math.floor(calculatedLevel);
            int offsetInt = floorLevel - 15;
            long xpAtLevel = 315 + 37L * offsetInt + (int) (2.5 * offsetInt * (offsetInt - 1));
            long xpNeededForNextLevel = 37 + 5L * offsetInt;

            double progressWithinLevel = (totalXP - xpAtLevel) / (double) xpNeededForNextLevel;

            return MathUtils.round(floorLevel + progressWithinLevel, 1);
        } else {
            double xpOffset = totalXP - 1395;
            double levelOffset = (-107.5 + Math.sqrt(107.5 * 107.5 + 18 * xpOffset)) / 9.0;
            double calculatedLevel = 30 + levelOffset;

            int floorLevel = (int) Math.floor(calculatedLevel);
            int offsetInt = floorLevel - 30;
            long xpAtLevel = 1395 + 112L * offsetInt + (long) (4.5 * offsetInt * (offsetInt - 1));
            long xpNeededForNextLevel = 112 + 9L * offsetInt;

            double progressWithinLevel = (totalXP - xpAtLevel) / (double) xpNeededForNextLevel;

            return MathUtils.round(floorLevel + progressWithinLevel, 1);
        }
    }

    public static double calculateExperienceLevelLoss(Player player, long experience) {
        long totalExperience = getPlayerTotalExperience(player);
        long targetTotalExperience = Math.max(0, totalExperience - experience);

        double currentLevel = getLevelFromTotalExperience(totalExperience);
        double newLevel = getLevelFromTotalExperience(targetTotalExperience);

        return MathUtils.round(currentLevel - newLevel, 1);
    }

    public static boolean isAlliedTo(@Nullable Entity source, @Nullable Entity target) {
        return (source == null || target == null) || (source.isAlliedTo(target) || target.isAlliedTo(source)) || (target.getUUID().equals(source.getUUID()))
                || ((target instanceof OwnableEntity ownableTarget && ownableTarget.getOwnerUUID() != null && ownableTarget.getOwnerUUID().equals(source.getUUID()))
                || (source instanceof OwnableEntity ownableSource && ownableSource.getOwnerUUID() != null && ownableSource.getOwnerUUID().equals(target.getUUID())));
    }

    public static boolean hurt(LivingEntity entity, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity sourceEntity && isAlliedTo(sourceEntity, entity))
            return false;

        return entity.hurt(source, amount);
    }

    public static List<ItemStack> getEquippedRelics(LivingEntity entity) {
        List<ItemStack> items = new ArrayList<>();

        if (!(entity instanceof Player player))
            return items;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof IRelicItem))
                continue;

            items.add(stack);
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (int i = 0; i < handler.getEquippedCurios().getSlots(); i++) {
                ItemStack stack = handler.getEquippedCurios().getStackInSlot(i);

                if (!(stack.getItem() instanceof IRelicItem))
                    continue;

                items.add(stack);
            }
        });

        return items;
    }

    public static <T extends LivingEntity> Stream<T> gatherPotentialTargets(Entity seeker, Class<T> type, double radius) {
        return seeker.getCommandSenderWorld().getEntitiesOfClass(type, seeker.getBoundingBox().inflate(radius)).stream()
                .filter(entity -> !(entity instanceof ArmorStand)
                        && !entity.isDeadOrDying()
                        && entity.hasLineOfSight(seeker)
                        && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
                .sorted(Comparator.comparingInt((T entity) -> (entity instanceof Player || entity instanceof Monster) ? 0 : (entity instanceof NeutralMob ? 1 : 2))
                        .thenComparing(entity -> seeker.position().distanceTo(entity.position())));
    }
}
