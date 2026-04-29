package it.hurts.sskirillss.relics.api.relics.abilities;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.misc.function.Function3;
import it.hurts.sskirillss.relics.misc.function.Function4;
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

    private final Function4<LivingEntity, ItemStack, String, String, VisibilityState> visibilityState;
    private final Function4<LivingEntity, ItemStack, String, String, MutableComponent> descriptionComponent;
    private final Function4<LivingEntity, ItemStack, String, String, MutableComponent> conditionComponent;

    public static ExperienceSourceTemplateBuilder builder(String id) {
        return new ExperienceSourceTemplateBuilder(id);
    }

    public ExperienceSourceTemplateBuilder toBuilder() {
        return new ExperienceSourceTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class ExperienceSourceTemplateBuilder {
        private String id;

        private Function4<LivingEntity, ItemStack, String, String, VisibilityState> visibilityState = (entity, stack, ability, source) -> VisibilityState.VISIBLE;
        private Function4<LivingEntity, ItemStack, String, String, MutableComponent> descriptionComponent = (entity, stack, ability, source) -> Component.translatable("relics.description." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability + ".experience_source." + source);
        private Function4<LivingEntity, ItemStack, String, String, MutableComponent> conditionComponent = (entity, stack, ability, source) -> Component.empty();

        private ExperienceSourceTemplateBuilder(String id) {
            this.id = id;
        }

        private ExperienceSourceTemplateBuilder(ExperienceSourceTemplate base) {
            this.id = base.getId();

            this.visibilityState = base.getVisibilityState();
            this.descriptionComponent = base.getDescriptionComponent();
            this.conditionComponent = base.getConditionComponent();
        }

        public ExperienceSourceTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public ExperienceSourceTemplateBuilder visibilityState(Function4<LivingEntity, ItemStack, String, String, VisibilityState> visibilityState) {
            var prevState = this.visibilityState;

            this.visibilityState = (entity, stack, ability, source) -> {
                var state = prevState.apply(entity, stack, ability, source);

                if (state != VisibilityState.VISIBLE)
                    return state;

                return visibilityState.apply(entity, stack, ability, source);
            };

            return this;
        }

        public ExperienceSourceTemplateBuilder rankModifierVisibilityState(String rankModifier, VisibilityState state) {
            this.conditionComponent = (entity, stack, ability, source) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                if (abilityData == null)
                    return Component.empty();

                return abilityData.getTemplate().getRankModifiers().entries().stream()
                        .filter(entry -> entry.getValue().equals(rankModifier))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .map(integer -> Component.translatable("relics.description.ability.experience_source.condition.rank", integer))
                        .orElseGet(Component::empty);
            };

            return this.visibilityState((entity, stack, ability, source) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                return abilityData != null && abilityData.getRankModifierData(rankModifier).isUnlocked() ? VisibilityState.VISIBLE : state;
            });
        }

        public ExperienceSourceTemplateBuilder modeVisibilityState(String mode, VisibilityState state) {
            return this.visibilityState((entity, stack, ability, source) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                return abilityData != null && abilityData.getMode().equals(mode) ? state : VisibilityState.VISIBLE;
            });
        }

        public ExperienceSourceTemplate build() {
            return new ExperienceSourceTemplate(this.id, this.visibilityState, this.descriptionComponent, this.conditionComponent);
        }
    }
}
