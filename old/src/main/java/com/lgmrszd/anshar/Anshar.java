package com.lgmrszd.anshar;

import com.lgmrszd.anshar.advancements.EnteredNetworkCriterion;
import com.lgmrszd.anshar.advancements.NetworkJumpCriterion;
import net.fabricmc.api.ModInitializer;

import net.minecraft.advancement.criterion.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anshar implements ModInitializer {
	
	public static final String MOD_ID = "anshar";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static EnteredNetworkCriterion ENTERED_NETWORK = Criteria.register(MOD_ID + "/entered_network", new EnteredNetworkCriterion());
	public static NetworkJumpCriterion NETWORK_JUMP = Criteria.register(MOD_ID + "/network_jump", new NetworkJumpCriterion());

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModRegistration.registerAll();
	}
}