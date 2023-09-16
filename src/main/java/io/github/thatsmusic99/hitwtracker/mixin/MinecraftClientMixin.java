package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("TAIL"), method = "joinWorld")
    private void disconnect(ClientWorld world, CallbackInfo ci) {
        GameTracker.reset();
    }

    @Inject(at = @At("TAIL"), method = "disconnect()V")
    private void disconnect(CallbackInfo ci) {
        GameTracker.reset();
    }
}
