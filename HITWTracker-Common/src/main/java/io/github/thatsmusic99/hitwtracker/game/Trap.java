package io.github.thatsmusic99.hitwtracker.game;

import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.StatusTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public enum Trap {

    ARROW_STORM(10, "Arrow Storm", player -> player.gameTracker$hasBeenArrowDamagedBy("minecraft:arrow")),
    EGG(10, "Eggs", player -> player.gameTracker$hasBeenDamagedBy("minecraft:egg")),
    MATRIX(10, "Matrix", player -> player.gameTracker$hasBeenDamagedBy("minecraft:fireball")),
    CREEPY_CRAWLIES(20, "Creepy Crawlies!", player -> player.gameTracker$hasBeenDamagedBy("minecraft:spider")),
    FEELING_HOT(10, "Feeling Hot", player -> player.gameTracker$hasBeenDamagedBy("minecraft:small_fireball")),
    PILLAGERS(20, "Pillagers", player -> player.gameTracker$hasBeenArrowDamagedBy("minecraft:pillager")),
    REVENGE(20, "Revenge!", player -> player.gameTracker$hasBeenDamagedBy("minecraft:slime")),
    HOGLIN(20, "Hoglins", player -> player.gameTracker$hasBeenDamagedBy("minecraft:hoglin")),
    MUMMY(20, "Mummy?", player -> player.gameTracker$hasBeenDamagedBy("minecraft:husk")),
    SO_LONELY(20, "So Lonely", player -> player.gameTracker$hasBeenDamagedBy("minecraft:zombie")),
    SWIMMY_FISH(10, "Swimmy Fish", player -> player.gameTracker$hasBeenDamagedBy("minecraft:guardian")),
    FISHING_RODS(10, "Fishing Rods", player -> ((VelocityTracking) player).gameTracker$getVelocityStatus() == VelocityTracking.VelocityStatus.FISHING_RODS),
    BLAST_OFF(15, "Blast Off", player -> ((VelocityTracking) player).gameTracker$getVelocityStatus() == VelocityTracking.VelocityStatus.BLAST_OFF),
    LEVITATION_DART(10, "Levitation Dart", player -> player.gameTracker$hasBeenArrowDamagedBy("minecraft:player")),
    ONE_PUNCH(10, "One Punch", player -> player.gameTracker$hasBeenDamagedBy("minecraft:player")),
    SNOWBALL_FIGHT(15, "Snowball Fight", player -> player.gameTracker$hasBeenDamagedBy("minecraft:snowball")),
    COBWEBS(15, "Cobwebs", player -> ((VelocityTracking) player).gameTracker$getVelocityStatus() == VelocityTracking.VelocityStatus.COBWEBS),
    STICKY_SHOES(15, "Sticky Shoes", player -> ((VelocityTracking) player).gameTracker$getVelocityStatus() == VelocityTracking.VelocityStatus.STICKY_SHOES),
    SANDFALL(15, "Sandfall", player -> ((VelocityTracking) player).gameTracker$getVelocityStatus() == VelocityTracking.VelocityStatus.SANDFALL),
    LEG_DAY(10, "Leg Day", player -> ((StatusTracking<?>) player).gameTracker$hadStatusEffect("minecraft:slowness")),
    LOW_GRAVITY(10, "Low Gravity", player -> ((StatusTracking<?>) player).gameTracker$hadStatusEffect("minecraft:slow_falling")),
    SPRINGY_SHOES(10, "Springy Shoes", player -> ((StatusTracking<?>) player).gameTracker$hadStatusEffect("minecraft:jump_boost")
            && ((StatusTracking<?>) player).gameTracker$getAmplifier("minecraft:jump_boost") > 2),
    SUPER_SPEED(10, "Super Speed", player -> ((StatusTracking<?>) player).gameTracker$hadStatusEffect("minecraft:speed")
            && ((StatusTracking<?>) player).gameTracker$getAmplifier("minecraft:speed") > 2),
    HEART_ATTACK(0, "Heart Attack", player -> GameTracker.startingY <= ((VelocityTracking) player).gameTracker$getBlockYNonFinal()),
    SKILL_ISSUE(0, "Skill Issue", player -> true);

    public final int duration;
    public final @NotNull String displayName;
    public final @NotNull Predicate<DamageTracking<?>> affected;

    Trap(int duration, @NotNull String displayName, @NotNull Predicate<DamageTracking<?>> affected) {
        this.duration = duration;
        this.displayName = displayName;
        this.affected = affected;
    }

    public static Trap getTrap(@NotNull Supplier<? extends DamageTracking<?>> player) {

        // Check each trap condition
        for (Trap trap : values()) {
            if (trap.affected.test(player.get())) return trap;
        }

        return SKILL_ISSUE;
    }
}
