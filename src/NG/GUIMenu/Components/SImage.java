package NG.GUIMenu.Components;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import de.matthiasmann.twl.utils.PNGDecoder;
import NG.AssetHandling.Image;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.Rendering.Textures.Texture;

public class SImage extends SComponent {

    private int imageId = -1;
    private int imageWidth;
    private int imageHeight;
    private ByteBuffer imageBufferRGBA;

    public SImage(File imageFile) throws IOException {
        InputStream in = new FileInputStream(imageFile);
        PNGDecoder image = new PNGDecoder(in);

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        // Load texture contents into a byte buffer
        int byteSize = 4;
        imageBufferRGBA = ByteBuffer.allocateDirect(byteSize * imageWidth * imageHeight);
        PNGDecoder.Format format = image.decideTextureFormat(PNGDecoder.Format.RGBA);
        image.decode(imageBufferRGBA, imageWidth * byteSize, format);
        imageBufferRGBA.flip();
    }

    public SImage(Image image) {
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        imageBufferRGBA = image.toByteBufferRGBA();
    }

    public SImage(Texture tex) {
        imageWidth = tex.getWidth();
        imageHeight = tex.getHeight();
        imageBufferRGBA = tex.toByteBufferRGBA();
    }

    @Override
    public int minWidth() {
        return imageWidth;
    }

    @Override
    public int minHeight() {
        return imageHeight;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (imageId == -1) {
            imageId = design.createImage(imageBufferRGBA, imageWidth, imageHeight);
        }

        Vector2ic size = getSize();

        int widthPad = size.x() - imageWidth;
        int heightPad = size.y() - imageHeight;

        // center image in component
        Vector2i paddedScreenPosition =  new Vector2i(screenPosition).add(widthPad / 2, heightPad / 2);
        design.drawImage(paddedScreenPosition, imageWidth, imageHeight, imageId);
    }
    
}
