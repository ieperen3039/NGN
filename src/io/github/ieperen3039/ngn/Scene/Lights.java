package io.github.ieperen3039.ngn.Scene;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3fc;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.Lights.DirectionalLight;
import io.github.ieperen3039.ngn.Rendering.Lights.Light;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.Shaders.LightShader;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;

public class Lights {
    // we don't work with point-lights
    private final List<DirectionalLight> lights = new ArrayList<>();

    public void addLight(DirectionalLight light) {
        synchronized (lights) {
            lights.add(light);
        }
    }

    public Light addLight(Vector3fc direction, Color4f color, float intensity) {
        DirectionalLight light = new DirectionalLight(color, direction, intensity);
        addLight(light);
        return light;
    }

    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof LightShader) {
            LightShader lightShader = (LightShader) shader;

            synchronized (lights) {
                for (DirectionalLight light : lights) {
                    lightShader.setDirectionalLight(light.getDirectionToLight(), light.getColor(), light.getIntensity());
                }
            }
        }
    }

    public void clear() {
        synchronized (lights) {
            lights.clear();
        }
    }
}
