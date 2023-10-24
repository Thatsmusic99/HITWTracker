package io.github.thatsmusic99.hitwtracker.game;

import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public enum Trap {

    ARROW_STORM(10, "Arrow Storm", player -> ((DamageTracking) player).getLastDamageSource() != null
            && ((DamageTracking) player).getLastDamageSource().getSource() instanceof ArrowEntity
            && ((DamageTracking) player).getLastDamageSource().getAttacker() instanceof ArrowEntity),
    EGG(10, "Eggs", player -> checkDamageSource(player, EggEntity.class)),
    MATRIX(10, "Matrix", player -> checkDamageSource(player, FireballEntity.class)),
    CREEPY_CRAWLIES(20, "Creepy Crawlies!", player -> checkDamageSource(player, SpiderEntity.class)),
    FEELING_HOT(10, "Feeling Hot", player -> checkDamageSource(player, SmallFireballEntity.class)),
    PILLAGERS(20, "Pillagers", player -> checkArrowDamageSource(player, PillagerEntity.class)),
    REVENGE(20, "Revenge!", player -> checkDamageSource(player, SlimeEntity.class)),
    HOGLIN(20, "Hoglins", player -> checkDamageSource(player, HoglinEntity.class)),
    MUMMY(20, "Mummy?", player -> checkDamageSource(player, HuskEntity.class)),
    SO_LONELY(20, "So Lonely", player -> checkDamageSource(player, ZombieEntity.class)),
    SWIMMY_FISH(10, "Swimmy Fish", player -> checkDamageSource(player, GuardianEntity.class)),
    FISHING_RODS(10, "Fishing Rods", player -> ((VelocityTracking) player).getVelocityStatus() == VelocityTracking.VelocityStatus.FISHING_RODS),
    BLAST_OFF(15, "Blast Off", player -> ((VelocityTracking) player).getVelocityStatus() == VelocityTracking.VelocityStatus.BLAST_OFF),
    LEVITATION_DART(10, "Levitation Dart", player -> checkArrowDamageSource(player, PlayerEntity.class)),
    ONE_PUNCH(10, "One Punch", player -> checkDamageSource(player, PlayerEntity.class)),
    SNOWBALL_FIGHT(15, "Snowball Fight", player -> checkDamageSource(player, SnowballEntity.class)),
    COBWEBS(15, "Cobwebs", player -> ((VelocityTracking) player).getVelocityStatus() == VelocityTracking.VelocityStatus.COBWEBS),
    STICKY_SHOES(15, "Sticky Shoes", player -> ((VelocityTracking) player).getVelocityStatus() == VelocityTracking.VelocityStatus.STICKY_SHOES),
    SANDFALL(15, "Sandfall", player -> ((VelocityTracking) player).getVelocityStatus() == VelocityTracking.VelocityStatus.SANDFALL),
    LEG_DAY(10, "Leg Day", player -> player.hasStatusEffect(StatusEffects.SLOWNESS)),
    LOW_GRAVITY(10, "Low Gravity", player -> player.hasStatusEffect(StatusEffects.SLOW_FALLING)),
    SPRINGY_SHOES(10, "Springy Shoes", player -> player.hasStatusEffect(StatusEffects.JUMP_BOOST)
            && player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() > 2),
    SUPER_SPEED(10, "Super Speed", player -> player.hasStatusEffect(StatusEffects.SPEED)
            && player.getStatusEffect(StatusEffects.SPEED).getAmplifier() > 2),
    HEART_ATTACK(0, "Heart Attack", player -> GameTracker.startingY <= player.getBlockY()),
    SKILL_ISSUE(0, "Skill Issue", player -> true);

    public final int duration;
    public final @NotNull String displayName;
    public final @NotNull Predicate<ClientPlayerEntity> affected;

    Trap(int duration, @NotNull String displayName, @NotNull Predicate<ClientPlayerEntity> affected) {
        this.duration = duration;
        this.displayName = displayName;
        this.affected = affected;
    }

    private static boolean checkDamageSource(@NotNull ClientPlayerEntity player, @NotNull Class<? extends Entity> clazz) {

        DamageSource source = ((DamageTracking) player).getLastDamageSource();

        return source != null
                && source.getSource() != null
                && clazz.isAssignableFrom(source.getSource().getClass());
    }

    private static boolean checkArrowDamageSource(@NotNull ClientPlayerEntity player, @NotNull Class<? extends Entity> clazz) {

        DamageSource source = ((DamageTracking) player).getLastDamageSource();

        return source != null
                && source.getSource() instanceof ArrowEntity
                && source.getAttacker() != null
                && clazz.isAssignableFrom(source.getAttacker().getClass());
    }

    public static Trap getTrap(@NotNull ClientPlayerEntity player) {

        // Check each trap condition
        for (Trap trap : values()) {
            if (trap.affected.test(player)) return trap;
        }

        return SKILL_ISSUE;
    }
}
