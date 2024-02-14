package com.lgmrszd.anshar.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.lgmrszd.anshar.compat.GodsOlympusCompat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

@Mixin(targets = "dev/galiev/gofo/utils/GodsData", remap = false)
@Pseudo
public class GodsOlympusMixin {
    @Shadow(remap = false) private static short removeRepPoseidon(PlayerEntity player, short amount){ return 0; }
    @Shadow(remap = false) private static short removeRepZeus(PlayerEntity player, short amount) { return 0; }
    @Shadow(remap = false) private static short getRepPoseidon(PlayerEntity player) { return 0; }
    @Shadow(remap = false) private static short getRepZeus(PlayerEntity player) { return 0; }

    static {
        GodsOlympusCompat.setFunny((player, item) -> {
            removeRepPoseidon(player, (short)(getRepPoseidon(player)-5));
            removeRepZeus(player, (short)(getRepZeus(player)-5));
            player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS);
            item.kill();
        });
    }
}
