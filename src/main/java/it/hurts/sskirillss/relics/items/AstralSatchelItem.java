package it.hurts.sskirillss.relics.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.client.astral.AstralSpace;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.S2CAstralSatchelMotion;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AstralSatchelItem extends RelicItem {
    public static final int MAX_ENTRIES = 64;
    public static final int MAX_AMOUNT = 1_000_000_000;
    public static final double DEFAULT_RADIUS = 8D;
    public static final double MERGE_RANGE = 5D;
    public static final double REACH_MARGIN = 1.5D;
    public static final double REACH_MULTIPLIER = 2D;
    public static final double DROP_DISTANCE = 6D;
    public static final double DROP_OFFSET = 1D;
    public static final double DROP_HAND_SIDE = 0.35D;
    public static final double DROP_HAND_DROP = 0.3D;
    public static final double MIN_ENTRY_GAP_SQR = 0.36D;
    public static final double PUSH_DISTANCE = 7D;
    public static final double PUSH_RESTITUTION = 0.6D;
    public static final int PUSH_BOUNCES = 3;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("subspace")
                                .initialMaxLevel(10)
                                .rankModifier(3, "immortality")
                                .rankModifier(5, "infinite_stacks")
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(8D, 12D)
                                        .thresholdValue(4D, 48D)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 32D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("storing")
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("stored_items")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("withdrawn_items")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 14, 4).star(1, 8, 6).star(2, 19, 13).star(3, 12, 14).star(4, 3, 15).star(5, 7, 22).star(6, 14, 22).star(7, 7, 28)
                                        .link(7, 5).link(5, 4).link(5, 6).link(5, 3).link(3, 2).link(3, 1).link(3, 0)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.END_LIKE, LootEntries.SCULK)
                        .build())
                .build();
    }

    public AbilityData getAbility(LivingEntity entity, ItemStack stack) {
        return this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("subspace");
    }

    public boolean isRankModifierEnabled(LivingEntity entity, ItemStack stack, String modifier) {
        var ability = this.getAbility(entity, stack);

        return ability.canPlayerUse(entity) && ability.getRankModifierData(modifier).isEnabled();
    }

    public double getRadius(LivingEntity entity, ItemStack stack) {
        return this.getAbility(entity, stack).getStatData("radius").getValue();
    }

    public static ItemStack find(Player player) {
        var inventory = player.getInventory();

        for (var i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);

            if (stack.getItem() instanceof AstralSatchelItem)
                return stack;
        }

        return ItemStack.EMPTY;
    }

    public static boolean canUse(Player player) {
        var stack = find(player);

        return stack.getItem() instanceof AstralSatchelItem relic && relic.getAbility(player, stack).canPlayerUse(player);
    }

    public static double getRadius(Player player) {
        var stack = find(player);

        return stack.getItem() instanceof AstralSatchelItem relic ? relic.getRadius(player, stack) : DEFAULT_RADIUS;
    }

    public static State getState(Player player) {
        return player.getData(RelicsAttachments.ASTRAL_SATCHEL_STATE.get());
    }

    public static boolean isActive(Player player) {
        return getState(player).active;
    }

    public static void setActive(Player player, boolean active) {
        var state = getState(player);

        state.aimed = null;

        if (!active) {
            state.active = false;

            return;
        }

        var satchel = find(player);

        if (!canUse(player))
            return;

        var radius = getRadius(player);

        setEntries(satchel, getEntries(satchel).stream()
                .map(entry -> entry.withPosition(clamp(entry.position(), radius)))
                .toList());

        state.camera = Vec3.ZERO;
        state.active = true;
    }

    public static Vec3 getCamera(Player player) {
        return getState(player).camera;
    }

    public static void syncCamera(Player player, Vec3 camera) {
        if (!isActive(player) || !Double.isFinite(camera.x) || !Double.isFinite(camera.y) || !Double.isFinite(camera.z))
            return;

        getState(player).camera = clamp(camera, getRadius(player));
    }

    public static void setAimed(Player player, UUID id) {
        if (isActive(player))
            getState(player).aimed = id;
    }

    public static List<Entry> getEntries(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.ASTRAL_SATCHEL_ENTRIES.get(), List.of());
    }

    public static void setEntries(ItemStack stack, List<Entry> entries) {
        stack.set(RelicsDataComponents.ASTRAL_SATCHEL_ENTRIES.get(), List.copyOf(entries));
    }

    public static Vec3 clamp(Vec3 position, double radius) {
        return position.length() > radius - 1D ? position.normalize().scale(radius - 1D) : position;
    }

    public static int getStackLimit(Player player, ItemStack stack) {
        var satchel = find(player);

        if (satchel.getItem() instanceof AstralSatchelItem relic && relic.isRankModifierEnabled(player, satchel, "infinite_stacks"))
            return MAX_AMOUNT;

        return stack.getMaxStackSize();
    }

    private static int indexOf(List<Entry> entries, UUID id) {
        for (var i = 0; i < entries.size(); i++) {
            if (entries.get(i).id().equals(id))
                return i;
        }

        return -1;
    }

    private static boolean isReachable(Player player, Entry entry) {
        return getCamera(player).distanceTo(entry.position()) <= player.entityInteractionRange() * REACH_MULTIPLIER + REACH_MARGIN;
    }

    private static void sendMotion(Player player, UUID id, List<Vec3> path, boolean thrown) {
        if (player instanceof ServerPlayer serverPlayer)
            NetworkHandler.sendToClient(new S2CAstralSatchelMotion(id, path, thrown), serverPlayer);
    }

    private static List<Vec3> getDropPath(Player player, List<Entry> entries, double radius) {
        var look = player.getLookAngle();
        var camera = getCamera(player);
        var side = look.cross(new Vec3(0D, 1D, 0D)).normalize();
        var path = new ArrayList<Vec3>();

        path.add(camera.add(look.scale(DROP_OFFSET)).add(side.scale(DROP_HAND_SIDE)).add(0D, -DROP_HAND_DROP, 0D));
        path.addAll(trajectory(clamp(camera.add(look.scale(DROP_OFFSET)), radius), look, radius - 1D, DROP_DISTANCE, PUSH_BOUNCES));

        var base = path.getLast();
        var point = base;

        for (var attempt = 0; attempt < 12; attempt++) {
            var candidate = point;

            if (entries.stream().noneMatch(entry -> entry.position().distanceToSqr(candidate) < MIN_ENTRY_GAP_SQR))
                break;

            var random = player.getRandom();

            point = clamp(base.add(random.nextGaussian() * 0.6D, random.nextGaussian() * 0.6D, random.nextGaussian() * 0.6D), radius);
        }

        path.set(path.size() - 1, point);

        return path;
    }

    public static void merge(Player player, UUID firstId, UUID secondId) {
        var satchel = find(player);

        if (!isActive(player) || !(satchel.getItem() instanceof AstralSatchelItem) || firstId.equals(secondId))
            return;

        var entries = new ArrayList<>(getEntries(satchel));
        var first = indexOf(entries, firstId);
        var second = indexOf(entries, secondId);

        if (first < 0 || second < 0)
            return;

        var a = entries.get(first);
        var b = entries.get(second);

        if (!ItemStack.isSameItemSameComponents(a.stack(), b.stack()) || a.position().distanceTo(b.position()) > MERGE_RANGE + 0.5D)
            return;

        var total = (long) a.amount() + b.amount();

        if (total > getStackLimit(player, a.stack()))
            return;

        entries.set(first, new Entry(a.id(), a.stack(), a.position().add(b.position()).scale(0.5D), (int) total));
        entries.remove(second);

        setEntries(satchel, entries);
    }

    public static boolean deposit(Player player, ItemStack stack) {
        var satchel = find(player);

        if (!(satchel.getItem() instanceof AstralSatchelItem relic) || stack.isEmpty() || stack.getItem() instanceof AstralSatchelItem)
            return false;

        var entries = new ArrayList<>(getEntries(satchel));
        var moved = stack.getCount();
        var aimedId = getState(player).aimed;
        var aimed = aimedId == null ? -1 : indexOf(entries, aimedId);

        if (aimed >= 0 && isReachable(player, entries.get(aimed)) && ItemStack.isSameItemSameComponents(entries.get(aimed).stack(), stack)) {
            var entry = entries.get(aimed);
            var transfer = Math.min(moved, getStackLimit(player, stack) - entry.amount());

            if (transfer > 0) {
                entries.set(aimed, entry.withAmount(entry.amount() + transfer));

                moved -= transfer;
            }
        }

        Entry thrown = null;
        List<Vec3> path = List.of();

        if (moved > 0) {
            if (entries.size() >= MAX_ENTRIES)
                return false;

            path = getDropPath(player, entries, relic.getRadius(player, satchel));
            thrown = Entry.create(stack, path.getLast(), moved);

            entries.add(thrown);
        }

        setEntries(satchel, entries);

        if (thrown != null)
            sendMotion(player, thrown.id(), path, true);

        var ability = relic.getAbility(player, satchel);

        if (ability.canPlayerUse(player)) {
            relic.getRelicData(player, satchel).getLevelingData().addExperience("subspace", "storing", stack.getCount());

            ability.getStatisticData().getMetricData("stored_items").addValue(stack.getCount());
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.4F);

        return true;
    }

    public static void withdraw(Player player, UUID id) {
        var satchel = find(player);

        if (!isActive(player) || !(satchel.getItem() instanceof AstralSatchelItem relic))
            return;

        var entries = new ArrayList<>(getEntries(satchel));
        var index = indexOf(entries, id);

        if (index < 0 || !isReachable(player, entries.get(index)))
            return;

        var entry = entries.get(index);
        var stack = entry.stack().copyWithCount(Math.min(entry.amount(), entry.stack().getMaxStackSize()));
        var requested = stack.getCount();

        player.getInventory().add(stack);

        var withdrawn = requested - stack.getCount();

        if (withdrawn <= 0)
            return;

        if (withdrawn >= entry.amount())
            entries.remove(index);
        else
            entries.set(index, entry.withAmount(entry.amount() - withdrawn));

        setEntries(satchel, entries);

        var ability = relic.getAbility(player, satchel);

        if (ability.canPlayerUse(player))
            ability.getStatisticData().getMetricData("withdrawn_items").addValue(withdrawn);

        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.8F);
    }

    public static List<Vec3> trajectory(Vec3 origin, Vec3 heading, double limit) {
        return trajectory(origin, heading, limit, PUSH_DISTANCE, PUSH_BOUNCES);
    }

    public static List<Vec3> trajectory(Vec3 origin, Vec3 heading, double limit, double distance, int bounces) {
        var points = new ArrayList<Vec3>();
        var position = origin;
        var direction = heading.normalize();
        var remaining = distance;

        points.add(position);

        for (var bounce = 0; bounce <= bounces && remaining > 0.05D; bounce++) {
            var along = position.dot(direction);
            var exit = -along + Math.sqrt(Math.max(along * along - (position.lengthSqr() - limit * limit), 0D));

            if (exit >= remaining) {
                points.add(position.add(direction.scale(remaining)));

                break;
            }

            position = position.add(direction.scale(exit));

            points.add(position);

            var normal = position.normalize();

            direction = direction.subtract(normal.scale(2D * direction.dot(normal)));
            remaining = (remaining - exit) * PUSH_RESTITUTION;
        }

        return points;
    }

    public static void push(Player player, UUID id, Vec3 direction) {
        var satchel = find(player);

        if (!isActive(player) || !(satchel.getItem() instanceof AstralSatchelItem relic) || direction.lengthSqr() < 1.0E-6D)
            return;

        var entries = new ArrayList<>(getEntries(satchel));
        var index = indexOf(entries, id);

        if (index < 0 || !isReachable(player, entries.get(index)))
            return;

        var entry = entries.get(index);
        var path = trajectory(entry.position(), direction, relic.getRadius(player, satchel) - 1D);

        if (path.getLast().distanceToSqr(entry.position()) < 0.01D)
            return;

        entries.set(index, entry.withPosition(path.getLast()));

        setEntries(satchel, entries);

        sendMotion(player, id, path, false);
    }

    public static class State {
        private boolean active;
        private Vec3 camera = Vec3.ZERO;
        private UUID aimed;
    }

    public record Entry(UUID id, ItemStack stack, Vec3 position, int amount) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Entry::id),
                ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack),
                Vec3.CODEC.fieldOf("position").forGetter(Entry::position),
                Codec.intRange(1, MAX_AMOUNT).fieldOf("amount").forGetter(Entry::amount)
        ).apply(instance, Entry::new));

        public Entry {
            stack = stack.copyWithCount(1);
        }

        public static Entry create(ItemStack stack, Vec3 position, int amount) {
            return new Entry(UUID.randomUUID(), stack, position, amount);
        }

        public Entry withPosition(Vec3 position) {
            return new Entry(id, stack, position, amount);
        }

        public Entry withAmount(int amount) {
            return new Entry(id, stack, position, amount);
        }
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            var player = event.getPlayer();

            if (!(player instanceof ServerPlayer) || !isActive(player))
                return;

            var stack = event.getEntity().getItem();

            event.setCanceled(true);

            if (deposit(player, stack))
                return;

            player.getInventory().placeItemBackInInventory(stack.copy());

            if (!(stack.getItem() instanceof AstralSatchelItem))
                player.displayClientMessage(Component.translatable("relics.message.astral_satchel.full"), true);
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            var player = event.getEntity();

            if (!(player instanceof ServerPlayer) || player.tickCount % 2 != 0 || !isActive(player))
                return;

            var satchel = find(player);

            if (!(satchel.getItem() instanceof AstralSatchelItem relic)) {
                setActive(player, false);

                return;
            }

            if (relic.isRankModifierEnabled(player, satchel, "immortality"))
                player.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, 5, 0, false, false, true));
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            setActive(event.getEntity(), false);
        }
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (!event.getLevel().isClientSide() || !(event.getItemStack().getItem() instanceof AstralSatchelItem) || !canUse(event.getEntity()))
                return;

            AstralSpace.enter();
        }
    }
}
