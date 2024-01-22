package io.github.thatsmusic99.hitwtracker.gui.games;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.thatsmusic99.hitwtracker.gui.games.tab.*;
import io.github.thatsmusic99.hitwtracker.gui.tab.LenientTabManager;
import io.github.thatsmusic99.hitwtracker.mixin.ScreenMixin;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class HITWStatScreen extends Screen {

    public static final Identifier LIGHT_DIRT_BACKGROUND_TEXTURE = new Identifier("textures/gui/light_dirt_background.png");
    public static final Identifier FOOTER_SEPARATOR_TEXTURE = new Identifier("textures/gui/footer_separator.png");
    private final @NotNull TabManager tabManager = new LenientTabManager<>(this, this::addDrawableChild, this::remove);
    private final @NotNull Screen parent;
    private TabNavigationWidget tabNavigationWidget;
    private GridWidget grid;

    public HITWStatScreen(final @NotNull Screen parent) {
        super(Text.of("Hole in the Brain"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        // Draw the tab navigator
        this.tabNavigationWidget = TabNavigationWidget
                .builder(this.tabManager, this.width)
                .tabs(new AllMatchesTab(this.client, this, 66, this.height - 36, 10),
                        new DayStatisticsTab(this.client, this, 66, this.height - 36, 10),
                        new DeathStatisticsTab(this.client, this, 66, this.height - 36),
                        new MapStatisticsTab(this.client, this, 66, this.height - 36),
                        new MiscStatisticsTab(this.client, this, 66, this.height - 36),
                        new TieStatisticsTab(this.client, this, 66, this.height - 36))
                .build();
        this.addDrawableChild(this.tabNavigationWidget);

        // Draw the grid that holds the close button
        this.grid = new GridWidget().setColumnSpacing(10);
        final GridWidget.Adder adder = this.grid.createAdder(1);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.closeScreen()).build());

        this.grid.forEachChild((child) -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });

        // Switch to the first tab
        this.tabNavigationWidget.selectTab(0, false);
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.tabNavigationWidget == null || this.grid == null) return;

        this.tabNavigationWidget.setWidth(this.width);
        this.tabNavigationWidget.init();

        this.grid.refreshPositions();
        SimplePositioningWidget.setPos(this.grid, 0, this.height - 36, this.width, 36);

        int i = this.tabNavigationWidget.getNavigationFocus().getBottom();
        ScreenRect screenRect = new ScreenRect(0, i, this.width, this.grid.getY() - i);
        this.tabManager.setTabArea(screenRect);
    }

    public void closeScreen() {
        this.client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        RenderSystem.setShaderTexture(0, FOOTER_SEPARATOR_TEXTURE);
        drawTexture(matrices, 0, MathHelper.roundUpToMultiple(64, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.setShaderTexture(1, FOOTER_SEPARATOR_TEXTURE);
        drawTexture(matrices, 0, MathHelper.roundUpToMultiple(this.height - 36 - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        super.render(matrices, mouseX, mouseY, delta);

        // For each selectable child
        for (Selectable selectable : ((ScreenMixin) this).getSelectables()) {
            if (!(selectable instanceof Drawable drawable)) continue;
            drawable.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        this.renderBackgroundTexture(matrices);
    }

    public void renderBackgroundTexture(MatrixStack matrices) {
        RenderSystem.setShaderTexture(0, LIGHT_DIRT_BACKGROUND_TEXTURE);
        drawTexture(matrices, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }

    @Override
    public <T extends Element & Selectable> T addSelectableChild(T child) {
        return super.addSelectableChild(child);
    }

    @Override
    public void remove(Element child) {
        super.remove(child);
    }
}
