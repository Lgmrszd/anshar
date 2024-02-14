package com.lgmrszd.anshar.frequency;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.*;
import java.util.function.Consumer;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public class NetworkManagerComponent implements Component {
    private final HashMap<UUID, FrequencyNetwork> networksByUUID;
    public static final ComponentKey<NetworkManagerComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "network_manager"), NetworkManagerComponent.class
    );

    public NetworkManagerComponent() {
        networksByUUID = new HashMap<>();
    }

    public Collection<FrequencyNetwork> getNetworks() {
        return networksByUUID.values();
    }

    private FrequencyNetwork getOrCreateNetwork(IFrequencyIdentifier query){
        List<FrequencyNetwork> matchedNetworks = networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().equals(query))
                .toList();
        if (matchedNetworks.isEmpty()) {
            UUID randuuid = UUID.randomUUID();
            while (networksByUUID.containsKey(randuuid)) randuuid = UUID.randomUUID();
            FrequencyNetwork frequencyNetwork = new FrequencyNetwork(randuuid, query);
            networksByUUID.put(randuuid, frequencyNetwork);
            return frequencyNetwork;
        }
        if (matchedNetworks.size() > 1) {
            LOGGER.error("More than one networks detected with given frequency identifier!");
            matchedNetworks.forEach(frequencyNetwork ->
                    LOGGER.error(String.format(
                            "UUID: %s, Number of beacons: %d",
                            frequencyNetwork.getId(),
                            frequencyNetwork.getBeacons().size()
                    )));
        }
        return matchedNetworks.get(0);
    }

    public Optional<FrequencyNetwork> getNetwork(UUID uuid){
        return Optional.ofNullable(networksByUUID.get(uuid));
    }

    public Optional<BeaconBlockEntity> getNearestConnectedBeacon(World world, BlockPos pos) {
        if (world == null) return Optional.empty();
        Identifier dim = world.getRegistryKey().getValue();
        return networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().isValidInDim(dim))
                .map(FrequencyNetwork::getBeacons)
                .flatMap(Collection::stream)
                .filter(blockPos -> world.isChunkLoaded(
                        ChunkSectionPos.getSectionCoord(blockPos.getX()),
                        ChunkSectionPos.getSectionCoord(blockPos.getZ()))
                )
                .sorted((pos1, pos2) -> {
                    double distance1 = pos.getSquaredDistance(pos1);
                    double distance2 = pos.getSquaredDistance(pos2);
                    return Double.compare(distance1, distance2);
                })
                .map(pos1 -> world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public List<BeaconBlockEntity> getConnectedBeaconsInRadius(World world, BlockPos pos, Double radius) {
        if (world == null) return Collections.emptyList();
        Identifier dim = world.getRegistryKey().getValue();
        return networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().isValidInDim(dim))
                .map(FrequencyNetwork::getBeacons)
                .flatMap(Collection::stream)
                .filter(blockPos -> pos.isWithinDistance(blockPos, radius)
                        && world.isChunkLoaded(
                        ChunkSectionPos.getSectionCoord(blockPos.getX()),
                        ChunkSectionPos.getSectionCoord(blockPos.getZ()))
                )
                .map(pos1 -> world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null)
                .filter(Objects::nonNull)
                .toList();
    }

    public void updateBeaconNetwork(BeaconComponent beaconComponent, IFrequencyIdentifier frequencyIdentifier, Consumer<FrequencyNetwork> consumer) {
        // Check if it's actually the same frequency as before
        if (beaconComponent.getFrequencyNetwork()
                // continue if there's network with different frequency
                .map(frequencyNetwork -> frequencyNetwork.getFreqID().equals(frequencyIdentifier))
                // continue if no network on the beacon component
                .orElse(false)
        ) return;
        BlockPos beaconPos = beaconComponent.getBeaconPos();
        // Remove beacon from the old network if present
        // TODO: Just to make it safe for now
//        beaconComponent.getFrequencyNetwork().ifPresent(frequencyNetwork -> {
//            if (!frequencyNetwork.removeBeacon(beaconPos)) {
//                LOGGER.warn(String.format(
//                        "ANSHAR WARNING: Tried to remove beacon %s from network %s but it's already not there",
//                        beaconPos,
//                        frequencyNetwork
//                ));
//            }
//        });
        getNetworks().forEach(frequencyNetwork -> {
            frequencyNetwork.removeBeacon(beaconPos);
        });
        // Add beacon to the new network
        if (frequencyIdentifier.isValid()) {
            FrequencyNetwork network = getOrCreateNetwork(frequencyIdentifier);
            if (!network.addBeacon(beaconComponent)) {
                LOGGER.warn(String.format(
                        "ANSHAR WARNING: Tried to add beacon %s to network %s but it's already there!",
                        beaconPos,
                        network
                ));
            }
            consumer.accept(network);
        } else consumer.accept(null);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        try {
            NbtCompound networksTag = tag.getCompound("networks");
            for (String uuid_string : networksTag.getKeys()) {
                UUID uuid = UUID.fromString(uuid_string);
                NbtCompound networkTag = networksTag.getCompound(uuid_string);
                FrequencyNetwork network = FrequencyNetwork.fromNbt(uuid, networkTag);
                if (network == null) {
                    LOGGER.error("Failed to load Network! Network Compound: {}", networkTag);
                    continue;
                }
                networksByUUID.put(uuid, network);
            }
        } catch (CrashException e) {
            LOGGER.error("Error while reading Network data from world save!");
            LOGGER.error("crash report as follows:");
            LOGGER.error("\n"+e.getReport().asString());
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound networksTag = new NbtCompound();
        networksByUUID.forEach((uuid, network) -> {
            NbtCompound networkTag = new NbtCompound();
            network.writeToNbt(networkTag);
            networksTag.put(uuid.toString(), networkTag);
        });
        tag.put("networks", networksTag);
    }
}
