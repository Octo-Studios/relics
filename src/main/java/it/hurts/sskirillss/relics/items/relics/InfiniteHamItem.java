package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.UpgradeOperationRegistry;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class InfiniteHamItem extends RelicItem {
    public InfiniteHamItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .food(new FoodProperties.Builder().build())
                .rarity(Rarity.RARE));
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("regeneration")
                                .requiredPoints(2)
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(30D, 15D)
                                        .upgradeModifier(UpgradeOperationRegistry.ADDITIVE.get(), -0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("feed")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperationRegistry.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 7).star(1, 19, 7).star(2, 5, 15)
                                        .star(3, 10, 16).star(4, 17, 20).star(5, 7, 24)
                                        .link(1, 3).link(3, 0).link(3, 2).link(3, 4).link(3, 5)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("marinade")
                                .requiredLevel(5)
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 18, 5).star(1, 8, 7).star(2, 4, 16)
                                        .star(3, 17, 16).star(4, 10, 21).star(5, 17, 23)
                                        .star(6, 5, 25).star(7, 10, 29)
                                        .link(0, 1).link(0, 3).link(1, 3).link(1, 2).link(3, 4).link(2, 4).link(3, 5).link(4, 7).link(2, 6)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("meat_bat")
                                .requiredLevel(10)
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.5D, 2D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.05D, 0.25D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 10, 5).star(1, 18, 7).star(2, 3, 9)
                                        .star(3, 17, 14).star(4, 10, 17).star(5, 3, 19)
                                        .star(6, 9, 24).star(7, 18, 24)
                                        .link(1, 4).link(4, 0).link(4, 2).link(4, 3).link(4, 5).link(4, 6).link(4, 7)
                                        .link(0, 3).link(3, 7).link(7, 6).link(6, 5).link(5, 2).link(2, 0).link(2, 0)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(20)
                        .step(100)
                        .sources(LevelingSourcesTemplate.builder()
                                .source(LevelingSourceTemplate.abilityBuilder("regeneration")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .source(LevelingSourceTemplate.abilityBuilder("marinade")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .source(LevelingSourceTemplate.abilityBuilder("meat_bat")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff644a41)
                                .borderBottom(0xff592410)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        ItemStack stack = this.getDefaultInstance();

        setPieces(stack, getMaxPieces());

        constructor.entry(CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if (level.isClientSide() || !(entityIn instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration")
                || entityIn.tickCount % (int) Math.max(1, getStatValue(stack, "regeneration", "cooldown") * 20) != 0 || getPieces(stack) >= getMaxPieces())
            return;

        addPieces(stack, 1);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (canPlayerUseAbility(player, stack, "regeneration") && getPieces(stack) > 0 && player.getFoodData().needsFood()) {
            player.startUsingItem(hand);

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration"))
            return stack;

        var properties = getFoodProperties(stack, entity);

        if (properties == null)
            return stack;

        player.eat(level, stack.copy());

        var eaten = (int) Math.ceil(properties.nutrition() / getStatValue(stack, "regeneration", "feed"));

        if (eaten > 0) {
            addPieces(stack, -eaten);

            if (isLevelingSourceUnlocked(stack, "regeneration"))
                spreadRelicExperience(player, stack, eaten);
        }

        return stack;
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        if (!(entity instanceof Player player) || !canPlayerUseAbility(player, stack, "regeneration"))
            return super.getFoodProperties(stack, entity);

        var charge = getPieces(stack);

        if (charge == 0)
            return null;

        var nutrition = Math.min((int) Math.ceil(charge * getStatValue(stack, "regeneration", "feed")), 20 - player.getFoodData().getFoodLevel());

        var builder = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(nutrition / 3F);

        if (canPlayerUseAbility(player, stack, "marinade")) {
            var contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents != null)
                contents.forEachEffect(effect -> {
                    var holder = effect.getEffect();
                    var isInstant = holder.value().isInstantenous();

                    builder.effect(() -> new MobEffectInstance(holder, isInstant ? 1 : (int) (nutrition * getStatValue(stack, "marinade", "duration") * 20),
                            effect.getAmplifier(), !isInstant && effect.isAmbient(), !isInstant && effect.isVisible(), !isInstant && effect.showIcon()), 1F);
                });
        }

        return builder.build();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(DataComponents.POTION_CONTENTS) != null;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    public int getMaxPieces() {
        return 6;
    }

    public int getPieces(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(CHARGE, 0), 0, getMaxPieces());
    }

    public void setPieces(ItemStack stack, int amount) {
        stack.set(CHARGE, Math.clamp(amount, 0, getMaxPieces()));
    }

    public void addPieces(ItemStack stack, int amount) {
        setPieces(stack, getPieces(stack) + amount);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var contents = stack.get(DataComponents.POTION_CONTENTS);

        return contents == null ? Optional.empty() : Optional.of(new InfiniteHamTooltip(StreamSupport.stream(contents.getAllEffects().spliterator(), false).collect(Collectors.toList())));
    }

    public record InfiniteHamTooltip(List<MobEffectInstance> effects) implements TooltipComponent {

    }

    @OnlyIn(Dist.CLIENT)
    public record ClientInfiniteHamTooltip(InfiniteHamTooltip tooltip) implements ClientTooltipComponent {
        @Override
        public int getHeight() {
            return tooltip.effects().size() * 11;
        }

        @Override
        public int getWidth(Font font) {
            return 100;
        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
            var yOff = 0;

            for (MobEffectInstance effect : tooltip.effects()) {
                var holder = effect.getEffect();

                var manager = Minecraft.getInstance().getMobEffectTextures();

                guiGraphics.blit(mouseX, mouseY + yOff, 0, 10, 10, manager.get(holder));

                var name = Component.translatable(effect.getDescriptionId());

                if (effect.getAmplifier() > 0)
                    name = Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.getAmplifier()));

                name.withStyle(holder.value().getCategory().getTooltipFormatting());

                guiGraphics.drawString(font, name, mouseX + 12, mouseY + 2 + yOff, 0xFFFFFF);

                yOff += 11;
            }
        }
    }

    @EventBusSubscriber
    public static class InfinityHamEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingIncomingDamageEvent event) {
            var source = event.getSource();

            if (!(source.getDirectEntity() instanceof Player player) || !source.is(DamageTypeTags.IS_PLAYER_ATTACK))
                return;

            var stack = player.getMainHandItem();

            if (!(stack.getItem() instanceof InfiniteHamItem relic) || !relic.canPlayerUseAbility(player, stack, "meat_bat"))
                return;

            var charge = relic.getPieces(stack);

            if (charge <= 0)
                return;

            if (relic.isLevelingSourceUnlocked(stack, "meat_bat"))
                relic.spreadRelicExperience(player, stack, charge);

            event.setAmount((float) (event.getAmount() + (relic.getStatValue(stack, "meat_bat", "damage") * charge)));
            event.getEntity().addEffect(new MobEffectInstance(EffectRegistry.STUN, (int) Math.round(relic.getStatValue(stack, "meat_bat", "stun") * charge * 20), 0));

            relic.setPieces(stack, 0);
        }

        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {
            if (event.getAction() != ClickAction.PRIMARY)
                return;

            var player = event.getEntity();

            var heldStack = event.getHeldStack();
            var slotStack = event.getSlotStack();

            if (!(heldStack.getItem() instanceof PotionItem) || !(slotStack.getItem() instanceof InfiniteHamItem relic)
                    || !relic.canPlayerUseAbility(player, slotStack, "marinade"))
                return;

            var contents = heldStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            var effects = StreamSupport.stream(contents.getAllEffects().spliterator(), false).toList();

            if (effects.isEmpty())
                slotStack.set(DataComponents.POTION_CONTENTS, null);
            else {
                slotStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects));

                if (relic.isLevelingSourceUnlocked(slotStack, "marinade"))
                    relic.spreadRelicExperience(player, slotStack, effects.size());
            }

            var bottle = new ItemStack(Items.GLASS_BOTTLE);

            if (player.containerMenu.getCarried().getCount() <= 1)
                player.containerMenu.setCarried(bottle);
            else {
                player.containerMenu.getCarried().shrink(1);

                EntityUtils.addItem(player, bottle);
            }

            player.playSound(SoundEvents.BOTTLE_FILL, 1F, 1F);

            event.setCanceled(true);
        }
    }
}