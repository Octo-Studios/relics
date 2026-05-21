package it.hurts.sskirillss.relics.api.events.relic;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class GatherRelicTemplateCacheKeyEvent extends RelicEvent {
    private final IRelicItem relic;
    private final List<Entry> entries = new ArrayList<>();
    private boolean cacheable = true;

    public GatherRelicTemplateCacheKeyEvent(LivingEntity bearer, ItemStack stack, IRelicItem relic) {
        super(bearer, stack);

        this.relic = relic;
    }

    public void add(String id, Object value) {
        this.entries.add(new Entry(Objects.requireNonNull(id), Objects.requireNonNull(value)));
    }

    public void disableCache() {
        this.cacheable = false;
    }

    public List<Entry> getEntries() {
        return List.copyOf(this.entries);
    }

    public boolean hasEntries() {
        return !this.entries.isEmpty();
    }

    public record Entry(String id, Object value) {

    }
}
