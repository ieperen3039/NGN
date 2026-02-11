package io.github.ieperen3039.ngn.GUIMenu.Rendering;

import io.github.ieperen3039.ngn.GenericThreadLoop;
import io.github.ieperen3039.ngn.Main;
import io.github.ieperen3039.ngn.RenderManager;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.SimpleUIManager;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.TickTime;
import io.github.ieperen3039.ngn.Tools.TimeObserver;
import io.github.ieperen3039.ngn.Tools.Toolbox;

import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;

/**
 * A renderloop specialized for rendering just a UI, requiring no {@link Main} class
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class UIRenderer extends GenericThreadLoop {
    public final TimeObserver timer;
    private final NVGOverlay overlay;
    private final SimpleUIManager uiManager;
    public boolean accurateTiming = true;
    private GLFWWindow window;

    /**
     * creates a new, paused render loop specifically for UI
     * @param targetFPS the target frames per second
     */
    public UIRenderer(int targetFPS) {
        super("UI Renderloop", targetFPS);
        overlay = new NVGOverlay();
        uiManager = new SimpleUIManager();
        timer = new TimeObserver((targetFPS / 4) + 1, true);
    }

    public void init(RenderManager root, GLFWWindow window, Settings settings) throws IOException {
        this.window = window;
        uiManager.init(window, root);

        accurateTiming = settings.ACCURATE_RENDER_TIMING;
        overlay.init(settings.ANTIALIAS_LEVEL);
        overlay.addHudItem(uiManager::draw);
        overlay.addHudItem((hud) -> {
            if (settings.PRINT_ROLL) {
                Logger.putOnlinePrint(hud::printRoll);
            }
        });
    }

    @Override
    protected void update(TickTime deltaTime) {
        Toolbox.checkGLError("Pre-loop");
        timer.startNewLoop();
        // cache value of accurateTiming for this loop
        boolean accurateTimingThisLoop = this.accurateTiming;

        if (accurateTimingThisLoop) timer.startTiming("loop init");

        if (window.getWidth() == 0 || window.getHeight() == 0) {
            window.pollEvents();
            return;
        }

        // restore window state
        glViewport(0, 0, window.getWidth(), window.getHeight());

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(1, 1, 1, 1); // white

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Toolbox.checkGLError(window.toString());

        if (accurateTimingThisLoop) {
            glFinish();
            timer.endTiming("loop init");
        }

        if (accurateTimingThisLoop) timer.startTiming("GUI");
        overlay.draw(window.getWidth(), window.getHeight(), 10, 10, 12);

        if (accurateTimingThisLoop) {
            glFinish();
            timer.endTiming("GUI");
        }
        Toolbox.checkGLError(overlay.toString());

        // update window
        timer.startTiming("GPU Update");
        window.update();
        timer.endTiming("GPU Update");

        timer.startTiming("event handling");
        window.pollEvents();
        timer.endTiming("event handling");

        // loop clean
        Toolbox.checkGLError(window.toString());
        if (window.shouldClose()) initiateStop();
    }

    @Override
    public void cleanup() {
        overlay.cleanup();
    }

    public UIManager getUiManager() {
        return uiManager;
    }
}
