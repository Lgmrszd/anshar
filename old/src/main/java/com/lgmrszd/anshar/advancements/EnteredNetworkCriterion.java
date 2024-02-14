package com.lgmrszd.anshar.advancements;

import com.lgmrszd.anshar.Anshar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class EnteredNetworkCriterion extends AbstractCriterion<EnteredNetworkCriterion.Conditions> {

    @Override
    public Codec<EnteredNetworkCriterion.Conditions> getConditionsCodec() {
        return EnteredNetworkCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<EnteredNetworkCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                conditionsInstance -> conditionsInstance.group(
                        Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player)
                ).apply(conditionsInstance, Conditions::new));

        public static AdvancementCriterion<EnteredNetworkCriterion.Conditions> create() {
            return Anshar.ENTERED_NETWORK.create(new EnteredNetworkCriterion.Conditions(Optional.empty()));
        }
    }
}
