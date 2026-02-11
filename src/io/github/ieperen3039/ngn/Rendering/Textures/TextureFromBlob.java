package io.github.ieperen3039.ngn.Rendering.Textures;

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

    public TextureFromBlob(int width, int height, int widthStep, int format, ByteBuffer blob) {
        this.width = width;
        this.height = height;
        this.format = format;

        assert blob.limit() >= width * height;

        // Create a new OpenGL texture
        this.id = glGenTextures();

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Specifies the alignment requirements for the start of each pixel row in memory.
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        // Define the number of pixels in a row. pixels between `witdh` and `widthStep` are skipped when reading
        glPixelStorei(GL_UNPACK_ROW_LENGTH, widthStep);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, blob);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    @Override
    public void bind(int sampler) {
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

    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean isSingleChannel() {
        return switch (format) {
            case GL_RED, GL_DEPTH_COMPONENT -> true;
            default -> false;
        };
    }
}
