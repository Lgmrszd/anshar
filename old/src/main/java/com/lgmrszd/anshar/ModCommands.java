package com.lgmrszd.anshar;

import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.storage.EmbeddedStorage;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.*;

public class ModCommands {

    private static final SuggestionProvider<ServerCommandSource> NETWORK_UUID_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(() -> {
                ServerWorld world = context.getSource().getWorld();
                return NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNetworks().stream().map(frequencyNetwork -> frequencyNetwork.getId().toString()).sorted().iterator();
            }, builder);

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(literal("anshar")
                        .then(literal("network")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(argument("Network ID", UuidArgumentType.uuid())
                                        .suggests(NETWORK_UUID_SUGGESTIONS)
                                        .executes(ModCommands::networkIDCommand)
                                        .then(literal("storage")
                                                .executes(ModCommands::networkIDStorageCommand)
                                        )
                                )
                                .then(literal("list")
                                        .executes(ModCommands::networkListCommand)
                                )
                        )
                        .then(literal("debug")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(literal("nearestBeacon")
                                        .then(argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(ModCommands::debugNearestBeaconCommand)
                                                .then(literal("advanced")
                                                        .executes(ModCommands::debugNearestBeaconAdvancedCommand)
                                                )
                                        )
                                )
                                .then(literal("config")
                                        .executes(ModCommands::debugServerConfigCommand)
                                )
                        )
                        .then(literal("player")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(argument("player", EntityArgumentType.player())
                                        .then(literal("kick")
                                                .executes(ModCommands::playerKickCommand)
                                        )
                                        .then(literal("enter")
                                                .then(argument("Network ID", UuidArgumentType.uuid())
                                                        .suggests(NETWORK_UUID_SUGGESTIONS)
                                                        .executes(ModCommands::playerEnterCommand)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int networkIDCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        final UUID networkID = UuidArgumentType.getUuid(context, "Network ID");
        return NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNetwork(networkID)
                .map(frequencyNetwork -> {
                    sendFeedback(
                            context,
                            "Frequency Network with ID\n%s\nhas this many Item Stacks: %d"
                                    .formatted(
                                            networkID.toString(),
                                            frequencyNetwork
                                                    .getStorage()
                                                    .getHeldStacks()
                                                    .stream()
                                                    .filter(itemStack -> !itemStack.isEmpty())
                                                    .count()
                                    )
                    );
                    sendFeedback(context, "And has following Beacons:");
                    frequencyNetwork.getBeacons().forEach(pos -> {
                        String beacon_data = String.format("%s", pos.toString());
                        sendFeedback(context, beacon_data);
                    });
                    return 0;
                })
                .orElseGet(() -> {
                    sendFeedback(context, "Invalid Frequency Network with ID " + networkID.toString());
                    return -1;
                });
    }

    private static int networkIDStorageCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return -1;
        final UUID networkID = UuidArgumentType.getUuid(context, "Network ID");
        return NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNetwork(networkID)
                .map(frequencyNetwork -> {
                    EmbeddedStorage inventory = frequencyNetwork.getStorage();
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                            (syncId, playerInv, playerx) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInv, inventory),
                            Text.literal("[")
                                    .append(Text.translatable("block.minecraft.beacon"))
                                    .append("] ")
                                    .append(Text.translatable("container.enderchest"))));
                    return 0;
                })
                .orElseGet(() -> {
                    sendFeedback(context, "Invalid Frequency Network with ID " + networkID.toString());
                    return -1;
                });
    }

