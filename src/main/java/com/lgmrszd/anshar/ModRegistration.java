package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;

public class ModRegistration {
    public static void registerAll() {
        registerEvents();
    }

    private static void registerEvents() {
        BeaconEvents.register();
    }
}