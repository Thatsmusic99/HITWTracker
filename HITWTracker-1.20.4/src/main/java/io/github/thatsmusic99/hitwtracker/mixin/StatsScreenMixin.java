package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsScreen.class)
public class StatsScreenMixin extends Screen {

    protected StatsScreenMixin() {
        super(Text.of(""));
    }

    @Inject(at = @At("TAIL"), method = "createButtons")
    private void addHITWButton(CallbackInfo ci) {
        ScreenMixin mixin = (ScreenMixin) this;
        mixin.invokeDrawableChild(ButtonWidget.builder(Text.of("HITW"), (button) -> {
            MinecraftClient.getInstance().setScreen(new HITWStatScreen(this));
        }).dimensions(mixin.getWidth() / 2 + 120, mixin.getHeight() - 52, 60, 20).build());
    }
}
