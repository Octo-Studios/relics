package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.misc.function.Function3;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Function;

@Data
public class AbilityMetricTemplate extends MetricTemplate {
    private final Function3<LivingEntity, ItemStack, String, Component> descriptionComponent;
    private final Function3<LivingEntity, ItemStack, String, MutableComponent> conditionComponent;
    private final Function3<LivingEntity, ItemStack, String, VisibilityState> visibilityState;

    private AbilityMetricTemplate(String id, Function<Double, ? extends String> formatValue, Function3<LivingEntity, ItemStack, String, Component> descriptionComponent, Function3<LivingEntity, ItemStack, String, MutableComponent> conditionComponent, Function3<LivingEntity, ItemStack, String, VisibilityState> visibilityState) {
        super(id, formatValue);

        this.descriptionComponent = descriptionComponent;
        this.conditionComponent = conditionComponent;
        this.visibilityState = visibilityState;
    }

    public static MetricTemplateBuilder builder(String id) {
        return new MetricTemplateBuilder(id);
    }

    public MetricTemplateBuilder toBuilder() {
        return new MetricTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class MetricTemplateBuilder {
        private String id;

        private Function<Double, ? extends String> formatValue = String::valueOf;
        private Function3<LivingEntity, ItemStack, String, Component> descriptionComponent = (entity, stack, ability) -> Component.translatable("relics.description." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability + ".statistic." + this.id);
        private Function3<LivingEntity, ItemStack, String, MutableComponent> conditionComponent = (entity, stack, ability) -> Component.empty();
        private Function3<LivingEntity, ItemStack, String, VisibilityState> visibilityState = (entity, stack, ability) -> VisibilityState.VISIBLE;

        private MetricTemplateBuilder(String id) {
            this.id = id;
        }

        private MetricTemplateBuilder(AbilityMetricTemplate base) {
            this.id = base.getId();

            this.formatValue = base.getFormatValue();
            this.descriptionComponent = base.getDescriptionComponent();
            this.conditionComponent = base.getConditionComponent();
            this.visibilityState = base.getVisibilityState();
        }

        public MetricTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public MetricTemplateBuilder formatValue(Function<Double, ? extends String> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public MetricTemplateBuilder descriptionComponent(Function3<LivingEntity, ItemStack, String, Component> descriptionComponent) {
            this.descriptionComponent = descriptionComponent;

            return this;
        }

        public MetricTemplateBuilder visibilityState(Function3<LivingEntity, ItemStack, String, VisibilityState> visibilityState) {
            var prevState = this.visibilityState;

            this.visibilityState = (entity, stack, ability) -> {
                var state = prevState.apply(entity, stack, ability);

                if (state != VisibilityState.VISIBLE)
                    return state;

                return visibilityState.apply(entity, stack, ability);
            };

            return this;
        }

        public MetricTemplateBuilder rankModifierVisibilityState(String rankModifier, VisibilityState state) {
            this.conditionComponent = (entity, stack, ability) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                if (abilityData == null)
                    return Component.empty();

                return abilityData.getTemplate().getRankModifiers().entries().stream()
                        .filter(entry -> entry.getValue().equals(rankModifier))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .map(integer -> Component.translatable("relics.description.ability.statistic.condition.rank", integer))
                        .orElseGet(Component::empty);
            };

            return this.visibilityState((entity, stack, ability) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                return abilityData != null && abilityData.getRankModifierData(rankModifier).isUnlocked() ? VisibilityState.VISIBLE : state;
            });
        }

        public MetricTemplateBuilder modeVisibilityState(String mode, VisibilityState state) {
            return this.visibilityState((entity, stack, ability) -> {
                var relic = (IRelicItem) stack.getItem();
                var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

                return abilityData != null && abilityData.getMode().equals(mode) ? state : VisibilityState.VISIBLE;
            });
        }

        public AbilityMetricTemplate build() {
            return new AbilityMetricTemplate(id, formatValue, descriptionComponent, conditionComponent, visibilityState);
        }
    }
}
