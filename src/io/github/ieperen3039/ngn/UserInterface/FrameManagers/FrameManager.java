package io.github.ieperen3039.ngn.UserInterface.FrameManagers;

import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.UserInterface.Components.SFrame;

public interface FrameManager extends UIManager {
    void addFrame(SFrame frame);

    void addFrame(SFrame frame, int x, int y);

    void addFrameCenter(SFrame frame, GLFWWindow window);

    void focus(SFrame frame);

    void clear();

    SFrame getFrame(int xSc, int ySc);
}
