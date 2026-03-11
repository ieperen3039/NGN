package io.github.ieperen3039.ngn.Rendering.Textures;

import io.github.ieperen3039.ngn.AssetHandling.Asset;
import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.AssetHandling.Resource.Path;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.MeshFile;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public enum GenericTextures implements Texture {
    CHECKER("check.png"),
    GRADIENT("Gradient.png"),
    ;

    private final Asset<Texture> tex;

    GenericTextures(String... relative) {
        Path path = Resource.Path.get("ngn/images").resolve(relative);
        tex = Texture.createAsset(path);
    }

    @Override
    public void attach(int sampler) {
        tex.get().attach(sampler);
    }

    @Override
    public void cleanup() {
        tex.drop();
    }

    @Override
    public int getWidth() {
        return tex.get().getWidth();
    }

    @Override
    public int getHeight() {
        return tex.get().getHeight();
    }
}
