package io.github.ieperen3039.ngn.AssetHandling;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;
import io.github.ieperen3039.ngn.Rendering.Textures.TextureFromBlob;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_RGBA;

public class Image {
    private int imageWidth;
    private int imageHeight;
    // number of _pixels_ in one row of the image data, may include padding
    private int imageWidthStep;
    private int bytesPerPixel;

    private byte[] imageBytes;

    public Image(int imageWidth, int imageHeight, int imageWidthStep, int bytesPerPixel, byte[] imageBytes) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageWidthStep = imageWidthStep;
        this.bytesPerPixel = bytesPerPixel;
        this.imageBytes = imageBytes;
    }

    public Image(int imageWidth, int imageHeight, int imageWidthStep, byte[] imageBytes) {
        this(imageWidth, imageHeight, imageWidthStep, 1, imageBytes);
    }

    public Image(Texture tex) {
        this(tex.getWidth(), tex.getHeight(), tex.getWidth(), 4, tex.toByteBufferRGBA().array());
    }

    // construct an empty image of the given size
    public Image(int imageWidth, int imageHeight, int bytesPerPixel) {
        this(imageWidth, imageHeight, imageWidth, bytesPerPixel, new byte[imageWidth * imageHeight * bytesPerPixel]);
    }

    public static Image merge(Image red, Image green, Image blue) {
        assert red.bytesPerPixel == 1;
        assert green.bytesPerPixel == 1;
        assert blue.bytesPerPixel == 1;
        assert red.imageWidth == blue.imageWidth;
        assert red.imageWidth == green.imageWidth;
        assert red.imageHeight == blue.imageHeight;
        assert red.imageHeight == green.imageHeight;

        int bytesPerPixel = 3;
        Image mergedImage = new Image(red.imageWidth, red.imageHeight, bytesPerPixel);

        int i = 0;
        for (int y = 0; y < mergedImage.imageHeight; y++) {
            for (int x = 0; x < mergedImage.imageWidth; x++) {
                mergedImage.imageBytes[i++] = red.getByteUnchecked(x, y, 0);
                mergedImage.imageBytes[i++] = green.getByteUnchecked(x, y, 0);
                mergedImage.imageBytes[i++] = blue.getByteUnchecked(x, y, 0);
            }
        }

        return mergedImage;
    }

    public int getWidth() {
        return imageWidth;
    }

    public int getHeight() {
        return imageHeight;
    }

    public Color4f getColor(int x, int y) {
        check(x, y);

        int startByte = (y * imageWidthStep + x) * bytesPerPixel;

        switch (bytesPerPixel) {
            case 1:
                byte pixelValue = imageBytes[startByte];
                return Color4f.rgb(pixelValue, pixelValue, pixelValue);

            case 3:
                return Color4f.rgb(imageBytes[startByte], imageBytes[startByte + 1], imageBytes[startByte + 2]);

            case 4:
                float alpha = imageBytes[startByte + 3] / 255.0f;
                return Color4f.rgb(imageBytes[startByte], imageBytes[startByte + 1], imageBytes[startByte + 2], alpha);

            default:
                throw new UnsupportedOperationException(bytesPerPixel + " bytes per pixel is not supported");
        }
    }

    /** returns an array with all bytes belonging to pixel (x, y) */
    public byte[] getBytes(int x, int y) {
        check(x, y);

        int startByte = (y * imageWidthStep + x) * bytesPerPixel;

        // collect the bytes of one pixel
        byte[] result = new byte[bytesPerPixel];
        System.arraycopy(imageBytes, startByte, result, 0, bytesPerPixel);

        return result;
    }

    /** returns from pixel (x, y) the byte on index idx. */
    public byte getByte(int x, int y, int idx) {
        check(x, y);

        if (idx >= bytesPerPixel)
            throw new IndexOutOfBoundsException("Requested byte index " + idx + " in an image containing " + bytesPerPixel + " bytes per pixel");

        return getByteUnchecked(x, y, idx);
    }

    public byte getByteUnchecked(int x, int y, int idx) {
        int startByte = (y * imageWidthStep + x) * bytesPerPixel;
        return imageBytes[startByte + idx];
    }

    /**
     * returns this image as an RGBA bytebuffer of size {@link #getWidth()} * {@link #getHeight()} * 4
     */
    public ByteBuffer toByteBufferRGBA() {
        if (imageWidth == imageWidthStep && bytesPerPixel == 4) {
            return ByteBuffer.wrap(imageBytes);
        }

        // need to convert
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int startByte = (y * imageWidthStep + x) * bytesPerPixel;

                switch (bytesPerPixel) {
                    case 1:
                        byte pixelValue = imageBytes[startByte];
                        newBuffer.put(pixelValue);
                        newBuffer.put(pixelValue);
                        newBuffer.put(pixelValue);
                        newBuffer.put((byte) 255);
                        break;
        
                    case 3:
                        newBuffer.put(imageBytes[startByte]); 
                        newBuffer.put(imageBytes[startByte + 1]); 
                        newBuffer.put(imageBytes[startByte + 2]);
                        newBuffer.put((byte) 255);
                        break;
        
                    case 4:
                        newBuffer.put(imageBytes[startByte]);
                        newBuffer.put(imageBytes[startByte + 1]);
                        newBuffer.put(imageBytes[startByte + 2]);
                        newBuffer.put(imageBytes[startByte + 3]);
                        break;
        
                    default:
                        throw new UnsupportedOperationException(bytesPerPixel + " bytes per pixel is not supported");
                }
            }
        }

        newBuffer.flip();

        return newBuffer;
    }

    public Texture toTexture() {
        /* 
        * // This creates an EXCEPTION_ACCESS_VIOLATION for RGB images
        * int format = switch (bytesPerPixel) {
        *     case 1 -> GL_RED;
        *     case 3 -> GL_RGB;
        *     case 4 -> GL_RGBA;
        *     default -> throw new UnsupportedOperationException(bytesPerPixel + " bytes per pixel is not supported");
        * };
        * return new TextureFromBlob(imageWidth, imageHeight, imageWidthStep, format, ByteBuffer.wrap(imageBytes));
        */

        return new TextureFromBlob(imageWidth, imageHeight, imageWidth, GL_RGBA, toByteBufferRGBA());
    }

    private void check(int x, int y) throws IndexOutOfBoundsException {
        if (x < 0 || x > imageWidth) {
            throw new IndexOutOfBoundsException(
                    "x position of " + x + " is out of bounds for image of size " + imageWidth + " by " + imageHeight);
        }
        if (y < 0 || y > imageHeight) {
            throw new IndexOutOfBoundsException(
                    "y position of " + y + " is out of bounds for image of size " + imageWidth + " by " + imageHeight);
        }
    }
}
