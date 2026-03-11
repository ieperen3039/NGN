package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL30.*;

public class RenderTarget {
    protected final int id;

    public RenderTarget(int id) {
        this.id = id;
    }

    static RenderTarget screen() {
        return new RenderTarget(0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void cleanup() {
        // glDeleteFramebuffers: zero is reserved by the GL and is silently ignored, should it occur in framebuffers, as are other unused names
        glDeleteFramebuffers(id);
    }
}
