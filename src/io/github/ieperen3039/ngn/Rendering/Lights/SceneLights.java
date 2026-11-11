package io.github.ieperen3039.ngn.Rendering.Lights;

import io.github.ieperen3039.ngn.Core.ToolElement;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Tools.TickTime;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public interface SceneLights extends ToolElement {
    /**
     * update the lights after entity position updates
     */
    void update();

    /**
     * initializes the lights on the scene.
     * @param gl the current gl object
     */
    void draw(SGL gl, TickTime time);

    /**
     * adds a point-light to the simulation.
     * @param position world-space position of light source
     * @param color     the color of the light
     * @param intensity the brightness
     */
    default void addPointLight(Vector3fc position, Color4f color, float intensity) {
        addPointLight(new FixedPointLight(position, color, intensity));
    }

    void addPointLight(PointLight light);

    /**
     * set the parameters of the one infinitely-far light source of the scene.
     * @param vectorToLight    a vector TO the light source
     * @param color     the color of the light
     * @param intensity the brightness
     */
    
    default void addDirectionalLight(Vector3fc vectorToLight, Color4f color, float intensity) {
        addDirectionalLight(new FixedDirectionalLight(vectorToLight, color, intensity));
    }

    void addDirectionalLight(DirectionalLight light);

    /** remove all lights from the scene */
    void clear();
}
