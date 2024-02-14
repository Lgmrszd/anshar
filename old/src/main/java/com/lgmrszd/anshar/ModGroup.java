package com.lgmrszd.anshar;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class ModGroup {
    private static final ItemGroup ANSHAR_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.BEACON))
            .displayName(Text.translatable("itemGroup.anshar.anshar_group"))
            .entries((context, entries) -> {
                entries.add(new ItemStack(Items.BEACON));
                entries.add(new ItemStack(Items.ENDER_CHEST));
                entries.add(new ItemStack(Items.END_CRYSTAL));
            })
            .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "anshar_group"), ANSHAR_GROUP);
    }
}
