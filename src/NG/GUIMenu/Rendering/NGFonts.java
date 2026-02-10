package NG.GUIMenu.Rendering;

import NG.AssetHandling.Resource;
import NG.Tools.Logger;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public enum NGFonts {
    LUCIDA_CONSOLE("fonts","LucidaConsole", "lucon.ttf");

    public final String name;
    private ByteBuffer byteFormat;
    private Font awtFormat;

    public enum TextType {
        TITLE, ACCENT, REGULAR, FANCY, TOOLTIP, RED, FLOATING
    }

    NGFonts(String... path) {
        Resource.Path directory = Resource.Path.get(path);
        this.name = toString().toLowerCase().replace("_", " ");

        try {
            InputStream fontStream = directory.asStream();
            byte[] bytes = fontStream.readAllBytes();
            byteFormat = BufferUtils.createByteBuffer(bytes.length + 1);
            byteFormat.put(bytes);
            byteFormat.flip();
            awtFormat = Font.createFont(Font.TRUETYPE_FONT, directory.asStream());

        } catch (IOException | FontFormatException e) {
            Logger.ERROR.print("Error loading font " + name + ": " + e);
        }
    }

    ByteBuffer asByteBuffer() {
        return byteFormat;
    }

    public Font asAWTFont(float size) {
        return awtFormat.deriveFont(size);
    }
}
