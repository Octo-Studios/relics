package it.hurts.sskirillss.relics.items.relics.feet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.utility.FluidCollisionEvent;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.cut_glass_boot.C2SCycleFluid;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.*;

public class CutGlassBootItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("glass")
                                .stat(AbilityStatTemplate.builder("capacity")
                                        .initialValue(1D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.9)
                                        .formatValue(value -> (int) MathUtils.round(value, 0) * 1000)
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_fluids")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.4D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(0.01D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("standing")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 3).star(1, 16, 5).star(2, 5, 10).star(3, 11, 10).star(4, 3, 16).star(5, 19, 20).star(6, 10, 21).star(7, 2, 23).star(8, 20, 26).star(9, 11, 27)
                                        .link(4, 6).link(6, 5).link(5, 8).link(8, 9).link(9, 7).link(7, 4).link(6, 9).link(4, 2).link(2, 0).link(0, 1).link(1, 3).link(3, 5)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxRank(0)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.AQUATIC)
                        .build())
                .build();
    }

    private static ResourceLocation getMovementSpeedAttributeId(ItemStack stack, SlotContext slotContext) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID,
                BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()
                        + "_" + BuiltInRegistries.ATTRIBUTE.getKey(Attributes.MOVEMENT_SPEED.value()).getPath()
                        + "_" + slotContext.identifier()
                        + "_" + slotContext.index()
                        + "_glass");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();
        var level = entity.level();
        var movementSpeedAttributeId = getMovementSpeedAttributeId(stack, slotContext);

        if (level.isClientSide())
            return;

        var position = entity.position();
        var state = level.getFluidState(level.clip(new ClipContext(position, position.add(0, -2, 0), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity)).getBlockPos());

        var attribute = Attributes.MOVEMENT_SPEED;
        var operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

        var targetProgress = 0F;
        var fluidAmount = 0;
        var progressBeforeUpdate = this.getSpeedBlend(stack);

        if (state.getType() != Fluids.EMPTY) {
            var entry = this.getFluidEntries(entity, stack).get(state.getFluidType().toString());

            if (entry != null) {
                targetProgress = 20F;
                fluidAmount = entry.getAmount();
            }
        }

        if (targetProgress > 0F && entity.tickCount % 20 == 0) {
            if (entity.getKnownMovement().multiply(1, 0, 1).length() > 0)
                this.getRelicData(entity, stack).getLevelingData().addExperience("glass", "standing", 1);

            this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glass").getStatisticData().getMetricData("duration").addValue(1);
        }

        var progress = progressBeforeUpdate;

        if (progress < targetProgress)
            progress = Math.min(targetProgress, progress + 1F);
        else if (progress > targetProgress)
            progress = Math.max(targetProgress, progress - 1F);

        this.setSpeedBlend(stack, progress);

        var maxModifier = (float) (-0.5F + fluidAmount / 1000F * this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glass").getStatData("speed").getValue());

        if (targetProgress <= 0F && progressBeforeUpdate > 1.0E-4F) {
            var attributeInstance = entity.getAttribute(attribute);

            if (attributeInstance != null) {
                var existing = attributeInstance.getModifier(movementSpeedAttributeId);

                if (existing != null) {
                    var ratioBeforeUpdate = progressBeforeUpdate / 20F;

                    if (ratioBeforeUpdate > 1.0E-4F)
                        maxModifier = (float) (existing.amount() / ratioBeforeUpdate);
                }
            }
        }

        var modifier = maxModifier * (progress / 20F);

        EntityUtils.resetAttribute(entity, attribute, modifier, operation, movementSpeedAttributeId);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        EntityUtils.removeAttribute(slotContext.entity(), Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, getMovementSpeedAttributeId(stack, slotContext));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return false;

        return Item.getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.SOURCE_ONLY).getType() == HitResult.Type.MISS;
    }

    public int getMaxCapacity(LivingEntity entity, ItemStack stack) {
        var amount = 0;

        for (var fluid : this.getFluidEntries(entity, stack).values())
            amount += fluid.getAmount();

        return Math.max(amount, (int) Math.ceil(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glass").getStatData("capacity").getValue() * 1000));
    }

    public int getMaxFluidEntries(LivingEntity entity, ItemStack stack) {
        return Math.max(this.getFluidEntries(entity, stack).size(), (int) Math.ceil(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glass").getStatData("max_fluids").getValue()));
    }

    public int getSelectedFluidIndex(LivingEntity entity, ItemStack stack) {
        return Math.clamp(stack.getOrDefault(RelicsDataComponents.CUT_GLASS_BOOT_SELECTED_FLUID_INDEX, 0), 0, this.getFluidEntries(entity, stack).size());
    }

    public void setSelectedFluidIndex(LivingEntity entity, ItemStack stack, int index) {
        stack.set(RelicsDataComponents.CUT_GLASS_BOOT_SELECTED_FLUID_INDEX, Math.clamp(index, 0, this.getFluidEntries(entity, stack).size()));
    }

    public Fluid getSelectedFluid(LivingEntity entity, ItemStack stack) {
        var vals = new ArrayList<>(this.getFluidEntries(entity, stack).values());

        if (vals.isEmpty())
            return Fluids.EMPTY;

        var idx = Math.max(0, Math.min(vals.size() - 1, this.getSelectedFluidIndex(entity, stack)));

        return BuiltInRegistries.FLUID.get(ResourceLocation.parse(vals.get(idx).getFluid()));
    }

    public Map<String, FluidEntry> getFluidEntries(LivingEntity entity, ItemStack stack) {
        var stored = stack.getOrDefault(RelicsDataComponents.CUT_GLASS_BOOT_FLUIDS, Map.<String, FluidEntry>of());

        return new HashMap<>(stored);
    }

    public void setFluidEntries(LivingEntity entity, ItemStack stack, Map<String, FluidEntry> entries) {
        var copy = new HashMap<>(entries);

        stack.set(RelicsDataComponents.CUT_GLASS_BOOT_FLUIDS, copy);
    }

    public float getSpeedBlend(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(RelicsDataComponents.CUT_GLASS_BOOT_SPEED_BLEND, 0F), 0F, 20F);
    }

    public void setSpeedBlend(ItemStack stack, float value) {
        stack.set(RelicsDataComponents.CUT_GLASS_BOOT_SPEED_BLEND, Math.clamp(value, 0F, 20F));
    }

    public void setFluidEntry(LivingEntity entity, ItemStack stack, FluidEntry entry) {
        var entries = this.getFluidEntries(entity, stack);

        entries.put(entry.getFluid(), entry);

        this.setFluidEntries(entity, stack, entries);
    }

    public void removeFluidEntry(LivingEntity entity, ItemStack stack, FluidEntry entry) {
        var entries = this.getFluidEntries(entity, stack);

        entries.remove(entry.getFluid());

        this.setFluidEntries(entity, stack, entries);
    }

    public int getTotalAmount(LivingEntity entity, ItemStack stack) {
        var total = 0;
        for (var e : this.getFluidEntries(entity, stack).values()) total += e.getAmount();
        return total;
    }

    public boolean canAdd(LivingEntity entity, ItemStack stack, String fluidKey, int amount) {
        var entries = this.getFluidEntries(entity, stack);
        var total = getTotalAmount(entity, stack);

        if (total + amount > getMaxCapacity(entity, stack))
            return false;

        return entries.containsKey(fluidKey) || entries.size() < getMaxFluidEntries(entity, stack);
    }

    public void addAmount(LivingEntity entity, ItemStack stack, String fluidKey, int amount) {
        var entries = this.getFluidEntries(entity, stack);
        var cur = entries.getOrDefault(fluidKey, FluidEntry.EMPTY.toBuilder().fluid(fluidKey).build());
        var updated = cur.toBuilder().amount(cur.getAmount() + amount).build();

        entries.put(fluidKey, updated);

        this.setFluidEntries(entity, stack, entries);
    }

    public void consumeSelected(LivingEntity entity, ItemStack stack, int amount) {
        var entries = this.getFluidEntries(entity, stack);

        if (entries.isEmpty())
            return;

        var list = new ArrayList<>(entries.values());
        var idx = Math.max(0, Math.min(list.size() - 1, this.getSelectedFluidIndex(entity, stack)));
        var entry = list.get(idx);
        var key = entry.getFluid();
        var left = entry.getAmount() - amount;

        if (left > 0)
            entries.put(key, entry.toBuilder().amount(left).build());
        else
            entries.remove(key);

        this.setFluidEntries(entity, stack, entries);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        var hitResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        var pos = hitResult.getBlockPos();
        var face = hitResult.getDirection();

        var fluidState = level.getFluidState(pos);
        var type = fluidState.getType();
        var perUse = 1000;

        if (type != Fluids.EMPTY && fluidState.isSource()) {
            var fluidKey = BuiltInRegistries.FLUID.getKey(type).toString();

            if (!this.canAdd(player, stack, fluidKey, perUse))
                return InteractionResultHolder.pass(stack);

            var state = level.getBlockState(pos);
            var block = state.getBlock();

            if (block instanceof BucketPickup pickup) {
                if (level.isClientSide())
                    return InteractionResultHolder.sidedSuccess(stack, true);

                var result = pickup.pickupBlock(player, level, pos, state);

                if (result.isEmpty())
                    return InteractionResultHolder.fail(stack);

                this.addAmount(player, stack, fluidKey, perUse);

                this.playFluidSound(type, null, level, pos, SoundActions.BUCKET_FILL, SoundSource.PLAYERS, 1F, 1F);

                return InteractionResultHolder.sidedSuccess(stack, false);
            } else if (block instanceof LiquidBlock) {
                if (level.isClientSide())
                    return InteractionResultHolder.sidedSuccess(stack, true);

                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                this.addAmount(player, stack, fluidKey, perUse);

                this.playFluidSound(type, null, level, pos, SoundActions.BUCKET_FILL, SoundSource.PLAYERS, 1F, 1F);

                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            return InteractionResultHolder.pass(stack);
        }

        var selectedFluid = this.getSelectedFluid(player, stack);
        var totalAmount = this.getTotalAmount(player, stack);

        if (selectedFluid == Fluids.EMPTY || totalAmount < perUse)
            return InteractionResultHolder.pass(stack);

        var placePos = pos.relative(face);

        if (!player.mayUseItemAt(placePos, face, stack))
            return InteractionResultHolder.fail(stack);

        if (level.isClientSide())
            return InteractionResultHolder.sidedSuccess(stack, true);

        var fluidType = selectedFluid.getFluidType();
        var stackForCheck = new FluidStack(selectedFluid, perUse);

        if (fluidType.isVaporizedOnPlacement(level, placePos, stackForCheck)) {
            if (!level.isClientSide()) {
                this.consumeSelected(player, stack, perUse);

                fluidType.onVaporize(player, level, placePos, stackForCheck);

                this.playFluidSound(selectedFluid, null, level, placePos, SoundActions.FLUID_VAPORIZE, SoundSource.BLOCKS, 1F, 1F);
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        var stateForPlacement = fluidType.getStateForPlacement(level, placePos, stackForCheck);

        if (stateForPlacement.isEmpty())
            return InteractionResultHolder.fail(stack);

        var placeState = stateForPlacement.createLegacyBlock();
        var placeBlockState = level.getBlockState(placePos);

        if (!placeBlockState.isAir() && !placeBlockState.canBeReplaced() && placeBlockState.getFluidState().getType() != selectedFluid)
            return InteractionResultHolder.fail(stack);

        level.setBlockAndUpdate(placePos, placeState);

        this.consumeSelected(player, stack, perUse);

        this.playFluidSound(selectedFluid, null, level, placePos, SoundActions.BUCKET_EMPTY, SoundSource.PLAYERS, 1F, 1F);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void playFluidSound(Fluid fluid, @Nullable Player player, Level level, net.minecraft.core.BlockPos pos, net.neoforged.neoforge.common.SoundAction action, SoundSource source, float volume, float pitch) {
        if (level.isClientSide())
            return;

        var type = fluid.getFluidType();
        var sound = type.getSound(player, level, pos, action);

        if (sound == null) {
            if (action == SoundActions.BUCKET_FILL)
                sound = (fluid == Fluids.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
            else if (action == SoundActions.BUCKET_EMPTY)
                sound = (fluid == Fluids.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }

        if (sound != null)
            level.playSound(player, pos, sound, source, volume, pitch);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CutGlassBootTooltip(new ArrayList<>(this.getFluidEntries(null, stack).values()), this.getSelectedFluidIndex(null, stack), this.getMaxCapacity(null, stack)));
    }

    public record CutGlassBootTooltip(List<FluidEntry> fluids, int selectedFluid, int capacity) implements TooltipComponent {

    }

    @OnlyIn(Dist.CLIENT)
    public record ClientCutGlassBootTooltip(CutGlassBootTooltip tooltip) implements ClientTooltipComponent {
        @Override
        public int getHeight() {
            var MC = Minecraft.getInstance();

            var baseHeight = 22;

            var lines = 0;

            for (var e : tooltip.fluids())
                if (e != null && e.getAmount() > 0)
                    lines++;

            var gap = lines > 0 ? 14 : 0;

            return baseHeight + gap + lines * MC.font.lineHeight;
        }

        @Override
        public int getWidth(Font font) {
            var maxWidth = 100;

            for (var line : this.constructTooltip()) {
                var width = Minecraft.getInstance().font.width(line);

                if (width > maxWidth)
                    maxWidth = width;
            }

            return maxWidth + 4;
        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
            var MC = Minecraft.getInstance();
            var player = MC.player;

            if (player == null)
                return;

            var pose = guiGraphics.pose();

            int capacity = tooltip.capacity();
            var list = tooltip.fluids();

            int total = 0;

            for (var entry : list)
                total += entry.getAmount();

            var overlay = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/tooltip/cut_glass_boot/flask.png");
            var atlasLoc = InventoryMenu.BLOCK_ATLAS;
            var atlas = MC.getTextureAtlas(atlasLoc);

            int innerX = 4;
            int innerY = 2;
            int innerW = 160;
            int innerH = 16;

            pose.pushPose();

            int filledW = Math.min(innerW, Math.round(innerW * (total / (float) capacity)));
            int used = 0;

            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);

                var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(entry.getFluid()));

                if (fluid == Fluids.EMPTY)
                    continue;

                int segW = (total == 0) ? 0 : Math.round(filledW * (entry.getAmount() / (float) total));

                if (i == list.size() - 1)
                    segW = Math.max(0, filledW - used);

                if (segW <= 0)
                    continue;

                if (used >= filledW)
                    break;

                var ext = IClientFluidTypeExtensions.of(fluid);

                var still = ext.getStillTexture();
                var sprite = atlas.apply(still);
                var contents = sprite.contents();

                int spriteW = contents.width();
                int spriteH = contents.height();
                int spriteX0 = sprite.getX();
                int spriteY0 = sprite.getY();

                int argb = ext.getTintColor();

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int drawX = mouseX + innerX + used;
                int drawYBase = mouseY + innerY;
                int remainingW = Math.min(segW, filledW - used);

                int localX = 0;

                while (remainingW > 0) {
                    int takeW = Math.min(spriteW - localX, remainingW);

                    int yRemaining = innerH;
                    int localY = 0;
                    int drawY = drawYBase;

                    while (yRemaining > 0) {
                        int takeH = Math.min(spriteH - localY, yRemaining);

                        GUIRenderer.begin(atlasLoc, pose)
                                .anchor(SpriteAnchor.TOP_LEFT)
                                .pos(drawX, drawY)
                                .patternSize(takeW, takeH)
                                .texOff(spriteX0 + localX, spriteY0 + localY)
                                .color(r, g, b, a)
                                .end();

                        drawY += takeH;
                        yRemaining -= takeH;
                        localY = (localY + takeH) % spriteH;
                    }

                    drawX += takeW;
                    remainingW -= takeW;
                    localX = (localX + takeW) % spriteW;
                }

                used += segW;
            }

            GUIRenderer.begin(overlay, pose)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(mouseX, mouseY)
                    .end();

            pose.popPose();

            int textX = mouseX + innerX;
            int textY = mouseY + innerY + innerH + 4;

            for (var line : this.constructTooltip()) {
                guiGraphics.drawString(font, line, textX, textY, 0xFFFFFF, false);

                textY += font.lineHeight;
            }
        }

        private List<Component> constructTooltip() {
            var MC = Minecraft.getInstance();
            var list = tooltip.fluids();

            var result = new ArrayList<Component>();

            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);

                var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(entry.getFluid()));
                if (fluid == Fluids.EMPTY) continue;

                var fluidComponent = Component.literal(fluid.getFluidType().getDescription().getString());

                var isSelected = i == tooltip.selectedFluid();

                var line = Component.empty()
                        .append(Component.literal(i == list.size() - 1 ? "┗━" : "┣━").withStyle(ChatFormatting.BOLD))
                        .append(fluidComponent)
                        .append(": ")
                        .append(String.valueOf(entry.getAmount()))
                        .append(" mB")
                        .withStyle(ChatFormatting.GRAY);

                if (isSelected)
                    line.append(Component.literal(" ◀").withStyle(MC.player.tickCount % 30 <= 15 ? ChatFormatting.WHITE : ChatFormatting.GOLD));

                result.add(line);
            }

            return result;
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onFluidCollision(FluidCollisionEvent event) {
            var entity = event.getEntity();

            if (entity.isInFluidType())
                return;

            if (entity.isShiftKeyDown())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.CUT_GLASS_BOOT.get())) {
                var relic = (CutGlassBootItem) stack.getItem();

                var fluids = relic.getFluidEntries(entity, stack);

                if (!fluids.containsKey(event.getFluid().getFluidType().toString()))
                    continue;

                event.setCanceled(true);
            }
        }
    }

    @EventBusSubscriber
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            var player = Minecraft.getInstance().player;

            if (player == null || !player.isShiftKeyDown())
                return;

            for (var stack : Arrays.asList(player.getMainHandItem(), player.getOffhandItem())) {
                if (!(stack.getItem() instanceof CutGlassBootItem))
                    continue;

                NetworkHandler.sendToServer(new C2SCycleFluid((int) Math.round(event.getScrollDeltaY())));

                event.setCanceled(true);

                break;
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class FluidEntry {
        private String fluid;
        private int amount;

        public static final FluidEntry EMPTY = new FluidEntry(Fluids.EMPTY.toString(), 0);

        public static final Codec<FluidEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("fluid").forGetter(FluidEntry::getFluid),
                        Codec.INT.fieldOf("amount").forGetter(FluidEntry::getAmount)
                ).apply(instance, FluidEntry::new)
        );
    }

    public static class CutGlassBootFluidHandler implements IFluidHandlerItem {
        private final ItemStack container;
        private final CutGlassBootItem item;

        public CutGlassBootFluidHandler(ItemStack stack) {
            this.container = stack;
            this.item = (CutGlassBootItem) stack.getItem();
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            var fluid = item.getSelectedFluid(null, container);

            if (fluid == Fluids.EMPTY)
                return FluidStack.EMPTY;

            var entries = item.getFluidEntries(null, container);
            var selected = item.getSelectedFluidIndex(null, container);

            var list = new ArrayList<>(entries.values());

            if (list.isEmpty())
                return FluidStack.EMPTY;

            var entry = list.get(Math.clamp(selected, 0, list.size() - 1));

            return new FluidStack(fluid, entry.getAmount());
        }

        @Override
        public int getTankCapacity(int tank) {
            return item.getMaxCapacity(null, container);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty())
                return 0;

            if (!isFluidValid(0, resource))
                return 0;

            var capacity = getTankCapacity(0);
            var maxFluidEntries = item.getMaxFluidEntries(null, container);

            var entries = item.getFluidEntries(null, container);
            var fluidKey = resource.getFluid().toString();

            var hasFluid = entries.containsKey(fluidKey);

            if (!hasFluid && entries.size() >= maxFluidEntries)
                return 0;

            var currentTotal = 0;

            for (var entry : entries.values())
                currentTotal += entry.getAmount();

            var space = capacity - currentTotal;

            if (space <= 0)
                return 0;

            var step = 1000;
            var toFill = Math.clamp(resource.getAmount(), 0, Math.min(space, step));

            if (toFill <= 0)
                return 0;

            if (action.execute())
                item.addAmount(null, container, fluidKey, toFill);

            return toFill;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty())
                return FluidStack.EMPTY;

            var inTank = this.getFluidInTank(0);

            if (inTank.isEmpty())
                return FluidStack.EMPTY;

            if (inTank.getFluid() != resource.getFluid())
                return FluidStack.EMPTY;

            var toDrain = Math.min(inTank.getAmount(), resource.getAmount());

            if (toDrain <= 0)
                return FluidStack.EMPTY;

            if (action.execute())
                item.consumeSelected(null, container, toDrain);

            return new FluidStack(inTank.getFluid(), toDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0)
                return FluidStack.EMPTY;

            var inTank = this.getFluidInTank(0);

            if (inTank.isEmpty())
                return FluidStack.EMPTY;

            var toDrain = Math.min(inTank.getAmount(), maxDrain);

            if (toDrain <= 0)
                return FluidStack.EMPTY;

            if (action.execute())
                item.consumeSelected(null, container, toDrain);

            return new FluidStack(inTank.getFluid(), toDrain);
        }
    }
}
