package io.github.ieperen3039.ngn.Rendering.Shaders;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SceneShaderGL;
import io.github.ieperen3039.ngn.Rendering.Textures.GenericTextures;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;

import static org.lwjgl.opengl.GL13.*;

/**
 * A shader that uses a shadow-map and a Blinn-Phong model for lighting
 * @author Geert van Ieperen
 */
@SuppressWarnings("Duplicates")
public class BlinnPhongShader extends SceneShader implements TextureShader {
    private static final Resource.Path BLINN_PHONG_PATH = SHADER_DIRECTORY.resolve("BlinnPhong");
    private static final Resource.Path VERTEX_PATH = BLINN_PHONG_PATH.resolve("vertex.glsl");
    private static final Resource.Path FRAGMENT_PATH = BLINN_PHONG_PATH.resolve("fragment.glsl");
    private static final int MAX_POINT_LIGHTS = 2;
    private static final int MAX_DIRECTIONAL_LIGHTS = 8;
    private static final float SPECULAR_POWER = 1f;

    private int nextPointLightIndex = 0;
    private int nextDirectionalLightIndex = 0;

    /**
     * @throws ShaderException if a new shader could not be created by some opengl reason
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public BlinnPhongShader() throws ShaderException, IOException {
        super(VERTEX_PATH, null, FRAGMENT_PATH);

        uniforms.createUniform("material.diffuse");
        uniforms.createUniform("material.specular");
        uniforms.createUniform("material.reflectance");

        createLightsUniform("directionalLights", MAX_DIRECTIONAL_LIGHTS);
        createLightsUniform("pointLights", MAX_POINT_LIGHTS);

        uniforms.createUniform("texture_sampler");

        uniforms.createUniform("ambientLight");
        uniforms.createUniform("specularPower");
        uniforms.createUniform("cameraPosition");

        uniforms.createUniform("hasTexture");
        uniforms.createUniform("renderToBuffer");
    }

    protected void initialize(Vector3fc cameraPosition, Settings settings) {
        uniforms.setUniform("ambientLight", settings.AMBIENT_LIGHT.toVector3f());
        uniforms.setUniform("cameraPosition", cameraPosition);
        uniforms.setUniform("specularPower", SPECULAR_POWER);

        uniforms.setUniform("hasTexture", false);
        uniforms.setUniform("renderToBuffer", false);

        // Texture for the model
        uniforms.setUniform("texture_sampler", 0);

        GenericTextures.CHECKER.bind(GL_TEXTURE0);
        GenericTextures.CHECKER.bind(GL_TEXTURE1);
        GenericTextures.CHECKER.bind(GL_TEXTURE2);

        nextPointLightIndex = 0;
        nextDirectionalLightIndex = 0;
        discardRemainingLights();
        nextPointLightIndex = 0;
        nextDirectionalLightIndex = 0;
    }

    @Override
    public void setPointLight(Vector3fc position, Color4f color, float intensity) {
        int lightNumber = nextPointLightIndex++;
        if (lightNumber >= MAX_POINT_LIGHTS) {
            Logger.ERROR.print("Too many point lights: ", lightNumber, "/", MAX_POINT_LIGHTS);
            return;
        }
        setLightsUniform("pointLights", lightNumber, color, position, intensity);
    }

    @Override
    public void setDirectionalLight(Vector3fc direction, Color4f color, float intensity) {
        int lightNumber = nextDirectionalLightIndex++;
        if (lightNumber >= MAX_DIRECTIONAL_LIGHTS) {
            Logger.ERROR.print("Too many directional lights: ", lightNumber, "/", MAX_DIRECTIONAL_LIGHTS);
            return;
        }
        setLightsUniform("directionalLights", lightNumber, color, direction.mul(-1, new Vector3f()), intensity);
    }

    @Override
    public void setMaterial(Color4f diffuse, Color4f specular, float reflectance) {
        uniforms.setUniform("hasTexture", false);
        uniforms.setUniform("material.diffuse", diffuse);
        uniforms.setUniform("material.specular", specular);
        uniforms.setUniform("material.reflectance", reflectance);
    }

    @Override
    public void setTexture(Texture tex) {
        if (tex != null) {
            uniforms.setUniform("hasTexture", true);
            tex.bind(GL_TEXTURE0);

        } else {
            unsetTexture();
        }
    }

    @Override
    public void unsetTexture() {
        glBindTexture(GL_TEXTURE_2D, 0);
        uniforms.setUniform("hasTexture", false);
    }

    @Override
    public void discardRemainingLights() {
        while (nextPointLightIndex < MAX_POINT_LIGHTS) {
            setPointLight(new Vector3f(), Color4f.INVISIBLE, 0);
        }
        while (nextDirectionalLightIndex < MAX_DIRECTIONAL_LIGHTS) {
            setDirectionalLight(new Vector3f(), Color4f.INVISIBLE, 0);
        }
    }

    @Override
    public SGL getGL(Main main) {
        Camera camera = main.camera();
        initialize(camera.getEye(), main.settings());
        return new SceneShaderGL(this, camera, main.getViewPort());
    }
}
