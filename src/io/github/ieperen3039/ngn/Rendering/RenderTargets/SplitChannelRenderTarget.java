package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

import io.github.ieperen3039.ngn.Rendering.Textures.DerivedTexture;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;
import io.github.ieperen3039.ngn.Tools.Toolbox;

public class SplitChannelRenderTarget extends RenderTarget {
    private final int[] colorBuffers;

    private final int targetTextureWidth;
    private final int targetTextureHeight;

    public SplitChannelRenderTarget(int nrOfChannels, int targetTextureWidth, int targetTextureHeight) {
        super(glGenFramebuffers());

        this.colorBuffers = new int[nrOfChannels];
        this.targetTextureWidth = targetTextureWidth;
        this.targetTextureHeight = targetTextureHeight;

        bind();
        int[] drawBuffers = new int[colorBuffers.length];

        glGenTextures(colorBuffers);
        for (int i = 0; i < colorBuffers.length; i++) {
            // each channel gets its own single-channel texture
            drawBuffers[i] = GL_COLOR_ATTACHMENT0 + i;

            glBindTexture(GL_TEXTURE_2D, colorBuffers[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, targetTextureWidth, targetTextureHeight, 0, GL_RED, GL_UNSIGNED_BYTE, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_FRAMEBUFFER, drawBuffers[i], GL_TEXTURE_2D, colorBuffers[i], 0);
        }

        // Set the list of draw buffers.
        glDrawBuffers(drawBuffers);

        Toolbox.checkGLError(this.toString());
        
        glBindTexture(GL_TEXTURE_2D, 0);
        unbind();
    }

    public ByteBuffer getRenderBuffer(int channelIndex) {
        bind();
        ByteBuffer buffer = getRenderBufferAlreadyBound(channelIndex);
        unbind();
        return buffer;
    }

    // remember to call glFinish if you didnt unbind since rendering
    public ByteBuffer getRenderBufferAlreadyBound(int channelIndex) {
        glBindTexture(GL_TEXTURE_2D, colorBuffers[channelIndex]);
        ByteBuffer buffer = ByteBuffer.allocateDirect(targetTextureWidth * targetTextureHeight);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        Toolbox.checkGLError("getTexture");
        return buffer;
    }

    public Texture getRenderBufferTexture(int channelIndex) {
        return new DerivedTexture(colorBuffers[channelIndex], targetTextureWidth, targetTextureHeight);
    }

    @Override
    public void cleanup() {
        for (int renderBuffer : colorBuffers) {
            glDeleteRenderbuffers(renderBuffer);
        }
        super.cleanup();
    }
    
}
