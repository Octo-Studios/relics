package it.hurts.sskirillss.relics.api.relics.description;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DescriptionCategories {
    private static final Map<String, DescriptionCategory> CATEGORIES = new HashMap<>();

    public static Map<String, DescriptionCategory> getCategories() {
        return CATEGORIES;
    }

    @Nullable
    public static DescriptionCategory getCategory(String id) {
        return DescriptionCategories.getCategories().get(id);
    }

    public static void registerCategory(@Nonnull DescriptionCategory category) {
        CATEGORIES.put(category.getId(), category);
    }

    public static void registerCategory(@Nonnull Supplier<DescriptionCategory> category) {
        DescriptionCategories.registerCategory(category.get());
    }
}