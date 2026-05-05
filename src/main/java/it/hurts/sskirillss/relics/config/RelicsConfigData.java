package it.hurts.sskirillss.relics.config;

import com.google.common.collect.Lists;
import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.octostudios.octolib.module.config.impl.OctoConfig;
import lombok.Data;

import java.util.List;

@Data
public class RelicsConfigData implements OctoConfig {
    @Prop(comment = """
            Toggles advanced configuration files, allowing customization of most of the mod's functionality. May contain WIP content that may change in the future.
            
            Activating this feature may lead to unintended consequences, so use it only if you know what you're doing. If any part of the mod update involves changes to the configuration file values, these changes will not be applied automatically. You will need to manually update the necessary sections or reset them to their original state.
            """)
    private boolean enabledExtendedConfigs = false;

    @Prop(comment = """
        A list of entity attributes that are ignored by the Ring of The Seven Deadly Sins' gluttony ability.
        """)
    private List<String> ringOfSDSGluttonyAttributesBlacklist = Lists.newArrayList(
            "minecraft:generic.gravity",
            "minecraft:generic.scale",
            "additionalentityattributes:generic.width",
            "additionalentityattributes:generic.height",
            "additionalentityattributes:generic.hitbox_scale",
            "additionalentityattributes:generic.hitbox_width",
            "additionalentityattributes:generic.hitbox_height",
            "additionalentityattributes:generic.model_scale",
            "additionalentityattributes:generic.model_width",
            "additionalentityattributes:generic.model_height",
            "ars_nouveau:ars_nouveau.perk.weight",
            "ars_nouveau:ars_nouveau.perk.wixie"
    );

    @Prop(comment = """
        A list of entity IDs that cannot be revived by the Hunting Belt.
        """)
    private List<String> huntingBeltRevivalEntitiesBlacklist = Lists.newArrayList(
            "ars_nouveau:animated_block",
            "ars_nouveau:animated_head"
    );
}