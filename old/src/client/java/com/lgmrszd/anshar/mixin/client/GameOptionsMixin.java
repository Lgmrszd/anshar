package com.lgmrszd.anshar.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    public void setPerspective(Perspective perspective, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && PlayerTransportComponent.KEY.get(player).isInNetwork() && perspective != Perspective.FIRST_PERSON) {
            ci.cancel();
        }
    }
}
