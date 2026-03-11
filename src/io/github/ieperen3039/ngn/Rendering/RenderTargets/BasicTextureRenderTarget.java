package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.*;

import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderException;
import io.github.ieperen3039.ngn.Tools.Toolbox;

// 
public class BasicTextureRenderTarget extends TextureRenderTarget {
    public BasicTextureRenderTarget(int targetTextureWidth, int targetTextureHeight) {
        super(targetTextureWidth, targetTextureHeight);
        
        bind();

        // Attach a depth buffer for rendering only
        int depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, targetTextureWidth, targetTextureHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        // Attach a color buffer as the texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, targetTextureWidth, targetTextureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        Toolbox.checkGLError(this.toString());
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new ShaderException(this.toString() + " could not init FrameBuffer : error " + Toolbox.asHex(status));
        }

        unbind();
    }
}
