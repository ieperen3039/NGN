package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import io.github.ieperen3039.ngn.Rendering.Textures.Texture;

public abstract class TextureRenderTarget extends RenderTarget implements Texture {
    protected final int textureId;

    private int textureWidth;
    private int textureHeight;

    public TextureRenderTarget(int targetTextureWidth, int targetTextureHeight) {
        super(glGenFramebuffers());
        this.textureId = glGenTextures();
        this.textureWidth = targetTextureWidth;
        this.textureHeight = targetTextureHeight;
    }

    public void check(){}

    @Override
    public void attach(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    @Override
    public void cleanup() {
        glDeleteTextures(textureId);
    }

    @Override
    public int getWidth() {
        return textureWidth;
    }

    @Override
    public int getHeight() {
        return textureHeight;
    }
}
