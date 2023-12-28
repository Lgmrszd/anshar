package com.lgmrszd.anshar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.beacon.IBeaconComponent;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockWithEntity {
    private BeaconBlockMixin(AbstractBlock.Settings settings) {super(settings);}
    
    @Inject(method = "onSteppedOn", at = @At("HEAD"))
    public void Anshar_BeaconBlock_onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            world.getBlockEntity(pos, BlockEntityType.BEACON).ifPresent(beacon -> 
                IBeaconComponent.KEY.get(beacon).onPlayerSteppedOn(player)
            );
        }
   }
}
