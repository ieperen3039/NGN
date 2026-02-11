package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.InputHandling.MouseListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen created on 24-4-2020.
 */
public abstract class MouseTool implements MouseListener {
    protected Main root;

    private MouseReleaseListener releaseListener;
    private Runnable onCancel = null;

    public MouseTool(Main root) {
        this.root = root;
        releaseListener = root.camera();
    }

    @Override
    public void onClick(int button, int x, int y) {
        if (root.gui().checkMouseClick(button, x, y)) {
            releaseListener = root.gui();
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && onCancel != null) {
            disableThis();
            return;
        }

        Camera camera = root.camera();
        camera.onClick(button, x, y);
        releaseListener = camera;
    }

    public void disableThis() {
        if (onCancel == null) {
            root.inputHandling().setMouseTool(null);

        } else {
            onCancel.run();
        }
    }

    @Override
    public void onRelease(int button) {
        // this prevents the case when a mouse-down caused a mouse tool switch
        if (releaseListener != null) {
            releaseListener.onRelease(button);
            releaseListener = null;
        }
    }

    @Override
    public void onScroll(float value) {
        Vector2i pos = root.window().getMousePosition();
        UIManager gui = root.gui();

        if (gui.covers(pos.x, pos.y)) {
            gui.onScroll(value);
            return;
        }

        // camera
        root.camera().onScroll(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public final void onMouseMove(int xDelta, int yDelta, float xPos, float yPos) {
        root.gui().onMouseMove(xDelta, yDelta, xPos, yPos);
        if (root.gui().covers((int) xPos, (int) yPos)) return;

        root.camera().onMouseMove(xDelta, yDelta, xPos, yPos);
    }

    /**
     * activates when this mousetool is deactivated
     */
    public void dispose() {
    }
}
