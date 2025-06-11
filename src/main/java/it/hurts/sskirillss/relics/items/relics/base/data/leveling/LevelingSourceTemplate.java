package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;

import it.hurts.sskirillss.relics.utils.Reference;
import lombok.Builder;
import lombok.Data;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

@Data
@Builder
public class LevelingSourceTemplate {
    private final String id;

    private static LevelingSourceTemplateBuilder builder(String id) {
        var builder = new LevelingSourceTemplateBuilder();

        builder.id(id);

        return builder;
    }

    public static LevelingSourceTemplateBuilder genericBuilder(String id) {
        var builder = builder(id);

        builder.translationPath((stack) -> "tooltip.relics.leveling_source.generic." + id);
        builder.genericIcon(id);

        return builder;
    }

    public static LevelingSourceTemplateBuilder abilityBuilder(String id, String ability) {
        var builder = builder(id);

        builder.requiredAbility(ability);
        builder.abilityIcon(ability);
        builder.translationPath((stack) -> "tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".leveling_source." + id);

        return builder;
    }

    public static LevelingSourceTemplateBuilder abilityBuilder(String ability) {
        return abilityBuilder(ability, ability);
    }

    @Builder.Default
    private int initialValue = 1;

    @Builder.Default
    private Pair<ScalingModel, Integer> upgradeModifier;

    @Builder.Default
    private int maxLevel = 0;

    @Builder.Default
    private int cost = 0;

    @Builder.Default
    private int requiredLevel = 0;
    @Builder.Default
    private String requiredAbility = "";

    @Builder.Default
    private Function<ItemStack, ResourceLocation> icon;
    @Builder.Default
    private Function<ItemStack, String> translationPath;
    @Builder.Default
    private GemShape shape = GemShape.SQUARE;
    @Builder.Default
    private GemColor color = GemColor.RED;

    public static class LevelingSourceTemplateBuilder {
        private Function<ItemStack, ResourceLocation> icon = (stack) -> ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/abilities/missing.png");
        private Function<ItemStack, String> translationPath = (stack) -> "";

        private Pair<ScalingModel, Integer> upgradeModifier = Pair.of(ScalingModelRegistry.ADDITIVE.get(), 0);

        private LevelingSourceTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }

        public LevelingSourceTemplateBuilder upgradeModifier(ScalingModel operation, int step) {
            upgradeModifier = Pair.of(operation, step);

            return this;
        }

        private LevelingSourceTemplateBuilder icon(Function<ItemStack, ResourceLocation> icon) {
            this.icon = icon;

            return this;
        }

        public LevelingSourceTemplateBuilder manualIcon(Function<ItemStack, ResourceLocation> icon) {
            return icon(icon);
        }

        public LevelingSourceTemplateBuilder genericIcon(String icon) {
            return manualIcon((stack) -> ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/leveling_source/generic/" + icon + ".png"));
        }

        public LevelingSourceTemplateBuilder abilityIcon(String ability) {
            return manualIcon((stack) -> DescriptionTextures.getAbilityCardTexture(stack, ability));
        }

        public LevelingSourceTemplateBuilder gem(GemShape shape, GemColor color) {
            this.shape(shape);
            this.color(color);

            return this;
        }
    }
}