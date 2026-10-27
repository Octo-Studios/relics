package it.hurts.sskirillss.relics.init;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class RelicsCoreShaders {
    public static ShaderInstance REVEAL_SHADER = null;
    public static ShaderInstance ASTRAL_NEBULA_SHADER = null;
    public static ShaderInstance ASTRAL_VOID_SHADER = null;
    public static RenderStateShard.ShaderStateShard REVEAL_SHADER_SHARD = new RenderStateShard.ShaderStateShard(()->REVEAL_SHADER);

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),ResourceLocation.tryBuild(Relics.MODID,"reveal_panel"), DefaultVertexFormat.POSITION_TEX),
                (inst)->{
                    REVEAL_SHADER = inst;
                });

        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_nebula"), DefaultVertexFormat.POSITION),
                (inst) -> {
                    ASTRAL_NEBULA_SHADER = inst;
                });

        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_void"), DefaultVertexFormat.POSITION_TEX),
                (inst) -> {
                    ASTRAL_VOID_SHADER = inst;
                });
    }
}
