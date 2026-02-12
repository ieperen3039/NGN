package io.github.ieperen3039.ngn.GUIMenu.Rendering;

import java.nio.ByteBuffer;

import org.joml.Vector2ic;

import io.github.ieperen3039.ngn.Version;
import io.github.ieperen3039.ngn.Core.RenderManager;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;

/**
 * a stateless mapping from abstract descriptions to drawings in NanoVG
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public interface SFrameLookAndFeel {
    enum Alignment {
        LEFT_TOP, CENTER_TOP, RIGHT_TOP,
        LEFT_MIDDLE, CENTER_MIDDLE, RIGHT_MIDDLE
    }
    
    enum UIState {
       ENABLED, // default
       DISABLED,
       HOVERED,
       ACTIVATED, // pressed/held
   }

    enum UIComponentType {
        /** a clickable rectangle */
        BUTTON,
        /** a button with an image on it */
        ICON_BUTTON,
        /** The part of a dropdown menu that is clicked to open the dropdown, and the options that are shown. */
        DROP_DOWN_HEAD, DROP_DOWN_OPTION_FIELD, 
        /** The background of some elements. */
        SCROLL_BAR_BACKGROUND,
        /** A visible rectangle, for grouping ui elements together*/
        PANEL,
        /** Elements of a progress bar */
        PROGRESS_BAR, PROGRESS_BAR_FILL,
        /** the bar on top of a frame carrying the title */
        FRAME_HEADER,
        /** An area with text that hints the user that the text can be changed. */
        INPUT_FIELD,
        /** the drag bar element of a scrollbar */
        SCROLL_BAR_DRAG_ELEMENT,
    }

    void init(RenderManager root);

    /**
     * draw with default color
     * @see #draw(UIComponentType, UIState, Vector2ic, Vector2ic, Color4f)
     */
    default void draw(UIComponentType type, UIState state, Vector2ic pos, Vector2ic size) {
        draw(type, state, pos, size, null);
    }

    /**
     * Draw the given element on the given position
     * @param pos   the position of the upper left corner of this element in pixels
     * @param size  the (width, height) of the button in pixels
     * @param color an overriding color, or null to use the default color for this type and state
     */
    void draw(UIComponentType type, UIState state, Vector2ic pos, Vector2ic size, Color4f color);

    void drawText(UIComponentType parentType, Vector2ic pos, Vector2ic size, String text, NGFont.TextType type, Alignment align);

    /**
     * creates an image, and returns an id to reference the image later in {@link SFrameLookAndFeel#drawImage}
     * @return an image id
     */
    int createImage(ByteBuffer buffer, int width, int height);

    /**
     * draws an image previously created using {@link SFrameLookAndFeel#createImage}.
     * the given imagewidth and imageheight must be the size of the original image
     */
    void drawImage(Vector2ic pos, int imageWidth, int imageHeight, int imageId);

    /**
     * @return the used painter instance
     */
    NVGOverlay.Painter getPainter();

    /**
     * sets the LF to draw with the specified painter
     * @param painter a new, fresh Painter instance
     */
    void setPainter(NVGOverlay.Painter painter);

    /**
     * @param text     any string
     * @param textType the type displayed
     * @return the width of the text displayed in pixels
     */
    int getTextWidth(String text, NGFont.TextType textType);

    void cleanup();

    Version getVersionNumber();
}
