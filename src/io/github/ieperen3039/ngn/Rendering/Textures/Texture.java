package io.github.ieperen3039.ngn.Rendering.Textures;

import io.github.ieperen3039.ngn.AssetHandling.Asset;
import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Tools.Toolbox;

import java.io.File;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public interface Texture {
    /**
     * activate this texture to be applied on the next model
     * @param sampler the texture slot to bind to
     */
    void attach(int sampler);

    /** destroy the resources claimed by the texture */
    void cleanup();

    int getWidth();

    int getHeight();

    /** @return the contents of this texture in RGBA format, top row first */
    default ByteBuffer toByteBufferRGBA() {
        attach(GL_TEXTURE0);
        int rowBytes = getWidth() * 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(rowBytes * getHeight());
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        Toolbox.checkGLError("toByteBufferRGBA");
        return Toolbox.flipRows(buffer, rowBytes, rowBytes, getHeight());
    }

    default void dump(String fileName) {
        ByteBuffer buffer = toByteBufferRGBA();
        Toolbox.writePNG(new File(fileName), buffer, 4, getWidth(), getHeight());
        glBindTexture(GL_TEXTURE_2D, 0);

        Toolbox.checkGLError("texture dump");
    }

    static Asset<Texture> createAsset(Resource.Path path) {
        return Resource.get(p -> new FileTexture(p.asStream()), path);
    }
}
