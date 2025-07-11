package it.hurts.sskirillss.relics.items.relics.hands;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Comparator;
import java.util.Optional;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class WoolMittenItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("mold")
                                .stat(StatTemplate.builder("size")
                                        .initialValue(12D, 32D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.05D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.025D, 0.05D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 3))
                                        .build())
                                .stat(StatTemplate.builder("freeze")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.3D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.FROST, LootEntries.TAIGA, LootEntries.MOUNTAIN)
                        .build())
                .build();
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();

            ItemStack relicStack = EntityUtils.findEquippedCurio(player, RelicsItems.WOOL_MITTEN.get());

            if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty() || !(relicStack.getItem() instanceof IRelicItem relic))
                return;

            Level level = player.level();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            int layers = block == Blocks.SNOW ? state.getValue(SnowLayerBlock.LAYERS) : block == Blocks.SNOW_BLOCK ? 9 : 0;

            if (layers == 0)
                return;

            Inventory inventory = player.getInventory();

            int size = (int) Math.round(relic.getStatValue(player, relicStack, "mold", "size"));

            Optional<Integer> slot = EntityUtils.getSlotsWithItem(player, RelicsItems.SOLID_SNOWBALL.get()).stream()
                    .filter(id -> inventory.getItem(id).getOrDefault(CHARGE, 0) < size)
                    .max(Comparator.comparingInt(s -> inventory.items.get(s).getOrDefault(CHARGE, 0)));

            if (slot.isEmpty()) {
                if (inventory.add(new ItemStack(RelicsItems.SOLID_SNOWBALL.get()))) {
                    slot = EntityUtils.getSlotsWithItem(player, RelicsItems.SOLID_SNOWBALL.get()).stream().findFirst();

                    if (slot.isEmpty())
                        return;
                } else
                    return;
            }

            ItemStack stack = inventory.getItem(slot.get());

            level.destroyBlock(pos, false);

            int snow = stack.getOrDefault(CHARGE, 0);

            stack.set(CHARGE, Math.min(snow + layers, size));
        }
    }
}