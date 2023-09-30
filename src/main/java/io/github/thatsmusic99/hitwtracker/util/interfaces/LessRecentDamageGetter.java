package io.github.thatsmusic99.hitwtracker.util.interfaces;

import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;

public interface LessRecentDamageGetter {

    @Nullable DamageSource getLessRecentDamageSource();
}
