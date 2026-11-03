package io.github.ieperen3039.ngn.Rendering.Lights;

import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.Shaders.LightShader;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;

import java.util.ArrayList;
import java.util.List;

public class SimpleLights implements SceneLights {
    private final List<DirectionalLight> directionLights = new ArrayList<>();
    private final List<PointLight> pointLights = new ArrayList<>();

    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof LightShader lightShader) {
            synchronized (directionLights) {
                for (DirectionalLight light : directionLights) {
                    lightShader.setDirectionalLight(light.getDirectionToLight(), light.getColor(),
                            light.getIntensity());
                }
            }
            synchronized (pointLights) {
                for (PointLight light : pointLights) {
                    lightShader.setPointLight(light.getPosition(), light.getColor(), light.getIntensity());
                }
            }
        }
    }

    @Override
    public void addPointLight(PointLight light) {
        synchronized (pointLights) {
            pointLights.add(light);
        }
    }

    @Override
    public void addDirectionalLight(DirectionalLight light) {
        synchronized (directionLights) {
            directionLights.add(light);
        }
    }
    
    @Override
    public void update() {
    }

    public void clear() {
        synchronized (directionLights) {
            directionLights.clear();
        }
        synchronized (pointLights) {
            pointLights.clear();
        }
    }
}
