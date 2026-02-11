package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.InputHandling.KeyControl;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * A GLFW mouse and key callback handler for UIs.
 * 
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class UIInputCallbacks {
    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor();
    private final KeyControl keyControl = new KeyControl();
    private MouseReleaseListener releaseListener;
    private UIManager gui;
    private GLFWWindow window;

    public void init(GLFWWindow window, UIManager gui) {
        this.gui  = gui;
        this.window = window;
        Vector2i mousePosition = window.getMousePosition();
        window.setCallbacks(new KeyPressCallback(), new MouseButtonPressCallback(),
                new MouseMoveCallback(mousePosition), new MouseScrollCallback());
        window.setTextCallback(new CharTypeCallback());
    }

    public void cleanup() {
        taskScheduler.shutdown();
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

    private void onKeyPress(int button, Vector2i pos) {
        gui.checkMouseClick(button, pos.x, pos.y);
        releaseListener = gui;
    }

    private void onKeyRelease(int button, Vector2i pos) {
        if (releaseListener != null) {
            releaseListener.onRelease(button);
            releaseListener = null;
        }
    }

    public void onScroll(float value, Vector2i pos) {
        if (gui.covers(pos.x, pos.y)) {
            gui.onScroll(value);
            return;
        }
    }

    public final void onMouseMove(int xDelta, int yDelta, float xPos, float yPos) {
        gui.onMouseMove(xDelta, yDelta, xPos, yPos);
    }

    private void onKeyTyped(char s) {
        gui.keyTyped(s);
    }

    private class KeyPressCallback extends GLFWKeyCallback {
        @Override
        public void invoke(long windowHandle, int keyCode, int scanCode, int action, int mods) {
            if (keyCode < 0)
                return;
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
            Vector2i pos = window.getMousePosition();

            if (action == GLFW_PRESS) {
                execute(() -> onKeyPress(button, pos));

            } else if (action == GLFW_RELEASE) {
                execute(() -> onKeyRelease(button, pos));
            }
        }
    }

    private class MouseMoveCallback extends GLFWCursorPosCallback {
        // position when adding all integer move calls
        private double xExact;
        private double yExact;

        MouseMoveCallback(Vector2i mousePosition) {
            xExact = mousePosition.x;
            yExact = mousePosition.y;
        }

        @Override
        public void invoke(long windowHandle, double xpos, double ypos) {
            int xDiff = (int) (xpos - xExact);
            int yDiff = (int) (ypos - yExact);
            float xFloat = (float) xpos;
            float yFloat = (float) ypos;

            xExact = xpos;
            yExact = ypos;

            execute(() -> onMouseMove(xDiff, yDiff, xFloat, yFloat));
        }
    }

    private class MouseScrollCallback extends GLFWScrollCallback {
        @Override
        public void invoke(long windowHandle, double xoffset, double yoffset) {
            Vector2i pos = window.getMousePosition();
            execute(() -> onScroll((float) yoffset, pos));
        }
    }

    private class CharTypeCallback extends GLFWCharCallback {
        @Override
        public void invoke(long windowHandle, int codepoint) {
            if (Character.isAlphabetic(codepoint)) {
                char s = Character.toChars(codepoint)[0];
                execute(() -> onKeyTyped(s));
            }
        }
    }
}
