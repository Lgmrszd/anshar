package com.lgmrszd.anshar.dispenser;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;
import com.lgmrszd.anshar.mixin.accessor.DispenserBlockAccessor;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ModDispenserBehaviors {
    public static void register() {
        DispenserBehavior oldFireworkBehavior = DispenserBlockAccessor.getBehaviors().get(Items.FIREWORK_ROCKET);
        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, (pointer, stack) -> {
            BlockPos facingPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            // TODO IDEA gives me weird warning, what??
            ServerWorld world = pointer.world();
            if (world.getBlockEntity(facingPos) instanceof BeaconBlockEntity bbe) {
                IBeaconComponent beaconComponent = BeaconComponent.KEY.get(bbe);
                if (beaconComponent.isActive()) {
                    beaconComponent.getFrequencyNetwork().ifPresent(frequencyNetwork -> {
                        frequencyNetwork.getBeacons().forEach(blockPos -> {
                            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, stack);
                            world.spawnEntity(fireworkRocketEntity);
                        });
                    });
                    stack.decrement(1);
                    return stack;
                }
            }
//            LOGGER.info("Firework injected!!");
            return oldFireworkBehavior.dispense(pointer, stack);
        });
//        DispenserBlock.registerBehavior(Items.AMETHYST_SHARD, (pointer, stack) -> {
//            BlockPos facingPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
//            // TODO IDEA gives me weird warning, what??
//            ServerWorld world = pointer.world();
//            if (world.getBlockEntity(facingPos) instanceof BeaconBlockEntity bbe) {
//                IBeaconComponent beaconComponent = BeaconComponent.KEY.get(bbe);
//                beaconComponent.activate();
//            }
//            return stack;
//        });
    }
}
