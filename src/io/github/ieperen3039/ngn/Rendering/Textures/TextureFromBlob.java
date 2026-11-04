package io.github.ieperen3039.ngn.Rendering.Textures;

import io.github.ieperen3039.ngn.Tools.Toolbox;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureFromBlob implements Texture {
    private final int id;

    private final int width;
    private final int height;
    private final int format;

    public TextureFromBlob(int width, int height, ByteBuffer blob) {
        this(width, height, width, GL_RGBA, blob);
    }

    /**
     * @param widthStep the number of pixels in one row of the blob, including padding
     * @param blob      the image data, top row first
     */
    public TextureFromBlob(int width, int height, int widthStep, int format, ByteBuffer blob) {
        this.width = width;
        this.height = height;
        this.format = format;

        assert blob.limit() >= width * height;

        // GL textures store the bottom row first. This also drops the padding between `width` and `widthStep`
        int bytesPerPixel = bytesPerPixel(format);
        ByteBuffer flipped = Toolbox.flipRows(blob, widthStep * bytesPerPixel, width * bytesPerPixel, height);

        // Create a new OpenGL texture
        this.id = glGenTextures();

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Specifies the alignment requirements for the start of each pixel row in memory.
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, flipped);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    private static int bytesPerPixel(int format) {
        return switch (format) {
            case GL_RED -> 1;
            case GL_RGB -> 3;
            case GL_RGBA -> 4;
            default -> throw new UnsupportedOperationException("Unsupported texture format " + Toolbox.asHex(format));
        };
    }

    @Override
    public void attach(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void cleanup() {
        glDeleteTextures(id);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
