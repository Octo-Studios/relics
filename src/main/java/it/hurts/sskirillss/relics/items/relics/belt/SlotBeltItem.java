package it.hurts.sskirillss.relics.items.relics.belt;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.PlayerResearchChangedEvent;
import it.hurts.sskirillss.relics.api.relics.data.AbilityStatData;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.HashSet;

@EventBusSubscriber
public abstract class SlotBeltItem extends WearableRelicItem {
    private static final ResourceLocation STACKING_MODIFIER = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "belt_slot_stacking");

    private AbilityStatData getSlotAmount(LivingEntity entity, ItemStack stack) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData("slots").getStatData("amount");
    }

    @Override
    public RelicSlotModifier getSlotModifiers(LivingEntity entity, ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) getSlotAmount(entity, stack).getValue())
                .build();
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getFrom().getItem() instanceof SlotBeltItem || event.getTo().getItem() instanceof SlotBeltItem)
            updateSlots(event.getEntity(), event.getTo());
    }

    @SubscribeEvent
    public static void onPlayerResearchChanged(PlayerResearchChangedEvent event) {
        updateSlots(event.getEntity(), ItemStack.EMPTY);
    }

    private static void updateSlots(LivingEntity entity, ItemStack changedStack) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inventory -> inventory.getStacksHandler("charm").ifPresent(slots -> {
            var adjustment = 0;
            var counted = new HashSet<ItemStack>();

            for (var equipped : inventory.findCurios(stack -> stack.getItem() instanceof SlotBeltItem)) {
                var stack = equipped.stack();
                var stat = ((SlotBeltItem) stack.getItem()).getSlotAmount(entity, stack);
                var applied = slots.getModifiers().get(CuriosApi.getSlotId(equipped.slotContext()));
                var baseSlots = applied == null ? 0 : (int) applied.amount();

                if (stack == changedStack)
                    baseSlots = (int) stat.getValue();

                adjustment -= baseSlots;

                if (counted.contains(stack) || !stat.isAvailable())
                    continue;

                var stacking = stat.getStacking();
                var total = stacking.total();

                if (total.isPresent()) {
                    adjustment += (int) total.getAsDouble();
                    stacking.entries().forEach(entry -> counted.add(entry.stack()));
                } else
                    adjustment += (int) stat.getStackedValue();
            }

            var previous = slots.getModifiers().get(STACKING_MODIFIER);

            if ((previous == null ? 0D : previous.amount()) == adjustment)
                return;

            slots.removeModifier(STACKING_MODIFIER);

            if (adjustment != 0)
                slots.addTransientModifier(new AttributeModifier(STACKING_MODIFIER, adjustment, AttributeModifier.Operation.ADD_VALUE));
        }));
    }
}
