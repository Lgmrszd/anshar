package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.config.ServerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class BeaconComponentClient {
    static {
        BeaconComponent.clientTick = BeaconComponentClient::clientTick;
    }

    private static int cooldownTicks = 0;

    private static void clientTick (BeaconComponent beaconComponent) {
        if (!beaconComponent.isActive()) return;
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;
        if (world.getTime() % 5L == 0L) {
            Vec3i pos = beaconComponent.getBeaconPos();
            Vec3d particlePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(beaconComponent.particleVec);
            beaconComponent.particleVec = beaconComponent.particleVec.rotateY(36f * (float) (Math.PI / 180));
            world.addParticle(
                    ParticleTypes.GLOW,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    0, 0, 0
            );
        }
        if (ServerConfig.beamClientCheck.get()) beamCheck(beaconComponent);
    }


    private static void beamCheck (BeaconComponent beaconComponent) {
        if (cooldownTicks > 0) return;
        Box box = beaconComponent.beamBoundingBox();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.getBoundingBox().intersects(box)) return;
        if (!beaconComponent.isValid()) return;
        LOGGER.debug("Intersecting with beacon beam at {}", beaconComponent.getBeaconPos());
        sendEnterNetworkPacketC2S(beaconComponent.getBeaconPos());
        cooldownTicks = 20;
    }

    public static void clientGlobalTick () {
        if (cooldownTicks > 0) cooldownTicks--;
    }

    private static void sendEnterNetworkPacketC2S(BlockPos pos) {
        var enterPacket = PacketByteBufs.create();
        enterPacket.writeBlockPos(pos);
        ClientPlayNetworking.send(BeaconComponent.ENTER_PACKET_ID, enterPacket);
    }

//    private static void sendEnterNetworkPacketC2S(BlockPos pos, UUID freqUUID) {
//        var enterPacket = PacketByteBufs.create();
//        enterPacket.writeBlockPos(pos);
//        enterPacket.writeUuid(freqUUID);
//        ClientPlayNetworking.send(BeaconComponent.ENTER_PACKET_ID, enterPacket);
//    }

    public static void init() {
    }
}
