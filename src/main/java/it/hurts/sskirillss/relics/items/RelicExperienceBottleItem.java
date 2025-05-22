package it.hurts.sskirillss.relics.items;

import it.hurts.sskirillss.relics.entities.ThrownRelicExperienceBottle;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RelicExperienceBottleItem extends ItemBase implements ProjectileItem {
    public RelicExperienceBottleItem() {
        super(new Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ThrownRelicExperienceBottle bottle = new ThrownRelicExperienceBottle(RelicsEntities.THROWN_RELIC_EXPERIENCE_BOTTLE.get(), level);

            bottle.setItem(stack);
            bottle.setOwner(player);
            bottle.setPos(player.getEyePosition());
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 1.0F);

            level.addFreshEntity(bottle);
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild)
            stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public Projectile asProjectile(Level p_338868_, Position p_338766_, ItemStack p_338321_, Direction p_338772_) {
        ThrownRelicExperienceBottle thrownexperiencebottle = new ThrownRelicExperienceBottle(RelicsEntities.THROWN_RELIC_EXPERIENCE_BOTTLE.get(), p_338868_);

        thrownexperiencebottle.setPos(p_338766_.x(), p_338766_.y(), p_338766_.z());
        thrownexperiencebottle.setItem(p_338321_);

        return thrownexperiencebottle;
    }
}