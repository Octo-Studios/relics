package it.hurts.sskirillss.relics.client.renderer.items.handlers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayDeque;
import java.util.Deque;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class ArmorRenderHandler {
    private static final LoadingCache<Object, Deque<Runnable>> cache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Object, Deque<Runnable>>() {
        @Override
        public Deque<Runnable> load(@NotNull Object key) {
            return new ArrayDeque<>();
        }
    });

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handlePostRenderPlayerLow(RenderPlayerEvent.Post event) {
        Player player = event.getPlayer();

        if (!haveBoot(player))
            return;

        restoreItems(cache.getUnchecked(player));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handlePreRenderPlayerHigh(RenderPlayerEvent.Pre event) {
        Player player = event.getPlayer();

        if (!haveBoot(player))
            return;

        Deque<Runnable> queue = cache.getUnchecked(player);

        restoreItems(queue);

        NonNullList<ItemStack> armor = player.getInventory().armor;
        ItemStack stack = armor.get(0);

        queue.add(() -> armor.set(0, stack));
        armor.set(0, ItemStack.EMPTY);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handlePreRenderPlayerLowest(RenderPlayerEvent.Pre event) {
        if (!event.isCanceled())
            return;

        Player player = event.getPlayer();

        if (!haveBoot(player))
            return;

        restoreItems(cache.getUnchecked(player));
    }

    private static void restoreItems(Deque<Runnable> queue) {
        Runnable runnable;

        while ((runnable = queue.poll()) != null) {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean haveBoot(Player player) {
        if (ModList.get().isLoaded("cosmeticarmorreworked"))
            return false;

        LazyOptional<ICuriosItemHandler> helper = CuriosApi.getCuriosHelper().getCuriosHandler(player);

        return helper.map(curios -> curios.getStacksHandler("feet").map(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStacks().getStackInSlot(i);

                if (!stack.isEmpty() && stack.getItem() instanceof IRenderableCurio && handler.getRenders().get(i))
                    return true;
            }

            return false;
        }).orElse(false)).orElse(false);
    }
}