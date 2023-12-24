package com.lgmrszd.anshar;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.minecraft.block.entity.BeaconBlockEntity;

import com.lgmrszd.anshar.frequency.FrequencyIdentifierComponent;
import com.lgmrszd.anshar.frequency.IFrequencyIdentifierComponent;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;

public final class ModComponents implements BlockComponentInitializer, LevelComponentInitializer {

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(BeaconBlockEntity.class, IFrequencyIdentifierComponent.KEY, b -> new FrequencyIdentifierComponent());
        registry.registerFor(BeaconBlockEntity.class, IBeaconComponent.KEY, BeaconComponent::new);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(NetworkManagerComponent.KEY, p -> new NetworkManagerComponent());
    }
}