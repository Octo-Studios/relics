package it.hurts.sskirillss.relics.items.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.rider_flute.C2SCycleRiderFluteSlot;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.*;

public class RiderFluteItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("stable")
                                .initialMaxLevel(5)
                                .maxLevelRankModifier(0.1)
                                .rankModifier(1, "regeneration")
                                .rankModifier(3, "recall")
                                .rankModifier(5, "resistance")
                                .stat(AbilityStatTemplate.builder("max_slots")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 4.286D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("heal")
                                        .initialValue(0.05D, 0.1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("resistance")
                                        .initialValue(0.05D, 0.1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("riding")
                                        .source(ExperienceSourceTemplate.builder("healing")
                                                .rankModifierVisibilityState("regeneration", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("recall")
                                                .rankModifierVisibilityState("recall", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("resistance")
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("distance_traveled")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("captured_mounts")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("released_mounts")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("healed_health")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("regeneration", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_resisted")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 22).star(1, 13, 4).star(2, 13, 8).star(3, 8, 4).star(4, 8, 7).star(5, 3, 13).star(6, 5, 17).star(7, 17, 14).star(8, 12, 19).star(9, 12, 13).star(10, 11, 26)
                                        .link(4, 3).link(4, 2).link(2, 1).link(6, 5).link(5, 4).link(8, 6).link(7, 2).link(8, 7).link(8, 0).link(6, 9).link(9, 2).link(10, 0)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.VILLAGE, LootEntries.CAVE)
                        .build())
                .build();
    }

    public List<HorseSlotData> getHorseSlots(ItemStack stack) {
        var stored = stack.getOrDefault(RelicsDataComponents.RIDER_FLUTE_SLOTS, List.<HorseSlotData>of());
        var copy = new ArrayList<HorseSlotData>(stored.size());

        for (var slot : stored)
            copy.add(slot.copy());

        return copy;
    }

    public void setHorseSlots(ItemStack stack, List<HorseSlotData> slots) {
        var copy = new ArrayList<HorseSlotData>();

        for (var slot : slots) {
            if (slot.isEmpty())
                continue;

            copy.add(slot.copy());
        }

        stack.set(RelicsDataComponents.RIDER_FLUTE_SLOTS, copy);
        this.setSelectedHorseSlotIndex(stack, stack.getOrDefault(RelicsDataComponents.RIDER_FLUTE_SELECTED_SLOT_INDEX, 0));
    }

    public int getSelectedHorseSlotIndex(ItemStack stack) {
        var size = this.getHorseSlots(stack).size();

        if (size <= 0)
            return 0;

        return Math.clamp(stack.getOrDefault(RelicsDataComponents.RIDER_FLUTE_SELECTED_SLOT_INDEX, 0), 0, size - 1);
    }

    public void setSelectedHorseSlotIndex(ItemStack stack, int index) {
        var size = this.getHorseSlots(stack).size();

        if (size <= 0) {
            stack.set(RelicsDataComponents.RIDER_FLUTE_SELECTED_SLOT_INDEX, 0);

            return;
        }

        stack.set(RelicsDataComponents.RIDER_FLUTE_SELECTED_SLOT_INDEX, Math.clamp(index, 0, size - 1));
    }

    public int getMaxHorseSlots(LivingEntity entity, ItemStack stack) {
        var currentSlots = this.getHorseSlots(stack).size();
        var configuredSlots = (int) Math.ceil(this.getRelicData(entity, stack)
                .getAbilitiesData()
                .getAbilityData("stable")
                .getStatData("max_slots")
                .getValue());

        return Math.max(currentSlots, Math.max(1, configuredSlots));
    }

    private void tickStoredHorseRegeneration(LivingEntity entity, ItemStack stack) {
        var level = entity.level();

        if (level.isClientSide() || entity.tickCount % 20 != 0)
            return;

        var ability = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("stable");

        if (!ability.getRankModifierData("regeneration").isEnabled())
            return;

        var amount = (float) ability.getStatData("heal").getValue();
        var slots = this.getHorseSlots(stack);
        var changed = false;
        var healedTotal = 0D;

        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);

            if (!slot.isStored())
                continue;

            var horse = this.decodeHorse(slot, level);

            if (horse == null)
                continue;

            var current = horse.getHealth();
            var max = horse.getMaxHealth();
            var healed = Math.min(max, current + amount);
            var difference = healed - current;

            if (difference <= 0F)
                continue;

            horse.setHealth(healed);
            slots.set(i, HorseSlotData.fromEntity(horse));

            changed = true;
            healedTotal += difference;
        }

        if (changed)
            this.setHorseSlots(stack, slots);

        if (healedTotal > 0D && ability.getRankModifierData("regeneration").isEnabled()) {
            this.getRelicData(entity, stack).getLevelingData().addExperience("stable", "healing", healedTotal);

            ability.getStatisticData().getMetricData("healed_health").addValue(healedTotal);
        }
    }

    private void tickAutoRecall(Player player, ItemStack stack) {
        var level = player.level();

        if (level.isClientSide() || player.tickCount % 20 != 0)
            return;

        if (!(level instanceof ServerLevel serverLevel))
            return;

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");

        if (!ability.getRankModifierData("recall").isEnabled())
            return;

        var slots = this.getHorseSlots(stack);
        var changed = false;
        var maxDistanceSqr = 16D * 16D;
        var recalled = 0D;

        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);

            if (!slot.isDeployed())
                continue;

            var entity = serverLevel.getEntity(slot.deployedUuid());

            if (!(entity instanceof LivingEntity horse) || horse.isDeadOrDying())
                continue;

            if (!this.isSupportedMount(horse))
                continue;

            if (horse.distanceToSqr(player) <= maxDistanceSqr)
                continue;

            if (!this.canStoreHorse(player, horse))
                continue;

            slots.set(i, HorseSlotData.fromEntity(horse));
            horse.discard();
            level.playSound(null, horse.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.9F, 1F);

            changed = true;
            recalled += 1D;
        }

        if (changed)
            this.setHorseSlots(stack, slots);

        if (recalled > 0D && ability.getRankModifierData("recall").isEnabled()) {
            this.getRelicData(player, stack).getLevelingData().addExperience("stable", "recall", recalled);

            ability.getStatisticData().getMetricData("captured_mounts").addValue(recalled);
        }
    }

    public boolean cycleSelectedHorseSlot(ItemStack stack, int delta) {
        var slots = this.getHorseSlots(stack);

        if (slots.isEmpty())
            return false;

        this.setSelectedHorseSlotIndex(stack, Math.floorMod(this.getSelectedHorseSlotIndex(stack) + delta, slots.size()));

        return true;
    }

    public Component getSelectedHorseSlotMessage(Level level, ItemStack stack) {
        var slots = this.getHorseSlots(stack);

        if (slots.isEmpty())
            return Component.empty();

        var selected = this.getSelectedHorseSlotIndex(stack);
        var horse = slots.get(selected);

        return Component.translatable("relics.message.rider_flute.slot", selected + 1, slots.size(), this.getSlotName(level, horse));
    }

    public void notifySelectedHorseSlot(Player player, ItemStack stack) {
        player.displayClientMessage(this.getSelectedHorseSlotMessage(player.level(), stack), true);
    }

    private Component getSlotName(Level level, HorseSlotData slot) {
        var id = ResourceLocation.tryParse(slot.entityType());
        var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        var typeName = type == null ? Component.literal(slot.entityType()) : type.getDescription();
        var customName = this.parseCustomName(slot.entityData(), level.registryAccess());
        var result = typeName.copy();

        if (customName != null)
            result.append(" ").append(customName.copy());

        if (slot.isDeployed())
            return result.append(Component.translatable("relics.message.rider_flute.deployed_suffix"));

        return result;
    }

    private Component parseCustomName(CompoundTag data, net.minecraft.core.HolderLookup.Provider registries) {
        if (!data.contains("CustomName", Tag.TAG_STRING))
            return null;

        var raw = data.getString("CustomName");

        if (raw.isBlank())
            return null;

        try {
            return Component.Serializer.fromJsonLenient(raw, registries);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setHorseSlot(ItemStack stack, int index, HorseSlotData slotData) {
        var slots = this.getHorseSlots(stack);

        if (slotData.isEmpty()) {
            if (index >= 0 && index < slots.size())
                slots.remove(index);
        } else if (index >= 0 && index < slots.size())
            slots.set(index, slotData.copy());
        else if (index == slots.size() && slots.size() < this.getMaxHorseSlots(null, stack))
            slots.add(slotData.copy());

        this.setHorseSlots(stack, slots);
    }

    private int getHorseSlotByEntityUUID(ItemStack stack, UUID uuid) {
        var slots = this.getHorseSlots(stack);

        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isDeployed() && slots.get(i).deployedUuid().equals(uuid))
                return i;
        }

        return -1;
    }

    private boolean isSupportedMount(LivingEntity entity) {
        return entity instanceof AbstractHorse || entity instanceof Saddleable;
    }

    private boolean canStoreHorse(Player player, LivingEntity horse) {
        if (horse.isDeadOrDying() || !this.isSupportedMount(horse))
            return false;

        if (horse instanceof AbstractHorse abstractHorse) {
            var owner = abstractHorse.getOwnerUUID();

            if (owner != null)
                return owner.equals(player.getUUID());

            return abstractHorse.isTamed();
        }

        if (horse instanceof Saddleable saddleable)
            return saddleable.isSaddled();

        return false;
    }

    private boolean catchHorse(Player player, ItemStack stack, LivingEntity horse, int slotIndex) {
        if (!this.canStoreHorse(player, horse))
            return false;

        var slots = this.getHorseSlots(stack);

        if (slotIndex >= slots.size() && slots.size() >= this.getMaxHorseSlots(player, stack))
            return false;

        var slot = HorseSlotData.fromEntity(horse);

        this.setHorseSlot(stack, slotIndex, slot);
        this.setSelectedHorseSlotIndex(stack, slotIndex);

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");

        ability.getStatisticData().getMetricData("captured_mounts").addValue(1);

        horse.discard();
        player.level().playSound(null, horse.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.9F, 1F);

        return true;
    }

    private LivingEntity decodeHorse(HorseSlotData slot, Level level) {
        var id = ResourceLocation.tryParse(slot.entityType());

        if (id == null)
            return null;

        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);

        if (type == null)
            return null;

        var entity = type.create(level);

        if (!(entity instanceof LivingEntity horse))
            return null;

        var data = slot.entityData().copy();

        data.remove("UUID");
        data.remove("Pos");
        data.remove("Motion");
        data.remove("Rotation");

        horse.load(data);

        if (!this.isSupportedMount(horse))
            return null;

        return horse;
    }

    private boolean releaseHorse(ServerLevel level, Player player, ItemStack stack, int slotIndex, HorseSlotData slot, boolean removeFromFlute) {
        if (!slot.isStored())
            return false;

        var horse = this.decodeHorse(slot, level);

        if (horse == null)
            return false;

        horse.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        if (!level.addFreshEntity(horse))
            return false;

        horse.setDeltaMovement(player.getLookAngle().normalize());
        horse.fallDistance = 0F;

        if (removeFromFlute)
            this.setHorseSlot(stack, slotIndex, HorseSlotData.EMPTY);
        else
            this.setHorseSlot(stack, slotIndex, slot.withDeployedUuid(horse.getUUID()));

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");

        ability.getStatisticData().getMetricData("released_mounts").addValue(1);

        level.playSound(null, horse.blockPosition(), SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 0.9F, 1F);

        return true;
    }

    private boolean catchReleasedHorse(ServerLevel level, Player player, ItemStack stack, int slotIndex, HorseSlotData slot) {
        if (!slot.isDeployed())
            return false;

        var entity = level.getEntity(slot.deployedUuid());

        if (!(entity instanceof LivingEntity horse))
            return false;

        if (!this.isSupportedMount(horse))
            return false;

        return this.catchHorse(player, stack, horse, slotIndex);
    }

    private boolean toggleSelectedHorse(ServerLevel level, Player player, ItemStack stack) {
        var slots = this.getHorseSlots(stack);

        if (slots.isEmpty())
            return false;

        var slotIndex = this.getSelectedHorseSlotIndex(stack);

        if (slotIndex < 0 || slotIndex >= slots.size())
            return false;

        var selected = slots.get(slotIndex);
        var removeFromFlute = player.isShiftKeyDown();

        if (selected.isStored())
            return this.releaseHorse(level, player, stack, slotIndex, selected, removeFromFlute);

        if (selected.isDeployed()) {
            if (removeFromFlute) {
                this.setHorseSlot(stack, slotIndex, HorseSlotData.EMPTY);

                return true;
            }

            return this.catchReleasedHorse(level, player, stack, slotIndex, selected);
        }

        return false;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!this.isSupportedMount(entity))
            return InteractionResult.PASS;

        var slotIndex = this.getHorseSlotByEntityUUID(stack, entity.getUUID());

        if (slotIndex < 0)
            slotIndex = this.getHorseSlots(stack).size();

        var slots = this.getHorseSlots(stack);

        if (!this.canStoreHorse(player, entity))
            return InteractionResult.PASS;

        if (slotIndex >= slots.size() && slots.size() >= this.getMaxHorseSlots(player, stack))
            return InteractionResult.PASS;

        if (player.level().isClientSide())
            return InteractionResult.SUCCESS;

        return this.catchHorse(player, stack, entity, slotIndex) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide())
            return InteractionResultHolder.sidedSuccess(stack, true);

        var handled = this.toggleSelectedHorse((ServerLevel) level, player, stack);

        if (!handled && player.getVehicle() instanceof LivingEntity horse && this.isSupportedMount(horse)) {
            var slotIndex = this.getHorseSlotByEntityUUID(stack, horse.getUUID());

            if (slotIndex < 0)
                slotIndex = this.getHorseSlots(stack).size();

            handled = this.catchHorse(player, stack, horse, slotIndex);
        }

        return handled ? InteractionResultHolder.sidedSuccess(stack, false) : InteractionResultHolder.pass(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var entries = new ArrayList<RiderFluteTooltipEntry>();
        var ability = this.getRelicData(null, stack).getAbilitiesData().getAbilityData("stable");
        var regenerationActive = ability.getRankModifierData("regeneration").isEnabled();

        for (var slot : this.getHorseSlots(stack)) {
            var id = ResourceLocation.tryParse(slot.entityType());
            var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            var data = slot.entityData();
            var current = data.contains("Health", Tag.TAG_ANY_NUMERIC) ? data.getFloat("Health") : 0F;
            var max = 0F;

            List<CompoundTag> attributes = List.of();

            if (data.contains("attributes", Tag.TAG_LIST))
                attributes = data.getList("attributes", Tag.TAG_COMPOUND).stream().map(tag -> (CompoundTag) tag).toList();
            else if (data.contains("Attributes", Tag.TAG_LIST))
                attributes = data.getList("Attributes", Tag.TAG_COMPOUND).stream().map(tag -> (CompoundTag) tag).toList();

            for (var attribute : attributes) {
                var attributeId = attribute.contains("id", Tag.TAG_STRING) ? attribute.getString("id") : attribute.getString("Name");

                if (!attributeId.equals("minecraft:max_health") && !attributeId.equals("minecraft:generic.max_health") && !attributeId.equals("generic.max_health"))
                    continue;

                max = (float) (attribute.contains("base", Tag.TAG_ANY_NUMERIC) ? attribute.getDouble("base") : attribute.getDouble("Base"));

                break;
            }

            if (max <= 0F && type != null && DefaultAttributes.hasSupplier(type)) {
                @SuppressWarnings("unchecked")
                var livingType = (net.minecraft.world.entity.EntityType<? extends LivingEntity>) type;
                var supplier = DefaultAttributes.getSupplier(livingType);

                if (supplier.hasAttribute(Attributes.MAX_HEALTH))
                    max = (float) supplier.getBaseValue(Attributes.MAX_HEALTH);
            }

            if (max <= 0F)
                max = current > 0F ? current : 1F;

            var safeCurrent = Math.max(0F, current);
            var safeMax = Math.max(1F, max);
            var regenerating = regenerationActive && !slot.isDeployed() && safeCurrent < safeMax;

            entries.add(new RiderFluteTooltipEntry(slot.entityType(), data.getString("CustomName"), slot.deployedUuid(), slot.isDeployed(), safeCurrent, safeMax, regenerating));
        }

        return Optional.of(new RiderFluteTooltip(entries, this.getSelectedHorseSlotIndex(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(entity instanceof LivingEntity livingEntity))
            return;

        this.tickStoredHorseRegeneration(livingEntity, stack);

        if (livingEntity instanceof Player player) {
            this.tickAutoRecall(player, stack);

            if (!level.isClientSide() && player.getVehicle() instanceof LivingEntity horse && this.getHorseSlotByEntityUUID(stack, horse.getUUID()) >= 0) {
                var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");
                var movement = horse.getKnownMovement().multiply(1D, 0D, 1D).length();

                if (movement > 0D) {
                    this.getRelicData(player, stack).getLevelingData().addExperience("stable", "riding", movement * 0.025D);

                    ability.getStatisticData().getMetricData("distance_traveled").addValue(movement);
                }
            }
        }
    }

    public record RiderFluteTooltip(List<RiderFluteTooltipEntry> slots, int selectedSlot) implements TooltipComponent {

    }

    @OnlyIn(Dist.CLIENT)
    public record ClientRiderFluteTooltip(RiderFluteTooltip tooltip) implements ClientTooltipComponent {
        @Override
        public int getHeight() {
            return Math.max(1, this.constructTooltip().size()) * Minecraft.getInstance().font.lineHeight + 2;
        }

        @Override
        public int getWidth(Font font) {
            var maxWidth = 70;

            for (var line : this.constructTooltip()) {
                var width = Minecraft.getInstance().font.width(line);

                if (width > maxWidth)
                    maxWidth = width;
            }

            return maxWidth + 4;
        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
            var textX = mouseX + 2;
            var textY = mouseY + 1;

            for (var line : this.constructTooltip()) {
                guiGraphics.drawString(font, line, textX, textY, 0xFFFFFF, false);

                textY += font.lineHeight;
            }
        }

        private List<Component> constructTooltip() {
            var lines = new ArrayList<Component>();
            var slots = tooltip.slots();

            if (slots.isEmpty())
                return lines;

            var selected = Math.clamp(tooltip.selectedSlot(), 0, slots.size() - 1);

            var ticks = Minecraft.getInstance().player.tickCount;

            for (int i = 0; i < slots.size(); i++) {
                var slot = slots.get(i);
                var health = this.getHealthText(slot, ticks);
                var name = Component.literal("- ").append(this.getSlotName(slot).copy()).withStyle(ChatFormatting.GRAY);
                var pointer = i == selected ? Component.literal(" ◀").withStyle(ticks % 30 <= 15 ? ChatFormatting.WHITE : ChatFormatting.GOLD) : Component.empty();

                lines.add(Component.empty()
                        .append(health)
                        .append(name));

                if (i == selected)
                    lines.set(i, lines.get(i).copy().append(pointer));
            }

            return lines;
        }

        private Component getSlotName(RiderFluteTooltipEntry slot) {
            var id = ResourceLocation.tryParse(slot.entityType());
            var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            var typeName = type == null ? Component.literal(slot.entityType()) : type.getDescription();
            var customName = this.parseCustomName(slot.customName());
            var result = typeName.copy();

            if (customName != null)
                result.append(" ").append(customName.copy());

            if (slot.deployed())
                return result.append(Component.translatable("relics.message.rider_flute.deployed_suffix"));

            return result;
        }

        private Component parseCustomName(String raw) {
            if (raw == null || raw.isBlank())
                return null;

            var registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : RegistryAccess.EMPTY;

            try {
                return Component.Serializer.fromJsonLenient(raw, registries);
            } catch (Exception ignored) {
                return null;
            }
        }

        private Component getHealthText(RiderFluteTooltipEntry slot, int ticks) {
            var health = slot.currentHealth();
            var max = slot.maxHealth();

            var safeHealth = Math.max(0F, health);
            var safeMax = Math.max(1F, max);
            var animate = slot.regenerating() && safeHealth < safeMax;
            var heartColor = animate && ticks % 30 <= 15 ? ChatFormatting.RED : animate ? ChatFormatting.DARK_RED : ChatFormatting.RED;

            return Component.empty()
                    .append(Component.literal("[" + this.formatHealth(safeHealth) + "/" + this.formatHealth(safeMax) + " ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("❤").withStyle(heartColor))
                    .append(Component.literal("] ").withStyle(ChatFormatting.RED));
        }
        private String formatHealth(float value) {
            if (Math.abs(value - Math.round(value)) < 0.05F)
                return String.valueOf(Math.round(value));

            return String.format(java.util.Locale.ROOT, "%.1f", value);
        }
    }

    public record RiderFluteTooltipEntry(String entityType, String customName, UUID deployedUuid, boolean deployed, float currentHealth, float maxHealth, boolean regenerating) {

    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static double getMountedResistance(Player player, LivingEntity horse) {
            var total = 0D;

            for (var stack : EntityUtils.getEquippedRelics(player)) {
                if (!(stack.getItem() instanceof RiderFluteItem relic))
                    continue;

                if (relic.getHorseSlotByEntityUUID(stack, horse.getUUID()) < 0)
                    continue;

                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");

                if (!ability.getRankModifierData("resistance").isEnabled())
                    continue;

                total += ability.getStatData("resistance").getValue();
            }

            return Math.clamp(total, 0D, 1D);
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var original = event.getNewDamage();

            if (original <= 0F)
                return;

            Player player;
            LivingEntity horse;

            if (event.getEntity() instanceof Player damagedPlayer && damagedPlayer.getVehicle() instanceof LivingEntity riddenHorse) {
                player = damagedPlayer;
                horse = riddenHorse;
            } else if (event.getEntity() instanceof LivingEntity damagedHorse && damagedHorse.getFirstPassenger() instanceof Player rider) {
                player = rider;
                horse = damagedHorse;
            } else
                return;

            if (!(horse instanceof AbstractHorse || horse instanceof Saddleable))
                return;

            var resistance = getMountedResistance(player, horse);

            if (resistance <= 0D)
                return;

            event.setNewDamage((float) (original * (1D - resistance)));

            var blocked = original - event.getNewDamage();

            if (blocked <= 0F)
                return;

            for (var stack : EntityUtils.getEquippedRelics(player)) {
                if (!(stack.getItem() instanceof RiderFluteItem relic))
                    continue;

                if (relic.getHorseSlotByEntityUUID(stack, horse.getUUID()) < 0)
                    continue;

                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("stable");

                if (!ability.getRankModifierData("resistance").isEnabled())
                    continue;

                relic.getRelicData(player, stack).getLevelingData().addExperience("stable", "resistance", blocked);
                ability.getStatisticData().getMetricData("damage_resisted").addValue(blocked);

                break;
            }
        }
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            var player = Minecraft.getInstance().player;

            if (player == null || !player.isShiftKeyDown())
                return;

            var delta = (int) Math.round(event.getScrollDeltaY());

            if (delta == 0)
                return;

            for (var stack : Arrays.asList(player.getMainHandItem(), player.getOffhandItem())) {
                if (!(stack.getItem() instanceof RiderFluteItem item))
                    continue;

                if (item.getHorseSlots(stack).isEmpty())
                    continue;

                NetworkHandler.sendToServer(new C2SCycleRiderFluteSlot(delta));

                event.setCanceled(true);

                break;
            }
        }
    }

    public record HorseSlotData(String entityType, CompoundTag entityData, UUID deployedUuid) {
        public static final Codec<HorseSlotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("entity_type").forGetter(HorseSlotData::entityType),
                CompoundTag.CODEC.fieldOf("entity_data").forGetter(HorseSlotData::entityData),
                UUIDUtil.CODEC.fieldOf("deployed_uuid").forGetter(HorseSlotData::deployedUuid)
        ).apply(instance, HorseSlotData::new));

        public static final HorseSlotData EMPTY = new HorseSlotData("", new CompoundTag(), Util.NIL_UUID);

        public static HorseSlotData fromEntity(LivingEntity horse) {
            var data = new CompoundTag();

            horse.saveWithoutId(data);

            data.remove("UUID");
            data.remove("Pos");
            data.remove("Motion");
            data.remove("Rotation");

            var typeId = BuiltInRegistries.ENTITY_TYPE.getKey(horse.getType()).toString();

            return new HorseSlotData(typeId, data, Util.NIL_UUID);
        }

        public boolean isStored() {
            return !this.entityType().isBlank() && !this.isDeployed();
        }

        public boolean isDeployed() {
            return !this.deployedUuid().equals(Util.NIL_UUID);
        }

        public boolean isEmpty() {
            return this.entityType().isBlank() && !this.isDeployed();
        }

        public HorseSlotData withDeployedUuid(UUID uuid) {
            return new HorseSlotData(this.entityType(), this.entityData().copy(), uuid);
        }

        public HorseSlotData copy() {
            return new HorseSlotData(this.entityType(), this.entityData().copy(), this.deployedUuid());
        }
    }
}
