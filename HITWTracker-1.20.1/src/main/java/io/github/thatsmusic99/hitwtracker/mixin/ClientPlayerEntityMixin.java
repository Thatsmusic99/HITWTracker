package io.github.thatsmusic99.hitwtracker.mixin;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.game.Trap;
import io.github.thatsmusic99.hitwtracker.util.interfaces.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.SandBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Timer;
import java.util.TimerTask;


@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DamageTracking<DamageSource>,
        VelocityTracking, HotPotatoTracking, CobwebTracking, StatusTracking<StatusEffect> {

    private static final @Unique Logger LOGGER = LoggerFactory.getLogger(ClientPlayerEntityMixin.class);
    private @Unique long lastInAir = System.currentTimeMillis() / 50;
    private @Unique @Nullable DamageSource source;
    private @Unique @Nullable VelocityStatus velocity;
    private @Unique @Nullable StatusEffect effect;
    private @Unique byte count = 0;
    private @Unique @Nullable Timer cobwebTimer;
    private @Unique boolean hotPotatoTracker = false;
    private @Unique boolean explosion = false;
    private @Unique boolean launched = false;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void gameTracker$onDamage(DamageSource damageSource) {
        LOGGER.info("Damage tracked");
        if (!GameTracker.isTracking()) return;
        if (damageSource == null) return;
        LOGGER.info("Game is tracking");
        source = damageSource;
        count = 1;
        lastInAir = System.currentTimeMillis() / 50;
        LOGGER.info("Damage source: " + source + ", " + Trap.getTrap(() -> this));
    }

    @Override
    public boolean gameTracker$hasBeenDamagedBy(@NotNull String namespace) {
        return source != null
                && source.getSource() != null
                && Registries.ENTITY_TYPE.get(Identifier.splitOn(namespace, ':')) == source.getSource().getType();
    }

    @Override
    public boolean gameTracker$hasBeenArrowDamagedBy(@NotNull String namespace) {
        return source != null
                && source.getSource() instanceof ArrowEntity
                && source.getAttacker() != null
                && Registries.ENTITY_TYPE.get(Identifier.splitOn(namespace, ':')) == source.getAttacker().getType();
    }

    @Override
    public void gameTracker$onGround(boolean onGround) {
        boolean changedGround = onGround ^ this.isOnGround();
        if (!onGround) this.lastInAir = System.currentTimeMillis() / 50;
        if (!GameTracker.isTracking()) return;
        checkSandfall();
        if (this.source == null && this.velocity == null) return;

        boolean onGroundTooLong = System.currentTimeMillis() / 50 - this.lastInAir > 60;
        if (onGroundTooLong) {
            gameTracker$flush();
            LOGGER.info("Tracking wiped (ground)");
            return;
        }

        if (changedGround && onGround) {
            if (this.count == 0) {
                gameTracker$flush();
                LOGGER.info("Tracking wiped");
            } else {
                this.count = 0;
                LOGGER.info("Tracking put down");
            }
        }
    }

    @Override
    public void gameTracker$onSandfall() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.SANDFALL;
            this.lastInAir = System.currentTimeMillis() / 50;
            this.count = 1;
        }
    }

    public @Nullable DamageSource gameTracker$getLastDamageSource() {
        return this.source == null ? this.getRecentDamageSource() : this.source;
    }

    @Override
    public @Nullable VelocityStatus gameTracker$getVelocityStatus() {
        return velocity;
    }

    public void gameTracker$flush() {
        this.source = null;
        this.velocity = null;
        this.count = -1;
    }

    @Override
    public void gameTracker$onPotatoWarning() {
        this.hotPotatoTracker = true;
    }

    @Override
    public void gameTracker$coolPotatoWarning() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hotPotatoTracker = false;
            }
        }, 1000);
    }

    @Override
    public boolean gameTracker$hasPotatoWarning() {
        return hotPotatoTracker;
    }

    @Unique
    private void checkSandfall() {

        // Store the player entity
        final PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        final BlockState bottomPos = player.getSteppingBlockState();
        final BlockState playerPos = player.getWorld().getBlockState(player.getBlockPos());

        if (bottomPos.getBlock() instanceof SandBlock || playerPos.getBlock() instanceof SandBlock) {
            gameTracker$onSandfall();
        }
    }

    @Override
    public void gameTracker$onExplosion() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.velocity = VelocityStatus.HOT_POTATO;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Explosion tracked");
        }
    }

    @Override
    public void gameTracker$onRodPull() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.FISHING_RODS;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Rod pull tracked");
        }
    }

    @Override
    public void gameTracker$onBlastOff() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.BLAST_OFF;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Blast-off tracked");
        }
    }

    @Override
    public void gameTracker$onWeb() {
        if (!GameTracker.isTracking()) return;
        if (cobwebTimer == null) {
            this.velocity = VelocityStatus.STICKY_SHOES;
        } else {
            this.velocity = VelocityStatus.COBWEBS;
        }
        this.count = 1;
        this.lastInAir = System.currentTimeMillis() / 50;
    }

    @Override
    public void gameTracker$onCobwebProvide() {
        cobwebTimer = new Timer();
        cobwebTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cobwebTimer = null;
            }
        }, 15000);
    }

    @Override
    public int gameTracker$getBlockYNonFinal() {
        return this.getBlockY();
    }

    @Override
    public boolean gameTracker$hadStatusEffect(@NotNull String namespace) {
        return effect != null && Registries.STATUS_EFFECT.get(Identifier.splitOn(namespace, ':')) == effect;
    }

    @Override
    public void gameTracker$applyStatusEffect(@NotNull StatusEffect effect) {
        this.effect = effect;
    }

    @Override
    public void gameTracker$onExplosionSound() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.explosion = true;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            if (!this.launched) return;
            this.velocity = VelocityStatus.HOT_POTATO;
        }
    }

    @Override
    public void gameTracker$onExplosionLaunch() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.launched = true;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            if (!this.explosion) return;
            this.velocity = VelocityStatus.HOT_POTATO;
        }
    }

    @Override
    public int gameTracker$getAmplifier(@NotNull String namespace) {
        return gameTracker$hadStatusEffect(namespace) ?
                (this.effect == null ? 0 : (getStatusEffect(effect) == null ? 0 : getStatusEffect(effect).getAmplifier())) : 0;
    }
}
