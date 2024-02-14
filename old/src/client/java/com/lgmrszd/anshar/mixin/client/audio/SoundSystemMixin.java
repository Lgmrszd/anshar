package com.lgmrszd.anshar.mixin.client.audio;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.IEmbeddedAudio;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        // cancel all audio  while in embedded space other than those whitelisted with IEmbeddedAudio
        if (client.player != null && !client.isPaused() && PlayerTransportComponent.KEY.get(client.player).isInNetwork() && !(sound instanceof IEmbeddedAudio)) ci.cancel();
    }
}
