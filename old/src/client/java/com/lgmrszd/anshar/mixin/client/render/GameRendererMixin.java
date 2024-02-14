package com.lgmrszd.anshar.mixin.client.render;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow private MinecraftClient client;
    @Shadow private boolean renderHand;

    private boolean networkMode = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void anshar$render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        networkMode = client.getCameraEntity() instanceof ClientPlayerEntity player && PlayerTransportComponent.KEY.get(player).isInNetwork();
    }

    @Redirect(
        method = "renderWorld", 
        at = @At(
            value = "FIELD", 
            target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", 
            opcode = Opcodes.GETFIELD 
    ))
    private boolean proxyRenderHand(GameRenderer thisButImAccessingItFunny) {
        return this.renderHand && !networkMode;
    }
}
