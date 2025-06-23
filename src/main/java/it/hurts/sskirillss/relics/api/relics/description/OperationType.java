package it.hurts.sskirillss.relics.api.relics.description;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class OperationType {
    private final String id;

    private final DescriptionCategory category;
}