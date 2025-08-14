package it.hurts.sskirillss.relics.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.init.RelicsRelicRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

public abstract class EquippableRelicLayer<T extends LivingEntity, M extends EntityModel<T>> extends RelicLayer<T, M> {
    private final String slot;

    public EquippableRelicLayer(RenderLayerParent<T, M> renderer, String slot) {
        super(renderer);

        this.slot = slot;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CuriosApi.getCuriosInventory(livingEntity).ifPresent(handler -> {
            for (var slot : handler.findCurios(slot)) {
                if (!slot.slotContext().visible())
                    continue;

                var slotStack = slot.stack();
                var renderer = RelicsRelicRenderers.getRenderer(slotStack.getItem());

                if (renderer.isPresent()) {
                    renderer.get().render(slotStack, slot.slotContext(), poseStack, renderLayerParent, buffer, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);

                    break;
                }
            }
        });
    }
}