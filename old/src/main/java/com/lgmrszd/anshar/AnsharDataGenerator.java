package com.lgmrszd.anshar;

import com.lgmrszd.anshar.advancements.EnteredNetworkCriterion;
import com.lgmrszd.anshar.advancements.NetworkJumpCriterion;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.ConstructBeaconCriterion;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class AnsharDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(AdvancementsProvider::new);
	}

	static class AdvancementsProvider extends FabricAdvancementProvider {
		protected AdvancementsProvider(FabricDataOutput dataGenerator) {
			super(dataGenerator);
		}

		@Override
		public void generateAdvancement(Consumer<AdvancementEntry> consumer) {
			AdvancementEntry rootAdvancement = Advancement.Builder.create()
					.display(
							Items.BEACON,
							Text.translatable("advancements.anshar.root.title"),
							Text.translatable("advancements.anshar.root.description"),
							new Identifier("textures/block/obsidian.png"),
							AdvancementFrame.TASK,
							false,
							false,
							false
					)
					.criterion("beacon", ConstructBeaconCriterion.Conditions.level(NumberRange.IntRange.atLeast(1)))
					.build(consumer, MOD_ID + "/root");

			AdvancementEntry networkEnter = Advancement.Builder.create().parent(rootAdvancement)
					.display(
							Items.BEACON,
							Text.translatable("advancements.anshar.network_enter.title"),
							Text.translatable("advancements.anshar.network_enter.description"),
							null,
							AdvancementFrame.GOAL,
							true,
							true,
							true
					)
					.criterion("entered_network", EnteredNetworkCriterion.Conditions.create())
					.build(consumer, MOD_ID + "/network_enter");

			AdvancementEntry networkJump = Advancement.Builder.create().parent(networkEnter)
					.display(
							Items.NETHER_STAR, // The display icon
							Text.translatable("advancements.anshar.network_jump.title"),
							Text.translatable("advancements.anshar.network_jump.description"),
							null,
							AdvancementFrame.GOAL,
							true,
							true,
							true
					)
					.criterion("network_jump", NetworkJumpCriterion.Conditions.create())
					.build(consumer, MOD_ID + "/network_jump");
		}
	}
}
