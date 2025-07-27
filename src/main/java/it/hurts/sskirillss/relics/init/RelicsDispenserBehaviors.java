package it.hurts.sskirillss.relics.init;

import net.minecraft.world.level.block.DispenserBlock;

public class RelicsDispenserBehaviors {
    public static void register() {
        DispenserBlock.registerProjectileBehavior(RelicsItems.RELIC_EXPERIENCE_BOTTLE.get());
    }
}