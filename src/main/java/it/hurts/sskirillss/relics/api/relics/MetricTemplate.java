package it.hurts.sskirillss.relics.api.relics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MetricTemplate {
    private final String id;

    private final Function<Double, ? extends String> formatValue;
}