package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.*;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.FallingStarEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicsEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Relics.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<FallingStarEntity>> FALLING_STAR = ENTITIES.register("falling_star", () ->
            EntityType.Builder.<FallingStarEntity>of(FallingStarEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("falling_star")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ConstellationStarEntity>> CONSTELLATION_STAR = ENTITIES.register("constellation_star", () ->
            EntityType.Builder.<ConstellationStarEntity>of(ConstellationStarEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("constellation_star")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RollerSparkEntity>> ROLLER_SPARK = ENTITIES.register("roller_spark", () ->
            EntityType.Builder.<RollerSparkEntity>of(RollerSparkEntity::new, MobCategory.MISC)
                    .sized(0.15F, 0.15F)
                    .build("roller_spark")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ElectricSparkEntity>> ELECTRIC_SPARK = ENTITIES.register("electric_spark", () ->
            EntityType.Builder.<ElectricSparkEntity>of(ElectricSparkEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("electric_spark")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ReflectiveOrbEntity>> REFLECTIVE_ORB = ENTITIES.register("reflective_orb", () ->
            EntityType.Builder.<ReflectiveOrbEntity>of(ReflectiveOrbEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .build("reflective_orb")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ShadowGlaiveEntity>> SHADOW_GLAIVE = ENTITIES.register("shadow_glaive", () ->
            EntityType.Builder.<ShadowGlaiveEntity>of(ShadowGlaiveEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.1F)
                    .build("shadow_glaive")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ShockwaveBlockEntity>> SHOCKWAVE_BLOCK = ENTITIES.register("shockwave_block", () ->
            EntityType.Builder.<ShockwaveBlockEntity>of(ShockwaveBlockEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .build("shockwave_block")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<LeavesBlockEntity>> LEAVES_BLOCK = ENTITIES.register("leaves_block", () ->
            EntityType.Builder.<LeavesBlockEntity>of(LeavesBlockEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .build("leaves_block")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<LifeEssenceEntity>> LIFE_ESSENCE = ENTITIES.register("life_essence", () ->
            EntityType.Builder.<LifeEssenceEntity>of(LifeEssenceEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("life_essence")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DeathEssenceEntity>> DEATH_ESSENCE = ENTITIES.register("death_essence", () ->
            EntityType.Builder.<DeathEssenceEntity>of(DeathEssenceEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("death_essence")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SporeEntity>> SPORE = ENTITIES.register("spore", () ->
            EntityType.Builder.<SporeEntity>of(SporeEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .build("spore")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RelicExperienceOrbEntity>> RELIC_EXPERIENCE_ORB = ENTITIES.register("relic_experience_orb", () ->
            EntityType.Builder.of(RelicExperienceOrbEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .build("relic_experience_orb")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownRelicExperienceBottle>> THROWN_RELIC_EXPERIENCE_BOTTLE = ENTITIES.register("thrown_relic_experience_bottle", () ->
            EntityType.Builder.of(ThrownRelicExperienceBottle::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("thrown_relic_experience_bottle")
    );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}