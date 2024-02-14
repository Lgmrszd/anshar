package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.storage.EmbeddedStorage;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.lgmrszd.anshar.Anshar.LOGGER;

// mixins anonymous inner EnderChestBlockEntity.ViewerCountManager class
@Mixin(targets = "net/minecraft/block/entity/EnderChestBlockEntity$1")
public class EnderChestBEVCMMixin {
    
    @Shadow(remap = false, aliases = {"field_27218"}) @Final private EnderChestBlockEntity anshar$bevcOwner;

    @Inject(at = @At("HEAD"), method = "isPlayerViewing", cancellable = true)
    public void injected(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player.currentScreenHandler instanceof GenericContainerScreenHandler gcsh) {
            Inventory inventory = gcsh.getInventory();
            if (inventory instanceof EmbeddedStorage embeddedStorage) {
                LOGGER.info("Has embedded inventory opened: {}", embeddedStorage);
                cir.setReturnValue(embeddedStorage.isActiveBlockEntity(anshar$bevcOwner));
            }
        }
    }
}
