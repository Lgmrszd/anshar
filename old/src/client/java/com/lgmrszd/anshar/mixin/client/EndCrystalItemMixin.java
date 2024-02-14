package com.lgmrszd.anshar.mixin.client;

import com.lgmrszd.anshar.ModApi;
import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import com.lgmrszd.anshar.config.ServerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(EndCrystalItem.class)
public abstract class EndCrystalItemMixin extends ItemMixin {

    @Override
    public void anshar$addToTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(stack, null);
        if (container == null || MinecraftClient.getInstance().player == null) return;
        int maxDistance = ServerConfig.endCrystalMaxDistance.get();
        container.getBeaconPos().ifPresent(pos -> {
            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            double distance = playerPos.distanceTo(pos.toCenterPos());
            tooltip.add(Text.translatable(
                    "anshar.tooltip.end_crystal.linked",
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            ).withColor(Colors.LIGHT_GRAY));
            tooltip.add(Text.translatable(
                    "anshar.tooltip.end_crystal.linked.distance",
                    Text.literal(String.format("%.1f / %d", distance, maxDistance))
                            .withColor(distance > maxDistance ? Colors.LIGHT_RED : Colors.LIGHT_GRAY)
            ).withColor(Colors.GRAY));
        });
    }
}
