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
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderException;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShadowLightShader;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import io.github.ieperen3039.ngn.Tools.Vectors;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

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
    private final AtomicReference<Matrix4fc> pendingLightSpaceMatrix = new AtomicReference<>();
    private Matrix4fc trueLightSpaceMatrix = new Matrix4f();
    // this is overridden by #addDirectionalLight
    private DirectionalLight sunLight = null;

    private DepthRenderTarget renderTarget;
    private Main main;

    private float lightDist = 1;
    private Vector3f lightFocus = new Vector3f(0, 0, 0);

    public void init(Main main) throws Exception {
        this.main = main;
        // todo settings for DRT size
        Future<DepthRenderTarget> futureRenderTarget = main.computeOnRenderThread(() -> new DepthRenderTarget(1024, 1024));
        this.renderTarget = futureRenderTarget.get();
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
        boolean shouldUpdate = false;

        if (playerFocus.distanceSquared(lightFocus) > LIGHT_BOX_MARGIN * LIGHT_BOX_MARGIN) {
            shouldUpdate = true;

        } else if (Math.abs(viewDist - lightDist) > LIGHT_BOX_MARGIN) {
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            lightDist = viewDist;
            lightFocus.set(playerFocus);
            
            float size = viewDist * LIGHT_CUBE_SIZE_MULTIPLIER;
            Vector3f eye = new Vector3f(sunLight.getDirectionToLight()).normalize().mul(size).add(lightFocus);
            // must be a new instance: the render thread reads this
            pendingLightSpaceMatrix.set(
                    new Matrix4f() 
                        .setOrthoSymmetric(size, size, LIGHT_Z_NEAR, LIGHT_Z_FAR_MULTIPLIER * size + LIGHT_Z_NEAR)
                        .lookAt(eye, lightFocus, Vectors.Z)
            );
        }
    }

    @Override
    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof ShadowLightShader lightShader) {
            lightShader.setLightWithShadows(trueLightSpaceMatrix, sunLight.getDirectionToLight(), sunLight.getColor(), sunLight.getIntensity(), renderTarget);
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

    public DepthRenderTarget getRenderTarget() {
        return renderTarget;
    }

    /** 
     * returns the light space matrix for rendering. 
     * After this functioncall, this class assumes the shadow map is updated 
     */
    public Matrix4fc loadLightSpaceMatrix() {
        Matrix4fc mat = pendingLightSpaceMatrix.getAndSet(null);
        if (mat != null) trueLightSpaceMatrix = mat;
        return mat;
    }

    /** Creates a shader; must run on the render thread */
    public ShaderProgram createShadowPass() throws ShaderException, IOException {
        return new LightDepthShader(this);
    }

    @Override
    public void clear() {
        sunLight = null;
        lights.clear();
    }
}