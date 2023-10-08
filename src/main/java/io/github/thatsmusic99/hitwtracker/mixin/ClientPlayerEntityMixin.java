package io.github.thatsmusic99.hitwtracker.mixin;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DamageTracking {

    private long lastInAir = System.currentTimeMillis() / 50;
    private @Nullable DamageSource source;
    private byte count = 0;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void setOnGround(boolean onGround) {
        boolean changedGround = onGround ^ this.isOnGround();
        if (!onGround) this.lastInAir = System.currentTimeMillis() / 50;
        if (GameTracker.isTracking() && (changedGround || this.lastInAir > 20)) {
            if (this.count == 0) {
                this.source = null;
            } else {
                this.count = 0;
            }
        }
        super.setOnGround(onGround);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && GameTracker.isTracking()) {
            this.source = source;
            this.count = 1;
        }
        return result;
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        super.onDamaged(damageSource);
        if (GameTracker.isTracking()) {
            this.source = damageSource;
            this.count = 1;
        }
    }

    public @Nullable DamageSource getLastDamageSource() {
        return this.source == null ? this.getRecentDamageSource() : this.source;
    }

    public void flush() {
        this.source = null;
        this.count = 0;
    }
}
