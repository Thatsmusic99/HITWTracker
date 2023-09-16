package io.github.thatsmusic99.hitwtracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

public class MCCIStatListWidget extends AlwaysSelectedEntryListWidget<MCCIStatListWidget.Entry> {
    public MCCIStatListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {

        @Override
        public Text getNarration() {
            return null;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        }
    }
}
