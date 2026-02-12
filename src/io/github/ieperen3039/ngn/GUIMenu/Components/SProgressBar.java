package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Supplier;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.PANEL;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.PROGRESS_BAR;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.PROGRESS_BAR_FILL;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.ACTIVATED;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.ENABLED;

/**
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public class SProgressBar extends SComponent {
    private int minWidth;
    private int minHeight;
    private final Supplier<Float> progress;

    public SProgressBar(Supplier<Float> progress, SComponentProperties properties) {
        this.progress = progress;
        this.minWidth = properties.minWidth;
        this.minHeight = properties.minHeight;
        setGrowthPolicy(properties.wantHzGrow, properties.wantVtGrow);
    }

    public SProgressBar(int minWidth, int minHeight, Supplier<Float> progressSource) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.progress = progressSource;
        setGrowthPolicy(false, false);
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(PROGRESS_BAR, ENABLED, screenPosition, getSize());
        Float heath = progress.get();

        if (heath > 0) {
            Vector2i bar = new Vector2i((int) (getWidth() * heath), getHeight());
            design.draw(PROGRESS_BAR_FILL, ENABLED, screenPosition, bar);
        }
    }
}
