package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("TAIL"), method = "onDamaged")
    public void onDamage(DamageSource damageSource, CallbackInfo ci) {

        if (MinecraftClient.getInstance().player == null) return;
        if (((Entity) (Object) this).getId() == MinecraftClient.getInstance().player.getId()) {
            ((DamageTracking<DamageSource>) MinecraftClient.getInstance().player).gameTracker$onDamage(damageSource);
        }
    }
}
