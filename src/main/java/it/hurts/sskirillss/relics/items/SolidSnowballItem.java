package it.hurts.sskirillss.relics.items;

import it.hurts.sskirillss.relics.entities.SolidSnowballEntity;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class SolidSnowballItem extends ItemBase {
    public SolidSnowballItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (EntityUtils.findEquippedCurio(entity, RelicsItems.WOOL_MITTEN.get()).isEmpty())
            entity.setTicksFrozen(entity.getTicksFrozen() + 3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        SolidSnowballEntity entity = new SolidSnowballEntity(level);

        entity.setOwner(player);
        entity.setSize(stack.getOrDefault(CHARGE, 0));
        entity.setPos(player.getX(), player.getEyeY(), player.getZ());
        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(entity);

        level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.MASTER, 0.5F, 0.5F);

        if (!player.isCreative())
            stack.shrink(1);

        return InteractionResultHolder.pass(stack);
    }
}