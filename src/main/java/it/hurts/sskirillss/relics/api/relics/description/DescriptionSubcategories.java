package it.hurts.sskirillss.relics.api.relics.description;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DescriptionSubcategories {
    private static final Map<String, DescriptionSubcategory> SUBCATEGORIES = new HashMap<>();

    public static Map<String, DescriptionSubcategory> getSubcategories() {
        return SUBCATEGORIES;
    }

    @Nullable
    public static DescriptionSubcategory getSubcategory(String id) {
        return DescriptionSubcategories.getSubcategories().get(id);
    }

    public static void registerSubcategory(@Nonnull DescriptionSubcategory category) {
        SUBCATEGORIES.put(category.getId(), category);
    }

    public static void registerSubcategory(@Nonnull Supplier<DescriptionSubcategory> category) {
        DescriptionSubcategories.registerSubcategory(category.get());
    }
}