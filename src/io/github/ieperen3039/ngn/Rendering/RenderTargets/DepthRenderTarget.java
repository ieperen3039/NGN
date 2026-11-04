package io.github.ieperen3039.ngn.Rendering.RenderTargets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderException;
import io.github.ieperen3039.ngn.Tools.Toolbox;

public class DepthRenderTarget extends TextureRenderTarget {
    public DepthRenderTarget(int width, int height) {
        super(width, height);

        bind();

        // Attach a color buffer as the texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textureId, 0);
        
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindTexture(GL_TEXTURE_2D, 0);

        Toolbox.checkGLError(this.toString());
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new ShaderException(this.toString() + " could not init FrameBuffer : error " + Toolbox.asHex(status));
        }

        unbind();
    }
    
    @Override
    public ByteBuffer toByteBufferRGBA() {
        int width = getWidth();
        int height = getHeight();

        attach(GL_TEXTURE0);
        ByteBuffer depth = BufferUtils.createByteBuffer(width * height);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        // GL maps depth [0, 1] to [0, 255]
        glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, depth);
        glBindTexture(GL_TEXTURE_2D, 0);
        Toolbox.checkGLError("DepthRenderTarget read");

        ByteBuffer rgba = ByteBuffer.allocateDirect(width * height * 4);
        for (int i = 0; i < width * height; i++) {
            byte d = depth.get(i);
            rgba.put(d).put(d).put(d).put((byte) 0xFF);
        }
        return rgba.flip();
    }
}
