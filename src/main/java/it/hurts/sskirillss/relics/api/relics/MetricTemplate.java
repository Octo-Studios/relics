package it.hurts.sskirillss.relics.api.relics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricTemplate {
    private final String id;

    private final Function<Double, ? extends String> formatValue;

    public static MetricTemplateBuilder builder(String id) {
        return new MetricTemplateBuilder(id);
    }

    public MetricTemplateBuilder toBuilder() {
        return new MetricTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class MetricTemplateBuilder {
        private String id;
        private Function<Double, ? extends String> formatValue;

        private MetricTemplateBuilder(String id) {
            this.id = id;
        }

        private MetricTemplateBuilder(MetricTemplate base) {
            this.id = base.getId();

            this.formatValue = base.getFormatValue();
        }

        public MetricTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public MetricTemplateBuilder formatValue(Function<Double, ? extends String> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public MetricTemplate build() {
            return new MetricTemplate(id, formatValue);
        }
    }
}