package it.hurts.sskirillss.relics.items.relics.feet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.*;

public class CutGlassBootItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("skating")
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.6279D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 3, 7).star(1, 13, 9).star(2, 6, 16).star(3, 16, 18).star(4, 9, 25).star(5, 15, 29)
                                        .link(5, 4).link(4, 2).link(2, 0).link(2, 1).link(4, 3)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    public int getMaxCapacity(LivingEntity entity, ItemStack stack) {
        return 10000;
    }

    public int getMaxFluidEntries(LivingEntity entity, ItemStack stack) {
        return 5;
    }

    public int getSelectedFluidIndex(LivingEntity entity, ItemStack stack) {
        return 0;
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

        if (!entries.containsKey(fluidKey) && entries.size() >= getMaxFluidEntries(entity, stack))
            return false;

        return true;
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

        if (left > 0) {
            entries.put(key, entry.toBuilder().amount(left).build());
        } else {
            entries.remove(key);
        }

        this.setFluidEntries(entity, stack, entries);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var sneaking = player.isShiftKeyDown();
        var clip = sneaking ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY;
        var hit = getPlayerPOVHitResult(level, player, clip);

        if (hit.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        var targetPos = hit.getBlockPos();
        var face = hit.getDirection();
        var placePos = targetPos.relative(face);

        if (!level.mayInteract(player, targetPos) || !player.mayUseItemAt(placePos, face, stack))
            return InteractionResultHolder.fail(stack);

        if (sneaking) {
            var selected = this.getSelectedFluid(player, stack);

            if (selected == Fluids.EMPTY)
                return InteractionResultHolder.pass(stack);

            var entries = this.getFluidEntries(player, stack);
            var list = new ArrayList<>(entries.values());
            var idx = Math.max(0, Math.min(list.size() - 1, this.getSelectedFluidIndex(player, stack)));
            var entry = list.get(idx);

            if (entry.getAmount() < 1000) {
                this.consumeSelected(player, stack, entry.getAmount());

                player.awardStat(Stats.ITEM_USED.get(this));

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            var actualPos = canBlockContainFluid(player, level, targetPos, level.getBlockState(targetPos), stack) ? targetPos : placePos;

            if (this.emptyContents(player, level, actualPos, hit, stack)) {
                if (player instanceof ServerPlayer sp)
                    CriteriaTriggers.PLACED_BLOCK.trigger(sp, actualPos, stack);

                player.awardStat(Stats.ITEM_USED.get(this));

                this.consumeSelected(player, stack, 1000);

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.fail(stack);
        } else {
            var state = level.getBlockState(targetPos);

            if (!(state.getBlock() instanceof BucketPickup pickup))
                return InteractionResultHolder.pass(stack);

            var fluidState = state.getFluidState();

            if (!fluidState.isSource())
                return InteractionResultHolder.pass(stack);

            var fluid = fluidState.getType();
            var key = BuiltInRegistries.FLUID.getKey(fluid).toString();

            if (!this.canAdd(player, stack, key, 1000))
                return InteractionResultHolder.fail(stack);

            var picked = pickup.pickupBlock(player, level, targetPos, state);

            if (!picked.isEmpty()) {
                pickup.getPickupSound(state).ifPresent(snd -> player.playSound(snd, 1.0F, 1.0F));
                level.gameEvent(player, GameEvent.FLUID_PICKUP, targetPos);

                if (!level.isClientSide && player instanceof ServerPlayer sp)
                    CriteriaTriggers.FILLED_BUCKET.trigger(sp, picked);

                this.addAmount(player, stack, key, 1000);

                player.awardStat(Stats.ITEM_USED.get(this));

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.fail(stack);
        }
    }

    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult hit, @Nullable ItemStack container) {
        var selectedFluid = this.getSelectedFluid(player, container);

        if (!(selectedFluid instanceof FlowingFluid flowing))
            return false;

        var state = level.getBlockState(pos);
        var block = state.getBlock();
        var canReplace = state.canBeReplaced(selectedFluid);

        boolean canPlaceHere;

        if (!state.isAir() && !canReplace) {
            if (block instanceof LiquidBlockContainer tank && tank.canPlaceLiquid(player, level, pos, state, selectedFluid)) {
                canPlaceHere = true;
            } else {
                canPlaceHere = false;
            }
        } else {
            canPlaceHere = true;
        }

        var contained = java.util.Optional.ofNullable(container).flatMap(net.neoforged.neoforge.fluids.FluidUtil::getFluidContained);

        if (!canPlaceHere) {
            return hit != null && this.emptyContents(player, level, hit.getBlockPos().relative(hit.getDirection()), null, container);
        }

        if (contained.isPresent() && selectedFluid.getFluidType().isVaporizedOnPlacement(level, pos, contained.get())) {
            selectedFluid.getFluidType().onVaporize(player, level, pos, contained.get());
            return true;
        }

        if (level.dimensionType().ultraWarm() && selectedFluid.is(FluidTags.WATER)) {
            var x = pos.getX();
            var y = pos.getY();
            var z = pos.getZ();

            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for (var n = 0; n < 8; n++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0, 0.0, 0.0);
            }

            return true;
        }
        if (block instanceof LiquidBlockContainer tank2 && tank2.canPlaceLiquid(player, level, pos, state, selectedFluid)) {
            tank2.placeLiquid(level, pos, state, flowing.getSource(false));

            this.playEmptySound(player, level, pos, container);

            return true;
        }

        if (!level.isClientSide && canReplace && !state.liquid())
            level.destroyBlock(pos, true);

        if (!level.setBlock(pos, selectedFluid.defaultFluidState().createLegacyBlock(), 11) && !state.getFluidState().isSource()) {
            return false;
        }

        this.playEmptySound(player, level, pos, container);

        return true;
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos, ItemStack stack) {
        SoundEvent soundevent = this.getSelectedFluid(player, stack).getFluidType().getSound(player, level, pos, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);

        if (soundevent == null)
            soundevent = this.getSelectedFluid(player, stack).is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;

        level.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }

    protected boolean canBlockContainFluid(@Nullable Player player, Level worldIn, BlockPos posIn, BlockState blockstate, ItemStack stack) {
        return blockstate.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer) blockstate.getBlock()).canPlaceLiquid(player, worldIn, posIn, blockstate, this.getSelectedFluid(player, stack));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CutGlassBootTooltip(new ArrayList<>(this.getFluidEntries(null, stack).values()), this.getMaxCapacity(null, stack)));
    }

    public record CutGlassBootTooltip(List<FluidEntry> fluids, int capacity) implements TooltipComponent {

    }

    @OnlyIn(Dist.CLIENT)
    public record ClientCutGlassBootTooltip(CutGlassBootTooltip tooltip) implements ClientTooltipComponent {
        @Override
        public int getHeight() {
            return 22;
        }

        @Override
        public int getWidth(Font font) {
            return 100;
        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
            var MC = net.minecraft.client.Minecraft.getInstance();
            var pose = guiGraphics.pose();

            int capacity = tooltip.capacity();
            var list = tooltip.fluids();

            int total = 0;

            for (var e : list)
                total += Math.max(0, e.getAmount());

            total = Math.max(0, total);

            var overlay = ResourceLocation.fromNamespaceAndPath(it.hurts.sskirillss.relics.Relics.MODID, "textures/gui/tooltip/cut_glass_boot/flask.png");
            var atlasLoc = InventoryMenu.BLOCK_ATLAS;
            var atlas = MC.getTextureAtlas(atlasLoc);

            int innerX = 5;
            int innerY = 2;
            int innerW = 159;
            int innerH = 16;

            pose.pushPose();

            int filledW = Math.min(innerW, Math.round(innerW * (total / (float) capacity)));
            int used = 0;

            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);

                if (entry == null || entry.getAmount() <= 0)
                    continue;

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
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onFluidCollision(FluidCollisionEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.CUT_GLASS_BOOT.get())) {
                var relic = (CutGlassBootItem) stack.getItem();

                var fluids = relic.getFluidEntries(entity, stack);

                if (fluids.containsKey(event.getFluid().getFluidType().toString()))
                    event.setCanceled(true);
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
}