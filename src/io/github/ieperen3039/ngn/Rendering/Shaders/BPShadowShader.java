package io.github.ieperen3039.ngn.Rendering.Shaders;

import java.io.IOException;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Core.Main.ViewPort;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SceneShaderGL;
import io.github.ieperen3039.ngn.Rendering.Textures.GenericTextures;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.*;

public class BPShadowShader extends SceneShader implements ShadowLightShader, TextureShader {
    private static final Resource.Path BP_SHADOW_PATH = SHADER_DIRECTORY.resolve("BPShadow");
    private static final Resource.Path VERTEX_PATH = BP_SHADOW_PATH.resolve("vertex.glsl");
    private static final Resource.Path FRAGMENT_PATH = BP_SHADOW_PATH.resolve("fragment.glsl");
    private static final int MAX_POINT_LIGHTS = 16;
    private static final float SPECULAR_POWER = 1f;

    private int nextPointLightIndex = 0;

    /**
     * @throws ShaderException if a new shader could not be created by some opengl reason
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public BPShadowShader() throws ShaderException, IOException {
        super(VERTEX_PATH, null, FRAGMENT_PATH);

        uniforms.createUniform("material.diffuse");
        uniforms.createUniform("material.specular");
        uniforms.createUniform("material.reflectance");

        createLightsUniform("pointLights", MAX_POINT_LIGHTS);
        uniforms.createUniform("directionalLight.color");
        uniforms.createUniform("directionalLight.mDirection");
        uniforms.createUniform("directionalLight.intensity");
        uniforms.createUniform("directionalLight.lightSpaceMatrix");
        uniforms.createUniform("dynamicShadowMap");

        uniforms.createUniform("texture_sampler");
        uniforms.createUniform("hasTexture");
        uniforms.createUniform("doDrawHeightLines");

        uniforms.createUniform("ambientLight");
        uniforms.createUniform("specularPower");
        uniforms.createUniform("cameraPosition");
    }

    protected void initialize(Vector3fc cameraPosition, Settings settings) {
        uniforms.setUniform("ambientLight", settings.AMBIENT_LIGHT.toVector3f());
        uniforms.setUniform("cameraPosition", cameraPosition);
        uniforms.setUniform("specularPower", SPECULAR_POWER);

        uniforms.setUniform("hasTexture", false);
        uniforms.setUniform("doDrawHeightLines", false);

        // Texture for the model
        uniforms.setUniform("texture_sampler", 0);
        GenericTextures.CHECKER.attach(GL_TEXTURE0);

        uniforms.setUniform("dynamicShadowMap", 1);

        nextPointLightIndex = 0;
        discardRemainingLights();
        nextPointLightIndex = 0;
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
        // ignore
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
            tex.attach(GL_TEXTURE0);

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
    }

    @Override
    public SGL getGL(Main main) {
        return getGL(main.camera(), main.getViewPort(), main.settings());
    }

    public SGL getGL(Camera camera, ViewPort viewPort, Settings settings) {
        glViewport(viewPort.x(), viewPort.y(), viewPort.width(), viewPort.height() );
        initialize(camera.getEye(), settings);
        return new SceneShaderGL(this, camera, viewPort);
    }

    @Override
    public void setLightWithShadows(Matrix4fc lightSpace, Vector3fc direction, Color4f color, float intensity, Texture shadowMap) {
        shadowMap.attach(GL_TEXTURE1);
        
        uniforms.setUniform("hasTexture", false);
        uniforms.setUniform("directionalLight.color", color.toVector3f()); // vec3
        uniforms.setUniform("directionalLight.mDirection", direction); // vec3
        uniforms.setUniform("directionalLight.intensity", intensity); // float
        uniforms.setUniform("directionalLight.lightSpaceMatrix", lightSpace); // mat4
    }
}
