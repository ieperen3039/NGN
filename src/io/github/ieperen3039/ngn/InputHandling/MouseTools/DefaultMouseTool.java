package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.Core.Main;

/**
 * A mouse tool that implements the standard behaviour of the pointer user input: the user interface if the cursor is
 * on it, the camera otherwise.
 * @author Geert van Ieperen. Created on 26-11-2018.
 */
public class DefaultMouseTool implements MouseTool {
    protected final Main root;
    private boolean gestureOnGui = false;

    public DefaultMouseTool(Main root) {
        this.root = root;
    }

    @Override
    public boolean onHover(MouseTarget target, int xDelta, int yDelta, float xSc, float ySc) {
        root.gui().onMouseMove(xDelta, yDelta, xSc, ySc);
        if (!(target instanceof UITarget)) {
            root.camera().onMouseMove(xDelta, yDelta, xSc, ySc);
        }
        return true;
    }

    @Override
    public boolean onPress(int button, MouseTarget target, int xSc, int ySc) {
        // ask the gui even without a UITarget, as an active modal component captures clicks outside itself
        gestureOnGui = root.gui().checkMouseClick(button, xSc, ySc);
        if (!gestureOnGui) {
            root.camera().onClick(button, xSc, ySc);
        }
        return true;
    }

    @Override
    public void onDrag(int xDelta, int yDelta, float xSc, float ySc) {
        if (gestureOnGui) {
            root.gui().onMouseMove(xDelta, yDelta, xSc, ySc);
        } else {
            root.camera().onMouseMove(xDelta, yDelta, xSc, ySc);
        }
    }

    @Override
    public void onRelease(int button, MouseTarget target, int xSc, int ySc) {
        if (gestureOnGui) {
            root.gui().onRelease(button);
        } else {
            root.camera().onRelease(button);
        }
    }

    @Override
    public boolean onScroll(float value, MouseTarget target, int xSc, int ySc) {
        if (target instanceof UITarget) {
            root.gui().onScroll(value);
        } else {
            root.camera().onScroll(value);
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
