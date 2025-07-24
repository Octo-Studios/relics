package it.hurts.sskirillss.relics.api.relics.abilities;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.misc.function.QuadFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;

import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperienceSourceTemplate {
    private final String id;

    private final TriPredicate<LivingEntity, ItemStack, String> condition;

    private final QuadFunction<LivingEntity, ItemStack, String, String, MutableComponent> descriptionComponent;
    private final QuadFunction<LivingEntity, ItemStack, String, String, MutableComponent> conditionComponent;

    public static ExperienceSourceTemplateBuilder builder(String id) {
        return new ExperienceSourceTemplateBuilder(id);
    }

    public ExperienceSourceTemplateBuilder toBuilder() {
        return new ExperienceSourceTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class ExperienceSourceTemplateBuilder {
        private String id;

        private TriPredicate<LivingEntity, ItemStack, String> condition = (entity, stack, ability) -> true;
        private QuadFunction<LivingEntity, ItemStack, String, String, MutableComponent> descriptionComponent = (entity, stack, ability, source) -> Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability + ".experience_source." + source);
        private QuadFunction<LivingEntity, ItemStack, String, String, MutableComponent> conditionComponent = (entity, stack, ability, source) -> Component.empty();

        private ExperienceSourceTemplateBuilder(String id) {
            this.id = id;
        }

        private ExperienceSourceTemplateBuilder(ExperienceSourceTemplate base) {
            this.id = base.getId();

            this.condition = base.getCondition();
            this.descriptionComponent = base.getDescriptionComponent();
            this.conditionComponent = base.getConditionComponent();
        }

        public ExperienceSourceTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public ExperienceSourceTemplateBuilder condition(TriPredicate<LivingEntity, ItemStack, String> condition) {
            this.condition = condition;

            return this;
        }

        public ExperienceSourceTemplateBuilder rankModifierCondition(String rankModifier) {
            this.condition((entity, stack, ability) -> ((IRelicItem) stack.getItem()).isAbilityRankModifierUnlocked(entity, stack, ability, rankModifier));

            this.conditionComponent = (entity, stack, ability, source) -> ((IRelicItem) stack.getItem()).getAbilityTemplate(entity, stack, ability).getRankModifiers().entries().stream()
                    .filter(entry -> entry.getValue().equals(rankModifier))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .map(integer -> Component.translatable("tooltip.relics.description.ability.experience_source.condition.rank", integer)).orElseGet(Component::empty);

            return this;
        }

        public ExperienceSourceTemplate build() {
            return new ExperienceSourceTemplate(this.id, this.condition, this.descriptionComponent, this.conditionComponent);
        }
    }
}