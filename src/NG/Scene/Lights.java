package NG.Scene;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3fc;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Lights.DirectionalLight;
import NG.Rendering.Lights.Light;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.LightShader;
import NG.Rendering.Shaders.ShaderProgram;

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
