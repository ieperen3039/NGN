package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import io.github.ieperen3039.ngn.Tools.Logger;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collection;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.*;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A Button that may execute actions for both left and right clicks upon release.
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SButton extends STextComponent implements MouseReleaseListener, MouseClickListener {
    public static final int DEFAULT_MIN_WIDTH = 250;
    public static final int DEFAULT_MIN_HEIGHT = 30;
    public static final NGFont.TextType DEFAULT_TEXT_TYPE = NGFont.TextType.REGULAR;

    private Collection<Runnable> leftClickListeners = new ArrayList<>();
    private Collection<Runnable> rightClickListeners = new ArrayList<>();

    private boolean isPressed = false;

    /**
     * a button with no associated action (a dead button)
     * @param text the text of the button
     * @see #addLeftClickListener(Runnable)
     */
    public SButton(String text) {
        super(text, DEFAULT_TEXT_TYPE, SFrameLookAndFeel.Alignment.CENTER_MIDDLE, DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT);
        parentComponentType = UIComponentType.BUTTON;
    }

    /**
     * a button with a basic associated action
     * @param text   the text of the button
     * @param action the action that is executed upon (releasing a) left click
     */
    public SButton(String text, Runnable action) {
        this(text);
        addLeftClickListener(action);
    }

    /**
     * a button with no associated action (a dead button)
     * @param text  the text of the button
     * @param props component properties
     * @see #addLeftClickListener(Runnable)
     */
    public SButton(String text, SComponentProperties props) {
        super(text, props);
        parentComponentType = UIComponentType.BUTTON;
    }

    /**
     * a button with a basic associated action
     * @param text   the text of the button
     * @param action the action that is executed upon (releasing a) left click
     */
    public SButton(String text, SComponentProperties props, Runnable action) {
        this(text, props);
        leftClickListeners.add(action);
    }

    /**
     * a button with both a left and a right click action
     * @param text         the text of the button
     * @param onLeftClick  the action that is executed upon (releasing a) left click
     * @param onRightClick the action that is executed upon (releasing a) right click
     */
    public SButton(String text, SComponentProperties props, Runnable onLeftClick, Runnable onRightClick) {
        this(text, props, onLeftClick);
        rightClickListeners.add(onRightClick);
    }

    public SButton addLeftClickListener(Runnable action) {
        leftClickListeners.add(action);
        return this;
    }

    public SButton addRightClickListeners(Runnable action) {
        rightClickListeners.add(action);
        return this;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        UIState state = isPressed ? ACTIVATED : (isHovered ? HOVERED : ENABLED);
        design.draw(UIComponentType.BUTTON, state, screenPosition, getSize());
        super.draw(design, screenPosition);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        isPressed = true;
    }

    @Override
    public void onRelease(int button) {
        isPressed = false;

        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            leftClickListeners.forEach(Runnable::run);

        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            rightClickListeners.forEach(Runnable::run);

        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getText() + ")";
    }
}
