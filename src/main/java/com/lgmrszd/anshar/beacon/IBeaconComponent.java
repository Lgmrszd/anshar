package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.IFrequencyIdentifier;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface IBeaconComponent extends ServerTickingComponent {
    public static final ComponentKey<IBeaconComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "beacon"), IBeaconComponent.class
    );

    void rescanPyramid();
    IFrequencyIdentifier getFrequencyID();

    Optional<FrequencyNetwork> getFrequencyNetwork();
    public void onPlayerSteppedOn(PlayerEntity player);
}