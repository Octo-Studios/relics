package it.hurts.sskirillss.relics.init;

import it.hurts.octostudios.octolib.modules.config.ConfigManager;
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

    public static void register() {
        ConfigManager.registerConfig(Reference.MODID, RELICS_CONFIG);

        if (RELICS_CONFIG.isEnabledExtendedConfigs()) {
            for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                if (!(entry.getValue() instanceof IRelicItem relic))
                    continue;

                ConfigManager.registerConfig(entry.getKey().location().getNamespace() + "/relics/" + entry.getKey().location().getPath(), new RelicConfigData(relic));
            }
        }
    }
}