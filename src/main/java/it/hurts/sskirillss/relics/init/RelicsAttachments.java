package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.PlayerResearchComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicsAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Relics.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerResearchComponent>> PLAYER_RESEARCH = ATTACHMENTS.register("player_research",
            () -> AttachmentType.builder(() -> PlayerResearchComponent.EMPTY)
                    .serialize(PlayerResearchComponent.CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
