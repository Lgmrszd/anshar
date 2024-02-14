package com.lgmrszd.anshar;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.minecraft.block.entity.BeaconBlockEntity;

import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.EndCrystalComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;
import com.lgmrszd.anshar.beacon.IEndCrystalComponent;

import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class ModComponents implements BlockComponentInitializer, LevelComponentInitializer, EntityComponentInitializer {

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(BeaconBlockEntity.class, IBeaconComponent.KEY, BeaconComponent::new);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(NetworkManagerComponent.KEY, p -> new NetworkManagerComponent());
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(EndCrystalEntity.class, IEndCrystalComponent.KEY, EndCrystalComponent::new);
        registry.registerFor(PlayerEntity.class, PlayerTransportComponent.KEY, PlayerTransportComponent::new);
    }
}
