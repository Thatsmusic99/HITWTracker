package io.github.thatsmusic99.hitwtracker.mixin;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.game.GameTracker;
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

import java.util.Timer;
import java.util.TimerTask;


@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DamageTracking<DamageSource>,
        VelocityTracking, HotPotatoTracking, CobwebTracking, StatusTracking<StatusEffect> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPlayerEntityMixin.class);
    private long lastInAir = System.currentTimeMillis() / 50;
    private @Nullable DamageSource source;
    private @Nullable VelocityStatus velocity;
    private @Nullable StatusEffect effect;
    private byte count = 0;
    private @Nullable Timer cobwebTimer;
    private boolean hotPotatoTracker = false;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
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
    public void gameTracker$onSandfall() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.SANDFALL;
            this.count = 1;
            LOGGER.info("Sandfall tracked");
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
        this.count = 0;
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
            LOGGER.info("Explosion tracked");
        }
    }

    @Override
    public void gameTracker$onRodPull() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.FISHING_RODS;
            this.count = 1;
            LOGGER.info("Rod pull tracked");
        }
    }

    @Override
    public void gameTracker$onBlastOff() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.BLAST_OFF;
            this.count = 1;
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
    public int gameTracker$getAmplifier(@NotNull String namespace) {
        return gameTracker$hadStatusEffect(namespace) ?
                (this.effect == null ? 0 : (getStatusEffect(effect) == null ? 0 : getStatusEffect(effect).getAmplifier())) : 0;
    }
}
