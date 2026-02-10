package NG.Core;

import NG.Camera.Camera;
import NG.GUIMenu.FrameManagers.UIManager;
import NG.InputHandling.KeyControl;
import NG.InputHandling.MouseTools.MouseToolCallbacks;
import NG.Rendering.GLFWWindow;
import NG.Settings.Settings;
import NG.Tools.TickTime;

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
