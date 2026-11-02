package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Core.ToolElement;
import io.github.ieperen3039.ngn.InputHandling.KeyControl;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * A callback handler that can be attached to a 
 * mouse pointer for manipulating the behaviour of the mouse
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class MouseToolCallbacks implements ToolElement {
    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor();
    private final KeyControl keyControl = new KeyControl();
    private volatile List<MouseSelector> selectors = null;
    private Main root;
    private volatile MouseTool activeTool = null;
    private volatile MouseTool defaultTool = null;

    // only accessed from the input thread
    private MouseTool gestureOwner = null;
    private int buttonsHeld = 0;

    @Override
    public void init(Main root) {
        if (this.root != null) return;
        this.root = root;

        // keep any tool or layers set before init
        if (defaultTool == null) defaultTool = new DefaultMouseTool(root);
        if (selectors == null) setSelectionLayers(new UISelector(root.gui()));

        GLFWWindow target = root.window();
        Vector2i mousePosition = target.getMousePosition();
        target.setCallbacks(new KeyPressCallback(), new MouseButtonPressCallback(), new MouseMoveCallback(mousePosition), new MouseScrollCallback());
        target.setTextCallback(new CharTypeCallback());
    }

    @Override
    public void cleanup() {
        taskScheduler.shutdown();
    }

    public MouseTool getMouseTool() {
        return activeTool;
    }

    /**
     * @param tool the new active tool, or null for no active tool (only the default tool). A running gesture stays
     *             with its owner.
     */
    public void setMouseTool(MouseTool tool) {
        MouseTool old = activeTool;
        if (old == tool) return;

        if (old != null) old.onDeactivate();
        activeTool = tool;
        if (tool != null) tool.onActivate();
        Logger.DEBUG.print("Set mousetool to " + tool);
    }

    public MouseTool getDefaultMouseTool() {
        return defaultTool;
    }

    public void setDefaultMouseTool(MouseTool tool) {
        defaultTool = tool;
    }

    /**
     * Initialize all layers that listen to mouse behaviour.
     * The first layer that does not return null will have its target passed to the active MouseTool.
     * Make sure to add a UISelector, or the UI will never receive events.
     */
    public void setSelectionLayers(MouseSelector... layers) {
        selectors = List.of(layers);
    }

    public KeyControl getKeyControl() {
        return keyControl;
    }

    private void execute(Runnable action) {
        taskScheduler.submit(() -> {
            try {
                action.run();

            } catch (Throwable ex) {
                // Caught an error while executing an input handler.
                // Look at the second element of the stack trace
                Logger.ERROR.print(ex);
                Toolbox.display(ex);
            }
        });
    }

    private MouseTarget pick(int xSc, int ySc) {
        for (MouseSelector layer : selectors) {
            MouseTarget target = layer.pick(xSc, ySc);
            if (target != null) return target;
        }
        return MouseTarget.NONE;
    }

    private class KeyPressCallback extends GLFWKeyCallback {
        @Override
        public void invoke(long window, int keyCode, int scanCode, int action, int mods) {
            if (keyCode < 0) return;
            if (action == GLFW_PRESS) {
                execute(() -> keyControl.keyPressed(keyCode));

            } else if (action == GLFW_RELEASE) {
                execute(() -> keyControl.keyReleased(keyCode));
            }
        }
    }

    private class MouseButtonPressCallback extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            Vector2i pos = root.window().getMousePosition();

            if (action == GLFW_PRESS) {
                execute(() -> press(button, pos.x, pos.y));

            } else if (action == GLFW_RELEASE) {
                execute(() -> release(button, pos.x, pos.y));
            }
        }
    }

    private void press(int button, int x, int y) {
        MouseTarget target = pick(x, y);
        buttonsHeld++;

        if (gestureOwner != null) {
            gestureOwner.onPress(button, target, x, y);
            return;
        }

        if (activeTool != null) {
            boolean hold = activeTool.onPress(button, target, x, y);
            if (hold) gestureOwner = activeTool;
        } else {
            boolean hold = defaultTool.onPress(button, target, x, y);
            if (hold) gestureOwner = defaultTool;
        }
    }

    private void release(int button, int x, int y) {
        MouseTarget target = pick(x, y);
        buttonsHeld = Math.max(0, buttonsHeld - 1);

        MouseTool owner = gestureOwner;
        if (buttonsHeld == 0) gestureOwner = null;
        if (owner != null) owner.onRelease(button, target, x, y);
    }

    private void move(int xDiff, int yDiff, float x, float y) {
        if (gestureOwner != null) {
            gestureOwner.onDrag(xDiff, yDiff, x, y);
            return;
        }

        MouseTarget target = pick((int) x, (int) y);
        if (activeTool != null && activeTool.onHover(target, xDiff, yDiff, x, y)) return;
        defaultTool.onHover(target, xDiff, yDiff, x, y);
    }

    private void scroll(float value, int x, int y) {
        MouseTarget target = pick(x, y);
        if (activeTool != null && activeTool.onScroll(value, target, x, y)) return;
        defaultTool.onScroll(value, target, x, y);
    }

    private class MouseMoveCallback extends GLFWCursorPosCallback {
        // position when adding all integer move calls
        private int xGiven;
        private int yGiven;

        MouseMoveCallback(Vector2i mousePos) {
            xGiven = mousePos.x;
            yGiven = mousePos.y;
        }

        @Override
        public void invoke(long window, double xpos, double ypos) {
            int xDiff = (int) (xpos - xGiven);
            int yDiff = (int) (ypos - yGiven);
            float xFloat = (float) xpos;
            float yFloat = (float) ypos;

            if (xDiff != 0 || yDiff != 0) {
                xGiven += xDiff;
                yGiven += yDiff;
            }
            execute(() -> move(xDiff, yDiff, xFloat, yFloat));
        }
    }

    private class MouseScrollCallback extends GLFWScrollCallback {
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            Vector2i pos = root.window().getMousePosition();
            execute(() -> scroll((float) yoffset, pos.x, pos.y));
        }
    }

    private class CharTypeCallback extends GLFWCharCallback {
        @Override
        public void invoke(long window, int codepoint) {
            if (Character.isAlphabetic(codepoint)) {
                char s = Character.toChars(codepoint)[0];

                execute(() -> root.gui().keyTyped(s));
            }
        }
    }
}
