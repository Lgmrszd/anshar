package com.lgmrszd.anshar;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModResources {
    public static final Identifier EMBED_SPACE_AMBIENT_SOUND = new Identifier(Anshar.MOD_ID, "slunky_sound");
	public static final Identifier TRANSPORT_JUMP_SOUND = new Identifier(Anshar.MOD_ID, "transport_jump");
	public static final Identifier EMBED_SPACE_MUSIC = new Identifier(Anshar.MOD_ID, "eternal_tunes");
	public static SoundEvent EMBED_SPACE_AMBIENT_SOUND_EVENT = SoundEvent.of(EMBED_SPACE_AMBIENT_SOUND);
	public static SoundEvent TRANSPORT_JUMP_SOUND_EVENT = SoundEvent.of(TRANSPORT_JUMP_SOUND);
	public static SoundEvent EMBED_SPACE_MUSIC_EVENT = SoundEvent.of(EMBED_SPACE_MUSIC);
}
