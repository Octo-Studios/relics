package it.hurts.sskirillss.relics.api.relics.abilities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperienceSourceTemplate {
    private final String id;

    private final TriPredicate<LivingEntity, ItemStack, String> condition;

    public static ExperienceSourceTemplateBuilder builder(String id) {
        return new ExperienceSourceTemplateBuilder(id);
    }

    public ExperienceSourceTemplateBuilder toBuilder() {
        return new ExperienceSourceTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class ExperienceSourceTemplateBuilder {
        private String id;

        private TriPredicate<LivingEntity, ItemStack, String> condition = ((entity, stack, ability) -> true);

        private ExperienceSourceTemplateBuilder(String id) {
            this.id = id;
        }

        private ExperienceSourceTemplateBuilder(ExperienceSourceTemplate base) {
            this.id = base.getId();

            this.condition = base.getCondition();
        }

        public ExperienceSourceTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public ExperienceSourceTemplateBuilder condition(TriPredicate<LivingEntity, ItemStack, String> condition) {
            this.condition = condition;

            return this;
        }

        public ExperienceSourceTemplate build() {
            return new ExperienceSourceTemplate(this.id, this.condition);
        }
    }
}