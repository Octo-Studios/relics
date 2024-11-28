package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.utils.Reference;
import lombok.Builder;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;

@Data
@Builder
public class LevelingSourceData {
    private final String id;

    public static LevelingSourceDataBuilder builder(String id) {
        LevelingSourceDataBuilder builder = new LevelingSourceDataBuilder();

        builder.id(id);

        return builder;
    }

    @Builder.Default
    private ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/abilities/missing.png");

    @Builder.Default
    private int maxLevel = 0;

    @Builder.Default
    private int cost = 0;

    public static class LevelingSourceDataBuilder {
        private LevelingSourceDataBuilder id(String id) {
            this.id = id;

            return this;
        }
    }
}