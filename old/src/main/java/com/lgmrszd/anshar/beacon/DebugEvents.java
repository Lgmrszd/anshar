package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class DebugEvents {
    private static final List<Pair<Item, UseBlockCallback>> debugEvents = new ArrayList<>();

    public static void register() {
        UseBlockCallback.EVENT.register(DebugEvents::useBlockEvent);

        registerDebugEvent(Items.STICK, (player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof BeaconBlockEntity bbe)) return ActionResult.PASS;
            IBeaconComponent beacComp = IBeaconComponent.KEY.get(bbe);
            if (beacComp.getEffectiveFrequencyID().isValid())
                player.sendMessage(Text.literal(
                        String.format(
                                "Beacon pos: %s, frequency: %s, network: %s",
                                pos,
                                beacComp.getEffectiveFrequencyID().hashCode(),
                                beacComp.getFrequencyNetwork()
                                        .map(frequencyNetwork -> frequencyNetwork.getId().toString())
                                        .orElse("None")
                        )
                ));
            else {
                player.sendMessage(Text.literal(
                        String.format("Beacon pos: %s, no frequency!", pos)
                ));
            }
            return ActionResult.SUCCESS;
        });

        registerDebugEvent(Items.FIREWORK_ROCKET, (player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof BeaconBlockEntity bbe)) return ActionResult.PASS;
            IBeaconComponent beacComp = IBeaconComponent.KEY.get(bbe);
            beacComp.getFrequencyNetwork().ifPresent(frequencyNetwork -> {
                frequencyNetwork.getBeacons().forEach(blockPos -> {
                    FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, player.getMainHandStack());
                    world.spawnEntity(fireworkRocketEntity);
                });
            });
            return ActionResult.SUCCESS;
        });

        registerDebugEvent(Items.NETHER_STAR, (player, world, hand, hitResult) -> {
            NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
            networkManagerComponent
                    .getNearestConnectedBeacon(world, hitResult.getBlockPos())
                    .ifPresentOrElse(
                            bbe -> {
                                IBeaconComponent beacComp = IBeaconComponent.KEY.get(bbe);
                                BlockPos pos = bbe.getPos();
                                if (beacComp.getEffectiveFrequencyID().isValid())
                                    player.sendMessage(Text.literal(
                                            String.format("Nearest beacon pos: %s, frequency: %s", pos, beacComp.getEffectiveFrequencyID().hashCode())
                                    ));
                                else {
                                    player.sendMessage(Text.literal(
                                            String.format("Nearest beacon pos: %s, no frequency!", pos)
                                    ));
                                }
                            }, () -> player.sendMessage(Text.literal("No nearest loaded beacon found!")));

            return ActionResult.SUCCESS;
        });
    }

    private static void registerDebugEvent(Item item, UseBlockCallback useBlockCallback) {
        debugEvents.add(new Pair<>(item, useBlockCallback));
    }

    private static ActionResult callDebugEvents(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (
                world.isClient()
                        || !player.isSneaking()
                        || hand == Hand.OFF_HAND
        ) return ActionResult.PASS;
        for (Pair<Item, UseBlockCallback> itemUseBlockCallbackPair : debugEvents) {
            if (player.isHolding(itemUseBlockCallbackPair.getLeft())) {
                ActionResult res = itemUseBlockCallbackPair.getRight().interact(player, world, hand, hitResult);
                if (res != ActionResult.PASS) return res;
            }
        }
        return ActionResult.PASS;
    }

    private static ActionResult useBlockEvent(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        return callDebugEvents(player, world, hand, hitResult);
    }

}
