package it.hurts.sskirillss.relics.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
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
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SQuickStoreAstralSatchelDrop;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SQuickStoreAstralSatchelSlot;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.S2CAstralSatchelMotion;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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
    public static final double DROP_PICK_RADIUS = 0.3D;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("subspace")
                                .initialMaxLevel(10)
                                .rankModifier(1, "quick_storage")
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
        var slot = getState(player).slot;

        if (slot >= 0) {
            var stack = inventory.getItem(slot);

            return stack.getItem() instanceof AstralSatchelItem ? stack : ItemStack.EMPTY;
        }

        for (var i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);

            if (stack.getItem() instanceof AstralSatchelItem)
                return stack;
        }

        return ItemStack.EMPTY;
    }

    public static boolean canUse(Player player, ItemStack stack) {
        return stack.getItem() instanceof AstralSatchelItem relic && relic.getAbility(player, stack).canPlayerUse(player);
    }

    public static int getSlot(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
    }

    public static void setSlot(Player player, int slot) {
        getState(player).slot = slot;
    }

    public static boolean isLocked(Player player, int slot) {
        return slot >= 0 && getState(player).slot == slot;
    }

    public static boolean isLocked(Player player, Slot slot) {
        return slot.container == player.getInventory() && isLocked(player, slot.getContainerSlot());
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
        setActive(player, active, -1);
    }

    public static void setActive(Player player, boolean active, int slot) {
        var state = getState(player);

        state.aimed = null;

        if (!active) {
            state.active = false;
            state.slot = -1;

            return;
        }

        if (slot < 0 || slot >= player.getInventory().getContainerSize())
            return;

        var satchel = player.getInventory().getItem(slot);

        if (!canUse(player, satchel))
            return;

        state.slot = slot;

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
        return getStackLimit(player, find(player), stack);
    }

    public static int getStackLimit(Player player, ItemStack satchel, ItemStack stack) {
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

        if (!(satchel.getItem() instanceof AstralSatchelItem relic) || stack.isEmpty())
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

        reward(player, satchel, relic, stack.getCount());

        return true;
    }

    private static void reward(Player player, ItemStack satchel, AstralSatchelItem relic, int count) {
        var ability = relic.getAbility(player, satchel);

        if (ability.canPlayerUse(player)) {
            relic.getRelicData(player, satchel).getLevelingData().addExperience("subspace", "storing", count);

            ability.getStatisticData().getMetricData("stored_items").addValue(count);
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    public static boolean canQuickStore(Player player, ItemStack satchel) {
        return satchel.getItem() instanceof AstralSatchelItem relic && relic.isRankModifierEnabled(player, satchel, "quick_storage");
    }

    public static boolean quickStore(Player player, ItemStack satchel, ItemStack stack) {
        if (stack.isEmpty() || stack == satchel || !canQuickStore(player, satchel))
            return false;

        var relic = (AstralSatchelItem) satchel.getItem();
        var entries = new ArrayList<>(getEntries(satchel));
        var limit = getStackLimit(player, satchel, stack);
        var radius = relic.getRadius(player, satchel);
        var random = player.getRandom();
        var remaining = stack.getCount();

        for (var i = 0; i < entries.size() && remaining > 0; i++) {
            var entry = entries.get(i);
            var transfer = Math.min(remaining, limit - entry.amount());

            if (transfer <= 0 || !ItemStack.isSameItemSameComponents(entry.stack(), stack))
                continue;

            entries.set(i, entry.withAmount(entry.amount() + transfer));

            remaining -= transfer;
        }

        while (remaining > 0 && entries.size() < MAX_ENTRIES) {
            var amount = Math.min(remaining, limit);
            var position = clamp(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(radius * 0.3D), radius);

            entries.add(Entry.create(stack, position, amount));

            remaining -= amount;
        }

        var moved = stack.getCount() - remaining;

        if (moved <= 0) {
            player.displayClientMessage(Component.translatable("relics.message.astral_satchel.full"), true);

            return false;
        }

        stack.shrink(moved);

        setEntries(satchel, entries);

        reward(player, satchel, relic, moved);

        return true;
    }

    public static boolean quickClick(Player player, AbstractContainerMenu menu, Slot slot, ClickAction action, boolean apply) {
        var carried = menu.getCarried();
        var stored = slot.getItem();
        var satchel = ItemStack.EMPTY;
        var source = ItemStack.EMPTY;

        if (action == ClickAction.SECONDARY && carried.getItem() instanceof AstralSatchelItem && !stored.isEmpty()) {
            satchel = carried;
            source = stored;
        } else if (action == ClickAction.PRIMARY && stored.getItem() instanceof AstralSatchelItem && !carried.isEmpty()) {
            satchel = stored;
            source = carried;
        }

        if (source == satchel || isLocked(player, slot) || !canQuickStore(player, satchel))
            return false;

        if (apply) {
            quickStore(player, satchel, source);

            slot.setChanged();
            menu.broadcastChanges();
        }

        return true;
    }

    public static void quickStoreDrop(Player player, int id, InteractionHand hand) {
        var satchel = player.getItemInHand(hand);

        if (isActive(player) || !canQuickStore(player, satchel) || !(player.level().getEntity(id) instanceof ItemEntity drop) || !drop.isAlive())
            return;

        var reach = player.entityInteractionRange() + 2D;

        if (drop.distanceToSqr(player.getEyePosition()) > reach * reach)
            return;

        var stack = drop.getItem();

        quickStore(player, satchel, stack);

        if (stack.isEmpty())
            drop.discard();
        else
            drop.setItem(stack);
    }

    public static void withdraw(Player player, UUID id, int amount) {
        var satchel = find(player);

        if (!isActive(player) || !(satchel.getItem() instanceof AstralSatchelItem relic))
            return;

        var entries = new ArrayList<>(getEntries(satchel));
        var index = indexOf(entries, id);

        if (index < 0 || !isReachable(player, entries.get(index)))
            return;

        var entry = entries.get(index);
        var limit = entry.stack().getMaxStackSize();
        var wanted = amount < 0 ? entry.amount() : Math.min(entry.amount(), amount == 0 ? limit : amount);
        var withdrawn = 0;

        while (withdrawn < wanted) {
            var chunk = entry.stack().copyWithCount(Math.min(wanted - withdrawn, limit));
            var size = chunk.getCount();

            player.getInventory().add(chunk);

            withdrawn += size - chunk.getCount();

            if (!chunk.isEmpty())
                break;
        }

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
        private int slot = -1;
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

            if (stack.getItem() instanceof AstralSatchelItem) {
                var slot = getState(player).slot;

                if (slot >= 0 && player.getInventory().getItem(slot).isEmpty()) {
                    player.getInventory().setItem(slot, stack.copy());

                    return;
                }
            }

            player.getInventory().placeItemBackInInventory(stack.copy());

            player.displayClientMessage(Component.translatable("relics.message.astral_satchel.full"), true);
        }

        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {
            var player = event.getEntity();

            if (isLocked(player, event.getSlot()) || event.getType() == ClickType.SWAP && isLocked(player, event.getButton())) {
                event.setCanceled(true);

                return;
            }

            if (player.level().isClientSide() || event.getType() != ClickType.PICKUP || !quickClick(player, event.getContainer(), event.getSlot(), event.getAction(), true))
                return;

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (isActive(event.getEntity()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            if (isActive(event.getEntity()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            if (isActive(event.getEntity()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
            if (isActive(event.getEntity()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (isActive(event.getEntity()))
                event.setCanceled(true);
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
        public static void onSlotClick(ContainerSlotClickEvent event) {
            var player = event.getEntity();
            var menu = event.getContainer();
            var slot = event.getSlot();

            if (event.getType() != ClickType.PICKUP || !quickClick(player, menu, slot, event.getAction(), false))
                return;

            event.setCanceled(true);

            if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
                quickClick(player, menu, slot, event.getAction(), true);

                Minecraft.getInstance().gameMode.handleCreativeModeItemAdd(slot.getItem(), slot.index);
            } else {
                NetworkHandler.sendToServer(new C2SQuickStoreAstralSatchelSlot(menu.slots.indexOf(slot), event.getAction() == ClickAction.SECONDARY));
            }
        }

        private static ItemEntity findDrop(Player player) {
            var reach = player.entityInteractionRange();
            var start = player.getEyePosition();
            var end = start.add(player.getViewVector(1F).scale(reach));
            var block = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            var limit = block.getType() == HitResult.Type.MISS ? reach * reach : block.getLocation().distanceToSqr(start);

            ItemEntity result = null;

            var nearest = limit;

            for (var drop : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1D))) {
                var hit = drop.getBoundingBox().inflate(DROP_PICK_RADIUS).clip(start, end);

                if (hit.isEmpty())
                    continue;

                var distance = hit.get().distanceToSqr(start);

                if (distance > nearest)
                    continue;

                nearest = distance;
                result = drop;
            }

            return result;
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            var player = event.getEntity();

            if (!event.getLevel().isClientSide() || !canUse(player, event.getItemStack()) || AstralSpace.isEngaged())
                return;

            if (canQuickStore(player, event.getItemStack())) {
                var drop = findDrop(player);

                if (drop != null) {
                    NetworkHandler.sendToServer(new C2SQuickStoreAstralSatchelDrop(drop.getId(), event.getHand() == InteractionHand.OFF_HAND));

                    player.swing(event.getHand());

                    return;
                }
            }

            AstralSpace.enter(getSlot(player, event.getHand()));
        }
    }
}
