package com.lgmrszd.anshar.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportGateParticle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    private boolean anshar$filterParticles = false;
    @Inject(method = "renderParticles", at = @At("HEAD"))
    public void renderParticles(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        anshar$filterParticles = PlayerTransportComponent.KEY.get(MinecraftClient.getInstance().player).isInNetwork();
    }

    @Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"))
    public void proxyBuildGeometry(Particle particle, VertexConsumer bufferBuilder, Camera camera, float tickDelta) {
        if (anshar$filterParticles && !(particle instanceof TransportGateParticle)) return;
        particle.buildGeometry(bufferBuilder, camera, tickDelta);

    } 
}
