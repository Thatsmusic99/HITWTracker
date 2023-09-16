package io.github.thatsmusic99.hitwtracker.mixin;

import net.minecraft.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow private long lastDamageTime;

    @Redirect(method = "getRecentDamageSource", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTime:J", opcode = Opcodes.GETFIELD))
    private long adjustLastTime(LivingEntity instance) {

        return lastDamageTime - 60;
    }
}
