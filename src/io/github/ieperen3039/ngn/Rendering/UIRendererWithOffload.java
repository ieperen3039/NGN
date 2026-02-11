package io.github.ieperen3039.ngn.Rendering;

import io.github.ieperen3039.ngn.GenericThreadLoop;
import io.github.ieperen3039.ngn.Main;
import io.github.ieperen3039.ngn.RenderManager;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.SimpleUIManager;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.NVGOverlay;
import io.github.ieperen3039.ngn.InputHandling.MouseTools.UIInputCallbacks;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.TickTime;
import io.github.ieperen3039.ngn.Tools.Toolbox;

import java.io.IOException;
import java.util.AbstractQueue;

import static org.lwjgl.opengl.GL11.*;

/**
 * A renderloop specialized for rendering just a UI, requiring no {@link Main} class
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class UIRendererWithOffload extends GenericThreadLoop {
    private final NVGOverlay overlay;
    private final SimpleUIManager uiManager;
    private GLFWWindow window;
    private AbstractQueue<Runnable> targetQueue;
    private Runnable onStop;
    private UIInputCallbacks mouseHandler;

    /**
     * creates a new, paused render loop specifically for UI
     * @param targetFPS the target frames per second
     */
    public UIRendererWithOffload(int targetFPS, AbstractQueue<Runnable> targetQueue) {
        super("UI Renderloop", targetFPS);
        this.targetQueue = targetQueue;
        overlay = new NVGOverlay();
        uiManager = new SimpleUIManager();
        mouseHandler = new UIInputCallbacks();
    }

    public void init(RenderManager root, GLFWWindow window, Settings settings) throws IOException {
        this.window = window;
        uiManager.init(window, root);
        mouseHandler.init(window, uiManager);

        overlay.init(settings.ANTIALIAS_LEVEL);
        overlay.addHudItem(uiManager::draw);
        overlay.addHudItem((hud) -> {
            if (settings.PRINT_ROLL) {
                Logger.putOnlinePrint(hud::printRoll);
            }
        });
    }

    @Override
    protected void update(TickTime timer) {
        targetQueue.offer(() -> {
            if (window.getWidth() == 0 || window.getHeight() == 0) {
                window.pollEvents();
                return;
            }

            // restore window state
            glViewport(0, 0, window.getWidth(), window.getHeight());
            glClearColor(1, 1, 1, 1); // white
            glClear(GL_COLOR_BUFFER_BIT);
            overlay.draw(window.getWidth(), window.getHeight(), 10, 10, 12);

            Toolbox.checkGLError(overlay.toString());
            
            // update window
            window.update();
            window.pollEvents();
            Toolbox.checkGLError(window.toString());
        });

        if (window.shouldClose() && onStop != null) {
            onStop.run();
        }
    }

    public void setOnStopCallback(Runnable action) {
        this.onStop = action;
        
    }

    @Override
    public void cleanup() {
        overlay.cleanup();
        mouseHandler.cleanup();
    }

    public UIManager getUiManager() {
        return uiManager;
    }
}
