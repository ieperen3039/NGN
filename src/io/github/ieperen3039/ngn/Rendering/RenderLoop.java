package io.github.ieperen3039.ngn.Rendering;

import io.github.ieperen3039.ngn.Core.GenericThreadLoop;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Core.Main.ViewPort;
import io.github.ieperen3039.ngn.Core.ToolElement;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.NVGOverlay;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.TickTime;
import io.github.ieperen3039.ngn.Tools.TimeObserver;
import io.github.ieperen3039.ngn.Tools.Toolbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Repeatedly renders a frame of the main camera of the simulation given by {@link #init(Main)}
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class RenderLoop extends GenericThreadLoop implements ToolElement {
    public final TimeObserver timer;
    private final NVGOverlay overlay;
    public boolean accurateTiming = true;
    private final List<RenderBundle> renders;
    private Main root;

    /**
     * creates a new, paused gameloop
     * @param targetFPS the target frames per second
     */
    public RenderLoop(int targetFPS) {
        super("Renderloop", targetFPS);
        overlay = new NVGOverlay();
        renders = new ArrayList<>();

        timer = new TimeObserver((targetFPS / 4) + 1, true);
    }

    public void init(Main root) throws IOException {
        if (this.root != null) return;
        this.root = root;

        Settings settings = root.settings();

        accurateTiming = settings.ACCURATE_RENDER_TIMING;
        overlay.init(settings.ANTIALIAS_LEVEL);
        overlay.addHudItem((hud) -> {
            if (root.settings().PRINT_ROLL) {
                Logger.putOnlinePrint(hud::printRoll);
            }
        });
    }

    /**
     * generates a new render bundle, which allows adding rendering actions which are executed in order on the given
     * shader. There is no guarantee on execution order between shaders
     * @param shader the shader used, or null to use a basic Phong shading
     * @return a bundle that allows adding rendering options.
     */
    public RenderBundle renderSequence(ShaderProgram shader) {
        RenderBundle r = new RenderBundle(shader);
        renders.add(r);
        return r;
    }

    public void removeRenderSequence(RenderBundle bundle){
        renders.remove(bundle);
    }

    @Override
    protected void update(TickTime time) {
        Toolbox.checkGLError("Pre-loop");
        timer.startNewLoop();
        // cache value of accurateTiming for this loop
        boolean accurateTimingThisLoop = this.accurateTiming;

        if (accurateTimingThisLoop) timer.startTiming("loop init");

        GLFWWindow window = root.window();
        if (window.getWidth() == 0 || window.getHeight() == 0) {
            window.pollEvents();
            return;
        }

        // camera
        root.camera().updatePosition((float) time.getDeltaTimeSeconds()); // real-time deltatime
        
        // restore window state
        ViewPort viewPort = root.getViewPort();
        glViewport(viewPort.x(), viewPort.y(), viewPort.width(), viewPort.height());

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
        
        for (RenderBundle renderBundle : renders) {
            String identifier = renderBundle.shader.getClass().getSimpleName();
            if (accurateTimingThisLoop) timer.startTiming(identifier);

            renderBundle.draw();

            if (accurateTimingThisLoop) {
                glFinish();
                timer.endTiming(identifier);
            }
            Toolbox.checkGLError(identifier);
        }

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        if (accurateTimingThisLoop) timer.startTiming("GUI");
        overlay.draw(windowWidth, windowHeight, 10, 10, 12);

        if (accurateTimingThisLoop) {
            glFinish();
            timer.endTiming("GUI");
        }
        Toolbox.checkGLError(overlay.toString());

        timer.startTiming("GPU Update");
        // update window
        window.update();
        timer.endTiming("GPU Update");

        timer.startTiming("event handling");
        window.pollEvents();
        timer.endTiming("event handling");

        // loop clean
        Toolbox.checkGLError("Render loop");
        if (window.shouldClose()) initiateStop();
    }

    public void addHudItem(Consumer<NVGOverlay.Painter> draw) {
        overlay.addHudItem(draw);
    }

    @Override
    public void cleanup() {
        overlay.cleanup();
    }

    public class RenderBundle {
        private final ShaderProgram shader;
        private final List<Consumer<SGL>> targets;

        private RenderBundle(ShaderProgram shader) {
            this.shader = shader;
            this.targets = new ArrayList<>();
        }

        /**
         * appends the given consumer to the end of the render sequence
         * @return this
         */
        public RenderBundle add(Consumer<SGL> drawable) {
            targets.add(drawable);
            return this;
        }

        /**
         * executes the given drawables in order
         */
        public void draw() {
            shader.bind();
            {
                SGL gl = shader.getGL(root);
                // draw everything on screen
                for (Consumer<SGL> tgt : targets) {
                    tgt.accept(gl);
                }
            }
            shader.unbind();
        }
    }
}
