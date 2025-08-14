package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.misc.function.Function2;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

@Data
public class RelicMetricTemplate extends MetricTemplate {
    private final Function2<LivingEntity, ItemStack, Component> component;
    private final Function2<LivingEntity, ItemStack, VisibilityState> visibilityState;

    private RelicMetricTemplate(String id, Function<Double, ? extends String> formatValue, Function2<LivingEntity, ItemStack, Component> component, Function2<LivingEntity, ItemStack, VisibilityState> visibilityState) {
        super(id, formatValue);

        this.component = component;
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
        private Function2<LivingEntity, ItemStack, Component> component = (entity, stack) -> Component.translatable("relics.description." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".statistic." + this.id);
        private Function2<LivingEntity, ItemStack, VisibilityState> visibilityState = (entity, stack) -> VisibilityState.VISIBLE;

        private MetricTemplateBuilder(String id) {
            this.id = id;
        }

        private MetricTemplateBuilder(RelicMetricTemplate base) {
            this.id = base.getId();

            this.formatValue = base.getFormatValue();
            this.component = base.getComponent();
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

        public MetricTemplateBuilder component(Function2<LivingEntity, ItemStack, Component> component) {
            this.component = component;

            return this;
        }

        public MetricTemplateBuilder visibilityState(Function2<LivingEntity, ItemStack, VisibilityState> visibilityState) {
            this.visibilityState = visibilityState;

            return this;
        }

        public RelicMetricTemplate build() {
            return new RelicMetricTemplate(id, formatValue, component, visibilityState);
        }
    }
}