    private static int networkListCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        sendFeedback(context, "Frequency Networks:");
        NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNetworks().forEach(frequencyNetwork -> {
            String network_data = String.format(
                    "UUID: %s, Beacons: %d, Item Stacks stored: %d",
                    frequencyNetwork.getId(),
                    frequencyNetwork.getBeacons().size(),
                    frequencyNetwork
                            .getStorage()
                            .getHeldStacks()
                            .stream()
                            .filter(itemStack -> !itemStack.isEmpty())
                            .count()
            );
            sendFeedback(context, network_data);
        });
        return 0;
    }

    private static int debugNearestBeaconCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
        NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNearestConnectedBeacon(world, pos)
                .ifPresentOrElse(beaconBlockEntity ->
                                sendFeedback(context,
                                        "Nearest active Beacon to [%s] is [%s] (distance: %.1f)"
                                                .formatted(
                                                        pos.toShortString(),
                                                        beaconBlockEntity.getPos().toShortString(),
                                                        Math.sqrt(pos.getSquaredDistance(beaconBlockEntity.getPos()))
                                                )
                                )
                        , () ->
                                sendFeedback(context, "No nearest active Beacon to [%s]".formatted(pos.toShortString()))
                );
        return 0;
    }

    private static int debugNearestBeaconAdvancedCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
        Identifier dim = world.getRegistryKey().getValue();
        sendFeedback(context, "Looking for nearest beacon at [%s] in dimension [%s]".formatted(pos.toShortString(), dim.toString()));
        Collection<FrequencyNetwork> networks = NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNetworks();
        sendFeedback(context, "Total networks: %d".formatted(networks.size()));
        List<FrequencyNetwork> networksThisDim = networks.stream().filter(frequencyNetwork -> frequencyNetwork.getFreqID().isValidInDim(dim)).toList();
        sendFeedback(context, "Total networks in this dimension: %d".formatted(networksThisDim.size()));
        List<BlockPos> allBeacons = networksThisDim.stream().map(FrequencyNetwork::getBeacons).flatMap(Collection::stream).toList();
        sendFeedback(context, "Total beacons in this dimension: %d".formatted(allBeacons.size()));
        List<BlockPos> allLoadedBeacons = allBeacons.stream().filter(blockPos -> world.isChunkLoaded(
                ChunkSectionPos.getSectionCoord(blockPos.getX()),
                ChunkSectionPos.getSectionCoord(blockPos.getY()))
        ).toList();
        sendFeedback(context, "Total loaded beacons in this dimension: %d".formatted(allLoadedBeacons.size()));
        List<BeaconBlockEntity> allLoadedBBESorted = allLoadedBeacons.stream()
                .sorted((pos1, pos2) -> {
                    double distance1 = pos.getSquaredDistance(pos1);
                    double distance2 = pos.getSquaredDistance(pos2);
                    return Double.compare(distance1, distance2);
                })
                .map(pos1 -> world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null)
                .filter(Objects::nonNull)
                .toList();
        sendFeedback(context, "Total loaded beacon block entities in this dimension: %d".formatted(allLoadedBBESorted.size()));
        if (!allLoadedBBESorted.isEmpty())
            sendFeedback(context, "Nearest beacon block entity in this dimension: %s".formatted(allLoadedBBESorted.get(0).getPos().toShortString()));
        return 0;
    }

    private static int debugServerConfigCommand(CommandContext<ServerCommandSource> context) {
        sendFeedback(context, "Server config values:");
        sendFeedback(context, "Beam Check Period: %d".formatted(ServerConfig.beamCheckPeriod.get()));
        sendFeedback(context, "Beam Client Check: %b".formatted(ServerConfig.beamClientCheck.get()));
        sendFeedback(context, "End Crystal Max Distance: %d".formatted(ServerConfig.endCrystalMaxDistance.get()));
        sendFeedback(context, "End Crystals Per Beacon: %d".formatted(ServerConfig.endCrystalsPerBeacon.get()));
        return 0;
    }

    private static int playerKickCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        PlayerTransportComponent ptc = PlayerTransportComponent.KEY.get(player);
        if (!ptc.isInNetwork()) {
            sendFeedback(
                    context,
                    Text.literal("Player ")
                            .append(player.getDisplayName())
                            .append(Text.literal(" is not Embedded!")),
                    false
            );
            return -1;
        }
        ptc.exitNetwork();
        sendFeedback(
                context,
                Text.literal("Kicked player ")
                        .append(player.getDisplayName())
                        .append(Text.literal(" from the Embedded state!")),
                true
        );
        return 1;
    }

    private static int playerEnterCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        World world = player.getWorld();
        PlayerTransportComponent ptc = PlayerTransportComponent.KEY.get(player);
        if (ptc.isInNetwork()) {
            sendFeedback(
                    context,
                    Text.literal("Player ")
                            .append(player.getDisplayName())
                            .append(Text.literal(" is already Embedded!")),
                    false
            );
            return -1;
        }
        final UUID networkID = UuidArgumentType.getUuid(context, "Network ID");
        return NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNetwork(networkID)
                .map(frequencyNetwork -> {
                    ptc.enterNetwork(frequencyNetwork, player.getBlockPos());
                    sendFeedback(
                            context,
                            Text.literal("Sent player ")
                                    .append(player.getDisplayName())
                                    .append(Text.literal(" to Frequency Network with ID " + networkID.toString())),
                            true
                    );
                    return 1;
                }).orElseGet(() -> {
                    sendFeedback(context, "Invalid Frequency Network with ID " + networkID.toString());
                    return -1;
                });
    }

    private static void sendFeedback(CommandContext<ServerCommandSource> context, String text) {
        sendFeedback(context, Text.literal(text), false);
    }

    private static void sendFeedback(CommandContext<ServerCommandSource> context, String text, boolean broadcastToOps) {
        sendFeedback(context, Text.literal(text), broadcastToOps);
    }

    private static void sendFeedback(CommandContext<ServerCommandSource> context, Text text, boolean broadcastToOps) {
        context.getSource().sendFeedback(
                () -> text,
                broadcastToOps
        );
    }
}
