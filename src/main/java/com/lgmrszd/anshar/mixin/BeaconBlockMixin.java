package com.lgmrszd.anshar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import static com.lgmrszd.anshar.Anshar.LOGGER;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockMixin {    
    @Override
    public void anshar$onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (world.getTime() % 5L == 0L && !world.isClient && entity instanceof PlayerEntity player) {
            world.getBlockEntity(pos, BlockEntityType.BEACON).ifPresent(
                beacon -> IBeaconComponent.KEY.get(beacon).getFrequencyNetwork().ifPresent(
                    network -> {
                        PlayerTransportComponent.KEY.get(player).enterNetwork(network, beacon.getPos());
                    }
                )
            );
        }
    }

    @Override
    public void anshar$onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (state.getBlock().equals(Blocks.BEACON) && !newState.getBlock().equals(Blocks.BEACON)) {
            LOGGER.debug("Detected Beacon Block removed...");
            if (!(world.getBlockEntity(pos) instanceof BeaconBlockEntity bbe)) return;
            // TODO: rewrite this with new NetworkManagerComponent code. Maybe force the component to update with invalid frequency?
            BeaconComponent.KEY.get(bbe).getFrequencyNetwork().ifPresent(frequencyNetwork -> {
                frequencyNetwork.removeBeacon(bbe.getPos());
            });
        }
    }
}
