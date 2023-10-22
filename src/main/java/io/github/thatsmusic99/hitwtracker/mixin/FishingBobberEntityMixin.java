package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(FishingBobberEntity.class);

    @Inject(at = @At("TAIL"), method = "pullHookedEntity")
    private void checkEntity(Entity entity, CallbackInfo ci) {

        LOGGER.info("Hooked entity pulled: " + entity.getEntityName());

        // Check if it's ourselves
        if (entity != MinecraftClient.getInstance().player) return;
        LOGGER.info("Hooked entity pulled is own player");

        ((VelocityTracking) MinecraftClient.getInstance().player).onRodPull();
    }
}
