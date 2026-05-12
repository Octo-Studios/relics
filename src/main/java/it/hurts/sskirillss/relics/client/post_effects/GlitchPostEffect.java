package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.post_effects.PostEffect;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.GlitchyMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class GlitchPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        postChain.setUniform("time", player.tickCount + MC.getTimer().getGameTimeDeltaTicks());
    }

    @Override
    public boolean shouldRender() {
        var player = MC.player;

        if (player == null)
            return false;

        var pos = BlockPos.containing(player.getEyePosition());

        if (player.level().getBlockState(pos).getCollisionShape(player.level(), pos).isEmpty())
            return false;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GLITCHY_MANTLE.get())) {
            if (!(stack.getItem() instanceof GlitchyMantleItem relic))
                continue;

            var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("glitch");

            if (relic.isGlitchEnabled(player, stack) && ability.getRankModifierData("phase").isEnabled())
                return true;
        }

        return false;
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/glitch.json");
    }
}
