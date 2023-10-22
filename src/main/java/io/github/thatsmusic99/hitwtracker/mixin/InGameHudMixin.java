package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.game.Trap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "render")
    private void showRecording(DrawContext context, float tickDelta, CallbackInfo ci) {

        if (client.options.hudHidden) return;
        if (MinecraftClient.getInstance().player == null) return;
        if (!GameTracker.isTracking()) return;

        // Get the position to go for
        int width = context.getScaledWindowWidth();
        int x = width / 8;
        int y = 3;

        context.drawTextWithShadow(client.textRenderer, "Tracking", x, y, 16777215);
        context.drawTextWithShadow(client.textRenderer, Trap.getTrap(MinecraftClient.getInstance().player).displayName, width - x, y, 16777215);
    }
}
