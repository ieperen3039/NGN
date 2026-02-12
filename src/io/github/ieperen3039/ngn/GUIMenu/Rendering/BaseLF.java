package io.github.ieperen3039.ngn.GUIMenu.Rendering;

import io.github.ieperen3039.ngn.Core.RenderManager;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Version;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont.LUCIDA_CONSOLE;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.NVGOverlay.Alignment.*;

/**
 * Little more than the absolute basic appearance of a GUI
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public class BaseLF implements SFrameLookAndFeel {
    private static final int DEFAULT_INDENT = 1;
    private static final int BUTTON_INDENT = 2;
    private static final int STROKE_WIDTH = 1;
    private static final int TEXT_SIZE_REGULAR = 12;
    private static final int TEXT_SIZE_LARGE = 16;

    private static final NGFont FONT = LUCIDA_CONSOLE;

    private static final Color4f TEXT_COLOR = Color4f.BLACK;
    private static final Color4f PANEL_COLOR = Color4f.WHITE;
    private static final Color4f STROKE_COLOR = Color4f.BLACK;
    private static final Color4f BUTTON_COLOR = Color4f.LIGHT_GREY;
    private static final Color4f SELECTION_COLOR = BUTTON_COLOR.darken(0.1f);
    private static final Color4f INPUT_FIELD_COLOR = Color4f.LIGHT_GREY;

    private NVGOverlay.Painter hud;

    @Override
    public void init(RenderManager root) {
    }

    @Override
    public NVGOverlay.Painter getPainter() {
        return hud;
    }

    @Override
    public void setPainter(NVGOverlay.Painter painter) {
        this.hud = painter;
        painter.setFillColor(PANEL_COLOR);
        painter.setStroke(STROKE_COLOR, STROKE_WIDTH);
    }

    @Override
    public int getTextWidth(String text, NGFont.TextType textType) {
        int actualSize = TEXT_SIZE_REGULAR;

        if (textType == NGFont.TextType.TITLE || textType == NGFont.TextType.ACCENT) {
            actualSize = TEXT_SIZE_LARGE;
        }

        return hud.getTextWidth(text, actualSize, FONT);
    }

    @Override
    public void draw(UIComponent type, Vector2ic pos, Vector2ic dim, Color4f color) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        assert width > 0 && height > 0 : String.format("Non-positive dimensions: height = %d, width = %d", height, width);

        switch (type) {
            case SCROLL_BAR_BACKGROUND:
                break;

            case BUTTON_ACTIVE:
            case BUTTON_INACTIVE:
            case SCROLL_BAR_DRAG_ELEMENT:
                Color4f thisColor = color == null ? BUTTON_COLOR : color;
                drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor);
                break;

            case BUTTON_HOVERED:
                Color4f thisColor1 = color == null ? SELECTION_COLOR.intensify(0.1f) : color;
                drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor1);
                break;

            case BUTTON_PRESSED:
                Color4f thisColor2 = color == null ? BUTTON_COLOR.darken(0.5f) : color;
                drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor2);
                break;

            case INPUT_FIELD:
                Color4f thisColor3 = color == null ? INPUT_FIELD_COLOR : color;
                drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor3);
                break;

            case SELECTION:
                hud.setStroke(STROKE_COLOR, 0);
                Color4f thisColor4 = color == null ? SELECTION_COLOR : color;
                drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor4);
                break;

            case DROP_DOWN_HEAD_CLOSED:
            case DROP_DOWN_HEAD_OPEN:
            case DROP_DOWN_OPTION_FIELD:
            case PANEL:
            case FRAME_HEADER:
            default:
                drawRectangle(x, y, width, height, DEFAULT_INDENT, color == null ? Color4f.WHITE : color);
        }
    }

    private void drawRectangle(int x, int y, int width, int height, int indent, Color4f color) {
        int xMax = x + width;
        int yMax = y + height;

        hud.polygon(color, STROKE_COLOR, STROKE_WIDTH,
                new Vector2i(x + indent, y),
                new Vector2i(xMax - indent, y),
                new Vector2i(xMax, y + indent),
                new Vector2i(xMax, yMax - indent),
                new Vector2i(xMax - indent, yMax),
                new Vector2i(x + indent, yMax),
                new Vector2i(x, yMax - indent),
                new Vector2i(x, y + indent)
        );
    }

    @Override
    public void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFont.TextType type, Alignment align
    ) {
        if (text == null || text.isEmpty()) return;

        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        int actualSize = TEXT_SIZE_REGULAR;
        Color4f textColor = TEXT_COLOR;
        NGFont font = FONT;

        switch (type) {
            case TITLE:
            case ACCENT:
                actualSize = TEXT_SIZE_LARGE;
                break;
            case RED:
                textColor = new Color4f(0.8f, 0.1f, 0.1f);
                break;
            default:
                break;
        }

        switch (align) {
            case LEFT_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_LEFT), textColor, text, width
                );
                break;
            case LEFT_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP, ALIGN_LEFT), textColor, text, width
                );
                break;
            case CENTER_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.noneOf(NVGOverlay.Alignment.class), textColor, text, width
                );
                break;
            case CENTER_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP), textColor, text, width
                );
                break;
            case RIGHT_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_RIGHT), textColor, text, width
                );
                break;
            case RIGHT_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP, ALIGN_RIGHT), textColor, text, width
                );
                break;
            default:
                throw new IllegalArgumentException(align.toString());
        }
    }

    @Override
    public int createImage(ByteBuffer buffer, int width, int height) {
        return hud.createImageFromBuffer(buffer, width, height);
    }

    @Override
    public void drawImage(Vector2ic pos, int imageWidth, int imageHeight, int imageId) {
        hud.drawImage(imageId, pos.x(), pos.y(), imageWidth, imageHeight, 0, 1.0f);
    }

    @Override
    public void cleanup() {
        hud = null;
    }

    @Override
    public Version getVersionNumber() {
        return new Version(0, 0);
    }
}
