package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class ModApi {
    public static final ItemApiLookup<EndCrystalItemContainer, Void> END_CRYSTAL_ITEM = ItemApiLookup.get(
            new Identifier(MOD_ID, "end_crystal_item"),
            EndCrystalItemContainer.class,
            Void.class
    );

    public static void register() {
        END_CRYSTAL_ITEM.registerForItems((itemStack, context) -> {
            return new EndCrystalItemContainer(itemStack);
        }, Items.END_CRYSTAL);
    }
}
