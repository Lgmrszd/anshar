package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.*;
import com.lgmrszd.anshar.mixin.BeaconBlockEntityAccessor;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class BeaconComponent implements IBeaconComponent {
    private List<List<Identifier>> topBlocks;
    private List<List<Identifier>> bottomBlocks;
    private final BeaconBlockEntity beaconBlockEntity;
    private IFrequencyIdentifierComponent frequency;

    private FrequencyNetwork frequencyNetwork;
    private boolean shouldRestoreFrequencyNetwork;
    private int level;

    public BeaconComponent(BeaconBlockEntity beaconBlockEntity) {
        this.beaconBlockEntity = beaconBlockEntity;
        level = 0;
        topBlocks = Collections.emptyList();
        bottomBlocks = Collections.emptyList();
        shouldRestoreFrequencyNetwork = true;
    }

    public void rescanPyramid() {
        World world = beaconBlockEntity.getWorld();
        if (world == null) return;

        BlockPos pos = beaconBlockEntity.getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        this.level = BeaconBlockEntityAccessor.updateLevel(world, x, y, z);

        if (level == 0) {
            topBlocks = Collections.emptyList();
            bottomBlocks = Collections.emptyList();
            IFrequencyIdentifier oldFreqID = getFreqComponent().get();
            IFrequencyIdentifier newFreqID = generateFrequencyID();
            if (!oldFreqID.equals(newFreqID)) onFrequencyIDUpdate(oldFreqID, newFreqID);
            return;
        }
        if (level == 1) {
            topBlocks = Collections.emptyList();
        } else {
            int up_level = level - 1;
            topBlocks = new ArrayList<>(up_level * 2 + 1);
            for (int i = 0; i < up_level * 2 + 1; i++)
                topBlocks.add(new ArrayList<>(Collections.nCopies(up_level * 2 + 1, null)));
        }
        bottomBlocks = new ArrayList<>(level * 2 + 1);
        for (int i = 0; i < level * 2 + 1; i++)
            bottomBlocks.add(new ArrayList<>(Collections.nCopies(level * 2 + 1, null)));

        // Scan blocks
        // Tob blocks (aka edges of upper layers)
        if (level > 1) {
            int up_level = level - 1;
            for (int layer_x = 0; layer_x <= up_level * 2; layer_x++)
                for (int layer_z = 0; layer_z <= up_level * 2; layer_z++) {
                    int level_y_abs = y - up_level + Math.min(
                            up_level - Math.abs(layer_x - up_level),
                            up_level - Math.abs(layer_z - up_level)
                    );
                    if (level_y_abs == y) level_y_abs = y-1;
                    int level_x_abs = layer_x + x - up_level;
                    int level_z_abs = layer_z + z - up_level;
                    topBlocks.get(layer_x).set(layer_z,
                            Registries.BLOCK.getId(
                                    world.getBlockState(
                                            new BlockPos(
                                                    level_x_abs,
                                                    level_y_abs,
                                                    level_z_abs
                                            )
                                    ).getBlock()
                            )
                    );
                }
        }

        // Bottom blocks (bottom layer)
        int level_y_abs = y - level;
        for (int layer_x = 0; layer_x <= level * 2; layer_x++)
            for (int layer_z = 0; layer_z <= level * 2; layer_z++) {
                int level_x_abs = layer_x + x - level;
                int level_z_abs = layer_z + z - level;
                bottomBlocks.get(layer_x).set(layer_z,
                        Registries.BLOCK.getId(
                                world.getBlockState(
                                        new BlockPos(
                                                level_x_abs,
                                                level_y_abs,
                                                level_z_abs
                                        )
                                ).getBlock()
                        )
                );
            }
        IFrequencyIdentifier oldFreqID = getFreqComponent().get();
        IFrequencyIdentifier newFreqID = generateFrequencyID();
        if (!oldFreqID.equals(newFreqID)) onFrequencyIDUpdate(oldFreqID, newFreqID);
//        getFreqComponent().set(arraysHashCode());
    }

    private void onFrequencyIDUpdate(IFrequencyIdentifier oldFreqID, IFrequencyIdentifier newFreqID) {
        World world = beaconBlockEntity.getWorld();
        if (world == null) {
            return;
        }
        getFreqComponent().set(newFreqID);
        shouldRestoreFrequencyNetwork = true;
//        NetworkManagerComponent networkManagerComponent = world.getLevelProperties().getComponent(NetworkManagerComponent.KEY);
//        getFreqComponent().set(newFreqID);
//        if (frequencyNetwork != null) {
//            frequencyNetwork.getBeacons().remove(beaconBlockEntity.getPos());
//        }
//        if (!newFreqID.isValid()) {
//            frequencyNetwork = null;
//            return;
//        }
//        frequencyNetwork = networkManagerComponent.getOrCreateNetwork(newFreqID);
//        frequencyNetwork.getBeacons().add(beaconBlockEntity.getPos());
        beaconBlockEntity.getWorld().getPlayers().forEach(playerEntity -> {
            playerEntity.sendMessage(Text.of(
                    String.format("Frequency updated!!\nOld: %s\nNew: %s", oldFreqID, newFreqID))
            );
        });
        //        LOGGER.debug();
    }

    private void tryUpdateFrequencyNetwork() {
        if (!shouldRestoreFrequencyNetwork) return;
        World world = beaconBlockEntity.getWorld();
        if (world == null) {
            LOGGER.warn("Tried to restore frequency network and epic failed!!");
            return;
        }
        if (frequencyNetwork != null) {
            frequencyNetwork.getBeacons().remove(beaconBlockEntity.getPos());
        }
        if (!getFrequencyID().isValid()) {
            frequencyNetwork = null;
            return;
        }
        // TODO: basically the same thing, but the commented one is a weird mixin thingy that VSCode doesn't like, need to figure out if its safe
//        NetworkManagerComponent networkManagerComponent = world.getLevelProperties().getComponent(NetworkManagerComponent.KEY);
        NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
        frequencyNetwork = networkManagerComponent.getOrCreateNetwork(getFrequencyID());
        frequencyNetwork.getBeacons().add(beaconBlockEntity.getPos());
        shouldRestoreFrequencyNetwork = false;
    }

    private int arraysHashCode() {
        int blockHash = topBlocks.hashCode();
        blockHash = 31 * blockHash + bottomBlocks.hashCode();
        return blockHash;
    }

    private IFrequencyIdentifier generateFrequencyID() {
        if (level == 0) return NullFrequencyIdentifier.get();
        return new HashFrequencyIdentifier(arraysHashCode());
    }

    // components may not be ready in order, so get this lazily and cache
    private IFrequencyIdentifierComponent getFreqComponent(){
        if (this.frequency == null)
            this.frequency = (FrequencyIdentifierComponent) IFrequencyIdentifierComponent.KEY.get(beaconBlockEntity);
        return this.frequency;
    }

    @Override
    public IFrequencyIdentifier getFrequencyID() {
        return getFreqComponent().get();
    }

    @Override
    public Optional<FrequencyNetwork> getFrequencyNetwork() {
        return Optional.ofNullable(frequencyNetwork);
    }

    private static List<Identifier> flattenList(List<List<Identifier>> topBlocks, List<List<Identifier>> bottomBlocks) {
//        int size = level == 1 ? 9 :
//                (level * 2 + 1) * (level * 2 + 1) + (level * 2 + 3) * (level * 2 + 3);
        List<Identifier> flattened = new ArrayList<>(topBlocks.size() + bottomBlocks.size());
        bottomBlocks.forEach(flattened::addAll);
        topBlocks.forEach(flattened::addAll);
        return flattened;
    }

    public List<Identifier> flattenList() {
        return flattenList(topBlocks, bottomBlocks);
    }

    public void fromFlattenedList(List<Identifier> flattenedList, int level) {
        int size = flattenedList.size();
        if (size < 9)
            return;
//            throw new RuntimeException("Trying to unflatten list smaller than 9");
        if (size == 9 && level != 1) {
            LOGGER.warn(String.format("Unflattening error: wrong size %d for level %d, for Beacon at %s", size, level, beaconBlockEntity.getPos()));
            return;
        }
        if (size > 9 && (8*level*level + 2) != size) {
            LOGGER.warn(String.format("Unflattening error: wrong size %d for level %d, for Beacon at %s", size, level, beaconBlockEntity.getPos()));
            return;
        }

        int bottomEdge = level * 2 + 1;
        bottomBlocks = new ArrayList<>(bottomEdge);
        for (int i = 0; i < bottomEdge; i++) {
            List<Identifier> slice = flattenedList.subList(i * bottomEdge, (i + 1) * bottomEdge);
            bottomBlocks.add(new ArrayList<>(slice));
        }
        if (size == 9) {
            topBlocks = Collections.emptyList();
            return;
        }
        int offset = bottomEdge * bottomEdge;
        int topEdge = bottomEdge - 2;
        topBlocks = new ArrayList<>(topEdge);
        for (int i = 0; i < topEdge; i++) {
            List<Identifier> slice = flattenedList.subList(i * topEdge + offset, (i + 1) * topEdge + offset);
            topBlocks.add(new ArrayList<>(slice));
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        level = tag.getInt("level");
        String blockList = tag.getString("blocks");
        List<Identifier> flatList = Arrays.stream(blockList.split(","))
                .map(Identifier::tryParse)
                .toList();
        fromFlattenedList(flatList, level);
        shouldRestoreFrequencyNetwork = true;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        List<Identifier> flatList = flattenList();
        String blockList = flatList.stream()
                .map(Identifier::toString)
                .collect(StringBuilder::new, (builder, str) -> builder.append(str).append(","), StringBuilder::append)
                .toString();
        tag.putInt("level", level);
        tag.putString("blocks", blockList);
    }

    @Override
    public void serverTick() {
        World world = beaconBlockEntity.getWorld();
        tryUpdateFrequencyNetwork();
        if (world == null) return;
        if (world.getTime() % 80L == 0L) {
            rescanPyramid();
        }

        if (frequencyNetwork != null) {
            Set<BlockPos> teleTargets = frequencyNetwork.getBeacons();

            for (PlayerEntity player : this.playersStandingOn) {
                if (player.getBlockPos() != beaconBlockEntity.getPos().up()) {
                    this.playersStandingOn.remove(player);
                } else {
                    // show available beacons to teleport to
                    // if player is crouched, teleport player to nearest
                    if(player.isSneaking() && world.getTime() % 80L == 0L) {
                        double lookX = Math.sin(player.getHeadYaw());
                        double lookZ = Math.cos(player.getHeadYaw());
                        double lookY = Math.cos(player.getRoll());
                        System.out.println("you are standing on beacon " + beaconBlockEntity);
                    }
                }
            }
        }
    }

    public Set<PlayerEntity> playersStandingOn = new HashSet<>();

    @Override
    public void onPlayerSteppedOn(PlayerEntity player) {
        System.out.println("(beacon component) player stepped on");
        playersStandingOn.add(player);
    }


}
