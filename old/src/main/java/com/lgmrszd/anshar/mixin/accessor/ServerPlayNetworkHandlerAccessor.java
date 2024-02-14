package com.lgmrszd.anshar.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public interface ServerPlayNetworkHandlerAccessor {
    @Accessor("floatingTicks")
    void anshar$setFloatingTicks(int ticks);
}
