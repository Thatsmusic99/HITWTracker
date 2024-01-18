package io.github.thatsmusic99.hitwtracker.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class MiscUtils {

    @Contract("null -> null")
    public static String capitalise(@Nullable String str) {
        if (str == null) return null;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
