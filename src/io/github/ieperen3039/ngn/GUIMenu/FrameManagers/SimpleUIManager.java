package io.github.ieperen3039.ngn.GUIMenu.FrameManagers;

import java.util.Optional;

import org.joml.Vector2i;

import io.github.ieperen3039.ngn.RenderManager;
import io.github.ieperen3039.ngn.GUIMenu.Components.SComponent;
import io.github.ieperen3039.ngn.GUIMenu.Components.SFiller;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.BaseLF;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.NVGOverlay;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.InputHandling.KeyTypeListener;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.InputHandling.MouseDragListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;
import io.github.ieperen3039.ngn.InputHandling.MouseScrollListener;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;

public class SimpleUIManager implements UIManager {
    protected GLFWWindow window;
    private MouseDragListener dragListener = null;
    private MouseReleaseListener releaseListener = null;
    private KeyTypeListener typeListener = null;

    protected SComponent mainPanel;
    protected SComponent modalComponent;
    protected SComponent hoveredComponent;

    private SFrameLookAndFeel lookAndFeel;

    public SimpleUIManager() {
        lookAndFeel = new BaseLF();
        mainPanel = new SFiller(0, 0);
    }

    public void init(GLFWWindow window, RenderManager renderer) {
        this.window = window;

        lookAndFeel.init(renderer);
        mainPanel.setSize(window.getWidth(), window.getHeight());
    }

    @Override
    public void draw(NVGOverlay.Painter painter) {
        if (window.getWidth() != mainPanel.getWidth() || window.getHeight() != mainPanel.getHeight()) {
            mainPanel.setSize(window.getWidth(), window.getHeight());
        }

        lookAndFeel.setPainter(painter);
        mainPanel.validateLayout();
        mainPanel.draw(lookAndFeel, new Vector2i(0, 0));

        drawInternal(lookAndFeel);

        if (modalComponentIsActive()) {
            modalComponent.validateLayout();
            modalComponent.draw(lookAndFeel, modalComponent.getScreenPosition());
        }
    }

    protected void drawInternal(SFrameLookAndFeel lookAndFeel) {
    }

    @Override
    public boolean checkMouseClick(int button, final int xSc, final int ySc) {
        if (modalComponentIsActive()){
            SComponent modalSubComponent = modalComponent.getComponentAt(xSc, ySc);
            processClick(button, (modalSubComponent == null) ? modalComponent : modalSubComponent, xSc, ySc);
            return true;
        }

        SComponent component = getComponentAt(xSc, ySc);
        if (component == null) {
            return false;
        }

        processClick(button, component, xSc, ySc);

        Optional<SComponent> parent = component.getParent();
        while (parent.isPresent()) {
            component = parent.get();
            parent = component.getParent();
        }

        processParentClick(button, component);

        return true;
    }

    protected void processClick(int button, SComponent component, int xSc, int ySc) {
        // click listener
        SComponent target = component;
        do {
            if (target instanceof MouseClickListener) {
                MouseClickListener cl = (MouseClickListener) target;
                // by def. of MouseRelativeClickListener, give relative coordinates
                Vector2i pos = component.getScreenPosition();
                cl.onClick(button, xSc - pos.x, ySc - pos.y);
            }

            if (target instanceof MouseDragListener) {
                dragListener = (MouseDragListener) target;
            }

            if (target instanceof MouseReleaseListener) {
                releaseListener = (MouseReleaseListener) target;
            }

            target = target.getParent().orElse(null);
        } while (target != null);
    }

    protected void processParentClick(int button, SComponent component) {
    }

    @Override
    public void setLookAndFeel(SFrameLookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    @Override
    public SFrameLookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    @Override
    public void setModalListener(SComponent listener) {
        modalComponent = listener;
    }

    @Override
    public void setTextListener(KeyTypeListener listener) {
        typeListener = listener;
    }

    @Override
    public void keyTyped(char letter) {
        if (typeListener == null)
            return;
        typeListener.keyTyped(letter);
    }

    @Override
    public void onRelease(int button) {
        dragListener = null;
        if (releaseListener == null)
            return;
        releaseListener.onRelease(button);
        releaseListener = null;
    }

    public void onMouseMove(int xDelta, int yDelta, float xPos, float yPos) {
        if (hoveredComponent != null)
            hoveredComponent.setHovered(false);

        hoveredComponent = getComponentAt((int) xPos, (int) yPos);

        if (hoveredComponent != null)
            hoveredComponent.setHovered(true);

        if (dragListener != null) {
            dragListener.onMouseDrag(xDelta, yDelta, xPos, yPos);
        }
    }

    @Override
    public void onScroll(float value) {
        SComponent target = hoveredComponent;
        while (target != null) {
            if (target instanceof MouseScrollListener) {
                MouseScrollListener listener = (MouseScrollListener) target;
                listener.onScroll(value);
                break;
            }

            target = target.getParent().orElse(null);
        }
    }

    @Override
    public void setMainPanel(SComponent container) {
        this.mainPanel = container;

        if (window != null) {
            container.setSize(window.getWidth(), window.getHeight());
        }
    }

    @Override
    public SComponent getComponentAt(int xSc, int ySc) {
        if (modalComponentIsActive() && modalComponent.contains(xSc, ySc)) {
            return modalComponent.getComponentAt(xSc, ySc);
        }

        return mainPanel.getComponentAt(xSc, ySc);
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        if (modalComponentIsActive() && modalComponent.contains(xSc, ySc)) {
            return true;
        }

        SComponent c = mainPanel.getComponentAt(xSc, ySc);
        return c != null && c.isVisible();
    }

    private boolean modalComponentIsActive() {
        return modalComponent != null && modalComponent.isVisible();
    }
}
