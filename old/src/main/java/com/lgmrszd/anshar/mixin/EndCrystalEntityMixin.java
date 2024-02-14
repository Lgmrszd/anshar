package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.beacon.EndCrystalComponent;
import com.lgmrszd.anshar.beacon.IEndCrystalComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin extends EntityMixin {
    @Override
    public void anshar$onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        IEndCrystalComponent endCrystalComponent = EndCrystalComponent.KEY.get(this);
        ActionResult result = endCrystalComponent.onUse(player, hand);
        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
    public void anshar$onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient()) return;
        IEndCrystalComponent endCrystalComponent = EndCrystalComponent.KEY.get(this);
        if (!endCrystalComponent.onCrystalDamage(source)) cir.setReturnValue(false);
    }
}
