package io.github.thatsmusic99.hitwtracker.mixin;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.util.interfaces.CobwebTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.HotPotatoTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.block.BlockState;
import net.minecraft.block.SandBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Timer;
import java.util.TimerTask;


@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DamageTracking,
        VelocityTracking, HotPotatoTracking, CobwebTracking {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPlayerEntityMixin.class);
    private long lastInAir = System.currentTimeMillis() / 50;
    private @Nullable DamageSource source;
    private @Nullable VelocityStatus velocity;
    private byte count = 0;
    private @Nullable Timer cobwebTimer;
    private boolean hotPotatoTracker = false;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void onGround(boolean onGround) {
        boolean changedGround = onGround ^ this.isOnGround();
        if (!onGround) this.lastInAir = System.currentTimeMillis() / 50;
        if (!GameTracker.isTracking()) return;
        checkSandfall();
        if (this.source == null && this.velocity == null) return;

        if (changedGround || System.currentTimeMillis() / 50 - this.lastInAir > 60) {
            if (this.count == 0) {
                this.source = null;
                this.velocity = null;
                LOGGER.debug("Tracking wiped");
            } else {
                this.count = 0;
                LOGGER.info("Tracking put down");
            }
        }
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        super.onDamaged(damageSource);
        LOGGER.info("onDamage called");
        if (GameTracker.isTracking()) {
            this.source = damageSource;
            this.count = 1;
            LOGGER.info("Damage tracked");
        }
    }

    @Override
    public void onSandfall() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.SANDFALL;
            this.count = 1;
            LOGGER.info("Sandfall tracked");
        }
    }

    public @Nullable DamageSource getLastDamageSource() {
        return this.source == null ? this.getRecentDamageSource() : this.source;
    }

    @Override
    public @Nullable VelocityStatus getVelocityStatus() {
        return velocity;
    }

    public void flush() {
        this.source = null;
        this.velocity = null;
        this.count = 0;
    }

    @Override
    public void onPotatoWarning() {
        this.hotPotatoTracker = true;
    }

    @Override
    public void coolPotatoWarning() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hotPotatoTracker = false;
            }
        }, 1000);
    }

    @Override
    public boolean hasPotatoWarning() {
        return hotPotatoTracker;
    }

    private void checkSandfall() {

        // Store the player entity
        final PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        final BlockState bottomPos = player.getSteppingBlockState();
        final BlockState playerPos = player.getWorld().getBlockState(player.getBlockPos());

        if (bottomPos.getBlock() instanceof SandBlock || playerPos.getBlock() instanceof SandBlock) {
            onSandfall();
        }
    }

    @Override
    public void onExplosion() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.velocity = VelocityStatus.HOT_POTATO;
            this.count = 1;
            LOGGER.info("Explosion tracked");
        }
    }

    @Override
    public void onRodPull() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.FISHING_RODS;
            this.count = 1;
            LOGGER.info("Rod pull tracked");
        }
    }

    @Override
    public void onBlastOff() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.BLAST_OFF;
            this.count = 1;
            LOGGER.info("Blast-off tracked");
        }
    }

    @Override
    public void onWeb() {
        if (!GameTracker.isTracking()) return;
        if (cobwebTimer == null) {
            this.velocity = VelocityStatus.STICKY_SHOES;
        } else {
            this.velocity = VelocityStatus.COBWEBS;
        }
        this.count = 1;
    }

    @Override
    public void onCobwebProvide() {
        cobwebTimer = new Timer();
        cobwebTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cobwebTimer = null;
            }
        }, 15000);
    }
}
