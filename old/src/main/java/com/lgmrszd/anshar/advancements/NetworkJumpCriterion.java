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

public class NetworkJumpCriterion extends AbstractCriterion<NetworkJumpCriterion.Conditions> {

    @Override
    public Codec<NetworkJumpCriterion.Conditions> getConditionsCodec() {
        return NetworkJumpCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<NetworkJumpCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                conditionsInstance -> conditionsInstance.group(
                        Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player)
                ).apply(conditionsInstance, Conditions::new));

        public static AdvancementCriterion<NetworkJumpCriterion.Conditions> create() {
            return Anshar.NETWORK_JUMP.create(new NetworkJumpCriterion.Conditions(Optional.empty()));
        }
    }
}
