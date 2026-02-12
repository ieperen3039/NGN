package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import org.joml.Vector2ic;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.*;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SCloseButton extends SComponent implements MouseReleaseListener, MouseClickListener {
    private final int frameTitleBarSize;
    private Runnable closeAction;
    private boolean isPressed = false;

    public SCloseButton(SFrame frame) {
        this(SFrame.FRAME_TITLE_BAR_SIZE, () -> frame.setVisible(false));
    }

    public SCloseButton(int size, Runnable closeAction) {
        this.closeAction = closeAction;
        setGrowthPolicy(false, false);
        frameTitleBarSize = size;
    }

    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    public int minWidth() {
        return frameTitleBarSize;
    }

    @Override
    public int minHeight() {
        return frameTitleBarSize;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic scPos) {
        design.draw(BUTTON, isPressed ? ACTIVATED : (isHovered ? HOVERED : ENABLED), scPos, getSize());
        design.drawText(BUTTON, scPos, getSize(), "X", NGFont.TextType.REGULAR, SFrameLookAndFeel.Alignment.CENTER_MIDDLE);
    }

    @Override
    public void onClick(int button, int x, int y) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) isPressed = true;
    }

    @Override
    public void onRelease(int button) {
        if (isPressed && button == GLFW_MOUSE_BUTTON_LEFT) {
            isPressed = false;
            closeAction.run();
        }
    }

    @Override
    public String toString() {
        return "SCloseButton";
    }
}
