package com.lgmrszd.anshar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public class BlockMixin extends AbstractBlockMixin {
    @Inject(method = "onSteppedOn", at = @At("HEAD"))
    public void anshar$onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
    }
}
