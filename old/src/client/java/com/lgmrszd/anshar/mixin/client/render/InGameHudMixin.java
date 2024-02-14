package com.lgmrszd.anshar.mixin.client.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.transport.PlayerTransportClient;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;


@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private ChatHud chatHud;

    @Shadow private int ticks;
    
    @Shadow private TextRenderer getTextRenderer() { return null; }
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void anshar$render(DrawContext context, float tickDelta, CallbackInfo ci) {
        PlayerTransportComponent transportComponent = PlayerTransportComponent.KEY.get(client.player);
        if (transportComponent.isInNetwork() && !client.options.hudHidden) {

            // calculate text fade
            var transportClient = PlayerTransportClient.getInstance();
            int alpha = transportClient == null ? 255 : Math.max(0, 255 - (int)(2 * 255 * PlayerTransportClient.getInstance().getJumpPercentage()));

            if (alpha > 3) {
                alpha <<= 24;

                // hud render setup
                // make into custom render layer? perchance?
                RenderSystem.enableBlend();
                TextRenderer textRenderer = getTextRenderer();
                int scaledWidth = context.getScaledWindowWidth();
                int scaledHeight = context.getScaledWindowHeight();
                context.getMatrices().push();
                context.getMatrices().translate(scaledWidth / 2, 0, 0.0f);

                // panic instructions
                int helpColor = 0x707070 + alpha;
                anshar$drawText(context, textRenderer, Text.translatable("anshar.help.transport.gate", Text.keybind("key.forward")), 10, helpColor);
                anshar$drawText(context, textRenderer, Text.translatable("anshar.help.transport.exit", Text.keybind("key.sneak")), 22, helpColor);

                // current node info
                BeaconNode target = transportComponent.getTarget();
                if (target != null) anshar$drawText(context, textRenderer, Text.translatable("anshar.help.transport.location", target.getName()), 34, helpColor);

                // dest node info
                var node = transportComponent.getNearestLookedAt();
                if (node != null) {
                    int rgb = (int)(node.getColor()[0] * 255);
                    rgb = (rgb<<8) + (int)(node.getColor()[1] * 255);
                    rgb = (rgb<<8) + (int)(node.getColor()[2] * 255);
                    anshar$drawText(context, textRenderer, node.getName(), scaledHeight-20, rgb + alpha);
                    
                    var coords = Text.literal(node.getPos().toShortString()).copy().append(" (" + (int) transportComponent.distanceTo(node) + ")");
                    anshar$drawText(context, textRenderer, coords, scaledHeight-32, 0xFFFFFF + alpha);
                }

                // clear render setup
                context.getMatrices().pop();
                
                // allow chat rendering
                Window window = this.client.getWindow();
                int n = MathHelper.floor((double)(this.client.mouse.getX() * (double)window.getScaledWidth() / (double)window.getWidth()));
                int p = MathHelper.floor((double)(this.client.mouse.getY() * (double)window.getScaledHeight() / (double)window.getHeight()));
                this.client.getProfiler().push("chat");
                this.chatHud.render(context, this.ticks, n, p);
                this.client.getProfiler().pop();
                RenderSystem.disableBlend();
            }

            ci.cancel();
        }
    }

    @Unique
    private void anshar$drawText(DrawContext context, TextRenderer textRenderer, Text text, int verticalPos, int color) {
        context.drawText(textRenderer, text, -getTextRenderer().getWidth((StringVisitable)text)/2, verticalPos, color, false);
    }
}
