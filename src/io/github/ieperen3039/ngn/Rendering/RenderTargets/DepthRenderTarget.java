package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.*;

import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderException;
import io.github.ieperen3039.ngn.Tools.Toolbox;

public class DepthRenderTarget extends TextureRenderTarget {
    public DepthRenderTarget(int width, int height) {
        super(width, height);

        bind();

        int depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        Toolbox.checkGLError(this.toString());
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new ShaderException(this.toString() + " could not init FrameBuffer : error " + Toolbox.asHex(status));
        }

        unbind();
    }

}
