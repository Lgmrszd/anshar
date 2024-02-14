package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Optional;

public class EndCrystalItemContainer {
    private final ItemStack stack;
    public EndCrystalItemContainer(ItemStack itemStack) {
        stack = itemStack;
    }

    public ActionResult onUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player == null || player.isSpectator()) return ActionResult.PASS;
        BlockPos targetPos = hitResult.getBlockPos();
        if (!player.isSneaking()) return ActionResult.PASS;
        if (world.getBlockState(targetPos).isOf(Blocks.BEACON)) {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.SUCCESS;
            boolean samePos = getBeaconPos().map(pos -> pos.equals(targetPos)).orElse(false);
            if (samePos) {
                clearBeaconPos(serverPlayer);
            } else {
                saveBeaconPos(targetPos);
                player.sendMessage(Text.translatable("anshar.tooltip.end_crystal.use.linked"));
                playLinkingSound(serverPlayer, false);
            }
            return ActionResult.SUCCESS;
        }
        // Case 1.5: clear if pos is present and cancel further stuff
        boolean isChest = world.getBlockState(targetPos).isOf(Blocks.ENDER_CHEST);
        if (getBeaconPos().isPresent() && !isChest) {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.SUCCESS;
            clearBeaconPos(serverPlayer);
            return ActionResult.SUCCESS;
        }
        // Case 2: placing on top of an Ender Chest
        // VanillaCopy: EndCrystalItem
        if (isChest) {
            BlockPos up = targetPos.up();
            if (!world.isAir(up)) return ActionResult.PASS;

            double x = up.getX();
            double y = up.getY();
            double z = up.getZ();
            List<Entity> list = world.getOtherEntities(null, new Box(x, y, z, x + 1.0, y + 2.0, z + 1.0));
            if (!list.isEmpty()) return ActionResult.FAIL;
            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.SUCCESS;
            int maxDistance = ServerConfig.endCrystalMaxDistance.get();


            Optional<BlockPos> beaconPos = getBeaconPos()
                    .or(() -> NetworkManagerComponent.KEY.get(world.getLevelProperties())
                            .getNearestConnectedBeacon(world, up)
                            .map(BlockEntity::getPos)
                    )
                    .filter(pos -> pos.isWithinDistance(up, maxDistance));


            return beaconPos.map(pos -> {
                if (world.getBlockEntity(pos) instanceof BeaconBlockEntity bbe) {
                    List<IEndCrystalComponent> crystals = IBeaconComponent.KEY.get(bbe).getConnectedEndCrystals();
                    if (crystals.size() >= ServerConfig.endCrystalsPerBeacon.get()) {
                        // Highlight current End Crystals
                        // TODO make a more visible effect
                        crystals.forEach(iEndCrystalComponent -> serverWorld.spawnParticles(
                                ParticleTypes.POOF,
                                iEndCrystalComponent.getPos().x+0.5,
                                iEndCrystalComponent.getPos().y+1,
                                iEndCrystalComponent.getPos().z+0.5,
                                5, 0, 0.5, 0, 0.5
                        ));
                        playDenySound(serverWorld, x, y, z);
                        return ActionResult.FAIL;
                    }
                }
                EndCrystalEntity endCrystalEntity = new EndCrystalEntity(world, x + 0.5, y, z + 0.5);
                endCrystalEntity.setShowBottom(false);
                world.spawnEntity(endCrystalEntity);
                IEndCrystalComponent component = EndCrystalComponent.KEY.get(endCrystalEntity);
                component.setBeacon(pos);
                world.emitGameEvent(player, GameEvent.ENTITY_PLACE, up);
                player.getStackInHand(hand).decrement(1);
                return ActionResult.SUCCESS;
            }).orElseGet(() -> {
                playDenySound(serverWorld, x, y, z);
                return ActionResult.FAIL;
            });
        }
        return ActionResult.PASS;
    }

    private static void playDenySound(ServerWorld serverWorld, Double x, Double y, Double z) {
        serverWorld.playSound(
                null,
                x, y, z,
                SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                SoundCategory.BLOCKS,
                1f,
                0.5f
        );
    }

    private static void playLinkingSound(ServerPlayerEntity player, boolean clear) {
        player.playSound(
                clear ? SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE : SoundEvents.ITEM_LODESTONE_COMPASS_LOCK,
                SoundCategory.BLOCKS,
                1f,
                clear ? 2.0f : 1.0f
        );
    }

    public void saveBeaconPos(BlockPos pos) {
        NbtCompound tag = stack.getOrCreateNbt();
        NbtCompound posTag = NbtHelper.fromBlockPos(pos);
        tag.put("BeaconPos", posTag);
        stack.setNbt(tag);
    }

    public void clearBeaconPos(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("anshar.tooltip.end_crystal.use.unlinked"));
        playLinkingSound(player, true);
        NbtCompound tag = stack.getNbt();
        if (tag == null) return;
        tag.remove("BeaconPos");
        if (tag.isEmpty()) stack.setNbt(null);
        else stack.setNbt(tag);
    }

    public Optional<BlockPos> getBeaconPos() {
        if (!stack.hasNbt()) return Optional.empty();
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("BeaconPos")) return Optional.empty();
        return Optional.of(NbtHelper.toBlockPos(tag.getCompound("BeaconPos")));
    }
}
