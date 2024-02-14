package com.lgmrszd.anshar.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static ModConfigSpec.BooleanValue beamClientCheck;
    public static ModConfigSpec.IntValue beamCheckPeriod;
    public static ModConfigSpec.IntValue endCrystalMaxDistance;
    public static ModConfigSpec.IntValue endCrystalsPerBeacon;

    static {
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        setupConfig(configBuilder);
        CONFIG_SPEC = configBuilder.build();
    }

    private static void setupConfig(ModConfigSpec.Builder builder) {
        beamClientCheck = builder
                .comment("Wherever to delegate checking for the beam intersection to the client")
                .comment("Final check is performed on the server to prevent malicious clients")
                .comment("This probably should always stay as false")
                .define("beam_client_check", false);
        beamCheckPeriod = builder
                .comment("How often (in ticks) to scan for players intersecting the beacon")
                .defineInRange("beam_check_period", 5, 1, 5);
        builder.comment("Category for End Crystal related things");
        builder.push("End Crystal options");
        endCrystalMaxDistance = builder
                .comment("Max distance at which End Crystals are allowed to connect to the Beacons")
                .comment("Existing crystals won't disconnect if the value is decreased!")
                .defineInRange("end_crystal_max_distance", 16, 4, 64);
        endCrystalsPerBeacon = builder
                .comment("How many End Crystals can be connected to the same Beacon at once?")
                .comment("Existing crystals won't disconnect if the value is decreased!")
                .defineInRange("end_crystals_per_beacon", 4, 0, 64);
        builder.pop();
    }
}
