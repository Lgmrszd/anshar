package com.lgmrszd.anshar.transport;

import com.lgmrszd.anshar.ModResources;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

class PlayerTransportAudioClient {
    // state machine for managing a transport client's audio just to keep things tidy
    // if there's a better way to do this let me know

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Random random = SoundInstance.createRandom();
    private final PlayerTransportClient transport;
    
    private EmbeddedAmbiance ambientSound = null;
    private EmbeddedAudio embedMusic = null;
    private SoundInstance jumpSound = null;

    public PlayerTransportAudioClient(PlayerTransportClient transportClient) {
        this.transport = transportClient;
        reset();
    }

    public void reset() {
        client.getSoundManager().stopAll();
        playAmbient();
    }
    
    public void stopAll() {
        stopJump();
    }

    // TODO turn this shit into 2 methods and a Map
    public void playJump() {
        if (jumpSound != null) return;
        jumpSound = new JumpSound();
        client.getSoundManager().play(jumpSound);
    }

    public void stopJump() {
        if (jumpSound == null) return;
        client.getSoundManager().stop(jumpSound);
        jumpSound = null;
    }

    private void playAmbient() {
        if (ambientSound != null) client.getSoundManager().stop(ambientSound);
        ambientSound = new EmbeddedAmbiance();
        client.getSoundManager().play(ambientSound);
    }

    private void playMusic() {
        if (embedMusic != null) client.getSoundManager().stop(embedMusic);
        embedMusic = new EmbeddedMusic();
        client.getSoundManager().play(embedMusic);
    }

    private void stopMusic() {
        if (embedMusic == null) return;
        client.getSoundManager().stop(embedMusic);
        embedMusic = null;
    }

    private static final float TICKS_TO_PLAY_TUNES = 120 * 20;
    private static final float TICKS_TO_TRANSITION = 10 * 20;
    private float ticksToPlay = TICKS_TO_PLAY_TUNES;
    public void tick() {
        // hhhhhhhhh
        if (!client.getSoundManager().isPlaying(embedMusic)) {
            --ticksToPlay;
            if (client.getSoundManager().isPlaying(ambientSound)) {
                if (ticksToPlay < 0 && ticksToPlay > -TICKS_TO_TRANSITION) {
                    var percentage = Math.max(0, Math.min(1, 1.0f - ticksToPlay/-TICKS_TO_TRANSITION));
                    ambientSound.setTransitionVolume(percentage);
                } else if (ticksToPlay < -TICKS_TO_TRANSITION) {
                    playMusic();
                    ticksToPlay = TICKS_TO_PLAY_TUNES;
                }
            } else {
                stopMusic();
                playAmbient();
            }
        } else if (transport.getJumpPercentage() > .9) {
            stopMusic();
        }
    }

    private abstract class EmbeddedAudio extends MovingSoundInstance implements IEmbeddedAudio {
        protected final ClientPlayerEntity player;

        protected EmbeddedAudio(SoundEvent event, SoundCategory category) {
            super(event, category, PlayerTransportAudioClient.this.random);
            this.player = client.player;
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || !PlayerTransportComponent.KEY.get(player).isInNetwork() || this.volume == 0) {
                this.setDone();
            }
        }

        @Override
        public float getVolume() {
            return super.getVolume() * (1f - transport.getJumpPercentage());
        }
    }

    private class EmbeddedAmbiance extends EmbeddedAudio {
        private static final float BASE_VOLUME = 0.2f;
        private static final float FADE_IN_TICKS = 20;
        private float fade = 1;
        
        protected EmbeddedAmbiance() {
            super(ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT, SoundCategory.AMBIENT);
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = BASE_VOLUME;
            this.relative = true;
        }

        @Override public void tick() {
            super.tick();
            if (fade < FADE_IN_TICKS) fade += 1;
        }

        public void setTransitionVolume(float v) { this.volume = BASE_VOLUME * v; }

        @Override public float getVolume() { return super.getVolume() * fade / FADE_IN_TICKS; }
    }

    private class EmbeddedMusic extends EmbeddedAudio {        
        protected EmbeddedMusic() {
            super(ModResources.EMBED_SPACE_MUSIC_EVENT, SoundCategory.MUSIC);
            this.repeat = false;
            this.volume = 0.5f;
            this.relative = true;
        }
    }

    private class JumpSound extends AbstractSoundInstance implements IEmbeddedAudio {
        protected JumpSound() {
            super(ModResources.TRANSPORT_JUMP_SOUND_EVENT, SoundCategory.NEUTRAL, PlayerTransportAudioClient.this.random);
            this.volume = 0.2f;
            this.pitch = 1.0f;
            this.repeat = false;
            this.relative = false;
        }
    }
}
