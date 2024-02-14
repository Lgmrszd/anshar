package com.lgmrszd.anshar.transport;

import static com.lgmrszd.anshar.Anshar.LOGGER;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.DefaultParticleType;

public class TransportEffects {
    @Nullable
    public static final NbtCompound makeTransportFirework(int color) {
        NbtCompound transportExplosion = null;
        try {
            transportExplosion = StringNbtReader.parse("{Explosions:[{Type:1,Colors:[I;" + color + "]}]}");
        } catch (CommandSyntaxException lol) {
            LOGGER.error("Failed to create transport explosion effect for hex " + color);
        }
        return transportExplosion;
    }

    // TODO setup to cancel particles
    public static final DefaultParticleType GATE_STAR = FabricParticleTypes.simple(false);
}
