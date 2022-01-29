package it.hurts.sskirillss.relics.items.relics.hands;

import it.hurts.sskirillss.relics.client.renderer.items.models.EndersHandModel;
import it.hurts.sskirillss.relics.client.tooltip.base.AbilityTooltip;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicTooltip;
import it.hurts.sskirillss.relics.configs.data.relics.RelicConfigData;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStats;
import it.hurts.sskirillss.relics.utils.DurabilityUtils;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

public class EndersHandItem extends RelicItem<EndersHandItem.Stats> {
    public static final String TAG_UPDATE_TIME = "time";

    public EndersHandItem() {
        super(RelicData.builder()
                .rarity(Rarity.RARE)
                .hasAbility()
                .build());
    }

    @Override
    public RelicTooltip getTooltip(ItemStack stack) {
        return RelicTooltip.builder()
                .borders("#00c98f", "#027f44")
                .ability(AbilityTooltip.builder()
                        .build())
                .ability(AbilityTooltip.builder()
                        .active(Minecraft.getInstance().options.keyShift)
                        .build())
                .ability(AbilityTooltip.builder()
                        .active()
                        .build())
                .build();
    }

    @Override
    public RelicConfigData<Stats> getConfigData() {
        return RelicConfigData.<Stats>builder()
                .stats(new Stats())
                .build();
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        if (!(livingEntity instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) livingEntity;
        int time = NBTUtils.getInt(stack, TAG_UPDATE_TIME, 0);

        if (player.getCooldowns().isOnCooldown(stack.getItem()) || DurabilityUtils.isBroken(stack))
            return;

        if (player.isShiftKeyDown()) {
            Predicate<Entity> predicate = (entity) -> !entity.isSpectator() && entity.isPickable();
            EntityRayTraceResult result = EntityUtils.rayTraceEntity(player, predicate, stats.maxDistance);

            if (result != null && result.getEntity() instanceof EndermanEntity) {
                if (time >= stats.preparationTime * 20) {
                    Vector3d swapVec = player.position();
                    EndermanEntity enderman = (EndermanEntity) result.getEntity();
                    World world = player.getCommandSenderWorld();

                    player.teleportTo(enderman.getX(), enderman.getY(), enderman.getZ());
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    enderman.teleportTo(swapVec.x(), swapVec.y(), swapVec.z());
                    world.playSound(null, swapVec.x(), swapVec.y(), swapVec.z(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    NBTUtils.setInt(stack, TAG_UPDATE_TIME, 0);

                    player.getCooldowns().addCooldown(stack.getItem(), stats.cooldown * 20);
                } else
                    NBTUtils.setInt(stack, TAG_UPDATE_TIME, time + 1);
            } else if (time > 0)
                NBTUtils.setInt(stack, TAG_UPDATE_TIME, time - 1);
        } else if (time > 0)
            NBTUtils.setInt(stack, TAG_UPDATE_TIME, time - 1);
    }

    @Override
    public void castAbility(PlayerEntity player, ItemStack stack) {
        if (player.getCommandSenderWorld().isClientSide)
            return;

        player.openMenu(new SimpleNamedContainerProvider((windowId, playerInv, playerEntity) ->
                ChestContainer.threeRows(windowId, playerInv, playerEntity.getEnderChestInventory()), stack.getDisplayName()));
        player.playSound(SoundEvents.ENDER_CHEST_OPEN, 1F, 1F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel<LivingEntity> getModel() {
        return new EndersHandModel();
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class EndersHandClientEvents {
        @SubscribeEvent
        public static void onFOVUpdate(FOVUpdateEvent event) {
            ItemStack stack = EntityUtils.findEquippedCurio(event.getEntity(), ItemRegistry.ENDERS_HAND.get());

            if (stack.isEmpty())
                return;

            int time = NBTUtils.getInt(stack, TAG_UPDATE_TIME, 0);

            if (time > 0)
                event.setNewfov(event.getNewfov() - time / 32.0F);
        }
    }

    public static class Stats extends RelicStats {
        public int preparationTime = 1;
        public int maxDistance = 64;
        public int cooldown = 0;
    }
}