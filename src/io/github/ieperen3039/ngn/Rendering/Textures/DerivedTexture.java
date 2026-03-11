package io.github.ieperen3039.ngn.Rendering.Textures;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.glActiveTexture;

// wrapper for an existing texture
public class DerivedTexture implements Texture {
    private final int id;

    private final int width;
    private final int height;

    public DerivedTexture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    @Override
    public void attach(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void cleanup() {
        // do not clean up, because we do not own it
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
