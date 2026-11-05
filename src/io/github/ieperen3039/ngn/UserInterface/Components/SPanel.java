package io.github.ieperen3039.ngn.UserInterface.Components;

import static io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel.UIComponentType.PANEL;
import static io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel.UIState.ENABLED;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import io.github.ieperen3039.ngn.UserInterface.LayoutManagers.GridLayoutManager;
import io.github.ieperen3039.ngn.UserInterface.LayoutManagers.ListLayoutManager;
import io.github.ieperen3039.ngn.UserInterface.LayoutManagers.SLayoutManager;
import io.github.ieperen3039.ngn.UserInterface.LayoutManagers.SingleElementLayout;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SPanel extends SContainer {
    /**
     * creates a panel with the given layout manager
     * @param layoutManager  a new layout manager for this component
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            SLayoutManager layoutManager, boolean growHorizontal,
            boolean growVertical
    ) {
        super(layoutManager);
        setGrowthPolicy(growHorizontal, growVertical);
    }

    /**
     * creates a panel that uses a grid layout with the given number of rows and columns
     * @param cols           number of columns in this panel
     * @param rows           number of grid rows in this panel
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            int cols, int rows, boolean growHorizontal, boolean growVertical
    ) {
        this(new GridLayoutManager(cols, rows), growHorizontal, growVertical);
    }

    /**
     * the basic panel for holding components. Use {@link Vector2i} for positioning elements. Note that indices are
     * 0-indexed.
     * @param cols number of columns in x direction
     * @param rows number of rows in y direction
     */
    public SPanel(int cols, int rows) {
        super(new GridLayoutManager(cols, rows));
    }

    /**
     * creates a panel with the given layout manager, minimum size of (0, 0) and growth policy in both dimensions
     * @param layoutManager a new layout manager for this component
     * @param growPolicy    if true, try to grow
     */
    public SPanel(SLayoutManager layoutManager, boolean growPolicy) {
        this(layoutManager, growPolicy, growPolicy);
    }

    /**
     * creates a panel with the given layout manager, minimum size of (0, 0)
     * @param layoutManager a layout manager for this component
     */
    public SPanel(SLayoutManager layoutManager) {
        super(layoutManager);
    }

    /**
     * creates a panel with a single element. Calling {@link #add(SComponent)} causes the new element to be 
     * added below the current element.
     * @param content the element of this panel.
     */
    public SPanel(SComponent content) {
        super(new ListLayoutManager(false));
        add(content);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        assert getWidth() > 0 && getHeight() > 0 :
                String.format("Non-positive dimensions of %s: width = %d, height = %d", this, getWidth(), getHeight());

        design.draw(PANEL, ENABLED, screenPosition, getSize());
        drawChildren(design, screenPosition);
    }
}
