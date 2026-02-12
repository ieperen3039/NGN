package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIState.*;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel.UIComponentType.*;

/**
 * A button with a state that only changes upon clicking the button
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SToggleButton extends STextComponent implements MouseClickListener, MouseReleaseListener {
    private final List<Consumer<Boolean>> stateChangeListeners = new ArrayList<>();
    private boolean state;
    private boolean isPressed;
    private Color4f color = null;

    /**
     * Create a button with the given properties, starting disabled
     * @param text the displayed text
     */
    public SToggleButton(String text) {
        this(text, SButton.DEFAULT_MIN_WIDTH, SButton.DEFAULT_MIN_HEIGHT, false);
    }

    /**
     * Create a button with the given properties
     * @param text         the displayed text
     * @param minWidth     the minimal width of this button, which {@link io.github.ieperen3039.ngn.GUIMenu.LayoutManagers.SLayoutManager}s
     *                     should respect
     * @param minHeight    the minimal height of this button.
     * @param initialState the initial state of the button. If true, the button will be enabled
     */
    public SToggleButton(String text, int minWidth, int minHeight, boolean initialState) {
        super(text, NGFont.TextType.REGULAR, SFrameLookAndFeel.Alignment.CENTER_MIDDLE, minWidth, minHeight);
        parentComponentType = UIComponentType.BUTTON;
        this.state = initialState;
        this.isPressed = initialState;
    }

    /**
     * Create a button with the given properties, starting disabled
     * @param text the displayed text
     */
    public SToggleButton(String text, SComponentProperties properties) {
        super(text, properties);
        parentComponentType = UIComponentType.BUTTON;
        this.state = false;
        this.isPressed = false;
    }

    public SToggleButton(String text, SComponentProperties properties, Consumer<Boolean> action) {
        this(text, properties);
        addStateChangeListener(action);
    }

    public SToggleButton(String text, SComponentProperties properties, boolean initial, Consumer<Boolean> action) {
        this(text, properties, initial);
        addStateChangeListener(action);
    }

    public SToggleButton(String text, SComponentProperties properties, boolean initial) {
        this(text, properties);
        this.state = initial;
        this.isPressed = initial;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (getWidth() == 0 || getHeight() == 0) return;
        UIState state = isPressed ? ACTIVATED : (isHovered ? HOVERED : ENABLED);
        design.draw(BUTTON, state, screenPosition, getSize(), color);
        super.draw(design, screenPosition);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        isPressed = !state;
    }

    @Override
    public void onRelease(int button) {
        setActive(!state);
    }

    /**
     * @param action Upon change, this action is activated
     */
    public SToggleButton addStateChangeListener(Consumer<Boolean> action) {
        stateChangeListeners.add(action);
        return this;
    }

    public void setColor(Color4f color) {
        this.color = color;
    }

    public boolean isActive() {
        return state;
    }

    public void setActive(boolean state) {
        if (this.state != state) {
            this.state = state;
            this.isPressed = state;

            for (Consumer<Boolean> c : stateChangeListeners) {
                c.accept(state);
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getText() + ")";
    }

    public void toggle() {
        setActive(!state);
    }
}
