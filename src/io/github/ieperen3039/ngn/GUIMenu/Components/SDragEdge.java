package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.InputHandling.MouseDragListener;
import org.joml.Vector2ic;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.BUTTON;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.*;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SDragEdge extends SComponent implements MouseDragListener {
    private final SComponent parent;
    private final int width;
    private final int height;

    public SDragEdge(SComponent parent, int width, int height) {
        this.width = width;
        this.height = height;
        this.parent = parent;
        setSize(width, height);
        setGrowthPolicy(false, false);
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(BUTTON, ENABLED, screenPosition, getSize());
        design.drawText(BUTTON, screenPosition, getSize(), "+", NGFont.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT_MIDDLE);
    }

    @Override
    public void onMouseDrag(int xDelta, int yDelta, float xPos, float yPos) {
        parent.addToSize(xDelta, yDelta);
    }
}
