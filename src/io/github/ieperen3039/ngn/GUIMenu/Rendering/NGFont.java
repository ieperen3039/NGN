package io.github.ieperen3039.ngn.GUIMenu.Rendering;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Tools.Logger;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public class NGFont {
    // make sure ALL_FONTS is above all default fonts
    public static List<NGFont> ALL_FONTS = new ArrayList<>();
    public static NGFont LUCIDA_CONSOLE = new NGFont("ngn/fonts/LucidaConsole/lucon.ttf");

    public final String name;
    private ByteBuffer byteFormat;
    private Font awtFormat;

    public enum TextType {
        REGULAR, TITLE, EMPHASIS
    }

    public NGFont(String... path) {
        Resource.Path directory = Resource.Path.get(path);
        String name = path[path.length - 1];
        this.name = name.substring(name.lastIndexOf('/') + 1);

        try (InputStream fontStream = directory.asStream()) {
            byte[] bytes = fontStream.readAllBytes();
            byteFormat = BufferUtils.createByteBuffer(bytes.length + 1);
            byteFormat.put(bytes);
            byteFormat.flip();
            awtFormat = Font.createFont(Font.TRUETYPE_FONT, directory.asStream());

            ALL_FONTS.add(this);

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
