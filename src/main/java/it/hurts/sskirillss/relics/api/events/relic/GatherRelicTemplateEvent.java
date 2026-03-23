package it.hurts.sskirillss.relics.api.events.relic;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
public class GatherRelicTemplateEvent extends RelicEvent {
    private RelicTemplate template;

    public GatherRelicTemplateEvent(LivingEntity bearer, ItemStack stack, RelicTemplate template) {
        super(bearer, stack);

        this.template = template;
    }
}
