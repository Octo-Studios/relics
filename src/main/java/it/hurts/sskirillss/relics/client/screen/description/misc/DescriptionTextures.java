package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DescriptionTextures {
    public static final ResourceLocation PLATE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/plate_background.png");
    public static final ResourceLocation PLATE_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/plate_outline.png");

    public static final ResourceLocation SPACE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/background.png");

    public static final ResourceLocation TOP_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/top_background.png");
    public static final ResourceLocation BOTTOM_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/bottom_background.png");

    public static final ResourceLocation ACTION_BUTTON_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/ability/action_button_outline.png");
    public static final ResourceLocation STAT_DELIMITER = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/ability/stat_delimiter.png");

    public static final ResourceLocation TAB = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tab.png");
    public static final ResourceLocation TAB_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tab_outline.png");

    public static final ResourceLocation BIG_STAR_HOLE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_star_hole.png");
    public static final ResourceLocation BIG_STAR_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_star_active.png");
    public static final ResourceLocation BIG_STAR_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_star_inactive.png");

    public static final ResourceLocation SMALL_CARD_LOCK_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_lock_background.png");
    public static final ResourceLocation SMALL_CARD_RESEARCH_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_research_background.png");
    public static final ResourceLocation SMALL_CARD_FRAME_UNLOCKED_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_frame_unlocked_active.png");
    public static final ResourceLocation SMALL_CARD_FRAME_UNLOCKED_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_frame_unlocked_inactive.png");
    public static final ResourceLocation SMALL_CARD_FRAME_LOCKED_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_frame_locked_active.png");
    public static final ResourceLocation SMALL_CARD_FRAME_LOCKED_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_frame_locked_inactive.png");
    public static final ResourceLocation SMALL_CARD_FRAME_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/small_card_frame_outline.png");

    public static final ResourceLocation RESEARCH_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/research_background.png");
    public static final ResourceLocation RESEARCH_FOG = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/research_fog.png");

    public static final ResourceLocation BIG_CARD_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_background.png");
    public static final ResourceLocation BIG_CARD_FRAME_UNLOCKED_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_frame_unlocked_active.png");
    public static final ResourceLocation BIG_CARD_FRAME_UNLOCKED_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_frame_unlocked_inactive.png");
    public static final ResourceLocation BIG_CARD_FRAME_LOCKED_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_frame_locked_active.png");
    public static final ResourceLocation BIG_CARD_FRAME_LOCKED_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_frame_locked_inactive.png");
    public static final ResourceLocation BIG_CARD_FRAME_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/big_card_frame_outline.png");

    public static final ResourceLocation CHAINS_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/chains_inactive.png");

    public static final ResourceLocation SMALL_STAR_HOLE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/small_star_hole.png");
    public static final ResourceLocation SMALL_STAR_ACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/small_star_active.png");
    public static final ResourceLocation SMALL_STAR_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/small_star_inactive.png");

    public static final ResourceLocation LOCK_INACTIVE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/icons/lock_inactive.png");
    public static final ResourceLocation UPGRADE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/icons/upgrade.png");
    public static final ResourceLocation RESEARCH = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/icons/research.png");

    public static final ResourceLocation RELIC_EXPERIENCE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/relic_experience_background.png");
    public static final ResourceLocation RELIC_EXPERIENCE_FILLER = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/relic_experience_filler.png");
    public static final ResourceLocation RELIC_EXPERIENCE_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/relic/relic_experience_outline.png");

    public static final ResourceLocation PLATE_PLAYER_EXPERIENCE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/plate_player_experience_background.png");
    public static final ResourceLocation PLATE_PLAYER_EXPERIENCE_FILLER = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/plate_player_experience_filler.png");

    public static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/logo.png");

    public static final ResourceLocation HINT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/hint_background.png");
    public static final ResourceLocation HINT_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/hint_outline.png");

    public static final ResourceLocation TIP_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/tip_background.png");
    public static final ResourceLocation TIP_OUTLINE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/tip_outline.png");

    public static final ResourceLocation BULB = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/bulb.png");
    public static final ResourceLocation BULB_BROKEN = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/bulb_broken.png");
    public static final ResourceLocation BULB_GLOWING = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/bulb_glowing.png");
    public static final ResourceLocation BULB_BURNING = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/research/bulb_burning.png");

    private static final ResourceLocation SMALL_CARD_MISSING = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/abilities/missing.png");

    // TODO: Since ability may have different icons based on the relic state we need to implement some sort of default icon that will be used in the description UIs
    @OnlyIn(Dist.CLIENT)
    public static ResourceLocation getAbilityCardTexture(ItemStack stack, String ability) {
        var item = stack.getItem();

        if (!(item instanceof IRelicItem relic))
            return SMALL_CARD_MISSING;

        var minecraft = Minecraft.getInstance();

        var texture = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/abilities/" + BuiltInRegistries.ITEM.getKey(item).getPath() + "/" + relic.getAbilityData(ability).getIcon().apply(minecraft.player, stack, ability) + ".png");

        return minecraft.getResourceManager().getResource(texture).orElse(null) == null ? SMALL_CARD_MISSING : texture;
    }
}