package io.github.ieperen3039.ngn.UserInterface.Components;

import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.UserInterface.SComponentProperties;
import org.joml.Vector2ic;

/**
 * An area that can show a component or be hidden. This component has its own minimum size, and the given component is
 * stretched to fit. The growth policy and minimum size of the given component is ignored. If the minimum size of the
 * component is larger than the minimum size of this component, an assertion is thrown.
 * @author Geert van Ieperen created on 12-7-2019.
 */
public class SComponentArea extends SComponent {
    private SComponent inner = null;
    private int width;
    private int height;

    public SComponentArea(SComponentProperties props) {
        this.width = props.minWidth;
        this.height = props.minHeight;
        setGrowthPolicy(props.wantHzGrow, props.wantVtGrow);
    }

    public SComponentArea(int width, int height) {
        this.width = width;
        this.height = height;
        setGrowthPolicy(false, false);
    }

    /**
     * removes the current component
     */
    public void hide() {
        inner = null;
    }

    /**
     * sets the current component
     *
     * @param element an element, or null if the element should be removed
     * @see hide
     */
    public void show(SComponent element) {
        if (element == null) {
            hide();
            return;
        }

        // validate layout of base, because we will replace this component
        super.validateLayout();
        int width = getWidth();
        int height = getHeight();

        if (element.minWidth() <= width && element.minHeight() <= height) {
            element.setSize(width, height);
            element.setGrowthPolicy(true, true);
            inner = element;
            element.setParent(this);
            invalidateLayout();

        } else {
            Logger.ASSERT.print("Element too large to show", element, element.getSize(), getSize());
        }
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
        if (inner != null) inner.draw(design, screenPosition);
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (inner != null) {
            return inner.getComponentAt(xRel, yRel);
        } else {
            return null;
        }
    }

    @Override
    public void doValidateLayout() {
        super.doValidateLayout();
        if (inner != null) {
            inner.validateLayout();
        }
    }
}
