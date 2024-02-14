package com.lgmrszd.anshar.transport;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.lgmrszd.anshar.beacon.BeaconNode;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.random.Random;

public class PlayerTransportClient {
    // created when player enters a network to manage their presence there, removed when they exit

    private static final int TICKS_TO_JUMP = (int)(20f * 11.5);
    private static Random random = Random.create();

    private static PlayerTransportClient INSTANCE = null;

    public static void enterNetworkCallback(){ INSTANCE = new PlayerTransportClient(); }
    public static void tickCallback(){ if(INSTANCE != null) INSTANCE.tick(); }
    public static void exitNetworkCallback(){ INSTANCE.onExit(); INSTANCE = null; }

    public static PlayerTransportClient getInstance() { return INSTANCE; }

    private final ParticleManager particleManager;
    private final PlayerTransportAudioClient audioManager = new PlayerTransportAudioClient(this);
    private final ClientPlayerEntity player;
    private final PlayerTransportComponent transport;
    
    private int gateTicks = 0;
    private BeaconNode nearest = null;
    private boolean done = false;
    private int jumpCooldown = 0;

    public PlayerTransportClient(){
        var client = MinecraftClient.getInstance();
        this.particleManager = client.particleManager;
        this.player = client.player;
        this.transport = PlayerTransportComponent.KEY.get(player);

        // first tick behavior
        client.options.setPerspective(Perspective.FIRST_PERSON);
        player.setPitch(0);
    }
    
    public void tick() {
        if (done) return;
        if (jumpCooldown > 0) {
            jumpCooldown--;
            return;
        }

        // update gate status
        // check sneak to ensure jump and exit don't happen at the same time
        if (player.input.pressingForward && !player.input.sneaking) {
            // wait for a nearest to be set
            if (nearest == null && gateTicks == 0 && player.getWorld().getTime() % 10 == 0) {
                nearest = transport.getNearestLookedAt(); // should rename to something like "jumpTarget"
                if (nearest != null) audioManager.playJump();
            }
            if (nearest != null) gateTicks++;
        } else {
            gateTicks = 0;
            nearest = null;
            audioManager.stopJump();
            spawnOrientationParticles();
        }

        // draw nearest gate only if jumping, otherwise draw all
        if (nearest == null) {
            for (var node : transport.getJumpCandidates()) {
                drawGate(player, node, false, gateTicks);
            }
        } else {
            drawGate(player, nearest, true, gateTicks);
        }

        // jump if we ball
        if (gateTicks >= TICKS_TO_JUMP) {
            var jumpPacket = PacketByteBufs.create();
            jumpPacket.writeNbt(nearest.toNBT());
            ClientPlayNetworking.send(PlayerTransportComponent.JUMP_PACKET_ID, jumpPacket);
            audioManager.stopJump();
            gateTicks = 0;
            nearest = null;
            jumpCooldown = 10;
            audioManager.reset();
        } else {
            audioManager.tick();
        }
    }

    private void onExit() {
        audioManager.stopAll();
        done = true;
    }
    
    private static final float GATE_PLAYER_DISTANCE = 20;
    private static final float BASE_GATE_HEIGHT = 10;
    private static final int BASE_PARTICLE_COUNT = 3;
    private static final float GATE_OPENING_COEF = 3f;
    private static final float PARTICLE_VEL_COEF = .1f;

    private void drawGate(ClientPlayerEntity player, BeaconNode node, boolean nearest, int ticks) {
        // TODO replace some of this with making gates particle emitters/shaders

        // get vector from player to node pos
        var transport = PlayerTransportComponent.KEY.get(player);
        Vector3f normalGirl = transport.compassNormToNode(node);
        
        float jumpRatio = (float)ticks / (float)TICKS_TO_JUMP;
        float intensity = !nearest ? 1f : 1f + jumpRatio;

        Matrix3f M = new Matrix3f().setLookAlong(normalGirl, new Vector3f(0, 1, 0)).invert();
        Vector3f normalExt = normalGirl.mul(GATE_PLAYER_DISTANCE, new Vector3f());
        float gateWidth = GATE_OPENING_COEF * (intensity-1);
        float gateHeight = BASE_GATE_HEIGHT * (float)Math.pow(intensity, 4);
        float starSpeed = PARTICLE_VEL_COEF * ((float)Math.pow(intensity, 4)-1);
        
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < BASE_PARTICLE_COUNT * (int)Math.pow(intensity, 6); i++) {
                float pY = random.nextFloat() * gateHeight - gateHeight / 2;
                // should definitely do translate with a matrix4f instead, skipping perspective div
                // huge performance increase
                Vector3f pPos = new Vector3f(gateWidth * side, pY, 0f).mul(M).add(normalExt).add(player.getEyePos().toVector3f());
                Vector3f pVel = new Vector3f(0, 0, starSpeed).mul(M);

                // why am I not using a shader at this point? pathetic! die scoundrel! villain!
                var particle = particleManager.addParticle(TransportEffects.GATE_STAR, pPos.x, pPos.y, pPos.z, pVel.x, pVel.y, pVel.z);
                float[] colors = node.getColor();
                if (nearest) {
                    particle.setColor(
                        colors[0] + jumpRatio * (random.nextFloat() - colors[0]), 
                        colors[1] + jumpRatio * (random.nextFloat() - colors[1]), 
                        colors[2] + jumpRatio * (random.nextFloat() - colors[2])
                    );
                } else {
                    particle.setColor(colors[0], colors[1], colors[2]);
                }
            }
        }
    }

    public float getJumpPercentage() { return (float)gateTicks / (float)TICKS_TO_JUMP; }

    public static void acceptExplosionPacketS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (client.player == null) return;
        var pos = buf.readBlockPos().toCenterPos();
        var color = buf.readInt();
        client.execute(() -> {
            // TODO This has opposite effect of not showing effect when landing, so I commented it out :/
            // we really should delay sending the packet by like two ticks
            // Alternatively if the problem doesn't happen when exiting the network, we can just remove this
            // (As I made it not create the effect when entering for the player who enters)
//            var playerPos = MinecraftClient.getInstance().player.getPos();
//            if (!playerPos.isInRange(pos, PlayerTransportComponent.EXPLOSION_MAX_DISTANCE)) return;
            handler.getWorld().addFireworkParticle(pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, TransportEffects.makeTransportFirework(color));
        });
    }

    private void spawnOrientationParticles() {
        // draws particles above and below players to help with orientation in embedded space
        float[] color = {1f, 1f, 1f};
        var tgt = transport.getTarget();
        if (tgt != null) color = tgt.getColor();
        var ppos = player.getPos();
        for (double dir = 1; dir >= -1; dir -= 2) {
            double x = ppos.getX() + (random.nextFloat()-0.5) * 4;
            double y = ppos.getY() + 1.7 + dir*5;
            double z = ppos.getZ() + (random.nextFloat()-0.5) * 4;
            var particle = particleManager.addParticle(TransportEffects.GATE_STAR, x, y, z, 0, dir/5, 0);
            particle.setColor(color[0], color[1], color[2]);
        }
    }
    
}
