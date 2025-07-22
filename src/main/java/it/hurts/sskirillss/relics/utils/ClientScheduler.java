package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.Relics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class ClientScheduler {
    private static final Deque<ScheduledTask> allTasksQueue = new LinkedList<>();
    private static final List<ScheduledTask> allTasks = new ArrayList<>();

    public static ScheduledTask schedule(int ticks, Runnable task) {
        var scheduled = new ScheduledTask(ticks, task);

        allTasksQueue.add(scheduled);

        return scheduled;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (!allTasksQueue.isEmpty()) {
            var task = allTasksQueue.removeFirst();

            allTasks.add(task);
        }

        allTasks.removeIf(ScheduledTask::tick);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        allTasks.clear();
        allTasksQueue.clear();
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        allTasks.clear();
        allTasksQueue.clear();
    }

    public static class ScheduledTask {
        private int ticks;
        private final Runnable task;
        private boolean cancelled;

        public ScheduledTask(int ticks, Runnable task) {
            this.ticks = ticks;
            this.task = task;
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean tick() {
            if (cancelled)
                return true;

            if (ticks == 0)
                task.run();

            return --ticks < 0;
        }
    }
}