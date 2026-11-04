package io.github.ieperen3039.ngn.Rendering.Lights;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.RenderTargets.DepthRenderTarget;
import io.github.ieperen3039.ngn.Rendering.Shaders.LightDepthShader;
import io.github.ieperen3039.ngn.Rendering.Shaders.LightShader;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShadowLightShader;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import io.github.ieperen3039.ngn.Tools.Vectors;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.lwjgl.opengl.GL11.*;

/**
 * Handles pointlights, and one directional light source with shadows
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class SingleShadowMapLights implements SceneLights, Serializable {
    private static final float LIGHT_BOX_MARGIN = 5f;
    private static final float LIGHT_Z_NEAR = 0.5f;
    private static final float LIGHT_Z_FAR_MULTIPLIER = 2;
    private static final float LIGHT_CUBE_SIZE_MULTIPLIER = 2;

    private final List<PointLight> lights = new ArrayList<>();
    // this is overridden by #addDirectionalLight
    private DirectionalLight sunLight = null;

    private LightDepthShader shadowShader;
    private DepthRenderTarget renderTarget;
    private Main main;

    // if the camera moves too much, we must re-render the shadow map.
    // the detection sets the dirty flag, updating the light map clears it.
    private boolean dirty = true;
    private float lightDist = 1;
    private Vector3fc lightFocus = new Vector3f(0, 0, 0);
    private Matrix4fc lightSpaceMatrix = new Matrix4f();

    public void init(Main main) throws Exception {
        this.main = main;
        Future<LightDepthShader> futureShadowShader = main.computeOnRenderThread(LightDepthShader::new);
        // todo settings for DRT size
        Future<DepthRenderTarget> futureRenderTarget = main.computeOnRenderThread(() -> new DepthRenderTarget(1024, 1024));

        this.shadowShader = futureShadowShader.get();
        this.renderTarget = futureRenderTarget.get();
        shadowShader.setRenderTarget(renderTarget);
    }

    @Override
    public void addPointLight(PointLight light) {
        synchronized (lights) {
            lights.add(light);
        }
    }

    @Override
    public void addDirectionalLight(DirectionalLight light) {
        if (sunLight != null) throw new RuntimeException("SingleShadowMapLights only supports one directional light total");

        sunLight = light;
    }

    @Override
    public void update() {
        Camera camera = main.camera();
        Vector3fc playerFocus = camera.getFocus();
        float viewDist = camera.vectorToFocus().length();

        if (playerFocus.distanceSquared(lightFocus) > LIGHT_BOX_MARGIN * LIGHT_BOX_MARGIN) {
            dirty = true;

        } else if (Math.abs(viewDist - lightDist) > LIGHT_BOX_MARGIN) {
            dirty = true;
        }

        if (dirty) {
            lightDist = viewDist;
            lightFocus = playerFocus;
            
            float size = viewDist * LIGHT_CUBE_SIZE_MULTIPLIER;
            Vector3f eye = new Vector3f(sunLight.getDirectionToLight()).normalize().mul(size).add(lightFocus);
            lightSpaceMatrix = new Matrix4f()   // new instance: the render thread reads this
                    .setOrthoSymmetric(size, size, LIGHT_Z_NEAR, LIGHT_Z_FAR_MULTIPLIER * size + LIGHT_Z_NEAR)
                    .lookAt(eye, lightFocus, Vectors.Z);    // pick an `up` that isn't parallel to the light
        }
    }

    public void print(String name, int res) {
        glReadBuffer(GL_BACK);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(res * res * bpp);
        GL11.glReadPixels(0, 0, res, res, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        new Thread(() -> Toolbox.writePNG(new File(name), buffer, bpp, res, res), "Writing frame to disc" ).start();
    }

    @Override
    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof ShadowLightShader lightShader) {
            lightShader.setLightWithShadows(lightSpaceMatrix, sunLight.getColor(), sunLight.getIntensity(), renderTarget);
        }
        
        if (shader instanceof LightShader lightShader) {
            synchronized (lights) {
                for (PointLight light : lights) {
                    Vector3fc mPosition = gl.getPosition(light.getPosition());
                    lightShader.setPointLight(mPosition, light.getColor(), light.getIntensity());
                }
            }
        }
    }

    public ShaderProgram shadowPass() {
        shadowShader.setLightSpaceMatrix(lightSpaceMatrix);
        return shadowShader;
    }

    @Override
    public void clear() {
        shadowShader.cleanup();
        sunLight = null;
        lights.clear();
    }
}