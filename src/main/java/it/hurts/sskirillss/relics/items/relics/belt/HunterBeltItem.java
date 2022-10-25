package it.hurts.sskirillss.relics.items.relics.belt;

import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

public class HunterBeltItem extends RelicItem {
    @Override
    public RelicData getRelicData() {
        return RelicData.builder()
                .abilityData(RelicAbilityData.builder()
                        .ability("slots", RelicAbilityEntry.builder()
                                .requiredPoints(2)
                                .stat("talisman", RelicAbilityStat.builder()
                                        .initialValue(1D, 2D)
                                        .upgradeModifier("add", 1D)
                                        .formatValue(value -> String.valueOf((int) (MathUtils.round(value, 0))))
                                        .build())
                                .build())
                        .ability("training", RelicAbilityEntry.builder()
                                .stat("damage", RelicAbilityStat.builder()
                                        .initialValue(1.25D, 2D)
                                        .upgradeModifier("add", 0.25F)
                                        .formatValue(value -> String.valueOf((int) (MathUtils.round(value, 3) * 100)))
                                        .build())
                                .build())
                        .build())
                .levelingData(new RelicLevelingData(100, 10, 100))
                .styleData(RelicStyleData.builder()
                        .borders("#32a167", "#16702e")
                        .build())
                .build();
    }

    @Override
    public RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return RelicSlotModifier.builder()
                .entry(Pair.of("talisman", (int) Math.round(getAbilityValue(stack, "slots", "talisman"))))
                .build();
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class HunterBeltEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof TamableAnimal pet)
                    || !(pet.getOwner() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.HUNTER_BELT.get());

            if (stack.isEmpty())
                return;

            addExperience(player, stack, 1);

            event.setAmount((float) (event.getAmount() * getAbilityValue(stack, "training", "damage")));
        }
    }
}