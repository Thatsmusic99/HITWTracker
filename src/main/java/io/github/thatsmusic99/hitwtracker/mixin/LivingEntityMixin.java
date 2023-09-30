package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.LessRecentDamageGetter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LessRecentDamageGetter {

    @Shadow private long lastDamageTime;
    @Shadow @Nullable private DamageSource lastDamageSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(LivingEntityMixin.class);

    private long lastDamageTimeAdjusted;

    private @Nullable DamageSource lastDamageSourceAdjusted;

    @Redirect(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTime:J", opcode = Opcodes.PUTFIELD))
    public void inject(LivingEntity instance, long value) {
        this.lastDamageTime = value;
        this.lastDamageTimeAdjusted = System.currentTimeMillis() / 50 / 1000;
    }

    @Redirect(method = "onDamaged", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTime:J", opcode = Opcodes.PUTFIELD))
    public void injectDamaged(LivingEntity instance, long value) {
        this.lastDamageTime = value;
        this.lastDamageTimeAdjusted = System.currentTimeMillis() / 50 / 1000;
    }

    @Redirect(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageSource:Lnet/minecraft/entity/damage/DamageSource;", opcode = Opcodes.PUTFIELD))
    public void inject(LivingEntity instance, DamageSource source) {
        this.lastDamageSource = source;
        this.lastDamageSourceAdjusted = source;
    }

    @Redirect(method = "onDamaged", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageSource:Lnet/minecraft/entity/damage/DamageSource;", opcode = Opcodes.PUTFIELD))
    public void injectDamaged(LivingEntity instance, DamageSource source) {
        this.lastDamageSource = source;
        this.lastDamageSourceAdjusted = source;
    }

    public @Nullable DamageSource getLessRecentDamageSource() {

        // Get the time correctly - 1 tick = 50ms
        long millis = System.currentTimeMillis();
        long ticks = millis / 50 / 1000;
        if (ticks - this.lastDamageTimeAdjusted > 80L) {
            LOGGER.info("Resetting source: " + ticks + ", " + this.lastDamageTimeAdjusted);
            this.lastDamageSourceAdjusted = null;
        }

        return this.lastDamageSourceAdjusted;
    }
}
