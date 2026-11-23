package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.UserInterface.Components.SComponent;
import io.github.ieperen3039.ngn.UserInterface.FrameManagers.UIManager;

/**
 * Picks a {@link UITarget} when the user interface covers the position
 * @author Geert van Ieperen created on 2-11-2026.
 */
public class UISelector implements MouseSelector {
    private final UIManager gui;

    public UISelector(UIManager gui) {
        this.gui = gui;
    }

    @Override
    public MouseTarget pick(int xSc, int ySc) {
        SComponent component = gui.getComponentAt(xSc, ySc);
        if (component == null) return null;
        return new UITarget(component);
    }
}
