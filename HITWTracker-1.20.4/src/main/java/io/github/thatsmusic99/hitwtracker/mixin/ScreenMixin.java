package io.github.thatsmusic99.hitwtracker.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenMixin {

    @Invoker("addDrawableChild")
    <T extends Element & Drawable & Selectable> T invokeDrawableChild(T drawableElement);

    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();

    @Accessor("selectables")
    List<Selectable> getSelectables();
}
