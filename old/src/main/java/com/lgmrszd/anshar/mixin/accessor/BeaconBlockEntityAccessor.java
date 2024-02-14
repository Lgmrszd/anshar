package com.lgmrszd.anshar.mixin.accessor;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
    @Accessor("level")
    int getLevel();

    @Invoker("updateLevel")
    static int updateLevel(World world, int x, int y, int z) {
        return 0;
    }
}
