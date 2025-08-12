package it.hurts.sskirillss.relics.init;

import it.hurts.octostudios.octolib.module.config.ConfigManager;
import it.hurts.sskirillss.relics.config.LootConfigData;
import it.hurts.sskirillss.relics.config.RelicsConfigData;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ConfigRegistry {
    public static final RelicsConfigData RELICS_CONFIG = new RelicsConfigData();
    public static final LootConfigData LOOT_CONFIG = new LootConfigData();

    public static void register() {
        ConfigManager.registerConfig(Reference.MODID, RELICS_CONFIG);

        if (RELICS_CONFIG.isEnabledExtendedConfigs()) {
            ConfigManager.registerConfig(Reference.MODID + "/loot", LOOT_CONFIG);

            for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                if (!(entry.getValue() instanceof IRelicItem relic))
                    continue;

                var data = relic.constructDefaultConfigData(new RelicConfigData(relic));

                if (data == null)
                    continue;

                ConfigManager.registerConfig(relic.getConfigRoute() + "/relics/" + entry.getKey().location().getPath(), data);
            }
        }
    }
}