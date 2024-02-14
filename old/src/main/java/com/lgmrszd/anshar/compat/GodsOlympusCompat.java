package com.lgmrszd.anshar.compat;

import java.util.function.BiConsumer;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public class GodsOlympusCompat {
    // I have done nothing but teleport bread for 3 days
    private static BiConsumer<PlayerEntity, ItemEntity> funny = null;
    public static void setFunny(BiConsumer<PlayerEntity, ItemEntity> consumer) { funny = consumer; }

    public static boolean isPresent() {
        return FabricLoaderImpl.INSTANCE.isModLoaded("gofo");
    }

    public static void doFunny(PlayerEntity player, ItemEntity bread) {
        if (isPresent() && funny != null) {
            funny.accept(player, bread);
        }
    }
}
