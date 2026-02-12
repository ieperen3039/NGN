package io.github.ieperen3039.ngn.GUIMenu.Rendering;

import io.github.ieperen3039.ngn.Core.RenderManager;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont.TextType;
import io.github.ieperen3039.ngn.Version;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import static io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont.LUCIDA_CONSOLE;
import static io.github.ieperen3039.ngn.GUIMenu.Rendering.NVGOverlay.Alignment.*;

/**
 * Little more than the absolute basic appearance of a GUI
 * 
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
    private static final Color4f TEXT_EMPHASIS_COLOR = new Color4f(0.8f, 0.1f, 0.1f);
    private static final Color4f BACKGROUND_COLOR = Color4f.WHITE;
    private static final Color4f STROKE_COLOR = Color4f.BLACK;
    private static final Color4f BUTTON_COLOR = Color4f.LIGHT_GREY;
    private static final Color4f SELECTION_COLOR = BUTTON_COLOR.intensify(0.1f);
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
        painter.setFillColor(BACKGROUND_COLOR);
        painter.setStroke(STROKE_COLOR, STROKE_WIDTH);
    }

    @Override
    public int getTextWidth(String text, NGFont.TextType textType) {
        int actualSize = TEXT_SIZE_REGULAR;

        if (textType == NGFont.TextType.TITLE) {
            actualSize = TEXT_SIZE_LARGE;
        }

        return hud.getTextWidth(text, actualSize, FONT);
    }

    @Override
    public void draw(UIComponentType type, UIState state, Vector2ic pos, Vector2ic size, Color4f color) {
        int x = pos.x();
        int y = pos.y();
        int width = size.x();
        int height = size.y();
        assert width > 0 && height > 0
                : String.format("Non-positive dimensions: height = %d, width = %d", height, width);

        switch (type) {
            case SCROLL_BAR_BACKGROUND:
                break;

            case BUTTON:
            case SCROLL_BAR_DRAG_ELEMENT:
            case ICON_BUTTON:
                switch (state) {
                    default:
                    case ENABLED:
                        Color4f thisColor = color == null ? BUTTON_COLOR : color;
                        drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor);
                        break;
                    case DISABLED:
                        break;
                    case HOVERED:
                        Color4f thisColor1 = color == null ? SELECTION_COLOR : color;
                        drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor1);
                        break;
                    case ACTIVATED:
                        Color4f thisColor2 = color == null ? BUTTON_COLOR.darken(0.5f) : color;
                        drawRectangle(x, y, width, height, BUTTON_INDENT, thisColor2);
                        break;
                }

            case INPUT_FIELD:
                Color4f thisColor3 = color == null ? INPUT_FIELD_COLOR : color;
                drawRectangle(x, y, width, height, DEFAULT_INDENT, thisColor3);
                break;

            case DROP_DOWN_HEAD:
            case DROP_DOWN_OPTION_FIELD:
                switch (state) {
                    default:
                    case ENABLED:
                        drawRectangle(x, y, width, height, DEFAULT_INDENT, color == null ? BACKGROUND_COLOR : color);
                        break;
                    case DISABLED:
                        drawRectangle(x, y, width, height, DEFAULT_INDENT, Color4f.LIGHT_GREY);
                    case ACTIVATED:
                    case HOVERED:
                        drawRectangle(x, y, width, height, DEFAULT_INDENT, color == null ? BACKGROUND_COLOR.darken(0.1f)  : color);
                        break;
                }
                break;
            case PANEL:
            case FRAME_HEADER:
            default:
                drawRectangle(x, y, width, height, DEFAULT_INDENT, color == null ? BACKGROUND_COLOR : color);
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
                new Vector2i(x, y + indent));
    }

    @Override
    public void drawText(UIComponentType parentType, Vector2ic pos, Vector2ic size, String text, TextType type,
            Alignment align) {
        if (text == null || text.isEmpty())
            return;

        int x = pos.x();
        int y = pos.y();
        int width = size.x();
        int height = size.y();
        int actualSize = TEXT_SIZE_REGULAR;
        Color4f textColor = TEXT_COLOR;
        NGFont font = FONT;

        switch (type) {
            case TITLE:
                actualSize = TEXT_SIZE_LARGE;
                break;
            case EMPHASIS:
                textColor = TEXT_EMPHASIS_COLOR;
                break;
            default:
                break;
        }

        switch (align) {
            case LEFT_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_LEFT), textColor, text, width);
                break;
            case LEFT_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP, ALIGN_LEFT), textColor, text, width);
                break;
            case CENTER_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.noneOf(NVGOverlay.Alignment.class), textColor, text, width);
                break;
            case CENTER_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP), textColor, text, width);
                break;
            case RIGHT_MIDDLE:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_RIGHT), textColor, text, width);
                break;
            case RIGHT_TOP:
                hud.text(x, y, actualSize,
                        font, EnumSet.of(ALIGN_TOP, ALIGN_RIGHT), textColor, text, width);
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
