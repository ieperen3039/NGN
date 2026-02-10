package NG.GUIMenu.FrameManagers;

import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Rendering.NVGOverlay;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.InputHandling.KeyTypeListener;
import NG.InputHandling.MouseClickListener;
import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseReleaseListener;

/**
 * A class that manages a UI inside of a GLFW window.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface UIManager
        extends KeyTypeListener, MouseClickListener, MouseReleaseListener, MouseMoveListener {

    /**
     * sets the given component to cover the entire screen
     * @param container
     */
    void setMainPanel(SComponent container);

    /**
     * draws the elements of this HUD
     * @param painter
     */
    void draw(NVGOverlay.Painter painter);

    /**
     * sets the appearance of the frames on the next drawing cycles to the given object. This overrides any previous
     * setting.
     * @param lookAndFeel any look-and-feel provider.
     */
    void setLookAndFeel(SFrameLookAndFeel lookAndFeel);

    SFrameLookAndFeel getLookAndFeel();

    @Override
    default void onClick(int button, int xRel, int yRel) {
        checkMouseClick(button, xRel, yRel);
    }

    boolean checkMouseClick(int button, int xSc, int ySc);

    SComponent getComponentAt(int xSc, int ySc);

    /**
     * @param xSc screen x coordinate in pixels from left
     * @param ySc screen y coordinate in pixels from top
     * @return the SFrame covering the given coordinate
     */
    boolean covers(int xSc, int ySc);

    /**
     * The next click action is redirected to the given listener instead of being processed by the frames. This is reset
     * after such click occurs.
     * @param listener a listener that receives the button and screen positions of the next click exactly once.
     */
    void setModalListener(SComponent listener);

    void setTextListener(KeyTypeListener listener);

    void onScroll(float value);
}
