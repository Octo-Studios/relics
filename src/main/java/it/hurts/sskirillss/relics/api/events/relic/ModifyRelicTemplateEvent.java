package it.hurts.sskirillss.relics.api.events.relic;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
@EqualsAndHashCode(callSuper = false)
public class ModifyRelicTemplateEvent extends RelicEvent {
    private RelicTemplate template;

    public ModifyRelicTemplateEvent(LivingEntity bearer, ItemStack stack, RelicTemplate template) {
        super(bearer, stack);

        this.template = template;
    }
}
