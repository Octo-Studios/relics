package it.hurts.sskirillss.relics.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PetBoneItem extends ItemBase {
    public PetBoneItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide())
            return InteractionResultHolder.pass(stack);

        var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneData.EMPTY);

        if (!data.isValid() || data.collectedHealth() < data.requiredHealth())
            return InteractionResultHolder.fail(stack);

        var id = ResourceLocation.tryParse(data.entityType());

        if (id == null)
            return InteractionResultHolder.fail(stack);

        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);

        if (type == null)
            return InteractionResultHolder.fail(stack);

        var entity = type.create(level);

        if (!(entity instanceof LivingEntity living))
            return InteractionResultHolder.fail(stack);

        var tag = data.entityData().copy();

        tag.remove("UUID");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");

        living.load(tag);
        living.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        living.setHealth(living.getMaxHealth());

        level.addFreshEntity(living);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.1F);

        if (!player.getAbilities().instabuild)
            stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneData.EMPTY);

        return data.isValid() && data.requiredHealth() > 0D
                && data.collectedHealth() < data.requiredHealth();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneData.EMPTY);

        if (!data.isValid() || data.requiredHealth() <= 0D)
            return 0;

        var progress = (float) (data.collectedHealth() / data.requiredHealth());

        return Math.round(Math.clamp(progress, 0F, 1F) * 13F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneData.EMPTY);

        if (!data.isValid() || data.requiredHealth() <= 0D)
            return super.getBarColor(stack);

        var progress = Math.clamp((float) (data.collectedHealth() / data.requiredHealth()), 0F, 1F);

        return java.awt.Color.HSBtoRGB(progress / 3F, 1F, 1F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneData.EMPTY);

        return data.isValid() && data.collectedHealth() >= data.requiredHealth();
    }

    public record PetBoneData(String entityType, CompoundTag entityData, double requiredHealth, double collectedHealth, UUID owner) {
        public static final Codec<PetBoneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("entity_type").forGetter(PetBoneData::entityType),
                CompoundTag.CODEC.fieldOf("entity_data").forGetter(PetBoneData::entityData),
                Codec.DOUBLE.fieldOf("required_health").forGetter(PetBoneData::requiredHealth),
                Codec.DOUBLE.fieldOf("collected_health").forGetter(PetBoneData::collectedHealth),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(PetBoneData::owner)
        ).apply(instance, PetBoneData::new));

        public static final PetBoneData EMPTY = new PetBoneData("", new CompoundTag(), 0D, 0D, Util.NIL_UUID);

        public static PetBoneData fromEntity(LivingEntity entity, UUID owner, double requiredHealth) {
            var data = new CompoundTag();

            entity.saveWithoutId(data);

            data.remove("UUID");
            data.remove("Pos");
            data.remove("Motion");
            data.remove("Rotation");

            var typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

            return new PetBoneData(typeId, data, Math.max(1D, requiredHealth), 0D, owner);
        }

        public boolean isValid() {
            return !this.entityType().isBlank() && this.requiredHealth() > 0D && !this.owner().equals(Util.NIL_UUID);
        }

        public PetBoneData withCollectedHealth(double health) {
            return new PetBoneData(this.entityType(), this.entityData(), this.requiredHealth(), Math.max(0D, health), this.owner());
        }
    }
}
