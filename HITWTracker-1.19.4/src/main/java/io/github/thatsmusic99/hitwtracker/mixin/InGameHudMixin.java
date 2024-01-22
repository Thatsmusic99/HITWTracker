package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.game.Trap;
import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.DrawableHelper.drawTextWithShadow;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "render")
    private void showRecording(MatrixStack matrixStack, float tickDelta, CallbackInfo ci) {

        if (client.options.hudHidden) return;
        if (MinecraftClient.getInstance().player == null) return;
        if (!GameTracker.isTracking()) return;

        // Get the position to go for
        int width = client.getWindow().getScaledWidth();
        int x = width / 8;
        int x2 = width / 4 * 3;
        int y = 3;

        DamageSource damageSource = ((DamageTracking<DamageSource>) MinecraftClient.getInstance().player).gameTracker$getLastDamageSource();
        String damageSourceStr = damageSource == null ? "null (null)" : damageSource.getName() +
                " (" + (damageSource.getAttacker() == null ? "null" : damageSource.getAttacker().getEntityName()) + ")";
        VelocityTracking.VelocityStatus velocitySource = ((VelocityTracking) MinecraftClient.getInstance().player).gameTracker$getVelocityStatus();
        String velocityStatusStr = velocitySource == null ? "null" : velocitySource.name();

        drawTextWithShadow(matrixStack, client.textRenderer, "Tracking", x, y, 16777215);
        drawTextWithShadow(matrixStack, client.textRenderer, Trap.getTrap(() -> (DamageTracking<DamageSource>) MinecraftClient.getInstance().player).displayName, x2, y, 16777215);
        drawTextWithShadow(matrixStack, client.textRenderer, damageSourceStr, x2, y + 10, 16777215);
        drawTextWithShadow(matrixStack, client.textRenderer, velocityStatusStr, x2, y + 20, 16777215);
    }
}
