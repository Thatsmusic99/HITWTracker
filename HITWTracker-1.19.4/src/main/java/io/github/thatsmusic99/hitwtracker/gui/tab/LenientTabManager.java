package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LenientTabManager<T extends Widget & Drawable & Selectable> extends TabManager {

    private final Consumer<T> lenientTabLoadConsumer;
    private final Consumer<T> lenientTabUnloadConsumer;
    private final Consumer<ClickableWidget> tabLoadConsumer;
    private final Consumer<ClickableWidget> tabUnloadConsumer;
    private @Nullable Tab tab;
    private @Nullable ScreenRect tabArea;
    private final @NotNull HITWStatScreen parent;

    public LenientTabManager(final @NotNull HITWStatScreen parent,
                             final Consumer<T> tabLoadConsumer,
                             final Consumer<T> tabUnloadConsumer) {
        super(widget -> tabLoadConsumer.accept((T) widget), widget -> tabUnloadConsumer.accept((T) widget));
        this.lenientTabLoadConsumer = tabLoadConsumer;
        this.lenientTabUnloadConsumer = tabUnloadConsumer;
        this.tabLoadConsumer = widget -> tabLoadConsumer.accept((T) widget);
        this.tabUnloadConsumer = widget -> tabUnloadConsumer.accept((T) widget);
        this.parent = parent;
    }

    @Override
    public void setCurrentTab(Tab tab, boolean clickSound) {
        if (this.getCurrentTab() == tab) return;

        if (this.getCurrentTab() != null) {
            if (this.getCurrentTab() instanceof LenientTab lenientTab) {
                lenientTab.unload(parent);
            } else {
                tab.forEachChild(this.tabUnloadConsumer);
            }
        }

        this.tab = tab;

        if (this.getCurrentTab() instanceof LenientTab lenientTab) {
            lenientTab.load(this.parent);
        } else {
            tab.forEachChild(this.tabLoadConsumer);
        }

        if (this.tabArea != null) {
            tab.refreshGrid(this.tabArea);
        }

        if (clickSound) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public void setTabArea(ScreenRect tabArea) {
        this.tabArea = tabArea;
        super.setTabArea(tabArea);
    }

    public Tab getCurrentTab() {
        return tab;
    }
}
