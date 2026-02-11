package io.github.ieperen3039.ngn;

import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.InputHandling.KeyControl;
import io.github.ieperen3039.ngn.InputHandling.MouseTools.MouseToolCallbacks;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.TickTime;

/**
 * A collection of references to any major element of the simulation.
 * @author Geert van Ieperen. Created on 16-9-2018.
 */
public interface Main extends RenderManager {

    TickTime timer();

    Camera camera();

    Settings settings();

    GLFWWindow window();

    MouseToolCallbacks inputHandling();

    UIManager gui();

    KeyControl keyControl();

    Version getVersionNumber();

    default ViewPort getViewPort() {
        return new ViewPort(0, 0, window().getWidth(), window().getHeight());
    }
    
    record ViewPort(int x, int y, int width, int height){};
}
