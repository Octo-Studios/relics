package it.hurts.sskirillss.relics.api.relics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricTemplate {
    private final String id;

    private final Function<Double, ? extends String> formatValue;
    private final TriFunction<LivingEntity, ItemStack, Optional<String>, Component> component;
    private final TriPredicate<LivingEntity, ItemStack, Optional<String>> visibilityCondition;

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
        private TriFunction<LivingEntity, ItemStack, Optional<String>, Component> component = (entity, stack, optional) -> {
            var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

            return Component.translatable(optional.map(ability -> "tooltip.relics." + itemId + ".ability." + ability + ".statistic." + this.id).orElseGet(() -> "tooltip.relics." + itemId + ".statistic." + this.id));
        };
        private TriPredicate<LivingEntity, ItemStack, Optional<String>> visibilityCondition = ((entity, stack, optional) -> true);

        private MetricTemplateBuilder(String id) {
            this.id = id;
        }

        private MetricTemplateBuilder(MetricTemplate base) {
            this.id = base.getId();

            this.formatValue = base.getFormatValue();
            this.component = base.getComponent();
            this.visibilityCondition = base.getVisibilityCondition();
        }

        public MetricTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public MetricTemplateBuilder formatValue(Function<Double, ? extends String> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public MetricTemplateBuilder component(TriFunction<LivingEntity, ItemStack, Optional<String>, Component> component) {
            this.component = component;

            return this;
        }

        public MetricTemplateBuilder visibilityCondition(TriPredicate<LivingEntity, ItemStack, Optional<String>> visibilityCondition) {
            this.visibilityCondition = visibilityCondition;

            return this;
        }

        public MetricTemplate build() {
            return new MetricTemplate(id, formatValue, component, visibilityCondition);
        }
    }
}