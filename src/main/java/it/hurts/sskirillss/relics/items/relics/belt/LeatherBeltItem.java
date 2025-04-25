package it.hurts.sskirillss.relics.items.relics.belt;

import com.google.common.collect.Lists;
import it.hurts.sskirillss.relics.api.events.leveling.ExperienceAddEvent;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber
public class LeatherBeltItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("slots")
                                .requiredPoints(2)
                                .stat(StatTemplate.builder("charm")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 1)))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingTemplate(100, 10, 100))
                .loot(LootTemplate.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .build();
    }

    @Override
    public RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) Math.round(getStatValue(entity, stack, "slots", "charm")))
                .build();
    }

    @SubscribeEvent
    public static void onExperienceAdded(ExperienceAddEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack sourceStack = event.getStack();

        if (entity == null || sourceStack.getItem() == ItemRegistry.LEATHER_BELT.get())
            return;

        if (sourceStack.getTags().map(tag -> tag.location().getPath()).anyMatch(tag -> tag.equals("charm"))) {
            ItemStack stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.LEATHER_BELT.get());

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            relic.addRelicExperience(entity, stack, 1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition bone = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 7).addBox(-2.05F, -1.55F, -0.675F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, -2.5F, -0.1516F, 0.0512F, -0.0859F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }
}