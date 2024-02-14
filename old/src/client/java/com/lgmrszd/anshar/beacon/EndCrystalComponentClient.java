package com.lgmrszd.anshar.beacon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class EndCrystalComponentClient {
    static {
        EndCrystalComponent.clientTick = EndCrystalComponentClient::clientTick;
    }

    private static void clientTick(EndCrystalComponent component) {
        if (component.beaconPos == null) return;
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;
        if (world.getTime() % 5L == 0L) {
            Vec3d particlePos = component.endCrystal.getPos().add(component.vec).add(0, 0.7, 0);
            component.vec = component.vec.rotateY(36f * (float) (Math.PI / 180));
            world.addParticle(
                    ParticleTypes.GLOW,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    0, 0, 0
            );
        }
    }

    public static void init() {
    }
}
