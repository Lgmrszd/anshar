package com.lgmrszd.anshar.frequency;

import java.util.*;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.storage.EmbeddedStorage;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class FrequencyNetwork {
    private final UUID id;
    private final IFrequencyIdentifier freqID;
    private EmbeddedStorage storage;
    private final Map<BlockPos, BeaconNode> beacons = new HashMap<>();
//    private final Map<BlockPos, BeaconNode> beacons = new Object2ReferenceOpenHashMap<>();

    public FrequencyNetwork(UUID id, IFrequencyIdentifier FreqID) {
        this.id = id;
        this.freqID = FreqID;
    }

    public UUID getId() {
        return id;
    }

    public IFrequencyIdentifier getFreqID() {
        return freqID;
    }

    public Set<BlockPos> getBeacons() {
        return Collections.unmodifiableSet(this.beacons.keySet());
    }

    // TODO: find a way to make it protected?
    public boolean removeBeacon (BlockPos beaconPos) {
        return beacons.remove(beaconPos) != null;
    }

    public boolean removeBeacon (BeaconComponent beaconComponent) {
        return beacons.remove(beaconComponent.getBeaconPos()) != null;
    }

    protected boolean addBeacon (BeaconComponent beaconComponent) {
        BeaconNode node = new BeaconNode(beaconComponent);
        return beacons.put(node.getPos(), node) == null;
    }

    public void updateBeacon (BeaconComponent beaconComponent) {
        BeaconNode node = new BeaconNode(beaconComponent);
        if (beacons.containsKey(node.getPos()))
            beacons.put(node.getPos(), node);
    }

    public Optional<BeaconNode> getNode(BlockPos pos) {
        return Optional.ofNullable(beacons.get(pos));
    }
    public Set<BeaconNode> getAllNodes() {
        return new HashSet<>(beacons.values());
    }

    public EmbeddedStorage getStorage(){
        if (this.storage == null){
            this.storage = new EmbeddedStorage();
        }
        return this.storage;
    }

//    public void readFromNbt(NbtCompound tag) {
//
//    }

    // TODO: store key as constant
    // TODO: generalize to an interface
    public static FrequencyNetwork fromNbt(UUID id, NbtCompound tag) {
        IFrequencyIdentifier freqID = PyramidFrequencyIdentifier.fromNbt(tag.getCompound("frequency"));
        if (freqID == null) {
            LOGGER.error("Failed to retrieve Frequency! Frequency Compound: {}", tag.getCompound("frequency"));
            return null;
        }
        var network = new FrequencyNetwork(id, freqID);
        network.getStorage().readNbtList(tag.getList("storage", NbtCompound.COMPOUND_TYPE));
        NbtList allBeaconsList = tag.getList("beaconNodes", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < allBeaconsList.size(); i++) {
            NbtCompound nbtCompound = allBeaconsList.getCompound(i);
            BeaconNode beaconNode = BeaconNode.fromNBT(nbtCompound);
            network.beacons.put(beaconNode.getPos(), beaconNode);
        }
        return network;
    }

    public void writeToNbt(NbtCompound tag) {
        NbtCompound pfIDTag = new NbtCompound();
        // TODO improve this
        if (freqID instanceof PyramidFrequencyIdentifier pfID) {
            pfID.toNbt(pfIDTag);
            tag.put("frequency", pfIDTag);
        } else
            tag.putInt("frequency", freqID.hashCode());
        tag.put("storage", this.getStorage().toNbtList());
        NbtList allBeaconsList = new NbtList();
        beacons.values().forEach(beaconNode -> allBeaconsList.add(beaconNode.toNBT()));
        tag.put("beaconNodes", allBeaconsList);
    }

}
