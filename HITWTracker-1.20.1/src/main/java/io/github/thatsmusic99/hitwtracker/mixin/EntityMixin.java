package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At(value = "HEAD"), method = "updateSupportingBlockPos")
    public void onSetGround(boolean onGround, Vec3d movement, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        if (((Entity) (Object) this).getId() == MinecraftClient.getInstance().player.getId()) {
            ((VelocityTracking) MinecraftClient.getInstance().player).gameTracker$onGround(onGround);
        }
    }
}
